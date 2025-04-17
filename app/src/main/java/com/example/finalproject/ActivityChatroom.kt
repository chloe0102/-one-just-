package com.example.finalproject

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalproject.databinding.ActivityChatroomBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

data class Message(
    val sender: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class ActivityChatroom : AppCompatActivity() {

    private lateinit var binding: ActivityChatroomBinding
    private lateinit var firestore: FirebaseFirestore
    private val messageList = mutableListOf<Message>()
    private lateinit var chatAdapter: ChatAdapter

    private var currentUserID: String = "" // 當前用戶ID
    private var friendID: String = "" // 好友的 ID
    private var friendName: String = "" // 好友的名稱
    private lateinit var chatRoomID: String // 聊天室唯一 ID

    companion object {
        private const val TAG = "ActivityChatroom"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatroomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // 獲取 Intent 中的當前用户 ID 和好友 ID
        currentUserID = intent.getStringExtra("currentUserID") ?: ""
        friendID = intent.getStringExtra("friendID") ?: ""

        if (currentUserID.isEmpty() || friendID.isEmpty()) {
            Toast.makeText(this, "初始化聊天室失败", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 計算聊天室唯一 ID
        chatRoomID = if (currentUserID < friendID) {
            "${currentUserID}_${friendID}"
        } else {
            "${friendID}_${currentUserID}"
        }

        // 獲取好友名稱並顯示
        loadFriendName()

        setupRecyclerView()
        loadMessages()

        binding.sendButton.setOnClickListener {
            val messageText = binding.inputMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                binding.inputMessage.text.clear()
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadFriendName() {
        val userDoc = firestore.collection("users").document(friendID)
        userDoc.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    friendName = document.getString("nickname") ?: "未知用戶"
                    binding.titleText.text = friendName // 顯示好友名稱
                    Log.d(TAG, "好友名稱加載成功: $friendName")
                } else {
                    binding.titleText.text = "未知用戶"
                    Log.w(TAG, "好友名稱不存在")
                }
            }
            .addOnFailureListener { exception ->
                binding.titleText.text = "未知用戶"
                Log.e(TAG, "加載好友名稱失敗: ${exception.message}")
            }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messageList, currentUserID)
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ActivityChatroom)
            adapter = chatAdapter
        }
    }

    private fun loadMessages() {
        val messagesRef = firestore.collection("chats").document(chatRoomID).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        messagesRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Toast.makeText(this@ActivityChatroom, "無法加載訊息: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "加載訊息失敗: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshots != null) {
                messageList.clear()
                for (document in snapshots.documents) {
                    val message = document.toObject(Message::class.java)
                    if (message != null) {
                        messageList.add(message)
                    }
                }
                chatAdapter.notifyDataSetChanged()
                binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
            }
        }
    }

    private fun sendMessage(text: String) {
        val newMessage = mapOf(
            "sender" to currentUserID,
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )

        val chatRoomRef = firestore.collection("chats").document(chatRoomID)

        // 添加訊息到 messages 子集合
        chatRoomRef.collection("messages").add(newMessage)
            .addOnSuccessListener {
                // 更新聊天的 lastMessage 訊息
                chatRoomRef.set(
                    mapOf(
                        "lastMessage" to newMessage,
                        "participants" to listOf(currentUserID, friendID)
                    ),
                    SetOptions.merge() // 合併訊息
                ).addOnSuccessListener {
                    Log.d(TAG, "聊天室 lastMessage 更新成功")
                }.addOnFailureListener { e ->
                    Log.e(TAG, "更新聊天室失敗: ${e.message}")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "訊息發送失敗", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "訊息發送失敗: ${exception.message}")
            }
    }
}
