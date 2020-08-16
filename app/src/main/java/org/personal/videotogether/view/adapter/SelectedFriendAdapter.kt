package org.personal.videotogether.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.FriendData

class SelectedFriendAdapter
constructor(
    val context: Context,
    private val friendList: ArrayList<FriendData>,
    private val itemClickListener: ItemClickListener
) : RecyclerView.Adapter<SelectedFriendAdapter.ViewHolder>() {

    class ViewHolder(itemView: View, private val itemClickListener: ItemClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.findViewById<ImageButton>(R.id.deleteBtn).setOnClickListener(this)
        }

        val friendProfileIV: ImageView = itemView.findViewById(R.id.profileIV)
        val friendName: TextView = itemView.findViewById(R.id.nameTV)
        private val deleteBtn: ImageButton = itemView.findViewById(R.id.deleteBtn)

        override fun onClick(view: View?) {

            if (adapterPosition != RecyclerView.NO_POSITION) {

                itemClickListener.onItemClick(view, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_friend, parent, false)
        return ViewHolder(view, itemClickListener)
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friendData = friendList[position]
        Glide.with(context).load(friendData.profileImageUrl).into(holder.friendProfileIV)
        holder.friendName.text = friendData.name
    }
}