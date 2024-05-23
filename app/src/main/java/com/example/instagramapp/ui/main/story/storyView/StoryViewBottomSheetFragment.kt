package com.example.instagramapp.ui.main.story.storyView

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.instagramapp.base.util.Resource
import com.example.instagramapp.data.model.Users
import com.example.instagramapp.databinding.FragmentStoryViewBottomSheetBinding
import com.example.instagramapp.ui.main.comment.adapter.CommentsAdapter
import com.example.instagramapp.ui.search.userDetail.adapter.UserAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StoryViewBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentStoryViewBottomSheetBinding
    private val viewModel: StoryViewBottomSheetViewModel by viewModels()
    private lateinit var userAdapter: UserAdapter

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStoryViewBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        val storyId = arguments?.getString("storyId") ?: ""
        val userId = arguments?.getString("userId") ?: ""
        viewModel.fetchStoryViewers(storyId, userId)
        viewModel.userResult.observe(viewLifecycleOwner) { userResource ->
            when (userResource) {
                is Resource.Success -> {
                  userAdapter.submitList(userResource.data)
                }
                is Resource.Error -> {
                    // Handle error
                }
                is Resource.Loading -> {
                    // Handle loading state
                }
            }
        }
    }
    private fun setupRecyclerView() {
        userAdapter = UserAdapter(
            itemClick = {

            }
        )
        binding.rvView.adapter = userAdapter
    }
}
