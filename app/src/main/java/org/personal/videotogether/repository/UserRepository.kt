package org.personal.videotogether.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.personal.videotogether.domianmodel.UserData
import org.personal.videotogether.room.ChatRoomDAO
import org.personal.videotogether.room.FriendDAO
import org.personal.videotogether.room.entity.UserCacheEntity
import org.personal.videotogether.room.entity.UserCacheMapper
import org.personal.videotogether.room.UserDAO
import org.personal.videotogether.server.entity.UserMapper
import org.personal.videotogether.server.RetrofitRequest
import org.personal.videotogether.server.RequestData
import org.personal.videotogether.util.DataState
import java.lang.Exception

class UserRepository
constructor(
    private val retrofitRequest: RetrofitRequest,
    private val userDAO: UserDAO,
    private val userCacheMapper: UserCacheMapper,
    private val userMapper: UserMapper,
    private val chatRoomDAO: ChatRoomDAO,
    private val friendDAO: FriendDAO
) {

    private val TAG by lazy { javaClass.name }

    // ------------------유저 데이터 가져오는 메소드 ------------------
    suspend fun getUserDataFromLocal(): Flow<UserData?> = flow {
        try {
            val userCacheEntity = userDAO.getUserData()
            Log.i(TAG, "getUserDataFromLocal: $userCacheEntity")
            val userData = userCacheMapper.mapFromEntity(userCacheEntity[0])

            emit(userData)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getUserDataFromLocal: 룸 쿼리 도중 에러발생($e)")
            emit(null)
        }
    }

    // ------------------ 회원가입 관련 메소드 ------------------
    // 회원가입 시 이메일 중복 체크하는 요청
    suspend fun postCheckEmailValid(email: String): Flow<DataState<Boolean?>> = flow {
        emit(DataState.Loading)
        Log.e(TAG, "test")
        try {
            val response = retrofitRequest.existsUserByEmail(email)

            if (response.code() == 200) emit(DataState.Success(false))
            if (response.code() == 404) emit(DataState.Success(true))

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postCheckEmailValid: 서버 통신 에러발생($e)")
            emit(DataState.Error(e))
        }
    }

    // 회원가입 완료 시 서버에 유저 데이터 업로드 요청
    // 회원정보는 룸을 사용해 로컬에 저장
    suspend fun uploadUser(email: String, password: String): Flow<DataState<Boolean?>> = flow {
        emit(DataState.Loading)

        try {
            val response = retrofitRequest.uploadUser(email, password)

            if (response.code() == 200) {
                val insertedId = response.body()!!
                // 이름과 프로필 이미지를 제외한 정보 저장
                val userCacheEntity = UserCacheEntity(insertedId, email, password, null, null)

                userDAO.insertUserData(userCacheEntity)
                emit(DataState.Success(true))
            }
            if (response.code() == 204) emit(DataState.NoData)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "uploadUser: 서버 통신 에러발생($e)")
            emit(DataState.Error(e))
        }
    }

    // 유저 프로필을 서버로 업로드하는 메소드
    suspend fun uploadUserProfile(base64Image: String, name: String, firebaseToken: String): Flow<DataState<Boolean?>> = flow {
        emit(DataState.Loading)

        try {
            val userTableId = userDAO.getUserData()[0].id
            val postData = HashMap<String, Any?>().apply {
                put("id", userTableId)
                put("base64Image", base64Image)
                put("name", name)
                put("firebaseToken", firebaseToken)
            }
            val requestData = RequestData("uploadUserProfile", postData)
            val response = retrofitRequest.uploadUserProfile(requestData)

            if (response.code() == 200) {
                val userData = userMapper.mapFromEntity(response.body()!!)
                val userCacheEntity = userCacheMapper.mapToEntity(userData)
                userDAO.insertUserData(userCacheEntity)

                emit(DataState.Success(true))
            }
            if (response.code() == 204) emit(DataState.NoData)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "uploadUser: 서버 통신 에러발생($e)")
            emit(DataState.Error(e))
        }
    }

    // ------------------ 로그인 / 로그아웃 관련 메소드 ------------------
    // 로그인
    suspend fun signIn(email: String, password: String): Flow<DataState<UserData>> = flow {
        emit(DataState.Loading)

        try {
            val response = retrofitRequest.signIn(email, password)

            if (response.code() == 200) {
                val userData = userMapper.mapFromEntity(response.body()!!)
                val userCacheEntity = userCacheMapper.mapToEntity(userData)
                userDAO.insertUserData(userCacheEntity)

                emit(DataState.Success(userData))
            }
            if (response.code() == 204) emit(DataState.NoData)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postCheckEmailValid: 서버 통신 에러발생($e)")
            emit(DataState.Error(e))
        }
    }

    // 로그아웃
    suspend fun signOut() {
        try {
            userDAO.deleteAllUserData()
            chatRoomDAO.deleteAllChatRoomData()
            friendDAO.deleteAllFriendsData()
        } catch (e: Exception) {
            Log.i(TAG, "signOut: 룸 에러 발생 $e")
        }
    }

    // 기기 파이어베이스 토큰 값 서버에 업로드(업데이트)
    suspend fun uploadFirebaseToken(userId: Int, firebaseToken: String): Flow<DataState<Boolean>> = flow {
        emit(DataState.Loading)
        try {
            val postData = HashMap<String, Any?>().apply {
                put("userId", userId)
                put("token", firebaseToken)
            }
            val requestData = RequestData("uploadFirebaseToken", postData)
            val response = retrofitRequest.uploadFirebaseToken(requestData)

            if (response.code() == 200) emit(DataState.Success(true)) // 업로드가 됬을 때
            if (response.code() == 204) emit(DataState.DuplicatedData) // 이미 존재할 때

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "uploadFirebaseToken: 서버 통신 에러발생($e)")
            emit(DataState.Error(e))
        }
    }

    // ------------------ 유저 프로필 변경 관련 메소드 ------------------
    // 유저 프로필을 서버로 업로드하는 메소드
    suspend fun updateUserProfile(userId: Int, base64Image: String?, name: String): Flow<DataState<UserData>> = flow {
        emit(DataState.Loading)

        try {
            val postData = HashMap<String, Any?>().apply {
                put("userId", userId)
                put("base64Image", base64Image)
                put("name", name)
            }
            val requestData = RequestData("updateUserProfile", postData)
            val response = retrofitRequest.updateUserProfile(requestData)

            if (response.code() == 200) {
                val userData = userMapper.mapFromEntity(response.body()!!)
                val userCacheEntity = userCacheMapper.mapToEntity(userData)
                userDAO.insertUserData(userCacheEntity)

                emit(DataState.Success(userData))
            }
            if (response.code() == 204) emit(DataState.NoData)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "uploadUser: 서버 통신 에러발생($e)")
            emit(DataState.Error(e))
        }
    }
}