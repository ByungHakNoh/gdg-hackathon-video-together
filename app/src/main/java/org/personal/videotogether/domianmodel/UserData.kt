package org.personal.videotogether.model

data class UserData(
    val id : Int,
    val email: String,
    val password: String,
    val name : String?,
    val profileImageUrl: String?
)
