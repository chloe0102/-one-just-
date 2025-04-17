package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.MyProfileActivity.Companion
import com.example.finalproject.databinding.PersonalInformationBinding
import com.google.firebase.firestore.FirebaseFirestore

class ProfileDetailsActivity : AppCompatActivity() {

    private lateinit var binding: PersonalInformationBinding
    private lateinit var firestore: FirebaseFirestore

    companion object {
        private const val TAG = "ProfileDetailsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PersonalInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // 获取 Intent 传递的 userId
        val userId = intent.getStringExtra("userId")
        if (userId != null) {
            Log.d(TAG, "查看用户主頁，userId: $userId")
            loadUserProfile(userId)
            loadFocusHours(userId)
        } else {
            Toast.makeText(this, "用户信息加载失败", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "未传递 userId")
            finish()
        }

        // 返回按钮功能
        binding.backButton.setOnClickListener { finish() }

    }

    /**
     * 加载用户基本信息
     */
    private fun loadUserProfile(userId: String) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nickname = document.getString("nickname") ?: "未知暱稱"
                    val identity = document.getString("status") ?: "未知身份"
                    val signature = document.getString("signature") ?: "未設置簽名"
                    val backgroundImageId = document.getString("background")
                    val characterImageId = document.getString("character")
                    val useravatarId=document.getString("profilepicture")

                    // 更新 UI
                    binding.userName.text = nickname
                    binding.userIdentity.text = identity
                    binding.userSignature.text = signature

                    // 加載頭貼
                    if (!useravatarId.isNullOrEmpty()) {
                        val useravatarId = resources.getIdentifier(
                            useravatarId, "drawable", packageName
                        )
                        if (useravatarId != 0) {
                            binding.userAvatar.setImageResource(useravatarId)
                        } else {
                            Log.w(com.example.finalproject.ProfileDetailsActivity.TAG, "頭貼 ID 無效: $useravatarId")
                        }
                    } else {
                        Log.w(com.example.finalproject.ProfileDetailsActivity.TAG, "頭貼 ID 為空，無法加載")
                    }

                    // 加載背景圖片
                    if (!backgroundImageId.isNullOrEmpty()) {
                        val backgroundResId = resources.getIdentifier(
                            backgroundImageId, "drawable", packageName
                        )
                        if (backgroundResId != 0) {
                            binding.backgroundImage.setImageResource(backgroundResId)
                        } else {
                            Log.w(TAG, "背景圖片 ID 無效: $backgroundImageId")
                        }
                    } else {
                        Log.w(TAG, "背景圖片 ID 為空，無法加載")
                    }

                    // 加載人物圖片
                    if (!characterImageId.isNullOrEmpty()) {
                        val characterResId = resources.getIdentifier(
                            characterImageId, "drawable", packageName
                        )
                        if (characterResId != 0) {
                            binding.characterIcon.setImageResource(characterResId)
                        } else {
                            Log.w(TAG, "人物圖片 ID 無效: $characterImageId")
                        }
                    } else {
                        Log.w(TAG, "人物圖片 ID 為空，無法加載")
                    }

                    Log.d(TAG, "用户信息加载成功: $nickname, $identity, $signature")
                } else {
                    Toast.makeText(this, "用户信息不存在", Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "未找到用户信息, userId: $userId")
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "加载用户信息失败，请稍后再试", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "加载用户信息失败: ${exception.message}")
                finish()
            }
    }

    /**
     * 加载累计专注时长
     */
    private fun loadFocusHours(userId: String) {
        firestore.collection("focusDiary").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val totalFocusTime = document.getLong("totalFocusHours")?.toString() ?: "0"
                    val longestFocusTime = document.getLong("longestFocusHours")?.toString() ?: "0"

                    // 更新 UI
                    binding.focusHours.text = totalFocusTime
                    Log.d(TAG, "专注时长加载成功: Total=$totalFocusTime, Longest=$longestFocusTime")
                } else {
                    Log.w(TAG, "专注时长数据不存在, userId: $userId")
                    binding.focusHours.text = "0"
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "加载专注时长失败，请稍后再试", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "加载专注时长失败: ${exception.message}")
            }
    }
}
