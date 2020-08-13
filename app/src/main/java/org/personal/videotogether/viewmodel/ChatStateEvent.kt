package org.personal.videotogether.viewmodel

import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.domianmodel.UserData

sealed class ChatStateEvent{
    class AddChatRoom(val userData: UserData, val participantList: List<FriendData>) : ChatStateEvent()
    class GetChatRoomList(val userId: Int): ChatStateEvent()
}