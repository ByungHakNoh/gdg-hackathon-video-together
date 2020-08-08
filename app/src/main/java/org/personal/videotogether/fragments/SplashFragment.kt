package org.personal.videotogether.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_splash.*
import org.personal.videotogether.R

class SplashFragment : Fragment() {

    private val TAG = javaClass.name

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        splashAnimLA.playAnimation() // 로띠 애니메이션 재생

        // TODO : splash 시간 정하기
        Handler(Looper.getMainLooper()).postDelayed({
            Log.i(TAG, "onViewCreated: handler working")
            navController.navigate(R.id.action_splashFragment_to_signInFragment)
        }, 0)
    }
}