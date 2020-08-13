package org.personal.videotogether.viewmodel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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

    // ------------------ Add Friend live data ------------------
    private val _addChatRoom: MutableLiveData<DataState<ChatRoomData?>> = MutableLiveData()
    val addChatRoom: LiveData<DataState<ChatRoomData?>> get() = _addChatRoom

    private val _getChatRoomList: MutableLiveData<DataState<List<ChatRoomData>?>> = MutableLiveData()
    val getChatRoomList: LiveData<DataState<List<ChatRoomData>?>> get() = _getChatRoomList

    fun setStateEvent(chatStateEvent: ChatStateEvent) {
        viewModelScope.launch {
            when (chatStateEvent) {

                is ChatStateEvent.AddChatRoom -> {
                    chatRepository.addChatRoom(chatStateEvent.userData, chatStateEvent.participantList).onEach { dataState ->
                        _addChatRoom.value = dataState
                        _addChatRoom.value = null
                    }.launchIn(viewModelScope)
                }

                is ChatStateEvent.GetChatRoomList -> {
                    chatRepository.getChatRoomList(chatStateEvent.userId).onEach { dataState ->
                        _getChatRoomList.value = dataState
                    }.launchIn(viewModelScope)
                }
            }
        }
    }
}