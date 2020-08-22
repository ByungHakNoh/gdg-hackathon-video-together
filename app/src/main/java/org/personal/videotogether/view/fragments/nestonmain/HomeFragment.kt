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

    private val argument: HomeFragmentArgs by navArgs() // 유투브 같이보기 관련 arguments

    private lateinit var homeNavController: NavController

    private val friendViewModel: FriendViewModel by lazy { ViewModelProvider(requireActivity())[FriendViewModel::class.java] }
    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val chatViewModel by lazy { ViewModelProvider(requireActivity())[ChatViewModel::class.java] }
    private val youtubeViewModel: YoutubeViewModel by lazy { ViewModelProvider(requireActivity())[YoutubeViewModel::class.java] }
    private val socketViewModel by lazy { ViewModelProvider(requireActivity())[SocketViewModel::class.java] }

    private lateinit var broadcastReceiver: BroadcastReceiver
    private val intentFilter by lazy { IntentFilter().apply { addAction(RECEIVE_VIDEO_TOGETHER_INVITATION) } }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setNavigation()
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

    private fun setNavigation() {
        val homeFragmentContainer = childFragmentManager.findFragmentById(R.id.homeFragmentContainer)
        homeNavController = homeFragmentContainer!!.findNavController()

        bottomNavBN.setupWithNavController(homeNavController) // 바텀 네비게이션 설정
    }

    private fun subscribeObservers() {
        // 사용자 정보를 room 으로부터 가져옴
        // 유저 정보를 이용해 친구 데이터 업데이트, 채팅 소켓 등록을 함
        userViewModel.userData.observe(viewLifecycleOwner, Observer { userData ->
            // TODO : 친구 데이터 로컬에서 가져오기로 바꾸기
            friendViewModel.setStateEvent(FriendStateEvent.GetFriendListFromServer(userData!!.id))
            // TODO : 채팅방 데이터 로컬에서 가져오기로 바꾸기
            chatViewModel.setStateEvent(ChatStateEvent.GetChatRoomsFromServer(userData.id))
            // 유저 Id를 로컬에서 가져오면 -> 서버의 소켓 클라이언트 정보 업데이트 + 서버에서 데이터 수신
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

    // 노티피케이션으로 같이보기 초대받아서 들어올 때 : 소켓 연결이 완료되면 비디오 같이보기 설정 확인
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

    // 파이어베이스에서 보내주는 데이터 받는 로컬 브로드캐스트 리시버 등록
    private fun defineReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    RECEIVE_VIDEO_TOGETHER_INVITATION -> {
                        val roomId = intent.getIntExtra("roomId", 0)
                        val inviterName = intent.getStringExtra("inviterName")
                        val youtubeData = intent.getParcelableExtra<YoutubeData>("youtubeData")

                        val invitationDialog = InvitationDialog()
                        val bundle = Bundle().apply {
                            putInt("roomId", roomId)
                            putString("inviterName", inviterName)
                            putParcelable("youtubeData", youtubeData)
                        }
                        invitationDialog.arguments = bundle
                        invitationDialog.show(childFragmentManager, "invitationDialog")
                    }
                }
            }
        }
    }

    // ------------------ 초대 다이얼로그 리스너 ------------------
    override fun onConfirm(roomId: Int, inviterName: String, youtubeData: YoutubeData) {
        youtubeViewModel.setStateEvent(YoutubeStateEvent.SetVideoTogether(true))
        youtubeViewModel.setStateEvent(YoutubeStateEvent.SetJoiningVideoTogether(true))
        youtubeViewModel.setStateEvent(YoutubeStateEvent.SetFrontPlayer(youtubeData))
        socketViewModel.setStateEvent(SocketStateEvent.SendToTCPServer(JOIN_YOUTUBE_ROOM, roomId.toString()))

        when (homeNavController.currentDestination?.id) {
            R.id.friendsListFragment -> homeNavController.navigate(R.id.action_friendsListFragment_to_youtubeFragment2)
            R.id.chatListFragment -> homeNavController.navigate(R.id.action_chatListFragment_to_youtubeFragment)
            R.id.youtubeSearchFragment -> requireActivity().onBackPressed()
            else -> Log.i(TAG, "onConfirm: ${homeNavController.currentDestination}")
        }
    }
}