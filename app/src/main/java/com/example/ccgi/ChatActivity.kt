package com.example.ccgi

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await  // ← 중요!

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: LinearLayout

    private lateinit var chatId: String
    private lateinit var currentUserId: String
    private val db = FirebaseFirestore.getInstance()

    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter
    private var listenerRegistration: ListenerRegistration? = null

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatId = intent.getStringExtra("chatId") ?: return
        currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getString("studentId", "") ?: ""

        recyclerView = findViewById(R.id.recycler_chat)
        messageInput = findViewById(R.id.et_message)
        sendButton = findViewById(R.id.btn_send)

        adapter = MessageAdapter(messages, currentUserId)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        sendButton.setOnClickListener {
            val content = messageInput.text.toString().trim()
            if (content.isNotEmpty()) {
                coroutineScope.launch {
                    sendMessage(content)
                }
                messageInput.setText("")
            }
        }

        listenForMessages()
    }

    // ✅ suspend로 변경 + await() 사용
    private suspend fun sendMessage(content: String) {
        val message = mapOf(
            "senderId" to currentUserId,
            "content" to content,
            "timestamp" to Timestamp.now()
        )
        try {
            db.collection("chat_messages")
                .document(chatId)
                .collection("messages")
                .add(message)
                .await()  // Firebase 호출을 코루틴으로 기다림
        } catch (e: Exception) {
            e.printStackTrace() // 에러 로그
        }
    }

    private fun listenForMessages() {
        listenerRegistration = db.collection("chat_messages")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    messages.clear()
                    for (doc in snapshots) {
                        val msg = Message(
                            senderId = doc.getString("senderId") ?: "",
                            content = doc.getString("content") ?: "",
                            timestamp = doc.getTimestamp("timestamp")?.toDate()
                        )
                        messages.add(msg)
                    }
                    adapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
        coroutineScope.cancel() // 코루틴 취소
    }
}
