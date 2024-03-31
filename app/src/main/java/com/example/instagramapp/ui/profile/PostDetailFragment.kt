package com.example.instagramapp.ui.profile

import Post
import ProfileViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.instagramapp.databinding.FragmentPostDetailBinding
import com.example.instagramapp.ui.profile.PostDetailViewModel
import com.example.instagramapp.ui.search.UserDetailFragmentArgs
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource

class PostDetailFragment : Fragment() {
    private lateinit var binding: FragmentPostDetailBinding
    private val viewModel: PostDetailViewModel by viewModels()
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

        viewModel.fetchUserInformation(args.userId)
        viewModel.fetchPosts(args.postId)

    }

    private fun updateUserUI(user: Users) {
        binding.txtUsername2.text = user.username
        binding.txtUsername.text = user.username

    }

    private fun updatePostUI(post: Post) {
        binding.txtCaption.text = post.caption
        Glide.with(binding.root)
            .load(post.postImageUrl)
            .into(binding.imgPost)
    }

    private fun btnBack() {
        binding.btnBack.setOnClickListener {
            val action = PostDetailFragmentDirections.actionPostDetailFragmentToProfileFragment()
            findNavController().navigate(action)
        }
    }
}
