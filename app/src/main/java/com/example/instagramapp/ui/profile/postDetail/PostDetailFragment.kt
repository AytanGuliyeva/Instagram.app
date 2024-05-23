package com.example.instagramapp.ui.profile.postDetail

import android.app.AlertDialog
import com.example.instagramapp.data.model.Post
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentPostDetailBinding
import com.example.instagramapp.ui.main.comment.CommentsBottomSheetFragment
import com.example.instagramapp.data.model.Users
import com.example.instagramapp.base.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class PostDetailFragment : Fragment() {
    private lateinit var binding: FragmentPostDetailBinding
    val viewModel: PostDetailViewModel by viewModels()
    val args: PostDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()

        viewModel.postDeleted.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    if (resource.data == true) {
                        Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error deleting post", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    // Show a loading indicator if needed
                }
            }
        }
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
                binding.txtTime.text = getString(R.string.just_now)
            } else if (minutesAgo < 60) {
                val timeAgoText = if (minutesAgo == 1) getString(R.string._1_minute_ago) else "$minutesAgo minutes ago"
                binding.txtTime.text = timeAgoText
            } else if (minutesAgo < 1440) {
                val hoursAgo = minutesAgo / 60
                val timeAgoText = if (hoursAgo == 1) getString(R.string._1_hour_ago) else "$hoursAgo hours ago"
                binding.txtTime.text = timeAgoText
            } else if (minutesAgo < 10080) {
                val daysAgo = minutesAgo / 1440
                val timeAgoText = if (daysAgo == 1) getString(R.string._1_day_ago) else "$daysAgo days ago"
                binding.txtTime.text = timeAgoText
            } else {
                val dateFormat = SimpleDateFormat(getString(R.string.dd_mmmm), Locale.getDefault())
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

    private fun initListener() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnOption.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deletePost(args.postId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}