package org.personal.videotogether.view.fragments.home.nestonvideo

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.views.YouTubePlayerSeekBarListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_video_play.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.repository.SocketRepository.Companion.EXIT_YOUTUBE_ROOM
import org.personal.videotogether.repository.SocketRepository.Companion.SEND_VISITOR_PLAYER_STATE
import org.personal.videotogether.repository.SocketRepository.Companion.SEND_YOUTUBE_PLAYER_STATE
import org.personal.videotogether.repository.SocketRepository.Companion.SYNC_YOUTUBE_PLAYER
import org.personal.videotogether.viewmodel.SocketStateEvent
import org.personal.videotogether.viewmodel.SocketViewModel
import org.personal.videotogether.viewmodel.YoutubeStateEvent
import org.personal.videotogether.viewmodel.YoutubeViewModel


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class VideoPlayFragment : Fragment(R.layout.fragment_video_play), View.OnClickListener, YouTubePlayerListener, MotionLayout.TransitionListener, YouTubePlayerSeekBarListener {

    private val TAG by lazy { javaClass.name }

    private lateinit var videoDetailNavController: NavController
    private lateinit var homeDetailNavController: NavController

    private lateinit var backPressCallback: OnBackPressedCallback // 뒤로가기 callback

    // 뷰 모델
    private val youtubeViewModel: YoutubeViewModel by lazy { ViewModelProvider(requireActivity())[YoutubeViewModel::class.java] }
    private val socketViewModel by lazy { ViewModelProvider(requireActivity())[SocketViewModel::class.java] }

    // 유투브 플레이어 관련 변수
    private lateinit var youtubePlayer: YouTubePlayer
    private var isYoutubePlaying = false
    private var isVideoTogetherOn = false
    private var playerTime = 0f

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homeDetailFragmentContainer: FragmentContainerView = view.rootView.findViewById(R.id.homeDetailFragmentContainer)
        val videoDetailFragmentContainer: FragmentContainerView = view.rootView.findViewById(R.id.videoDetailContainer)

        homeDetailNavController = Navigation.findNavController(homeDetailFragmentContainer)
        videoDetailNavController = Navigation.findNavController(videoDetailFragmentContainer)

        setBackPressCallback(view as MotionLayout)
        setListener(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backPressCallback.remove()
    }

    // TODO : 뒤로가기 설정 좀 더 나은 방법 생각해보기
    @SuppressLint("RestrictedApi")
    private fun setBackPressCallback(motionLayout: MotionLayout) {
        backPressCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // 유투브 같이 보기 시 SelectFriendList 로 이동하기 때문에 백스텍 확인
            when {
                homeDetailNavController.backStack.count() > 2 -> homeDetailNavController.popBackStack()
                else -> {
                    motionLayout.transitionToStart()
                    isEnabled = false
                }
            }
        }
        // 유투브 플레이어가 visible 일 때만 동작하도록 초기 값은 false
        backPressCallback.isEnabled = false
    }

    private fun subscribeObservers() {
        youtubeViewModel.currentPlayedYoutube.observe(viewLifecycleOwner, Observer { youtubeData ->
            if (youtubeData != null) {
                videoTitleTV.text = youtubeData.title
                channelTitleTV.text = youtubeData.channelTitle
                Log.i(TAG, "youtube title: ${videoTitleTV.text}")
                Log.i(TAG, "youtube title: ${channelTitleTV.text}")

                youtubePlayer.cueVideo(youtubeData.videoId, 0f)
                youtubePlayer.play()
            }
        })

        youtubeViewModel.setVideoTogether.observe(viewLifecycleOwner, Observer { videoTogetherState ->
            if (videoTogetherState != null) isVideoTogetherOn = videoTogetherState
        })

        socketViewModel.youtubePlayerState.observe(viewLifecycleOwner, Observer { playerStateData ->
            if (playerStateData != null) {
                when (playerStateData.state) {
                    "play" -> youtubePlayer.play()
                    "pause" -> youtubePlayer.pause()
                    "seek" -> {
                        youtubePlayer.seekTo(playerStateData.currentSecond)
                        Log.i(TAG, "youtubePlayerState: seek ${playerStateData.currentSecond}")
                    }
                }
            }
        })

        socketViewModel.youtubeJoinRoomData.observe(viewLifecycleOwner, Observer { youtubeJoinRoomData ->
            if (youtubeJoinRoomData != null) {
                Log.i(TAG, "youtubeJoinRoomData: $youtubeJoinRoomData")
                when (youtubeJoinRoomData.flag) {
                    "empty" -> {
                        Toast.makeText(requireContext(), "방이 더이상 존재하지 않습니다", Toast.LENGTH_SHORT).show()
                    }
                    "visitorJoin" -> {
                        socketViewModel.setStateEvent(
                            SocketStateEvent.SendToTCPServer(
                                SEND_VISITOR_PLAYER_STATE, "seek", "$playerTime@${youtubeJoinRoomData.userId}"
                            )
                        )
                    }
                }
            }
        })
    }

    private fun setListener(view: View) {
        (view as MotionLayout).setTransitionListener(this) // 모션 레이아웃 리스너
        youtubePlayerYP.addYouTubePlayerListener(this) // 유투브 플레이어 리스너
        youtubeSeekBarSB.youtubePlayerSeekBarListener = this
        // 버튼 리스너
        playerControlBtn.setOnClickListener(this)
        closePlayerBtn.setOnClickListener(this)
    }

    // ------------------ 클릭 리스너 메소드 모음 ------------------
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.playerControlBtn -> if (isYoutubePlaying) youtubePlayer.pause() else youtubePlayer.play()
            R.id.closePlayerBtn -> {
                // 닫기 버튼 누르면 유투브 일시정지
                // TODO : 같이보기 상태인지 확인
                youtubePlayer.pause()
                youtubeViewModel.setStateEvent(YoutubeStateEvent.SetFrontPlayer(null))

                // 같이보기 켜져있으면 같이 끄기
                if (isVideoTogetherOn) {
                    socketViewModel.setStateEvent(SocketStateEvent.SendToTCPServer(EXIT_YOUTUBE_ROOM))
                    youtubeViewModel.setStateEvent(YoutubeStateEvent.SetVideoTogether(false))
                }
            }
        }
    }

    // ------------------ 유투브 플레이어 리스너 메소드 모음 ------------------
    // 유투브 영상 준비되었을 때
    override fun onReady(youTubePlayer: YouTubePlayer) {
        youtubePlayer = youTubePlayer

        if (youtubeSeekBarSB != null) youtubePlayer.addListener(youtubeSeekBarSB)

        subscribeObservers()
    }

    // 영상 상태가 변했을 때 : 재생 여부 확인 후 재생 버튼 상태 변화 시켜줌
    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
        when (state) {
            PlayerConstants.PlayerState.PLAYING -> {
                handlePlayerState(R.drawable.ic_baseline_pause_24, true, "play")
                checkIfJoining()
            }

            PlayerConstants.PlayerState.PAUSED -> {
                handlePlayerState(R.drawable.ic_baseline_play_arrow_24, false, "pause")
            }
            else -> Log.i(TAG, "onStateChange: $state")
        }
    }

    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
        if (isVideoTogetherOn) playerTime = second
    }

    // 플레이 상태 조절하는 메소드
    private fun handlePlayerState(playBtnImage: Int, isPlaying: Boolean, stateMessage: String) {
        if (playerControlBtn != null) {
            playerControlBtn.setImageResource(playBtnImage)
            isYoutubePlaying = isPlaying

            checkIfJoining()
            // 같이보기 켜져있으면 소켓으로 정보 보내기
            if (isVideoTogetherOn) {
                socketViewModel.setStateEvent(SocketStateEvent.SendToTCPServer(SEND_YOUTUBE_PLAYER_STATE, stateMessage, "0"))
            }
        }
    }

    // 비디오 같이보기 참여한건지 확인
    private fun checkIfJoining() {
        val isJoiningVideoTogether = youtubeViewModel.isJoiningVideoTogether.value
        if (isJoiningVideoTogether != null) {
            if (isJoiningVideoTogether) {
                socketViewModel.setStateEvent(SocketStateEvent.SendToTCPServer(SYNC_YOUTUBE_PLAYER))
                youtubeViewModel.setStateEvent(YoutubeStateEvent.SetJoiningVideoTogether(false))
            }
        }
    }

    override fun onApiChange(youTubePlayer: YouTubePlayer) {}
    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {}
    override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, playbackQuality: PlayerConstants.PlaybackQuality) {}
    override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: PlayerConstants.PlaybackRate) {}
    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {}
    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {}
    override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {}

    // ------------------ 모션 레이아웃 리스너 메소드 모음 ------------------
    override fun onTransitionCompleted(motionLayout: MotionLayout?, p1: Int) {
        // 플레이이어가 펼쳐진 상태인지 여부 확인 후 뷰 모델 데이터 업데이트
        // 라이브 데이터는 홈에서 뒤로가기 버튼 누를 떄 사용
        when (motionLayout!!.currentState) {
            R.id.start -> backPressCallback.isEnabled = true
            R.id.end -> backPressCallback.isEnabled = false
        }
    }

    override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
    override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}
    override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}
    override fun seekTo(time: Float) {
        youtubePlayer.seekTo(time)

        // 같이보기 켜져있으면 소켓으로 정보 보내기
        if (isVideoTogetherOn) {
            socketViewModel.setStateEvent(SocketStateEvent.SendToTCPServer(SEND_YOUTUBE_PLAYER_STATE, "seek", time.toString()))
        }
    }
}