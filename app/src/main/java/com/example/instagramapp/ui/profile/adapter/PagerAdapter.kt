package com.example.instagramapp.ui.profile.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.instagramapp.ui.profile.FollowerFragment
import com.example.instagramapp.ui.profile.FollowingFragment

class PagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    val followerFragment: FollowerFragment,
    val followingFragment: FollowingFragment
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> followerFragment
            1 -> followingFragment
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}