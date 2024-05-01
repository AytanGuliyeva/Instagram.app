package com.example.instagramapp.ui.search

import Post
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentUserDetailBinding
import com.example.instagramapp.ui.profile.ProfileFragmentDirections
import com.example.instagramapp.ui.profile.adapter.PostAdapter
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import dagger.hilt.android.AndroidEntryPoint

class UserDetailFragment : Fragment() {
    private lateinit var binding: FragmentUserDetailBinding
    private val viewModel: UserDetailViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter
    val args: UserDetailFragmentArgs by navArgs()
    private var selectedPost:Post?=null




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = args.userId
        viewModel.fetchUserInformation(userId)
        viewModel.fetchFollowersCount(userId)
        viewModel.fetchFollowingCount(userId)
        viewModel.checkIsFollowing(userId)
        follow()

        observeUserResult()
        observeFollowerCount()
        observeFollowingCount()
        viewModel.fetchPosts(userId)
        setupRecyclerView()


        viewModel.isFollowing.observe(viewLifecycleOwner) { isFollowing ->
            if (isFollowing) {
                binding.btnFollow.setText(R.string.following)
            } else {
                binding.btnFollow.setText(R.string.follow)
            }
        }

        viewModel.postResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    if (resource.data.isEmpty()){
                        binding.txtNoPost.visibility = View.VISIBLE
                        binding.imgCamera.visibility=View.VISIBLE
                    } else {
                        binding.txtNoPost.visibility = View.GONE
                        binding.imgCamera.visibility=View.GONE
                    }
                    postAdapter.submitList(resource.data)
                    binding.progressBar.visibility=View.GONE
                }
                is Resource.Error -> {
                    binding.progressBar.visibility=View.GONE
                    Toast.makeText(requireContext(), "Error occurred!", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    binding.progressBar.visibility=View.VISIBLE
                }
            }
        }
        viewModel.postSize.observe(viewLifecycleOwner){ postCount ->
            binding.txtPostCount.text = postCount.toString()
        }
        btnFollow()
        btnBack()
    }

    private fun observeUserResult(){
        viewModel.userInformation.observe(viewLifecycleOwner){
                resource ->
            when (resource) {
                is Resource.Success -> {
                    val user = resource.data
                    updateUserUI(user)
                    binding.progressBar.visibility=View.GONE
                }
                is Resource.Loading -> {
                    binding.progressBar.visibility=View.VISIBLE
                }
                is Resource.Error -> {
                    binding.progressBar.visibility=View.GONE
                }
            }
        }
    }


    private fun setupRecyclerView() {
        postAdapter = PostAdapter(itemClick = {
            selectedPost=it;postDetail(selectedPost!!.postId,selectedPost!!.userId)
        })
        binding.rvPost.adapter = postAdapter
    }
    fun postDetail(postId:String,userId:String){
        if (selectedPost !=null){
            val action= UserDetailFragmentDirections.actionUserDetailFragmentToPostDetailFragment(postId,userId)
            findNavController().navigate(action)
        }
    }
    private fun observeFollowerCount() {
        viewModel.followersCount.observe(viewLifecycleOwner) { followersCount ->
            binding.txtFollowersCount.text = followersCount.toString()
        }
    }

    private fun observeFollowingCount() {
        viewModel.followingCount.observe(viewLifecycleOwner) { followingCount ->
            binding.txtFollowingCount.text = followingCount.toString()
        }
    }
    private fun updateUserUI(user: Users) {
        binding.txtUsername.text = user.username
        binding.txtUsername2.text=user.bio
        Glide.with(binding.root)
            .load(user.imageUrl)
            .into(binding.imgProfile)

    }
    private fun btnBack(){
        binding.btnBack.setOnClickListener {
            val action=UserDetailFragmentDirections.actionUserDetailFragmentToSearchFragment()
            findNavController().navigate(action)
        }
    }
    private fun btnFollow() {
        binding.btnFollow.setOnClickListener {
            viewModel.followClickListener(args.userId)
        }
    }
    private fun follow(){
        binding.txtFollowingCount.setOnClickListener {
            val action = UserDetailFragmentDirections.actionUserDetailFragmentToFollowFragment(args.userId)
            findNavController().navigate(action)
        }
        binding.txtFollowersCount.setOnClickListener {
            val action = UserDetailFragmentDirections.actionUserDetailFragmentToFollowFragment(args.userId)
            findNavController().navigate(action)
        }
    }

}
