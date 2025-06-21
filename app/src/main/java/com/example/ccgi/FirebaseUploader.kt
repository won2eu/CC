package com.example.ccgi

import android.content.Context
import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.*

object FirebaseUploader {

    fun uploadImageToFirebase(
        context: Context,
        uri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val storage = Firebase.storage
        val filename = "images/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(filename)

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                    onSuccess(downloadUrl.toString())
                }.addOnFailureListener {
                    onFailure("URL 가져오기 실패: ${it.message}")
                }
            }
            .addOnFailureListener {
                onFailure("업로드 실패: ${it.message}")
            }
    }
}
