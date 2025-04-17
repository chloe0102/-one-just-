package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // 初始化 Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // 獲取用戶ID並更新頭像和暱稱
        updateUserNameAndAvatar(view)

        // 更新背景和角色
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            fetchAndSetUserProfile(view, userId)
        }

        // 導航圖標點擊事件
        setupIconClickListeners(view)

        return view
    }

    private fun updateUserNameAndAvatar(view: View) {
        val userNameTextView: TextView = view.findViewById(R.id.user_name)
        val userAvatarImageView: ImageView = view.findViewById(R.id.user_avatar)
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        error.printStackTrace()
                        userNameTextView.text = "加載失敗"
                        return@addSnapshotListener
                    }

                    if (document != null && document.exists()) {
                        // 更新暱稱
                        val nickname = document.getString("nickname") ?: "未設置"
                        userNameTextView.text = nickname

                        // 更新頭像
                        val profilePictureResourceId = document.getString("profilepicture")
                        if (!profilePictureResourceId.isNullOrEmpty()) {
                            val resId = resources.getIdentifier(profilePictureResourceId, "drawable", requireContext().packageName)
                            if (resId != 0) { // 確保資源存在
                                userAvatarImageView.setImageResource(resId)
                            } else {
                                userAvatarImageView.setImageResource(R.drawable.avatar01) // 設置默認頭像
                            }
                        } else {
                            userAvatarImageView.setImageResource(R.drawable.avatar01) // 設置默認頭像
                        }
                    }
                }
        } else {
            userNameTextView.text = "未登入"
            userAvatarImageView.setImageResource(R.drawable.avatar01) // 設置默認頭像
        }
    }

    private fun fetchAndSetUserProfile(view: View, userId: String) {
        val backgroundImageView: ImageView = view.findViewById(R.id.background_image)
        val characterImageView: ImageView = view.findViewById(R.id.character_icon)

        firestore.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    error.printStackTrace() // 打印錯誤日誌
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    // 獲取背景
                    val backgroundResourceId = document.getString("background")
                    val characterIconResourceId = document.getString("character")

                    // 設置背景
                    if (!backgroundResourceId.isNullOrEmpty()) {
                        val resId = resources.getIdentifier(backgroundResourceId, "drawable", requireContext().packageName)
                        backgroundImageView.setImageResource(resId)
                    }

                    // 設置角色
                    if (!characterIconResourceId.isNullOrEmpty()) {
                        val resId = resources.getIdentifier(characterIconResourceId, "drawable", requireContext().packageName)
                        characterImageView.setImageResource(resId)
                    }
                }
            }
    }

    private fun setupIconClickListeners(view: View) {
        // 定义图标和目标页面的映射
        val iconMappings = mapOf(
            R.id.icon1 to MyProfileActivity::class.java,
            R.id.icon2 to DressUpActivity::class.java,
            R.id.icon4 to IdentityBadgeActivity::class.java,
            R.id.icon5 to FocusLevelActivity::class.java,
            R.id.icon6 to FocusDiaryActivity::class.java,
            R.id.icon7 to WhiteNoiseActivity::class.java,
            R.id.icon8 to QuoteDictionaryActivity::class.java,
            R.id.icon9 to FriendListActivity::class.java,
            R.id.icon10 to ChatPreviewActivity::class.java,
            R.id.icon11 to MembershipUpgradeActivity::class.java,
            R.id.icon12 to LogoutActivity::class.java
        )

        // 導航
        iconMappings.forEach { (iconId, targetActivity) ->
            val imageView: ImageView? = view.findViewById(iconId)
            imageView?.setOnClickListener {
                val intent = Intent(requireContext(), targetActivity)
                startActivity(intent)
            }
        }

        val icon3 = view.findViewById<View>(R.id.icon3)
        icon3.setOnClickListener {
            findNavController().navigate(R.id.navigation_book)
        }
    }
}
