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

                    // 🔽 Firebase Storage에 업로드
                    FirebaseUploader.uploadImageToFirebase(
                        this,
                        uri,
                        onSuccess = { url ->
                            Log.d("FirebaseUpload", "업로드 성공: $url")
                            Toast.makeText(this, "업로드 성공", Toast.LENGTH_SHORT).show()

                            // 🔽 GPT 요청
                            GptApiHelper.callGptWithImageUrl(
                                imageUrl = url,
                                onSuccess = { gptResponse ->
                                    Log.d("GPTSuccess", gptResponse)
                                    runOnUiThread {
                                        gptResultView.text = gptResponse
                                    }

                                    // 🔽 로그인한 studentId 가져오기
                                    val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                    val studentId = prefs.getString("studentId", null)

                                    if (studentId != null) {
                                        // 🔽 결과를 users/{studentId} 문서에 "face" 필드로 저장
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

    override fun onCreate(savedInstanceState: Bundle?) {
        GptKeyProvider.init(applicationContext)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.img_profile)
        uploadHint = findViewById(R.id.tv_upload_hint)
        gptResultView = findViewById(R.id.tv_result_description)

        imageView.setOnClickListener {
            openImagePicker()
        }

        val matchBtn = findViewById<Button>(R.id.btn_start_matching)
        matchBtn.setOnClickListener {
            val intent = Intent(this, MatchResultActivity::class.java)
            startActivity(intent)
        }

    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }
}
