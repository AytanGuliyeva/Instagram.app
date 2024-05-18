package com.example.instagramapp.data.model

import com.google.firebase.Timestamp

data class Chat(
    val receiverId: String,
    val username: String,
    val imageUrl: String,
    val lastMessage:String,
    val time: Timestamp,
    val seen: Boolean
)