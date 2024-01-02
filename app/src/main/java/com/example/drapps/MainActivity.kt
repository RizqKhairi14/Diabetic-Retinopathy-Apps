package com.example.drapps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {

    private var tag = "Main Activity"
    private fun check(){
        if(OpenCVLoader.initDebug()){
            Log.d(tag, "OpenCV has been loaded successfully")
        }else{
            Log.d(tag, "Failed to load OpenCV")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        check()

        Tv_Disclaimer

        Tv_Intro

        Btn_Try.setOnClickListener {
            val predict = Intent(this, PredictActivity::class.java)
            startActivity(predict)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.about, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onClick(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    private fun onClick(selected: Int){
        when(selected){
            R.id.About->{
                title = "About"
                showAboutLayout()
            }
        }
    }

    private fun showAboutLayout() {
        val move = Intent(this, About::class.java)
        startActivity(move)
    }
}