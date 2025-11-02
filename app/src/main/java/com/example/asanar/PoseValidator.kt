package com.example.asanar

import kotlin.math.atan2

data class Point2D(val x: Float, val y: Float)

data class AngleRange(
    val minAngle: Float,
    val maxAngle: Float,
    val jointName: String
)

class PoseValidator {

    fun calculateAngle(point1: Point2D, point2: Point2D, point3: Point2D): Float {
        val radians = atan2(point3.y - point2.y, point3.x - point2.x) -
                atan2(point1.y - point2.y, point1.x - point2.x)
        var angle = Math.toDegrees(radians.toDouble()).toFloat()

        if (angle < 0) {
            angle += 360f
        }

        if (angle > 180f) {
            angle = 360f - angle
        }

        return angle
    }

    fun extractJointAngles(landmarks: List<Point2D>): Map<String, Float> {
        val angles = mutableMapOf<String, Float>()

        if (landmarks.size > 16) {
            angles["Left Shoulder"] = calculateAngle(
                landmarks[11],
                landmarks[13],
                landmarks[15]
            )

            angles["Left Elbow"] = calculateAngle(
                landmarks[11],
                landmarks[13],
                landmarks[15]
            )

            angles["Right Shoulder"] = calculateAngle(
                landmarks[12],
                landmarks[14],
                landmarks[16]
            )

            angles["Right Elbow"] = calculateAngle(
                landmarks[12],
                landmarks[14],
                landmarks[16]
            )
        }

        if (landmarks.size > 28) {
            angles["Left Hip"] = calculateAngle(
                landmarks[11],
                landmarks[23],
                landmarks[25]
            )

            angles["Left Knee"] = calculateAngle(
                landmarks[23],
                landmarks[25],
                landmarks[27]
            )

            angles["Right Hip"] = calculateAngle(
                landmarks[12],
                landmarks[24],
                landmarks[26]
            )

            angles["Right Knee"] = calculateAngle(
                landmarks[24],
                landmarks[26],
                landmarks[28]
            )
        }

        return angles
    }
}