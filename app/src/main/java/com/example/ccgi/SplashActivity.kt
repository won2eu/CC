package com.example.ccgi

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Firebase 초기화
        FirebaseApp.initializeApp(this)

        Handler(Looper.getMainLooper()).postDelayed({
            // SharedPreferences로 로그인 상태 확인
            val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

            val intent = if (isLoggedIn) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 1500) // 1.5초 지연
    }
}
