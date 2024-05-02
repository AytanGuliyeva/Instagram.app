package com.example.instagramapp.ui.search.adapter

import Post
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramapp.R
import com.example.instagramapp.databinding.PostItemBinding
import com.example.instagramapp.databinding.SearchPostItemBinding
import com.example.instagramapp.ui.search.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Locale

class PostSearchAdapter(
    private val itemClick: (item: Post) -> Unit
) : RecyclerView.Adapter<PostSearchAdapter.PostViewHolder>() {


    private var postWithUsernames: List<Pair<Post, String>> = emptyList()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
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

    inner class PostViewHolder(private val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post, username: String) {
            Glide.with(binding.root)
                .load(post.postImageUrl)
                .into(binding.imgPost)

            binding.txtUsername2.text = username
            binding.txtUsername.text = username
            binding.txtCaption.text = post.caption
            fetchUserProfile(username)

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
                toggleLikeStatus(post.postId, binding.btnLike)
            }

            checkLikeStatus(post.postId, binding.btnLike)
            likeCount(binding.txtLikes, post.postId)

            binding.btnSaved.setOnClickListener {
                toggleSaveStatus(post.postId, binding.btnSaved)
            }

            checkSaveStatus(post.postId, binding.btnSaved)
        }

        //like
        private fun toggleLikeStatus(postId: String, imageView: ImageView) {
            val tag = imageView.tag?.toString() ?: ""

            if (tag == "liked") {
                imageView.setImageResource(R.drawable.like_icon)
                imageView.tag = "like"
                removeLikeFromFirestore(postId)
            } else {
                imageView.setImageResource(R.drawable.icon_liked)
                imageView.tag = "liked"
                addLikeToFirestore(postId)
            }
        }

        private fun likeCount(likes: TextView, postId: String) {
            firestore.collection("Likes").document(postId).addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("likeCount", "Error fetching like count: $error")
                    return@addSnapshotListener
                }
                if (value != null && value.exists()) {
                    val likesCount = value.data?.size ?: 0
                    val likesString = if (likesCount == 0) {
                        "0 likes"
                    } else if (likesCount == 1) {
                        "1 like"
                    } else {
                        "$likesCount likes"
                    }
                    likes.text = likesString
                } else {
                    likes.text = "0 likes"
                }
            }
        }

        private fun checkLikeStatus(postId: String, imageView: ImageView) {
            firestore.collection("Likes").document(postId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val likedByCurrentUser =
                            document.getBoolean(auth.currentUser!!.uid) ?: false
                        if (likedByCurrentUser) {
                            imageView.setImageResource(R.drawable.icon_liked)
                            imageView.tag = "liked"
                        } else {
                            imageView.setImageResource(R.drawable.like_icon)
                            imageView.tag = "like"
                        }
                    } else {
                        imageView.setImageResource(R.drawable.like_icon)
                        imageView.tag = "like"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("checkLikeStatus", "Error checking like status: $exception")
                }
        }

        private fun addLikeToFirestore(postId: String) {
            val likeData = hashMapOf(
                auth.currentUser!!.uid to true
            )
            firestore.collection("Likes").document(postId).set(likeData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("addLikeToFirestore", "Like added successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("addLikeToFirestore", "Error adding like: $exception")
                }
        }

        private fun removeLikeFromFirestore(postId: String) {
            firestore.collection("Likes").document(postId)
                .update(auth.currentUser!!.uid, FieldValue.delete())
                .addOnSuccessListener {
                    Log.d("removeLikeFromFirestore", "Like removed successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("removeLikeFromFirestore", "Error removing like: $exception")
                }
        }

        //for image url
        private fun fetchUserProfile(username: String) {
            firestore.collection("Users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val userDocument = querySnapshot.documents[0]
                        val user = userDocument.toObject(Users::class.java)
                        user?.let {
                            Glide.with(binding.root)
                                .load(it.imageUrl)
                                .into(binding.imgProfile)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("fetchUserProfile", "Error fetching user profile: $exception")
                }
        }

        //save

        private fun addSaveToFirebase(postId: String){
            val savedData = hashMapOf(
                postId to true
            )
            firestore.collection("Saves").document(auth.currentUser!!.uid)
                .set(savedData,SetOptions.merge())
                .addOnSuccessListener {
                Log.d("addSavedToFirestore", "Save added successfully")
            }
                .addOnFailureListener { exception ->
                    Log.e("addSavedToFirestore", "Error adding save: $exception")
                }
        }

        private fun removeSaveFromFirestore(postId: String){
            firestore.collection("Saves").document(auth.currentUser!!.uid)
                .update(postId, FieldValue.delete())
                .addOnSuccessListener {
                    Log.d("removeSaveFromFirestore", "Save removed successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("removeSaveFromFirestore", "Error removing save: $exception")
                }
        }

        private fun checkSaveStatus(postId: String, imageView: ImageView) {
            firestore.collection("Saves").document(auth.currentUser!!.uid).get()
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


        private fun toggleSaveStatus(postId: String, imageView: ImageView) {
            val tag = imageView.tag?.toString() ?: ""

            if (tag == "saved") {
                imageView.setImageResource(R.drawable.save_icon)
                imageView.tag = "save"
                removeSaveFromFirestore(postId)
            } else {
                imageView.setImageResource(R.drawable.icons8_saved_icon)
                imageView.tag = "saved"
                addSaveToFirebase(postId)
            }
        }
        }

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



