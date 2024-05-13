package com.example.instagramapp.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapp.databinding.AddStoryBinding
import com.example.instagramapp.databinding.CommentsItemBinding
import com.example.instagramapp.databinding.ItemStoryBinding
import com.example.instagramapp.ui.main.model.Comments
import com.example.instagramapp.ui.main.model.Story

class StoryAdapter( private var storyList: List<Story>):RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

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
            if (position==0){
                binding.addStory.visibility= View.VISIBLE
            }else{
                binding.addStory.visibility=View.GONE
            }
//            binding.profilImage.setOnClickListener {
//                if (position==0){
//
//                }
//            }
        }
    }

}