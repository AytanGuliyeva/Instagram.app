package com.example.instagramapp.ui.profile

import com.example.instagramapp.data.model.Post
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.data.model.Users
import com.example.instagramapp.base.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val firestore: FirebaseFirestore,
    val auth: FirebaseAuth
) : ViewModel() {

    private val _postResult = MutableLiveData<Resource<List<Post>>>()
    val postResult: LiveData<Resource<List<Post>>>
        get() = _postResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading
    private val _followersCount = MutableLiveData<Int>()
    val followersCount: LiveData<Int>
        get() = _followersCount

    private val _followingCount = MutableLiveData<Int>()
    val followingCount: LiveData<Int>
        get() = _followingCount

    private val _userInformation = MutableLiveData<Resource<Users>>()
    val userInformation: LiveData<Resource<Users>>
        get() = _userInformation

    private val _postSize = MutableLiveData<Int>()
    val postSize: LiveData<Int>
        get() = _postSize

    init {
        fetchFollowersCount()
        fetchFollowingCount()
    }

    fun fetchUserInformation() {
        _userInformation.postValue(Resource.Loading)
        firestore.collection(ConstValues.USERS)
            .document(auth.currentUser!!.uid)
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

    private fun fetchFollowersCount() {
        firestore.collection(ConstValues.FOLLOW).document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val follow = documentSnapshot.data
                if (follow != null) {
                    val followers = (follow[ConstValues.FOLLOWERS] as? HashMap<*, *>)?.size ?: 0
                    _followersCount.postValue(followers)
                } else {
                    _followersCount.postValue(0)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "com.example.instagramapp.ui.profile.ProfileViewModel",
                    "Error getting followers count: $exception"
                )
            }
    }

    private fun fetchFollowingCount() {
        firestore.collection(ConstValues.FOLLOW).document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val follow = documentSnapshot.data
                if (follow != null) {
                    val following = (follow[ConstValues.FOLLOWING] as? HashMap<*, *>)?.size ?: 0
                    _followingCount.postValue(following)
                } else {
                    _followingCount.postValue(0)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "com.example.instagramapp.ui.profile.ProfileViewModel",
                    "Error getting following count: $exception"
                )
            }
    }

    fun fetchPosts() {
        _loading.postValue(true)
        firestore.collection(ConstValues.POSTS).get()
            .addOnSuccessListener { querySnapshot ->
                val postList = mutableListOf<Post>()
                for (document in querySnapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    post?.let {
                        if (Firebase.auth.currentUser?.uid == it.userId) {
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

    companion object {
        private const val TAG = "com.example.instagramapp.ui.profile.ProfileViewModel"
    }
}
