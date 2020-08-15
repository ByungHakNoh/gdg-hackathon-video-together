package org.personal.videotogether.viewmodel

import org.personal.videotogether.domianmodel.UserData

sealed class SocketStateEvent {
    // tcp 관련
    object ConnectToTCPServer : SocketStateEvent()
    object DisconnectFromTCPServer: SocketStateEvent()
    class RegisterSocket(val userData: UserData) : SocketStateEvent()
    class SendToTCPServer(val flag: String, val roomId: String? = null, val message: String? = null) : SocketStateEvent()
    object ReceiveFromTCPServer : SocketStateEvent()
}