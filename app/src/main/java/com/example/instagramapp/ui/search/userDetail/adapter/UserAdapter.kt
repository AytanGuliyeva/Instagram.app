package com.example.instagramapp.ui.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramapp.databinding.PostItemBinding
import com.example.instagramapp.databinding.ProfilePostItemBinding
import com.example.instagramapp.databinding.SearchUserItemBinding
import com.example.instagramapp.ui.search.model.Users

class UserAdapter(
    private var itemClick: (item: Users) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    private val diffUtilCallBack = object : DiffUtil.ItemCallback<Users>() {
        override fun areItemsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem == newItem
        }
    }

    private val diffUtil = AsyncListDiffer(this, diffUtilCallBack)

    fun submitList(user: List<Users>) {
        diffUtil.submitList(user)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            SearchUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return diffUtil.currentList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(diffUtil.currentList[position])

    }

    inner class UserViewHolder(private val binding: SearchUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Users) {
            Glide.with(binding.root)
                .load(item.imageUrl)
                .into(binding.imgProfile)

            binding.txtUsername.text = item.username

            itemView.setOnClickListener {
                itemClick(item)
            }

        }
    }

}