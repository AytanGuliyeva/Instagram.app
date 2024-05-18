package com.example.instagramapp.ui.profile.follow.follower

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.instagramapp.databinding.FragmentFollowerBinding
import com.example.instagramapp.ui.search.userDetail.adapter.UserAdapter
import com.example.instagramapp.data.model.Users
import com.example.instagramapp.base.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FollowerFragment : Fragment() {
    private lateinit var binding: FragmentFollowerBinding
    private val userAdapter by lazy {
        UserAdapter(
            itemClick = {
                selectedUser = it
            }
        )
    }
    val viewModel: FollowerViewModel by viewModels()
    private var selectedUser: Users? = null

    val args: FollowerFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFollowerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchFollowers(args.userId)
        setupRecyclerView()
        observeFollowers()
    }


    private fun observeFollowers() {
        viewModel.followerResult.observe(viewLifecycleOwner) { resource ->
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
        binding.rvFollower.adapter = userAdapter
    }
}