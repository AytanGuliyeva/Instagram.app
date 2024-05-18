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
import com.example.instagramapp.data.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class PostSearchAdapter(
    private val itemClick: (item: Post) -> Unit,
    private val commentButtonClick: (postId: String) -> Unit,
    private val likeButtonClick: (postId: String, imageView: ImageView) -> Unit,
    private var likeCountList: List<LikeCount> = emptyList(),
    private val saveButtonClick: (postId: String, imageView: ImageView) -> Unit,

    ) : RecyclerView.Adapter<PostSearchAdapter.PostViewHolder>() {

    private var postWithUsernames: List<Pair<Post, String>> = emptyList()

    //TODO migrate repos from adapter to viewmodel and fragment
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    fun submitList(posts: List<Pair<Post, String>>) {
        postWithUsernames = posts
        //TODO move data to constructor
        notifyDataSetChanged()
    }

    fun updateLikeCount(likeCountList: List<LikeCount>) {
        this.likeCountList = likeCountList
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

    inner class PostViewHolder(private val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post, username: String) {
            Glide.with(binding.root)
                .load(post.postImageUrl)
                .into(binding.imgPost)
            binding.txtCaption.text = post.caption
            fetchCommentCount(post.postId)
            binding.btnComment.setOnClickListener {
                commentButtonClick(post.postId)
            }
            binding.txtComment.setOnClickListener {
                commentButtonClick(post.postId)
            }

            fetchUsername(post.userId)
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

            itemView.setOnClickListener {
                itemClick(post)
            }
            binding.btnLike.setOnClickListener {
                likeButtonClick(post.postId, binding.btnLike)
            }
            if (post.isLiked) {
                binding.btnLike.setImageResource(R.drawable.icon_liked)
                binding.btnLike.tag = "liked"
            } else {
                binding.btnLike.setImageResource(R.drawable.like_icon)
                binding.btnLike.tag = "like"
            }
            likeCount(binding.txtLikes, post.postId)
            binding.btnSaved.setOnClickListener {
                saveButtonClick(post.postId, binding.btnSaved)
            }
            checkSaveStatus(post.postId, binding.btnSaved)
        }

        private fun fetchCommentCount(postId: String) {
            firestore.collection(ConstValues.COMMENTS).document(postId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val comments = documentSnapshot.data?.size ?: 0
                    val commentText = "View all $comments comments"
                    binding.txtComment.text = commentText
                }
                .addOnFailureListener { exception ->
                    Log.e("PostSearchAdapter", "Error getting comment count: $exception")
                }
        }

        fun fetchUsername(userId: String) {
            firestore.collection(ConstValues.USERS)
                .document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toUser()
                    val username = user?.username ?: ""
                    binding.txtUsername.text = username
                    binding.txtUsername2.text = username
                    Glide.with(binding.root)
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

        //like
        private fun likeCount(likes: TextView, postId: String) {
            firestore.collection(ConstValues.LIKES).document(postId)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e("likeCount", "Error fetching like count: $error")
                        return@addSnapshotListener
                    }
                    if (value != null && value.exists()) {
                        val likesCount = value.data?.size ?: 0
                        val likesString = "$likesCount likes"
                        likes.text = likesString
                    } else {
                        likes.text = "0 likes"
                    }
                }
        }

//        //for image url
//        private fun fetchUserProfile(username: String) {
//            firestore.collection("Users")
//                .whereEqualTo("username", username)
//                .get()
//                .addOnSuccessListener { querySnapshot ->
//                    if (!querySnapshot.isEmpty) {
//                        val userDocument = querySnapshot.documents[0]
//                        val user = userDocument.toObject(Users::class.java)
//                        user?.let {
//                            Glide.with(binding.root)
//                                .load(it.imageUrl)
//                                .into(binding.imgProfile)
//                        }
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    Log.e("fetchUserProfile", "Error fetching user profile: $exception")
//                }
//        }


        private fun checkSaveStatus(postId: String, imageView: ImageView) {
            firestore.collection(ConstValues.SAVES).document(auth.currentUser!!.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val savedPostId = document.getBoolean(postId) ?: false
                        if (savedPostId) {
                            imageView.setImageResource(R.drawable.icons8_saved_icon)
                            imageView.tag = "saved"
                        } else {
                            imageView.setImageResource(R.drawable.save_icon)
                            imageView.tag = "save"
                        }
                    } else {
                        imageView.setImageResource(R.drawable.save_icon)
                        imageView.tag = "save"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("checkSaveStatus", "Error checking save status: $exception")
                }
        }
    }
}

//        private fun shareWithWp(post: com.example.instagramapp.data.model.Post){
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