package org.personal.videotogether.repository

import android.util.Log
import org.personal.videotogether.domianmodel.ChatData
import org.personal.videotogether.domianmodel.UserData
import org.personal.videotogether.server.RetrofitRequest
import org.personal.videotogether.server.TCPClient
import java.lang.Exception
import java.lang.Integer.parseInt

class SocketRepository
constructor(
    private val retrofitRequest: RetrofitRequest
) {
    private val TAG by lazy { javaClass.name }

    private lateinit var tcpClient: TCPClient
    private var isTCPClientStop = false // 클라이언트 연결 여부
    private var isSocketRegistered = false // 소켓 등록 헀는지 여부
    private var isReceivingMessage = false // 서버에서 데이터 받는지 여부

    interface SocketListener {
        fun onChatMessage(chatData: ChatData)
        fun onYoutubeMessage(youtubeData: String)
    }

    // ------------------ TCP 통신 관련 메소드 모음 ------------------
    fun connectToTCPServer() {
        Log.i(TAG, "connection: 연결 시작")
        try {
            tcpClient = TCPClient("3.34.22.62", 20205)

            if (tcpClient.connect()) Log.e(TAG, "connectToTCPServer: 연결 완료")
            else Log.e(TAG, "connectToTCPServer: tcp 서버 연결 안됨")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "connectToTCPServer: tcp 서버 통신 에러발생($e)")
        }
    }

    fun disConnectFromTCPServer() {
        tcpClient.writeMessage("quit")
        isTCPClientStop = true
        isSocketRegistered = false
    }

    fun registerSocket(userData: UserData) {
        if (isSocketRegistered) return
        isSocketRegistered = true

        val socketInfo = "registerUserInfo@${userData.id}@${userData.name}@${userData.profileImageUrl}"
        Log.i(TAG, "registerSocket: registered")
        tcpClient.writeMessage(socketInfo)
    }

    fun receiveFromTCPServer(socketListener: SocketListener) {
        if (isReceivingMessage) return
        isReceivingMessage = true

        try {
            Log.i(TAG, "receiveFromTCPServer: receiving data start")
            while (!isTCPClientStop) {

                val respond = tcpClient.readMessage()

                // 서버에서 읽어드린 메시지가 있으면 Flag 확인
                if (!respond.isNullOrEmpty()) {

                    val splitMessageData = respond.split("@")
                    Log.i(TAG, "receiveFromTCPServer: $splitMessageData")
                    // 서버에서 보낸 flag 확인
                    when (splitMessageData[0]) {
                        "chat" -> socketListener.onChatMessage(formChatData(splitMessageData))
                        "youtube" -> socketListener.onYoutubeMessage(respond)
                    }
                }
            }
            tcpClient.socketClose()
            isReceivingMessage = false
            Log.i(TAG, "receiveFromTCPServer: receiving data stop")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "receiveFromTCPServer: tcp 서버 통신 에러발생($e)")
        }
    }

    private fun formChatData(splitMessageData: List<String>): ChatData {
        val roomId = parseInt(splitMessageData[1])
        val userId = parseInt(splitMessageData[2])
        val senderName = splitMessageData[3]
        val profileImageUrl = splitMessageData[4]
        val message = splitMessageData[5]
        val messageTime  = splitMessageData[6]

        return ChatData(roomId, userId, senderName, profileImageUrl, message, messageTime)
    }

    fun sendToTCPServer(flag: String, roomId: String?, message: String?) {

        val formedMessage = "$flag@$roomId@$message"

        tcpClient.writeMessage(formedMessage)
        Log.i(TAG, "전송 완료")
    }


    companion object {
        const val JOIN_CHAT_ROOM = "joinChatRoom"
        const val EXIT_CHAT_ROOM = "exitChatRoom"
        const val SEND_CHAT_MESSAGE = "sendChatMessage"
    }
}