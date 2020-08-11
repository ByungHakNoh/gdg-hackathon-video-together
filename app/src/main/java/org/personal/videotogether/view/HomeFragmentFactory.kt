package org.personal.videotogether.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.util.DataStateHandler
import org.personal.videotogether.util.ImageHandler
import javax.inject.Inject

@ExperimentalCoroutinesApi
class HomeFragmentFactory
@Inject
constructor(
    private val dataStateHandler: DataStateHandler,
    private val imageHandler : ImageHandler
) : FragmentFactory() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {

        return when (className) {


            else -> super.instantiate(classLoader, className)
        }
    }
}