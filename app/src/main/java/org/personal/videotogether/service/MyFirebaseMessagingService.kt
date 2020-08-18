package org.personal.videotogether.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.GsonBuilder
import org.personal.videotogether.R
import org.personal.videotogether.server.RequestData
import org.personal.videotogether.server.RetrofitRequest
import org.personal.videotogether.util.SharedPreferenceHelper
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = javaClass.name

    private val GROUP_KEY = "org.personal.coupleapp.message"

    companion object {
        const val ACTION_SEND_COUPLE_CHAT = "INTENT_ACTION_SEND_COUPLE_CHAT"
        const val ACTION_SEND_OPEN_CHAT = "INTENT_ACTION_SEND_OPEN_CHAT"
    }

    /*
    새로운 토큰을 파이어베이스로부터 받으면 shared preference 에 저장 -> 서버에 userColumnID 와 함께 저장하기 위해 대기
     */
    override fun onNewToken(token: String) {

        Log.i(TAG, "onNewToken : $token")
        // 새로운 토큰을 받으면 shared preference 에 저장해 놓고, 회원가입이 완료 되거나 로그인을 했을 시에 DB 에 업데이트 해준다.
        val sharedPreferenceHelper = SharedPreferenceHelper()

        sharedPreferenceHelper.setString(this, getText(R.string.firebase_messaging_token).toString(), token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.i(TAG, "onMessageReceived: receive")
    }
}