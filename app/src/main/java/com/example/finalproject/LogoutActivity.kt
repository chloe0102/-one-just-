package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LogoutActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logout_activity)

        // 初始化 FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // 設定返回按鈕
        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish() // 返回上一頁
        }

        // 設定登出按鈕
        val logoutButton: Button = findViewById(R.id.btn_logout)
        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        auth.signOut()
        Toast.makeText(this, "已成功登出", Toast.LENGTH_SHORT).show()

        // 跳轉到登入頁面
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // 關閉當前活動
    }
}
