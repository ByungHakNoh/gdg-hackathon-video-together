package org.personal.videotogether.view.fragments.home.nestonhome

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_chat_list.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.util.DataState
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.view.adapter.ChatRoomAdapter
import org.personal.videotogether.view.adapter.ItemClickListener
import org.personal.videotogether.view.fragments.nestonmain.HomeFragmentDirections
import org.personal.videotogether.viewmodel.ChatStateEvent
import org.personal.videotogether.viewmodel.ChatViewModel
import org.personal.videotogether.viewmodel.UserViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ChatListFragment
constructor(
    private val dataStateHandler: DataStateHandler
) : Fragment(R.layout.fragment_chat_list), ItemClickListener {

    private val TAG by lazy { javaClass.name }

    private lateinit var mainNavController: NavController


    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val chatViewModel by lazy { ViewModelProvider(requireActivity())[ChatViewModel::class.java] }

    private val chatRoomList by lazy { ArrayList<ChatRoomData>() }
    private val chatRoomAdapter by lazy { ChatRoomAdapter(requireContext(), chatRoomList, this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainFragmentContainer: FragmentContainerView = view.rootView.findViewById(R.id.mainFragmentContainer)
        mainNavController = Navigation.findNavController(mainFragmentContainer)
        subscribeObservers()
        buildRecyclerView()
        chatViewModel.setStateEvent(ChatStateEvent.GetChatRoomsFromServer(userViewModel.userData.value!!.id))
    }

    private fun subscribeObservers() {

        chatViewModel.chatRoomList.observe(viewLifecycleOwner, Observer { localChatRoomList ->
            chatRoomList.clear()
            localChatRoomList!!.forEach { chatRoomData ->
                chatRoomList.add(chatRoomData)
            }
            chatRoomAdapter.notifyDataSetChanged()
        })

        // 친구 검색하기
        chatViewModel.getChatRoomList.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Loading -> {
                    Log.i(TAG, "getChatRoomList: 로딩")
                }
                is DataState.Success<List<ChatRoomData>?> -> {
                    Log.i(TAG, "getChatRoomList: 성공")
                }
                is DataState.NoData -> {
                    Log.i(TAG, "getChatRoomList: 데이터 없음")
                }
                is DataState.Error -> {
                    Log.i(TAG, "getChatRoomList: 에러 발생")
                }
            }
        })
    }

    private fun buildRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())

        chatRoomListRV.setHasFixedSize(true)
        chatRoomListRV.layoutManager = layoutManager
        chatRoomListRV.adapter = chatRoomAdapter
    }

    override fun onItemClick(view: View?, itemPosition: Int) {
        val selectedChatRoom = chatRoomList[itemPosition]
        val action = HomeFragmentDirections.actionMainHomeFragmentToChattingFragment(selectedChatRoom)
        mainNavController.navigate(action)
    }
}