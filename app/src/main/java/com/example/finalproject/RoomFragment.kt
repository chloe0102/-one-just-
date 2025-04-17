package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.fragment.app.Fragment
import com.example.finalproject.databinding.FragmentRoomBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RoomFragment : Fragment() {

    private var _binding: FragmentRoomBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    @OptIn(ExperimentalGetImage::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoomBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 獲取 FirebaseFirestore 和用戶 ID
        FirebaseApp.initializeApp(requireContext())
        auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser
        // 創建 FocusManager
        val focusManager = FocusManager(userId.toString(), db)
        // 初始化當日文檔
        focusManager.initializeDailyFocusDocument()

        // 跳轉到視訊自習室
        binding.VideoStudyRoomCard.setOnClickListener {
            startActivity(Intent(requireContext(), VideoStudyroom::class.java))
        }

        // 跳轉到圖書自習室
        binding.LibraryStudyRoomCard.setOnClickListener {
            startActivity(Intent(requireContext(), BasicStudyroom::class.java))
        }

        // 跳轉到校園自習室
        binding.SchoolStudyRoomCard.setOnClickListener {
            startActivity(Intent(requireContext(), SchoolStudyroom::class.java))
        }

        // 跳轉到等級自習室
        binding.RankStudyRoomCard.setOnClickListener {
            startActivity(Intent(requireContext(), LevelStudyroom::class.java))
        }

        // 跳轉到好友自習室
        binding.FriendStudyRoomCard.setOnClickListener {
            startActivity(Intent(requireContext(), FriendStudyroomCheck::class.java))
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
