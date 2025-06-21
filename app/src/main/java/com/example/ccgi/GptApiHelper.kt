package com.example.ccgi

import android.content.Context
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Properties



object GptApiHelper {

    // 가장 최근 업로드된 이미지 URL을 찾고 GPT API에 요청
    fun getLatestImageAndCallGpt(
        context: Context,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val storage = Firebase.storage
        val listRef = storage.reference.child("images/")

        // Firebase Storage의 파일 목록 가져오기
        listRef.listAll()
            .addOnSuccessListener { listResult ->
                if (listResult.items.isEmpty()) {
                    onFailure("Storage에 이미지가 없습니다.")
                    return@addOnSuccessListener
                }

                // 최근 업로드 기준: 가장 마지막 파일 (Firebase는 이름 기준 정렬이 아님, 가장 마지막 UUID를 사용)
                val latestRef = listResult.items.last()

                latestRef.downloadUrl
                    .addOnSuccessListener { downloadUrl ->
                        Log.d("GPTRequest", "가장 최근 이미지 URL: $downloadUrl")
                        callGptWithImageUrl(downloadUrl.toString(), onSuccess, onFailure)
                    }
                    .addOnFailureListener {
                        onFailure("다운로드 URL 가져오기 실패: ${it.message}")
                    }
            }
            .addOnFailureListener {
                onFailure("Storage 목록 가져오기 실패: ${it.message}")
            }
    }

    // GPT API에 이미지 URL 요청
    fun callGptWithImageUrl(
        imageUrl: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val apikey = GptKeyProvider.get()
        val prompt = "이 사진을 보고, 얼굴 생김새를 긍정적으로 귀엽고 유쾌하게 3줄로 평가해줘.\n" +
                "\n" +
                "- 무조건 칭찬만 해줘\n" +
                "- 얼굴의 특징(눈, 코, 입, 분위기 등)을 중심으로 말해줘\n" +
                "- 유행어, MZ스러운 말투 살짝 섞어도 좋아\n" +
                "- 너무 과학적이거나 딱딱한 표현은 말고, 자연스럽게!\n" +
                "- 딱 짧은 문장 3개로 간단하게 정리해서!\n"

        val json = JSONObject().apply {
            put("model", "gpt-4o")
            put("max_tokens", 150)
            put("temperature", 0.7)

            put("messages", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("content", JSONArray().apply {
                    put(JSONObject().apply {
                        put("type", "image_url")
                        put("image_url", JSONObject().apply {
                            put("url", imageUrl)
                        })
                    })
                    put(JSONObject().apply {
                        put("type", "text")
                        put("text", prompt)
                    })
                })
            }))
        }

        val client = OkHttpClient()
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apikey")  // 🔑 실제 API 키로 교체
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure("GPT 요청 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()
                if (!response.isSuccessful || bodyStr == null) {
                    onFailure("GPT 응답 오류: ${response.code}")
                    return
                }

                try {
                    val jsonObject = JSONObject(bodyStr)
                    val reply = jsonObject
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    onSuccess(reply)
                } catch (e: Exception) {
                    onFailure("GPT 응답 파싱 실패: ${e.message}")
                }
            }
        })
    }
}
