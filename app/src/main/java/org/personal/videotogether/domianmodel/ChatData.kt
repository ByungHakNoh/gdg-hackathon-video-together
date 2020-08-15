package org.personal.videotogether.domianmodel

data class ChatData(
    val roomId : Int,
    val senderId : Int,
    val senderName : String,
    val profileImageUrl : String,
    val message : String,
    val messageTime : String
)