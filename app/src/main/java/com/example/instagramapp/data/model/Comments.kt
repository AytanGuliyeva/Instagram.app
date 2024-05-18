package com.example.instagramapp.data.model

import com.google.firebase.Timestamp

data class Comments(
    val comment: String,
    val userId: String,
    val postId: String,
    val commentId: String,
    val time: Timestamp
)
