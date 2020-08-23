package org.personal.videotogether.view.fragments.home.nestonhomedetail

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_chatting.*
import kotlinx.android.synthetic.main.fragment_chatting.chattingInputED
import kotlinx.android.synthetic.main.fragment_chatting.sendBtn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.ChatData
import org.personal.videotogether.repository.SocketRepository.Companion.EXIT_CHAT_ROOM
import org.personal.videotogether.repository.SocketRepository.Companion.JOIN_CHAT_ROOM
import org.personal.videotogether.repository.SocketRepository.Companion.SEND_CHAT_MESSAGE
import org.personal.videotogether.util.DataState
import org.personal.videotogether.util.view.ViewHandler
import org.personal.videotogether.view.adapter.ChatAdapter
import org.personal.videotogether.viewmodel.*

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ChattingFragment
constructor(
    private val viewHandler: ViewHandler
) : Fragment(R.layout.fragment_chatting), View.OnClickListener {

    private val TAG by lazy { javaClass.name }

    private lateinit var homeDetailNavController: NavController

    // args : 채팅 방 데이터 받아옴
    private val argument: ChattingFragmentArgs by navArgs()
    private val chatRoomData by lazy { argument.chatRoomData }

    private val userViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val chatViewModel by lazy { ViewModelProvider(requireActivity())[ChatViewModel::class.java] }
    private val socketViewModel by lazy { ViewModelProvider(requireActivity())[SocketViewModel::class.java] }

    private val chatList by lazy { ArrayList<ChatData>() }
    private val chatAdapter by lazy { ChatAdapter(requireContext(), userViewModel.userData.value!!.id, chatList) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeDetailNavController = Navigation.findNavController(view)
        homeToolbarTB.title = viewHandler.formChatRoomName(chatRoomData.participantList, userViewModel.userData.value!!.id)

        subscribeObservers()
        setListener()
        buildRecyclerview()
        socketViewModel.setStateEvent(SocketStateEvent.SendToTCPServer(JOIN_CHAT_ROOM, chatRoomData.id.toString()))
        chatViewModel.setStateEvent(ChatStateEvent.GetChatMessageFromServer(chatRoomData.id))
    }

    private fun subscribeObservers() {
        socketViewModel.chatMessage.observe(viewLifecycleOwner, Observer { chatData ->
            if (chatData != null) {
                chatList.add(chatData)
                chatAdapter.notifyItemInserted(chatList.size - 1)
                chattingBoxRV.scrollToPosition(chatAdapter.itemCount - 1)
            }
        })

        // TODO : 채팅 로컬에서 가져올 수 있도록 구현하기
        chatViewModel.chatMessage.observe(viewLifecycleOwner, Observer { chatHistory ->
            if (chatHistory != null) {
                chatList.clear()
                chatHistory.forEach { chatData ->  chatList.add(chatData)}
                chatAdapter.notifyDataSetChanged()
                chattingBoxRV.scrollToPosition(chatAdapter.itemCount - 1)
            }
        })

        chatViewModel.getChatFromServer.observe(viewLifecycleOwner, Observer { dataState->
            when(dataState) {
                is DataState.Loading-> {
                    Log.i(TAG, "getChatMessageList: 로딩중")
                }
                is DataState.Success -> {
                    val responseChatList = dataState.data

                    chatList.clear()
                    responseChatList.forEach { chatData ->  chatList.add(chatData)}
                    chatAdapter.notifyDataSetChanged()
                    chattingBoxRV.scrollToPosition(chatAdapter.itemCount - 1)
                }
                is DataState.NoData -> {
                    Log.i(TAG, "getChatMessageList: 데이터 없음")
                }
                is DataState.Error -> {
                    Log.i(TAG, "getChatMessageList: 서버 연결 문제")
                }
            }
        })
    }

    private fun setListener() {
        backBtn.setOnClickListener(this)
        sendBtn.setOnClickListener(this)
    }

    private fun buildRecyclerview() {
        val layoutManager = LinearLayoutManager(requireContext())

        chattingBoxRV.setHasFixedSize(true)
        chattingBoxRV.layoutManager = layoutManager
        chattingBoxRV.adapter = chatAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        socketViewModel.setStateEvent(SocketStateEvent.SendToTCPServer(EXIT_CHAT_ROOM))
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backBtn -> requireActivity().onBackPressed()
            R.id.sendBtn -> {
                val message = chattingInputED.text.toString()

                if (message.trim().isNotEmpty()) {
                    val userData= userViewModel.userData.value!!
                    val chatData = ChatData(chatRoomData.id, userData.id, userData.name!!, userData.profileImageUrl!!, message, null)

                    socketViewModel.setStateEvent(SocketStateEvent.SendToTCPServer(SEND_CHAT_MESSAGE, chatRoomData.id.toString(), message))
                    chatViewModel.setStateEvent(ChatStateEvent.UploadChatMessage(chatData))
                    chattingInputED.text = null
                }
            }
        }
    }
}