package org.personal.videotogether.server.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ChatRoomEntity (
    @SerializedName("id")
    @Expose
    val id : Int,

    @SerializedName("last_chat_message")
    @Expose
    val lastChatMessage: String?,

    @SerializedName("participants_list")
    @Expose
    val participantsList: List<UserEntity>
)