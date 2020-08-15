package org.personal.videotogether.view.fragments.nestonmain

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_friends_list.*
import kotlinx.android.synthetic.main.fragment_main_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.viewmodel.*

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_main_home), NavController.OnDestinationChangedListener {

    private val TAG = javaClass.name

    private lateinit var mainNavController: NavController
    private lateinit var homeNavController: NavController

    private val friendViewModel: FriendViewModel by lazy { ViewModelProvider(requireActivity())[FriendViewModel::class.java] }
    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val chatViewModel by lazy { ViewModelProvider(requireActivity())[ChatViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // 상단 앱 바 사용
        Log.i(TAG, "onCreate: ")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nestedNavHostFragment = childFragmentManager.findFragmentById(R.id.homeFragmentContainer)
        homeNavController = nestedNavHostFragment!!.findNavController()
        mainNavController = Navigation.findNavController(view)
        (requireActivity() as AppCompatActivity).setSupportActionBar(homeToolbarTB)
        subscribeObservers()
        setBottomNav()
        userViewModel.setStateEvent(UserStateEvent.GetUserDataFromLocal)
    }

    private fun subscribeObservers() {
        // 사용자 정보를 room 으로부터 가져옴
        // 유저 정보를 이용해 친구 데이터 업데이트, 채팅 소켓 등록을 함
        userViewModel.userData.observe(viewLifecycleOwner, Observer { userData ->
            friendViewModel.setStateEvent(FriendStateEvent.GetFriendListFromServer(userData!!.id))
            chatViewModel.setStateEvent(ChatStateEvent.RegisterSocket(userData))
            chatViewModel.setStateEvent(ChatStateEvent.ReceiveFromTCPServer)
            Log.i(TAG, "subscribeObservers: 한번?")
        })
    }

    // 상단 앱 바 아이템 추가
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.app_bar_menu, menu)
        when(homeNavController.currentDestination?.label) {
            "친구 목록" -> {
                menu.removeItem(R.id.addChatRoomFragment)
                menu.removeItem(R.id.youtubeSearchFragment)
            }
            "채팅 목록" -> {
                menu.removeItem(R.id.youtubeSearchFragment)
                menu.removeItem(R.id.addFriendFragment)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    // 바텀 네비게이션 관련 세팅
    private fun setBottomNav() {
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.friendsListFragment, R.id.chatListFragment, R.id.youtubeFragment))

        NavigationUI.setupWithNavController(homeToolbarTB, homeNavController, appBarConfiguration)
        bottomNavigationBN.setupWithNavController(homeNavController)
        homeNavController.addOnDestinationChangedListener(this) // 상단 앱 바 메뉴 아이템 변경해주기 위해 사용
    }

    // ------------------ 상단 앱 바 아이템 클릭 리스너 모음 ------------------
    @SuppressLint("RestrictedApi")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, "onOptionsItemSelected: ${homeNavController.getBackStackEntry(R.id.friendsListFragment)}")
        when(item.itemId) {
            R.id.youtubeSearchFragment -> {
                homeNavController.navigate(R.id.action_youtubeFragment_to_youtubeSearchFragment)
            }
            else ->{
                NavigationUI.onNavDestinationSelected(item, mainNavController)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // ------------------ homeNavController 리스너 모음 ------------------
    // 네비게이션 아이템을 클릭해서 destination 이 변경 될때마다 상단 앱 바 메뉴 아이템 변경
    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        val removeItemList = ArrayList<Int>()
        val menu = homeToolbarTB.menu
        menu.clear()
        searchET.visibility = View.GONE

        when (destination.id) {

            R.id.friendsListFragment -> {
                Log.i(TAG, "onDestinationChanged: friend")
                removeItemList.apply {
                    add(R.id.youtubeSearchFragment)
                    add(R.id.addChatRoomFragment)
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
                    add(R.id.searchFragment)
                    add(R.id.addFriendFragment)
                    add(R.id.addChatRoomFragment)
                }
                refreshMenuItem(menu, removeItemList)
            }

            R.id.youtubeSearchFragment -> {
                homeToolbarTB.title = null
                searchET.visibility = View.VISIBLE
            }
        }
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