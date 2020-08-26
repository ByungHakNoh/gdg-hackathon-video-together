package org.personal.videotogether.viewmodel

import org.personal.videotogether.domianmodel.ChatData
import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.domianmodel.UserData

sealed class ChatStateEvent {
    object GetChatRoomsFromLocal : ChatStateEvent()
    data class GetChatRoomsFromServer(val userId: Int) : ChatStateEvent()
    data class AddChatRoom(val userData: UserData, val participantList: List<FriendData>) : ChatStateEvent()
    data class UploadChatMessage(val userId: Int, val participantIds: ArrayList<Int>, val chatData: ChatData) : ChatStateEvent()
    data class GetChatMessageFromServer(val roomId: Int) : ChatStateEvent()
    data class GetChatMessageFromLocal(val roomId: Int) : ChatStateEvent()
    data class RefreshUnReadCount(val userId: Int, val roomId: Int) : ChatStateEvent()
    object SignOut : ChatStateEvent()
}