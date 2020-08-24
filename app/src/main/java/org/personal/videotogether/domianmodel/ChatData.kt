package org.personal.videotogether.domianmodel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatData(
    val roomId : Int,
    val senderId : Int,
    val senderName : String,
    val profileImageUrl : String,
    val message : String,
    val messageTime : String?
) : Parcelable