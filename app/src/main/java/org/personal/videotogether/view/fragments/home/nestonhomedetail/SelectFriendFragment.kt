package org.personal.videotogether.view.fragments.home.nestonhomedetail

import android.os.Bundle
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

    // args : 채팅방 생성, 유투브 같이보기 구분
    private val argument: SelectFriendFragmentArgs by navArgs()
    private val whichRequest by lazy { argument.whichRequest }

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
                if (selectedFriendList.size == 0) {
                    Toast.makeText(requireContext(), "1명 이상 선택해주세요", Toast.LENGTH_SHORT).show()
                } else {
                    when (whichRequest) {
                        "addChatRoom" -> chatViewModel.setStateEvent(ChatStateEvent.AddChatRoom(userViewModel.userData.value!!, selectedFriendList))
                    }
                }
            }
        }
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