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
        val majorSpinner = findViewById<Spinner>(R.id.spinner_major)
        val mbtiSpinner = findViewById<Spinner>(R.id.spinner_mbti)
        val signupBtn = findViewById<Button>(R.id.btn_signup_submit)

        // ✅ Spinner 어댑터 설정 (textColor 검정 적용)
        val genderAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender_array,
            R.layout.spinner_item
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        val majorAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.major_array,
            R.layout.spinner_item
        )
        majorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        majorSpinner.adapter = majorAdapter

        val mbtiAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.mbti_array,
            R.layout.spinner_item
        )
        mbtiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mbtiSpinner.adapter = mbtiAdapter

        signupBtn.setOnClickListener {
            val name = nameEdit.text.toString().trim()
            val studentId = studentIdEdit.text.toString().trim()
            val password = pwEdit.text.toString()
            val gender = genderSpinner.selectedItem.toString()
            val major = majorSpinner.selectedItem.toString()
            val mbti = mbtiSpinner.selectedItem.toString()

            // ✅ 필수 입력 확인
            if (name.isEmpty() || studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ 결과 출력
            Toast.makeText(
                this,
                "$name 님 환영합니다!\n학번: $studentId\n성별: $gender\n전공: $major\nMBTI: $mbti",
                Toast.LENGTH_LONG
            ).show()

            // TODO: 회원가입 로직 추가

            finish()
        }
    }
}
