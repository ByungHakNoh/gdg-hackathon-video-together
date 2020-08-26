package org.personal.videotogether.viewmodel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.personal.videotogether.domianmodel.InviteYoutubeData
import org.personal.videotogether.domianmodel.YoutubeData
import org.personal.videotogether.domianmodel.YoutubePageData
import org.personal.videotogether.repository.YoutubeRepository
import org.personal.videotogether.util.DataState

@ExperimentalCoroutinesApi
class YoutubeViewModel
@ViewModelInject // 뷰모델을 hilt 를 사용해서 불러오기
constructor(
    private val youtubeRepository: YoutubeRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _youtubeDefaultPage: MutableLiveData<DataState<YoutubePageData?>> = MutableLiveData()
    val youtubeDefaultPage: LiveData<DataState<YoutubePageData?>> get() = _youtubeDefaultPage

    private val _youtubeSearchedPage: MutableLiveData<DataState<YoutubePageData?>> = MutableLiveData()
    val youtubeSearchedPage: LiveData<DataState<YoutubePageData?>> get() = _youtubeSearchedPage

    private val _youtubeNotificationData: MutableLiveData<InviteYoutubeData?> = MutableLiveData()
    val youtubeNotificationData: LiveData<InviteYoutubeData?> get() = _youtubeNotificationData

    // 유투브 재생 데이터
    private val _currentPlayedYoutube: MutableLiveData<YoutubeData?> = MutableLiveData()
    val currentPlayedYoutube: LiveData<YoutubeData?> get() = _currentPlayedYoutube

    // ------------------ Video Together live data ------------------
    // 유투브 같이보기가 설정되어있는지 확인하는 데이터
    private val _setVideoTogether: MutableLiveData<Boolean?> = MutableLiveData()
    val setVideoTogether: LiveData<Boolean?> get() = _setVideoTogether

    // 유투브 같이보기 초대되어 참여중인지 확인하는 데이터
    private val _isJoiningVideoTogether: MutableLiveData<Boolean?> = MutableLiveData()
    val isJoiningVideoTogether: LiveData<Boolean?> get() = _isJoiningVideoTogether

    fun setStateEvent(youtubeStateEvent: YoutubeStateEvent) {
        viewModelScope.launch {
            when (youtubeStateEvent) {
                is YoutubeStateEvent.GetYoutubeDefaultPage -> {
                    withContext(IO) {
                        youtubeRepository.getYoutubePage(youtubeStateEvent.youtubeChannel).onEach { dataState ->
                            _youtubeDefaultPage.value = dataState
                            _youtubeDefaultPage.value = null
                        }.launchIn(viewModelScope)
                    }
                }

                is YoutubeStateEvent.GetNextYoutubeDefaultPage -> {
                    youtubeRepository.getYoutubeNextPage(
                        youtubeStateEvent.nextPageUrl,
                        youtubeStateEvent.nextPageToken,
                        youtubeStateEvent.channelTitle,
                        youtubeStateEvent.channelThumbnail
                    ).onEach { dataState ->
                        _youtubeDefaultPage.value = dataState
                        _youtubeDefaultPage.value = null
                    }.launchIn(viewModelScope)
                }

                is YoutubeStateEvent.GetYoutubeSearchedPage -> {
                    withContext(IO) {
                        youtubeRepository.getYoutubePage(youtubeStateEvent.youtubeChannel).onEach { dataState ->
                            _youtubeSearchedPage.value = dataState
                            _youtubeSearchedPage.value = null
                        }.launchIn(viewModelScope)
                    }
                }

                is YoutubeStateEvent.GetNextYoutubeSearchedPage -> {
                    youtubeRepository.getYoutubeNextPage(
                        youtubeStateEvent.nextPageUrl,
                        youtubeStateEvent.nextPageToken,
                        youtubeStateEvent.channelTitle,
                        youtubeStateEvent.channelThumbnail
                    ).onEach { dataState ->
                        _youtubeSearchedPage.value = dataState
                        _youtubeSearchedPage.value = null
                    }.launchIn(viewModelScope)
                }


                is YoutubeStateEvent.SetFrontPlayer -> {
                    _currentPlayedYoutube.value = youtubeStateEvent.youtubeData
                }

                // 상대방을 초대한 후 유투브 같이보기 실행
                is YoutubeStateEvent.InviteVideoTogether -> {
                    youtubeRepository.inviteVideoTogether(youtubeStateEvent.inviterData, youtubeStateEvent.friendIds, youtubeStateEvent.youtubeData).onEach { dataState ->

                        if (dataState is DataState.Success) setStateEvent(YoutubeStateEvent.SetVideoTogether(true))
                    }.launchIn(viewModelScope)
                }

                is YoutubeStateEvent.SetVideoTogether -> {
                    _setVideoTogether.value = youtubeStateEvent.isVideoTogetherOn
                }

                is YoutubeStateEvent.SetJoiningVideoTogether -> {
                    _isJoiningVideoTogether.value = youtubeStateEvent.isJoining
                }

                // ------------------ Sign Out ------------------
                is YoutubeStateEvent.SignOut -> {
                    _setVideoTogether.value = null
                    _isJoiningVideoTogether.value = null
                    _currentPlayedYoutube.value = null
                }

                // ------------------ 채팅 노티피케이션 관련 ------------------
                is YoutubeStateEvent.OnNotification -> {
                    _youtubeNotificationData.value = youtubeStateEvent.inviteYoutubeData
                    _youtubeNotificationData.value = null
                }
            }
        }
    }
}