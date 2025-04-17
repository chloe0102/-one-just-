package com.example.finalproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import com.example.finalproject.databinding.FragmentBookBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class BookFragment : Fragment() {

    private var _binding: FragmentBookBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 初始化 Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // 獲取當前用戶
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "請先登入", Toast.LENGTH_SHORT).show()
            return root
        }

        val userId = currentUser.uid

        // 設置當前日期
        val todayDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
        binding.todayDateText.text = todayDate

        // 加載 Firestore 中的當天札記
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val existingNote = getNoteFromFirestore(userId, todayDate)
                binding.todayNoteInput.setText(existingNote ?: "")
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "加載數據失敗: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // 儲存札記按鈕點擊事件
        binding.saveNoteButton.setOnClickListener {
            val noteContent = binding.todayNoteInput.text.toString()
            if (noteContent.isNotEmpty()) {
                saveNoteToFirestore(userId, todayDate, noteContent)
            } else {
                Toast.makeText(requireContext(), "請輸入札記內容", Toast.LENGTH_SHORT).show()
            }
        }

        // 跳轉到 BookBeforeFragment
        binding.iconImageView.setOnClickListener {
            findNavController().navigate(R.id.action_bookFragment_to_bookBeforeFragment)
        }

        return root
    }

    private suspend fun getNoteFromFirestore(userId: String, date: String): String? {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .collection("notes")
                .document(date)
                .get()
                .await()
            document.getString("content") // 從 Firestore 獲取札記內容
        } catch (e: Exception) {
            null // 若查詢失敗，返回 null
        }
    }

    private fun saveNoteToFirestore(userId: String, date: String, content: String) {
        val noteData = mapOf("content" to content)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("notes")
                    .document(date)
                    .set(noteData)
                    .await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "札記已儲存", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "儲存失敗：${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
