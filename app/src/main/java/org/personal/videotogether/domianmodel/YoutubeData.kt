package org.personal.videotogether.domianmodel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class YoutubeData (
    val channelTitle: String,
    val channelThumbnail: String,
    val title : String,
    val videoId : String
) : Parcelable