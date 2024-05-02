package com.example.instagramapp.ui.profile

import Post
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
import com.google.firebase.firestore.FirebaseFirestore

class SavedPostsViewModel:ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _savedPosts = MutableLiveData<Resource<List<Pair<Post, String>>>>()
    val savedPosts: LiveData<Resource<List<Pair<Post, String>>>>
        get() = _savedPosts

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
                                    val userTask = firestore.collection("Users").document(userId).get()
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
}