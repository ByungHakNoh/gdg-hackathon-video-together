package org.personal.videotogether.viewmodel

sealed class YoutubeStateEvent {
    class GetDefaultYoutubeVideos(val youtubeChannel: String) : YoutubeStateEvent()
    class GetSearchedYoutubeVideos(val youtubeChannel: String) : YoutubeStateEvent()
}