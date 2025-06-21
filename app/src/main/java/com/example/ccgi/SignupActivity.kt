package com.example.ccgi

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

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

        // 🔹 Firebase Firestore 인스턴스 생성
        val db = FirebaseFirestore.getInstance()

        signupBtn.setOnClickListener {
            val name = nameEdit.text.toString().trim()
            val studentId = studentIdEdit.text.toString().trim()
            val password = pwEdit.text.toString()
            val gender = genderSpinner.selectedItem.toString()
            val major = majorSpinner.selectedItem.toString()
            val mbti = mbtiSpinner.selectedItem.toString()

            // 🔸 필수 입력값 검사
            if (name.isEmpty() || studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔸 저장할 데이터 생성
            val user = hashMapOf(
                "name" to name,
                "studentId" to studentId,
                "password" to password,
                "sex" to gender,
                "major" to major,
                "MBTI" to mbti
            )

            // 🔸 Firestore "users" 컬렉션에 저장
            db.collection("users")
                .document(studentId) // 문서 ID = 학번
                .set(user)
                .addOnSuccessListener {
                    Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                    finish() // 화면 종료
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "회원가입 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
