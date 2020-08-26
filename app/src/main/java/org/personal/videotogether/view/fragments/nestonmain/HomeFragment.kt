package org.personal.videotogether.view.fragments.nestonmain

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.domianmodel.InviteYoutubeData
import org.personal.videotogether.domianmodel.YoutubeData
import org.personal.videotogether.repository.SocketRepository.Companion.JOIN_YOUTUBE_ROOM
import org.personal.videotogether.service.MyFirebaseMessagingService.Companion.RECEIVE_VIDEO_TOGETHER_INVITATION
import org.personal.videotogether.util.SharedPreferenceHelper
import org.personal.videotogether.view.dialog.AlertDialog
import org.personal.videotogether.view.dialog.InvitationDialog
import org.personal.videotogether.view.fragments.home.nestonhomedetail.HomeDetailBlankFragmentDirections
import org.personal.videotogether.viewmodel.*

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper
) : Fragment(R.layout.fragment_home), InvitationDialog.DialogListener, AlertDialog.DialogListener,NavController.OnDestinationChangedListener {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // 상단 앱 바 사용
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setNavigation(view)
        setBackPressCallback()
        subscribeObservers()
        setListener()
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
        // 네비게이션 설정
        val homeFragmentContainer = childFragmentManager.findFragmentById(R.id.homeFragmentContainer)
        val homeDetailFragment = childFragmentManager.findFragmentById(R.id.homeDetailFragmentContainer)

        mainNavController = Navigation.findNavController(view)
        homeNavController = homeFragmentContainer!!.findNavController()
        homeDetailNavController = homeDetailFragment!!.findNavController()

        bottomNavBN.setupWithNavController(homeNavController) // 바텀 네비게이션 설정
        // 상단 앱바 설정
        (requireActivity() as AppCompatActivity).setSupportActionBar(homeToolbarTB)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.friendsListFragment, R.id.chatListFragment, R.id.youtubeFragment, R.id.youtubeSearchFragment))
        NavigationUI.setupWithNavController(homeToolbarTB, homeNavController, appBarConfiguration)
    }

    // 비디오 모션 레이아웃 관련 뒤로가기 버튼은 VideoPlayFragment 에서 관리
    // VideoPlayFragment 뒤로가기 콜백이 disable 되면 실행됨
    @SuppressLint("RestrictedApi")
    private fun setBackPressCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            when {
                homeDetailNavController.backStack.count() > 2 -> homeDetailNavController.popBackStack()
                homeNavController.backStack.count() > 2 -> homeNavController.popBackStack()
                else -> {
                    socketViewModel.setStateEvent(SocketStateEvent.DisconnectFromTCPServer)
                    remove()
                    requireActivity().finish()
                }
            }
        }
    }

    private fun subscribeObservers() {
        // 사용자 정보를 room 으로부터 가져옴
        // 유저 정보를 이용해 친구 데이터 업데이트, 채팅 소켓 등록을 함
        userViewModel.userData.observe(viewLifecycleOwner, Observer { userData ->
            if (userData != null) {
                Log.i(TAG, "friendList:  = user -  ${userViewModel.userData.value}")
                friendViewModel.setStateEvent(FriendStateEvent.GetFriendListFromLocal)
                chatViewModel.setStateEvent(ChatStateEvent.GetChatRoomsFromLocal)
                // 유저 Id를 로컬에서 가져오면 -> 서버의 소켓 클라이언트 정보 업데이트 + 서버에서
                socketViewModel.setStateEvent(SocketStateEvent.RegisterSocket(userData))
                socketViewModel.setStateEvent(SocketStateEvent.ReceiveFromTCPServer)
                checkNotificationIntent()
            }
        })

        // 유투브 재생 시 하단 유튜브 플레이어 보여주기
        youtubeViewModel.currentPlayedYoutube.observe(viewLifecycleOwner, Observer { youtubeData ->
            if (youtubeData == null) videoFragmentContainer.visibility = View.GONE
            else videoFragmentContainer.visibility = View.VISIBLE
        })

        chatViewModel.chatNotificationData.observe(viewLifecycleOwner, Observer { chatRoomData ->
            if (chatRoomData != null) {
                val action = HomeDetailBlankFragmentDirections.actionHomeDetailBlankFragmentToChattingFragment(chatRoomData)
                homeDetailNavController.navigate(action)
            }
        })

        youtubeViewModel.youtubeNotificationData.observe(viewLifecycleOwner, Observer { inviteYoutubeData ->
            if (inviteYoutubeData != null) {
                notificationToVideoTogether(inviteYoutubeData)
            }
        })
    }

    // 비디오 같이보기 arguments 가 있는지 확인
    // arguments 가 존재하면 해당 유투브 재생 및 유투브 같이보기 방 참여
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

    private fun setListener() {
        homeNavController.addOnDestinationChangedListener(this)
    }

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
                        if (isAdded) {
                            Log.i(TAG, "onReceive: isAdded")
                            invitationDialog.show(childFragmentManager, "invitationDialog")
                        } else {
                            Log.i(TAG, "onReceive: isNotAdded")
                        }
                    }
                }
            }
        }
    }

    private fun checkNotificationIntent() {
        if (requireActivity().intent.hasExtra("request")) {
            when(requireActivity().intent.getStringExtra("request")) {
                "chat"-> {
                    val chatRoomData = requireActivity().intent.getParcelableExtra<ChatRoomData>("chatRoomData")!!
                    notificationToChat(chatRoomData)
                }
                "youtube"-> {
                    val inviteYoutubeData = requireActivity().intent.getParcelableExtra<InviteYoutubeData>("inviteYoutubeData")!!
                    notificationToVideoTogether(inviteYoutubeData)
                }
            }
        }
    }

    // 노티피케이션에서 채팅으로 들어갈 때
    private fun notificationToChat (chatRoomData: ChatRoomData) {
        val action = HomeDetailBlankFragmentDirections.actionHomeDetailBlankFragmentToChattingFragment(chatRoomData)
        homeDetailNavController.navigate(action)
    }

    // 노티피케이션에서 유투브 같이보기 들어갈 때
    private fun notificationToVideoTogether(inviteYoutubeData: InviteYoutubeData) {
        youtubeViewModel.setStateEvent(YoutubeStateEvent.SetVideoTogether(true))
        youtubeViewModel.setStateEvent(YoutubeStateEvent.SetJoiningVideoTogether(true))
        youtubeViewModel.setStateEvent(YoutubeStateEvent.SetFrontPlayer(inviteYoutubeData.youtubeData))
        socketViewModel.setStateEvent(SocketStateEvent.SendToTCPServer(JOIN_YOUTUBE_ROOM, inviteYoutubeData.roomId.toString()))
        homeNavController.navigate(R.id.action_friendsListFragment_to_youtubeFragment2)
    }

    // ------------------ 다이얼로그 리스너 ------------------
    // 로그아웃 확인 다이얼로그 리스너
    override fun onConfirm() {
        // 로그아웃 상태 -> 로켈에 데이터 삭제 및, 라이브 데이터 null 로 변경
        userViewModel.setStateEvent(UserStateEvent.SignOut)
        friendViewModel.setStateEvent(FriendStateEvent.SignOut)
        chatViewModel.setStateEvent(ChatStateEvent.SignOut)
        youtubeViewModel.setStateEvent(YoutubeStateEvent.SignOut)
        sharedPreferenceHelper.setBoolean(requireContext(), getString(R.string.auto_sign_in_key), false)
        mainNavController.popBackStack()
    }

    // 유투브 초대 다이얼로그 리스너
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

    // ------------------ 상단 앱바 메뉴 아이템 다이얼로그 리스너 ------------------
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.app_bar_menu, menu)

        when (homeNavController.currentDestination?.label) {
            "친구" -> {
                menu.removeItem(R.id.selectFriendsFragment)
                menu.removeItem(R.id.youtubeSearchFragment)
            }
            "채팅" -> {
                menu.removeItem(R.id.youtubeSearchFragment)
                menu.removeItem(R.id.addFriendFragment)
            }
            "유투브" -> {
                menu.removeItem(R.id.searchFragment2)
                menu.removeItem(R.id.selectFriendsFragment)
                menu.removeItem(R.id.addFriendFragment)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, "onOptionsItemSelected: ${homeNavController.getBackStackEntry(R.id.friendsListFragment)}")
        when (item.itemId) {
            R.id.youtubeSearchFragment -> {
                homeNavController.navigate(R.id.action_youtubeFragment_to_youtubeSearchFragment)
            }
            // 로그아웃 버튼 -> 다이얼로그 띄워주고 확인 누르면 로그인 화면으로 이동
            R.id.signInFragment -> {
                val signOutDialog = AlertDialog()
                val bundle = Bundle().apply {
                    putString("title", "로그아웃 확인")
                    putString("message", "로그아웃 하시겠습니까?")
                }
                signOutDialog.arguments = bundle
                if (isAdded) signOutDialog.show(childFragmentManager, "SignOutDialog")
            }
            else -> {
                NavigationUI.onNavDestinationSelected(item, homeDetailNavController)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        val removeItemList = ArrayList<Int>()
        val menu = homeToolbarTB.menu
        handleAppBarOption(true, View.VISIBLE)
        menu.clear()

        when (destination.id) {

            R.id.friendsListFragment -> {
                Log.i(TAG, "onDestinationChanged: friend")
                removeItemList.apply {
                    add(R.id.youtubeSearchFragment)
                    add(R.id.selectFriendsFragment)
                }
                refreshMenuItem(menu, removeItemList)
            }

            R.id.chatListFragment -> {
                Log.i(TAG, "onDestinationChanged: chat")
                removeItemList.apply {
                    add(R.id.youtubeSearchFragment)
                    add(R.id.addFriendFragment)
                }
                refreshMenuItem(menu, removeItemList)
            }

            R.id.youtubeFragment -> {
                removeItemList.apply {
                    add(R.id.searchFragment2)
                    add(R.id.addFriendFragment)
                    add(R.id.selectFriendsFragment)
                }
                refreshMenuItem(menu, removeItemList)
            }

            R.id.youtubeSearchFragment -> {
                handleAppBarOption(false, View.GONE)
            }
        }
    }

    // 상단 앱바 - 유투브 검색 때는 커스텀
    private fun handleAppBarOption(menuOption: Boolean, appbarVisibility: Int) {
        setHasOptionsMenu(menuOption) // 상단 앱 바 사용
        homeAppBarAB.visibility = appbarVisibility
    }

    private fun refreshMenuItem(menu: Menu, removeItemList: ArrayList<Int>) {
        val menuInflater = requireActivity().menuInflater
        menuInflater.inflate(R.menu.app_bar_menu, menu)

        removeItemList.forEach { item ->
            menu.removeItem(item)
        }
        Log.i(TAG, "onDestinationChanged: refresh $menu")
    }
}