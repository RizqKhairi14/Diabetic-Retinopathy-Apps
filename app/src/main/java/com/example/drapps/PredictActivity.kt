package com.example.drapps

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.drapps.ml.TfliteModel
import kotlinx.android.synthetic.main.activity_predict.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class PredictActivity : AppCompatActivity() {

    companion object{
        private const val CAMERA_PERMISSION_CODE = 0
        private const val CAMERA_REQUEST_CODE = 1
        private const val GALLERY_REQUEST_CODE = 2
        var imageBitmap: Bitmap? = null
        var grayBitmap: Bitmap? = null
        var gray: Bitmap? = null
    }


    private lateinit var imageUri: Uri
    private lateinit var currentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predict)

        //load Label for Classification
        val label = "Label.txt"
        val inputString = application.assets.open(label).bufferedReader().use { it.readText() }
        val level = inputString.split("\n")

        Btn_Gallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

        Btn_Camera.setOnClickListener {
            val filename = "photo"
            val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile: File = File.createTempFile(filename, "jpeg", storageDir)
            currentPhotoPath = imageFile.absolutePath

            imageUri = FileProvider.getUriForFile(this, "com.example.drapps.fileprovider", imageFile)
            askCameraPermission(imageUri)
        }

        Btn_Predict.setOnClickListener {

            if(Iv_Image.drawable == null){
                Toast.makeText(this, "Choose your image first", Toast.LENGTH_SHORT).show()
            }else{
                //rescale, resize and convert image to Greyscale
                val grey = rgb2Greyscale(imageBitmap)
                val resize = Bitmap.createScaledBitmap(grey, 256, 256, true)
                val byteBuffer1 = getByteBuffer(resize)

//            var imageProcessor = ImageProcessor.Builder()
//                    .add(QuantizeOp(128.0f, 1/128.0f))
//                    .build()

                //set image
                Iv_Gray.setImageBitmap(resize)

                val model = TfliteModel.newInstance(this)

                // Creates inputs for reference.
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 1), DataType.FLOAT32)

                //create Image buffer
                //var buffer = ByteBuffer.allocateDirect(4 * 256 * 256 * 1)
//            var image = TensorImage(DataType.FLOAT32)
//            image.load(byteBuffer1)
                //var imageBuffer = TensorImage.fromBitmap(resize)
                //val proccesedImage = imageProcessor.process(image)
                //var byteBuffer = image.buffer
                //println("jumlah Byte Buffer = " + byteBuffer.position())
                //println("jumlah Tensor Buffer = " + inputFeature0.buffer.remaining())
                //println("jumlah Allocated Buffer = " + buffer.remaining())
                //buffer = buffer.put(byteBuffer)

//            var compare = byteBuffer.compareTo(inputFeature0.buffer)
//            if (compare == 0)
//                System.out.println("\nboth buffer are lexicographically equal");
//            else if (compare >= 0)
//                System.out.println("\nByteBuffer is lexicographically greater than InputFeature");
//            else
//                System.out.println("\nByteBufferis lexicographically less than InputFeatures");
//
//            System.out.println("Byte Buffer : " + Arrays.toString(byteBuffer.array()))
//            System.out.println("Byte Buffer : " + Arrays.toString(inputFeature0.buffer.array()))

                inputFeature0.loadBuffer(byteBuffer1!!)

                // Runs model inference and gets result.
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer

                // Releases model resources if no longer used.
                model.close()

                //get max number for classification
                var max = getMax(outputFeature0.floatArray)
                when(max){
                    0-> Tv_predict.setText(getString(R.string.Normal))
                    1-> Tv_predict.setText(getString(R.string.Mild))
                    2-> Tv_predict.setText(getString(R.string.Medium))
                    3-> Tv_predict.setText(getString(R.string.Severe))
                    4-> Tv_predict.setText(getString(R.string.Proliferative))
                }
            }

        }

    }


    private fun askCameraPermission(imageUri: Uri?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }else{
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == GALLERY_REQUEST_CODE && data != null){
                imageUri = data.data!!
                imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                Iv_Image.setImageBitmap(imageBitmap)
            }
            if (requestCode == CAMERA_REQUEST_CODE){
                val picture: Bitmap = BitmapFactory.decodeFile(currentPhotoPath)
                imageBitmap = picture
                Iv_Image.setImageBitmap(picture)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Permission Request", "${grantResults[0]} Granted")
                Toast.makeText(this, "Camera permission is granted", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Camera permission is needed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun rgb2Greyscale(src: Bitmap?): Bitmap{
        val redVal = 0.21f
        val greenVal = 0.72f
        val blueVal = 0.07f

        val bmOut = Bitmap.createBitmap(src!!.width, src.height, src.config)
        var A: Int
        var R: Int
        var G: Int
        var B: Int
        var pixel: Int
        val width = src.width
        val height = src.height

        for (x in 0 until width){
            for (y in 0 until height){
                pixel = src.getPixel(x, y)
                A = Color.alpha(pixel)
                R = Color.red(pixel)
                G = Color.green(pixel)
                B = Color.blue(pixel)

                R = (redVal * R + greenVal * G + blueVal * B).toInt()
                G = R
                B = R

                bmOut.setPixel(x, y, Color.argb(A, R, G, B))
            }
        }
        return bmOut
    }

    private fun getByteBuffer(bitmap: Bitmap): ByteBuffer? {
        val width = bitmap.width
        val height = bitmap.height
        val mImgData = ByteBuffer
            .allocateDirect(4 * width * height)
        mImgData.order(ByteOrder.nativeOrder())
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        for (pixel in pixels) {
            mImgData.putFloat(Color.red(pixel).toFloat())
        }
        return mImgData
    }

    private fun getMax(arr: FloatArray) : Int{
        var index = 0
        var min = 0.0f

        for (i in 0..4){
            if (arr[i] > min){
                index = i
                min = arr[i]
            }
        }
        return index
    }

}