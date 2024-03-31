package com.example.instagramapp.ui.signUp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.ui.search.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.example.instagramapp.util.Resource
import com.google.firebase.firestore.FirebaseFirestore

class SignUpViewModel:ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _userCreated = MutableLiveData<Resource<Users>>()
    val userCreated:LiveData<Resource<Users>>
        get() = _userCreated
    fun signUp(username: String, email: String, password: String) {
        _userCreated.postValue(com.example.instagramapp.util.Resource.Loading)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                userAccount(username, email, password)
            }
            .addOnFailureListener {exception ->
                _userCreated.postValue(com.example.instagramapp.util.Resource.Error(exception))
            }
    }

    private fun userAccount(username: String, email: String, password: String) {
        val userId = auth.currentUser?.uid ?: return
        val userMap = hashMapOf(
            "userId" to userId,
            "username" to username,
            "email" to email,
            "password" to password,
            "bio" to "",
            "imageUrl" to "https://firebasestorage.googleapis.com/v0/b/instagramclone-d83f1.appspot.com/o/photo_5393077931670099315_m.jpg?alt=media&token=929e8f56-74c9-4247-b1f6-173f858d9f04"
        )

        val refDb = db.collection("Users").document(userId)
        refDb.set(userMap)
            .addOnSuccessListener {
                _userCreated.value =Resource.Success(Users(userId, username, email, password, "", ""))
            }
            .addOnFailureListener {
                    exception ->
                _userCreated.postValue(Resource.Error(exception))
            }
    }
}