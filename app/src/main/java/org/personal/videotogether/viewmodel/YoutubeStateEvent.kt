package org.personal.videotogether.viewmodel

import org.personal.videotogether.domianmodel.InviteYoutubeData
import org.personal.videotogether.domianmodel.UserData
import org.personal.videotogether.domianmodel.YoutubeData

sealed class YoutubeStateEvent {
    data class GetYoutubeDefaultPage(val youtubeChannel: String) : YoutubeStateEvent()
    data class GetYoutubeSearchedPage(val youtubeChannel: String) : YoutubeStateEvent()
    data class GetNextYoutubeDefaultPage(val nextPageUrl: String, val nextPageToken:String, val channelTitle: String, val channelThumbnail: String) : YoutubeStateEvent()
    data class GetNextYoutubeSearchedPage(val nextPageUrl: String, val nextPageToken:String, val channelTitle: String, val channelThumbnail: String) : YoutubeStateEvent()
    data class SetFrontPlayer(val youtubeData: YoutubeData?) : YoutubeStateEvent()
    data class SetVideoTogether(val isVideoTogetherOn: Boolean?) : YoutubeStateEvent()
    data class SetJoiningVideoTogether(val isJoining: Boolean) : YoutubeStateEvent()
    data class InviteVideoTogether(val inviterData: UserData, val friendIds: List<Int>, val youtubeData: YoutubeData) : YoutubeStateEvent()
    object SignOut: YoutubeStateEvent()
    data class OnNotification(val inviteYoutubeData: InviteYoutubeData) : YoutubeStateEvent()
}