package org.personal.videotogether.view.fragments.home.nestonhome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
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
import org.personal.videotogether.viewmodel.ChatStateEvent
import org.personal.videotogether.viewmodel.ChatViewModel
import org.personal.videotogether.viewmodel.UserViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ChatListFragment
    constructor(
        private val dataStateHandler: DataStateHandler
    ): Fragment(R.layout.fragment_chat_list), ChatRoomAdapter.ItemClickListener {

    private val TAG by lazy { javaClass.name }

    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val chatViewModel by lazy { ViewModelProvider(requireActivity())[ChatViewModel::class.java] }

    private val chatRoomList by lazy { ArrayList<ChatRoomData>() }
    private val chatRoomAdapter by lazy { ChatRoomAdapter(requireContext(), chatRoomList, this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeObservers()
        buildRecyclerView()
        chatViewModel.setStateEvent(ChatStateEvent.GetChatRoomList(userViewModel.userData.value!!.id))
    }

    private fun subscribeObservers() {
        // 친구 검색하기
        chatViewModel.getChatRoomList.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Loading -> {
                    dataStateHandler.displayLoadingDialog(true, childFragmentManager)
                }
                is DataState.Success<List<ChatRoomData>?> -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    chatRoomList.clear()
                    dataState.data!!.forEach { chatRoomData -> chatRoomList.add(chatRoomData) }
                    chatRoomAdapter.notifyDataSetChanged()
                }
                is DataState.NoData -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                }
                is DataState.Error -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
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

    }
}