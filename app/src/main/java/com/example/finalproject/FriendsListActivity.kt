package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalproject.databinding.ActivityFriendBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var friendListAdapter: FriendListAdapter
    private val friendList = mutableListOf<Friend>()

    companion object {
        private const val TAG = "FriendListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()

        binding.btnQuery.setOnClickListener {
            val queryId = binding.edid.text.toString().trim()
            if (queryId.isNotEmpty()) {
                Log.d(TAG, "Query button clicked. Querying for randomId: $queryId")
                queryNonFriendById(queryId)
            } else {
                Toast.makeText(this, "請輸入書友ID", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Query failed: input is empty")
            }
        }

        loadFriendList()

        // 返回按鈕設定
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        friendListAdapter = FriendListAdapter(
            friendList,
            onAddFriendClick = { friend -> addFriend(friend) },
            onViewProfileClick = { friend -> viewProfile(friend) },
            onChatClick = { friend -> openChatRoom(friend) },
            onDeleteClick = { friend -> removeFriend(friend) }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@FriendListActivity)
            adapter = friendListAdapter
        }
    }

    private fun queryNonFriendById(randomId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val usersRef = firestore.collection("users").whereEqualTo("randomId", randomId)

        usersRef.get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    Log.d(TAG, "Query success. Found user(s) with randomId: $randomId")
                    for (document in result) {
                        val targetUserId = document.id
                        val nickname = document.getString("nickname") ?: ""
                        Log.d(TAG, "Found user - ID: $targetUserId, Nickname: $nickname")

                        checkIfAlreadyFriend(currentUserId, targetUserId, nickname)
                    }
                } else {
                    Toast.makeText(this@FriendListActivity, "找不到該書友ID", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "No user found with randomId: $randomId")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this@FriendListActivity, "查詢失敗：${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Query failed", exception)
            }
    }

    private fun checkIfAlreadyFriend(currentUserId: String, targetUserId: String, nickname: String) {
        val friendRef = firestore.collection("friends").document(currentUserId)
            .collection("friendList").document(targetUserId)

        friendRef.get()
            .addOnSuccessListener { document ->
                val isFriend = document.exists()
                val friend = Friend(targetUserId, nickname, isFriend)

                if (!isFriend) {
                    Log.d(TAG, "User $targetUserId is not a friend yet")
                } else {
                    Log.d(TAG, "User $targetUserId is already a friend")
                }

                friendList.clear()
                friendList.add(friend)
                friendListAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this@FriendListActivity, "檢查好友失敗：${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Check friend failed", exception)
            }
    }

    private fun loadFriendList() {
        val currentUserId = auth.currentUser?.uid ?: return
        val friendsRef = firestore.collection("friends").document(currentUserId).collection("friendList")

        friendsRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Toast.makeText(this, "加載好友列表失敗：${error.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "監聽好友列表失敗", error)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                friendList.clear()
                for (document in snapshots) {
                    val id = document.id
                    val nickname = document.getString("nickname") ?: ""
                    friendList.add(Friend(id, nickname, true))
                    Log.d(TAG, "Loaded friend - ID: $id, Nickname: $nickname")
                }
                friendListAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun addFriend(friend: Friend) {
        val currentUserId = auth.currentUser?.uid ?: return
        val friendRef = firestore.collection("friends").document(currentUserId)
            .collection("friendList").document(friend.id)

        friendRef.set(mapOf("nickname" to friend.nickname))
            .addOnSuccessListener {
                Toast.makeText(this, "成功添加好友：${friend.nickname}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Friend added successfully: ${friend.nickname}")
                loadFriendList()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "添加好友失敗：${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to add friend", exception)
            }
    }

    private fun removeFriend(friend: Friend) {
        val currentUserId = auth.currentUser?.uid ?: return
        val friendRef = firestore.collection("friends").document(currentUserId)
            .collection("friendList").document(friend.id)

        friendRef.delete()
            .addOnSuccessListener {
                Toast.makeText(this, "成功刪除好友：${friend.nickname}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Friend removed successfully: ${friend.nickname}")
                loadFriendList()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "刪除好友失敗：${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to remove friend", exception)
            }
    }


    private fun viewProfile(friend: Friend) {
        val intent = Intent(this, ProfileDetailsActivity::class.java)
        intent.putExtra("userId", friend.id)
        startActivity(intent)
    }

    private fun openChatRoom(friend: Friend) {
        val intent = Intent(this, ActivityChatroom::class.java)
        intent.putExtra("currentUserID", auth.currentUser?.uid ?: "")
        intent.putExtra("friendID", friend.id)
        intent.putExtra("friendNickname", friend.nickname)
        startActivity(intent)
    }

}
