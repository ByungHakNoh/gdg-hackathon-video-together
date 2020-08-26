package org.personal.videotogether.view.fragments.home.nestonhome

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_chat_list.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.service.MyFirebaseMessagingService
import org.personal.videotogether.util.DataState
import org.personal.videotogether.util.SharedPreferenceHelper
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.util.view.ViewHandler
import org.personal.videotogether.view.adapter.ChatRoomAdapter
import org.personal.videotogether.view.adapter.ItemClickListener
import org.personal.videotogether.view.fragments.home.nestonhomedetail.HomeDetailBlankFragmentDirections
import org.personal.videotogether.viewmodel.ChatStateEvent
import org.personal.videotogether.viewmodel.ChatViewModel
import org.personal.videotogether.viewmodel.UserViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ChatListFragment
constructor(
    private val viewHandler: ViewHandler,
    private val sharedPreferenceHelper: SharedPreferenceHelper
) : Fragment(R.layout.fragment_chat_list), ItemClickListener {

    private val TAG by lazy { javaClass.name }

    private lateinit var homeDetailNavController: NavController

    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val chatViewModel by lazy { ViewModelProvider(requireActivity())[ChatViewModel::class.java] }

    private val chatRoomList by lazy { ArrayList<ChatRoomData>() }
    private val chatRoomAdapter by lazy {
        ChatRoomAdapter(requireContext(), false, userViewModel.userData.value!!.id, chatRoomList, viewHandler, this)
    }

    private lateinit var broadcastReceiver: BroadcastReceiver
    private val intentFilter by lazy { IntentFilter().apply { addAction(MyFirebaseMessagingService.RECEIVE_CHAT_MESSAGE) } }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainFragmentContainer: FragmentContainerView = view.rootView.findViewById(R.id.homeDetailFragmentContainer)
        homeDetailNavController = Navigation.findNavController(mainFragmentContainer)

        subscribeObservers()
        buildRecyclerView()
        defineReceiver()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun subscribeObservers() {

        userViewModel.userData.observe(viewLifecycleOwner, Observer { userData ->
            if (userData != null) {
                chatViewModel.setStateEvent(ChatStateEvent.GetChatRoomsFromServer(userData.id))
            }
        })

        // 로컬에 데이터가 없으면(새로 로그인을 하게되면) -> 서버에서 데이터 가져오기
        chatViewModel.chatRoomList.observe(viewLifecycleOwner, Observer { localChatRoomList ->
            if (localChatRoomList != null) {
                if (localChatRoomList.isNotEmpty()) {
                    chatRoomList.clear()
                    localChatRoomList.forEach { chatRoomData ->
                        chatRoomList.add(chatRoomData)
                    }
                    chatRoomAdapter.notifyDataSetChanged()
                }
            }
        })

        // 서버에서 채팅방 가져오는 라이브 데이터 -> 성공 시 채팅방 리스트 라이브 데이터 업데이트
        chatViewModel.getChatRoomList.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Loading -> Log.i(TAG, "getChatRoomList: 로딩")
                is DataState.NoData -> Log.i(TAG, "getChatRoomList: 데이터 없음")
                is DataState.Error -> Log.i(TAG, "getChatRoomList: 에러 발생")
            }
        })
    }

    // 로컬에 데이터가 없으면 서버에 데이터 요청
    private fun requestChatRoom() {
        if (chatRoomList.isNotEmpty()) {
            chatRoomList.clear()
            chatRoomAdapter.notifyDataSetChanged()
        }
        chatViewModel.setStateEvent(ChatStateEvent.GetChatRoomsFromServer(userViewModel.userData.value!!.id))
    }

    private fun buildRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())

        chatRoomListRV.setHasFixedSize(true)
        chatRoomListRV.layoutManager = layoutManager
        chatRoomListRV.adapter = chatRoomAdapter
    }

    private fun defineReceiver() {

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    MyFirebaseMessagingService.RECEIVE_CHAT_MESSAGE -> {
                        val chatRoomData = intent.getParcelableExtra<ChatRoomData>("chatRoomData")!!
                        val currentRoomId = sharedPreferenceHelper.getInt(requireContext(), getText(R.string.current_chat_room_id).toString())
                        chatRoomList.forEach { chatRoom ->
                            if (chatRoomData.id == chatRoom.id) {
                                // 현재 참여하고 있는 방이 아니라면 안읽은 메시지 더해주기
                                if (chatRoomData.id != currentRoomId) {
                                    chatRoom.unReadChatCount += 1
                                }
                                chatRoom.lastChatMessage = chatRoomData.lastChatMessage
                                chatRoom.lastChatTime = chatRoomData.lastChatTime

                                chatRoomList.remove(chatRoom)
                                chatRoomList.add(0, chatRoom)
                                chatRoomAdapter.notifyDataSetChanged()
                                return
                            }
                        }
                    }
                }
            }
        }
    }

    // ------------------ 리사이클러뷰 아이템 클릭 리스너 ------------------
    override fun onItemClick(view: View?, itemPosition: Int) {
        val selectedChatRoom = chatRoomList[itemPosition]
        val action = HomeDetailBlankFragmentDirections.actionHomeDetailBlankFragmentToChattingFragment(selectedChatRoom)

        homeDetailNavController.navigate(action)
    }
}