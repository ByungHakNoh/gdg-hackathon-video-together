package org.personal.videotogether.viewmodel

sealed class UserStateEvent {
    object GetUserDataFromLocal : UserStateEvent()
    data class CheckDuplicatedEmail(val email: String) : UserStateEvent()
    data class UploadUser(val email: String, val password: String) : UserStateEvent()
    data class UploadUserProfile(val base64Image: String, val name: String) : UserStateEvent()
    data class UpdateProfile(val userId: Int, val base64Image: String?, val name: String) : UserStateEvent()
    data class SignIn(val email: String, val password: String) : UserStateEvent()
    object SignOut : UserStateEvent()
    data class UploadFirebaseToken(val userId : Int, val firebaseToken: String) : UserStateEvent()
}