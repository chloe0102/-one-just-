package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.databinding.MyInformationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyProfileActivity : AppCompatActivity() {

    private lateinit var binding: MyInformationBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "MyProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MyInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            Log.d(TAG, "加載當前用戶主頁, userId: $currentUserId")
            listenToUserProfileUpdates(currentUserId) // 添加 Snapshot Listener
            loadFocusHours(currentUserId)
        } else {
            Toast.makeText(this, "無法加載用戶信息，请重新登入", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "当前未登入")
            finish()
        }

        binding.backButton.setOnClickListener { finish() }

        binding.editProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * 監聽用戶數據變化
     */
    private fun listenToUserProfileUpdates(userId: String) {
        firestore.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e(TAG, "監聽用戶數據失敗: ${error.message}")
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val nickname = document.getString("nickname") ?: "未知暱稱"
                    val identity = document.getString("status") ?: "未知身份"
                    val signature = document.getString("signature") ?: "未設置"
                    val backgroundImageId = document.getString("background")
                    val characterImageId = document.getString("character")
                    val userAvatarId = document.getString("profilepicture")

                    // 更新 UI
                    binding.userName.text = nickname
                    binding.userIdentity.text = identity
                    binding.userSignature.text = signature

                    // 加載頭貼
                    if (!userAvatarId.isNullOrEmpty()) {
                        val avatarResId = resources.getIdentifier(userAvatarId, "drawable", packageName)
                        if (avatarResId != 0) {
                            binding.userAvatar.setImageResource(avatarResId)
                        } else {
                            Log.w(TAG, "頭貼 ID 無效: $userAvatarId")
                        }
                    }

                    // 加載背景圖片
                    if (!backgroundImageId.isNullOrEmpty()) {
                        val backgroundResId = resources.getIdentifier(backgroundImageId, "drawable", packageName)
                        if (backgroundResId != 0) {
                            binding.backgroundImage.setImageResource(backgroundResId)
                        } else {
                            Log.w(TAG, "背景圖片 ID 無效: $backgroundImageId")
                        }
                    }

                    // 加載人物圖片
                    if (!characterImageId.isNullOrEmpty()) {
                        val characterResId = resources.getIdentifier(characterImageId, "drawable", packageName)
                        if (characterResId != 0) {
                            binding.characterIcon.setImageResource(characterResId)
                        } else {
                            Log.w(TAG, "人物圖片 ID 無效: $characterImageId")
                        }
                    }

                    Log.d(TAG, "用戶數據即時更新成功: $nickname, $identity, $signature")
                } else {
                    Toast.makeText(this, "用戶數據不存在", Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "未找到用戶數據, userId: $userId")
                }
            }
    }

    private fun loadFocusHours(userId: String) {
        firestore.collection("focusDiary").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val totalFocusTime = document.getLong("totalFocusHours")?.toString() ?: "0"
                    val longestFocusTime = document.getLong("longestFocusHours")?.toString() ?: "0"

                    // 更新 UI
                    binding.focusHours.text = totalFocusTime
                    Log.d(TAG, "專注時長加載成功: Total=$totalFocusTime, Longest=$longestFocusTime")
                } else {
                    Log.w(TAG, "專注時長數據不存在, userId: $userId")
                    binding.focusHours.text = "0"
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "加載專注數據失敗，请稍後再試", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "加載專注數據失敗: ${exception.message}")
            }
    }
}
