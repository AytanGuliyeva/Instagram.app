package com.example.instagramapp.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditProfileViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance().currentUser!!.uid
    private val _userInformation = MutableLiveData<Resource<Users>>()
    val userInformation: LiveData<Resource<Users>>
        get() = _userInformation

    fun getUserInfo() {
        firestore.collection("Users").document(auth).addSnapshotListener { value, error ->
            if (error != null) {
               // _userInformation.postValue(Resource.Error("Failed to fetch user information"))
            } else {
                val bio = value?.getString("bio") ?: ""
                val username = value?.getString("username") ?: ""
                val imageUrl = value?.getString("imageUrl") ?: ""
                _userInformation.postValue(Resource.Success(Users(username, imageUrl, bio)))
            }
        }
    }

    fun updateUserInfo(username: String, bio: String, imageUrl: String) {
        val userRef = firestore.collection("Users").document(auth)
        userRef.update(
            mapOf(
                "username" to username,
                "bio" to bio,
                "imageUrl" to imageUrl
            )
        ).addOnSuccessListener {
            // User information updated successfully
        }.addOnFailureListener { e ->
            // Handle failure
        }
    }
}
