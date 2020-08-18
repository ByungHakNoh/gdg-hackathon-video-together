package org.personal.videotogether.view.fragments.home.nestonvideo.videodetail

import android.content.Context
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class VideoDetailNaHostFragment : NavHostFragment() {

    @Inject
    lateinit var fragmentFactory: VideoDetailFragmentFactory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        childFragmentManager.fragmentFactory = fragmentFactory
    }
}