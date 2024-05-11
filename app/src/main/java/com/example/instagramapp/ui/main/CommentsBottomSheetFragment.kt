package com.example.instagramapp.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.instagramapp.databinding.FragmentCommentsBottomSheetBinding
import com.example.instagramapp.ui.main.adapters.CommentsAdapter
import com.example.instagramapp.ui.search.adapter.PostSearchAdapter
import com.example.instagramapp.util.Resource
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.UUID


class CommentsBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentCommentsBottomSheetBinding
    private val viewModel: CommentsBottomSheetViewModel by viewModels()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var commentAdapter: CommentsAdapter


    companion object {
        fun newInstance(postId: String): CommentsBottomSheetFragment {
            val fragment = CommentsBottomSheetFragment()
            val args = Bundle()
            args.putString("postId", postId)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var postId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommentsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        auth = FirebaseAuth.getInstance()
        arguments?.getString("postId")?.let {
            postId = it
        }
        // sentComment()
        btnPost()
        viewModel.readComment(postId)

        // setupRecyclerView()

        viewModel.commentResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Log.e("TAG", "onViewCreated: ${resource.data}")
                    commentAdapter.submitList(resource.data)
                }

                is Resource.Error -> {
                    // Handle error
                }

                is Resource.Loading -> {
                    // Show loading indicator
                }
            }
        }
//        viewModel.userResult.observe(viewLifecycleOwner) { resource ->
//            when (resource) {
//                is Resource.Success -> {
//                    val user = resource.data.firstOrNull()
//                    user?.let {
//                        Glide.with(requireContext())
//                            .load(user.imageUrl)
//                            .into(binding.profilImage)
//                    }
//                }
//                is Resource.Error -> {
//                    // Handle error
//                }
//                is Resource.Loading -> {
//                    // Show loading indicator
//                }
//            }
//        }


        viewModel.getCurrentUserProfileImage { imageUrl ->
            Glide.with(requireContext())
                .load(imageUrl)
                .into(binding.profilImage)
        }
    }


    fun btnPost() {
        binding.post.setOnClickListener {

            sentComment()
        }
    }


    fun addCommentToPost(postId: String, comment: String, userId: String) {
        val commentId = UUID.randomUUID().toString()

        val commentRef = firestore.collection("Comments").document(postId)

        val commentData = hashMapOf(
            "comment" to comment,
            "userId" to userId,
            "time" to com.google.firebase.Timestamp.now(),
            "commentId" to commentId
        )

        commentRef.set(mapOf(commentId to commentData), SetOptions.merge())
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->

                Log.e("Comments", "Error adding comment", e)
            }
    }

    fun sentComment() {
        val comment = binding.addToComment
        if (comment.text.trim().toString() == "") {
            Toast.makeText(requireContext(), "Please add the comment", Toast.LENGTH_SHORT).show()
            comment.text.clear()
        } else {
            addCommentToPost(postId, comment.text.toString(), auth.currentUser!!.uid)
            comment.text.clear()
        }
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentsAdapter()
        binding.rvComments.adapter = commentAdapter
    }

}