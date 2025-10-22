package com.example.asanar

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.io.File
import java.io.FileWriter
import java.io.IOException

class PoseAnalysisActivity : AppCompatActivity() {


    private lateinit var poseLandmarker: PoseLandmarker
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        imageView = findViewById(R.id.analysisImageView)

        //debugging code. if image not sent, say and close. if image unopen able, say and close.

        val imageUriString = intent.getStringExtra("imageUri")
        if (imageUriString == null) {
            Toast.makeText(this, "No image URI provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val imageUri = Uri.parse(imageUriString)
        Log.d("PoseAnalysisActivity", "Received URI: $imageUri")

        val inputStream = contentResolver.openInputStream(imageUri)
        if (inputStream == null) {
            Toast.makeText(this, "Cannot open image", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupPoseLandmarker()
        analyzePose(imageUri)
    }

    private fun setupPoseLandmarker() {


        try {
            val assetList = assets.list("")  // lists all files in assets/
            Log.d("Assets", "Files in assets: ${assetList?.joinToString()}")
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to list assets", Toast.LENGTH_SHORT).show()
        }

        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("pose_landmarker_full.task") // must be in assets folder
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE) // single image
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(this, options)
    }

    private fun analyzePose(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val mpImage = BitmapImageBuilder(bitmap).build()

            val result: PoseLandmarkerResult = poseLandmarker.detect(mpImage)

            // Show the image in the ImageView
            imageView.setImageBitmap(bitmap)

            // Save the landmarks to a CSV file
            saveLandmarks(result)

            Toast.makeText(
                this,
                "Detected ${result.landmarks().size} poses",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error analyzing pose: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveLandmarks(result: PoseLandmarkerResult) {
        val folder = File(filesDir, "pose_landmarks")
        if (!folder.exists()) folder.mkdirs()

        val file = File(folder, "pose_${System.currentTimeMillis()}.csv")

        try {
            FileWriter(file).use { writer ->
                // Loop through each pose
                result.landmarks().forEachIndexed { poseIndex, landmarkList ->
                    landmarkList.forEachIndexed { landmarkIndex, landmark ->
                        writer.append("${poseIndex},${landmarkIndex},${landmark.x()},${landmark.y()},${landmark.z()}\n")
                    }
                }
            }
            Toast.makeText(this, "Landmarks saved: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save landmarks", Toast.LENGTH_SHORT).show()
        }
    }
}