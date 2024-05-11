package com.example.instagramapp.ui.search

import Post
import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Button
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.ConstValues
import com.example.instagramapp.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserDetailViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth.currentUser!!.uid

    private val _userInformation = MutableLiveData<Resource<Users>>()
    val userInformation: LiveData<Resource<Users>>
        get() = _userInformation

    private val _isFollowing = MutableLiveData<Boolean>()
    val isFollowing: LiveData<Boolean>
        get() = _isFollowing

    private val _followersCount = MutableLiveData<Int>()
    val followersCount: LiveData<Int>
        get() = _followersCount

    private val _followingCount = MutableLiveData<Int>()
    val followingCount: LiveData<Int>
        get() = _followingCount

    private val _postSize = MutableLiveData<Int>()
    val postSize: LiveData<Int>
        get() = _postSize

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _postResult = MutableLiveData<Resource<List<Post>>>()
    val postResult: LiveData<Resource<List<Post>>>
        get() = _postResult

    init {
        _isFollowing.value = false
    }

    fun fetchPosts(userId: String) {
        _loading.postValue(true)
        firestore.collection("Posts").get()
            .addOnSuccessListener { querySnapshot ->
                val postList = mutableListOf<Post>()
                for (document in querySnapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    post?.let {
                        if (userId == it.userId) {
                            val timestamp = it.time
                            val postWithTimestamp = it.copy(time = timestamp)
                            postList.add(postWithTimestamp)
                        }
                    }
                }
                _postResult.postValue(Resource.Success(postList))
                _postSize.postValue(postList.size)

            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed! ${exception.message}", exception)
            }
    }

    fun fetchFollowersCount(userId: String) {
        firestore.collection("Follow").document(userId)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    error.localizedMessage?.let {
                        return@addSnapshotListener
                    }
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val follow = documentSnapshot.data
                    if (follow != null) {
                        val followers = (follow["followers"] as? HashMap<*, *>)?.size ?: 0
                        _followersCount.postValue(followers)
                    } else {
                        _followersCount.postValue(0)
                    }
                }
            }

    }

    fun fetchFollowingCount(userId: String) {
        firestore.collection("Follow").document(userId)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    error.localizedMessage?.let {
                        return@addSnapshotListener
                    }
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val follow = documentSnapshot.data
                    if (follow != null) {
                        val following = (follow["following"] as? HashMap<*, *>)?.size ?: 0
                        _followingCount.postValue(following)
                    } else {
                        _followingCount.postValue(0)
                    }
                }
            }
    }


    fun followClickListener(userId: String) {
        if (_isFollowing.value == true) {
            firestore.collection("Follow").document(auth).update(
                "following.$userId",
                FieldValue.delete()
            ).addOnSuccessListener {
                firestore.collection("Follow").document(userId).update(
                    "followers.$auth",
                    FieldValue.delete()
                ).addOnSuccessListener {
                    _isFollowing.postValue(false)
                }.addOnFailureListener { exception ->
                    Log.e("UserDetailViewModel", "Error updating follower data: $exception")
                }
            }.addOnFailureListener { exception ->
                Log.e("UserDetailViewModel", "Error updating following data: $exception")
            }
        } else {
            val following = hashMapOf<String, Boolean>()
            following[userId] = true

            val follower = hashMapOf<String, Boolean>()
            follower[auth] = true

            firestore.collection("Follow").document(auth)
                .set(mapOf("following" to following), SetOptions.merge())
                .addOnSuccessListener {
                    firestore.collection("Follow").document(userId)
                        .set(mapOf("followers" to follower), SetOptions.merge())
                        .addOnSuccessListener {
                            _isFollowing.postValue(true)
                        }.addOnFailureListener { exception ->
                            Log.e("UserDetailViewModel", "Error updating follower data: $exception")
                        }
                }.addOnFailureListener { exception ->
                    Log.e("UserDetailViewModel", "Error updating following data: $exception")
                }
        }
    }

    fun fetchUserInformation(userId: String) {
        _userInformation.postValue(Resource.Loading)
        firestore.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toUser()
                    if (user != null) {
                        _userInformation.postValue(Resource.Success(user))
                    } else {
                        _userInformation.postValue(Resource.Error(Exception("User data is null")))
                    }
                } else {
                    _userInformation.postValue(Resource.Error(Exception("User document does not exist")))
                }
            }
            .addOnFailureListener { exception ->
                _userInformation.postValue(Resource.Error(exception))
            }
    }

    fun checkIsFollowing(userId: String) {
        firestore.collection("Follow").document(auth)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val follow = documentSnapshot.data
                if (follow != null) {
                    val following = follow["following"] as? HashMap<*, *>
                    _isFollowing.postValue(following?.containsKey(userId) ?: false)
                } else {
                    _isFollowing.postValue(false)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserDetailViewModel", "Error getting follow data: $exception")
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
                userId ?: "",
                username ?: "",
                email ?: "",
                password ?: "",
                bio ?: "",
                imageUrl ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }
}
