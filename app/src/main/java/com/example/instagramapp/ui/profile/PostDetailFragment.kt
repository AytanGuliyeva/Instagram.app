package com.example.instagramapp.ui.profile

import Post
import ProfileViewModel
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentPostDetailBinding
import com.example.instagramapp.ui.main.CommentsBottomSheetFragment
import com.example.instagramapp.ui.profile.PostDetailViewModel
import com.example.instagramapp.ui.search.UserDetailFragmentArgs
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostDetailFragment : Fragment() {
    private lateinit var binding: FragmentPostDetailBinding
    private val viewModel: PostDetailViewModel by viewModels()
    val args: PostDetailFragmentArgs by navArgs()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnBack()


        viewModel.userInformation.observe(viewLifecycleOwner) { userResource ->
            when (userResource) {
                is Resource.Success -> {
                    updateUserUI(userResource.data)
                    binding.progressBar.visibility = View.GONE

                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                }

                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
        viewModel.postResult.observe(viewLifecycleOwner) { postResource ->
            when (postResource) {
                is Resource.Success -> {
                    updatePostUI(postResource.data)
                    binding.btnShare.setOnClickListener {
                        //        shareWithWp(postResource.data)

                    }
                    binding.progressBar.visibility = View.GONE

                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                }

                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
        viewModel.commentCount.observe(viewLifecycleOwner) { commentResource ->
            when (commentResource) {
                is Resource.Success -> {
                    val commentText = "View all ${commentResource.data} comments"
                    binding.txtComment.text = commentText

                }

                is Resource.Error -> {
                }

                is Resource.Loading -> {
                }
            }
        }
        viewModel.fetchUserInformation(args.userId)
        Log.e("TAG", "onViewCreated: ${args.postId}")
        viewModel.fetchPosts(args.postId)
        viewModel.fetchCommentCount(args.postId)


    }

    private fun updateUserUI(user: Users) {
        binding.txtUsername2.text = user.username
        binding.txtUsername.text = user.username
        Glide.with(binding.root)
            .load(user.imageUrl)
            .into(binding.imgProfile)

    }

    private fun updatePostUI(post: Post) {
        viewModel.checkLikeStatus(post.postId, binding.btnLike)
        viewModel.checkSaveStatus(post.postId, binding.btnSaved)
        viewModel.likeCount(binding.txtLikes, post.postId)
        binding.txtCaption.text = post.caption
        Glide.with(binding.root)
            .load(post.postImageUrl)
            .into(binding.imgPost)

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
        binding.btnLike.setOnClickListener {
            viewModel.toggleLikeStatus(post.postId, binding.btnLike)
        }
        binding.btnSaved.setOnClickListener {
            viewModel.toggleSaveStatus(post.postId, binding.btnSaved)
        }
        binding.btnComment.setOnClickListener {
            val bottomSheet = CommentsBottomSheetFragment.newInstance(post.postId)
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }
        binding.txtComment.setOnClickListener {
            val bottomSheet = CommentsBottomSheetFragment.newInstance(post.postId)
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

    }


    private fun btnBack() {
        binding.btnBack.setOnClickListener {
            val action = PostDetailFragmentDirections.actionPostDetailFragmentToProfileFragment()
            findNavController().popBackStack()
        }
    }
}