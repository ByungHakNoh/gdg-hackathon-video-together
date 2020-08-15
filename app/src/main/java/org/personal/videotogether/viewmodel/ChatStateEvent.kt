package org.personal.videotogether.viewmodel

import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.domianmodel.UserData

sealed class ChatStateEvent {
    object GetChatRoomsFromLocal : ChatStateEvent()
    class GetChatRoomsFromServer(val userId: Int) : ChatStateEvent()
    class AddChatRoom(val userData: UserData, val participantList: List<FriendData>) : ChatStateEvent()

    // tcp 관련
    object ConnectToTCPServer : ChatStateEvent()
    class SendToTCPServer(val message: String) : ChatStateEvent()
    object ReceiveFromTCPServer : ChatStateEvent()
}