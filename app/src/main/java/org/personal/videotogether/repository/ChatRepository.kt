package org.personal.videotogether.repository

import android.annotation.SuppressLint
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.personal.videotogether.domianmodel.ChatData
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.domianmodel.UserData
import org.personal.videotogether.room.ChatDAO
import org.personal.videotogether.room.ChatRoomDAO
import org.personal.videotogether.room.entity.ChatCacheMapper
import org.personal.videotogether.room.entity.ChatRoomCacheMapper
import org.personal.videotogether.server.RequestData
import org.personal.videotogether.server.RetrofitRequest
import org.personal.videotogether.server.entity.ChatMapper
import org.personal.videotogether.server.entity.ChatRoomMapper
import org.personal.videotogether.server.entity.FriendMapper
import org.personal.videotogether.server.entity.UserMapper
import org.personal.videotogether.util.DataState
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NAME_SHADOWING")
class ChatRepository
constructor(
    private val retrofitRequest: RetrofitRequest,
    private val chatRoomDAO: ChatRoomDAO,
    private val chatDAO: ChatDAO,
    private val chatRoomMapper: ChatRoomMapper,
    private val chatRoomCacheMapper: ChatRoomCacheMapper,
    private val chatMapper: ChatMapper,
    private val chatCacheMapper: ChatCacheMapper,
    private val userMapper: UserMapper,
    private val friendMapper: FriendMapper
) {
    private val TAG by lazy { javaClass.name }

    // ------------------ 채팅방 메소드 모음 ------------------
    // 채팅방 로컬에서 가져오기
    suspend fun getChatRoomFromLocal(): Flow<List<ChatRoomData>?> = flow {
        try {
            emit(orderChatRooms())

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

                chatRoomCacheEntityList.forEach { chatRoomEntity -> chatRoomDAO.insertChatRoom(chatRoomEntity) }

                emit(DataState.Success(orderChatRooms()))
            }
            if (response.code() == 204) emit(DataState.NoData)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postCheckEmailValid: 서버 통신 에러발생($e)")
            emit(DataState.Error(e))
        }
    }

    // 채팅 방 룸에서 가져와서 마지막 메시지 시간에 따라 정렬하는 메소드 -> 채팅방을 서버, 로컬에서 가져올 때 사용
    private suspend fun orderChatRooms(): List<ChatRoomData>? {
        val localChatRoomList = chatRoomCacheMapper.mapFromEntityList(chatRoomDAO.getChatRooms())
        val currentTimeDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val orderedChatRoomList = localChatRoomList.sortedByDescending { chatRoom ->
            if (chatRoom.lastChatTime != null) {
                currentTimeDateFormat.parse(chatRoom.lastChatTime!!).time
            } else {
                0
            }
        }
        orderedChatRoomList.forEach { chatRoom ->
            if (chatRoom.lastChatTime != null) {
                val messageTime = currentTimeDateFormat.parse(chatRoom.lastChatTime!!).time
                chatRoom.lastChatTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(messageTime)
            }
        }
        return orderedChatRoomList
    }

    // 채팅방 업로드
    suspend fun addChatRoom(userData: UserData, participantList: List<FriendData>, friendIds: ArrayList<Int>): Flow<DataState<ChatRoomData?>> = flow {
        emit(DataState.Loading)

        try {
            val userEntity = userMapper.mapToEntity(userData)
            val friendDataEntity = friendMapper.mapToEntityList(participantList)
            val postData = HashMap<String, Any>().apply {
                put("user", userEntity)
                put("participants", friendDataEntity)
                put("friendIds", friendIds)
            }
            val requestData = RequestData("addChatRoom", postData)
            val response = retrofitRequest.addChatRoom(requestData)

            if (response.code() == 200) {
                val chatRoomEntity = response.body()!!
                val chatRoomData = chatRoomMapper.mapFromEntity(chatRoomEntity)
                val chatRoomCacheEntity = chatRoomCacheMapper.mapToEntity(chatRoomData)
                chatRoomDAO.insertChatRoom(chatRoomCacheEntity)

                emit(DataState.Success(chatRoomData))
            }
            if (response.code() == 204) emit(DataState.NoData)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postCheckEmailValid: 서버 통신 에러발생($e)")
            emit(DataState.Error(e))
        }
    }

    // ------------------ 채팅 데이터 관련 메소드 모음 ------------------
    suspend fun uploadChatMessage(userId: Int, participantIds: ArrayList<Int>, chatData: ChatData) {
        try {
            val chatEntity = chatMapper.mapToEntity(chatData)
            val postData = HashMap<String, Any?>().apply {
                put("userId", userId)
                put("participantIds", participantIds)
                put("chatData", chatEntity)
            }
            val requestData = RequestData("uploadChatMessage", postData)
            val response = retrofitRequest.uploadChatMessage(requestData)

            if (response.code() == 200) {
                val responseChatEntity = response.body()!!
                val responseChatData = chatMapper.mapFromEntity(responseChatEntity)
                val chatCacheEntity = chatCacheMapper.mapToEntity(responseChatData)

                chatDAO.insertChat(chatCacheEntity)
            }
            if (response.code() == 204) Log.i(TAG, "uploadChatMessage: 서버에 문제 발생")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(TAG, "uploadChatMessage: 에러 발생 - $e")
        }
    }

    // 서버에서 채팅 데이터 가져오기
    suspend fun getChatMessageFromServer(roomId: Int): Flow<DataState<List<ChatData>>> = flow {
        emit(DataState.Loading)
        try {
            val response = retrofitRequest.getChatMessageList("getChatMessage", roomId)
            Log.i(TAG, "getChatMessageList: $response")
            if (response.code() == 200) {
                val responseChatEntityList = response.body()!!
                val responseChatDataList = chatMapper.mapFromEntityList(responseChatEntityList)

                emit(DataState.Success(responseChatDataList))
            }
            if (response.code() == 204) emit(DataState.NoData)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(TAG, "getChatMessageList: 에러 발생 - $e")
            emit(DataState.Error(e))
        }
    }

    // 로컬에서 채팅 데이터 가져오기
    suspend fun getChatMessageFromLocal(roomId: Int): Flow<List<ChatData>?> = flow {
        try {
            val chatCacheEntity = chatDAO.getChatList(roomId)
            val chatDataList = chatCacheMapper.mapFromEntityList(chatCacheEntity)

            emit(chatDataList)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(TAG, "getChatMessageList: 에러 발생 - $e")
            emit(null)
        }
    }

    // ------------------ 채팅방 마지막 메시지, 시간, 안읽은 갯수 관리하는 메소드 ------------------
    // 파이어베이스에서 받아온 채팅 데이터 업로드 업로드
    suspend fun updateChatRoomLastMessage(chatRoomData: ChatRoomData, currentChatRoomId: Int) {
        try {
            chatRoomDAO.updateLastChat(chatRoomData.id, chatRoomData.lastChatMessage!!, chatRoomData.lastChatTime!!)
            if (chatRoomData.id != currentChatRoomId) {
                chatRoomDAO.addUnReadCount(chatRoomData.id)
                Log.i(TAG, "updateChatRoomLastMessage: working?")
            }
            Log.i(TAG, "updateChatRoomLastMessage: ${chatRoomDAO.getChatRooms()}")
        } catch (e: Exception) {
            Log.i(TAG, "updateChatRoomLastMessage: 에러 발생 - $e")
        }
    }

    // 채팅방 읽지 않은 메시지 초기화하는 메소드
    suspend fun refreshUnReadChatCount(userId: Int, roomId: Int) {
        try {
            val putData = HashMap<String, Any>().apply {
                put("userId", userId)
                put("roomId", roomId)
            }
            val requestData = RequestData("refreshUnReadChatCount", putData)
            val response = retrofitRequest.refreshUnReadChatCount(requestData)
            if (response.code() == 200) Log.i(TAG, "refreshUnReadChatCount: server refreshed unread chat count")
        } catch (e: Exception) {
            Log.i(TAG, "refreshUnReadCount: 에러 발생 - $e")
        }
    }
}