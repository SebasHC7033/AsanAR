package com.example.asanar

import android.content.Intent
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

        val intent = Intent(this, PoseAnalysisActivity::class.java)
        intent.putExtra("imageUri", imageUrl.toString())
        startActivity(intent)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        imageUrl = createImageUri()
        captureIV = findViewById(R.id.captureImageView)
        val captureImgBtn = findViewById<Button>(R.id.camera)
        val homeButton = findViewById<Button>(R.id.homebutton)

        homeButton.setOnClickListener {
            // Finish this activity and return to the previous one
            finish()
        }

        captureImgBtn.setOnClickListener {
            contract.launch(imageUrl)
        }
    }
    private fun createImageUri(): Uri {
        val folder = File(filesDir, "camera_photos").apply { if (!exists()) mkdirs() }
        val image = File(folder, "photo_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            this, "com.coding.captureimage.FileProvider", image
        )
    }



}