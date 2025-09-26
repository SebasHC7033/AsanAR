package com.example.asanar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button


class Main : AppCompatActivity(){
    private lateinit var button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.button2)


        button.setOnClickListener{
            val intent = Intent(this@Main, MainActivity::class.java)
            startActivity(intent)
        }
    }










}