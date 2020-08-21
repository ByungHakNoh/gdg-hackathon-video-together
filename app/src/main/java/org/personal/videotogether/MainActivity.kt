package org.personal.videotogether

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.util.SharedPreferenceHelper
import org.personal.videotogether.viewmodel.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    private val userViewModel: UserViewModel by viewModels()
    private val friendViewModel: FriendViewModel by viewModels()
    private val youtubeViewModel: YoutubeViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private val socketViewModel: SocketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        socketViewModel.setStateEvent(SocketStateEvent.ConnectToTCPServer)
        sharedPreferenceHelper.setBoolean(this, getText(R.string.is_app_on).toString(), true)
        Log.i("TAG", "onCreate: ${sharedPreferenceHelper.getBoolean(this, getText(R.string.is_app_on).toString() )}")
    }

    // TODO : 시스템에서 종료할 때 생각하기
    override fun onDestroy() {
        super.onDestroy()
        sharedPreferenceHelper.setBoolean(this, getText(R.string.is_app_on).toString(), false)
        Log.i("TAG", "onCreate: ${sharedPreferenceHelper.getBoolean(this, getText(R.string.is_app_on).toString() )}")
    }
}