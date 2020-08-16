package org.personal.videotogether.view.fragments.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.util.view.DataStateHandler
import javax.inject.Inject

@ExperimentalCoroutinesApi
class VideoFragmentFactory
@Inject
constructor(
    private val dataStateHandler: DataStateHandler
) : FragmentFactory() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {

        return when (className) {


            else -> super.instantiate(classLoader, className)
        }
    }
}