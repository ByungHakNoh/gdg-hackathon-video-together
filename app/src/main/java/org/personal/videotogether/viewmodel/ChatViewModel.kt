package org.personal.videotogether.viewmodel

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    // ------------------ Add Chat Room live data ------------------
    private val _addChatRoom: MutableLiveData<DataState<ChatRoomData?>> = MutableLiveData()
    val addChatRoom: LiveData<DataState<ChatRoomData?>> get() = _addChatRoom

    // ------------------ Add Chat Room live data ------------------
    private val _getChatRoomList: MutableLiveData<DataState<List<ChatRoomData>?>> = MutableLiveData()
    val getChatRoomList: LiveData<DataState<List<ChatRoomData>?>> get() = _getChatRoomList

    fun setStateEvent(chatStateEvent: ChatStateEvent) {
        viewModelScope.launch(IO) {
            when (chatStateEvent) {

                is ChatStateEvent.GetChatRoomsFromLocal -> {
                    chatRepository.getChatRoomFromLocal().onEach { dataState ->
                        _chatRoomList.value = dataState
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
            }
        }
    }
}