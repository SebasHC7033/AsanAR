package com.example.asanar

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.media.MediaPlayer
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class loading_Splashpage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loading_splashpage)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val logo = findViewById<ImageView>(R.id.logoImageView)

        // --- Build a fade + scale animation entirely in code ---
        val fadeScale = AnimationSet(true).apply {
            duration = 1500                // total duration in milliseconds
            fillAfter = true               // keeps the final state visible
            addAnimation(AlphaAnimation(0f, 1f))  // fade in from transparent to visible
            addAnimation(
                ScaleAnimation(
                    0.8f, 1f,   // scale X from 80% → 100%
                    0.8f, 1f,   // scale Y from 80% → 100%
                    Animation.RELATIVE_TO_SELF, 0.5f, // pivot X center
                    Animation.RELATIVE_TO_SELF, 0.5f  // pivot Y center
                )
            )
        }

        //Play sound effect
        val mediaPlayer = MediaPlayer.create(this, R.raw.naturesound)
        mediaPlayer.start()

        // Start the animation
        logo.startAnimation(fadeScale)

        // Delay 3 seconds then go to stop sound effect and go to Main activity
        Handler(Looper.getMainLooper()).postDelayed({
            mediaPlayer.stop()
            mediaPlayer.release()

            startActivity(Intent(this, HomePage::class.java))
            finish()
        }, 3000)
    }
}