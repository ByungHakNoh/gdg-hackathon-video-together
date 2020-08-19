package org.personal.videotogether.view.fragments.home.nestonhomedetail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_select_chat_room.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.repository.SocketRepository
import org.personal.videotogether.util.view.ViewHandler
import org.personal.videotogether.view.adapter.ChatRoomAdapter
import org.personal.videotogether.view.adapter.ItemClickListener
import org.personal.videotogether.viewmodel.*

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SelectChatRoomFragment
constructor(
    private val viewHandler: ViewHandler
) : Fragment(R.layout.fragment_select_chat_room), ItemClickListener, View.OnClickListener {

    private val TAG by lazy { javaClass.name }

    private val userViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val chatViewModel by lazy { ViewModelProvider(requireActivity())[ChatViewModel::class.java] }
    private val youtubeViewModel: YoutubeViewModel by lazy { ViewModelProvider(requireActivity())[YoutubeViewModel::class.java] }
    private val socketViewModel by lazy { ViewModelProvider(requireActivity())[SocketViewModel::class.java] }

    private val chatRoomList by lazy { ArrayList<ChatRoomData>() }
    private val chatRoomAdapter by lazy {
        ChatRoomAdapter(requireContext(), true, userViewModel.userData.value!!.id, chatRoomList, viewHandler, this)
    }
    private var selectedItemPosition: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
        setListener()
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
    }

    private fun setListener() {
        backBtn.setOnClickListener(this)
        confirmBtn.setOnClickListener(this)
    }

    private fun buildRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())

        selectableChatRoomListRV.setHasFixedSize(true)
        selectableChatRoomListRV.layoutManager = layoutManager
        selectableChatRoomListRV.adapter = chatRoomAdapter
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backBtn -> requireActivity().onBackPressed()
            R.id.confirmBtn -> {
                if (selectedItemPosition == null) {
                    Toast.makeText(requireContext(), "채팅방을 선택해주세요", Toast.LENGTH_SHORT).show()

                } else {
                    val userId = userViewModel.userData.value!!.id
                    val inviterUserData = userViewModel.userData.value
                    val friendsIds = ArrayList<Int>()
                    val chatParticipants = chatRoomList[selectedItemPosition!!].participantList
                    val currentYoutubeData = youtubeViewModel.currentPlayedYoutube.value!!

                    chatRoomList[selectedItemPosition!!].participantList.forEach { userData ->
                        if (userData.id != userId) friendsIds.add(userData.id)
                    }

                    // 룸 아이디는 userId 로 사용
                    socketViewModel.setStateEvent(SocketStateEvent.SendToTCPServer(SocketRepository.JOIN_YOUTUBE_ROOM, userId.toString()))
                    youtubeViewModel.setStateEvent(YoutubeStateEvent.InviteVideoTogether(inviterUserData!!, friendsIds,currentYoutubeData))
                    requireActivity().onBackPressed()
                }
            }
        }
    }

    override fun onItemClick(view: View?, itemPosition: Int) {
        val selectedItem = chatRoomList[itemPosition]

        if (selectedItemPosition == null) {
            selectedItemPosition = itemPosition
            selectedItem.isSelected = true

            chatRoomAdapter.notifyItemChanged(itemPosition)
        } else {
            val previousSelectedItem = chatRoomList[selectedItemPosition!!]

            previousSelectedItem.isSelected = false
            selectedItem.isSelected = true

            chatRoomAdapter.notifyItemChanged(itemPosition)
            chatRoomAdapter.notifyItemChanged(selectedItemPosition!!)
            selectedItemPosition = itemPosition
        }
    }
}