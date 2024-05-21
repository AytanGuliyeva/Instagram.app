package com.example.instagramapp.data.model

data class PostInfo(
    var user:Users?=null,
    var post: Post,
    var commentCount:Int=0,
    var likeCount:Int=0
)
