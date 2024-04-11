package com.example.instagramapp.ui.search.adapter

import Post
import android.content.ActivityNotFoundException
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramapp.databinding.PostItemBinding
import com.example.instagramapp.databinding.SearchPostItemBinding
import com.example.instagramapp.ui.search.model.Users
import java.text.SimpleDateFormat
import java.util.Locale

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
            val timestamp = post.time?.toDate()

            timestamp?.let {
                val currentTime = System.currentTimeMillis()
                val postTime = it.time
                val timeDifference = currentTime - postTime

                val minutesAgo = (timeDifference / (1000 * 60)).toInt()

                if (minutesAgo < 1) {
                    binding.txtTime.text = "Just now"
                } else if (minutesAgo < 60) {
                    val timeAgoText = if (minutesAgo == 1) "1 minute ago" else "$minutesAgo minutes ago"
                    binding.txtTime.text = timeAgoText
                } else if (minutesAgo < 1440) {
                    val hoursAgo = minutesAgo / 60
                    val timeAgoText = if (hoursAgo == 1) "1 hour ago" else "$hoursAgo hours ago"
                    binding.txtTime.text = timeAgoText
                } else if (minutesAgo < 10080) {
                    val daysAgo = minutesAgo / 1440
                    val timeAgoText = if (daysAgo == 1) "1 day ago" else "$daysAgo days ago"
                    binding.txtTime.text = timeAgoText
                } else {
                    val dateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())
                    val formattedDate = dateFormat.format(it)
                    binding.txtTime.text = formattedDate
                }
            }

            itemView.setOnClickListener {
                itemClick(post)
            }
//            binding.btnShare.setOnClickListener {
//                shareWithWp(post)
//            }

        }
//        private fun shareWithWp(post: Post){
//            val shareText="Check this post: ${post.postImageUrl}"
//            val sendIntent= Intent().apply {
//                action= Intent.ACTION_SEND
//                putExtra(Intent.EXTRA_TEXT,shareText)
//                type="text/plain"
//                setPackage("com.whatsapp")
//            }
//            try {
//                binding.root.context.startActivity(sendIntent)
//            }catch (e: ActivityNotFoundException){
//                Toast.makeText(binding.root.context, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show()
//            }
//        }
    }
}
