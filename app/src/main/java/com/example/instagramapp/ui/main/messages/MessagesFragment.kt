package com.example.instagramapp.ui.main.messages

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentMessagesBinding
import com.example.instagramapp.ui.main.messages.adapter.MessagesAdapter
import com.example.instagramapp.base.util.NotificationConst.CONTENT_TYPE
import com.example.instagramapp.base.util.NotificationConst.FCM_API
import com.example.instagramapp.base.util.NotificationConst.SERVER_KEY
import com.example.instagramapp.base.util.Resource
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class MessagesFragment : Fragment() {
    private lateinit var binding: FragmentMessagesBinding
    val viewModel: MessagesViewModel by viewModels()
    private val args: MessagesFragmentArgs by navArgs()
    private var senderRoom: String? = null
    private var receiverRoom: String? = null
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var token: String
    private lateinit var photoURL: String



    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.context)
    }

    private val serverKey = "key=$SERVER_KEY"

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                val token = task.result?.toString()
                if (token != null) {
                    Log.d(TAG, "Token: $token")

                }
            })

        val receiverUid = args.userId
        val senderUid = auth.currentUser!!.uid
        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid
        viewModel.checkSession(receiverUid)
        viewModel.readMessages(senderRoom!!)
        binding.post.setOnClickListener {
            sendMessage(senderUid, receiverUid)
        }
        initListeners()

        viewModel.userInfo.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    token = resource.data[0].token ?: ""
                    binding.txtUsername.text=resource.data[0].username
                    photoURL=resource.data[0].imageUrl
                    Glide.with(binding.root).load(resource.data[0].imageUrl).into(binding.imgProfile)
                }

                else -> {

                }
            }
        }


        viewModel.messageList.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                val messages = resource.data
                messagesAdapter = MessagesAdapter(
                    messageClick = { _, messageId ->
                        showDeleteMessageDialog(senderUid, messageId)
                    }, messages,photoURL
                )
                messagesAdapter.updateMessages(messages)
                binding.nestedScroll.post {
                    binding.nestedScroll.fullScroll(View.FOCUS_DOWN)
                    binding.sendMessage.requestFocus()
                }
                setupRecyclerView()
            } else if (resource is Resource.Error) {
            }
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireActivity())
        binding.rvMessages.layoutManager = layoutManager
        binding.rvMessages.adapter = messagesAdapter

        binding.nestedScroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY == binding.nestedScroll.getChildAt(0).measuredHeight - binding.nestedScroll.measuredHeight) {
                layoutManager.scrollToPositionWithOffset(messagesAdapter.itemCount - 1, 0)
            }
        })
    }

    private fun showDeleteMessageDialog(senderId: String, messageId: String) {
        val alert = AlertDialog.Builder(requireContext())
        alert.setTitle(getString(R.string.delete_message))
        alert.setMessage(getString(R.string.do_you_want_to_delete_message))
        alert.setNegativeButton(getString(R.string.yes)) { d, _ ->
            if (senderId == auth.currentUser!!.uid) {
                firestore.collection(ConstValues.MESSAGES).document(senderRoom!!)
                    .update(messageId, FieldValue.delete())
                firestore.collection(ConstValues.MESSAGES).document(receiverRoom!!)
                    .update(messageId, FieldValue.delete())
            } else {
                firestore.collection(ConstValues.MESSAGES).document(senderRoom!!)
                    .update(messageId, FieldValue.delete())
            }
            d.dismiss()
        }
        alert.setPositiveButton(getString(R.string.no)) { d, _ ->
            d.dismiss()
        }
        alert.create()
        alert.show()
    }

    private fun sendMessage(senderUid: String, receiverUid: String) {
        val randomkey = UUID.randomUUID().toString()
        val message = binding.sendMessage.text.toString()

        if (message.trim() != "") {
            val hkey = hashMapOf<String, Any>()
            val hmessage = hashMapOf<Any, Any>()
            hmessage[ConstValues.MESSAGE_ID] = randomkey
            hmessage[ConstValues.MESSAGE_TXT] = message
            hmessage[ConstValues.SEEN] = false
            hmessage[ConstValues.SENDER_ID] = senderUid
            hmessage[ConstValues.TIME] = Timestamp.now()

            hkey[randomkey] = hmessage

            firestore.collection(ConstValues.USERS).document(senderUid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val senderName = document.getString(ConstValues.USERNAME)
                        if (senderName != null) {
                            sendNotification1(message, senderName)
                        }
                    } else {
                        Log.d(TAG, getString(R.string.no_such_document))
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, getString(R.string.get_failed_with), exception)
                }

            firestore.collection(ConstValues.MESSAGES).document(senderRoom!!)
                .set(hkey, SetOptions.merge())
                .addOnSuccessListener {}
            firestore.collection(ConstValues.MESSAGES).document(receiverRoom!!)
                .set(hkey, SetOptions.merge())
            firestore.collection(ConstValues.CHATS).document(receiverRoom!!)
                .set(
                    hashMapOf(
                        ConstValues.TIME to Timestamp.now(),
                        ConstValues.SEEN to true,
                        ConstValues.LAST_MESSAGE to "",
                        ConstValues.SENDER_ID to receiverUid
                    )
                )
            firestore.collection(ConstValues.CHATS).document(senderRoom!!)
                .set(
                    hashMapOf(
                        ConstValues.TIME to Timestamp.now(),
                        ConstValues.SEEN to false,
                        ConstValues.LAST_MESSAGE to message,
                        ConstValues.SENDER_ID to senderUid
                    )
                )

            binding.sendMessage.setText("")
        }

    }

    private fun sendNotification1(lastMessage: String, username: String) {

        val notification = JSONObject()
        val notifcationBody = JSONObject()

        try {
            notifcationBody.put("title", username)
            notifcationBody.put("message", lastMessage)
            notification.put("to", token)
            notification.put("data", notifcationBody)
            Log.e("TAG", "try")
        } catch (e: JSONException) {
            Log.e("TAG", "onCreate: " + e.message)
        }

        sendNotification(notification)
    }

    private fun sendNotification(notification: JSONObject) {
        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener { response ->
                Log.i("TAG", "onResponse: $response")
            },
            Response.ErrorListener {
                Toast.makeText(requireContext(), "Request error", Toast.LENGTH_LONG).show()
                Log.i("TAG", "onErrorResponse: Didn't work")
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = CONTENT_TYPE
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    private fun initListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

}