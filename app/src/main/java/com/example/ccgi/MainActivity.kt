package com.example.ccgi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.btn_start_matching)
        startButton.setOnClickListener {
            val intent = Intent(this, VerificationActivity::class.java)
            startActivity(intent)
        }
    }
}
