package org.personal.videotogether.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.util.view.ImageHandler
import org.personal.videotogether.util.view.ViewHandler
import org.personal.videotogether.view.fragments.main.friendlist.AddFriendFragment
import org.personal.videotogether.view.fragments.user.SetProfileFragment
import org.personal.videotogether.view.fragments.user.SignInFragment
import org.personal.videotogether.view.fragments.user.SignUpFragment
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MainFragmentFactory
@Inject
constructor(
    private val dataStateHandler: DataStateHandler,
    private val imageHandler: ImageHandler,
    private val viewHandler: ViewHandler
) : FragmentFactory() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {

        return when (className) {
            SignInFragment::class.java.name -> {
                SignInFragment(dataStateHandler, viewHandler)
            }

            SignUpFragment::class.java.name -> {
                SignUpFragment(dataStateHandler)
            }

            SetProfileFragment::class.java.name -> {
                SetProfileFragment(dataStateHandler, imageHandler)
            }

            AddFriendFragment::class.java.name -> {
                AddFriendFragment(dataStateHandler, imageHandler, viewHandler)
            }

            else -> super.instantiate(classLoader, className)
        }
    }
}