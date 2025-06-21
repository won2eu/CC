package com.example.ccgi

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var uploadHint: TextView
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                selectedImageUri = uri
                imageView.setImageURI(uri)
                uploadHint.text = "이미지가 업로드되었습니다."
                Toast.makeText(this, "이미지 선택됨", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.img_profile)
        uploadHint = findViewById(R.id.tv_upload_hint)

        imageView.setOnClickListener {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            // Download 폴더로 기본 위치 힌트를 주지만, 모든 기기에서 보장되진 않음
        }
        pickImageLauncher.launch(intent)
    }


}
