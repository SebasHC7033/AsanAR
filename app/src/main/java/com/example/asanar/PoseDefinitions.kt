package com.example.asanar

object PoseDefinitions {

    val WARRIOR_II = listOf(
        AngleRange(80f, 100f, "Left Knee"),
        AngleRange(170f, 190f, "Right Knee"),
        AngleRange(80f, 100f, "Left Shoulder"),
        AngleRange(80f, 100f, "Right Shoulder"),
        AngleRange(170f, 190f, "Left Elbow"),
        AngleRange(170f, 190f, "Right Elbow"),
        AngleRange(160f, 190f, "Left Hip"),
        AngleRange(160f, 190f, "Right Hip")
    )

    val TREE_POSE = listOf(
        AngleRange(40f, 60f, "Left Hip"),
        AngleRange(80f, 120f, "Left Knee"),
        AngleRange(170f, 190f, "Right Knee"),
        AngleRange(160f, 200f, "Right Hip"),
        AngleRange(80f, 120f, "Left Shoulder"),
        AngleRange(80f, 120f, "Right Shoulder")
    )

    val DOWNWARD_DOG = listOf(
        AngleRange(40f, 70f, "Left Hip"),
        AngleRange(40f, 70f, "Right Hip"),
        AngleRange(160f, 190f, "Left Knee"),
        AngleRange(160f, 190f, "Right Knee"),
        AngleRange(160f, 190f, "Left Elbow"),
        AngleRange(160f, 190f, "Right Elbow"),
        AngleRange(40f, 80f, "Left Shoulder"),
        AngleRange(40f, 80f, "Right Shoulder")
    )

    fun getAllPoses(): Map<String, List<AngleRange>> {
        return mapOf(
            "Warrior II" to WARRIOR_II,
            "Tree Pose" to TREE_POSE,
            "Downward Dog" to DOWNWARD_DOG
        )
    }

    fun getPoseByName(name: String): List<AngleRange>? {
        return getAllPoses()[name]
    }
}