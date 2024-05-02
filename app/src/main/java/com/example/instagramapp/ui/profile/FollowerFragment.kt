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
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentFollowerBinding
import com.example.instagramapp.ui.search.SearchFragmentDirections
import com.example.instagramapp.ui.search.adapter.UserAdapter
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import kotlin.math.log

class FollowerFragment : Fragment() {
    private lateinit var binding: FragmentFollowerBinding
    private val  userAdapter by lazy {
        UserAdapter(
            itemClick = {
                selectedUser = it
                userDetail(it.userId)
            }
    ) }
    private val viewModel: FollowerViewModel by viewModels()
    private var selectedUser: Users? = null
    private lateinit var userId: String

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
        // val userId = arguments?.getString("userId") as String  <-- Bu satırı kaldırın
        viewModel.fetchFollowers(args.userId)
        Log.e("TAG", "onViewCreated: ${args.userId}", )
        setupRecyclerView()
        observeFollowers()
    }


    private fun observeFollowers() {
        viewModel.followerResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    userAdapter.submitList(resource.data)
                    Log.e("TAG", "ayten: ${resource.data}", )
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


    fun userDetail(userId: String) {
        val action = FollowerFragmentDirections.actionFollowerFragmentToUserDetailFragment(userId)
        findNavController().navigate(action)
    }

    private fun setupRecyclerView() {
//        userAdapter = UserAdapter(
//            itemClick = {
//                //electedUser=it
//            // userDetail(selectedUser!!.userId)
//            }
//        )
        binding.rvFollower.adapter = userAdapter
    }
}