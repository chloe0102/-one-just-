package com.example.finalproject

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.databinding.MembershipBinding

class MembershipUpgradeActivity : AppCompatActivity() {

    private lateinit var binding: MembershipBinding
    private var selectedPlan: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MembershipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 按鈕點擊事件
        binding.btnMonthly.setOnClickListener {
            selectPlan(binding.btnMonthly, "月付 60/月")
        }

        binding.btnQuarterly.setOnClickListener {
            selectPlan(binding.btnQuarterly, "季付 170/季")
        }

        binding.btnYearly.setOnClickListener {
            selectPlan(binding.btnYearly, "年付 700/年")
        }

        // 確認付款按鈕
        binding.btnConfirm.setOnClickListener {
            if (selectedPlan != null) {
                Toast.makeText(this, "已選擇 $selectedPlan，確認付款", Toast.LENGTH_SHORT).show()
                // 在此處加入付款處理邏輯
            } else {
                Toast.makeText(this, "請選擇一個訂閱計劃", Toast.LENGTH_SHORT).show()
            }
        }

        // 返回按鈕
        binding.backButton.setOnClickListener {
            finish() // 結束當前 Activity 返回上一頁
        }
    }

    private fun selectPlan(selectedButton: Button, plan: String) {
        // 將已選擇按鈕設為深色
        selectedButton.setBackgroundColor(Color.parseColor("#78A2A9")) // 深色
        selectedButton.setTextColor(Color.WHITE)

        // 如果之前有其他選擇的按鈕，恢復其樣式
        when (selectedPlan) {
            "月付 60/月" -> if (selectedButton != binding.btnMonthly) resetButtonStyle(binding.btnMonthly)
            "季付 170/季" -> if (selectedButton != binding.btnQuarterly) resetButtonStyle(binding.btnQuarterly)
            "年付 700/年" -> if (selectedButton != binding.btnYearly) resetButtonStyle(binding.btnYearly)
        }

        // 更新已選擇的計劃
        selectedPlan = plan
    }

    private fun resetButtonStyle(button: Button) {
        button.setBackgroundColor(Color.parseColor("#A7BCC1")) // 默認顏色
        button.setTextColor(Color.WHITE)
    }
}
