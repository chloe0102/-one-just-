package com.example.finalproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.fragment.app.Fragment
import com.example.finalproject.databinding.BookbeforeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*

class BookBeforeFragment : Fragment() {

    private var _binding: BookbeforeBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var snapshotListener: ListenerRegistration? = null
    private var currentDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BookbeforeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 初始化 Firestore 和 Auth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            binding.noteContentTextView.text = "請先登入以查看札記。"
            return root
        }

        val userId = currentUser.uid

        // 獲取今天的日期
        currentDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
        binding.selectedDateTextView.text = currentDate

        // 設置返回按鈕事件
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // 設置日曆選擇事件
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d/%02d/%02d", year, month + 1, dayOfMonth)
            binding.selectedDateTextView.text = selectedDate

            // 切換到新的日期，更新監聽
            startListeningForNotes(userId, selectedDate)
        }

        // 初次顯示今天的札記，並開始監聽
        startListeningForNotes(userId, currentDate)

        return root
    }

    /**
     * 開始監聽 Firestore 中指定日期的札記
     */
    private fun startListeningForNotes(userId: String, date: String) {
        // 停止之前的監聽
        snapshotListener?.remove()

        // 設置新的監聽
        snapshotListener = firestore.collection("users")
            .document(userId)
            .collection("notes")
            .document(date)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    binding.noteContentTextView.text = "加載失敗：${error.message}"
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val noteContent = document.getString("content")
                    binding.noteContentTextView.text = noteContent ?: "這天沒有任何札記。"
                } else {
                    binding.noteContentTextView.text = "這天沒有任何札記。"
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 移除監聽
        snapshotListener?.remove()
        _binding = null
    }
}
