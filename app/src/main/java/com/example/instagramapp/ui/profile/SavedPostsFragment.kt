package com.example.instagramapp.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.instagramapp.databinding.FragmentSavedPostsBinding
import com.example.instagramapp.ui.search.adapter.PostSearchAdapter
import com.example.instagramapp.util.Resource


class SavedPostsFragment : Fragment() {
    private lateinit var binding: FragmentSavedPostsBinding
    private val viewModel: SavedPostsViewModel by viewModels()
    private lateinit var postAdapter: PostSearchAdapter



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentSavedPostsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        btnBack()
        viewModel.fetchSavedPosts()
        setupRecyclerView()
        viewModel.savedPosts.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show loading progress if needed
                }

                is Resource.Success -> {
                    postAdapter.submitList(resource.data)
                }

                is Resource.Error -> {
                    // Handle error scenario
                    Toast.makeText(
                        requireContext(),
                        "Error: ${resource.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    private fun btnBack(){
        binding.btnBack.setOnClickListener {
            val action= SavedPostsFragmentDirections.actionSavedPostsFragmentToSettingFragment()
            findNavController().navigate(action)
        }
    }
    private fun setupRecyclerView() {
        postAdapter = PostSearchAdapter(itemClick = {
            // selectedPost = it;postDetail(selectedPost!!.postId, selectedPost!!.userId)
        })
        binding.rvSaved.adapter = postAdapter
    }


}