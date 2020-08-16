package org.personal.videotogether

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.view.fragments.nestonmain.HomeFragment
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
}