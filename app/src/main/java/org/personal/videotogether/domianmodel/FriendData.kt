package org.personal.videotogether.domianmodel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FriendData (
    val id : Int,
    val email: String,
    val name : String?,
    val profileImageUrl: String?,
    var isSelected: Boolean?
) : Parcelable