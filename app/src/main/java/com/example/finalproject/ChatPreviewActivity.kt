package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalproject.databinding.ActivityChatPreviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class ChatPreview(
    val chatRoomID: String = "",
    val friendID: String = "",
    val friendName: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L
)

class ChatPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatPreviewBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val chatPreviewList = mutableListOf<ChatPreview>()
    private lateinit var chatPreviewAdapter: ChatPreviewAdapter

    companion object {
        private const val TAG = "ChatPreviewActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 返回按鈕設置
        binding.backButton.setOnClickListener {
            finish()
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadChatPreviews()
    }

    private fun setupRecyclerView() {
        chatPreviewAdapter = ChatPreviewAdapter(chatPreviewList) { chatPreview ->
            openChatRoom(chatPreview)
        }
        binding.recyclerViewChatPreviews.apply {
            layoutManager = LinearLayoutManager(this@ChatPreviewActivity)
            adapter = chatPreviewAdapter
        }
    }

    private fun loadChatPreviews() {
        val currentUserId = auth.currentUser?.uid ?: return

        val chatsRef = firestore.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastMessage.timestamp", Query.Direction.DESCENDING)

        chatsRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Toast.makeText(this, "無法監聽聊天室列表: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "監聽聊天室失敗", error)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                chatPreviewList.clear()
                for (document in snapshots) {
                    val chatRoomID = document.id
                    val participants = document.get("participants") as? List<*>
                    if (participants.isNullOrEmpty()) {
                        Log.e(TAG, "聊天室 $chatRoomID 的參與者為空")
                        continue
                    }

                    val lastMessage = (document.get("lastMessage") as? Map<*, *>)?.get("text") as? String ?: "暫無訊息"
                    val timestamp = (document.get("lastMessage") as? Map<*, *>)?.get("timestamp") as? Long ?: 0L
                    val friendID = participants.firstOrNull { it != currentUserId } as? String ?: ""

                    loadFriendNickname(currentUserId, friendID) { friendName ->
                        val chatPreview = ChatPreview(
                            chatRoomID = chatRoomID,
                            friendID = friendID,
                            friendName = friendName,
                            lastMessage = lastMessage,
                            timestamp = timestamp
                        )
                        chatPreviewList.add(chatPreview)
                        chatPreviewAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun loadFriendNickname(currentUserID: String, friendID: String, callback: (String) -> Unit) {
        if (friendID.isEmpty()) {
            Log.e(TAG, "好友 ID 為空，無法加載暱稱")
            callback("未知用戶")
            return
        }

        val friendDoc = firestore.collection("friends")
            .document(currentUserID)
            .collection("friendList")
            .document(friendID)

        friendDoc.get()
            .addOnSuccessListener { document ->
                val nickname = document.getString("nickname") ?: "未知用戶"
                callback(nickname)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "加載好友暱稱失敗: ${exception.message}")
                callback("未知用戶")
            }
    }

    private fun openChatRoom(chatPreview: ChatPreview) {
        val intent = Intent(this, ActivityChatroom::class.java)
        intent.putExtra("currentUserID", auth.currentUser?.uid ?: "")
        intent.putExtra("friendID", chatPreview.friendID)
        startActivity(intent)
    }
}
