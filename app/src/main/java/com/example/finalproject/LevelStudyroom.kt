package com.example.finalproject

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LevelStudyroom : AppCompatActivity() {

    // 計時器變數
    private lateinit var timerTextView: TextView
    private var seconds = 0
    private var isRunning = true
    private val handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.level_studyroom)

        // 初始化計時器和 Firebase
        timerTextView = findViewById(R.id.timer)
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // 檢查是否已登入
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "請先登入！", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userId = currentUser.uid

        // 將用戶狀態設置為 active
        db.collection("users").document(userId)
            .update("studyRoomStatus", "active")
            .addOnSuccessListener {
                Log.d("LevelStudyroom", "用戶狀態已設置為 active")
            }
            .addOnFailureListener { e ->
                Log.e("LevelStudyroom", "無法設置用戶狀態", e)
                Toast.makeText(this, "進入自習室失敗：${e.message}", Toast.LENGTH_SHORT).show()
            }

        val focusManager = FocusManager(userId, db)
        val startTime = focusManager.startFocus()
        Log.d("LevelStudyroom", "當前用戶 ID: $userId")

        // 啟動計時器
        startTimer()

        // 動態生成用戶頭像布局
        setupUserAvatars(db)

        // 返回自習室選擇頁面
        findViewById<Button>(R.id.finishButton).setOnClickListener {
            stopTimer() // 停止計時器
            updateFirestoreStatusAndLog(focusManager, startTime, userId, db)
        }
    }

    // 動態生成用戶頭像布局
    private fun setupUserAvatars(db: FirebaseFirestore) {
        val userContainer = findViewById<LinearLayout>(R.id.userContainer)

        db.collection("users")
            .whereEqualTo("studyRoomStatus", "active") // 只顯示仍在自習室的用戶
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.w("LevelStudyroom", "沒有用戶在自習室")
                    Toast.makeText(this, "沒有用戶在自習室中", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // 初始化變數
                var userIndex = 0 // 用戶的索引
                var currentRow: LinearLayout? = null // 當前行布局
                val rowContainerParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                for (document in documents) {
                    val characterIdString = document.getString("character")
                    val nickname = document.getString("nickname") ?: "Unknown"
                    val characterId = characterIdString?.toIntOrNull()

                    if (characterId == null) {
                        Log.w("LevelStudyroom", "無效的 character ID: $characterIdString")
                        continue
                    }

                    // 每行最多 3 個用戶
                    if (userIndex % 3 == 0) {
                        currentRow = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { setMargins(0, 8, 0, 8) }
                        }
                        userContainer.addView(currentRow)
                    }

                    val userLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }

                    val userImage = ImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(300, 500)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        setImageResource(characterId)
                    }

                    val userNameView = TextView(this).apply {
                        text = nickname
                        textSize = 14f
                        gravity = Gravity.CENTER
                    }

                    userLayout.addView(userImage)
                    userLayout.addView(userNameView)
                    currentRow?.addView(userLayout)
                    userIndex++
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "加載數據失敗：${exception.message}", Toast.LENGTH_SHORT).show()
                exception.printStackTrace()
            }
    }

    // 停止計時
    private fun stopTimer() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    // 更新用戶狀態到 Firestore並記錄結果
    private fun updateFirestoreStatusAndLog(
        focusManager: FocusManager,
        startTime: Timestamp,
        userId: String,
        db: FirebaseFirestore
    ) {
        val endTime = Timestamp.now()
        val duration = seconds

        // 儲存專注結果
        focusManager.storeFocusResult(startTime, endTime, duration)

        // 更新 Firestore 中的用戶狀態
        db.collection("users").document(userId)
            .update("studyRoomStatus", "left") // 將狀態設置為 "離開"
            .addOnSuccessListener {
                Log.d("LevelStudyroom", "用戶狀態已成功更新為離開")
                Toast.makeText(this, "已退出自習室", Toast.LENGTH_SHORT).show()
                finish() // 返回上一頁
            }
            .addOnFailureListener { e ->
                Log.e("LevelStudyroom", "更新用戶狀態失敗", e)
                Toast.makeText(this, "退出失敗：${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 啟動計時
    private fun startTimer() {
        handler.post(object : Runnable {
            override fun run() {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val secs = seconds % 60
                timerTextView.text = String.format("%02d:%02d:%02d", hours, minutes, secs)

                if (isRunning) {
                    seconds++
                }
                handler.postDelayed(this, 1000)
            }
        })
    }
}
