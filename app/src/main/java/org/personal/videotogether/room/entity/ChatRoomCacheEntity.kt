package org.personal.videotogether.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_room")
data class ChatRoomCacheEntity (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id : Int,

    @ColumnInfo(name = "last_chat_message")
    val last_chat_message : String?,

    @ColumnInfo(name = "participant_list")
    val participant_list : String?
)
