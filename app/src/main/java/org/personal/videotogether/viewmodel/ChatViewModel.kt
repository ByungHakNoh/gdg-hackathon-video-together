package org.personal.videotogether.viewmodel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.personal.videotogether.domianmodel.ChatData
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.repository.ChatRepository
import org.personal.videotogether.util.DataState

@ExperimentalCoroutinesApi
class ChatViewModel
@ViewModelInject // 뷰모델을 hilt 를 사용해서 불러오기
constructor(
    private val chatRepository: ChatRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _chatRoomList: MutableLiveData<List<ChatRoomData>?> = MutableLiveData()
    val chatRoomList: LiveData<List<ChatRoomData>?> get() = _chatRoomList

//    private val _chatMessage : MutableLiveData<List<ChatData>?> = MutableLiveData()
//    val chatMessage: LiveData<List<ChatData>?> get() = _chatMessage

    // ------------------ Add Chat Room live data ------------------
    private val _addChatRoom: MutableLiveData<DataState<ChatRoomData?>> = MutableLiveData()
    val addChatRoom: LiveData<DataState<ChatRoomData?>> get() = _addChatRoom

    // ------------------ Chat Room live data ------------------
    private val _getChatRoomList: MutableLiveData<DataState<List<ChatRoomData>?>> = MutableLiveData()
    val getChatRoomList: LiveData<DataState<List<ChatRoomData>?>> get() = _getChatRoomList

    // ------------------ Chat Message live data ------------------
    private val _getChatFromServer: MutableLiveData<DataState<List<ChatData>>> = MutableLiveData()
    val getChatFromServer: LiveData<DataState<List<ChatData>>> get() = _getChatFromServer

    fun setStateEvent(chatStateEvent: ChatStateEvent) {
        viewModelScope.launch(IO) {
            when (chatStateEvent) {
                is ChatStateEvent.GetChatRoomsFromLocal -> {
                    chatRepository.getChatRoomFromLocal().onEach { chatRoomList ->
                        _chatRoomList.value = chatRoomList
                    }.launchIn(viewModelScope)
                }

                is ChatStateEvent.GetChatRoomsFromServer -> {
                    chatRepository.getChatRoomsFromServer(chatStateEvent.userId).onEach { dataState ->
                        _getChatRoomList.value = dataState

                        // 서버에서 데이터를 가져오면 채팅방 live data 업데이트
                        if (dataState is DataState.Success) _chatRoomList.value = dataState.data
                    }.launchIn(viewModelScope)
                }

                is ChatStateEvent.AddChatRoom -> {
                    chatRepository.addChatRoom(chatStateEvent.userData, chatStateEvent.participantList).onEach { dataState ->
                        _addChatRoom.value = dataState
                        _addChatRoom.value = null
                    }.launchIn(viewModelScope)
                }

                // ------------------ 채팅 메시지 관련 ------------------
                is ChatStateEvent.UploadChatMessage -> {
                    chatRepository.uploadChatMessage(chatStateEvent.userId, chatStateEvent.participantIds, chatStateEvent.chatData)
                }

                // 사용 X
//                is ChatStateEvent.GetChatMessageFromLocal -> {
//                    chatRepository.getChatMessageFromLocal(chatStateEvent.roomId).onEach { dataState ->
//                        _chatMessage.value = dataState
//                        _chatMessage.value = null
//                    }.launchIn(viewModelScope)
//                }

                is ChatStateEvent.GetChatMessageFromServer -> {
                    chatRepository.getChatMessageFromServer(chatStateEvent.roomId).onEach { dataState ->
                        _getChatFromServer.value = dataState
                        _getChatFromServer.value = null
                    }.launchIn(viewModelScope)
                }

                is ChatStateEvent.RefreshUnReadCount -> {
                    chatRepository.refreshUnReadChatCount(chatStateEvent.userId, chatStateEvent.roomId)
                }

                is ChatStateEvent.SignOut -> {
                    withContext(Main) { _chatRoomList.value = null }
                }
            }
        }
    }
}