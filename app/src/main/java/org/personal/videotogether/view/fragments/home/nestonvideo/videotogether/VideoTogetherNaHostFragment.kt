package org.personal.videotogether.view.fragments.home.nestonvideo.videotogether

import android.content.Context
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class VideoTogetherNaHostFragment : NavHostFragment() {

    @Inject
    lateinit var fragmentFactory: VideoTogetherFragmentFactory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        childFragmentManager.fragmentFactory = fragmentFactory
    }
}