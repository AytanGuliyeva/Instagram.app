package com.example.instagramapp.ui.profile

import Post
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.ConstValues
import com.example.instagramapp.R
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
    private val _commentCount = MutableLiveData<Resource<Int>>()
    val commentCount: LiveData<Resource<Int>>
        get() = _commentCount

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

    fun fetchCommentCount(postId: String) {
        firestore.collection("Comments").document(postId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val comments = documentSnapshot.data?.size ?: 0

                _commentCount.postValue(Resource.Success(comments))
            }
            .addOnFailureListener { exception ->
                _commentCount.postValue(Resource.Error(exception))

                Log.e("PostSearchAdapter", "Error getting comment count: $exception")
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

    fun toggleLikeStatus(postId: String, imageView: ImageView) {
        val tag = imageView.tag?.toString() ?: ""

        if (tag == "liked") {
            imageView.setImageResource(R.drawable.like_icon)
            imageView.tag = "like"
            removeLikeFromFirestore(postId)
        } else {
            imageView.setImageResource(R.drawable.icon_liked)
            imageView.tag = "liked"
            addLikeToFirestore(postId)
        }
    }

    fun likeCount(likes: TextView, postId: String) {
        firestore.collection("Likes").document(postId).addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("likeCount", "Error fetching like count: $error")
                return@addSnapshotListener
            }
            if (value != null && value.exists()) {
                val likesCount = value.data?.size ?: 0
                val likesString = if (likesCount == 0) {
                    "0 likes"
                } else if (likesCount == 1) {
                    "1 like"
                } else {
                    "$likesCount likes"
                }
                likes.text = likesString
            } else {
                likes.text = "0 likes"
            }
        }
    }


    fun checkLikeStatus(postId: String, imageView: ImageView) {
        firestore.collection("Likes").document(postId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val likedByCurrentUser =
                        document.getBoolean(auth) ?: false
                    if (likedByCurrentUser) {
                        imageView.setImageResource(R.drawable.icon_liked)
                        imageView.tag = "liked"
                    } else {
                        imageView.setImageResource(R.drawable.like_icon)
                        imageView.tag = "like"
                    }
                } else {
                    imageView.setImageResource(R.drawable.like_icon)
                    imageView.tag = "like"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("checkLikeStatus", "Error checking like status: $exception")
            }
    }

    private fun addLikeToFirestore(postId: String) {
        val likeData = hashMapOf(
            auth to true
        )
        firestore.collection("Likes").document(postId).set(likeData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("addLikeToFirestore", "Like added successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("addLikeToFirestore", "Error adding like: $exception")
            }
    }

    private fun removeLikeFromFirestore(postId: String) {
        firestore.collection("Likes").document(postId)
            .update(auth, FieldValue.delete())
            .addOnSuccessListener {
                Log.d("removeLikeFromFirestore", "Like removed successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("removeLikeFromFirestore", "Error removing like: $exception")
            }
    }
    //save

    fun addSaveToFirebase(postId: String) {
        val savedData = hashMapOf(
            postId to true
        )
        firestore.collection("Saves").document(auth)
            .set(savedData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("addSavedToFirestore", "Save added successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("addSavedToFirestore", "Error adding save: $exception")
            }
    }

    fun removeSaveFromFirestore(postId: String) {
        firestore.collection("Saves").document(auth)
            .update(postId, FieldValue.delete())
            .addOnSuccessListener {
                Log.d("removeSaveFromFirestore", "Save removed successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("removeSaveFromFirestore", "Error removing save: $exception")
            }
    }

    fun checkSaveStatus(postId: String, imageView: ImageView) {
        firestore.collection("Saves").document(auth).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val savedPostId = document.getBoolean(postId) ?: false
                    if (savedPostId) {
                        imageView.setImageResource(R.drawable.icons8_saved_icon)
                        imageView.tag = "saved"
                    } else {
                        imageView.setImageResource(R.drawable.save_icon)
                        imageView.tag = "save"
                    }
                } else {
                    imageView.setImageResource(R.drawable.save_icon)
                    imageView.tag = "save"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("checkSaveStatus", "Error checking save status: $exception")
            }
    }


    fun toggleSaveStatus(postId: String, imageView: ImageView) {
        val tag = imageView.tag?.toString() ?: ""

        if (tag == "saved") {
            imageView.setImageResource(R.drawable.save_icon)
            imageView.tag = "save"
            removeSaveFromFirestore(postId)
        } else {
            imageView.setImageResource(R.drawable.icons8_saved_icon)
            imageView.tag = "saved"
            addSaveToFirebase(postId)
        }
    }

    companion object {
        private const val TAG = "PostDetailViewModel"
    }
}
