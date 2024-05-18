package com.example.instagramapp.ui.main.dm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.data.model.Chat
import com.example.instagramapp.data.model.Users
import com.example.instagramapp.base.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DmViewModel @Inject constructor(val firestore: FirebaseFirestore, val auth: FirebaseAuth) :
    ViewModel() {
    private val _chatLiveData = MutableLiveData<Resource<List<Chat>>>()
    val chatLiveData: MutableLiveData<Resource<List<Chat>>>
        get() = _chatLiveData

    init {
        getUsersId()
    }

    fun getUsersId() {
        firestore.collection(ConstValues.CHATS).addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("chat_error", "getUsersId: ChatError ${error.localizedMessage}")
                return@addSnapshotListener
            }
            if (value != null && !value.isEmpty) {
                val chatUserIdList = ArrayList<String>()
                for (doc in value.documents) {
                    val senderId = doc.get("senderId") as? String
                    if (senderId != auth.currentUser!!.uid && auth.currentUser!!.uid + senderId == doc.id) {
                        if (senderId != null) {
                            chatUserIdList.add(senderId)
                        }
                    }
                }
                allUser(chatUserIdList)
            }
        }
    }

    private fun allUser(chatUserIdList: ArrayList<String>) {
        firestore.collection(ConstValues.USERS).addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("chat_error", "getUsersId: ChatError ${error.localizedMessage}")
                return@addSnapshotListener
            }
            if (value != null) {
                val alluser = ArrayList<Users>()
                for (users in value.documents) {
                    val userid = users.get(ConstValues.USER_ID) as String
                    val username = users.get(ConstValues.USERNAME) as String
                    val imageurl = users.get(ConstValues.IMAGE_URL) as String

                    if (chatUserIdList.contains(userid)) {
                        val user = Users(userid, username, "", "", "", imageurl)
                        alluser.add(user)
                    }
                }
                getChatUser(alluser)

            }

        }


    }

    private fun getChatUser(alluser: ArrayList<Users>) {

        val ref = firestore.collection(ConstValues.CHATS)
        ref.addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("chat_error", "ChatError ${error.localizedMessage}")
                return@addSnapshotListener
            }
            if (value != null) {
                val chatlist = ArrayList<Chat>()
                for (doc in value.documents) {
                    val time = doc.get(ConstValues.TIME) as Timestamp
                    val seen = doc.get(ConstValues.SEEN) as Boolean
                    val senderId = doc.get(ConstValues.SENDER_ID) as String
                    val lastMessage = doc.get(ConstValues.LAST_MESSAGE) as String
                    for (user in alluser) {
                        if (auth.currentUser!!.uid + user.userId == doc.id) {
                            val chatUser = Chat(
                                user.userId,
                                user.username,
                                user.imageUrl,
                                lastMessage,
                                time,
                                seen
                            )
                            chatlist.add(chatUser)
                        }
                    }
                    chatlist.sortByDescending { chatItem ->
                        chatItem.time
                    }
                }
                Log.e("TAG", "onViewCreated2: $chatlist")
                chatLiveData.postValue(Resource.Success(chatlist))
            }
        }
    }
}