package com.example.finalproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatPreviewAdapter(
    private val chatPreviewList: List<ChatPreview>,
    private val onChatPreviewClick: (ChatPreview) -> Unit
) : RecyclerView.Adapter<ChatPreviewAdapter.ChatPreviewViewHolder>() {

    inner class ChatPreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewLastMessage: TextView = itemView.findViewById(R.id.textViewPreviewMessage)

        fun bind(chatPreview: ChatPreview) {
            textViewName.text = chatPreview.friendName
            textViewLastMessage.text = chatPreview.lastMessage

            itemView.setOnClickListener { onChatPreviewClick(chatPreview) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatPreviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_preview, parent, false)
        return ChatPreviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatPreviewViewHolder, position: Int) {
        holder.bind(chatPreviewList[position])
    }

    override fun getItemCount(): Int = chatPreviewList.size
}
