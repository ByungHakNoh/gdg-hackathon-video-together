package org.personal.videotogether.viewmodel

import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.domianmodel.UserData

sealed class ChatStateEvent {
    object GetChatRoomsFromLocal : ChatStateEvent()
    data class GetChatRoomsFromServer(val userId: Int) : ChatStateEvent()
    data class AddChatRoom(val userData: UserData, val participantList: List<FriendData>) : ChatStateEvent()
}