package org.personal.videotogether.view.fragments.home.nestonvideo.videodetail.nestonvideodetail

import android.os.Bundle
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
import kotlinx.android.synthetic.main.fragment_video_detail.*
import kotlinx.android.synthetic.main.fragment_video_detail.participantsCountTV
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.ChatData
import org.personal.videotogether.domianmodel.YoutubeData
import org.personal.videotogether.domianmodel.YoutubePageData
import org.personal.videotogether.repository.SocketRepository.Companion.EXIT_YOUTUBE_ROOM
import org.personal.videotogether.repository.SocketRepository.Companion.SEND_YOUTUBE_MESSAGE
import org.personal.videotogether.util.DataState
import org.personal.videotogether.view.adapter.ChatAdapter
import org.personal.videotogether.view.adapter.ItemClickListener
import org.personal.videotogether.view.adapter.YoutubeAdapter
import org.personal.videotogether.viewmodel.*
import java.lang.Exception

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class VideoDetailFragment : Fragment(R.layout.fragment_video_detail), ItemClickListener, View.OnClickListener {

    private val TAG by lazy { javaClass.name }

    private lateinit var videoDetailNavController: NavController
    private lateinit var homeDetailNavController: NavController

    private val userViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val youtubeViewModel: YoutubeViewModel by lazy { ViewModelProvider(requireActivity())[YoutubeViewModel::class.java] }
    private val socketViewModel by lazy { ViewModelProvider(requireActivity())[SocketViewModel::class.java] }

    // 리사이클러 뷰
    private val youtubeList by lazy { ArrayList<YoutubeData>() }
    private val youtubeAdapter by lazy { YoutubeAdapter(this, youtubeList, this) }

    private val chatList by lazy { ArrayList<ChatData>() }
    private val chatAdapter: ChatAdapter by lazy { ChatAdapter(requireContext(), userViewModel.userData.value!!.id, chatList) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 네비게이션 설정
        val homeDetailFragmentContainer: FragmentContainerView = view.rootView.findViewById(R.id.homeDetailFragmentContainer)
        videoDetailNavController = Navigation.findNavController(view)
        homeDetailNavController = Navigation.findNavController(homeDetailFragmentContainer)

        subscribeObservers()
        setListener()
    }

    private fun subscribeObservers() {

        // 로컬에서 유저데이터를 가져오고 난 후에 리사이클러 뷰 만들기 (채팅 어뎁터에 유저 id 필요하기 때문)
        userViewModel.userData.observe(viewLifecycleOwner, Observer { userData ->
            if (userData != null) buildRecyclerView()
        })

        youtubeViewModel.currentPlayedYoutube.observe(viewLifecycleOwner, Observer { youtubeData ->
            if (youtubeData != null) {
                expandedVideoTitleTV.text = youtubeData.title
                expandedChannelTitleTV.text = youtubeData.channelTitle
            }
        })

        youtubeViewModel.youtubeDefaultPage.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Loading -> {
                    Log.i(TAG, "youtubeList: Loading")
                }
                is DataState.Success<YoutubePageData?> -> {
                    youtubeList.clear()
                    dataState.data!!.youtubeDataList.forEach { youtubeList.add(it) }
                    youtubeAdapter.notifyDataSetChanged()
                }
                is DataState.NoData -> {
                    Log.i(TAG, "youtubeList: NoData")
                }
                is DataState.Error -> {
                    Log.i(TAG, "youtubeList: Error")
                }
            }
        })

        socketViewModel.youtubeJoinRoomData.observe(viewLifecycleOwner, Observer { youtubeJoinRoomData ->
            if (youtubeJoinRoomData != null) {
                Log.i(TAG, "youtubeJoinRoomData: video detail -  $youtubeJoinRoomData")
                when (youtubeJoinRoomData.flag) {
                    "participantCount" -> {
                        Log.i(TAG, "youtubeJoinRoomData: participantCount -  $youtubeJoinRoomData")
                        participantsCountTV.text = youtubeJoinRoomData.participantCount.toString()
                    }
                }
            }
        })

        youtubeViewModel.setVideoTogether.observe(viewLifecycleOwner, Observer { isVideoTogetherOn ->
            Log.i(TAG, "setVideoTogether: -  $isVideoTogetherOn")
            if (isVideoTogetherOn != null) {
                if (isVideoTogetherOn) {
                    videoTogetherChatCL.visibility = View.VISIBLE
                } else {
                    // TODO : 가끔 에러가 발생 원인 확인하기
                    try {
                        if (userViewModel.userData.value != null) {
                            videoTogetherChatCL.visibility = View.GONE
                            chatList.clear()
                            chatAdapter.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                        Log.i(TAG, "subscribeObservers: $e")
                    }
                }
            }
        })

        socketViewModel.youtubeChatMessage.observe(viewLifecycleOwner, Observer { chatData ->
            if (chatData != null) {
                chatList.add(chatData)
                chatAdapter.notifyItemInserted(chatList.size - 1)
                youtubeChatBoxRV.scrollToPosition(chatAdapter.itemCount - 1)
            }
        })
    }

    private fun setListener() {
        videoTogetherIB.setOnClickListener(this)
        closeBtn.setOnClickListener(this)
        sendBtn.setOnClickListener(this)
    }

    private fun buildRecyclerView() {
        val playListLayoutManager = LinearLayoutManager(requireContext())
        val chatLayoutManager = LinearLayoutManager(requireContext())

        playListRV.layoutManager = playListLayoutManager
        playListRV.adapter = youtubeAdapter

        youtubeChatBoxRV.layoutManager = chatLayoutManager
        youtubeChatBoxRV.adapter = chatAdapter
    }


    // ------------------ 클릭 리스너 메소드 모음 ------------------
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.videoTogetherIB -> {
                homeDetailNavController.navigate(R.id.action_homeDetailBlankFragment_to_selectChatRoomFragment)
            }

            // TODO : 다이얼로그로 물어보기 추가하자
            R.id.closeBtn -> {
                socketViewModel.setStateEvent(SocketStateEvent.SendToTCPServer(EXIT_YOUTUBE_ROOM))
                youtubeViewModel.setStateEvent(YoutubeStateEvent.SetVideoTogether(false))
            }

            R.id.sendBtn -> {
                val message = chattingInputED.text.toString()
                if (message.trim().isNotEmpty()) {
                    val userData = userViewModel.userData.value!!

                    socketViewModel.setStateEvent(SocketStateEvent.SendToTCPServer(SEND_YOUTUBE_MESSAGE, userData.id.toString(), message))
                    chattingInputED.text = null
                }
            }
        }
    }

    // ------------------ 리사이클러뷰 아이템 클릭 리스너 메소드 모음 ------------------
    override fun onItemClick(view: View?, itemPosition: Int) {
        val youtubeData = youtubeList[itemPosition]
        youtubeViewModel.setStateEvent(YoutubeStateEvent.SetFrontPlayer(youtubeData))
    }
}