package org.personal.videotogether.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.ChatData

class ChatAdapter
constructor(
    val context: Context,
    private val myUserId: Int,
    private val messageList: ArrayList<ChatData>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = javaClass.name

    private val MY_CHAT = 1
    private val OTHER_CHAT = 2

    class OthersChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageIV: ImageView = itemView.findViewById(R.id.profileIV)
        val nameTV: TextView = itemView.findViewById(R.id.nameTV)
        val otherMessageTV: TextView = itemView.findViewById(R.id.messageTV)
        val otherMessageTimeTV: TextView = itemView.findViewById(R.id.messageTimeTV)
    }

    class MyChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val myMessageTV: TextView = itemView.findViewById(R.id.messageTV)
        val myMessageTimeTV: TextView = itemView.findViewById(R.id.messageTimeTV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View

        if (viewType == MY_CHAT) {
            view = inflater.inflate(R.layout.item_chat_mine, parent, false)
            return MyChatViewHolder(view)
        }
        view = inflater.inflate(R.layout.item_chat_others, parent, false)
        return OthersChatViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        if (myUserId == messageList[position].senderId) {
            return MY_CHAT
        }
        return OTHER_CHAT
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val messageData = messageList[position]

        if (myUserId == messageData.senderId) {
            val myChatViewHolder = holder as MyChatViewHolder
            myChatViewHolder.myMessageTV.text  = messageData.message
            myChatViewHolder.myMessageTimeTV.text = messageData.messageTime
        } else {
            val othersChatViewHolder = holder as OthersChatViewHolder
            Glide.with(context).load(messageData.
            profileImageUrl).into(othersChatViewHolder.profileImageIV)
            othersChatViewHolder.nameTV.text = messageData.senderName
            othersChatViewHolder.otherMessageTV.text = messageData.message
            othersChatViewHolder.otherMessageTimeTV.text = messageData.messageTime
        }
    }
}