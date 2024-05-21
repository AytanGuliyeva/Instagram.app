package com.example.instagramapp.ui.search.adapter

import com.example.instagramapp.data.model.Post
import android.content.ContentValues
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.R
import com.example.instagramapp.databinding.PostItemBinding
import com.example.instagramapp.data.model.LikeCount
import com.example.instagramapp.data.model.PostInfo
import com.example.instagramapp.data.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class PostSearchAdapter(
    private var postWithUsernames: List<PostInfo>,
    private val itemClick: (item: Post) -> Unit,
    private val commentButtonClick: (postId: String) -> Unit,
    private val likeButtonClick: (postId: String, imageView: ImageView) -> Unit,
    private val saveButtonClick: (postId: String, imageView: ImageView) -> Unit,
) : RecyclerView.Adapter<PostSearchAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return postWithUsernames.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val postInfo = postWithUsernames[position]
        holder.bind(postInfo)
    }

    inner class PostViewHolder(private val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(postInfo: PostInfo) {
            val post = postInfo.post
            Glide.with(binding.root)
                .load(post.postImageUrl)
                .into(binding.imgPost)
            binding.txtCaption.text = post.caption
            binding.btnComment.setOnClickListener {
                commentButtonClick(post.postId)
            }
            binding.txtComment.setOnClickListener {
                commentButtonClick(post.postId)
            }
            val timestamp = post.time?.toDate()
            timestamp?.let {
                val currentTime = System.currentTimeMillis()
                val postTime = it.time
                val timeDifference = currentTime - postTime
                val minutesAgo = (timeDifference / (1000 * 60)).toInt()
                if (minutesAgo < 1) {
                    binding.txtTime.text = "Just now"
                } else if (minutesAgo < 60) {
                    val timeAgoText =
                        if (minutesAgo == 1) "1 minute ago" else "$minutesAgo minutes ago"
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
            if (post.isSave) {
                binding.btnSaved.setImageResource(R.drawable.icons8_saved_icon)
                binding.btnSaved.tag = "saved"
            } else {
                binding.btnSaved.setImageResource(R.drawable.save_icon)
                binding.btnSaved.tag = "save"
            }
            binding.txtComment.text = "View all ${postInfo.commentCount} comments"
            binding.txtLikes.text = "${postInfo.likeCount} likes"
            itemView.setOnClickListener {
                itemClick(post)
            }
            binding.btnLike.setOnClickListener {
                likeButtonClick(post.postId, binding.btnLike)
                if (post.isLiked) {
                    post.isLiked = false
                    binding.txtLikes.text = "${postInfo.likeCount - 1} likes"
                    postInfo.likeCount -= 1
                } else {
                    post.isLiked = true
                    binding.txtLikes.text = "${postInfo.likeCount + 1} likes"
                    postInfo.likeCount += 1

                }
            }
            if (post.isLiked) {
                binding.btnLike.setImageResource(R.drawable.icon_liked)
                binding.btnLike.tag = "liked"
            } else {
                binding.btnLike.setImageResource(R.drawable.like_icon)
                binding.btnLike.tag = "like"

            }
            binding.btnSaved.setOnClickListener {
                saveButtonClick(post.postId, binding.btnSaved)
            }
            val user = postInfo.user
            binding.txtUsername.text = user?.username
            binding.txtUsername2.text = user?.username
            Glide.with(binding.root)
                .load(user?.imageUrl)
                .into(binding.imgProfile)
        }
    }
}