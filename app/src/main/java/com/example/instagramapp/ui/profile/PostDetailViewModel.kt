package com.example.instagramapp.ui.profile

import Post
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.ConstValues
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class PostDetailViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth.currentUser!!.uid


    private val _userInformation = MutableLiveData<Resource<Users>>()
    val userInformation: LiveData<Resource<Users>>
        get() = _userInformation

    private val _postInformation = MutableLiveData<Resource<Post>>()
    val postInformation: LiveData<Resource<Post>>
        get() = _postInformation

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading




    private val _postResult = MutableLiveData<Resource<Post>>()
    val postResult: LiveData<Resource<Post>>
        get() = _postResult

    fun fetchPosts(postId: String) {
        _loading.postValue(true)
        val postDocumentRef = firestore.collection("Posts").document(postId)
        Log.d(TAG, "Fetching post with postId: $postId")

        postDocumentRef.get()
            .addOnSuccessListener { documentSnapshot ->
                Log.d(TAG, "Post fetch success")
                val post = documentSnapshot.toObject(Post::class.java)
                if (post != null) {
                    _postResult.postValue(Resource.Success(post))
                } else {
                    _postResult.postValue(Resource.Error(Exception("Post data is null")))
                }
                _loading.postValue(false)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Post fetch failure: ${exception.message}")
                _postResult.postValue(Resource.Error(exception))
                _loading.postValue(false)
            }
    }

    fun fetchUserInformation(userId:String) {
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

    companion object {
        private const val TAG = "PostDetailViewModel"
    }
}
