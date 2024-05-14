package com.example.instagramapp.ui.main.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.ConstValues
import com.example.instagramapp.ui.main.model.Comments
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentsBottomSheetViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    private val _commentResult = MutableLiveData<Resource<List<Comments>>>()
    val commentResult: LiveData<Resource<List<Comments>>>
        get() = _commentResult

    private val _userResult = MutableLiveData<Resource<List<Users>>>()
    val userResult: LiveData<Resource<List<Users>>>
        get() = _userResult


    fun readComment(postId: String) {
        firestore.collection("Comments").document(postId).addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value != null && value.exists()) {
                val document = value.data as? HashMap<*, *>
                val userIdList = ArrayList<String>()
                val commentList = ArrayList<Comments>()
                if (document != null) {
                    for (i in document) {
                        val comments = i.value as HashMap<*, *>
                        val comment = comments[ConstValues.COMMENT] as String
                        val userId = comments[ConstValues.USER_ID] as String
                        val time = comments[ConstValues.TIME] as Timestamp
                        val commentId = comments[ConstValues.COMMENTID] as String

                        val commentt = Comments(comment, userId, postId, commentId, time)
                        userIdList.add(userId)
                        commentList.add(commentt)
                    }
                }
                allUsers(userIdList)
                commentList.sortedByDescending {
                    it.time
                }
                _commentResult.postValue(Resource.Success(commentList))
            }
        }
    }


    private fun allUsers(userIds: List<String>) {
        firestore.collection("Users").get().addOnSuccessListener { value ->
            if (value != null) {
                val allUsersList = ArrayList<Users>()
                for (users in value.documents) {
                    if (userIds.contains(users.id)) {
                        val userId = users.get(ConstValues.USER_ID) as? String ?: ""
                        val username = users.get(ConstValues.USERNAME) as? String ?: ""
                        val imageUrl = users.get(ConstValues.IMAGE_URL) as? String ?: ""
                        val user = Users(userId, username, "", "", "", imageUrl)
                        allUsersList.add(user)
                    }
                }
                _userResult.postValue(Resource.Success(allUsersList))
            }
        }
    }

    fun getCurrentUserProfileImage(callback: (String) -> Unit) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        currentUserUid?.let { uid ->
            firestore.collection("Users").document(uid).get()
                .addOnSuccessListener { document ->
                    val imageUrl = document.getString(ConstValues.IMAGE_URL)
                    imageUrl?.let { callback(it) }
                }
                .addOnFailureListener {}
        }
    }
}