package org.personal.videotogether.model.server

import org.personal.videotogether.model.server.entity.UserEntity
import retrofit2.Response
import retrofit2.http.*

interface RetrofitRequest {

    @POST("sign-up")
    suspend fun checkEmailValidation(@Body requestData: RequestData) : Response<*>

    @POST("sign-up")
    suspend fun uploadUser(@Body requestData: RequestData) : Response<Int>

    @POST("sign-up")
    suspend fun uploadUserProfile(@Body requestData: RequestData) : Response<UserEntity>

    @POST("sign-in")
    suspend fun signIn(@Body requestData: RequestData) : Response<UserEntity>
}