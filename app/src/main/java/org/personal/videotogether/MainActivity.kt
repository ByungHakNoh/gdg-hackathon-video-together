package org.personal.videotogether

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main_home.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.viewmodel.ChatViewModel
import org.personal.videotogether.viewmodel.FriendViewModel
import org.personal.videotogether.viewmodel.UserViewModel
import org.personal.videotogether.viewmodel.YoutubeViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels()
    private val friendViewModel: FriendViewModel by viewModels()
    private val youtubeViewModel: YoutubeViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    @SuppressLint("RestrictedApi")
    override fun onBackPressed() {
        if (mainFragmentContainer.homeFragmentContainer != null) {
            val homeNavController = Navigation.findNavController(mainFragmentContainer.homeFragmentContainer)

            if (homeNavController.backStack.count() > 2) {
                homeNavController.popBackStack()
            } else {
                moveTaskToBack(true)
                finishAndRemoveTask()
            }
        } else {
            super.onBackPressed()
        }
    }
}