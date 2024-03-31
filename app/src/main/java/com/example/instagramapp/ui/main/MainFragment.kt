package com.example.instagramapp.ui.main

import Post
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentMainBinding
import com.example.instagramapp.ui.profile.FollowerViewModel
import com.example.instagramapp.ui.profile.adapter.PostAdapter
import com.example.instagramapp.ui.search.adapter.PostSearchAdapter
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource


class MainFragment : Fragment() {
    private lateinit var binding:FragmentMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var postAdapter: PostSearchAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observePostData()
        viewModel.fetchPosts()
    }
    private fun observePostData() {
        viewModel.postResult.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val posts = resource.data
                    val postWithUsernames = mutableListOf<Pair<Post, String>>()
                    posts.forEach { post ->
                        viewModel.fetchUsername(post.userId) { username ->
                            postWithUsernames.add(Pair(post, username))
                            if (postWithUsernames.size == posts.size) {
                                postAdapter.submitList(postWithUsernames)
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    // Handle error state if needed
                }
            }
        })
    }

    private fun setupRecyclerView() {
        postAdapter = PostSearchAdapter(itemClick = {
           // selectedPost = it;postDetail(selectedPost!!.postId, selectedPost!!.userId)
        })
        binding.rvPost.adapter = postAdapter
    }


}