package org.personal.videotogether.server.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ChatEntity (
    @SerializedName("room_id")
    @Expose
    val roomId : Int,

    @SerializedName("sender_id")
    @Expose
    val senderId : Int,

    @SerializedName("sender_name")
    @Expose
    val senderName : String,

    @SerializedName("profile_image_url")
    @Expose
    val profileImageUrl : String,

    @SerializedName("message")
    @Expose
    val message : String,

    @SerializedName("message_time")
    @Expose
    val messageTime : String?
)