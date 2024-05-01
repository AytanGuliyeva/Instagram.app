package com.example.instagramapp.ui.search

import Post
import SearchViewModel
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentSearchBinding
import com.example.instagramapp.ui.profile.ProfileFragmentDirections
import com.example.instagramapp.ui.profile.adapter.PostAdapter
import com.example.instagramapp.ui.search.adapter.PostSearchAdapter
import com.example.instagramapp.ui.search.adapter.UserAdapter
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import dagger.hilt.android.AndroidEntryPoint

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var userAdapter: UserAdapter

    //  private lateinit var postAdapter: PostSearchAdapter
    private lateinit var postAdapter: PostAdapter
    private val viewModel: SearchViewModel by viewModels()
    private var selectedUser: Users? = null
    private var selectedPost: Post? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvUsername.visibility = View.GONE
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
                selectedUser = it
                userDetail(it.userId)
            }
        )
        binding.rvUsername.adapter = userAdapter
        postAdapter = PostAdapter(itemClick = {
            Log.e("TAG", "setupRecyclerView: ${it.postId} ")
            selectedPost = it
            postDetail(it.postId, it.userId)
        })
        binding.rvPost.adapter = postAdapter
    }

    private fun observeUserResult() {
      //  binding.searchView.setOnClickListener {
            viewModel.userResult.observe(viewLifecycleOwner) { resource ->
                when (resource) {
                    is Resource.Success<List<Users>> -> {
                        //    binding.searchView.setOnClickListener {
                        userAdapter.submitList(resource.data)
                        binding.rvUsername.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        //  }

                    }

                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.rvUsername.visibility = View.GONE
                    }

                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
       // }
    }

    private fun observePostResult() {
        viewModel.postResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success<List<Post>> -> {
                    postAdapter.submitList(resource.data)
                    binding.progressBar.visibility = View.GONE
                }

                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvPost.visibility = View.GONE
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

//    private fun filterUsersByUsername(query: String?) {
//        query?.let {
//            if (query.isNotEmpty())
//                viewModel.searchUsers(it) }
//    }
private fun filterUsersByUsername(query: String?) {
    query?.let { viewModel.searchUsers(it) }
}


    fun userDetail(userId: String) {
        if (selectedUser != null) {
            val action = SearchFragmentDirections.actionSearchFragmentToUserDetailFragment(userId)
            findNavController().navigate(action)
        }
    }

    fun postDetail(postId: String, userId: String) {
        if (selectedPost != null) {
            val action =
                SearchFragmentDirections.actionSearchFragmentToPostDetailFragment(postId, userId)
            findNavController().navigate(action)
        }
    }
}
