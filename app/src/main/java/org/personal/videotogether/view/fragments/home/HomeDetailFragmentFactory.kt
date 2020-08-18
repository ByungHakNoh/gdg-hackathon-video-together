package org.personal.videotogether.view.fragments.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.util.view.ImageHandler
import org.personal.videotogether.util.view.ViewHandler
import org.personal.videotogether.view.fragments.home.nestonhomedetail.SelectFriendFragment
import org.personal.videotogether.view.fragments.home.nestonhomedetail.AddFriendFragment
import javax.inject.Inject

@ExperimentalCoroutinesApi
class HomeDetailFragmentFactory
@Inject
constructor(
    private val dataStateHandler: DataStateHandler,
    private val imageHandler: ImageHandler,
    private val viewHandler: ViewHandler
) : FragmentFactory() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {

        return when (className) {
            AddFriendFragment::class.java.name -> {
                AddFriendFragment(dataStateHandler, imageHandler, viewHandler)
            }

            SelectFriendFragment::class.java.name -> {
                SelectFriendFragment(dataStateHandler)
            }

            else -> super.instantiate(classLoader, className)
        }
    }
}