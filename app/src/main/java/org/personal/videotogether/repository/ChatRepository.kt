package org.personal.videotogether.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.domianmodel.UserData
import org.personal.videotogether.room.ChatRoomDAO
import org.personal.videotogether.room.entity.ChatRoomCacheMapper
import org.personal.videotogether.server.RequestData
import org.personal.videotogether.server.RetrofitRequest
import org.personal.videotogether.server.TCPClient
import org.personal.videotogether.server.entity.ChatRoomMapper
import org.personal.videotogether.server.entity.FriendMapper
import org.personal.videotogether.server.entity.UserMapper
import org.personal.videotogether.util.DataState
import java.lang.Exception

class ChatRepository
constructor(
    private val retrofitRequest: RetrofitRequest,
    private val chatRoomDAO: ChatRoomDAO,
    private val chatRoomMapper: ChatRoomMapper,
    private val chatRoomCacheMapper: ChatRoomCacheMapper,
    private val userMapper: UserMapper,
    private val friendMapper: FriendMapper
) {
    private val TAG by lazy { javaClass.name }

    private lateinit var tcpClient: TCPClient
    private var isTCPClientStop = false
    private var isSocketRegistered = false
    private var isReceivingMessage = false

    interface SocketListener {
        fun onChatMessage(chatData: String)
        fun onYoutubeMessage(youtubeData: String)
    }

    suspend fun getChatRoomFromLocal(): Flow<List<ChatRoomData>?> = flow {
        try {
            val chatRoomCacheEntityList = chatRoomDAO.getChatRooms()
            val chatRoomList = chatRoomCacheMapper.mapFromEntityList(chatRoomCacheEntityList)
            emit(chatRoomList)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getFriendListFromLocal: 룸 쿼리 중 에러발생($e)")
            emit(null)
        }
    }

    // 채팅방 리스트 가져오기
    suspend fun getChatRoomsFromServer(userId: Int): Flow<DataState<List<ChatRoomData>?>> = flow {
        emit(DataState.Loading)

        try {
            val response = retrofitRequest.getChatRoomList("getChatRoomList", userId)

            if (response.code() == 200) {
                val chatRoomEntityList = response.body()!!
                val chatRoomList = chatRoomMapper.mapFromEntityList(chatRoomEntityList)
                val chatRoomCacheEntityList = chatRoomCacheMapper.mapToEntityList(chatRoomList)

                chatRoomCacheEntityList.forEach { chatRoomEntity ->
                    chatRoomDAO.insertChatRoom(chatRoomEntity)
                }
                val localChatRoomList = chatRoomCacheMapper.mapFromEntityList(chatRoomDAO.getChatRooms())
                emit(DataState.Success(localChatRoomList))
            }
            if (response.code() == 204) emit(DataState.NoData("서버에 데이터 없음 -> 에러 발생"))

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postCheckEmailValid: 서버 통신 에러발생($e)")
            emit(DataState.Error(e))
        }
    }

    // 채팅방 업로드
    // TODO : 친구 데이터 id만 보내도 될 것 같음
    suspend fun addChatRoom(userData: UserData, participantList: List<FriendData>): Flow<DataState<ChatRoomData?>> = flow {
        emit(DataState.Loading)

        try {
            val userEntity = userMapper.mapToEntity(userData)
            val friendDataEntity = friendMapper.mapToEntityList(participantList)
            val postData = HashMap<String, Any>().apply {
                put("user", userEntity)
                put("participants", friendDataEntity)
            }
            val requestData = RequestData("addChatRoom", postData)
            val response = retrofitRequest.addChatRoom(requestData)

            if (response.code() == 200) {
                val chatRoomEntity = response.body()!!
                val chatRoomData = chatRoomMapper.mapFromEntity(chatRoomEntity)
                emit(DataState.Success(chatRoomData))
            }
            if (response.code() == 204) emit(DataState.NoData("서버에 데이터 없음 -> 에러 발생"))

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postCheckEmailValid: 서버 통신 에러발생($e)")
            emit(DataState.Error(e))
        }
    }

    // ------------------ TCP 통신 관련 메소드 모음 ------------------
    fun connectToTCPServer() {
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

        Thread(Runnable {
            try {
                Log.i(TAG, "receiveFromTCPServer: thread 시작")

                while (!isTCPClientStop) {

                    val respond = tcpClient.readMessage()

                    // 서버에서 읽어드린 메시지가 있으면 Flag 확인
                    if (!respond.isNullOrEmpty()) {

                        val splitMessageData = respond.split("@")

                        // 서버에서 보낸 flag 확인
                        when (splitMessageData[0]) {
                            "chat" -> socketListener.onChatMessage(respond)
                            "youtube" -> socketListener.onYoutubeMessage(respond)
                        }
                    }
                }
                tcpClient.socketClose()
                isReceivingMessage = false
                Log.i(TAG, "receiveFromTCPServer: thread 종료")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "receiveFromTCPServer: tcp 서버 통신 에러발생($e)")
            }
        }).start()
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