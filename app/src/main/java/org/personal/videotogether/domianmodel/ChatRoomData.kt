package org.personal.videotogether.domianmodel

data class ChatRoomData (
    val id : Int,
    val lastChatMessage: String?,
    val participantList: List<UserData>
)