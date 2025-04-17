package com.example.finalproject

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalproject.databinding.QuotedictionaryBinding
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore

class QuoteDictionaryActivity : AppCompatActivity() {

    private lateinit var binding: QuotedictionaryBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var quoteListAdapter: QuoteListAdapter
    private val quoteList = mutableListOf<QuoteItem>()
    private var availableDraws = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 View Binding
        binding = QuotedictionaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 返回按钮設置
        binding.backButton.setOnClickListener {
            finish()
        }

        // 初始化 Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 獲取用戶ID
        val userId = auth.currentUser?.uid
        if (userId != null) {
            setupRecyclerView()
            loadQuotes(userId)
            loadUserStats(userId)

            // 抽取按鈕
            binding.drawButton.setOnClickListener {
                if (availableDraws > 0) {
                    drawQuote(userId)
                } else {
                    Toast.makeText(this, "可抽取次數不足", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "未登入", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        quoteListAdapter = QuoteListAdapter(quoteList)
        binding.quoteRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@QuoteDictionaryActivity)
            adapter = quoteListAdapter
        }
    }

    private fun loadQuotes(userId: String) {
        val quotesRef = firestore.collection("users").document(userId).collection("quotes")
        quotesRef.get()
            .addOnSuccessListener { result ->
                quoteList.clear()
                for (document in result) {
                    val quote = document.toObject(QuoteItem::class.java).apply {
                        id = document.id // 設置文檔ID 到 `id`
                    }
                    quoteList.add(quote)
                }
                quoteListAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "加載語錄失敗: ${exception.message}", Toast.LENGTH_SHORT).show()

            }
    }

    private fun loadUserStats(userId: String) {
        val userStatsRef = firestore.collection("users").document(userId)
        userStatsRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val readingTime = document.getLong("totalReadingTime")?.toInt() ?: 0
                    availableDraws = document.getLong("availableDraws")?.toInt() ?: 0

                    binding.readingTimeText.text = "讀書累計時長: ${readingTime}h"
                    binding.drawCountText.text = "可抽取次數: ${availableDraws}次"
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "加載用戶數據失敗: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun drawQuote(userId: String) {
        val availableQuotes = quoteList.filter { !it.drawn }
        if (availableQuotes.isEmpty()) {
            Toast.makeText(this, "沒有可抽取的語錄", Toast.LENGTH_SHORT).show()
            return
        }

        val randomQuote = availableQuotes.random()

        // 更新語錄的`drawn`狀態
        firestore.collection("users").document(userId)
            .collection("quotes").document(randomQuote.id)
            .update("drawn", true)
            .addOnSuccessListener {
                randomQuote.drawn = true
                quoteListAdapter.notifyDataSetChanged()

                // 顯示彈窗
                showQuoteDialog(randomQuote.text)
            }
            .addOnFailureListener {
                Toast.makeText(this, "標記語錄失敗", Toast.LENGTH_SHORT).show()
            }

        // 更新用戶的可抽取次數
        firestore.collection("quotesDictionary").document(userId)
            .update("availableDraws", availableDraws - 1)
            .addOnSuccessListener {
                availableDraws -= 1
                binding.drawCountText.text = "可抽取次數: ${availableDraws}次"
            }
            .addOnFailureListener {
                Toast.makeText(this, "更新抽取次數失敗", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showQuoteDialog(quoteText: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("恭喜您抽到了新語錄！")
        builder.setMessage(quoteText)
        builder.setPositiveButton("確定") { dialog, _ ->
            dialog.dismiss() // 關閉彈窗
        }
        builder.create().show()
    }
}
