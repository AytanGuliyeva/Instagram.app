package com.example.instagramapp.ui.profile.follow.follower

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.data.model.Users
import com.example.instagramapp.base.util.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FollowerViewModel @Inject constructor(val firestore: FirebaseFirestore) : ViewModel() {
    private val _followerList = MutableLiveData<Resource<List<Users>>>()
    val followerResult: LiveData<Resource<List<Users>>>
        get() = _followerList

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    fun fetchFollowers(userId: String) {
        _loading.value = true
        firestore.collection(ConstValues.FOLLOW).document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                try {
                    val idList = ArrayList<String>()
                    val follow = documentSnapshot.data
                    if (follow != null) {
                        val followersMap = follow[ConstValues.FOLLOWERS] as? HashMap<String, Boolean>
                        val followersIds = followersMap?.keys?.toList() ?: emptyList()
                        fetchUserDetails(followersIds)
                    } else {
                        _followerList.value = Resource.Error(Exception("No followers found"))
                        _loading.value = false
                    }
                } catch (e: Exception) {
                    _followerList.value = Resource.Error(e)
                    _loading.value = false
                }
            }
            .addOnFailureListener { exception ->
                _followerList.value = Resource.Error(exception)
                _loading.value = false
            }
    }

    private fun fetchUserDetails(userIds: List<String>) {
        val userDetails = mutableListOf<Users>()
        firestore.collection(ConstValues.USERS)
            .whereIn(ConstValues.USER_ID, userIds)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val user = document.toUser()
                    user?.let {
                        userDetails.add(it)
                    }
                }
                _followerList.value = Resource.Success(userDetails)
                _loading.value = false
            }
            .addOnFailureListener { exception ->
                Log.e("FollowerViewModel", "Error getting user details: $exception")
                _followerList.value = Resource.Error(exception)
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
