package org.personal.videotogether.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat")
data class ChatCacheEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int?,

    @ColumnInfo(name = "room_id")
    val roomId : Int,

    @ColumnInfo(name = "sender_id")
    val senderId : Int,

    @ColumnInfo(name = "sender_name")
    val senderName : String,

    @ColumnInfo(name = "profile_image_url")
    val profileImageUrl : String,

    @ColumnInfo(name = "message")
    val message : String,

    @ColumnInfo(name = "message_time")
    val messageTime : String
//    @ColumnInfo(name = "is_chat_read")
//    val isChatRead: Boolean
)