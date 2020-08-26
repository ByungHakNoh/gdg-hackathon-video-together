package org.personal.videotogether.view.fragments.home.nestonhomedetail

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_profile_friend.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.util.DataState
import org.personal.videotogether.viewmodel.ChatStateEvent
import org.personal.videotogether.viewmodel.ChatViewModel
import org.personal.videotogether.viewmodel.UserViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProfileFriendFragment : Fragment(R.layout.fragment_profile_friend), View.OnClickListener {

    private val TAG = javaClass.name

    private lateinit var homeDetailNavController: NavController
    private lateinit var homeNavController: NavController

    private val argument: ProfileFriendFragmentArgs by navArgs()
    private val friendData by lazy { argument.friendData }

    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val chatViewModel by lazy { ViewModelProvider(requireActivity())[ChatViewModel::class.java] }

    // 기존에 이미 만들어져있는 채팅방 null 이라면 서버에 채팅방 생성하기
    private var existedChatRoom: ChatRoomData? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homeFragmentContainer: FragmentContainerView = view.rootView.findViewById(R.id.homeFragmentContainer)
        homeDetailNavController = Navigation.findNavController(view)
        homeNavController = Navigation.findNavController(homeFragmentContainer)

        subscribeObservers()
        setInitView()
        setListener()
    }

    private fun subscribeObservers() {
        chatViewModel.chatRoomList.observe(viewLifecycleOwner, Observer { chatRoomList ->
            chatRoomList?.forEach { chatRoom ->
                if (chatRoom.participantList.size == 2) {
                    chatRoom.participantList.forEach { userData ->
                        Log.i(TAG, "chatRoomList: $chatRoom")
                        if (userData.id == friendData.id)  existedChatRoom = chatRoom
                    }
                }
            }
        })

        chatViewModel.addChatRoom.observe(viewLifecycleOwner, Observer { dataState ->
            when(dataState) {
                is DataState.Loading -> {
                    Log.i(TAG, "addChatRoom: no data")
                }
                is DataState.Success<ChatRoomData?> -> {
                    val chatRoomData = dataState.data!!

                    chatViewModel.setStateEvent(ChatStateEvent.GetChatRoomsFromLocal)
                    navigateToChatting(chatRoomData)
                }
                is DataState.Error -> {
                    Log.i(TAG, "addChatRoom: ${dataState.exception}")
                }
                is DataState.NoData -> {
                    Log.i(TAG, "addChatRoom: no data")
                }
            }
        })
    }

    private fun setInitView() {
        Glide.with(requireContext()).load(friendData.profileImageUrl).into(profileIV)
        nameTV.text = friendData.name
    }

    private fun setListener() {
        closeBtn.setOnClickListener(this)
        chatBtn.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.closeBtn -> requireActivity().onBackPressed()
            R.id.chatBtn -> {
                if (existedChatRoom == null) {
                    val userData = userViewModel.userData.value!!
                    val participants = ArrayList<FriendData>().apply { add(friendData) }

                    chatViewModel.setStateEvent(ChatStateEvent.AddChatRoom(userData, participants))
                } else {
                    navigateToChatting(existedChatRoom!!)
                }
            }
        }
    }

    private fun navigateToChatting(chatRoom : ChatRoomData) {
        val action = ProfileFriendFragmentDirections.actionProfileFriendFragmentToChattingFragment(chatRoom)
        homeDetailNavController.navigate(action)
        when (homeNavController.currentDestination?.id) {
            R.id.friendsListFragment -> homeNavController.navigate(R.id.action_friendsListFragment_to_chatListFragment)
            else -> Log.i(TAG, "onConfirm: ${homeNavController.currentDestination}")
        }
    }
}