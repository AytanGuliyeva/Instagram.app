package com.example.instagramapp.ui.profile.adapter

import Post
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramapp.databinding.PostItemBinding
import com.example.instagramapp.databinding.ProfilePostItemBinding
import com.example.instagramapp.ui.search.model.Users

class PostAdapter(
    private var itemClick: (item: Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.ProfileViewHolder>() {

    private val diffUtilCallBack = object : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }

    private val diffUtil = AsyncListDiffer(this, diffUtilCallBack)

    fun submitList(posts: List<Post>) {
        diffUtil.submitList(posts)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val binding =
            ProfilePostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfileViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return diffUtil.currentList.size
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(diffUtil.currentList[position])
    }

    inner class ProfileViewHolder(private val binding: ProfilePostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Post) {
            Glide.with(binding.root)
                .load(item.postImageUrl)
                .into(binding.imgPost)

            itemView.setOnClickListener {
                itemClick(item)
            }

        }

    }

}