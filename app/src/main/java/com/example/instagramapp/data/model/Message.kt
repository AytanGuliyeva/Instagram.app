package com.example.instagramapp.ui.main.model

import com.google.firebase.Timestamp

data class Message(
    val messageId: String,
    val messagetxt: String,
    val senderId: String,
    val time: Timestamp,
    val seen: Boolean
)