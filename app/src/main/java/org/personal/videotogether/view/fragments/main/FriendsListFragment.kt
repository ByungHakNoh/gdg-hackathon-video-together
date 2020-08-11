package org.personal.videotogether.view.fragments.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_friends_list.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FriendsListFragment : Fragment(R.layout.fragment_friends_list), View.OnClickListener {

    private val TAG = javaClass.name

    private lateinit var mainNavController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainFragmentContainer: FragmentContainerView = view.rootView.findViewById(R.id.mainFragmentContainer)
        mainNavController = Navigation.findNavController(mainFragmentContainer)
        setListener()
    }

    private fun setListener() {
        toChatBtn.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.toChatBtn -> {
                mainNavController.navigate(R.id.action_mainHomeFragment_to_chattingFragment)
            }
        }
    }
}