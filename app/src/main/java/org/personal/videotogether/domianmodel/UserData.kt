package org.personal.videotogether.domianmodel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserData(
    val id : Int,
    val email: String,
    val password: String,
    val name : String?,
    val profileImageUrl: String?
) : Parcelable
