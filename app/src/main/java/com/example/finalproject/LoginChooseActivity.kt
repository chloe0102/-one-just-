package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginChooseActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var selectedProfilePicture: String? = null
    private var selectedBackground: String? = null
    private var selectedCharacter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginchoose)

        // 初始化 Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 綁定圖片選擇事件
        setupImageSelection()

        // 儲存按鈕
        val saveButton: Button = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            saveSelections()
        }
    }

    private fun setupImageSelection() {
        // 為每個區塊綁定選擇邏輯
        bindSelection(R.id.avatarContainer, ::setProfilePicture)
        bindSelection(R.id.backgroundContainer, ::setBackground)
        bindSelection(R.id.characterContainer, ::setCharacter)
    }

    private fun bindSelection(containerId: Int, setter: (String?) -> Unit) {
        val container = findViewById<LinearLayout>(containerId)
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is androidx.cardview.widget.CardView) {
                val imageView = child.getChildAt(0) as ImageView // 獲取 CardView 內部的 ImageView
                child.setOnClickListener {
                    clearHighlight(container) // 清除高亮
                    imageView.setColorFilter(ContextCompat.getColor(this, R.color.gray)) // 高亮當前選中項
                    setter(resources.getResourceEntryName(imageView.id)) // 保存選擇的 ImageView ID
                    Log.d("LoginChooseActivity", "選擇的項目: ${resources.getResourceEntryName(imageView.id)}")
                }
            }
        }
    }


    private fun clearHighlight(container: LinearLayout) {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is androidx.cardview.widget.CardView) {
                val imageView = child.getChildAt(0) as ImageView // 獲取內部的 ImageView
                imageView.clearColorFilter() // 清除高亮效果
            }
        }
    }



    private fun setProfilePicture(identifier: String?) {
        selectedProfilePicture = identifier
    }

    private fun setBackground(identifier: String?) {
        selectedBackground = identifier
    }

    private fun setCharacter(identifier: String?) {
        selectedCharacter = identifier
    }

    private fun saveSelections() {
        val userId = auth.currentUser?.uid
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "用戶未登入，請重新登入", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedProfilePicture == null || selectedBackground == null || selectedCharacter == null) {
            Toast.makeText(this, "請選擇所有項目", Toast.LENGTH_SHORT).show()
            return
        }

        val userUpdates = mapOf(
            "profilepicture" to selectedProfilePicture,
            "background" to selectedBackground,
            "character" to selectedCharacter
        )

        db.collection("users").document(userId)
            .update(userUpdates)
            .addOnSuccessListener {
                Toast.makeText(this, "儲存成功", Toast.LENGTH_SHORT).show()
                // 跳轉到 MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "儲存失敗：${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
