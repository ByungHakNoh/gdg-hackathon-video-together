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
import org.personal.videotogether.domianmodel.YoutubeData
import org.personal.videotogether.repository.YoutubeRepository
import org.personal.videotogether.util.DataState

@ExperimentalCoroutinesApi
class YoutubeViewModel
@ViewModelInject // 뷰모델을 hilt 를 사용해서 불러오기
constructor(
    private val youtubeRepository: YoutubeRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _youtubeList: MutableLiveData<DataState<List<YoutubeData>?>> = MutableLiveData()
    val youtubeList: LiveData<DataState<List<YoutubeData>?>> get() = _youtubeList

    private val _currentPlayedYoutube: MutableLiveData<YoutubeData?> = MutableLiveData()
    val currentPlayedYoutube: LiveData<YoutubeData?> get() = _currentPlayedYoutube

    fun setStateEvent(youtubeStateEvent: YoutubeStateEvent) {
        viewModelScope.launch {
            when (youtubeStateEvent) {
                is YoutubeStateEvent.GetDefaultYoutubeVideos -> {
                    withContext(IO) {
                        youtubeRepository.getDefaultYoutubeList(youtubeStateEvent.youtubeChannel).onEach { dataState ->
                            _youtubeList.value = dataState
                        }.launchIn(viewModelScope)
                    }
                }

                is YoutubeStateEvent.SetFrontPlayer -> {
                    _currentPlayedYoutube.value = youtubeStateEvent.youtubeData
                }
            }
        }
    }
}