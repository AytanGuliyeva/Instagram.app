package com.example.instagramapp.ui.main

import Post
import android.util.Log
import android.widget.ImageView
import android.widget.TextView

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.ConstValues
import com.example.instagramapp.R
import com.example.instagramapp.ui.search.model.LikeCount
import com.example.instagramapp.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import javax.security.auth.callback.Callback

class MainViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val postList = mutableListOf<Pair<Post, String>>()
    val likeCountList = mutableListOf<LikeCount>()

    private val _postResult = MutableLiveData<Resource<List<Pair<Post, String>>>>()
    val postResult: LiveData<Resource<List<Pair<Post, String>>>>
        get() = _postResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    val likeCount = MutableLiveData<List<LikeCount>>()

    init {
        fetchPosts()
    }

    private fun fetchPosts() {
        _loading.postValue(true)
        val currentUserUid = Firebase.auth.currentUser?.uid
        if (currentUserUid != null) {
            firestore.collection("Follow").document(currentUserUid).get()
                .addOnSuccessListener { documentSnapshot ->
                    val followData = documentSnapshot.data
                    if (followData != null) {
                        val followingUserIds =
                            (followData["following"] as? Map<*, *>)?.keys ?: emptySet()
                        if (followingUserIds.isNotEmpty()) {
                            firestore.collection("Posts")
                                .whereIn(ConstValues.USER_ID, followingUserIds.toList())
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    postList.clear()
                                    for (document in querySnapshot.documents) {
                                        val post = document.toObject(Post::class.java)
                                        post?.let { postDetail ->
                                            checkLikeStatus(postDetail)
                                            //   likeCount(postDetail)
//                                            Log.e("TAG", "fetchPosts: $postDetail" )
//                                            val formattedTimestamp = postDetail.time?.toDate()
//                                            if (formattedTimestamp != null) {
//                                                val postWithFormattedTime =
//                                                    postDetail.copy(time = Timestamp(formattedTimestamp))
////                                                Log.e("TAG", "fetch: $postWithFormattedTime", )
//                                                postList.add(Pair(postWithFormattedTime, ""))
//                                            } else {
//                                                // Handle the case where post.time is null
//                                            }
                                        }
                                    }
                            //        likeCount()
//                                    _postResult.postValue(Resource.Success(postList))
                                }
                                .addOnFailureListener {
                                    _postResult.postValue(Resource.Error(Exception("Failed to fetch posts")))
                                }
                        } else {
                            _postResult.postValue(Resource.Success(emptyList()))
                        }
                    } else {
                        _postResult.postValue(Resource.Error(Exception("Failed to fetch follow data")))
                    }
                }
                .addOnFailureListener {
                    _postResult.postValue(Resource.Error(Exception("Failed to fetch follow data")))
                }
        } else {
            _postResult.postValue(Resource.Error(Exception("User not logged in")))
        }
    }

    //save
    private fun addSaveToFirebase(postId: String) {
        val savedData = hashMapOf(
            postId to true
        )
        firestore.collection("Saves").document(auth.currentUser!!.uid)
            .set(savedData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("addSavedToFirestore", "Save added successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("addSavedToFirestore", "Error adding save: $exception")
            }
    }

    private fun removeSaveFromFirestore(postId: String) {
        firestore.collection("Saves").document(auth.currentUser!!.uid)
            .update(postId, FieldValue.delete())
            .addOnSuccessListener {
                Log.d("removeSaveFromFirestore", "Save removed successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("removeSaveFromFirestore", "Error removing save: $exception")
            }
    }
     fun toggleSaveStatus(postId: String, tag: String) {
      //  val tag = imageView.tag?.toString() ?: ""

        if (tag == "saved") {
//            imageView.setImageResource(R.drawable.save_icon)
//            imageView.tag = "save"
            removeSaveFromFirestore(postId)
        } else {
//            imageView.setImageResource(R.drawable.icons8_saved_icon)
//            imageView.tag = "saved"
            addSaveToFirebase(postId)
        }
    }
    //like
    private fun checkLikeStatus(post: Post) {
        firestore.collection("Likes").document(post.postId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val likedByCurrentUser =
                        document.getBoolean(auth.currentUser!!.uid) ?: false
                    val formattedTimestamp = post.time?.toDate()
                    if (formattedTimestamp != null) {
                        val postWithFormattedTime =
                            post.copy(
                                time = Timestamp(formattedTimestamp),
                                isLiked = likedByCurrentUser
                            )
                        Log.e("TAG", "fetch: $postWithFormattedTime")
                        postList.add(Pair(postWithFormattedTime, ""))
                        _postResult.postValue(Resource.Success(postList))
                    } else {
                        // Handle the case where post.time is null
                    }
                } else {

                }
            }
            .addOnFailureListener { exception ->
                Log.e("checkLikeStatus", "Error checking like status: $exception")
            }
    }

//    fun likeCount() {
//        postList.forEach {
//            val (post, username) = it
//            firestore.collection("Likes").addSnapshotListener { value, error ->
//                if (error != null) {
//
//                    Log.e("likeCount", "Error fetching like count: $error")
//                    return@addSnapshotListener
//                }
//
//                if (value != null) {
//                    for (document in value.documents) {
//                        if (document.id == post.postId) {
//                            val likesCount = document.data?.size ?: 0
//                            likeCountList.add(LikeCount(post.postId, likesCount))
//                            break
//                        }
//                    }
//                    //likeCount.postValue(likeCountList)
//                  //  val likesString = "$likesCount likes"
//
//                } else {
//
//                }
//                likeCount.postValue(likeCountList)
//            }
//        }
//
//    }

    fun toggleLikeStatus(postId: String, tag: String) {
        if (tag == "liked") {
            removeLikeFromFirestore(postId)
        } else {
            addLikeToFirestore(postId)
        }
    }

    private fun addLikeToFirestore(postId: String) {
        val likeData = hashMapOf(
            auth.currentUser!!.uid to true
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
            .update(auth.currentUser!!.uid, FieldValue.delete())
            .addOnSuccessListener {
                Log.d("removeLikeFromFirestore", "Like removed successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("removeLikeFromFirestore", "Error removing like: $exception")
            }
    }

//    fun fetchUsername(userId: String, post: Post) {
//        firestore.collection("Users")
//            .document(userId)
//            .get()
//            .addOnSuccessListener { documentSnapshot ->
//                val user = documentSnapshot.toUser()
//                val username = user?.username ?: ""
//            }
//            .addOnFailureListener { exception ->
//                Log.e(TAG, "Failed to fetch username: ${exception.message}", exception)
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
//                userId.orEmpty(),
//                username.orEmpty(),
//                email.orEmpty(),
//                password.orEmpty(),
//                bio.orEmpty(),
//                imageUrl.orEmpty(),
//            )
//        } catch (e: Exception) {
//            null
//        }
//    }

}
