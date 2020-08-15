package org.personal.videotogether

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main_home.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.viewmodel.*

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels()
    private val friendViewModel: FriendViewModel by viewModels()
    private val youtubeViewModel: YoutubeViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private val socketViewModel: SocketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        socketViewModel.setStateEvent(SocketStateEvent.ConnectToTCPServer)
    }

    @SuppressLint("RestrictedApi")
    override fun onBackPressed() {
        if (mainFragmentContainer.homeFragmentContainer != null) {
            val homeNavController = Navigation.findNavController(mainFragmentContainer.homeFragmentContainer)

            if (homeNavController.backStack.count() > 2) {
                homeNavController.popBackStack()
            } else {
                killProcess()
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun killProcess() {
        moveTaskToBack(true)
        finishAndRemoveTask()
        // TODO : 뒤로가기 버튼으로 액티비티 스택을 지우면 SocketViewModel onCleared 에서 tcp disconnect 가 호출 되지 않음 -> 해결방안 찾기
        socketViewModel.setStateEvent(SocketStateEvent.DisconnectFromTCPServer)
    }
}