package org.personal.videotogether.view.fragments.nestonmain

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.viewmodel.*

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val TAG = javaClass.name

    private lateinit var mainNavController: NavController
    private lateinit var homeNavController: NavController
    private lateinit var homeDetailNavController: NavController

    private val friendViewModel: FriendViewModel by lazy { ViewModelProvider(requireActivity())[FriendViewModel::class.java] }
    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val chatViewModel by lazy { ViewModelProvider(requireActivity())[ChatViewModel::class.java] }
    private val youtubeViewModel: YoutubeViewModel by lazy { ViewModelProvider(requireActivity())[YoutubeViewModel::class.java] }
    private val socketViewModel by lazy { ViewModelProvider(requireActivity())[SocketViewModel::class.java] }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setNavigation(view)
        setBackPressCallback()
        subscribeObservers()
        userViewModel.setStateEvent(UserStateEvent.GetUserDataFromLocal)
    }

    private fun setNavigation(view: View) {
        val homeFragmentContainer = childFragmentManager.findFragmentById(R.id.homeFragmentContainer)
        val homeDetailFragment = childFragmentManager.findFragmentById(R.id.homeDetailFragmentContainer)

        mainNavController = Navigation.findNavController(view)
        homeNavController = homeFragmentContainer!!.findNavController()
        homeDetailNavController = homeDetailFragment!!.findNavController()

        bottomNavBN.setupWithNavController(homeNavController) // 바텀 네비게이션 설정
    }

    @SuppressLint("RestrictedApi")
    private fun setBackPressCallback() {
        // 비디오 모션 레이아웃 관련 뒤로가기 버튼은 VideoPlayFragment 에서 관리
        // VideoPlayFragment 뒤로가기 콜백이 disable 되면 실행됨
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            when {
                homeDetailNavController.backStack.count() > 2 -> homeDetailNavController.popBackStack()
                homeNavController.backStack.count() > 2 -> homeNavController.popBackStack()
                else -> {
                    remove()
                    killProcess()
                }
            }
        }
    }

    private fun subscribeObservers() {
        // 사용자 정보를 room 으로부터 가져옴
        // 유저 정보를 이용해 친구 데이터 업데이트, 채팅 소켓 등록을 함
        userViewModel.userData.observe(viewLifecycleOwner, Observer { userData ->
            friendViewModel.setStateEvent(FriendStateEvent.GetFriendListFromServer(userData!!.id))
            socketViewModel.setStateEvent(SocketStateEvent.RegisterSocket(userData))
            socketViewModel.setStateEvent(SocketStateEvent.ReceiveFromTCPServer)
            Log.i(TAG, "subscribeObservers: 한번?")
        })

        // 유투브 재생 시 하단 유튜브 플레이어 보여주기
        youtubeViewModel.currentPlayedYoutube.observe(viewLifecycleOwner, Observer { youtubeData ->
            if (youtubeData == null) videoFragmentContainer.visibility = View.GONE
            else videoFragmentContainer.visibility = View.VISIBLE
        })
    }

    private fun killProcess() {
        requireActivity().moveTaskToBack(true)
        requireActivity().finishAndRemoveTask()
        // TODO : 뒤로가기 버튼으로 액티비티 스택을 지우면 SocketViewModel onCleared 에서 tcp disconnect 가 호출 되지 않음 -> 해결방안 찾기
        socketViewModel.setStateEvent(SocketStateEvent.DisconnectFromTCPServer)
    }


    // ------------------ 상단 앱 바 아이템 클릭 리스너 모음 ------------------
    @SuppressLint("RestrictedApi")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, "onOptionsItemSelected: ${homeNavController.getBackStackEntry(R.id.friendsListFragment)}")
        when (item.itemId) {
            R.id.youtubeSearchFragment -> {
                homeNavController.navigate(R.id.action_youtubeFragment_to_youtubeSearchFragment)
            }
            else -> {
                NavigationUI.onNavDestinationSelected(item, mainNavController)
            }
        }

        return super.onOptionsItemSelected(item)
    }
}