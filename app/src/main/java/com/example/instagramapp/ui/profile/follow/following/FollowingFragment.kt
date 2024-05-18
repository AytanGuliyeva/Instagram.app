package com.example.instagramapp.ui.profile.follow.following

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.instagramapp.databinding.FragmentFollowingBinding
import com.example.instagramapp.ui.search.userDetail.adapter.UserAdapter
import com.example.instagramapp.data.model.Users
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.base.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FollowingFragment : Fragment() {
    private lateinit var binding: FragmentFollowingBinding

    private lateinit var userAdapter: UserAdapter
    val viewModel: FollowingViewModel by viewModels()
    private var selectedUser: Users? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFollowingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = arguments?.getString(ConstValues.USER_ID) ?: ""
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
            }
        )
        binding.rvFollowing.adapter = userAdapter
    }
}