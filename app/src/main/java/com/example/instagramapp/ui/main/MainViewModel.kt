package com.example.instagramapp.ui.main

import Post
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.ConstValues
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _postResult = MutableLiveData<Resource<List<Post>>>()
    val postResult: LiveData<Resource<List<Post>>>
        get() = _postResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _userList = MutableLiveData<Resource<List<Users>>>()
    val userList: LiveData<Resource<List<Users>>>
        get() = _userList

    fun fetchPosts() {
        _loading.postValue(true)
        val currentUserUid = Firebase.auth.currentUser?.uid
        if (currentUserUid != null) {
            firestore.collection("Follow").document(currentUserUid).get()
                .addOnSuccessListener { documentSnapshot ->
                    val followData = documentSnapshot.data
                    if (followData != null) {
                        val followingUserIds = (followData["following"] as? Map<String, Boolean>)?.keys ?: emptySet()
                        firestore.collection("Posts")
                            .whereIn("userId", followingUserIds.toList())
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                val postList = mutableListOf<Post>()
                                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                for (document in querySnapshot.documents) {
                                    val post = document.toObject(Post::class.java)
                                    post?.let {
                                        val formattedTimestamp = dateFormat.format(post.time!!.toDate())
                                        val postWithFormattedTime = post.copy(time = Timestamp(Date(formattedTimestamp)))
                                        postList.add(postWithFormattedTime)
                                    }
                                }
                                _postResult.postValue(Resource.Success(postList))
                            }
                            .addOnFailureListener { exception ->
                                _postResult.postValue(Resource.Error(Exception("Failed to fetch posts")))
                            }
                    } else {
                        _postResult.postValue(Resource.Error(Exception("Failed to fetch follow data")))
                    }
                }
                .addOnFailureListener { exception ->
                    _postResult.postValue(Resource.Error(Exception("Failed to fetch follow data")))
                }
        } else {
            _postResult.postValue(Resource.Error(Exception("User not logged in")))
        }
    }
    fun fetchUsername(userId: String, callback: (String) -> Unit) {
        firestore.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toUser()
                val username = user?.username ?: ""
                callback(username)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to fetch username: ${exception.message}", exception)
                callback("") // Return an empty string if there's an error
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
