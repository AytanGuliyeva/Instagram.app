package com.example.instagramapp.ui.profile

import Post
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SavedPostsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _savedPosts = MutableLiveData<Resource<List<Pair<Post, String>>>>()
    val savedPosts: LiveData<Resource<List<Pair<Post, String>>>>
        get() = _savedPosts

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
    fun fetchSavedPosts() {
        _savedPosts.value = Resource.Loading
        firestore.collection("Saves").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val savedPostIds = document.data?.keys ?: emptySet<String>()
                    val fetchPostTasks = mutableListOf<Task<DocumentSnapshot>>()
                    savedPostIds.forEach { postId ->
                        val task = firestore.collection("Posts").document(postId).get()
                        fetchPostTasks.add(task)
                    }

                    Tasks.whenAllSuccess<DocumentSnapshot>(fetchPostTasks)
                        .addOnSuccessListener { postDocuments ->
                            val savedPosts = mutableListOf<Pair<Post, String>>()

                            val fetchUserTasks = mutableListOf<Task<DocumentSnapshot>>()

                            postDocuments.forEach { document ->
                                val post = document.toObject(Post::class.java)
                                if (post != null) {
                                    val userId = post.userId
                                    val userTask =
                                        firestore.collection("Users").document(userId).get()
                                    fetchUserTasks.add(userTask)

                                    userTask.addOnSuccessListener { userDocument ->
                                        val user = userDocument.toObject(Users::class.java)
                                        val username = user?.username ?: ""
                                        savedPosts.add(Pair(post, username))
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

}