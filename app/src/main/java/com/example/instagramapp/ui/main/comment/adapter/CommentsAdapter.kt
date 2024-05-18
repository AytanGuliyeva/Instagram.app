package com.example.instagramapp.ui.main.comment.adapter

import android.content.ContentValues
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.databinding.CommentsItemBinding
import com.example.instagramapp.data.model.Comments
import com.example.instagramapp.data.model.Users
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class CommentsAdapter() : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {
    private val firestore = FirebaseFirestore.getInstance()
    private val diffUtilCallBack = object : DiffUtil.ItemCallback<Comments>() {
        override fun areItemsTheSame(oldItem: Comments, newItem: Comments): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: Comments, newItem: Comments): Boolean {
            return oldItem == newItem
        }
    }

    private val diffUtil = AsyncListDiffer(this, diffUtilCallBack)

    fun submitList(comment: List<Comments>) {
        diffUtil.submitList(comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding =
            CommentsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return diffUtil.currentList.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(diffUtil.currentList[position])
    }

    inner class CommentViewHolder(private val binding: CommentsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comments: Comments) {
            binding.txtComment.text = comments.comment
            binding.txtTime.text = comments.time.toString()
            val timestamp = comments.time.toDate()

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
            fetchUsername(comments.userId)
        }

        private fun fetchUsername(userId: String) {
            firestore.collection(ConstValues.USERS)
                .document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toUser()
                    val username = user?.username ?: ""
                    binding.txtUsername.text = username
                    Glide.with(binding.root.context.applicationContext)
                        .load(user?.imageUrl)
                        .into(binding.imgProfile)

                }
                .addOnFailureListener { exception ->
                    Log.e(
                        ContentValues.TAG,
                        "Failed to fetch username: ${exception.message}",
                        exception
                    )
                }
        }

        private fun DocumentSnapshot.toUser(): Users? {
            return try {
                val userId = getString(ConstValues.USER_ID)
                val username = getString(ConstValues.USERNAME)
                val email = getString(ConstValues.EMAIL)
                val password = getString(ConstValues.PASSWORD)
                val bio = getString(ConstValues.BIO)
                val imageUrl = getString(ConstValues.IMAGE_URL)

                Users(
                    userId.orEmpty(),
                    username.orEmpty(),
                    email.orEmpty(),
                    password.orEmpty(),
                    bio.orEmpty(),
                    imageUrl.orEmpty(),
                )
            } catch (e: Exception) {
                null
            }
        }

    }

}