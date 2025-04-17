package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp

class SplashActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private val progressMax = 100
    private val splashDuration = 1000L //3s

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash) //使用待機畫面.xml
        FirebaseApp.initializeApp(this) // 初始化 Firebase

        // 初始化進度條
        progressBar = findViewById(R.id.progressBar)

        // 模擬進度條增加
        simulateProgress()
    }

    private fun simulateProgress() {
        val handler = Handler(Looper.getMainLooper())
        var progress = 0

        val runnable = object : Runnable {
            override fun run() {
                if (progress <= progressMax) {
                    progressBar.progress = progress
                    progress++
                    handler.postDelayed(this, splashDuration / progressMax) // 計算進度條更新間隔
                } else {
                    // 進度完成，跳轉到RegisterActivity
                    navigateToRegister()
                }
            }
        }
        handler.post(runnable)
    }

    // 定義跳轉的方法
    private fun navigateToRegister() {
        val intent = Intent(this, LoginActivity::class.java) // 跳轉到 RegisterActivity
        startActivity(intent)
        finish() // 完成後結束 SplashActivity
    }
}
