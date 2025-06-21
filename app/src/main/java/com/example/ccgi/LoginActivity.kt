package com.example.ccgi

import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginBtn = findViewById<Button>(R.id.btn_login)
        val signupBtn = findViewById<Button>(R.id.btn_signup)
        val studentIdEdit = findViewById<EditText>(R.id.et_student_id)
        val passwordEdit = findViewById<EditText>(R.id.et_password)

        val db = FirebaseFirestore.getInstance()

        loginBtn.setOnClickListener {
            val studentId = studentIdEdit.text.toString().trim()
            val password = passwordEdit.text.toString()

            if (studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "학번과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firestore에서 해당 학번 문서 조회
            db.collection("users")
                .document(studentId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val storedPw = document.getString("password")
                        if (storedPw == password) {
                            // 로그인 성공 → SharedPreferences 저장
                            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                            prefs.edit().putBoolean("isLoggedIn", true).apply()

                            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "존재하지 않는 학번입니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "로그인 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        signupBtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}
