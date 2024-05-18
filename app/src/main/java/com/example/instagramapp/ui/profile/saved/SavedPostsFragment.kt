package com.example.instagramapp.ui.profile.saved

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentSavedPostsBinding
import com.example.instagramapp.ui.main.comment.CommentsBottomSheetFragment
import com.example.instagramapp.ui.search.adapter.PostSearchAdapter
import com.example.instagramapp.base.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SavedPostsFragment : Fragment() {
    private lateinit var binding: FragmentSavedPostsBinding
    val viewModel: SavedPostsViewModel by viewModels()
    private lateinit var postAdapter: PostSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSavedPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListener()
        viewModel.fetchSavedPosts()
        setupRecyclerView()
        viewModel.savedPosts.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    postAdapter.submitList(resource.data)
                }

                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${resource.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun initListener() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack() //TODO migrate back button callbacks to popbackstack
        }
    }

    private fun setupRecyclerView() {
        postAdapter = PostSearchAdapter(itemClick = {
        }, commentButtonClick = { postId ->
            val bottomSheet = CommentsBottomSheetFragment.newInstance(postId)
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }, likeButtonClick = { postId, imageView ->
            val tag = imageView.tag?.toString() ?: ""
            if (tag == getString(R.string.liked)) {
                viewModel.toggleLikeStatus(postId, tag)
                imageView.setImageResource(R.drawable.like_icon)
                imageView.tag = getString(R.string.unlike)
            } else {
                viewModel.toggleLikeStatus(postId, tag)
                imageView.setImageResource(R.drawable.icon_liked)
                imageView.tag = getString(R.string.liked)
            }
        }, saveButtonClick = { postId, imageView ->
            val tag = imageView.tag?.toString() ?: ""
            if (tag == getString(R.string.saved)) {
                viewModel.toggleSaveStatus(postId, tag)
                imageView.setImageResource(R.drawable.save_icon)
                imageView.tag = getString(R.string.save)
            } else {
                viewModel.toggleSaveStatus(postId, tag)
                imageView.setImageResource(R.drawable.icons8_saved_icon)
                imageView.tag = getString(R.string.saved)
            }
        })
        binding.rvSaved.adapter = postAdapter
    }
}