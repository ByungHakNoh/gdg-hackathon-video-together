package org.personal.videotogether.view.fragments.nestonmain

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_chat_room.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.util.DataState
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.view.adapter.FriendListAdapter
import org.personal.videotogether.view.adapter.ItemClickListener
import org.personal.videotogether.view.adapter.SelectedFriendAdapter
import org.personal.videotogether.viewmodel.ChatStateEvent
import org.personal.videotogether.viewmodel.ChatViewModel
import org.personal.videotogether.viewmodel.FriendViewModel
import org.personal.videotogether.viewmodel.UserViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddChatRoomFragment
constructor(
    private val dataStateHandler: DataStateHandler
) : Fragment(R.layout.fragment_add_chat_room), ItemClickListener, View.OnClickListener {

    private val TAG by lazy { javaClass.name }

    private lateinit var mainNavController: NavController

    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val friendViewModel: FriendViewModel by lazy { ViewModelProvider(requireActivity())[FriendViewModel::class.java] }
    private val chatViewModel by lazy { ViewModelProvider(requireActivity())[ChatViewModel::class.java] }

    private val selectedFriendList by lazy { ArrayList<FriendData>() }
    private val selectedFriendAdapter by lazy { SelectedFriendAdapter(requireContext(), selectedFriendList, this) }

    private val friendList by lazy { ArrayList<FriendData>() }
    private val selectableFriendAdapter by lazy { FriendListAdapter(requireContext(), friendList, true, this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainNavController = Navigation.findNavController(view)
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
        confirmBtn.setOnClickListener(this)
    }

    private fun buildRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        val horizontalLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        friendViewModel.friendList.value?.forEach { friendData -> friendList.add(friendData) }

        selectedFriendListRV.setHasFixedSize(true)
        selectedFriendListRV.layoutManager = horizontalLayoutManager
        selectedFriendListRV.adapter = selectedFriendAdapter

        selectableFriendsListRV.setHasFixedSize(true)
        selectableFriendsListRV.layoutManager = layoutManager
        selectableFriendsListRV.adapter = selectableFriendAdapter
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.confirmBtn -> {
                if (selectedFriendList.size == 0) {
                    Toast.makeText(requireContext(), "1명 이상 선택해주세요", Toast.LENGTH_SHORT).show()
                } else {
                    chatViewModel.setStateEvent(ChatStateEvent.AddChatRoom(userViewModel.userData.value!!, selectedFriendList))
                }
            }
        }
    }

    override fun onItemClick(view: View?, itemPosition: Int) {

        when(view?.id) {

            R.id.friendItemCL -> {
                selectedFriendListRV.visibility = View.VISIBLE
                val friendData = friendList[itemPosition]

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