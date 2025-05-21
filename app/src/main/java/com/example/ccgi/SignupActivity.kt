package com.example.ccgi

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val nameEdit = findViewById<EditText>(R.id.et_name)
        val emailEdit = findViewById<EditText>(R.id.et_email)
        val pwEdit = findViewById<EditText>(R.id.et_password)
        val signupBtn = findViewById<Button>(R.id.btn_signup_submit)

        signupBtn.setOnClickListener {
            val name = nameEdit.text.toString()
            val email = emailEdit.text.toString()
            val password = pwEdit.text.toString()

            // 지금은 단순 출력만 하고 실제 회원가입 로직은 생략
            Toast.makeText(this, "$name 님 환영합니다!", Toast.LENGTH_SHORT).show()

            finish() // 회원가입 후 LoginActivity로 돌아감
        }
    }
}
