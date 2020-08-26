package org.personal.videotogether.view.fragments.home.nestonhomedetail

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_select_friends.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.util.DataState
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.view.adapter.FriendListAdapter
import org.personal.videotogether.view.adapter.ItemClickListener
import org.personal.videotogether.view.adapter.SelectedFriendAdapter
import org.personal.videotogether.viewmodel.*

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SelectFriendFragment
constructor(
    private val dataStateHandler: DataStateHandler
) : Fragment(R.layout.fragment_select_friends), ItemClickListener, View.OnClickListener {

    private val TAG by lazy { javaClass.name }

    // 네비게이션 컨트롤러
    private lateinit var homeDetailNavController: NavController

    // 뷰 모델
    private val userViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val friendViewModel by lazy { ViewModelProvider(requireActivity())[FriendViewModel::class.java] }
    private val chatViewModel by lazy { ViewModelProvider(requireActivity())[ChatViewModel::class.java] }
    private val youtubeViewModel by lazy { ViewModelProvider(requireActivity())[YoutubeViewModel::class.java] }
    private val socketViewModel by lazy { ViewModelProvider(requireActivity())[SocketViewModel::class.java] }

    // 리사이클러 뷰
    private val selectedFriendList by lazy { ArrayList<FriendData>() }
    private val selectedFriendAdapter by lazy { SelectedFriendAdapter(requireContext(), selectedFriendList, this) }

    private val friendList by lazy { ArrayList<FriendData>() }
    private val selectableFriendAdapter by lazy { FriendListAdapter(requireContext(), friendList, true, this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeDetailNavController = Navigation.findNavController(view)
        subscribeObservers()
        setListener()
        buildRecyclerView()
    }

    private fun subscribeObservers() {
        // 친구 검색하기
        chatViewModel.addChatRoom.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Loading -> {
                    dataStateHandler.displayLoadingDialog(true, childFragmentManager)
                }
                is DataState.Success<ChatRoomData?> -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    chatViewModel.setStateEvent(ChatStateEvent.GetChatRoomsFromServer(userViewModel.userData.value!!.id))
                    requireActivity().onBackPressed()
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

    private fun setListener() {
        backBtn.setOnClickListener(this)
        confirmBtn.setOnClickListener(this)
    }

    // 친구 데이터는 뷰모델에서 관리하는 friend 데이터를 사용한다
    private fun buildRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        val horizontalLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        friendViewModel.friendList.value?.forEach { friendData ->
            friendData.isSelected = false
            friendList.add(friendData)
        }

        selectedFriendListRV.setHasFixedSize(true)
        selectedFriendListRV.layoutManager = horizontalLayoutManager
        selectedFriendListRV.adapter = selectedFriendAdapter

        selectableFriendsListRV.setHasFixedSize(true)
        selectableFriendsListRV.layoutManager = layoutManager
        selectableFriendsListRV.adapter = selectableFriendAdapter
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backBtn -> requireActivity().onBackPressed()
            R.id.confirmBtn -> {
                when (selectedFriendList.size) {
                    0 -> Toast.makeText(requireContext(), "1명 이상 선택해주세요", Toast.LENGTH_SHORT).show()
                    1 -> {
                        val selectedFriendData = selectedFriendList[0]
                        val chatRoomList = chatViewModel.chatRoomList.value
                        chatRoomList?.forEach { chatRoom ->
                            if (chatRoom.participantList.size == 2) {
                                chatRoom.participantList.forEach { userData ->
                                    // 이미 존재하는 방이라면 채팅방으로 이동
                                    if (userData.id == selectedFriendData.id) {
                                        val action = SelectFriendFragmentDirections.actionSelectFriendsFragmentToChattingFragment(chatRoom)
                                        homeDetailNavController.navigate(action)
                                        return
                                    }
                                }
                            }
                        }
                        // 중복된 1대1 채팅이 존재하지 않다면 채팅방 생성
                        chatViewModel.setStateEvent(ChatStateEvent.AddChatRoom(userViewModel.userData.value!!, selectedFriendList, getFriendIdList()))
                    }
                    else -> chatViewModel.setStateEvent(ChatStateEvent.AddChatRoom(userViewModel.userData.value!!, selectedFriendList, getFriendIdList()))
                }
            }
        }
    }

    private fun getFriendIdList(): ArrayList<Int> {
        val userData = userViewModel.userData.value!!
        val friendIds = ArrayList<Int>()
        selectedFriendList.forEach { friendData ->
            if (friendData.id != userData.id) friendIds.add(friendData.id)
        }
        return friendIds
    }

    override fun onItemClick(view: View?, itemPosition: Int) {

        when (view?.id) {

            R.id.friendItemCL -> {
                val friendData = friendList[itemPosition]
                selectedFriendListRV.visibility = View.VISIBLE

                if (friendData.isSelected == null) friendData.isSelected = false
                friendData.isSelected = !friendData.isSelected!!
                selectableFriendAdapter.notifyItemChanged(itemPosition)

                // 선택됬는지 판별
                if (friendData.isSelected!!) {
                    selectedFriendList.add(friendData)
                    selectedFriendAdapter.notifyDataSetChanged()

                } else {
                    if (selectedFriendList.contains(friendData)) {
                        selectedFriendList.remove(friendData)
                        selectedFriendAdapter.notifyDataSetChanged()
                    }
                }
            }

            R.id.deleteBtn -> {
                val friendListIndex = friendList.indexOf(selectedFriendList[itemPosition])
                friendList[friendListIndex].isSelected = !friendList[friendListIndex].isSelected!!

                selectedFriendList.removeAt(itemPosition)
                selectableFriendAdapter.notifyItemChanged(friendListIndex)
                selectedFriendAdapter.notifyDataSetChanged()
            }
        }
    }
}