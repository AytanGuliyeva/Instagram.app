package com.example.instagramapp.ui.search.adapter

import Post
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramapp.databinding.SearchPostItemBinding

class PostSearchAdapter() : RecyclerView.Adapter<PostSearchAdapter.PostViewHolder>() {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding =
            SearchPostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return diffUtil.currentList.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(diffUtil.currentList[position])
    }

    inner class PostViewHolder(private val binding: SearchPostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Post) {
            Glide.with(binding.root)
                .load(item.postImageUrl)
                .into(binding.imgPost)
        }


    }

}