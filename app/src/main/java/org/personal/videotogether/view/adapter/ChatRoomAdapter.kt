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
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.domianmodel.UserData

class ChatRoomAdapter
constructor(
    val context: Context,
    private val myUserId: Int,
    private val chatRoomList: ArrayList<ChatRoomData>,
    private val itemClickListener: ItemClickListener
) : RecyclerView.Adapter<ChatRoomAdapter.ViewHolder>() {

    class ViewHolder(itemView: View, private val itemClickListener: ItemClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        val chatRoomProfileIV: ImageView = itemView.findViewById(R.id.chatRoomProfileIV)
        val nameTV: TextView = itemView.findViewById(R.id.nameTV)
        val participantCountTV :TextView = itemView.findViewById(R.id.participantsCountTV)
        val latestChatMessageTV: TextView = itemView.findViewById(R.id.latestChatMessageTV)

        override fun onClick(view: View?) {

            if (adapterPosition != RecyclerView.NO_POSITION) {

                itemClickListener.onItemClick(view, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room, parent, false)
        return ViewHolder(view, itemClickListener)
    }

    override fun getItemCount(): Int {
        return chatRoomList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatRoomData = chatRoomList[position]
        val participantCount = chatRoomData.participantList.count() - 1

        Glide.with(context).load(chatRoomData.participantList[1].profileImageUrl).into(holder.chatRoomProfileIV)
        holder.nameTV.text = formChatRoomName(chatRoomData.participantList)
        holder.latestChatMessageTV.text = chatRoomData.lastChatMessage
        holder.participantCountTV.text =  if (participantCount > 1) {
            participantCount.toString()
        }  else {
            ""
        }
    }

    private fun formChatRoomName(participantList: List<UserData>): String {
        val stringBuilder = StringBuilder()
        var isFirstName = true

        participantList.forEach { participant ->
            if (myUserId != participant.id) {

                if (isFirstName) {

                    stringBuilder.append(participant.name)
                    isFirstName = false

                } else {

                    stringBuilder.append(", ").append(participant.name)
                }
            }
        }
        return stringBuilder.toString()
    }
}