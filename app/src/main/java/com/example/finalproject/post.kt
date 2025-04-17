package com.example.finalproject

data class Post(
    var id: String? = null,
    val username: String = "",
    val content: String = "",
    val date: Long = 0,
    val likes: Int = 0,
    val likedBy: Map<String, Boolean> = emptyMap() // 確保默認為空 Map
)
