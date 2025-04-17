package com.example.finalproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalproject.databinding.FragmentSocialBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SocialFragment : Fragment() {

    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var postAdapter: PostAdapter
    private val postList: MutableList<Post> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialBinding.inflate(inflater, container, false)

        // 初始化 Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        loadPosts()

        binding.imageButton.setOnClickListener {
            showCreatePostDialog()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(postList, onLikeClick = { post ->
            handleLikeClick(post)
        })
        binding.recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }
    }

    private fun loadPosts() {
        firestore.collection("posts")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(context, "Failed to load posts: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    postList.clear()
                    for (document in snapshots) {
                        val post = document.toObject(Post::class.java).apply { id = document.id }
                        postList.add(post)
                    }
                    postAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun handleLikeClick(post: Post) {
        val user = auth.currentUser ?: return
        val postRef = firestore.collection("posts").document(post.id!!)
        val userLikeKey = "likedBy.${user.uid}"

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val likedBy = snapshot.getBoolean(userLikeKey) ?: false
            val likes = (snapshot.getLong("likes") ?: 0).toInt()

            if (likedBy) {
                // 如果已點讚，取消點讚
                transaction.update(postRef, mapOf("likes" to likes - 1, userLikeKey to false))
            } else {
                // 點讚
                transaction.update(postRef, mapOf("likes" to likes + 1, userLikeKey to true))
            }
        }.addOnSuccessListener {
            Toast.makeText(context, "Like updated!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to update like: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCreatePostDialog() {
        val dialog = CreatePostDialog(requireContext()) { content ->
            createPost(content)
        }
        dialog.show()
    }

    private fun createPost(content: String) {
        val user = auth.currentUser
        val userId = user?.uid ?: return

        // 查詢用戶暱稱
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val nickname = document.getString("nickname") ?: "Anonymous"
                val date = System.currentTimeMillis()

                val newPost = mapOf(
                    "username" to nickname,
                    "content" to content,
                    "date" to date,
                    "likes" to 0,
                    "likedBy" to emptyMap<String, Boolean>()
                )

                firestore.collection("posts").add(newPost)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Post created successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to create post: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to fetch user info: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
