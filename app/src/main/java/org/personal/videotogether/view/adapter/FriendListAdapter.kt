package org.personal.videotogether.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.FriendData

class FriendListAdapter
constructor(
    val context: Context,
    private var friendList: ArrayList<FriendData>,
    private val isSelectable: Boolean,
    private val itemClickListener: ItemClickListener
) : RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View, private val itemClickListener: ItemClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        val friendProfileIV: ImageView = itemView.findViewById(R.id.profileIV)
        val friendName: TextView = itemView.findViewById(R.id.nameTV)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkboxCB)

        override fun onClick(view: View?) {

            if (adapterPosition != RecyclerView.NO_POSITION) {
                itemClickListener.onItemClick(view, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        if (isSelectable) view.findViewById<CheckBox>(R.id.checkboxCB).visibility = View.VISIBLE
        return ViewHolder(view, itemClickListener)
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friendData = friendList[position]
        Glide.with(context).load(friendData.profileImageUrl).into(holder.friendProfileIV)
        holder.friendName.text = friendData.name

        if (isSelectable) {
            if (friendData.isSelected != null)  {
                holder.checkBox.isChecked = friendData.isSelected!!
            }
        }
    }

    fun filterList(filteredFriendList: ArrayList<FriendData>) {
        friendList = filteredFriendList
        Log.i("TAG", "filterList: $friendList")
        Log.i("TAG", "filterList: $filteredFriendList")
        notifyDataSetChanged()
    }
}