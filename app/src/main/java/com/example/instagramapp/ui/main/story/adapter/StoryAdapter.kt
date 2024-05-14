package com.example.instagramapp.ui.main.story.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramapp.ConstValues
import com.example.instagramapp.R
import com.example.instagramapp.databinding.AddStoryBinding
import com.example.instagramapp.ui.main.model.Story
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StoryAdapter( private var storyList: List<Story>,
                    private val storyClick: (item: String) -> Unit,
                    private val addStoryClick: () -> Unit,

                    ):RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding =
            AddStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)    }

    override fun getItemCount(): Int {
      return storyList.size
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(storyList[position],position)
    }
    inner class StoryViewHolder(private val binding: AddStoryBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(story: Story,position: Int){
            userInfo(story.userId, position)
            if (position==0){
                binding.addStory.visibility= View.VISIBLE
            }else{
                binding.addStory.visibility=View.GONE
            }
            binding.profilImage.setOnClickListener {
                if (position == 0) {
                    addStoryClick()
                   // myStory(binding.username, true)
                } else {
                    storyClick(story.userId)
                }

            }
        }

        private fun userInfo(userId: String, position: Int) {
            firestore.collection("Users").document(userId)
                .get().addOnSuccessListener { value ->
                    if (value != null) {
                        val username = value.get(ConstValues.USERNAME) as String
                        val imageurl = value.get(ConstValues.IMAGE_URL) as String
                        if (position == 0) {
                            Glide.with(binding.root).load(imageurl).into(binding.profilImage)
                            binding.username.setText(R.string.add_story)
                            binding.profilImage.borderWidth = 0
                        } else {
                            Glide.with(binding.root).load(imageurl).into(binding.profilImage)
                            binding.profilImage.borderWidth = 6
                            binding.username.text = username
                        }
                    }


                }.addOnFailureListener {

                    it.localizedMessage?.let { it1 -> Log.e("user_error", it1) }
                }

        }
    }

}