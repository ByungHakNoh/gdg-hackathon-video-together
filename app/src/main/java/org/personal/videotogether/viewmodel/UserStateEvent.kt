package org.personal.videotogether.viewmodel

sealed class UserStateEvent {
    class CheckDuplicatedEmail(val email: String) : UserStateEvent()
    class UploadUser(val email: String, val password: String) : UserStateEvent()
    class UploadUserProfile(val base64Image: String, val name: String) : UserStateEvent()
    class SignIn(val email: String, val password: String) : UserStateEvent()
    object None : UserStateEvent()
}