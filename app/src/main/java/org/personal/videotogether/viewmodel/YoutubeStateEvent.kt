package org.personal.videotogether.viewmodel

import org.personal.videotogether.domianmodel.UserData
import org.personal.videotogether.domianmodel.YoutubeData

sealed class YoutubeStateEvent {
    data class GetDefaultYoutubeVideos(val youtubeChannel: String) : YoutubeStateEvent()
    data class GetSearchedYoutubeVideos(val youtubeChannel: String) : YoutubeStateEvent()
    data class SetFrontPlayer(val youtubeData: YoutubeData?) : YoutubeStateEvent()
    data class SetVideoTogether(val isVideoTogetherOn: Boolean) : YoutubeStateEvent()
    data class SetJoiningVideoTogether(val isJoining: Boolean) : YoutubeStateEvent()
    data class InviteVideoTogether(val inviterData: UserData, val friendIds: List<Int>, val youtubeData: YoutubeData) : YoutubeStateEvent()
}