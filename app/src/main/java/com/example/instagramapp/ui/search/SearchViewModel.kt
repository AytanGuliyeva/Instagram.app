package com.example.instagramapp.ui.search

import com.example.instagramapp.data.model.Post
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.data.model.Users
import com.example.instagramapp.base.util.Resource
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(val firestore: FirebaseFirestore) : ViewModel() {

    private val _userResult = MutableLiveData<Resource<List<Users>>>()
    val userResult: LiveData<Resource<List<Users>>>
        get() = _userResult

    private val _postResult = MutableLiveData<Resource<List<Post>>>()
    val postResult: LiveData<Resource<List<Post>>>
        get() = _postResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading


    private val _isFollowing = MutableLiveData<Boolean>()
    val isFollowing: LiveData<Boolean>
        get() = _isFollowing


    fun searchUsers(query: String) {
        _loading.value = true
        firestore.collection(ConstValues.USERS)
            .orderBy(ConstValues.USERNAME)
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .addOnSuccessListener { value ->
                val userList = mutableListOf<Users>()
                for (user in value.documents) {
                    val userId = user.getString(ConstValues.USER_ID) ?: ""
                    val username = user.getString(ConstValues.USERNAME) ?: ""
                    val email = user.getString(ConstValues.EMAIL) ?: ""
                    val password = user.getString(ConstValues.PASSWORD) ?: ""
                    val bio = user.getString(ConstValues.BIO) ?: ""
                    val imageUrl = user.getString(ConstValues.IMAGE_URL) ?: ""
                    val currentUserUid = Firebase.auth.currentUser?.uid
                    if (currentUserUid != userId) {
                        val user = Users(userId, username, email, password, bio, imageUrl)
                        userList.add(user)
                    }
                }
                _userResult.value = Resource.Success(userList)
            }
            .addOnFailureListener { exception ->
                _userResult.value = Resource.Error(exception)
            }
            .addOnCompleteListener {
                _loading.value = false
            }
    }

    fun fetchOtherUsersPosts(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            try {
                val postList = mutableListOf<Post>()
                val currentUserUid = Firebase.auth.currentUser?.uid
                val value = firestore.collection(ConstValues.POSTS).get()
                    .addOnSuccessListener {

                        for (postDoc in it.documents) {
                            val postUserId = postDoc.getString(ConstValues.USER_ID) ?: ""
                            if (currentUserUid != null) {
                                val caption = postDoc.getString(ConstValues.CAPTION) ?: ""
                                val postId = postDoc.getString(ConstValues.POST_ID) ?: ""
                                val time = postDoc.getTimestamp(ConstValues.TIME)
                                val imageUrl = postDoc.getString(ConstValues.POST_IMAGE_URL) ?: ""
                                val post = Post(caption, postId, postUserId, time, imageUrl)

                                if (postUserId != currentUserUid) postList.add(post)
                            }
                        }
                        _postResult.postValue(Resource.Success(postList))
                    }
            } catch (exception: Exception) {
                _postResult.postValue(Resource.Error(exception))
            } finally {
                _loading.postValue(false)
            }
        }
    }
}
