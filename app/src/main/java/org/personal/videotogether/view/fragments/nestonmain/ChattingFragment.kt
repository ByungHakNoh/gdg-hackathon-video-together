package org.personal.videotogether.view.fragments.nestonmain

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.ChatData
import org.personal.videotogether.repository.SocketRepository.Companion.EXIT_CHAT_ROOM
import org.personal.videotogether.repository.SocketRepository.Companion.JOIN_CHAT_ROOM
import org.personal.videotogether.repository.SocketRepository.Companion.SEND_CHAT_MESSAGE
import org.personal.videotogether.view.adapter.ChatAdapter
import org.personal.videotogether.viewmodel.ChatViewModel
import org.personal.videotogether.viewmodel.SocketStateEvent
import org.personal.videotogether.viewmodel.SocketViewModel
import org.personal.videotogether.viewmodel.UserViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ChattingFragment : Fragment(R.layout.fragment_chatting), View.OnClickListener {

    private val TAG by lazy { javaClass.name }

    private lateinit var mainNavController: NavController

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

        mainNavController = Navigation.findNavController(view)
        subscribeObservers()
        setListener()
        buildRecyclerview()
        socketViewModel.setStateEvent(SocketStateEvent.SendToTCPServer(JOIN_CHAT_ROOM, chatRoomData.id.toString()))
        Log.i(TAG, "onViewCreated: ${chatRoomData.id}")
    }

    private fun subscribeObservers() {
        socketViewModel.chatMessage.observe(viewLifecycleOwner, Observer { chatData ->
            //TODO : 채팅 서버에 저장하기 유투브 먼저 하고 하자
            chatList.add(chatData!!)
            chatAdapter.notifyItemInserted(chatList.size - 1)
        })
    }

    private fun setListener() {
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
        when(view?.id) {
            R.id.sendBtn -> {
                val message = chattingInputED.text.toString()

                if (message.trim().isNotEmpty()) {
                    socketViewModel.setStateEvent(SocketStateEvent.SendToTCPServer(SEND_CHAT_MESSAGE, chatRoomData.id.toString(), message))
                    chattingInputED.text = null
                }
            }
        }
    }
}