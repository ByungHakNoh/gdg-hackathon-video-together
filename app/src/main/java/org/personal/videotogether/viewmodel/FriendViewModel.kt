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

    private val _friendList: MutableLiveData<List<FriendData>?> = MutableLiveData()
    val friendList: LiveData<List<FriendData>?> get() = _friendList

    // ------------------ Add Friend live data ------------------
    private val _searchFriend: MutableLiveData<DataState<FriendData?>> = MutableLiveData()
    val searchFriend: LiveData<DataState<FriendData?>> get() = _searchFriend

    private val _addFriendList: MutableLiveData<DataState<List<FriendData>?>> = MutableLiveData()
    val addFriendList: LiveData<DataState<List<FriendData>?>> get() = _addFriendList

    fun setStateEvent(friendStateEvent: FriendStateEvent) {
        viewModelScope.launch {
            when (friendStateEvent) {

                is FriendStateEvent.GetFriendListFromLocal -> {
                    friendRepository.getFriendListFromLocal().onEach { dataState ->
                        _friendList.value = dataState
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