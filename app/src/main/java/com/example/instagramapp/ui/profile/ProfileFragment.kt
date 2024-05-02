package com.example.instagramapp.ui.profile

import Post
import ProfileViewModel
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentProfileBinding
import com.example.instagramapp.ui.profile.adapter.PostAdapter
import com.example.instagramapp.ui.search.SearchFragmentDirections
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var postAdapter: PostAdapter
    private val auth = Firebase.auth.currentUser!!.uid
    private val viewModel: ProfileViewModel by viewModels()
    private var selectedPost: Post? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSettings()
        btnEditProfile()
        setupRecyclerView()
        follow()
        viewModel.fetchUserInformation()
        viewModel.fetchPosts()



        viewModel.postResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    if (resource.data.isEmpty()) {
                        binding.txtNoPost.visibility = View.VISIBLE
                        binding.imgCamera.visibility = View.VISIBLE
                    } else {
                        binding.txtNoPost.visibility = View.GONE
                        binding.imgCamera.visibility = View.GONE
                    }
                    postAdapter.submitList(resource.data)
                    binding.progressBar.visibility = View.GONE
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE

                    Toast.makeText(requireContext(), "Error occurred!", Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
        viewModel.postSize.observe(viewLifecycleOwner){ postCount ->
            binding.txtPostCount.text = postCount.toString()
        }

        viewModel.followersCount.observe(viewLifecycleOwner) { followersCount ->
            binding.txtFollowersCount.text = followersCount.toString()
        }

        viewModel.followingCount.observe(viewLifecycleOwner) { followingCount ->
            binding.txtFollowingCount.text = followingCount.toString()
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
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(itemClick = {
            selectedPost = it
            postDetail(selectedPost!!.postId, selectedPost!!.userId)})
        binding.rvPost.adapter = postAdapter
    }

    fun postDetail(postId: String, userId: String) {
        if (selectedPost != null) {
            val action =
                ProfileFragmentDirections.actionProfileFragmentToPostDetailFragment(postId, userId)
            findNavController().navigate(action)
        }
    }

    private fun btnSettings() {
        binding.btnMenu.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingFragment)
        }
    }

    private fun updateUserUI(user: Users) {
        binding.txtUsername2.text = user.bio
        binding.txtProfileName.text = user.username
        Glide.with(binding.root)
            .load(user.imageUrl)
            .into(binding.imgProfile)
    }

    private fun btnEditProfile(){
        binding.btnEditProfile.setOnClickListener {
            val action=ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment()
            findNavController().navigate(action)
        }
    }

    private fun follow(){
        binding.txtFollowingCount.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToFollowFragment(auth)
            findNavController().navigate(action)
        }
        binding.txtFollowersCount.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToFollowFragment(auth)
            findNavController().navigate(action)
        }
    }

}
