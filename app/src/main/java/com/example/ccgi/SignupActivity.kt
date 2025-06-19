package com.example.ccgi

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val nameEdit = findViewById<EditText>(R.id.et_name)
        val studentIdEdit = findViewById<EditText>(R.id.et_student_id)
        val pwEdit = findViewById<EditText>(R.id.et_password)
        val genderSpinner = findViewById<Spinner>(R.id.spinner_gender)
        val signupBtn = findViewById<Button>(R.id.btn_signup_submit)

        signupBtn.setOnClickListener {
            val name = nameEdit.text.toString().trim()
            val studentId = studentIdEdit.text.toString().trim()
            val password = pwEdit.text.toString()
            val gender = genderSpinner.selectedItem.toString()

            // 필수 입력값 확인
            if (name.isEmpty() || studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 간단한 처리 결과
            Toast.makeText(this, "$name 님 환영합니다!\n학번: $studentId\n성별: $gender", Toast.LENGTH_LONG).show()

            // TODO: 회원가입 로직 추가 (예: 서버 전송 등)

            finish() // 회원가입 후 종료
        }
    }
}
