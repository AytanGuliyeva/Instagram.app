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

class FollowViewModel : ViewModel() {
//    private val firestore = FirebaseFirestore.getInstance()
//
//    private val _followingList = MutableLiveData<Resource<List<Users>>>()
//    val followingResult: LiveData<Resource<List<Users>>>
//        get() = _followingList
//
//    private val _followerList = MutableLiveData<Resource<List<Users>>>()
//    val followerResult: LiveData<Resource<List<Users>>>
//        get() = _followerList
//
//    private val _loading = MutableLiveData<Boolean>()
//    val loading: LiveData<Boolean>
//        get() = _loading
//
//    fun fetchFollowers(userId: String) {
//        firestore.collection("Follow").document(userId)
//            .get()
//            .addOnSuccessListener { documentSnapshot ->
//                val follow = documentSnapshot.data
//                if (follow != null) {
//                    val followersMap = follow["followers"] as? HashMap<String, Boolean>
//                    val followersIds = followersMap?.keys?.toList() ?: emptyList()
//
//                    fetchUserDetails(followersIds) { users ->
//                        Log.d("UserDetailViewModel", "Followers: $users")
//
//                        _followerList.value = Resource.Success(users)
//                    }
//                } else {
//                    _followerList.value = Resource.Error(Exception("No followers found"))
//                }
//            }
//            .addOnFailureListener { exception ->
//                _followerList.value = Resource.Error(exception)
//            }
//    }
//
//    fun fetchFollowing(userId: String) {
//        firestore.collection("Follow").document(userId)
//            .get()
//            .addOnSuccessListener { documentSnapshot ->
//                val follow = documentSnapshot.data
//                if (follow != null) {
//                    val followingMap = follow["following"] as? HashMap<String, Boolean>
//                    val followingIds = followingMap?.keys?.toList() ?: emptyList()
//
//                    fetchUserDetails(followingIds) { users ->
//                        Log.d("UserDetailViewModel", "Following: $users")
//                        _followingList.value = Resource.Success(users)
//                    }
//                } else {
//                    _followingList.value = Resource.Error(Exception("No following found"))
//                }
//            }
//            .addOnFailureListener { exception ->
//                _followingList.value = Resource.Error(exception)
//            }
//    }
//
//    private fun fetchUserDetails(userIds: List<String>, callback: (List<Users>) -> Unit) {
//        val userDetails = mutableListOf<Users>()
//        firestore.collection("Users")
//            .whereIn("userId", userIds)
//            .get()
//            .addOnSuccessListener { documents ->
//                for (document in documents) {
//                    val user = document.toUser()
//                    user?.let {
//                        userDetails.add(it)
//                    }
//                }
//                callback(userDetails)
//            }
//            .addOnFailureListener { exception ->
//                Log.e("FollowViewModel", "Error getting user details: $exception")
//                callback(emptyList())
//            }
//    }
//
//    private fun DocumentSnapshot.toUser(): Users? {
//        return try {
//            val userId = getString(ConstValues.USER_ID)
//            val username = getString(ConstValues.USERNAME)
//            val email = getString(ConstValues.EMAIL)
//            val password = getString(ConstValues.PASSWORD)
//            val bio = getString(ConstValues.BIO)
//            val imageUrl = getString(ConstValues.IMAGE_URL)
//
//            Users(
//                userId ?: "",
//                username ?: "",
//                email ?: "",
//                password ?: "",
//                bio ?: "",
//                imageUrl ?: ""
//            )
//        } catch (e: Exception) {
//            null
//        }
//    }
}
