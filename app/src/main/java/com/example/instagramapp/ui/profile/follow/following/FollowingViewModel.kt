package com.example.instagramapp.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.ConstValues
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class FollowingViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _followingList = MutableLiveData<Resource<List<Users>>>()
    val followingResult: LiveData<Resource<List<Users>>>
        get() = _followingList

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    fun fetchFollowing(userId: String) {
        _loading.value = true
        firestore.collection("Follow").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                try {

                    val follow = documentSnapshot.data
                    if (follow != null) {
                        val followingMap = follow["following"] as? HashMap<String, Boolean>
                        val followingIds = followingMap?.keys?.toList() ?: emptyList()

                        fetchUserDetails(followingIds)
                    } else {
                        _followingList.value = Resource.Error(Exception("No following found"))
                        _loading.value = false
                    }
                } catch (e: Exception) {
                    _followingList.value = Resource.Error(e)
                    _loading.value = false
                }
            }
            .addOnFailureListener { exception ->
                _followingList.value = Resource.Error(exception)
                _loading.value = false
            }
    }

    private fun fetchUserDetails(userIds: List<String>) {
        val userDetails = mutableListOf<Users>()
        firestore.collection("Users")
            .whereIn("userId", userIds)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val user = document.toUser()
                    user?.let {
                        userDetails.add(it)
                    }
                }
                _followingList.value = Resource.Success(userDetails)
                _loading.value = false
            }
            .addOnFailureListener { exception ->
                Log.e("FollowingViewModel", "Error getting user details: $exception")
                _followingList.value = Resource.Error(exception)
                _loading.value = false
            }
    }

    private fun DocumentSnapshot.toUser(): Users? {
        return try {
            val userId = getString(ConstValues.USER_ID)
            val username = getString(ConstValues.USERNAME)
            val email = getString(ConstValues.EMAIL)
            val password = getString(ConstValues.PASSWORD)
            val bio = getString(ConstValues.BIO)
            val imageUrl = getString(ConstValues.IMAGE_URL)

            Users(
                userId.orEmpty(),
                username.orEmpty(),
                email.orEmpty(),
                password.orEmpty(),
                bio.orEmpty(),
                imageUrl.orEmpty(),
            )
        } catch (e: Exception) {
            null
        }
    }
}
