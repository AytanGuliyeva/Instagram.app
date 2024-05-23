package com.example.instagramapp.ui.main.story.storyView

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.base.util.Resource
import com.example.instagramapp.data.model.Users
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.log

@HiltViewModel
class StoryViewBottomSheetViewModel @Inject constructor(
    val firestore: FirebaseFirestore,
    val auth: FirebaseAuth
) : ViewModel() {

    private val _userResult = MutableLiveData<Resource<List<Users>>>()
    val userResult: LiveData<Resource<List<Users>>>
        get() = _userResult

    fun fetchStoryViewers(storyId: String, userId: String) {
        _userResult.postValue(Resource.Loading)
        firestore.collection(ConstValues.STORY)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val viewers = document.get("$storyId.views") as? Map<String, Boolean>
                    val viewerIds = viewers?.keys?.toList() ?: emptyList()
                    Log.e("TAG", "fetchStoryViewers: $viewerIds", )
                    fetchViewersDetails(viewerIds)
                } else {
                    _userResult.postValue(Resource.Error(Exception("Story document does not exist")))
                }
            }
            .addOnFailureListener { exception ->
                _userResult.postValue(Resource.Error(exception))
            }
    }

    private fun fetchViewersDetails(viewerIds: List<String>) {
        val users = mutableListOf<Users>()
        val fetchTasks = mutableListOf<Task<DocumentSnapshot>>()
        for (viewerId in viewerIds) {
            val task = firestore.collection(ConstValues.USERS)
                .document(viewerId)
                .get()
            fetchTasks.add(task)
        }
        Tasks.whenAllSuccess<DocumentSnapshot>(fetchTasks)
            .addOnSuccessListener { snapshots ->
                for (snapshot in snapshots) {
                    val user = snapshot.toObject(Users::class.java)
                    Log.e("TAG", "fetchViewersDetails: $user", )
                    if (user!!.userId!=auth.currentUser!!.uid){

                    user?.let { users.add(it) }
                }}
                _userResult.postValue(Resource.Success(users))
            }
            .addOnFailureListener { exception ->
                _userResult.postValue(Resource.Error(exception))
            }
    }
}

