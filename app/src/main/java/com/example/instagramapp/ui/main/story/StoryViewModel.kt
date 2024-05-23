package com.example.instagramapp.ui.main.story

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.data.model.Story
import com.example.instagramapp.data.model.Users
import com.example.instagramapp.base.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(val firestore: FirebaseFirestore, val auth: FirebaseAuth) :
    ViewModel() {

    private val storyList = ArrayList<Story>()
    private val _userInformation = MutableLiveData<Resource<Users>>()
    val userInformation: LiveData<Resource<Users>>
        get() = _userInformation
    private val _storyInformation = MutableLiveData<Resource<List<Story>>>()
    val storyInformation: LiveData<Resource<List<Story>>>
        get() = _storyInformation

//         fun getUserInfo(userId: String) {
//
//        firestore.collection("Users").document(userId)
//            .get().addOnSuccessListener { value ->
//
//                if (value != null && value.exists()) {
//                    val username = value.get(ConstValues.USERNAME) as String
//                    val imageurl = value.get("image_url") as String
////                    Glide.with().load(imageurl).into(binding.storyPhoto)
////                    binding.storyUsername.text = username
//                }
//
//
//            }.addOnFailureListener {
//               // Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
//
//            }
//    }
    fun fetchUserInformation(userId: String) {
        _userInformation.postValue(Resource.Loading)
        firestore.collection(ConstValues.USERS)
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


    fun addView(storyId: String, userId: String) {
        firestore.collection(ConstValues.STORY).document(userId)
            .update("$storyId.views.${auth.currentUser!!.uid}", true)
    }

    fun getStories(userId: String) {

        val ref = firestore.collection(ConstValues.STORY).document(userId)
        ref.get().addOnSuccessListener { value ->
            if (value != null && value.exists()) {
                storyList.clear()
                try {
                    val doc = value.data as HashMap<*, *>
                    val timecurrent = System.currentTimeMillis()
                    for (i in doc) {
                        val story = i.value as HashMap<*, *>
                        val timestart = story[ConstValues.TIME_START] as Long
                        val timeend = story[ConstValues.TIME_END] as Long
                        val imageurl = story[ConstValues.IMAGE_URL] as String
                        val storyId = story[ConstValues.STORY_ID] as String

                        if (timecurrent in (timestart + 1) until timeend) {
                            val storyi =
                                Story(imageUrl = imageurl, timeStart = timestart, storyId = storyId)
                            storyList.add(storyi)
                        }
                        _storyInformation.postValue(Resource.Success(storyList))
                        Log.e("TAG", "getStories: $storyList")
                    }
                    storyList.sortBy {
                        it.timeStart
                    }
                } catch (e: java.lang.NullPointerException) {
                    e.printStackTrace()
                }
            }
        }
    }
}