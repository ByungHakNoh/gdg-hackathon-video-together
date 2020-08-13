package org.personal.videotogether.view.fragments.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.util.view.ImageHandler
import org.personal.videotogether.view.fragments.home.nestonhome.ChatListFragment
import org.personal.videotogether.view.fragments.home.nestonhome.FriendsListFragment
import javax.inject.Inject

@ExperimentalCoroutinesApi
class HomeFragmentFactory
@Inject
constructor(
    private val dataStateHandler: DataStateHandler
) : FragmentFactory() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {

        return when (className) {
            FriendsListFragment::class.java.name -> {
                FriendsListFragment(dataStateHandler)
            }

            ChatListFragment::class.java.name -> {
                ChatListFragment(dataStateHandler)
            }

            else -> super.instantiate(classLoader, className)
        }
    }
}