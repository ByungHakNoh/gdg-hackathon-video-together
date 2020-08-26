package org.personal.videotogether.view.fragments

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.util.SharedPreferenceHelper
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.util.view.ImageHandler
import org.personal.videotogether.util.view.ViewHandler
import org.personal.videotogether.view.fragments.nestonmain.HomeFragment
import org.personal.videotogether.view.fragments.nestonmain.SetProfileFragment
import org.personal.videotogether.view.fragments.nestonmain.SignInFragment
import org.personal.videotogether.view.fragments.nestonmain.SignUpFragment
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MainFragmentFactory
@Inject
constructor(
    private val dataStateHandler: DataStateHandler,
    private val imageHandler: ImageHandler,
    private val viewHandler: ViewHandler,
    private val sharedPreferenceHelper: SharedPreferenceHelper
) : FragmentFactory() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {

        return when (className) {
            SignInFragment::class.java.name -> {
                SignInFragment(dataStateHandler, viewHandler, sharedPreferenceHelper)
            }

            SignUpFragment::class.java.name -> {
                SignUpFragment(dataStateHandler)
            }

            SetProfileFragment::class.java.name -> {
                SetProfileFragment(dataStateHandler, imageHandler)
            }

            HomeFragment::class.java.name -> {
                HomeFragment(sharedPreferenceHelper)
            }

            else -> super.instantiate(classLoader, className)
        }
    }
}