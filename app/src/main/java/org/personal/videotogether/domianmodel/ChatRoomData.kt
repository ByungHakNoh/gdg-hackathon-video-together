package org.personal.videotogether.domianmodel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatRoomData(
    val id: Int,
    var lastChatMessage: String?,
    var lastChatTime: String?,
    val participantList: List<UserData>,
    var unReadChatCount: Int,
    var isSelected: Boolean?
) : Parcelable