package org.personal.videotogether.view.fragments.home.nestonvideo.videotogether.nestonvideotogether

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class VideoTogetherFragment : Fragment(R.layout.fragment_video_together) {

    private val TAG by lazy { javaClass.name }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated: created")
    }
}