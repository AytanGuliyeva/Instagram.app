package com.example.instagramapp.ui.profile.edit

import android.app.ProgressDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.data.model.Users
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.base.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    val firestore: FirebaseFirestore,
    val auth: FirebaseAuth
) : ViewModel() {

    private val _userInformation = MutableLiveData<Resource<Users>>()
    val userInformation: LiveData<Resource<Users>>
        get() = _userInformation

    fun getUserInfo() {
        firestore.collection(ConstValues.USERS).document(auth.currentUser!!.uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // _userInformation.postValue(Resource.Error("Failed to fetch user information"))
                } else {
                    val bio = value?.getString(ConstValues.BIO) ?: ""
                    val username = value?.getString(ConstValues.USERNAME) ?: ""
                    val imageUrl = value?.getString(ConstValues.IMAGE_URL) ?: ""
                    _userInformation.postValue(
                        Resource.Success(
                            Users(
                                username = username,
                                imageUrl = imageUrl,
                                bio = bio
                            )
                        )
                    )
                }
            }
    }

    fun updateUserInfo(
        username: String,
        bio: String,
        imageUrl: String,
        progressDialoq: ProgressDialog
    ) {
        val userRef = firestore.collection(ConstValues.USERS).document(auth.currentUser!!.uid)
        userRef.update(
            mapOf(
                ConstValues.USERNAME to username,
                ConstValues.BIO to bio,
                ConstValues.IMAGE_URL to imageUrl
            )
        ).addOnSuccessListener {
            progressDialoq.dismiss()
        }.addOnFailureListener {}
    }
}
