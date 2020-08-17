package org.personal.videotogether.view.fragments.home.nestonvideo

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_video_play.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.viewmodel.SocketViewModel
import org.personal.videotogether.viewmodel.YoutubeStateEvent
import org.personal.videotogether.viewmodel.YoutubeViewModel


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class VideoPlayFragment : Fragment(R.layout.fragment_video_play), View.OnClickListener, YouTubePlayerListener, MotionLayout.TransitionListener {

    private val TAG by lazy { javaClass.name }

    private val youtubeViewModel: YoutubeViewModel by lazy { ViewModelProvider(requireActivity())[YoutubeViewModel::class.java] }
    private val socketViewModel by lazy { ViewModelProvider(requireActivity())[SocketViewModel::class.java] }

    private lateinit var youtubePlayer: YouTubePlayer
    private var isYoutubePlaying = false
    private lateinit var backPressCallback: OnBackPressedCallback

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setBackPressCallback(view as MotionLayout)
        setListener(view)
    }

    private fun subscribeObservers() {
        youtubeViewModel.currentPlayedYoutube.observe(viewLifecycleOwner, Observer { youtubeData ->
            if (youtubeData != null) {
                videoTitleTV.text = youtubeData.title
                channelTitleTV.text = youtubeData.channelTitle

                youtubePlayer.cueVideo(youtubeData.videoId, 0f)
                youtubePlayer.play()
                isYoutubePlaying = true
            }
        })
    }

    private fun setBackPressCallback(motionLayout: MotionLayout) {
        backPressCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            motionLayout.transitionToStart()
            isEnabled = false
        }
    }

    private fun setListener(view: View) {
        (view as MotionLayout).setTransitionListener(this)
        playerControlBtn.setOnClickListener(this)
        closePlayerBtn.setOnClickListener(this)
        youtubePlayerYP.addYouTubePlayerListener(this) // 유투브 플레이어 리스너
    }

    // ------------------ 클릭 리스너 메소드 모음 ------------------
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.playerControlBtn -> if (isYoutubePlaying) youtubePlayer.pause() else youtubePlayer.play()
            R.id.closePlayerBtn -> youtubeViewModel.setStateEvent(YoutubeStateEvent.SetFrontPlayer(null))
        }
    }

    // ------------------ 유투브 플레이어 리스너 메소드 모음 ------------------
    // 유투브 영상 준비되었을 때
    override fun onReady(youTubePlayer: YouTubePlayer) {
        youtubePlayer = youTubePlayer
        subscribeObservers()
    }

    // 영상 상태가 변했을 때 : 재생 여부 확인 후 재생 버튼 상태 변화 시켜줌
    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
        when (state) {
            PlayerConstants.PlayerState.PLAYING -> {
                if (playerControlBtn != null) {
                    playerControlBtn.setImageResource(R.drawable.ic_baseline_pause_24)
                    isYoutubePlaying = true
                }
            }

            PlayerConstants.PlayerState.PAUSED -> {
                if (playerControlBtn != null) {
                    playerControlBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    isYoutubePlaying = false
                }
            }
            PlayerConstants.PlayerState.UNSTARTED -> Log.i(TAG, "onStateChange: UNSTARTED")
            PlayerConstants.PlayerState.VIDEO_CUED -> Log.i(TAG, "onStateChange: VIDEO_CUED")
            PlayerConstants.PlayerState.BUFFERING -> Log.i(TAG, "onStateChange: BUFFERING")
            PlayerConstants.PlayerState.UNKNOWN -> Log.i(TAG, "onStateChange: UNKNOWN")
            PlayerConstants.PlayerState.ENDED -> Log.i(TAG, "onStateChange: ENDED")
        }
    }

    override fun onApiChange(youTubePlayer: YouTubePlayer) {}
    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {}
    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {}
    override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, playbackQuality: PlayerConstants.PlaybackQuality) {}
    override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: PlayerConstants.PlaybackRate) {}
    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {}
    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {}
    override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {}

    // ------------------ 유투브 플레이어 리스너 메소드 모음 ------------------
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
}