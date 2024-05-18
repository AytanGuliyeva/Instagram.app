package com.example.instagramapp.ui.main.messages.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramapp.databinding.ItemMessageReceiverBinding
import com.example.instagramapp.databinding.ItemMessageSendBinding
import com.example.instagramapp.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessagesAdapter(
    private val messageClick: (senderId: String, messageId: String) -> Unit,
    private var messageList: List<Message>,
    private val photoURL:String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    val ITEM_RECEIVE = 1
    val ITEM_SENT = 2

    inner class SentViewHolder(private val bindingSent: ItemMessageSendBinding) :
        RecyclerView.ViewHolder(bindingSent.root) {
        fun bindSent(message: Message) {
            bindingSent.sendMessage.text = message.messagetxt

        }
    }
    inner class ReceiverViewHolder(private val bindingReceiver: ItemMessageReceiverBinding) :
        RecyclerView.ViewHolder(bindingReceiver.root) {
        fun bindReceiver(message: Message) {
            bindingReceiver.receiverMessage.text = message.messagetxt
            Glide.with(bindingReceiver.root).load(photoURL).into(bindingReceiver.imgProfile)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_RECEIVE) {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = ItemMessageReceiverBinding.inflate(layoutInflater, parent, false)
            ReceiverViewHolder(view)
        } else {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = ItemMessageSendBinding.inflate(layoutInflater, parent, false)
            SentViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if (auth.currentUser!!.uid == currentMessage.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    fun updateMessages(newMessageList: List<Message>) {
        this.messageList = newMessageList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        if (holder is SentViewHolder) {
            holder.bindSent(messageList[position])
            holder.itemView.setOnLongClickListener {
                messageClick(currentMessage.senderId, currentMessage.messageId)
                true
            }
        } else if (holder is ReceiverViewHolder) {
            holder.bindReceiver(messageList[position])
            holder.itemView.setOnLongClickListener {
                messageClick(currentMessage.senderId, currentMessage.messageId)
                true
            }
        }
    }
}
