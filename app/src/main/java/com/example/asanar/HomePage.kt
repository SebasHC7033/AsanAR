package com.example.asanar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button


class HomePage : AppCompatActivity(){
    private lateinit var button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.button2)
        val exitButton = findViewById<Button>(R.id.exitbutton)

        exitButton.setOnClickListener {
            finish()
        }

        button.setOnClickListener{
            val intent = Intent(this@HomePage, MainActivity::class.java)
            startActivity(intent)
        }
    }










}