package com.example.asanar

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import java.io.File

class MainActivity : AppCompatActivity() {


    private lateinit var captureIV : ImageView
    private lateinit var imageUrl : Uri

    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){
        captureIV.setImageURI(null)
        captureIV.setImageURI(imageUrl)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        imageUrl = createImageUri()
        captureIV = findViewById(R.id.captureImageView)
        val captureImgBtn = findViewById<Button>(R.id.camera)
        val text = findViewById<TextView>(R.id.textView4)
        captureImgBtn.setOnClickListener {
            contract.launch(imageUrl)
            text.isVisible = true
        }
    }
    private fun createImageUri():Uri {
        val image = File(filesDir, "camera_photos.png")
        return FileProvider.getUriForFile(this,
            "com.coding.captureimage.FileProvider",
            image)
    }


}