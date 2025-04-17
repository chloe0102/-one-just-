package com.example.finalproject

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.databinding.NameplateBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class IdentityBadgeActivity : AppCompatActivity() {

    private lateinit var binding: NameplateBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用 ViewBinding
        binding = NameplateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 返回
        binding.backButton.setOnClickListener {
            finish()
        }


        // 初始化 Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 獲取用戶UID
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "用戶未登入", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userId = currentUser.uid

        // 加載已有的自定義文字
        loadCustomText(userId)

        binding.identificationNameplate.setOnClickListener {
            val customText = binding.nameplateEditText.text.toString().trim()
            if (customText.isNotBlank()) {
                saveCustomText(userId, customText)
            } else {
                Toast.makeText(this, "請輸入自定義文字", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun loadCustomText(userId: String) {
        val userDoc = firestore.collection("users").document(userId)
        userDoc.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val text = document.getString("customNameplate")
                    if (!text.isNullOrBlank()) {
                        binding.nameplateTextView.text = text
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "無法加載自定義文字", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * 保存
     */
    private fun saveCustomText(userId: String, text: String) {
        val userDoc = firestore.collection("users").document(userId)
        userDoc.update("customNameplate", text)
            .addOnSuccessListener {
                binding.nameplateTextView.text = text
                Toast.makeText(this, "自定義文字保存成功！", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "保存失敗：${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
