package com.example.instagramapp.ui.main.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentCommentsBottomSheetBinding
import com.example.instagramapp.ui.main.comment.adapter.CommentsAdapter
import com.example.instagramapp.base.util.Resource
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class CommentsBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentCommentsBottomSheetBinding
    val viewModel: CommentsBottomSheetViewModel by viewModels()
    private lateinit var commentAdapter: CommentsAdapter


    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore

    companion object {
        fun newInstance(postId: String): CommentsBottomSheetFragment {
            val fragment = CommentsBottomSheetFragment()
            val args = Bundle()
            args.putString(ConstValues.POST_ID, postId)
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
        arguments?.getString(ConstValues.POST_ID)?.let {
            postId = it
        }
        buttonPost()
        viewModel.readComment(postId)
        viewModel.commentResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
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
        viewModel.getCurrentUserProfileImage { imageUrl ->
            Glide.with(requireContext())
                .load(imageUrl)
                .into(binding.profilImage)
        }
    }

    fun buttonPost() {
        binding.post.setOnClickListener {
            sentComment()
        }
    }


    fun addCommentToPost(postId: String, comment: String, userId: String) {
        val commentId = UUID.randomUUID().toString()

        val commentRef = firestore.collection(ConstValues.COMMENTS).document(postId)

        val commentData = hashMapOf(
            ConstValues.COMMENT to comment,
            ConstValues.USER_ID to userId,
            ConstValues.TIME to com.google.firebase.Timestamp.now(),
            ConstValues.COMMENTID to commentId
        )

        commentRef.set(mapOf(commentId to commentData), SetOptions.merge())
            .addOnSuccessListener {}
            .addOnFailureListener {}
    }

    fun sentComment() {
        val comment = binding.addToComment
        if (comment.text.trim().toString() == "") {
            Toast.makeText(
                requireContext(),
                getString(R.string.please_add_the_comment), Toast.LENGTH_SHORT
            ).show()
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