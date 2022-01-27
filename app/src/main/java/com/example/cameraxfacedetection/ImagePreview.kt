package com.example.cameraxfacedetection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.widget.ImageView
import java.io.FileInputStream
import java.lang.Exception


class ImagePreview : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)

        var bmp: Bitmap? = null
        val filename = intent.getStringExtra("image")
        try {
            val sd: FileInputStream = openFileInput(filename)
            bmp = BitmapFactory.decodeStream(sd)
            sd.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }


        val imagepre = findViewById<ImageView>(R.id.imagepre)
        imagepre.setImageBitmap(bmp)


    }
}