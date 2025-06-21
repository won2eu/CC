package com.example.ccgi

import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.ccgi.ModelInterpreter
import com.google.firebase.firestore.FirebaseFirestore

class MatchResultActivity : AppCompatActivity() {

    private lateinit var nameView: TextView
    private lateinit var mbtiView: TextView
    private lateinit var majorView: TextView
    private lateinit var faceDescView: TextView
    private lateinit var compatView: TextView
    private lateinit var chatButton: Button

    private val db = FirebaseFirestore.getInstance()
    private lateinit var modelInterpreter: ModelInterpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matching)

        nameView = findViewById(R.id.tv_name)
        mbtiView = findViewById(R.id.tv_mbti)
        majorView = findViewById(R.id.tv_major)
        faceDescView = findViewById(R.id.tv_face_desc)
        compatView = findViewById(R.id.tv_compatibility)
        chatButton = findViewById(R.id.btn_start_chat)

        modelInterpreter = ModelInterpreter(this)
        modelInterpreter.loadModel()

        performMatching()

        chatButton.setOnClickListener {
            Toast.makeText(this, "채팅 기능은 추후 추가됩니다!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performMatching() {
        val currentStudentId = getCurrentUserId()
        if (currentStudentId.isEmpty()) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(currentStudentId).get()
            .addOnSuccessListener { currentUser ->
                val myGender = currentUser.getString("sex")
                val myMbti = currentUser.getString("MBTI")
                val myMajor = currentUser.getString("major")

                if (myGender == null || myMbti == null || myMajor == null) {
                    Toast.makeText(this, "내 정보가 부족합니다.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                db.collection("users").get()
                    .addOnSuccessListener { result ->
                        val candidates = result.documents.filter {
                            it.id != currentStudentId && it.getString("sex") != myGender
                        }

                        if (candidates.isEmpty()) {
                            Toast.makeText(this, "매칭 가능한 사용자가 없습니다.", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        val match = candidates.random()
                        displayMatchInfo(match)

                        val matchMbti = match.getString("MBTI") ?: return@addOnSuccessListener
                        val matchMajor = match.getString("major") ?: return@addOnSuccessListener

                        val mbtiEncoded = mapOf(
                            "INFP" to 0, "ENFP" to 1, "INTP" to 2, "ENTP" to 3,
                            "ISFJ" to 4, "ESFJ" to 5, "ISTJ" to 6, "ESTJ" to 7,
                            "ISFP" to 8, "ESFP" to 9, "ISTP" to 10, "ESTP" to 11,
                            "INFJ" to 12, "ENFJ" to 13, "INTJ" to 14, "ENTJ" to 15
                        )

                        val majorEncoded = mapOf(
                            "Engineering" to 0,
                            "Arts" to 1,
                            "Business" to 2,
                            "Social" to 3,
                            "Natural" to 4
                        )

                        val myMbtiIdx = mbtiEncoded[myMbti] ?: 0
                        val myMajorIdx = majorEncoded[myMajor] ?: 0
                        val matchMbtiIdx = mbtiEncoded[matchMbti] ?: 0
                        val matchMajorIdx = majorEncoded[matchMajor] ?: 0

                        val score = modelInterpreter.predict(
                            myMajorIdx, myMbtiIdx,
                            matchMajorIdx, matchMbtiIdx
                        )

                        compatView.text = "잘 맞을 확률: ${"%.1f".format(score * 100)}%"
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "유저 조회 실패", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "내 정보 조회 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayMatchInfo(match: com.google.firebase.firestore.DocumentSnapshot) {
        nameView.text = "이름: ${match.getString("name") ?: "없음"}"
        mbtiView.text = "MBTI: ${match.getString("MBTI") ?: "없음"}"
        majorView.text = "전공: ${match.getString("major") ?: "없음"}"
        faceDescView.text = "외모: ${match.getString("face") ?: "없음"}"
    }

    private fun getCurrentUserId(): String {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("studentId", "") ?: ""
    }
}
