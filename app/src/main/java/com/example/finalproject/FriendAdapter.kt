package com.example.finalproject

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendListAdapter(
    private val friendList: List<Friend>,
    private val onAddFriendClick: (Friend) -> Unit,
    private val onViewProfileClick: (Friend) -> Unit,
    private val onChatClick: (Friend) -> Unit,
    private val onDeleteClick: (Friend) -> Unit
) : RecyclerView.Adapter<FriendListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val buttonAddFriend: Button = itemView.findViewById(R.id.buttonAddFriend)
        val buttonViewProfile: Button = itemView.findViewById(R.id.buttonViewProfile)
        val buttonChat: Button = itemView.findViewById(R.id.buttonChat)
        val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)

        // 綁定數據
        fun bind(friend: Friend) {
            textViewName.text = friend.nickname

            // 根據是否為好友，顯示狀態
            if (friend.isFriend) {
                buttonChat.visibility = View.VISIBLE
                buttonDelete.visibility = View.VISIBLE
                buttonAddFriend.visibility = View.GONE
                buttonViewProfile.visibility = View.GONE
            } else {
                buttonChat.visibility = View.GONE
                buttonDelete.visibility = View.GONE
                buttonAddFriend.visibility = View.VISIBLE
                buttonViewProfile.visibility = View.VISIBLE
            }

            buttonAddFriend.setOnClickListener {
                onAddFriendClick(friend)
            }
            buttonViewProfile.setOnClickListener {
                onViewProfileClick(friend)
            }
            buttonChat.setOnClickListener {
                onChatClick(friend)
            }
            buttonDelete.setOnClickListener {
                onDeleteClick(friend)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(friendList[position])
    }

    override fun getItemCount(): Int = friendList.size
}
