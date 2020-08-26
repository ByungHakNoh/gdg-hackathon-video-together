package org.personal.videotogether.server.entity

import androidx.room.ColumnInfo
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ChatRoomEntity (
    @SerializedName("id")
    @Expose
    val id : Int,

    @SerializedName("last_chat_message")
    @Expose
    val lastChatMessage: String?,

    @SerializedName("last_message_time")
    @Expose
    val lastChatTime: String?,

    @SerializedName("un_read_chat_count")
    @Expose
    val un_read_chat_count : Int,

    @SerializedName("participant_list")
    @Expose
    val participantList: List<UserEntity>
)