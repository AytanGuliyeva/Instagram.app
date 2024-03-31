package com.example.instagramapp.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentFollowBinding
import com.example.instagramapp.ui.profile.adapter.PagerAdapter
import com.example.instagramapp.ui.profile.adapter.PostAdapter
import com.example.instagramapp.ui.search.SearchFragmentDirections
import com.example.instagramapp.ui.search.UserDetailFragmentDirections
import com.example.instagramapp.ui.search.adapter.UserAdapter
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import com.google.android.material.tabs.TabLayoutMediator

class FollowFragment : Fragment() {
    private lateinit var binding: FragmentFollowBinding
    private lateinit var userAdapter: UserAdapter
    private val fragmentList = ArrayList<Fragment>()
    private val viewModel: FollowViewModel by viewModels()
    private var selectedUser: Users? = null
    val args: FollowFragmentArgs by navArgs()
    var tabTitle = arrayOf("Followers", "Following")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFollowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PagerAdapter(childFragmentManager, lifecycle)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Followers"
                1 -> "Following"
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }.attach()

//        setupRecyclerView(binding.rvFollower)
//        setupRecyclerView(binding.rvFollowing)

        btnBack()

//        viewModel.fetchFollowers(args.userId)
//        viewModel.fetchFollowing(args.userId)
//        observeFollowers()
//        observeFollowings()
        val userId = args?.userId ?: ""

        val followerArgs = Bundle().also {
            it.putString("userId", userId)
        }
        fragmentList.add(FollowerFragment().also {
            it.arguments = followerArgs
        })

        val followingArgs = Bundle().also {
            it.putString("userId", userId)
        }
        fragmentList.add(FollowingFragment().also {
            it.arguments = followingArgs
        })




    }

//    private fun observeFollowers() {
//        viewModel.followerResult.observe(viewLifecycleOwner) { resource ->
//            when (resource) {
//                is Resource.Success -> {
//                    userAdapter.submitList(resource.data)
//                }
//                is Resource.Loading -> {
//                    // Show loading indicator if needed
//                }
//                is Resource.Error -> {
//                    // Handle error if needed
//                }
//            }
//        }
//    }
//
//    private fun observeFollowings() {
//        viewModel.followingResult.observe(viewLifecycleOwner) { resource ->
//            when (resource) {
//                is Resource.Success -> {
//                    userAdapter.submitList(resource.data)
//                }
//                is Resource.Loading -> {
//                    // Show loading indicator if needed
//                }
//                is Resource.Error -> {
//                    // Handle error if needed
//                }
//            }
//        }
//    }
//
//    private fun setupRecyclerView(recyclerView: RecyclerView) {
//        userAdapter = UserAdapter(
//            itemClick = {
//                //electedUser=it;userDetail(selectedUser!!.userId)
//            }
//        )
//        recyclerView.adapter = userAdapter
//    }
//
//    private fun userDetail(userId: String) {
//        if (selectedUser != null) {
//            val action = SearchFragmentDirections.actionSearchFragmentToUserDetailFragment(userId)
//            findNavController().navigate(action)
//        }
//    }
//
    private fun btnBack() {
        binding.btnBack.setOnClickListener {
            val action = FollowFragmentDirections.actionFollowFragmentToProfileFragment()
            findNavController().navigate(action)
        }
    }
}
