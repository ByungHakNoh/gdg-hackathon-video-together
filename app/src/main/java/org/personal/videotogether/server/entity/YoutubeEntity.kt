package org.personal.videotogether.server.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class YoutubeEntity(
    @SerializedName("channel_title")
    @Expose
    val channelTitle: String,

    @SerializedName("channel_thumbnail")
    @Expose
    val channelThumbnail: String,

    @SerializedName("title")
    @Expose
    val title : String,

    @SerializedName("video_id")
    @Expose
    val videoId : String
)