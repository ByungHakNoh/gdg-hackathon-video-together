package org.personal.videotogether.domianmodel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InviteYoutubeData (
    val roomId: Int,
    val youtubeData: YoutubeData
) : Parcelable