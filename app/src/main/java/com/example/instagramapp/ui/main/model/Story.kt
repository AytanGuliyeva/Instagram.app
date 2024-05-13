package com.example.instagramapp.ui.main.model

data class Story(
    val storyId: String,
    val userId: String,
    val imageUrl: String,
    val timeStart: Long,
    val timeEnd: Long,
)
