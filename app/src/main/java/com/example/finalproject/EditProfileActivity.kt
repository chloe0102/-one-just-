package com.example.finalproject

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.databinding.ActivityEditprofileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditprofileBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    // 可供選擇的頭貼列表
    private val avatarOptions = listOf(
        R.drawable.avatar01,
        R.drawable.avatar02,
        R.drawable.avatar03,
        R.drawable.avatar04,
        R.drawable.avatar05
    )

    // 身份選項
    private val identityOptions = listOf(
        "大學生", "高中生", "國中生", "小學生", "其他"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用 View Binding
        binding = ActivityEditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 初始化加載提示框
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("加載中，請稍後...")
        progressDialog.setCancelable(false)

        // 加載用戶數據
        loadUserData()

        // 設置返回按鈕點擊事件
        binding.backButton.setOnClickListener {
            finish()
        }

        // 編輯頭貼
        binding.editProfileImageButton.setOnClickListener {
            showAvatarSelectionDialog()
        }

        // 編輯身份（使用下拉式選單）
        binding.identityText.setOnClickListener {
            showIdentitySelectionDialog()
        }

        // 編輯其他字段
        binding.nicknameText.setOnClickListener { showEditDialog("暱稱", "nickname", 20) }
        binding.signatureText.setOnClickListener { showEditDialog("個性簽名", "signature", 50) }
    }

    /**
     * 加載數據
     */
    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "用戶未登入", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressDialog.show()
        val userId = currentUser.uid
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                progressDialog.dismiss()
                if (document != null && document.exists()) {
                    binding.nicknameText.text = document.getString("nickname") ?: "未設置"
                    binding.genderText.text = document.getString("gender") ?: "未設置"
                    binding.identityText.text = document.getString("status") ?: "未設置"
                    binding.signatureText.text = document.getString("signature") ?: "未設置"
                    binding.randomIdText.text = document.getString("randomId") ?: "未設置"

                    val profilePicture = document.getString("profilepicture") ?: ""
                    val resId = resources.getIdentifier(profilePicture, "drawable", packageName)
                    if (resId != 0) {
                        binding.profileImage.setImageResource(resId)
                    }
                } else {
                    Toast.makeText(this, "未找到用戶數據", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss()
                Toast.makeText(this, "加載用戶數據失敗: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("EditProfileActivity", "Error loading user data", exception)
            }
    }

    /**
     * 顯示頭貼選擇對話框
     */
    private fun showAvatarSelectionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("選擇頭貼")

        // 創建一個布局來顯示頭貼圖片
        val avatarContainer = android.widget.LinearLayout(this)
        avatarContainer.orientation = android.widget.LinearLayout.HORIZONTAL
        avatarContainer.setPadding(16, 16, 16, 16)

        // 初始化對話框變數（延遲初始化）
        var avatarDialog: AlertDialog? = null

        // 動態添加頭貼圖片到布局
        avatarOptions.forEach { avatarResId ->
            val imageView = android.widget.ImageView(this)
            imageView.setImageResource(avatarResId)
            imageView.layoutParams = android.widget.LinearLayout.LayoutParams(
                150, 150
            ).apply {
                setMargins(16, 0, 16, 0)
            }

            // 設置圖片點擊事件
            imageView.setOnClickListener {
                updateAvatar(avatarResId) // 更新頭貼資料
                Toast.makeText(this, "頭貼已選擇", Toast.LENGTH_SHORT).show()
                avatarDialog?.dismiss() // 關閉對話框
            }

            avatarContainer.addView(imageView)
        }

        builder.setView(avatarContainer)
        avatarDialog = builder.create() // 創建對話框並賦值給 avatarDialog
        avatarDialog.show()
    }



    /**
     * 更新頭貼
     */
    private fun updateAvatar(selectedAvatar: Int) {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        // 映射資源名稱
        val avatarResourceName = resources.getResourceEntryName(selectedAvatar)

        progressDialog.show()
        firestore.collection("users").document(userId)
            .update("profilepicture", avatarResourceName)
            .addOnSuccessListener {
                progressDialog.dismiss()
                binding.profileImage.setImageResource(selectedAvatar)
                Toast.makeText(this, "頭貼更新成功", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss()
                Toast.makeText(this, "更新頭貼失敗: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * 顯示身份選擇對話框
     */
    private fun showIdentitySelectionDialog() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        val builder = AlertDialog.Builder(this)
        builder.setTitle("選擇身份")
        builder.setItems(identityOptions.toTypedArray()) { _, which ->
            val selectedIdentity = identityOptions[which]

            progressDialog.show()
            firestore.collection("users").document(userId)
                .update("status", selectedIdentity)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    binding.identityText.text = selectedIdentity
                    Toast.makeText(this, "身份更新成功", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "更新身份失敗: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
        builder.show()
    }

    /**
     * 顯示文本字段編輯對話框
     */
    private fun showEditDialog(fieldName: String, fieldKey: String, maxLength: Int) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "用戶未登入", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        val builder = AlertDialog.Builder(this)
        builder.setTitle("編輯$fieldName")
        val input = android.widget.EditText(this)
        input.hint = "請輸入新的$fieldName"
        builder.setView(input)

        builder.setPositiveButton("保存") { _, _ ->
            val newValue = input.text.toString().trim()
            if (newValue.isBlank()) {
                Toast.makeText(this, "$fieldName 不能為空", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (newValue.length > maxLength) {
                Toast.makeText(this, "$fieldName 不能超過 $maxLength 個字符", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            progressDialog.show()
            firestore.collection("users").document(userId)
                .update(fieldKey, newValue)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "$fieldName 更新成功", Toast.LENGTH_SHORT).show()
                    loadUserData()
                }
                .addOnFailureListener { exception ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "更新 $fieldName 失敗: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("EditProfileActivity", "Error updating $fieldKey", exception)
                }
        }

        builder.setNegativeButton("取消", null)
        builder.show()
    }
}
