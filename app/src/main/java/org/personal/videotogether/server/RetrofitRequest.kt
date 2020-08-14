package org.personal.videotogether.server

import org.personal.videotogether.server.entity.ChatRoomEntity
import org.personal.videotogether.server.entity.FriendEntity
import org.personal.videotogether.server.entity.UserEntity
import org.personal.videotogether.server.entity.YoutubeEntity
import retrofit2.Response
import retrofit2.http.*

interface RetrofitRequest {

    // ------------------------ 로그인/회원가입 관련 ------------------------
    @POST("sign-up")
    suspend fun checkEmailValidation(@Body requestData: RequestData): Response<*>

    @POST("sign-up")
    suspend fun uploadUser(@Body requestData: RequestData): Response<Int>

    @POST("sign-up")
    suspend fun uploadUserProfile(@Body requestData: RequestData): Response<*>

    @POST("sign-in")
    suspend fun signIn(@Body requestData: RequestData): Response<UserEntity>

    // ------------------------ 친구 관련 ------------------------
    @GET("friend-list")
    suspend fun getFriendsList(
        @Query("request") request:String,
        @Query("userId") userId : Int
    ): Response<List<FriendEntity>?>

    @GET("add-friend")
    suspend fun searchFriend(
        @Query("request") request: String,
        @Query("userId") userId: Int,
        @Query("email") friendEmail: String
    ): Response<UserEntity?>

    @POST("add-friend")
    suspend fun addFriend(@Body requestData: RequestData): Response<List<FriendEntity>>

    // ------------------------ 채팅 관련 ------------------------
    @GET("chat-room-list")
    suspend fun getChatRoomList(
        @Query("request") request: String,
        @Query("userId") userId: Int
    ): Response<List<ChatRoomEntity>?>

    @POST("add-chat-room")
    suspend fun addChatRoom(@Body requestData: RequestData): Response<ChatRoomEntity>

    // ------------------------ 유투브 관련 ------------------------
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