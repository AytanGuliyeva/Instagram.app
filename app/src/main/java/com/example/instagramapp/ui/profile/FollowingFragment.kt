package com.example.instagramapp.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentFollowingBinding
import com.example.instagramapp.ui.search.SearchFragmentDirections
import com.example.instagramapp.ui.search.adapter.UserAdapter
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import dagger.hilt.android.AndroidEntryPoint

class FollowingFragment : Fragment() {
    private lateinit var binding: FragmentFollowingBinding
    private lateinit var userId: String

    private lateinit var userAdapter: UserAdapter
     private  val viewModel: FollowingViewModel by viewModels()
    private var selectedUser: Users? = null
  //  val args: FollowingFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFollowingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = arguments?.getString("userId") ?: ""
        setupRecyclerView()
        viewModel.fetchFollowing(userId)
        observeFollowings()

    }

    private fun observeFollowings() {
        viewModel.followingResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    userAdapter.submitList(resource.data)
                }

                is Resource.Loading -> {
                    // Show loading indicator if needed
                }

                is Resource.Error -> {
                    // Handle error if needed
                }
            }
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(
                itemClick = {
                    selectedUser = it
                    userDetail(it.userId)
                }
        )
        binding.rvFollowing.adapter = userAdapter
    }

    fun userDetail(userId: String) {
        if (selectedUser != null) {
            val action = FollowingFragmentDirections.actionFollowingFragmentToUserDetailFragment(userId)
            findNavController().navigate(action)
        }
    }

}