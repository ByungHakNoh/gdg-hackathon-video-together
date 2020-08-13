package org.personal.videotogether.view.fragments.nestonmain

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_splash.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val TAG = javaClass.name

    private lateinit var mainNavController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainNavController = Navigation.findNavController(view)
        splashAnimLA.playAnimation() // 로띠 애니메이션 재생

        // TODO : splash 시간 정하기
        Handler(Looper.getMainLooper()).postDelayed({
            mainNavController.navigate(R.id.action_splashFragment_to_signInFragment)
        }, 0)
    }
}