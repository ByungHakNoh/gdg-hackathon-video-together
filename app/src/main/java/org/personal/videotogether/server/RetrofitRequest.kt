package org.personal.videotogether.model.server

import org.personal.videotogether.model.ChatRoomData
import org.personal.videotogether.model.YoutubeVideoData
import org.personal.videotogether.model.server.entity.FriendEntity
import org.personal.videotogether.model.server.entity.UserEntity
import retrofit2.Response
import retrofit2.http.*

interface RetrofitRequest {

    @POST("sign-up")
    suspend fun checkEmailValidation(@Body requestData: RequestData): Response<*>

    @POST("sign-up")
    suspend fun uploadUser(@Body requestData: RequestData): Response<Int>

    @POST("sign-up")
    suspend fun uploadUserProfile(@Body requestData: RequestData): Response<*>

    @POST("sign-in")
    suspend fun signIn(@Body requestData: RequestData): Response<UserEntity>

    @GET("add-friend")
    suspend fun searchFriend(
        @Query("request") request: String,
        @Query("userId") userId: Int,
        @Query("email") friendEmail: String
    ): Response<UserEntity>

    @POST("add-friend")
    suspend fun addFriend(@Body requestData: RequestData): Response<List<FriendEntity>>

    @GET("friends-list")
    suspend fun getMyProfile(@Body requestData: RequestData): Response<UserEntity>

    @GET("friends-list")
    suspend fun getFriendsList(@Body requestData: RequestData): Response<List<UserEntity>>

    // TODO : 채팅 부분 서버 구축해야함
    @POST("chat")
    suspend fun addChatRoom(@Body requestData: RequestData): Response<ChatRoomData>

    @GET("chat")
    suspend fun getChatRoomList(
        @Query("request") request: String,
        @Query("userId") userId: Int
    ): Response<List<ChatRoomData>?>

    @GET("youtube")
    suspend fun getDefaultYoutubeList(
        @Query("request") request: String,
        @Query("youtubeChannel") channel: String
    ): Response<List<YoutubeVideoData>?>

    @GET("youtube")
    suspend fun getSearchedYoutubeList(
        @Query("request") request: String,
        @Query("searchRequest") searchRequest: String
    ): Response<List<YoutubeVideoData>?>
}