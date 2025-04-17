package com.example.finalproject

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.databinding.RankIllustrateBinding

class FocusLevelExplanationActivity : AppCompatActivity() {

    private lateinit var binding: RankIllustrateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用 ViewBinding
        binding = RankIllustrateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 返回按鈕
        val backButton: ImageButton = binding.backButton
        backButton.setOnClickListener {
            finish()
        }
    }
}
