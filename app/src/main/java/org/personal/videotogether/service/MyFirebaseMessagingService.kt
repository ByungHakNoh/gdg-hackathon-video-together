package org.personal.videotogether.service

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavDeepLinkBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.personal.videotogether.MyApplication.Companion.CHAT_NOTIFICATION_CHANNEL_ID
import org.personal.videotogether.MyApplication.Companion.YOUTUBE_NOTIFICATION_CHANNEL_ID
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.ChatData
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.domianmodel.YoutubeData
import org.personal.videotogether.repository.ChatRepository
import org.personal.videotogether.server.entity.*
import org.personal.videotogether.util.SharedPreferenceHelper
import java.lang.Integer.parseInt
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = javaClass.name

    private val GROUP_KEY = "org.personal.coupleapp.message"

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    lateinit var youtubeMapper: YoutubeMapper

    @Inject
    lateinit var chatMapper: ChatMapper

    @Inject
    lateinit var chatRoomMapper: ChatRoomMapper

    @Inject
    lateinit var chatRepository: ChatRepository

    /*
    새로운 토큰을 파이어베이스로부터 받으면 shared preference 에 저장 -> 서버에 userColumnID 와 함께 저장하기 위해 대기
     */
    override fun onNewToken(token: String) {

        Log.i(TAG, "onNewToken : $token")
        // 새로운 토큰을 받으면 shared preference 에 저장해 놓고, 회원가입이 완료 되거나 로그인을 했을 시에 DB 에 업데이트 해준다.
        sharedPreferenceHelper.setString(this, getText(R.string.firebase_messaging_token).toString(), token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.i(TAG, "onMessageReceived: receive")

        val request = remoteMessage.data["request"]!!
        val gson = Gson()

        when (request) {
            "youtube" -> {
                val isAppOn = sharedPreferenceHelper.getBoolean(this, getText(R.string.is_app_on).toString())
                val roomId = parseInt(remoteMessage.data["roomId"]!!)
                val inviterName = remoteMessage.data["inviterName"]!!
                val jsonYoutubeEntity = remoteMessage.data["youtubeData"]!!
                val youtubeEntity = gson.fromJson(jsonYoutubeEntity, YoutubeEntity::class.java)
                val youtubeData = youtubeMapper.mapFromEntity(youtubeEntity)

                if (isAppOn) {
                    sendVideoTogetherBroadcast(roomId, inviterName, youtubeData)
                } else {
                    showVideoTogetherNotification(roomId, inviterName, youtubeData)
                }
            }
            "chat" -> {
                val currentChatRoomId = sharedPreferenceHelper.getInt(this, getText(R.string.current_chat_room_id).toString())
                val jsonChatEntity = remoteMessage.data["chatEntity"]
                val jsonChatRoomEntity = remoteMessage.data["chatRoomEntity"]
                val chatEntity = gson.fromJson(jsonChatEntity, ChatEntity::class.java)
                val chatRoomEntity = gson.fromJson(jsonChatRoomEntity, ChatRoomEntity::class.java)
                val chatData = chatMapper.mapFromEntity(chatEntity)
                val chatRoomData = chatRoomMapper.mapFromEntity(chatRoomEntity)

                // 채팅방에 있을 때는 노티피케이션 보여주지 않기
                if (currentChatRoomId != chatData.roomId) {
                    Glide.with(this).asBitmap().load(chatData.profileImageUrl).into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                            showChatNotification(chatRoomData, chatData, resource)
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
                }
                sendChatBroadcast(chatData, currentChatRoomId)
                // 채팅 방 최근 메시지 업데이트
                CoroutineScope(Dispatchers.IO).launch {
                    chatRepository.updateChatRoomLastMessage(chatRoomData, currentChatRoomId)
                }
            }
        }
    }

    private fun sendVideoTogetherBroadcast(roomId: Int, inviterName: String, youtubeData: YoutubeData) {
        val toHomeFragment = Intent().apply {
            action = RECEIVE_VIDEO_TOGETHER_INVITATION
            putExtra("roomId", roomId)
            putExtra("inviterName", inviterName)
            putExtra("youtubeData", youtubeData)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(toHomeFragment)
    }

    private fun showVideoTogetherNotification(roomId: Int, inviterName: String, youtubeData: YoutubeData) {

        val message = "$inviterName 이 유투브 같이보기에 초대했습니다"
        val argument = Bundle().apply {
            putParcelable("youtubeData", youtubeData)
        }
        val pendingIntent = NavDeepLinkBuilder(this)
            .setGraph(R.navigation.main_nav_graph)
            .setDestination(R.id.homeFragment)
            .setArguments(argument)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(this, YOUTUBE_NOTIFICATION_CHANNEL_ID)
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

    private fun sendChatBroadcast(chatData: ChatData, currentChatRoomId: Int) {
        val toHomeFragment = Intent().apply {
            action = RECEIVE_CHAT_MESSAGE
            putExtra("chatData", chatData)
            putExtra("currentChatRoomId", currentChatRoomId)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(toHomeFragment)
    }

    private fun showChatNotification(
        chatRoomData: ChatRoomData,
        chatData: ChatData,
        profileImageBitmap: Bitmap
    ) {
        val argument = Bundle().apply {
            putParcelable("chatRoomData", chatRoomData)
        }
        val pendingIntent = NavDeepLinkBuilder(this)
            .setGraph(R.navigation.main_nav_graph)
            .setDestination(R.id.homeFragment)
            .setArguments(argument)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(this, CHAT_NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(true)
            .setContentTitle(chatData.senderName)
            .setContentText(chatData.message)
            .setSmallIcon(R.drawable.ic_baseline_video_library_24)
            .setLargeIcon(profileImageBitmap)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = NotificationManagerCompat.from(this)
        manager.notify(chatData.roomId, notification)
    }

    companion object {
        const val RECEIVE_VIDEO_TOGETHER_INVITATION = "INTENT_ACTION_SEND_VIDEO_TOGETHER"
        const val RECEIVE_CHAT_MESSAGE = "INTENT_ACTION_SEND_CHAT"
    }
}