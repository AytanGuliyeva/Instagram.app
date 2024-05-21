package com.example.instagramapp.ui.profile.saved

import android.content.ContentValues
import com.example.instagramapp.data.model.Post
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.data.model.Users
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.base.util.Resource
import com.example.instagramapp.data.model.PostInfo
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SavedPostsViewModel @Inject constructor(
    val firestore: FirebaseFirestore,
    val auth: FirebaseAuth
) : ViewModel() {

    val postList = mutableListOf<PostInfo>()

    private val _savedPosts = MutableLiveData<Resource<List<PostInfo>>>()
    val savedPosts: LiveData<Resource<List<PostInfo>>>
        get() = _savedPosts
    init {
        getSavedPosts()
    }

    private fun getSavedPosts() {
        firestore.collection(ConstValues.SAVES).document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val savedPostIds = document.data?.keys ?: emptySet<String>()
                    val fetchPostTasks = mutableListOf<Task<DocumentSnapshot>>()
                    firestore.collection(ConstValues.POSTS)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            //  postList.clear()
                            for (document in querySnapshot.documents) {
                                val post = document.toObject(Post::class.java)
                                post?.let { postDetail ->
                                    if (savedPostIds.contains(postDetail.postId)){
                                        checkLikeStatus(PostInfo(post = postDetail))
                                    }

                                    // checkLikeStatus(PostInfo(post = postDetail))

                                }
                            }
//                                    Log.e("TAG", "fetchPosts: ${postList.size}")
//                                    Log.e("TAG", "fetchPosts1: $postList")

                            //        likeCount()
//                                    _postResult.postValue(Resource.Success(postList))
                        }
                        .addOnFailureListener {
                            //    _postResult.postValue(Resource.Error(Exception("Failed to fetch posts")))
                        }
                }
            }

            }
    private fun checkLikeStatus(postInfo: PostInfo) {
        firestore.collection(ConstValues.LIKES).document(postInfo.post.postId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val likedByCurrentUser =
                        document.getBoolean(auth.currentUser!!.uid) ?: false
                    val hashMap = document.data as HashMap<*, *>
                    val formattedTimestamp = postInfo.post.time?.toDate()
                    if (formattedTimestamp != null) {
                        val postWithFormattedTime =
                            postInfo.post.copy(
                                time = Timestamp(formattedTimestamp),
                                isLiked = likedByCurrentUser
                            )
                        // postList.add(Pair(postWithFormattedTime, ""))
//                        _postResult.postValue(Resource.Success(postList))
                        val newPostInfo = postInfo.copy(
                            post = postWithFormattedTime,
                            likeCount = hashMap.keys.size

                        )
                        commentCount(newPostInfo)
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

    fun commentCount(postInfo: PostInfo) {
        firestore.collection(ConstValues.COMMENTS).document(postInfo.post.postId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val comments = documentSnapshot.data?.size ?: 0
                val commentText = "View all $comments comments"
                val newPostInfo = postInfo.copy(commentCount = comments)
                // binding.txtComment.text = commentText
                checkSaveStatus(newPostInfo)
            }
            .addOnFailureListener { exception ->
                Log.e("PostSearchAdapter", "Error getting comment count: $exception")
            }
    }

    fun checkSaveStatus(postInfo: PostInfo) {
        firestore.collection(ConstValues.SAVES).document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val isSaved = document.getBoolean(postInfo.post.postId) ?: false
                    val post = postInfo.post.copy(isSave = isSaved)
                    val newPostInfo = postInfo.copy(
                        post = post
                    )
                    firestore.collection(ConstValues.USERS)
                        .document(post.userId)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            val user = documentSnapshot.toUser()
                            val username = user?.username ?: ""


                            postList.add(newPostInfo.copy(user = user))
                            Log.e("TAG", "savePosts: $postList")
                            Log.e("TAG", "fetchPosts: ${postList.size}")
                            _savedPosts.postValue(Resource.Success(postList))
                        }
                        .addOnFailureListener { exception ->
                            Log.e(
                                ContentValues.TAG,
                                "Failed to fetch username: ${exception.message}",
                                exception
                            )
                        }


                }
            }
            .addOnFailureListener { exception ->
                Log.e("checkSaveStatus", "Error checking save status: $exception")
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



    //save
        private fun addSaveToFirebase(postId: String) {
            val savedData = hashMapOf(
                postId to true
            )
            firestore.collection(ConstValues.SAVES).document(auth.currentUser!!.uid)
                .set(savedData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("addSavedToFirestore", "Save added successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("addSavedToFirestore", "Error adding save: $exception")
                }
        }

        private fun removeSaveFromFirestore(postId: String) {
            firestore.collection(ConstValues.SAVES).document(auth.currentUser!!.uid)
                .update(postId, FieldValue.delete())
                .addOnSuccessListener {
                    Log.d("removeSaveFromFirestore", "Save removed successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("removeSaveFromFirestore", "Error removing save: $exception")
                }
        }

        fun toggleSaveStatus(postId: String, tag: String) {
            if (tag == "saved") {
                removeSaveFromFirestore(postId)
            } else {
                addSaveToFirebase(postId)
            }
        }

        fun fetchSavedPosts() {
            _savedPosts.value = Resource.Loading
            firestore.collection(ConstValues.SAVES).document(auth.currentUser!!.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val savedPostIds = document.data?.keys ?: emptySet<String>()
                        val fetchPostTasks = mutableListOf<Task<DocumentSnapshot>>()
                        savedPostIds.forEach { postId ->
                            val task =
                                firestore.collection(ConstValues.POSTS).document(postId).get()
                            fetchPostTasks.add(task)
                        }
                        Tasks.whenAllSuccess<DocumentSnapshot>(fetchPostTasks)
                            .addOnSuccessListener { postDocuments ->
                                val savedPosts = mutableListOf<PostInfo>()
                                val fetchUserTasks = mutableListOf<Task<DocumentSnapshot>>()
                                postDocuments.forEach { document ->
                                    val post = document.toObject(Post::class.java)
                                    if (post != null) {
                                        val userId = post.userId
                                        val userTask =
                                            firestore.collection(ConstValues.USERS).document(userId)
                                                .get()
                                        fetchUserTasks.add(userTask)
                                        userTask.addOnSuccessListener { userDocument ->
                                            val user = userDocument.toObject(Users::class.java)
                                            val username = user?.username ?: ""
                                            // savedPosts.add(Pair(post, username))
                                        }
                                    }
                                }
                                Tasks.whenAllSuccess<DocumentSnapshot>(fetchUserTasks)
                                    .addOnSuccessListener {
                                        _savedPosts.value = Resource.Success(savedPosts)
                                    }
                                    .addOnFailureListener { exception ->
                                        _savedPosts.value = Resource.Error(exception)
                                    }
                            }
                            .addOnFailureListener { exception ->
                                _savedPosts.value = Resource.Error(exception)
                            }
                    } else {
                        _savedPosts.value = Resource.Success(emptyList())
                    }
                }
                .addOnFailureListener { exception ->
                    _savedPosts.value = Resource.Error(exception)
                }
        }

        //like
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
            firestore.collection(ConstValues.LIKES).document(postId)
                .set(likeData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("addLikeToFirestore", "Like added successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("addLikeToFirestore", "Error adding like: $exception")
                }
        }

        private fun removeLikeFromFirestore(postId: String) {
            firestore.collection(ConstValues.LIKES).document(postId)
                .update(auth.currentUser!!.uid, FieldValue.delete())
                .addOnSuccessListener {
                    Log.d("removeLikeFromFirestore", "Like removed successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("removeLikeFromFirestore", "Error removing like: $exception")
                }
        }
    }