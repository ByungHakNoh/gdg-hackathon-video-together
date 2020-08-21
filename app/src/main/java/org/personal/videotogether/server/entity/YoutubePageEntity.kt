package org.personal.videotogether.server.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class YoutubePageEntity(

    @SerializedName("next_page_url")
    @Expose
    val nextPageUrl: String,

    @SerializedName("next_page_token")
    @Expose
    val nextPageToken : String,

    @SerializedName("youtube_data_list")
    @Expose
    val youtubeDataList: List<YoutubeEntity>
)