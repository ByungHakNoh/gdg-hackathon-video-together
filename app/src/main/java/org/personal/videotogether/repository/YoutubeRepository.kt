package org.personal.videotogether.repository

import android.media.ThumbnailUtils
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.personal.videotogether.domianmodel.UserData
import org.personal.videotogether.domianmodel.YoutubeData
import org.personal.videotogether.domianmodel.YoutubePageData
import org.personal.videotogether.server.RequestData
import org.personal.videotogether.server.RetrofitRequest
import org.personal.videotogether.server.entity.YoutubeMapper
import org.personal.videotogether.server.entity.YoutubePageMapper
import org.personal.videotogether.util.DataState
import java.lang.Exception

class YoutubeRepository
constructor(
    private val retrofitRequest: RetrofitRequest,
    private val youtubeMapper: YoutubeMapper,
    private val youtubePageMapper: YoutubePageMapper
) {
    private val TAG by lazy { javaClass.name }

    // ------------------ 유투브 영상 데이터 가져오는 메소드 ------------------
    // (페이지 : 유투브 API 단위)
    // 유투브 첫 페이지 가져오기
    suspend fun getYoutubePage(youtubeChannel: String): Flow<DataState<YoutubePageData?>> = flow {
        emit(DataState.Loading)
        Log.i(TAG, "getYoutubePage: $youtubeChannel")
        try {
            val response = retrofitRequest.getDefaultYoutubeList("getChannelVideos", youtubeChannel)
            Log.i(TAG, "getYoutubePage: $response")
            if (response.code() == 200) {
                val youtubePageEntity = response.body()!!
                val youtubePageData = youtubePageMapper.mapFromEntity(youtubePageEntity)
                emit(DataState.Success(youtubePageData))
            }
            if (response.code() == 204) emit(DataState.NoData)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postCheckEmailValid: 서버 통신 에러발생($e)")
            // TODO: status code 안받아지는 원인 파악하기
            emit(DataState.Error(e))
        }
    }

    // 다음 유투브 페이지 가져오기
    suspend fun getYoutubeNextPage(nextPageUrl: String, nextPageToken: String, channelTitle: String, channelThumbnail: String): Flow<DataState<YoutubePageData?>> = flow {
        emit(DataState.Loading)

        try {
            val response = retrofitRequest.getNextPageYoutubeList("getNextPage", nextPageUrl, nextPageToken, channelTitle, channelThumbnail)

            if (response.code() == 200) {
                val youtubePageEntity = response.body()!!
                val youtubePageData = youtubePageMapper.mapFromEntity(youtubePageEntity)
                emit(DataState.Success(youtubePageData))
            }
            if (response.code() == 204) emit(DataState.NoData)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postCheckEmailValid: 서버 통신 에러발생($e)")
            // TODO: status code 안받아지는 원인 파악하기
            emit(DataState.Error(e))
        }
    }

    // ------------------ 유투브 같이보기 관련 메소드 ------------------
    // 유투브 같이보기 초대
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
            Log.i(TAG, "inviteVideoTogether: $response")
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