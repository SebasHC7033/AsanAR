package com.example.asanar

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PoseClassifier(context: Context) {

    private val interpreter: Interpreter
    private val labels: List<String>

    init {
        // Load TFLite model
        val model = context.assets.open("pose_classifier.tflite").readBytes()
        val buffer = ByteBuffer.allocateDirect(model.size)
        buffer.order(ByteOrder.nativeOrder())
        buffer.put(model)
        interpreter = Interpreter(buffer)

        // Load labels
        labels = context.assets.open("pose_labels.txt")
            .bufferedReader()
            .readLines()
    }

    fun classify(poseVector: FloatArray): String {
        val input = ByteBuffer.allocateDirect(26 * 4).order(ByteOrder.nativeOrder())
        poseVector.forEach { input.putFloat(it) }

        val output = ByteBuffer.allocateDirect(labels.size * 4)
            .order(ByteOrder.nativeOrder())

        interpreter.run(input, output)

        output.rewind()
        val probabilities = FloatArray(labels.size)
        for (i in probabilities.indices) {
            probabilities[i] = output.float
        }

        val maxIdx = probabilities.indices.maxBy { probabilities[it] }
        return labels[maxIdx]
    }
}