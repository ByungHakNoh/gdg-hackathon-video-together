package org.personal.videotogether.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.util.DataStateHandler
import org.personal.videotogether.util.ImageHandler
import org.personal.videotogether.view.fragments.user.SetProfileFragment
import org.personal.videotogether.view.fragments.user.SignUpFragment
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MainFragmentFactory
@Inject
constructor(
    private val dataStateHandler: DataStateHandler,
    private val imageHandler : ImageHandler
) : FragmentFactory() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {

        return when (className) {
            SignUpFragment::class.java.name -> {
                SignUpFragment(dataStateHandler)
            }

            SetProfileFragment::class.java.name -> {
                SetProfileFragment(dataStateHandler, imageHandler)
            }

            else -> super.instantiate(classLoader, className)
        }
    }
}