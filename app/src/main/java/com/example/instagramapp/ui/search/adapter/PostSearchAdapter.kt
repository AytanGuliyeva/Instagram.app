package com.example.instagramapp.ui.search.adapter

import Post
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramapp.databinding.PostItemBinding
import com.example.instagramapp.databinding.SearchPostItemBinding
import com.example.instagramapp.ui.search.model.Users

class PostSearchAdapter(
    private val itemClick: (item: Post) -> Unit
) : RecyclerView.Adapter<PostSearchAdapter.PostViewHolder>() {

    private var postWithUsernames: List<Pair<Post, String>> = emptyList()

    fun submitList(posts: List<Pair<Post, String>>) {
        postWithUsernames = posts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return postWithUsernames.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val (post, username) = postWithUsernames[position]
        holder.bind(post, username)
    }

    inner class PostViewHolder(private val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post, username: String) {
            Glide.with(binding.root)
                .load(post.postImageUrl)
                .into(binding.imgPost)

            binding.txtUsername2.text = username
            binding.txtUsername.text = username
            binding.txtCaption.text = post.caption

            itemView.setOnClickListener {
                itemClick(post)
            }
        }
    }
}
