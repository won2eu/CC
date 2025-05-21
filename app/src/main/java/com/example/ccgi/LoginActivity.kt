package com.example.ccgi

import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginBtn = findViewById<Button>(R.id.btn_login)
        val signupBtn = findViewById<Button>(R.id.btn_signup)

        loginBtn.setOnClickListener {
            // 로그인 성공했다고 가정하고 SharedPreferences에 저장
            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("isLoggedIn", true).apply()

            // MainActivity로 이동
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        signupBtn.setOnClickListener {
            // 실제로는 회원가입 화면으로 넘어가야 함
            // 지금은 일단 로그인과 같은 동작 처리
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}
