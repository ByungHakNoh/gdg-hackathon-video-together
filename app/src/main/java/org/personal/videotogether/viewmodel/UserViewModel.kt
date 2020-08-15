package org.personal.videotogether.viewmodel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.personal.videotogether.domianmodel.UserData
import org.personal.videotogether.repository.UserRepository
import org.personal.videotogether.util.DataState

@ExperimentalCoroutinesApi
class UserViewModel
@ViewModelInject // 뷰모델을 hilt 를 사용해서 불러오기
constructor(
    private val userRepository: UserRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _userData: MutableLiveData<UserData?> = MutableLiveData()
    val userData: LiveData<UserData?> get() = _userData

    // ------------------ Sign Up live data ------------------
    private val _validationDataState: MutableLiveData<DataState<Boolean?>> = MutableLiveData()
    val validationDataState: LiveData<DataState<Boolean?>> get() = _validationDataState

    private val _uploadUserDataState: MutableLiveData<DataState<Boolean?>> = MutableLiveData()
    val uploadUserDataState: LiveData<DataState<Boolean?>> get() = _uploadUserDataState

    // ------------------ Set Profile live data ------------------
    private val _uploadUserProfileState: MutableLiveData<DataState<Boolean?>> = MutableLiveData()
    val uploadUserProfileState: LiveData<DataState<Boolean?>> get() = _uploadUserProfileState

    // ------------------ Sign In live data ------------------
    private val _signInState: MutableLiveData<DataState<UserData?>> = MutableLiveData()
    val signInState: LiveData<DataState<UserData?>> get() = _signInState

    fun setStateEvent(userStateEvent: UserStateEvent) {
        viewModelScope.launch(IO) {
            when (userStateEvent) {
                is UserStateEvent.GetUserDataFromLocal -> {
                    userRepository.getUserDataFromLocal().onEach { dataState ->
                        _userData.value = dataState
                    }.launchIn(viewModelScope)
                }

                // ------------------ Sign Up ------------------
                is UserStateEvent.CheckDuplicatedEmail -> {
                    userRepository.postCheckEmailValid(userStateEvent.email).onEach { dataState ->
                        _validationDataState.value = dataState
                        _validationDataState.value = null
                    }.launchIn(viewModelScope)
                }

                is UserStateEvent.UploadUser -> {
                    userRepository.uploadUser(userStateEvent.email, userStateEvent.password).onEach { dataState ->
                        _uploadUserDataState.value = dataState
                        _uploadUserDataState.value = null
                    }.launchIn(viewModelScope)
                }

                // ------------------ Set Profile ------------------
                is UserStateEvent.UploadUserProfile -> {
                    userRepository.uploadUserProfile(userStateEvent.base64Image, userStateEvent.name).onEach { dataState ->
                        _uploadUserProfileState.value = dataState
                        _uploadUserProfileState.value = null
                    }.launchIn(viewModelScope)
                }

                // ------------------ Sign In ------------------
                is UserStateEvent.SignIn -> {
                    userRepository.signIn(userStateEvent.email, userStateEvent.password).onEach { dataState ->
                        _signInState.value = dataState
                        _signInState.value = null
                    }.launchIn(viewModelScope)
                }
            }
        }
    }
}