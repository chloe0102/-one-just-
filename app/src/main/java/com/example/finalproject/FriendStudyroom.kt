package com.example.finalproject

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.cardview.widget.CardView
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FriendStudyroom : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private var seconds = 0
    private var isRunning = true
    private val handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var db: FirebaseFirestore
    private lateinit var studyroomIdTextView: TextView
    private lateinit var userContainer: LinearLayout
    private var studyroomId: String? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.friend_studyroom)

        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        timerTextView = findViewById(R.id.timer)
        studyroomIdTextView = findViewById(R.id.studyroomId)
        userContainer = findViewById(R.id.userContainer)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "請先登入！", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userId = currentUser.uid

        val receivedStudyroomId = intent.getStringExtra("studyroomId")
        if (receivedStudyroomId != null) {
            studyroomId = receivedStudyroomId
            enterExistingStudyroom(receivedStudyroomId, userId)
        } else {
            createNewStudyroom(userId)
        }

        findViewById<CardView>(R.id.studyroomIdCard).setOnClickListener {
            studyroomId?.let {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Studyroom ID", it)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "自習室ID已複製到剪貼板：$it", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.finishButton).setOnClickListener {
            leaveStudyroom(userId)
        }
    }

    private fun startTimer() {
        handler.post(object : Runnable {
            override fun run() {
                val time = String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
                timerTextView.text = time
                if (isRunning) {
                    seconds++
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun enterExistingStudyroom(studyroomId: String, userId: String) {
        studyroomIdTextView.text = "進入自習室ID：$studyroomId"
        db.collection("studyrooms").document(studyroomId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    db.collection("studyrooms").document(studyroomId)
                        .update("count", FieldValue.increment(1))
                    db.collection("users").document(userId)
                        .update("currentStudyroomId", studyroomId)
                        .addOnSuccessListener { Log.d("Firestore", "更新用戶所在自習室成功") }
                    startTimer()
                    loadUsersFromFirestore(studyroomId) // 傳入 studyroomId
                } else {
                    Toast.makeText(this, "該自習室不存在！", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "查詢自習室失敗：${exception.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun createNewStudyroom(userId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                studyroomId = generateUniqueStudyroomID()
                studyroomId?.let { id ->
                    studyroomIdTextView.text = "您的自習室ID為：$id"
                    val studyroomData = mapOf(
                        "studyroomId" to id,
                        "ownerId" to userId,
                        "createdAt" to Timestamp.now(),
                        "count" to 1
                    )
                    db.collection("studyrooms").document(id).set(studyroomData).await()
                    db.collection("users").document(userId)
                        .update("currentStudyroomId", id)
                    startTimer()
                    loadUsersFromFirestore(id) // 傳入 studyroomId
                }
            } catch (e: Exception) {
                Log.e("Firestore", "創建自習室失敗：${e.message}")
            }
        }
    }

    private suspend fun generateUniqueStudyroomID(): String {
        var uniqueId: String
        do {
            uniqueId = (10000000..99999999).random().toString()
            val documentSnapshot = db.collection("studyrooms").document(uniqueId).get().await()
        } while (documentSnapshot.exists())
        return uniqueId
    }

    private fun loadUsersFromFirestore(studyroomId: String) {
        db.collection("users")
            .whereEqualTo("currentStudyroomId", studyroomId) // 只顯示該自習室中的用戶
            .get()
            .addOnSuccessListener { documents ->
                var userIndex = 0
                var currentRow: LinearLayout? = null
                val rowContainerParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 16)
                }
                val itemParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(16, 16, 16, 16)
                }

                for (document in documents) {
                    val characterIdString = document.getString("character")
                    val nickname = document.getString("nickname") ?: "Unknown"
                    val characterId = characterIdString?.toIntOrNull()

                    if (characterId == null) {
                        Log.w("FriendStudyroom", "無效的 character ID: $characterIdString")
                        continue
                    }

                    if (userIndex % 3 == 0) {
                        currentRow = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = rowContainerParams
                        }
                        userContainer.addView(currentRow)
                    }

                    val userLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        layoutParams = itemParams
                    }

                    val userImage = ImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(300, 500)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        setImageResource(characterId)
                    }

                    val userNameView = TextView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
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

    private fun leaveStudyroom(userId: String) {
        studyroomId?.let { id ->
            db.collection("studyrooms").document(id)
                .update("count", FieldValue.increment(-1))
                .addOnSuccessListener {
                    checkAndDeleteStudyroom(id)
                }
            db.collection("users").document(userId)
                .update("currentStudyroomId", FieldValue.delete())
        }
        stopTimer()
        finish()
    }

    private fun checkAndDeleteStudyroom(studyroomId: String) {
        db.collection("studyrooms").document(studyroomId).get()
            .addOnSuccessListener { document ->
                val count = document.getLong("count") ?: 0
                if (count <= 0) {
                    db.collection("studyrooms").document(studyroomId).delete()
                        .addOnSuccessListener { Log.d("Firestore", "自習室已刪除") }
                        .addOnFailureListener { e -> Log.e("Firestore", "刪除自習室失敗：${e.message}") }
                }
            }
            .addOnFailureListener { e -> Log.e("Firestore", "檢查自習室失敗：${e.message}") }
    }

    private fun stopTimer() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
    }
}
