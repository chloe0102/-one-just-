package com.example.finalproject

data class QuoteItem(
    var id: String = "", // 用於存放Firestore文檔的ID
    val text: String = "",
    var drawn: Boolean = false
)
