package com.example.asanar

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
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

        val backButton = findViewById<Button>(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

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
            // Load the image
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            val bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

            val mpImage = BitmapImageBuilder(bitmap).build()

            // Detect poses
            val result: PoseLandmarkerResult = poseLandmarker.detect(mpImage)

            // Draw landmarks and connections
            val canvas = Canvas(bitmap)
            val paintDot = Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
                strokeWidth = 12f  // larger dot size
            }
            val paintLine = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 6f   // slightly thicker line
            }

            // Standard pose connections (using MediaPipe Pose Landmarks)
            val connections = listOf(
                Pair(11, 12),  // LEFT_SHOULDER -> RIGHT_SHOULDER
                Pair(11, 13),  // LEFT_SHOULDER -> LEFT_ELBOW
                Pair(13, 15),  // LEFT_ELBOW -> LEFT_WRIST
                Pair(12, 14),  // RIGHT_SHOULDER -> RIGHT_ELBOW
                Pair(14, 16),  // RIGHT_ELBOW -> RIGHT_WRIST
                Pair(23, 24),  // LEFT_HIP -> RIGHT_HIP
                Pair(11, 23),  // LEFT_SHOULDER -> LEFT_HIP
                Pair(12, 24),  // RIGHT_SHOULDER -> RIGHT_HIP
                Pair(23, 25),  // LEFT_HIP -> LEFT_KNEE
                Pair(25, 27),  // LEFT_KNEE -> LEFT_ANKLE
                Pair(24, 26),  // RIGHT_HIP -> RIGHT_KNEE
                Pair(26, 28)   // RIGHT_KNEE -> RIGHT_ANKLE
            )
            // Draw landmarks
            result.landmarks().forEach { landmarkList ->
                landmarkList.forEach { landmark ->
                    val x = (landmark.x() * bitmap.width).toFloat()
                    val y = (landmark.y() * bitmap.height).toFloat()
                    canvas.drawCircle(x, y, 12f, paintDot) // bigger red dots
                }

                // Draw connections
                connections.forEach { (startIdx, endIdx) ->
                    val start = landmarkList[startIdx]
                    val end = landmarkList[endIdx]
                    val startX = (start.x() * bitmap.width).toFloat()
                    val startY = (start.y() * bitmap.height).toFloat()
                    val endX = (end.x() * bitmap.width).toFloat()
                    val endY = (end.y() * bitmap.height).toFloat()
                    canvas.drawLine(startX, startY, endX, endY, paintLine)
                }
            }

            // Show the final bitmap with landmarks
            imageView.setImageBitmap(bitmap)

            // Optionally save CSV
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