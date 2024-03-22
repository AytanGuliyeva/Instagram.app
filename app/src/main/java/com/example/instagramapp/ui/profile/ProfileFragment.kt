package com.example.instagramapp.ui.profile

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
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentProfileBinding
import com.example.instagramapp.ui.profile.adapter.PostAdapter
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
        setupRecyclerView()
        viewModel.fetchUserInformation()

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
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error occurred!", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    //progress
                }
            }
        }
        viewModel.fetchPosts()

        viewModel.followersCount.observe(viewLifecycleOwner) { followersCount ->
            binding.txtFollowersCount.text = followersCount.toString()
        }

        viewModel.followingCount.observe(viewLifecycleOwner) { followingCount ->
            binding.txtFollowingCount.text = followingCount.toString()
        }

        viewModel.userInformation.observe(viewLifecycleOwner) { userResource ->
            when (userResource) {
                is Resource.Success -> updateUserUI(userResource.data)
                is Resource.Error -> {
                    // Handle error
                }
                is Resource.Loading -> {
                    // Handle loading
                }
            }
        }
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()
        binding.rvPost.adapter = postAdapter
    }

    private fun btnSettings() {
        binding.btnMenu.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingFragment)
        }
    }

    private fun updateUserUI(user: Users) {
        binding.txtUsername2.text=user.bio
        binding.txtProfileName.text=user.username
    }
}
