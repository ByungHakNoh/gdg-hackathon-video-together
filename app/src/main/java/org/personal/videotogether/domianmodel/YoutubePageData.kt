package org.personal.videotogether.domianmodel

data class YoutubePageData(
    val nextPageUrl :String,
    val nextPageToken : String,
    val youtubeDataList:List<YoutubeData>
)