package com.example.instagramapp.ui.main.dm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramapp.databinding.SearchUserItemBinding
import com.example.instagramapp.data.model.Chat

class DmAdapter(
    private val messageClick: (currentUser: String) -> Unit,
    var chatUserList: List<Chat>
) : RecyclerView.Adapter<DmAdapter.DmViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DmViewHolder {
        val binding =
            SearchUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DmViewHolder(binding)
    }
    override fun getItemCount(): Int {
        return chatUserList.size
    }
    override fun onBindViewHolder(holder: DmViewHolder, position: Int) {
        holder.bind(chatUserList[position])
    }
    fun updateChatList(newList: List<Chat>) {
        this.chatUserList = newList
        notifyDataSetChanged()
    }

    inner class DmViewHolder(private val binding: SearchUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    messageClick(chatUserList[position].receiverId)
                }
            }
        }

        fun bind(chat: Chat) {
            Glide.with(binding.root).load(chat.imageUrl).into(binding.imgProfile)
            binding.txtUsername.text = chat.username
        }
    }
}