package com.example.instagramapp.ui.main.messages

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.data.model.Message
import com.example.instagramapp.data.model.Users
import com.example.instagramapp.base.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    val firestore: FirebaseFirestore,
    val auth: FirebaseAuth
) : ViewModel() {
    private val _messageList = MutableLiveData<Resource<List<Message>>>()
    val messageList: LiveData<Resource<List<Message>>>
        get() = _messageList

    private val _userInfo = MutableLiveData<Resource<List<Users>>>()
    val userInfo: LiveData<Resource<List<Users>>>
        get() = _userInfo

    private val _checkSession = MutableLiveData<Resource<List<String>>>()
    val checkSession: LiveData<Resource<List<String>>>
        get() = _checkSession

    fun checkSession(userId: String) {
        firestore.collection(ConstValues.USERS).document(userId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    error.localizedMessage?.let { Log.e("user_Error", it) }
                    return@addSnapshotListener
                }
                if (value != null && value.exists()) {
                    val token=value.get("token") as? String
                    Log.e("TAG", "checkSession: $token", )
                    val username = value.get(ConstValues.USERNAME) as String
                    val imageurl = value.get(ConstValues.IMAGE_URL) as String
                    val user = Users(token=token,username = username, imageUrl = imageurl)
                    val userList = listOf(user)
                    _userInfo.postValue(Resource.Success(userList))
                }
            }
    }

    fun readMessages(senderRoom: String) {
        firestore.collection(ConstValues.MESSAGES).document(senderRoom)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    error.localizedMessage?.let { Log.e("Messages_error", it) }
                    return@addSnapshotListener
                }
                if (value != null && value.exists()) {
                    try {
                        val doc = value.data as HashMap<*, *>
                        val messagesList = ArrayList<Message>()
                        for (i in doc) {
                            val message = i.value as HashMap<*, *>
                            val messageId = message[ConstValues.MESSAGE_ID] as String
                            val messageTxt = message[ConstValues.MESSAGE_TXT] as String
                            val senderId = message[ConstValues.SENDER_ID] as String
                            val time = message[ConstValues.TIME] as Timestamp
                            val seen = message[ConstValues.SEEN] as Boolean

                            val messages = Message(messageId, messageTxt, senderId, time, seen)
                            messagesList.add(messages)
                        }
                        messagesList.sortBy {
                            it.time
                        }
                        _messageList.postValue(Resource.Success(messagesList))
                    } catch (e: NullPointerException) {
                        e.localizedMessage?.let { Log.e("user_Error", it) }
                    }
                }
            }
    }
}