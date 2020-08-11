package org.personal.videotogether.model.server.entity

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UserEntity(
    @SerializedName("id")
    @Expose
    val id: Int,

    @SerializedName("email")
    @Expose
    val email: String,

    @SerializedName("password")
    @Expose
    val password: String,

    @SerializedName("name")
    @Expose
    val name: String?,

    @SerializedName("profile_image_url")
    @Expose
    val profile_image_url: String?
)