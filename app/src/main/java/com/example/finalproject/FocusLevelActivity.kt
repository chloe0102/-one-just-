package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.databinding.RankBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class FocusLevelActivity : AppCompatActivity() {

    private lateinit var binding: RankBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用 View Binding
        binding = RankBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 問號按鈕功能，跳轉到解釋頁面
        binding.question.setOnClickListener {
            val intent = Intent(this, FocusLevelExplanationActivity::class.java)
            startActivity(intent)
        }

        // 返回按鈕功能
        binding.backButton.setOnClickListener {
            finish()
        }

        // 檢查用戶是否已登入
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            loadFocusHours(userId)
        } else {
            finish() // 若未登入，返回上一頁
        }
    }

    private fun loadFocusHours(userId: String) {
        val focusDiaryRef = firestore.collection("focusDiary").document(userId)

        focusDiaryRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // 假設 `focusHours` 是一個鍵，包含累積專注時間
                    val focusHoursMap = document.data ?: emptyMap<String, Any>()
                    var totalFocusHours = 0

                    for ((_, value) in focusHoursMap) {
                        totalFocusHours += (value as? Number)?.toInt() ?: 0
                    }

                    updateUI(totalFocusHours)
                } else {
                    updateUI(0) // 如果文檔不存在，設置專注時數為 0
                }
            }
            .addOnFailureListener { e ->
                // 處理 Firestore 錯誤
                e.printStackTrace()
                updateUI(0)
            }
    }

    private fun updateUI(totalFocusHours: Int) {
        // 等級列表及徽章資源
        val levels = listOf("青銅", "白銀", "黃金", "鉑金", "鑽石", "星耀", "菁英")
        val levelIconsLit = listOf(
            R.drawable.badge2, R.drawable.badge4, R.drawable.badge5,
            R.drawable.badge6, R.drawable.badge7, R.drawable.badge8, R.drawable.badge9
        )
        val levelIconsUnlit = listOf(
            R.drawable.badge2_1, R.drawable.badge4_1, R.drawable.badge5_1,
            R.drawable.badge6_1, R.drawable.badge7_1, R.drawable.badge8_1, R.drawable.badge9_1
        )
        val currentLevel = totalFocusHours / 30 // 當前等級索引
        val remainingHours = 30 - (totalFocusHours % 30) // 距離下一等級所需小時數

        // 更新當前等級
        if (currentLevel < levels.size) {
            binding.currentRank.text = levels[currentLevel]
            binding.badgeImage.setImageResource(levelIconsLit[currentLevel])
        }

        // 更新下一等級及所需時間
        if (currentLevel + 1 < levels.size) {
            binding.nextRank.text = levels[currentLevel + 1]
            binding.remainingHours.text = remainingHours.toString()
        } else {
            binding.nextRank.text = "已滿級"
            binding.remainingHours.text = "0"
        }

        // 更新 Grid 中的徽章狀態
        for (i in levels.indices) {
            val iconResource = if (i <= currentLevel) levelIconsLit[i] else levelIconsUnlit[i]
            when (i) {
                0 -> binding.badge2Image.setImageResource(iconResource)
                1 -> binding.badge4Image.setImageResource(iconResource)
                2 -> binding.badge5Image.setImageResource(iconResource)
                3 -> binding.badge6Image.setImageResource(iconResource)
                4 -> binding.badge7Image.setImageResource(iconResource)
                5 -> binding.badge8Image.setImageResource(iconResource)
                6 -> binding.badge9Image.setImageResource(iconResource)
            }
        }
    }
}
