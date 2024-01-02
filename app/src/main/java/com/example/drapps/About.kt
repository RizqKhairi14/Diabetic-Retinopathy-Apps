package com.example.drapps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_about.*

class About : AppCompatActivity() {

    private lateinit var textLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        supportActionBar?.title = "About"

        Civ_Photo
        Tv_Name
        Tv_Info
        setUpHyperlink()
    }

    private fun setUpHyperlink() {
        textLink = findViewById(R.id.Tv_Hyperlink)
        textLink.movementMethod = LinkMovementMethod.getInstance()
    }
}