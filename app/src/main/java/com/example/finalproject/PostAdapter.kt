package com.example.finalproject

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val postList: List<Post>,
    private val onLikeClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance() // 初始化 FirebaseAuth

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val postUsername: TextView = view.findViewById(R.id.postUsername)
        val postDate: TextView = view.findViewById(R.id.postDate)
        val postContent: TextView = view.findViewById(R.id.postContent)
        val likeButton: ImageButton = view.findViewById(R.id.likeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.postUsername.text = post.username

        // 格式化日期
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(post.date))
        holder.postDate.text = formattedDate

        holder.postContent.text = post.content

        // 根據是否已點讚改變按鈕顏色
        val isLiked = post.likedBy[auth.currentUser?.uid] == true
        holder.likeButton.setColorFilter(if (isLiked) Color.RED else Color.GRAY)

        holder.likeButton.setOnClickListener {
            onLikeClick(post)
        }
    }

    override fun getItemCount(): Int = postList.size
}
