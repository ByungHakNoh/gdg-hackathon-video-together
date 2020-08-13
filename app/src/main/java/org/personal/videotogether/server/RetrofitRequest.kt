package org.personal.videotogether.server

import org.personal.videotogether.server.entity.ChatRoomEntity
import org.personal.videotogether.server.entity.FriendEntity
import org.personal.videotogether.server.entity.UserEntity
import org.personal.videotogether.server.entity.YoutubeEntity
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


    @POST("chat")
    suspend fun addChatRoom(@Body requestData: RequestData): Response<ChatRoomEntity>

    @GET("chat")
    suspend fun getChatRoomList(
        @Query("request") request: String,
        @Query("userId") userId: Int
    ): Response<List<ChatRoomEntity>?>

    @GET("youtube")
    suspend fun getDefaultYoutubeList(
        @Query("request") request: String,
        @Query("youtubeChannel") channel: String
    ): Response<List<YoutubeEntity>?>

    @GET("youtube")
    suspend fun getSearchedYoutubeList(
        @Query("request") request: String,
        @Query("searchRequest") searchRequest: String
    ): Response<List<YoutubeEntity>?>
}