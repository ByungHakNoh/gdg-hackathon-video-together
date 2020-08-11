package org.personal.videotogether.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SignInFragment : Fragment(R.layout.fragment_sign_in), View.OnClickListener {

    private val TAG = javaClass.name

    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        setListener()
    }

    private fun setListener() {
        signInBtn.setOnClickListener(this)
        signUpTV.setOnClickListener(this)
    }


    override fun onClick(view: View?) {
        when(view?.id) {

            R.id.signInBtn -> {
                Log.i(TAG, "onClick: 로그인 구현하기")
            }

            R.id.signUpTV -> {
                navController.navigate(R.id.action_signInFragment_to_signUpFragment)
            }
        }
    }
}