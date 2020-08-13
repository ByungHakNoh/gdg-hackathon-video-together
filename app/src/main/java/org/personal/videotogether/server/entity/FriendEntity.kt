package org.personal.videotogether.server.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class FriendEntity(
    @SerializedName("friend_id")
    @Expose
    val id: Int,

    @SerializedName("email")
    @Expose
    val email: String,

    @SerializedName("name")
    @Expose
    val name: String?,

    @SerializedName("profile_image_url")
    @Expose
    val profile_image_url: String?
)