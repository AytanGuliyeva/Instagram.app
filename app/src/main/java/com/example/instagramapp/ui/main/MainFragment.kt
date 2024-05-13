package com.example.instagramapp.ui.main

import Post
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentMainBinding
import com.example.instagramapp.ui.main.adapters.StoryAdapter
import com.example.instagramapp.ui.search.adapter.PostSearchAdapter
import com.example.instagramapp.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions


class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var postAdapter: PostSearchAdapter
    private lateinit var storyAdapter: StoryAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observePostData()
        // viewModel.fetchPosts()
    }

    private fun observePostData() {
        viewModel.postResult.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Resource.Success -> {
                    if (resource.data.isEmpty()) {
                        binding.txtNoPost.visibility = View.VISIBLE
                        binding.imgCamera.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                    } else {
                        binding.txtNoPost.visibility = View.GONE
                        binding.imgCamera.visibility = View.GONE
                        val posts = resource.data
                        val postWithUsernames = mutableListOf<Pair<Post, String>>()
//                        posts.forEach { post ->
//                                postWithUsernames.add(Pair(post, ""))
//                                if (postWithUsernames.size == posts.size) {
////                                    postAdapter.submitList(postWithUsernames)
////                                    binding.progressBar.visibility = View.GONE
//                                }
//                        }
                        postAdapter.submitList(posts)
                        binding.progressBar.visibility = View.GONE
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error occurred!", Toast.LENGTH_SHORT).show()
                }
            }
        })
//        viewModel.likeCount.observe(viewLifecycleOwner){
//            postAdapter.updateLikeCount(it)
//        }
    }


    private fun setupRecyclerView() {
        postAdapter = PostSearchAdapter(itemClick = {
            // selectedPost = it;postDetail(selectedPost!!.postId, selectedPost!!.userId)
        },
            commentButtonClick = { postId ->
                val bottomSheet = CommentsBottomSheetFragment.newInstance(postId)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            },
            likeButtonClick = { postId, imageView ->
                val tag = imageView.tag?.toString() ?: ""
                if (tag == "liked") {
                    viewModel.toggleLikeStatus(postId, tag)
                    imageView.setImageResource(R.drawable.like_icon)
                    imageView.tag = "like"
                } else {
                    viewModel.toggleLikeStatus(postId, tag)
                    imageView.setImageResource(R.drawable.icon_liked)
                    imageView.tag = "liked"
                }
                //toggleLikeStatus(postId, imageView)

            }, saveButtonClick = { postId, imageView ->
                val tag = imageView.tag?.toString() ?: ""
                if (tag == "saved") {
                    viewModel.toggleSaveStatus(postId, tag)
                    imageView.setImageResource(R.drawable.save_icon)
                    imageView.tag = "save"
                } else {
                    viewModel.toggleSaveStatus(postId, tag)
                    imageView.setImageResource(R.drawable.icons8_saved_icon)
                    imageView.tag = "saved"
                }
            })

        binding.rvPost.adapter = postAdapter

      //  storyAdapter=StoryAdapter()
    }

    //like
//    private fun toggleLikeStatus(postId: String, imageView: ImageView) {
//        val tag = imageView.tag?.toString() ?: ""
//
//        if (tag == "liked") {
//            imageView.setImageResource(R.drawable.like_icon)
//            imageView.tag = "like"
//            removeLikeFromFirestore(postId)
//        } else {
//            imageView.setImageResource(R.drawable.icon_liked)
//            imageView.tag = "liked"
//            addLikeToFirestore(postId)
//        }
//    }

    private fun likeCount(likes: TextView, postId: String) {
        firestore.collection("Likes").document(postId).addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("likeCount", "Error fetching like count: $error")
                return@addSnapshotListener
            }
            if (value != null && value.exists()) {
                val likesCount = value.data?.size ?: 0
                val likesString = if (likesCount == 0) {
                    "0 likes"
                } else if (likesCount == 1) {
                    "1 like"
                } else {
                    "$likesCount likes"
                }
                likes.text = likesString
            } else {
                likes.text = "0 likes"
            }
        }
    }

    private fun checkLikeStatus(postId: String, imageView: ImageView) {
        firestore.collection("Likes").document(postId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val likedByCurrentUser =
                        document.getBoolean(auth.currentUser!!.uid) ?: false
                    if (likedByCurrentUser) {
                        imageView.setImageResource(R.drawable.icon_liked)
                        imageView.tag = "liked"
                    } else {
                        imageView.setImageResource(R.drawable.like_icon)
                        imageView.tag = "like"
                    }
                } else {
                    imageView.setImageResource(R.drawable.like_icon)
                    imageView.tag = "like"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("checkLikeStatus", "Error checking like status: $exception")
            }
    }

//    private fun addLikeToFirestore(postId: String) {
//        val likeData = hashMapOf(
//            auth.currentUser!!.uid to true
//        )
//        firestore.collection("Likes").document(postId).set(likeData, SetOptions.merge())
//            .addOnSuccessListener {
//                Log.d("addLikeToFirestore", "Like added successfully")
//            }
//            .addOnFailureListener { exception ->
//                Log.e("addLikeToFirestore", "Error adding like: $exception")
//            }
//    }
//
//    private fun removeLikeFromFirestore(postId: String) {
//        firestore.collection("Likes").document(postId)
//            .update(auth.currentUser!!.uid, FieldValue.delete())
//            .addOnSuccessListener {
//                Log.d("removeLikeFromFirestore", "Like removed successfully")
//            }
//            .addOnFailureListener { exception ->
//                Log.e("removeLikeFromFirestore", "Error removing like: $exception")
//            }
//    }
}