package org.personal.videotogether.viewmodel

import org.personal.videotogether.domianmodel.YoutubeData

sealed class YoutubeStateEvent {
    data class GetDefaultYoutubeVideos(val youtubeChannel: String) : YoutubeStateEvent()
    data class GetSearchedYoutubeVideos(val youtubeChannel: String) : YoutubeStateEvent()
    data class SetFrontPlayer(val youtubeData: YoutubeData?) : YoutubeStateEvent()
}