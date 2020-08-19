package org.personal.videotogether.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.personal.videotogether.domianmodel.UserData
import org.personal.videotogether.domianmodel.YoutubeData
import org.personal.videotogether.server.RequestData
import org.personal.videotogether.server.RetrofitRequest
import org.personal.videotogether.server.entity.YoutubeMapper
import org.personal.videotogether.util.DataState
import java.lang.Exception

class YoutubeRepository
constructor(
    private val retrofitRequest: RetrofitRequest,
    private val youtubeMapper: YoutubeMapper
) {
    private val TAG by lazy { javaClass.name }

    // 회원가입 시 이메일 중복 체크하는 요청
    suspend fun getDefaultYoutubeList(youtubeChannel: String): Flow<DataState<List<YoutubeData>?>> = flow {
        emit(DataState.Loading)

        try {
            val response = retrofitRequest.getDefaultYoutubeList("getChannelVideos", youtubeChannel)

            if (response.code() == 200) {
                val youtubeEntityList = response.body()!!
                val youtubeList = youtubeMapper.mapFromEntityList(youtubeEntityList)
                emit(DataState.Success(youtubeList))
            }
            if (response.code() == 204) emit(DataState.NoData)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postCheckEmailValid: 서버 통신 에러발생($e)")
            // TODO: status code 안받아지는 원인 파악하기
            emit(DataState.DuplicatedData)
            emit(DataState.Error(e))
        }
    }

    suspend fun inviteVideoTogether(inviterUserData: UserData, friendIds: List<Int>, youtubeData: YoutubeData): Flow<DataState<Boolean?>> = flow {
        Log.i(TAG, "inviteVideoTogether: 전송?")
        try {
            val youtubeEntity = youtubeMapper.mapToEntity(youtubeData)
            val postData = HashMap<String, Any?>().apply {
                put("inviterId", inviterUserData.id)
                put("inviterName", inviterUserData.name)
                put("friendsIds", friendIds)
                put("youtubeData", youtubeEntity)
            }
            val requestData = RequestData("inviteFriends", postData)
            val response = retrofitRequest.inviteFriends(requestData)

            if (response.code() == 200) {

                emit(DataState.Success(true))
            }
            if (response.code() == 204) emit(DataState.NoData)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postCheckEmailValid: 서버 통신 에러발생($e)")
            // TODO: status code 안받아지는 원인 파악하기
        }
    }
}