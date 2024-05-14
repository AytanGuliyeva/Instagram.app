package com.example.instagramapp.ui.main.story

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.instagramapp.databinding.FragmentStoryBinding
import com.example.instagramapp.ui.main.model.Story
import com.example.instagramapp.util.Resource
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import jp.shts.android.storiesprogressview.StoriesProgressView

class StoryFragment : Fragment(), StoriesProgressView.StoriesListener {
    private lateinit var binding: FragmentStoryBinding
    val args: StoryFragmentArgs by navArgs()
    private val viewModel: StoryViewModel by viewModels()
    private lateinit var storiesProgressView: StoriesProgressView
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var prestime: Long = 0L
    private var limit: Long = 500
    private var counter = 0
    val storyList = ArrayList<Story>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStoryBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storiesProgressView = binding.stories

        binding.storyDelete.visibility = View.GONE

        if (args.userId == auth.currentUser!!.uid) {
            binding.storyDelete.visibility = View.VISIBLE
        }
        viewModel.getStories(args.userId)
        viewModel.storyInformation.observe(viewLifecycleOwner) { storyResource ->
            when (storyResource) {
                is Resource.Success -> {
                    storyList.clear()
                    storyList.addAll(storyResource.data)
                    story()
                    Log.e("TAG", "onViewCreated: ${storyResource.data}", )
                }

                is Resource.Error -> {
                    //  binding.progressBar.visibility = View.GONE
                }

                is Resource.Loading -> {
                    // binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
        viewModel.fetchUserInformation(args.userId)
        viewModel.userInformation.observe(viewLifecycleOwner) { userResource ->
            when (userResource) {
                is Resource.Success -> {
                    val userInfo = userResource.data
                    val username = userInfo.username
                    val imageurl = userInfo.imageUrl
                    Glide.with(binding.root).load(imageurl).into(binding.storyPhoto)
                    binding.storyUsername.text = username
                    Log.e("TAG", "onViewCreated: $username", )
                }

                //   binding.progressBar.visibility = View.GONE


                is Resource.Error -> {
                    //  binding.progressBar.visibility = View.GONE
                }

                is Resource.Loading -> {
                    // binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
        //viewModel.getUserInfo(args.userId)
        binding.skip.setOnClickListener {
            binding.stories.skip()
        }
        binding.reverse.setOnClickListener {
            binding.stories.reverse()
        }
        binding.storyDelete.setOnClickListener {
            Snackbar.make(it, "Delete this story?", Snackbar.LENGTH_INDEFINITE)
                .setAction("Yes") {
                    firestore.collection("Story").document(args.userId)
                        .update(storyList[counter].storyId, FieldValue.delete())
                    findNavController().popBackStack()
                }.show()
        }
        binding.skip.setOnTouchListener(onTouchListener)
        binding.reverse.setOnTouchListener(onTouchListener)
    }

    private val onTouchListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    prestime = System.currentTimeMillis()
                    storiesProgressView.pause()
                    return false
                }

                MotionEvent.ACTION_UP -> {
                    val now = System.currentTimeMillis()
                    storiesProgressView.resume()
                    return limit < now - prestime
                }

            }
            return false
        }

    }

    override fun onNext() {
        Glide.with(binding.root).load(storyList[++counter].imageUrl).into(binding.image)
        viewModel.addView(storyList[counter].storyId, args.userId)
    }

    fun story() {
        if (storyList.isNotEmpty() && counter < storyList.size) {
            storiesProgressView.setStoriesCount(storyList.size)
            storiesProgressView.setStoryDuration(5000L)
            storiesProgressView.setStoriesListener(this)
            storiesProgressView.startStories(counter)
            Glide.with(binding.root).load(storyList[counter].imageUrl).into(binding.image)
            viewModel.addView(storyList[counter].storyId, args.userId)
        }
    }

    override fun onPrev() {
        if (counter - 1 < 0) return
        Glide.with(binding.root).load(storyList[--counter].imageUrl).into(binding.image)
    }

    override fun onComplete() {
        findNavController().popBackStack()
    }

    override fun onDestroy() {
        storiesProgressView.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        storiesProgressView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        storiesProgressView?.resume()
    }
//    override fun onRestart() {
//        storiesProgressView.resume()
//        super.onRestart()
//    }
}