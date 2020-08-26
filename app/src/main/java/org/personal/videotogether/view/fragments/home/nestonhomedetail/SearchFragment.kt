package org.personal.videotogether.view.fragments.home.nestonhomedetail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import kotlinx.android.synthetic.main.fragment_chat_list.chatRoomListRV
import kotlinx.android.synthetic.main.fragment_friend_list.friendsListRV
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.util.view.ViewHandler
import org.personal.videotogether.view.adapter.ChatRoomAdapter
import org.personal.videotogether.view.adapter.FriendListAdapter
import org.personal.videotogether.view.adapter.ItemClickListener
import org.personal.videotogether.viewmodel.*

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SearchFragment
constructor(
    private val viewHandler: ViewHandler
) : Fragment(R.layout.fragment_search), ItemClickListener, View.OnClickListener, TextWatcher {

    private val TAG by lazy { javaClass.name }

    private lateinit var homeDetailNavController: NavController
    private lateinit var homeNavController: NavController

    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val friendViewModel: FriendViewModel by lazy { ViewModelProvider(requireActivity())[FriendViewModel::class.java] }
    private val chatViewModel by lazy { ViewModelProvider(requireActivity())[ChatViewModel::class.java] }

    // 리사이클러 뷰
    private val friendList by lazy { ArrayList<FriendData>() }
    private val filteredFriendList by lazy { ArrayList<FriendData>() }
    private val friendListAdapter by lazy { FriendListAdapter(requireContext(), friendList, false, this) }

    private val chatRoomList by lazy { ArrayList<ChatRoomData>() }
    private val filteredChatRoomList by lazy { ArrayList<ChatRoomData>() }
    private val chatRoomAdapter by lazy {
        ChatRoomAdapter(requireContext(), false, userViewModel.userData.value!!.id, chatRoomList, viewHandler, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homeFragmentContainer: FragmentContainerView = view.rootView.findViewById(R.id.homeFragmentContainer)
        homeDetailNavController = Navigation.findNavController(view)
        homeNavController = Navigation.findNavController(homeFragmentContainer)

        subscribeObservers()
        setListener()
        buildRecyclerView()
    }

    private fun subscribeObservers() {
        userViewModel.userData.observe(viewLifecycleOwner, Observer { userData ->
            if (userData != null) {
                friendViewModel.setStateEvent(FriendStateEvent.GetFriendListFromServer(userData.id))
                chatViewModel.setStateEvent(ChatStateEvent.GetChatRoomsFromServer(userData.id))
            }
        })
        // 친구 목록 불러오기
        friendViewModel.friendList.observe(viewLifecycleOwner, Observer { localFriendList ->
            if (localFriendList != null) {
                if (localFriendList.isNotEmpty()) {
                    friendList.clear()
                    localFriendList.forEach { friendData ->
                        friendList.add(friendData)
                    }
                    friendListAdapter.notifyDataSetChanged()
                }
            }
        })

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
    }

    private fun setListener() {
        backBtn.setOnClickListener(this)
        searchET.addTextChangedListener(this)
    }

    private fun buildRecyclerView() {
        val friendLayoutManager = LinearLayoutManager(requireContext())
        val chatRoomLayoutManager = LinearLayoutManager(requireContext())

        friendsListRV.setHasFixedSize(true)
        friendsListRV.layoutManager = friendLayoutManager
        friendsListRV.adapter = friendListAdapter

        chatRoomListRV.setHasFixedSize(true)
        chatRoomListRV.layoutManager = chatRoomLayoutManager
        chatRoomListRV.adapter = chatRoomAdapter
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backBtn -> requireActivity().onBackPressed()
        }
    }

    override fun onItemClick(view: View?, itemPosition: Int) {
        when (view?.id) {
            R.id.friendItemCL -> {
                val friendData = getSelectedFriend(itemPosition)
                val action = SearchFragmentDirections.actionSearchFragment2ToProfileFriendFragment(friendData)
                homeDetailNavController.navigate(action)
            }

            R.id.chatRoomItemCL -> {
                val chatRoomData = getSelectedChatRoom(itemPosition)
                val action = SearchFragmentDirections.actionSearchFragment2ToChattingFragment(chatRoomData)
                homeDetailNavController.navigate(action)
                when (homeNavController.currentDestination?.id) {
                    R.id.friendsListFragment -> homeNavController.navigate(R.id.action_friendsListFragment_to_chatListFragment)
                    else -> Log.i(TAG, "onConfirm: ${homeNavController.currentDestination}")
                }
            }
        }
    }

    // 검색하게되면 어뎁터의 list 가 changedChatRoomList 로 변경되기 떄문에 예외처리하여 방의 이름을 반환
    private fun getSelectedFriend(itemPosition: Int): FriendData {
        val searchText = searchET.text.toString()

        return if (searchText.isEmpty()) friendList[itemPosition]
        else filteredFriendList[itemPosition]
    }

    // 검색하게되면 어뎁터의 list 가 changedChatRoomList 로 변경되기 떄문에 예외처리하여 방의 이름을 반환
    private fun getSelectedChatRoom(itemPosition: Int): ChatRoomData {
        val searchText = searchET.text.toString()

        return if (searchText.isEmpty()) chatRoomList[itemPosition]
        else filteredChatRoomList[itemPosition]
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun afterTextChanged(text: Editable?) {
        searchFilter(text.toString())
    }

    private fun searchFilter(text: String) {
        filteredChatRoomList.clear()
        filteredFriendList.clear()


        chatRoomList.forEach { chatRoomData ->
            chatRoomData.participantList.forEach { userData ->
                if (userData.name!!.toLowerCase().contains(text.toLowerCase())) {
                    filteredChatRoomList.add(chatRoomData)
                }
            }
        }

        friendList.forEach { friendData ->
            if (friendData.name!!.toLowerCase().contains(text.toLowerCase())) {
                filteredFriendList.add(friendData)
            }
        }
        chatRoomAdapter.filterList(filteredChatRoomList)
        friendListAdapter.filterList(filteredFriendList)
    }
}