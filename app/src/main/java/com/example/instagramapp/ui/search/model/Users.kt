package com.example.instagramapp.ui.search.model

data class Users(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val bio: String = "",
    val imageUrl: String = ""
) {
//    constructor(username: String, imageUrl: String) : this("", "", username, "", "", imageUrl)
//    constructor(username: String, imageUrl: String, bio: String) : this("", "", username, "", bio, imageUrl)
//    constructor() : this("", "")
}
