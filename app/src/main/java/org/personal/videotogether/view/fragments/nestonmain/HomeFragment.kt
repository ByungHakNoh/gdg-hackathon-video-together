package org.personal.videotogether.view.fragments.nestonmain

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navDeepLink
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.YoutubeData
import org.personal.videotogether.repository.SocketRepository.Companion.JOIN_YOUTUBE_ROOM
import org.personal.videotogether.service.MyFirebaseMessagingService.Companion.RECEIVE_VIDEO_TOGETHER_INVITATION
import org.personal.videotogether.view.dialog.InvitationDialog
import org.personal.videotogether.view.fragments.home.nestonhome.FriendsListFragmentDirections
import org.personal.videotogether.viewmodel.*

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), InvitationDialog.DialogListener {

    private val TAG = javaClass.name

    private val argument: HomeFragmentArgs by navArgs()

    private lateinit var mainNavController: NavController
    private lateinit var homeNavController: NavController
    private lateinit var homeDetailNavController: NavController

    private val friendViewModel: FriendViewModel by lazy { ViewModelProvider(requireActivity())[FriendViewModel::class.java] }
    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val chatViewModel by lazy { ViewModelProvider(requireActivity())[ChatViewModel::class.java] }
    private val youtubeViewModel: YoutubeViewModel by lazy { ViewModelProvider(requireActivity())[YoutubeViewModel::class.java] }
    private val socketViewModel by lazy { ViewModelProvider(requireActivity())[SocketViewModel::class.java] }

    private lateinit var broadcastReceiver: BroadcastReceiver
    private val intentFilter by lazy { IntentFilter().apply { addAction(RECEIVE_VIDEO_TOGETHER_INVITATION) } }
    private val invitationDialog by lazy { InvitationDialog() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setNavigation(view)
        setBackPressCallback()
        subscribeObservers()
        defineReceiver()
        userViewModel.setStateEvent(UserStateEvent.GetUserDataFromLocal)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, intentFilter)
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

    private fun killProcess() {
        requireActivity().moveTaskToBack(true)
        requireActivity().finishAndRemoveTask()
        // TODO : 뒤로가기 버튼으로 액티비티 스택을 지우면 SocketViewModel onCleared 에서 tcp disconnect 가 호출 되지 않음 -> 해결방안 찾기
        socketViewModel.setStateEvent(SocketStateEvent.DisconnectFromTCPServer)
    }

    private fun subscribeObservers() {
        // 사용자 정보를 room 으로부터 가져옴
        // 유저 정보를 이용해 친구 데이터 업데이트, 채팅 소켓 등록을 함
        userViewModel.userData.observe(viewLifecycleOwner, Observer { userData ->
            friendViewModel.setStateEvent(FriendStateEvent.GetFriendListFromServer(userData!!.id))
            chatViewModel.setStateEvent(ChatStateEvent.GetChatRoomsFromServer(userData.id))
            socketViewModel.setStateEvent(SocketStateEvent.RegisterSocket(userData))
            socketViewModel.setStateEvent(SocketStateEvent.ReceiveFromTCPServer)
            checkVideoTogether()
        })

        // 유투브 재생 시 하단 유튜브 플레이어 보여주기
        youtubeViewModel.currentPlayedYoutube.observe(viewLifecycleOwner, Observer { youtubeData ->
            if (youtubeData == null) videoFragmentContainer.visibility = View.GONE
            else videoFragmentContainer.visibility = View.VISIBLE
        })
    }

    private fun checkVideoTogether() {
        val youtubeData = argument.youtubeData
        val roomId = argument.roomId

        if (youtubeData != null) {
            youtubeViewModel.setStateEvent(YoutubeStateEvent.SetVideoTogether(true))
            youtubeViewModel.setStateEvent(YoutubeStateEvent.SetJoiningVideoTogether(true))
            youtubeViewModel.setStateEvent(YoutubeStateEvent.SetFrontPlayer(youtubeData))
            socketViewModel.setStateEvent(SocketStateEvent.SendToTCPServer(JOIN_YOUTUBE_ROOM, roomId.toString()))

            homeNavController.navigate(R.id.action_friendsListFragment_to_youtubeFragment2)
        }
    }

    private fun defineReceiver() {

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.i(TAG, "onReceive: 되나 ? $intent")
                Log.i(TAG, "onReceive: 되나 ? ${intent?.getStringExtra("inviterName")}")
                Log.i(TAG, "onReceive: 되나 ? ${intent?.getIntExtra("roomId", 0)}")
                when (intent?.action) {
                    RECEIVE_VIDEO_TOGETHER_INVITATION -> {
                        val roomId = intent.getIntExtra("roomId", 0)
                        val inviterName = intent.getStringExtra("inviterName")
                        val youtubeData = intent.getParcelableExtra<YoutubeData>("youtubeData")

                        val bundle = Bundle().apply {
                            putInt("roomId", roomId)
                            putString("inviterName", inviterName)
                            putParcelable("youtubeData", youtubeData)
                        }
                        invitationDialog.arguments = bundle

                        if (!invitationDialog.isAdded) {

                            invitationDialog.show(childFragmentManager, "invitationDialog")
                        }
                    }
                }
            }
        }
    }

    override fun onConfirm(roomId: Int, inviterName: String, youtubeData: YoutubeData) {
        youtubeViewModel.setStateEvent(YoutubeStateEvent.SetVideoTogether(true))
        youtubeViewModel.setStateEvent(YoutubeStateEvent.SetJoiningVideoTogether(true))
        youtubeViewModel.setStateEvent(YoutubeStateEvent.SetFrontPlayer(youtubeData))
        socketViewModel.setStateEvent(SocketStateEvent.SendToTCPServer(JOIN_YOUTUBE_ROOM, roomId.toString()))

        Log.i(TAG, "onConfirm: ${homeNavController.currentDestination?.id}")
        when (homeNavController.currentDestination?.id) {
            R.id.friendsListFragment -> homeNavController.navigate(R.id.action_friendsListFragment_to_youtubeFragment2)
            R.id.chatListFragment -> homeNavController.navigate(R.id.action_chatListFragment_to_youtubeFragment)
            R.id.youtubeSearchFragment -> requireActivity().onBackPressed()
            else -> Log.i(TAG, "onConfirm: ${homeNavController.currentDestination}")
        }
    }
}