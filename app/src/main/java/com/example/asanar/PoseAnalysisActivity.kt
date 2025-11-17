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
import androidx.exifinterface.media.ExifInterface
import android.graphics.Matrix
import android.widget.TextView
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class PoseAnalysisActivity : AppCompatActivity() {


    private lateinit var poseLandmarker: PoseLandmarker
    private lateinit var imageView: ImageView
    private lateinit var predictionText: TextView
    private lateinit var tflite: Interpreter
    private val classNames = listOf("tree", "cobra", "warriorII")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        // Load TensorFlow Lite Model
        val tfliteModel = loadModelFile()
        val tfliteOptions = Interpreter.Options()
        tflite = Interpreter(tfliteModel, tfliteOptions)

        imageView = findViewById(R.id.analysisImageView)
        predictionText = findViewById(R.id.predictionText)

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
            val bitmap = loadUprightBitmap(uri)
            imageView.setImageBitmap(bitmap)
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


            // ---- Prepare landmark vector (26 floats: x,y pairs) ----
            val inputVector = FloatArray(26)  // 13 landmarks * 2 coords (X,Y)

            var i = 0
            val landmarkList = result.landmarks()[0]

            val selected = listOf(0,11,12,13,14,15,16,23,24,25,26,27,28)
            selected.forEach { idx ->
                inputVector[i++] = landmarkList[idx].x()
                inputVector[i++] = landmarkList[idx].y()
            }

            // ---- Run model ----
            val output = Array(1) { FloatArray(classNames.size) }
            tflite.run(inputVector, output)

            // ---- Pick highest confidence ----
            val predictedIdx = output[0].indices.maxBy { output[0][it] }
            val predictedPose = classNames[predictedIdx]
            predictionText.text = "Pose: $predictedPose"

            Toast.makeText(this, "Predicted: $predictedPose", Toast.LENGTH_LONG).show()



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

        // Landmarks to keep; face and finger landmarks not needed for pose classifications
        val selectedLandmarks = listOf(
            0,     // Nose
            11, 12, // Shoulders
            13, 14, // Elbows
            15, 16, // Wrists
            23, 24, // Hips
            25, 26, // Knees
            27, 28  // Ankles
        )

        val folder = File(getExternalFilesDir(null), "pose_landmarks")
        if (!folder.exists()) folder.mkdirs()

        val file = File(folder, "pose_${System.currentTimeMillis()}.csv")
        Log.d("PoseSave", "Saved at: ${file.absolutePath}")

        try {
            FileWriter(file).use { writer ->

                // --- Write header row ---
                val header = StringBuilder()
                selectedLandmarks.forEach { idx ->
                    header.append("lm_${idx}_x,lm_${idx}_y,")
                }
                writer.append(header.removeSuffix(",").toString())
                writer.append("\n")

                // --- Write one row of 24 numbers ---
                val row = StringBuilder()
                val landmarkList = result.landmarks()[0] // first person only

                selectedLandmarks.forEach { idx ->
                    val lm = landmarkList[idx]
                    row.append("${lm.x()},${lm.y()},")
                }

                writer.append(row.removeSuffix(",").toString())
            }

            Toast.makeText(
                this,
                "Landmarks saved: ${file.absolutePath}",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save landmarks", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUprightBitmap(uri: Uri): Bitmap {
        val original = contentResolver.openInputStream(uri)!!.use {
            BitmapFactory.decodeStream(it)!!
        }
        val orientation = contentResolver.openInputStream(uri)!!.use {
            ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }

        val m = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90  -> postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL   -> postScale( 1f,-1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> { postRotate(90f);  postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_TRANSVERSE-> { postRotate(270f); postScale(-1f, 1f) }
            }
        }

        val rotated = if (!m.isIdentity)
            Bitmap.createBitmap(original, 0, 0, original.width, original.height, m, true)
        else original

        return rotated.copy(Bitmap.Config.ARGB_8888, true)
    }

    private fun loadModelFile(): ByteBuffer {
        val fileDescriptor = assets.openFd("pose_classifier.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun FloatArray.reshape(rows: Int, cols: Int): Array<FloatArray> {
        return Array(rows) { row ->
            FloatArray(cols) { col -> this[row * cols + col] }
        }
    }
}