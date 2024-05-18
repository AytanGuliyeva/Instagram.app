package com.example.instagramapp.ui.search.userDetail

import com.example.instagramapp.data.model.Post
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.base.util.ConstValues
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.example.instagramapp.data.model.Users
import com.example.instagramapp.base.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    val firestore: FirebaseFirestore,
    val auth: FirebaseAuth
) : ViewModel() {

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
        firestore.collection(ConstValues.POSTS).get()
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
        firestore.collection(ConstValues.FOLLOW).document(userId)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    error.localizedMessage?.let {
                        return@addSnapshotListener
                    }
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val follow = documentSnapshot.data
                    if (follow != null) {
                        val followers = (follow[ConstValues.FOLLOWERS] as? HashMap<*, *>)?.size ?: 0
                        _followersCount.postValue(followers)
                    } else {
                        _followersCount.postValue(0)
                    }
                }
            }

    }

    fun fetchFollowingCount(userId: String) {
        firestore.collection(ConstValues.FOLLOW).document(userId)
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
            firestore.collection(ConstValues.FOLLOW).document(Firebase.auth.currentUser!!.uid)
                .update(
                    "following.$userId",
                    FieldValue.delete()
                ).addOnSuccessListener {
                firestore.collection(ConstValues.FOLLOW).document(userId).update(
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
            follower[Firebase.auth.currentUser!!.uid] = true

            firestore.collection(ConstValues.FOLLOW).document(Firebase.auth.currentUser!!.uid)
                .set(mapOf(ConstValues.FOLLOWING to following), SetOptions.merge())
                .addOnSuccessListener {
                    firestore.collection(ConstValues.FOLLOW).document(userId)
                        .set(mapOf(ConstValues.FOLLOWERS to follower), SetOptions.merge())
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
        firestore.collection(ConstValues.USERS)
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
        firestore.collection(ConstValues.FOLLOW).document(Firebase.auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val follow = documentSnapshot.data
                if (follow != null) {
                    val following = follow[ConstValues.FOLLOWING] as? HashMap<*, *>
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
