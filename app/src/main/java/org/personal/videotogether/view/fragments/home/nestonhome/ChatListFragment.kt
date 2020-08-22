package org.personal.videotogether.view.fragments.home.nestonhome

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
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
    private val dataStateHandler: DataStateHandler,
    private val viewHandler: ViewHandler
) : Fragment(R.layout.fragment_chat_list), ItemClickListener, View.OnClickListener {

    private val TAG by lazy { javaClass.name }

    private lateinit var homeNavController: NavController
    private lateinit var homeDetailNavController: NavController
    private lateinit var backPressCallback: OnBackPressedCallback // 뒤로가기 버튼 콜백

    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val chatViewModel by lazy { ViewModelProvider(requireActivity())[ChatViewModel::class.java] }

    private val chatRoomList by lazy { ArrayList<ChatRoomData>() }
    private val chatRoomAdapter by lazy {
        ChatRoomAdapter(requireContext(), false, userViewModel.userData.value!!.id, chatRoomList, viewHandler, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainFragmentContainer: FragmentContainerView = view.rootView.findViewById(R.id.homeDetailFragmentContainer)
        homeNavController = Navigation.findNavController(view)
        homeDetailNavController = Navigation.findNavController(mainFragmentContainer)

        setBackPressCallback()
        subscribeObservers()
        setListener()
        buildRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backPressCallback.remove()
    }

    private fun setBackPressCallback() {
        // 비디오 모션 레이아웃 관련 뒤로가기 버튼은 VideoPlayFragment 에서 관리
        // VideoPlayFragment 뒤로가기 콜백이 disable 되면 실행됨
        backPressCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            homeNavController.popBackStack()
        }
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

    private fun setListener() {
        backBtn.setOnClickListener(this)
        searchBtn.setOnClickListener(this)
        addChatRoomBtn.setOnClickListener(this)
    }

    private fun buildRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())

        chatRoomListRV.setHasFixedSize(true)
        chatRoomListRV.layoutManager = layoutManager
        chatRoomListRV.adapter = chatRoomAdapter
    }

    // ------------------ 클릭 리스너 ------------------
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backBtn -> requireActivity().onBackPressed()
            R.id.searchBtn -> homeDetailNavController.navigate(R.id.action_homeDetailBlankFragment_to_searchFragment2)
            R.id.addChatRoomBtn -> {
                val request = "addChatRoom"
                val action = HomeDetailBlankFragmentDirections.actionHomeDetailBlankFragmentToSelectFriendsFragment(request)
                homeDetailNavController.navigate(action)
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