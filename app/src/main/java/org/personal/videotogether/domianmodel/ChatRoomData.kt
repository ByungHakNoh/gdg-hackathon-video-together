package org.personal.videotogether.domianmodel

import org.personal.videotogether.server.entity.UserEntity

data class ChatRoomData (
    val id : Int,
    val lastChatMessage: String?,
    val participantsList: List<UserEntity>
)