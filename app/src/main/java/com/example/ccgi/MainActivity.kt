package com.example.ccgi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var uploadHint: TextView
    private lateinit var gptResultView: TextView
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    selectedImageUri = uri
                    imageView.setImageURI(uri)
                    Log.d("ImageUriTest", "선택된 이미지 URI: $uri")
                    uploadHint.text = "이미지가 업로드되었습니다."
                    Toast.makeText(this, "이미지 선택됨", Toast.LENGTH_SHORT).show()

                    FirebaseUploader.uploadImageToFirebase(
                        this,
                        uri,
                        onSuccess = { url ->
                            Log.d("FirebaseUpload", "업로드 성공: $url")
                            Toast.makeText(this, "업로드 성공", Toast.LENGTH_SHORT).show()

                            GptApiHelper.callGptWithImageUrl(
                                imageUrl = url,
                                onSuccess = { gptResponse ->
                                    Log.d("GPTSuccess", gptResponse)
                                    runOnUiThread {
                                        gptResultView.text = gptResponse
                                    }

                                    val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                    val studentId = prefs.getString("studentId", null)

                                    if (studentId != null) {
                                        val db = FirebaseFirestore.getInstance()
                                        db.collection("users")
                                            .document(studentId)
                                            .update("face", gptResponse)
                                            .addOnSuccessListener {
                                                Log.d("Firestore", "GPT 결과 저장 성공")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("Firestore", "GPT 결과 저장 실패: ${e.message}")
                                            }
                                    } else {
                                        Log.e("MainActivity", "studentId를 찾을 수 없음")
                                    }
                                },
                                onFailure = { error ->
                                    Log.e("GPTError", error)
                                    runOnUiThread {
                                        gptResultView.text = "GPT 오류: $error"
                                    }
                                }
                            )
                        },
                        onFailure = { error ->
                            Log.e("FirebaseUpload", "업로드 실패: $error")
                            Toast.makeText(this, "업로드 실패: $error", Toast.LENGTH_LONG).show()
                        }
                    )
                } else {
                    Toast.makeText(this, "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val verificationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val verified = result.data?.getBooleanExtra("verified", false) ?: false
            if (verified) {
                val intent = Intent(this, MatchResultActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "위치 인증 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        GptKeyProvider.init(applicationContext)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testFirebaseConnection()

        imageView = findViewById(R.id.img_profile)
        uploadHint = findViewById(R.id.tv_upload_hint)
        gptResultView = findViewById(R.id.tv_result_description)

        imageView.setOnClickListener {
            openImagePicker()
        }

        val matchBtn = findViewById<Button>(R.id.btn_start_matching)
        matchBtn.setOnClickListener {
            val intent = Intent(this, VerificationActivity::class.java)
            verificationLauncher.launch(intent)
        }

        val emailButton = findViewById<Button>(R.id.btn_send_email)
        emailButton.setOnClickListener {
            sendFeedbackEmail()
        }
        val logoutButton = findViewById<Button>(R.id.btn_logout)
        logoutButton.setOnClickListener {
            logout()
        }

    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun sendFeedbackEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:kimmc3423@naver.com")
        }

        try {
            startActivity(Intent.createChooser(intent, "이메일 앱을 선택하세요"))
            Toast.makeText(this, "이메일 앱을 열었습니다. 피드백을 작성해주세요.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "이메일 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }




    private fun testFirebaseConnection() {
        val db = FirebaseFirestore.getInstance()
        val testData = hashMapOf(
            "test" to "connection",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("test")
            .document("connection_test")
            .set(testData)
            .addOnSuccessListener {
                Log.d("FirebaseTest", "파이어베이스 연결 성공!")
                Toast.makeText(this, "파이어베이스 연결 성공!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseTest", "파이어베이스 연결 실패: ${e.message}")
                Toast.makeText(this, "파이어베이스 연결 실패: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun logout() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("studentId").apply() // studentId 삭제

        Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()

        // 로그인 화면으로 이동
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

}
