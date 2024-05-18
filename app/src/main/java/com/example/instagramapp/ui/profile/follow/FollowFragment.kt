package com.example.instagramapp.ui.profile.follow

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentFollowBinding
import com.example.instagramapp.ui.profile.follow.adapter.PagerAdapter
import com.example.instagramapp.ui.profile.follow.follower.FollowerFragment
import com.example.instagramapp.ui.profile.follow.following.FollowingFragment
import com.example.instagramapp.ui.search.userDetail.adapter.UserAdapter
import com.example.instagramapp.data.model.Users
import com.example.instagramapp.base.util.ConstValues
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FollowFragment : Fragment() {
    private lateinit var binding: FragmentFollowBinding
    private lateinit var userAdapter: UserAdapter
    private val fragmentList = ArrayList<Fragment>()
    private var selectedUser: Users? = null
    val args: FollowFragmentArgs by navArgs()

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
            it.putString(ConstValues.USER_ID, userId)
        }
        fragmentList.add(followerFragment.also {
            it.arguments = followerArgs
        })

        fragmentList.add(followingFragment.also {
            it.arguments = followerArgs
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter =
            PagerAdapter(childFragmentManager, lifecycle, followerFragment, followingFragment)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.followers_)
                1 -> getString(R.string.following_)
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }.attach()
        initListener()
    }

    private fun initListener() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
