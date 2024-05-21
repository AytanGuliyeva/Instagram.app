package com.example.instagramapp.ui.main

import android.content.ContentValues
import com.example.instagramapp.data.model.Post
import android.util.Log

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.example.instagramapp.R
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.data.model.Story
import com.example.instagramapp.data.model.LikeCount
import com.example.instagramapp.base.util.Resource
import com.example.instagramapp.data.model.PostInfo
import com.example.instagramapp.data.model.Users
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
class MainViewModel @Inject constructor(val firestore: FirebaseFirestore, val auth: FirebaseAuth) :
    ViewModel() {

    val postList = mutableListOf<PostInfo>()
    val likeCountList = mutableListOf<LikeCount>()

    private val _postResult = MutableLiveData<Resource<List<PostInfo>>>()
    val postResult: LiveData<Resource<List<PostInfo>>>
        get() = _postResult

    private val _storyResult = MutableLiveData<Resource<List<Story>>>()
    val storyResult: LiveData<Resource<List<Story>>>
        get() = _storyResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    val likeCount = MutableLiveData<List<LikeCount>>()
    var followList = ArrayList<String>()

    init {
        fetchPosts()
        readStory()
    }

    private fun fetchPosts() {
        _loading.postValue(true)
        val currentUserUid = Firebase.auth.currentUser?.uid
        if (currentUserUid != null) {
            firestore.collection(ConstValues.FOLLOW).document(currentUserUid).get()
                .addOnSuccessListener { documentSnapshot ->
                    val followData = documentSnapshot.data
                    if (followData != null) {
                        val followingUserIds =
                            (followData[ConstValues.FOLLOWING] as? Map<*, *>)?.keys ?: emptySet()
                        followingUserIds.forEach { key ->
                            key as String
                            followList.add(key)
                        }
                        if (followingUserIds.isNotEmpty()) {
                            firestore.collection(ConstValues.POSTS)
                                .whereIn(ConstValues.USER_ID, followingUserIds.toList())
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    postList.clear()
                                    for (document in querySnapshot.documents) {
                                        val post = document.toObject(Post::class.java)
                                        post?.let { postDetail ->
                                            checkLikeStatus(PostInfo(post = postDetail))

                                        }
                                    }
                                    Log.e("TAG", "fetchPosts: ${postList.size}")
                                    Log.e("TAG", "fetchPosts1: $postList")

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

    //like
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
                            _postResult.postValue(Resource.Success(postList))
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
        firestore.collection(ConstValues.LIKES).document(postId).set(likeData, SetOptions.merge())
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

    //story
    fun readStory() {

        firestore.collection(ConstValues.STORY).get().addOnSuccessListener { value ->
            if (value != null) {
                _storyResult.postValue(Resource.Loading)
                val storyList = ArrayList<Story>()
                var ustory: Story? = null
                val timecurrent = System.currentTimeMillis()
                for (document in value.documents) {
                    var countStory = 0
                    if (followList.contains(document.id)) {
                        val stories = document.data as? HashMap<*, *>
                        if (stories != null) {
                            for (storyIds in stories) {
                                val story = storyIds.value as? HashMap<*, *>
                                if (story != null) {
                                    val storyId = story[ConstValues.STORY_ID] as String
                                    val timestart = story[ConstValues.TIME_START] as Long
                                    val timeend = story[ConstValues.TIME_END] as Long
                                    val imageurl = story[ConstValues.IMAGE_URL] as String
                                    val userId = story[ConstValues.USER_ID] as String
                                    if (timecurrent in (timestart + 1) until timeend) {
                                        ++countStory
                                    }
                                    ustory = Story(storyId, userId, imageurl, timestart, timeend)
                                }
                            }
                            if (countStory > 0) {
                                if (ustory != null) {
                                    storyList.add(ustory)
                                }
                            }
                        }

                    }
                }
                storyList.sortedByDescending {
                    it.timeStart
                }

                storyList.add(0, Story("", Firebase.auth.currentUser!!.uid, "", 0, 0))

                _storyResult.postValue(Resource.Success(storyList))
            }

        }.addOnFailureListener { exception ->
            _storyResult.postValue(Resource.Error(exception))
        }
    }
}
