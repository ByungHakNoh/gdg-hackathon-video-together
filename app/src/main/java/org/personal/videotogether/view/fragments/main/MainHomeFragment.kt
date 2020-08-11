package org.personal.videotogether.view.fragments.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_main_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainHomeFragment : Fragment(R.layout.fragment_main_home) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val nestedNavHostFragment = childFragmentManager.findFragmentById(R.id.homeFragmentContainer)
        val homeNavController = nestedNavHostFragment!!.findNavController()
        bottomNavigationBN.setupWithNavController(homeNavController)
    }
}