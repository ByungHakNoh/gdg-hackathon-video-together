package org.personal.videotogether.domianmodel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatRoomData (
    val id : Int,
    val lastChatMessage: String?,
    val participantList: List<UserData>
) : Parcelable