package org.personal.videotogether.repository

import android.util.Log
import org.personal.videotogether.domianmodel.ChatData
import org.personal.videotogether.domianmodel.PlayerStateData
import org.personal.videotogether.domianmodel.UserData
import org.personal.videotogether.domianmodel.YoutubeJoinRoomData
import org.personal.videotogether.server.TCPClient
import java.lang.Exception
import java.lang.Float.parseFloat
import java.lang.Integer.parseInt

class SocketRepository {
    private val TAG by lazy { javaClass.name }

    private lateinit var tcpClient: TCPClient

    private var isTCPClientStopped = false // 클라이언트 연결 여부

    interface SocketListener {
        fun onChatMessage(chatData: ChatData)
        fun onYoutubeChatMessage(chatData: ChatData)
        fun onYoutubePlayerState(playerStateData: PlayerStateData)
        fun onYoutubeJoinRoom(youtubeJoinRoomData: YoutubeJoinRoomData)
    }

    // ------------------ TCP 통신 관련 메소드 모음 ------------------
    fun connectToTCPServer() {
        Log.i(TAG, "connection: 연결 시작")
        try {
            tcpClient = TCPClient("3.34.22.62", 20205)

            if (tcpClient.connect()) {
                isTCPClientStopped = false
                Log.e(TAG, "connectToTCPServer: 연결 완료")
            }
            else Log.e(TAG, "connectToTCPServer: tcp 서버 연결 안됨")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "connectToTCPServer: tcp 서버 통신 에러발생($e)")
        }
    }

    fun disConnectFromTCPServer() {
        Log.i(TAG, "sendToTCPServer: disconnect")
        try {
            tcpClient.writeMessage("quit")
            isTCPClientStopped = true

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "disConnectFromTCPServer: $e")
        }
    }

    fun registerSocket(userData: UserData) {
        Log.i(TAG, "registerSocket: register")
        try {
            val socketInfo = "registerUserInfo@${userData.id}@${userData.name}@${userData.profileImageUrl}"
            tcpClient.writeMessage(socketInfo)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "registerSocket: $e")
        }
    }

    fun receiveFromTCPServer(socketListener: SocketListener) {
        try {
            Log.i(TAG, "receiveFromTCPServer: receiving data start")
            while (!isTCPClientStopped) {

                val respond = tcpClient.readMessage()

                // 서버에서 읽어드린 메시지가 있으면 Flag 확인
                if (!respond.isNullOrEmpty()) {

                    val splitMessageData = respond.split("@")
                    Log.i(TAG, "receiveFromTCPServer: $splitMessageData")
                    // 서버에서 보낸 flag 확인
                    when (splitMessageData[0]) {
                        "chat" -> socketListener.onChatMessage(formChatData(splitMessageData))
                        "youtubeChat" -> socketListener.onYoutubeChatMessage(formChatData(splitMessageData))
                        "youtubeState" -> {
                            Log.i(TAG, "receiveFromTCPServer: $splitMessageData")
                            socketListener.onYoutubePlayerState(formYoutubeResponse(splitMessageData))
                        }
                        "youtubeJoinRoom" -> {
                            Log.i(TAG, "receiveFromTCPServer: $splitMessageData")
                            socketListener.onYoutubeJoinRoom(formYoutubeJoinResponse(splitMessageData))
                        }
                    }
                }
            }
            tcpClient.socketClose()
            Log.i(TAG, "receiveFromTCPServer: receiving data stop")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "receiveFromTCPServer: tcp 서버 통신 에러발생($e)")
        }
    }

    fun sendToTCPServer(flag: String, roomId: String?, message: String?) {
        Log.i(TAG, "sendToTCPServer: $message")
        try {
            val formedMessage = "$flag@$roomId@$message"

            tcpClient.writeMessage(formedMessage)
            Log.i(TAG, "전송 완료")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "sendToTCPServer: $e")
        }
    }

    private fun formChatData(splitMessageData: List<String>): ChatData {
        val roomId = parseInt(splitMessageData[1])
        val userId = parseInt(splitMessageData[2])
        val senderName = splitMessageData[3]
        val profileImageUrl = splitMessageData[4]
        val message = splitMessageData[5]
        val messageTime = splitMessageData[6]

        return ChatData(roomId, userId, senderName, profileImageUrl, message, messageTime)
    }

    private fun formYoutubeResponse(splitMessageData: List<String>): PlayerStateData {
        val playerState = splitMessageData[1]
        val playerCurrentSecond = parseFloat(splitMessageData[2])

        return PlayerStateData(playerState, playerCurrentSecond)
    }

    private fun formYoutubeJoinResponse(splitMessageData: List<String>): YoutubeJoinRoomData {
        val flag = splitMessageData[1]
        val visitorId = parseInt(splitMessageData[2])
        val participantCount = parseInt(splitMessageData[3])

        return YoutubeJoinRoomData(flag, visitorId, participantCount)
    }

    companion object {
        const val JOIN_CHAT_ROOM = "joinChatRoom"
        const val EXIT_CHAT_ROOM = "exitChatRoom"
        const val SEND_CHAT_MESSAGE = "sendChatMessage"
        const val CREATE_YOUTUBE_ROOM = "createYoutubeRoom"
        const val JOIN_YOUTUBE_ROOM = "joinYoutubeRoom"
        const val SYNC_YOUTUBE_PLAYER = "syncYoutubePlayer"
        const val EXIT_YOUTUBE_ROOM = "exitYoutubeRoom"
        const val SEND_VISITOR_PLAYER_STATE = "sendPlayerStateToVisitor"
        const val SEND_YOUTUBE_PLAYER_STATE = "sendYoutubePlayerState"
        const val SEND_YOUTUBE_MESSAGE = "sendYoutubeChat"
    }
}