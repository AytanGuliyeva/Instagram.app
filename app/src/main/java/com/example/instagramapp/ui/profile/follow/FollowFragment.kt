package com.example.instagramapp.ui.profile

import android.os.Bundle
import android.util.Log
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

    private lateinit var followerFragment: FollowerFragment
    private lateinit var followingFragment: FollowingFragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFollowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = args.userId

        followerFragment = FollowerFragment()
        followingFragment = FollowingFragment()
        val followerArgs = Bundle().also {
            it.putString("userId", userId)
        }
        fragmentList.add(followerFragment.also {
            it.arguments = followerArgs
        })

        fragmentList.add(followingFragment.also {
            it.arguments = followerArgs
        })
        Log.e("TAG", "onCreate: $userId")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter =
            PagerAdapter(childFragmentManager, lifecycle, followerFragment, followingFragment)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Followers"
                1 -> "Following"
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }.attach()

        btnBack()
    }

    private fun btnBack() {
        binding.btnBack.setOnClickListener {
            val action = FollowFragmentDirections.actionFollowFragmentToProfileFragment()
            findNavController().navigate(action)
        }
    }
}
