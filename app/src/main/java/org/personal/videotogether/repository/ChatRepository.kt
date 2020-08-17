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
            if (response.code() == 204) emit(DataState.NoData)

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
            if (response.code() == 204) emit(DataState.NoData)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postCheckEmailValid: 서버 통신 에러발생($e)")
            emit(DataState.Error(e))
        }
    }
}