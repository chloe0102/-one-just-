package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.finalproject.BasicStudyroom
import com.google.firebase.firestore.FirebaseFirestore

class FriendStudyroomCheck : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.friend_studyroom_check)

        //初始化使用者輸入ID、送出按鈕、firebaseStore
        val inputStudyroomId = findViewById<EditText>(R.id.inputStudyroomId)
        val goButton = findViewById<ImageView>(R.id.goButton)
        val db = FirebaseFirestore.getInstance()

        goButton.setOnClickListener {
            val inputText = inputStudyroomId.text.toString()

            // 1. 檢查輸入是否為 8 位數字
            if (inputText.length != 8 || !inputText.matches(Regex("\\d{8}"))) {
                Toast.makeText(this, "ID必須是8位數字，請重新檢查！", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. 查詢 Firebase，檢查該 ID 是否存在
            db.collection("studyrooms").document(inputText).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // 3. 該 ID 存在，進入對應的 FriendStudyroom
                        val intent = Intent(this, FriendStudyroom::class.java)
                        intent.putExtra("studyroomId", inputText) // 傳遞自習室ID到下個頁面
                        startActivity(intent)
                    } else {
                        // 該 ID 不存在
                        Toast.makeText(this, "輸入的自習室ID不存在，請重新輸入！", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    // Firebase 查詢失敗
                    Toast.makeText(this, "查詢自習室失敗：${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        //回上頁
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // 創建自習室
        findViewById<Button>(R.id.createStudyroomButton).setOnClickListener {
            val intent = Intent(this, FriendStudyroom::class.java)
            startActivity(intent)
        }
    }
}