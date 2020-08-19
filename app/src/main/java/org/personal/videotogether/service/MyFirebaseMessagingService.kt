package org.personal.videotogether.service

import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.personal.videotogether.MyApplication.Companion.CHAT_NOTIFICATION_CHANNEL_ID
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.YoutubeData
import org.personal.videotogether.server.entity.YoutubeEntity
import org.personal.videotogether.server.entity.YoutubeMapper
import org.personal.videotogether.util.SharedPreferenceHelper
import java.lang.Integer.parseInt

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
        val senderName = remoteMessage.data["inviterName"]
        showNotification(remoteMessage.data)

    }

    private fun showNotification(remoteMessageData: MutableMap<String, String>) {
        val gson = Gson()
        val youtubeMapper = YoutubeMapper()
        val roomId = parseInt(remoteMessageData["roomId"]!!)
        val inviterName = remoteMessageData["inviterName"]!!
        val jsonYoutubeEntity =  remoteMessageData["youtubeData"]!!
        val youtubeEntity = gson.fromJson(jsonYoutubeEntity, YoutubeEntity::class.java)
        val youtubeData = youtubeMapper.mapFromEntity(youtubeEntity)
        val message = "$inviterName 이 유투브 같이보기에 초대했습니다"
        val argument = Bundle().apply {
            putParcelable("youtubeData", youtubeData)
        }
        val pendingIntent = NavDeepLinkBuilder(this)
            .setGraph(R.navigation.main_nav_graph)
            .setDestination(R.id.homeFragment)
            .setArguments(argument)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(this, CHAT_NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(true)
            .setContentTitle("유투브 같이보기")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_baseline_video_library_24)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = NotificationManagerCompat.from(this)
        manager.notify(roomId, notification)
    }
}