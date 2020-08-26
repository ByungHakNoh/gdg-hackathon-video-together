package org.personal.videotogether.viewmodel

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.res.TypedArrayUtils.getText
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.ChatData
import org.personal.videotogether.domianmodel.PlayerStateData
import org.personal.videotogether.domianmodel.YoutubeJoinRoomData
import org.personal.videotogether.repository.SocketRepository
import org.personal.videotogether.util.SharedPreferenceHelper

@ExperimentalCoroutinesApi
class SocketViewModel
@ActivityRetainedScoped
@ViewModelInject // 뷰모델을 hilt 를 사용해서 불러오기
constructor(
    private val socketRepository: SocketRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel(), SocketRepository.SocketListener {

    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private var isSocketConnected = false

    // ------------------ TCP 통신 live data ------------------
    private val _chatMessage: MutableLiveData<ChatData?> = MutableLiveData()
    val chatMessage: LiveData<ChatData?> get() = _chatMessage

    private val _youtubePlayerState: MutableLiveData<PlayerStateData?> = MutableLiveData()
    val youtubePlayerState: LiveData<PlayerStateData?> get() = _youtubePlayerState

    private val _youtubeJoinRoomData: MutableLiveData<YoutubeJoinRoomData?> = MutableLiveData()
    val youtubeJoinRoomData: LiveData<YoutubeJoinRoomData?> get() = _youtubeJoinRoomData

    private val _youtubeChatMessage: MutableLiveData<ChatData?> = MutableLiveData()
    val youtubeChatMessage: LiveData<ChatData?> get() = _youtubeChatMessage
    // 액티비티 onCreate 에서 연결한 소켓 제거
    override fun onCleared() {
        super.onCleared()
        Log.i("TAG", "onCleared: cleared")
        setStateEvent(SocketStateEvent.DisconnectFromTCPServer)
    }

    fun setStateEvent(socketStateEvent: SocketStateEvent) {
        viewModelScope.launch(IO) {
            when (socketStateEvent) {

                is SocketStateEvent.ConnectToTCPServer -> {
                    if (!isSocketConnected) socketRepository.connectToTCPServer()
                    isSocketConnected = true
                }

                is SocketStateEvent.DisconnectFromTCPServer -> {
                    socketRepository.disConnectFromTCPServer()
                }

                is SocketStateEvent.RegisterSocket -> {
                    socketRepository.registerSocket(socketStateEvent.userData)
                }

                is SocketStateEvent.ReceiveFromTCPServer -> {
                    socketRepository.receiveFromTCPServer(this@SocketViewModel)
                }

                is SocketStateEvent.SendToTCPServer -> {
                    socketRepository.sendToTCPServer(socketStateEvent.flag, socketStateEvent.firstMessage, socketStateEvent.secondMessage)
                }
            }
        }
    }

    override fun onChatMessage(chatData: ChatData) {
        handler.post {
            _chatMessage.value = chatData
            _chatMessage.value = null
        }
    }

    override fun onYoutubeChatMessage(chatData: ChatData) {
        Log.i("TAG", "onYoutubeChatMessage: $chatData")
        handler.post {
            _youtubeChatMessage.value = chatData
            _youtubeChatMessage.value = null
        }
    }

    override fun onYoutubePlayerState(playerStateData: PlayerStateData) {
        handler.post {
            _youtubePlayerState.value = playerStateData
            _youtubePlayerState.value = null
        }
    }

    override fun onYoutubeJoinRoom(youtubeJoinRoomData: YoutubeJoinRoomData) {
        handler.post {
            _youtubeJoinRoomData.value = youtubeJoinRoomData
            _youtubeJoinRoomData.value = null
        }
    }
}