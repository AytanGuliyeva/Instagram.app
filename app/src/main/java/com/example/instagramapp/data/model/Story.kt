package com.example.instagramapp.data.model

data class Story(
    val storyId: String="",
    val userId: String="",
    val imageUrl: String="",
    val timeStart: Long=0,
    val timeEnd: Long=0,
)
