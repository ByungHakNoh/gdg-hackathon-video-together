package org.personal.videotogether.viewmodel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.repository.FriendRepository
import org.personal.videotogether.util.DataState

@ExperimentalCoroutinesApi
class FriendViewModel
@ViewModelInject // 뷰모델을 hilt 를 사용해서 불러오기
constructor(
    private val friendRepository: FriendRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 룸에 저장되어 있는 친구 목록
    private val _friendList: MutableLiveData<List<FriendData>?> = MutableLiveData()
    val friendList: LiveData<List<FriendData>?> get() = _friendList

    // ------------------ Friend List live data ------------------
    // 서버에서 최신화된 친구 목록 가져오기
    private val _updatedFriendList: MutableLiveData<DataState<List<FriendData>?>> = MutableLiveData()
    val updatedFriendList: LiveData<DataState<List<FriendData>?>> get() = _updatedFriendList

    // ------------------ Add Friend live data ------------------
    private val _searchFriend: MutableLiveData<DataState<FriendData?>> = MutableLiveData()
    val searchFriend: LiveData<DataState<FriendData?>> get() = _searchFriend

    private val _addFriendList: MutableLiveData<DataState<Boolean?>> = MutableLiveData()
    val addFriend: LiveData<DataState<Boolean?>> get() = _addFriendList

    fun setStateEvent(friendStateEvent: FriendStateEvent) {
        viewModelScope.launch {
            when (friendStateEvent) {

                // ------------------ Friend List ------------------
                is FriendStateEvent.GetFriendListFromLocal -> {
                    friendRepository.getFriendListFromLocal().onEach { dataState ->
                        _friendList.value = dataState
                    }.launchIn(viewModelScope)
                }

                is FriendStateEvent.GetFriendListFromServer -> {
                    friendRepository.getFriendListFromServer(friendStateEvent.userId).onEach { dataState ->
                        _updatedFriendList.value = dataState

                        // 서버에서 데이터를 가져오면 친구목록 live data 업데이트
                        if (dataState is DataState.Success) _friendList.value = dataState.data
                    }.launchIn(viewModelScope)
                }

                // ------------------ Add Friend ------------------
                is FriendStateEvent.SearchFriend -> {
                    friendRepository.searchFriend(friendStateEvent.userId, friendStateEvent.friendEmail).onEach { dataState ->
                        _searchFriend.value = dataState
                        _searchFriend.value = null
                    }.launchIn(viewModelScope)
                }

                is FriendStateEvent.AddFriend -> {
                    friendRepository.addFriend(friendStateEvent.userId, friendStateEvent.friendUserData).onEach { dataState ->
                        _addFriendList.value = dataState
                        _addFriendList.value = null
                    }.launchIn(viewModelScope)
                }
            }
        }
    }
}