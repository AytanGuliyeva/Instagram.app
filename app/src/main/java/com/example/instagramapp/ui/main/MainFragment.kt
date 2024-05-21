package com.example.instagramapp.ui.main

import com.example.instagramapp.data.model.Post
import android.app.AlertDialog
import android.content.Context
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
import androidx.navigation.fragment.findNavController
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentMainBinding
import com.example.instagramapp.ui.main.comment.CommentsBottomSheetFragment
import com.example.instagramapp.ui.main.story.adapter.StoryAdapter
import com.example.instagramapp.ui.search.adapter.PostSearchAdapter
import com.example.instagramapp.base.util.Resource
import com.example.instagramapp.data.model.PostInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private var token:String?=null
    val viewModel: MainViewModel by viewModels()
    private lateinit var postAdapter: PostSearchAdapter
    private lateinit var storyAdapter: StoryAdapter

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readPreference()
        observePostData()
        firestore.collection(ConstValues.USERS).document(auth.currentUser!!.uid).update("token",token)
        initNavigationListener()
    }

    private fun observePostData() {
        viewModel.postResult.observe(viewLifecycleOwner) { resource ->
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
                        initRecyclerView(posts)
                        binding.progressBar.visibility = View.GONE
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_occurred), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
//        viewModel.likeCount.observe(viewLifecycleOwner){
//            postAdapter.updateLikeCount(it)
//        }

        viewModel.storyResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show loading indicator if needed
                }

                is Resource.Success -> {
                    val storyList = resource.data
                    storyAdapter = StoryAdapter(storyList,
                        storyClick = {
                            selectedStory(it)
                        },
                        addStoryClick = {
                            selectedAddStory()
                        }
                    )
                    binding.rvStory.adapter = storyAdapter
                }

                is Resource.Error -> {
                    // Handle error case if needed
                }
            }
        }
    }

    private fun selectedAddStory() {
        firestore.collection(ConstValues.STORY).document(auth.currentUser!!.uid)
            .get().addOnSuccessListener { value ->
                var count = 0
                if (value != null) {
                    try {
                        val stories = value.data as? HashMap<*, *>
                        if (stories != null) {
                            for (story in stories) {
                                val storyinfo = story.value as HashMap<*, *>
                                val timecurrent = System.currentTimeMillis()
                                val timeStart = storyinfo[ConstValues.TIME_START] as Long
                                val timeEnd = storyinfo[ConstValues.TIME_END] as Long
                                if (timecurrent in (timeStart + 1) until timeEnd) {
                                    ++count

                                }
                            }
                        }
                    } catch (_: NullPointerException) {
                    }
                    if (count > 0) {
                        val alert = AlertDialog.Builder(requireContext())
                        alert.setTitle(getString(R.string.view_story_or_add_story))
                        alert.setMessage(getString(R.string.do_you_want_to_view_the_story_or_add_a_new_one))
                        alert.setNegativeButton(getString(R.string.view_story)) { d, _ ->
                            val action =
                                MainFragmentDirections.actionMainFragmentToStoryFragment(auth.currentUser!!.uid)
                            findNavController().navigate(action)
                            d.dismiss()
                        }
                        alert.setPositiveButton(getString(R.string.add_story)) { d, _ ->
                            val action =
                                MainFragmentDirections.actionMainFragmentToAddStoryFragment()
                            findNavController().navigate(action)
                            d.dismiss()
                        }
                        alert.setCancelable(true)
                        alert.create().show()

                    } else {
                        val action =
                            MainFragmentDirections.actionMainFragmentToAddStoryFragment()
                        findNavController().navigate(action)
                    }
                }
            }.addOnFailureListener {}
    }

    private fun selectedStory(userId: String) {
        val action = MainFragmentDirections.actionMainFragmentToStoryFragment(userId)
        findNavController().navigate(action)
    }

    private fun readPreference() {
        activity?.let {
            val sharedPreferences = it.getSharedPreferences("userPreference", Context.MODE_PRIVATE)
            token = sharedPreferences.getString("token", "")
            Log.e("TAG", "readPreference: $token", )
        }
    }
    private fun initRecyclerView(posts: List<PostInfo>) {

        postAdapter = PostSearchAdapter(
            posts,
            itemClick = {
            // selectedPost = it;postDetail(selectedPost!!.postId, selectedPost!!.userId)
        },
            commentButtonClick = { postId ->
                val bottomSheet = CommentsBottomSheetFragment.newInstance(postId)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            },
            likeButtonClick = { postId, imageView ->
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
                //toggleLikeStatus(postId, imageView)

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

        binding.rvPost.adapter = postAdapter
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
        firestore.collection(ConstValues.LIKES).document(postId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("likeCount", "Error fetching like count: $error")
                    return@addSnapshotListener
                }
                if (value != null && value.exists()) {
                    val likesCount = value.data?.size ?: 0
                    val likesString = if (likesCount == 0) {
                        getString(R.string._0_likes)
                    } else if (likesCount == 1) {
                        getString(R.string._1_like)
                    } else {
                        "$likesCount likes"
                    }
                    likes.text = likesString
                } else {
                    likes.text = getString(R.string._0_likes)

                }
            }
    }

    private fun checkLikeStatus(postId: String, imageView: ImageView) {
        firestore.collection(ConstValues.LIKES).document(postId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val likedByCurrentUser =
                        document.getBoolean(auth.currentUser!!.uid) ?: false
                    if (likedByCurrentUser) {
                        imageView.setImageResource(R.drawable.icon_liked)
                        imageView.tag = getString(R.string.liked)
                    } else {
                        imageView.setImageResource(R.drawable.like_icon)
                        imageView.tag = getString(R.string.unlike)
                    }
                } else {
                    imageView.setImageResource(R.drawable.like_icon)
                    imageView.tag = getString(R.string.unlike)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("checkLikeStatus", "Error checking like status: $exception")
            }
    }

    private fun initNavigationListener() {
        binding.btnDm.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToDmFragment()
            findNavController().navigate(action)
        }
    }
}