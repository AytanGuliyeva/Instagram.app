package com.example.instagramapp.ui.search

import SearchViewModel
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentSearchBinding
import com.example.instagramapp.ui.search.adapter.PostSearchAdapter
import com.example.instagramapp.ui.search.adapter.UserAdapter
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var postAdapter: PostSearchAdapter
    private val viewModel: SearchViewModel by viewModels()
    private var selectedUser:Users?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeUserResult()
        observePostResult()
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            viewModel.fetchOtherUsersPosts(currentUserUid)
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsersByUsername(newText)
                return true
            }
        })

    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(
            itemClick = {
                selectedUser=it;userDetail(selectedUser!!.userId)
            }
        )
        binding.rvUsername.adapter = userAdapter
        postAdapter = PostSearchAdapter()
        binding.rvPost.adapter = postAdapter
    }

    private fun observeUserResult() {
        viewModel.userResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    userAdapter.submitList(resource.data)
                    binding.rvUsername.visibility = View.VISIBLE
                }
                is Resource.Loading -> {
                    //  loading indicator if needed
                }
                is Resource.Error -> {
                    // Handle error state, e.g., show error message
                }
            }
        }
    }

    private fun observePostResult(){
        viewModel.postResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    postAdapter.submitList(resource.data)
                }
                is Resource.Loading -> {
                    // Show loading indicator if needed
                }
                is Resource.Error -> {
                    // Handle error state, e.g., show error message
                }
            }
        }
    }
    private fun filterUsersByUsername(query: String?) {
        query?.let { viewModel.searchUsers(it) }
    }

     fun userDetail(userId:String){
        if (selectedUser !=null){
            val action=SearchFragmentDirections.actionSearchFragmentToUserDetailFragment(userId)
            findNavController().navigate(action)
        }
    }
}
