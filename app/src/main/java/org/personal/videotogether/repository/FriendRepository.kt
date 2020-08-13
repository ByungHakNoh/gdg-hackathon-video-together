package org.personal.videotogether.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.room.FriendDAO
import org.personal.videotogether.room.entity.FriendCacheMapper
import org.personal.videotogether.server.RequestData
import org.personal.videotogether.server.RetrofitRequest
import org.personal.videotogether.server.entity.FriendMapper
import org.personal.videotogether.util.DataState
import java.lang.Exception

class FriendRepository
constructor(
    private val retrofitRequest: RetrofitRequest,
    private val friendDAO: FriendDAO,
    private val friendCacheMapper: FriendCacheMapper,
    private val friendMapper: FriendMapper
) {
    private val TAG by lazy { javaClass.name }

    // 회원가입 시 이메일 중복 체크하는 요청
    suspend fun searchFriend(userId: Int, friendEmail: String): Flow<DataState<FriendData?>> = flow {
        emit(DataState.Loading)

        try {
            val response = retrofitRequest.searchFriend("searchFriend", userId, friendEmail)

            if (response.code() == 200) {
                val friendData = friendMapper.mapUserDataToFriendData(response.body()!!)
                emit(DataState.Success(friendData))
            }

            if (response.code() == 204) emit(DataState.NoData("searchFriend : 서버에 데이터 없음"))

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postCheckEmailValid: 서버 통신 에러발생($e)")
            // TODO: status code 안받아지는 원인 파악하기
            emit(DataState.DuplicatedData)
            emit(DataState.Error(e))
        }
    }

    // 회원가입 시 이메일 중복 체크하는 요청
    suspend fun addFriend(userId: Int, friendUserData: FriendData): Flow<DataState<List<FriendData>?>> = flow {
        emit(DataState.Loading)

        try {
            val friendEntity = friendMapper.mapToEntity(friendUserData)
            val postData = HashMap<String, Any>().apply {
                put("userId", userId)
                put("friendData", friendEntity)
            }
            val requestData = RequestData("addFriend", postData)
            val response = retrofitRequest.addFriend(requestData)

            if (response.code() == 200) {
                val friendList = friendMapper.mapFromEntityList(response.body()!!)
                val friendCacheEntityList = friendCacheMapper.mapToEntityList(friendList)

                friendCacheEntityList.forEach { friendCacheEntity ->
                    friendDAO.insertFriendsData(friendCacheEntity)
                }
                Log.i(TAG, "addFriend: ${friendDAO.getFriendList()}")
                emit(DataState.Success(friendList))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postCheckEmailValid: 서버 통신 에러발생($e)")
            emit(DataState.Error(e))
        }
    }

    suspend fun getFriendListFromLocal(): Flow<List<FriendData>?> = flow {
        try {
            val friendCacheEntityList = friendDAO.getFriendList()
            val friendList = friendCacheMapper.mapFromEntityList(friendCacheEntityList)
            Log.i(TAG, "getFriendListFromLocal: $friendList")
            emit(friendList)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postCheckEmailValid: 서버 통신 에러발생($e)")
            emit(null)
        }
    }
}