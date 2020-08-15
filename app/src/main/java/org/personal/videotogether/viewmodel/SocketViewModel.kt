package org.personal.videotogether.viewmodel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.personal.videotogether.domianmodel.ChatData
import org.personal.videotogether.repository.SocketRepository
import org.personal.videotogether.util.DataState

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

    private val _sendMessage: MutableLiveData<DataState<Boolean>?> = MutableLiveData()
    val sendMessage: LiveData<DataState<Boolean>?> get() = _sendMessage

    override fun onCleared() {
        super.onCleared()
        Log.i("TAG", "onCleared: cleared")
        viewModelScope.launch(IO) {
            socketRepository.disConnectFromTCPServer()
        }
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
                    socketRepository.sendToTCPServer(socketStateEvent.flag, socketStateEvent.roomId, socketStateEvent.message)
                }
            }
        }
    }

    override fun onChatMessage(chatData: ChatData) {
        handler.post { _chatMessage.value = chatData }
    }

    override fun onYoutubeMessage(youtubeData: String) {
        TODO("Not yet implemented")
    }
}