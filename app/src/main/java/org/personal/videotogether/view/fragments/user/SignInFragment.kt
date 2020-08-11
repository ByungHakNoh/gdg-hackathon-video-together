package org.personal.videotogether.view.fragments.user

import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.util.DataState
import org.personal.videotogether.viewmodel.UserStateEvent
import org.personal.videotogether.viewmodel.UserViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SignInFragment : Fragment(R.layout.fragment_sign_in), View.OnClickListener {

    private val TAG = javaClass.name

    private lateinit var navController: NavController
    private val viewModel: UserViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        subscribeObservers()
        setListener()
    }

    private fun subscribeObservers() {
        // live data : 로그인 확인
        viewModel.signInState.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<Boolean?> -> {
                    navController.navigate(R.id.action_signInFragment_to_mainHomeFragment)
                }
                is DataState.ResponseError -> {
                    checkInputTV.visibility = View.VISIBLE

                    Handler(Looper.getMainLooper()).postDelayed({
                        checkInputTV.visibility = View.INVISIBLE
                    }, 1500)
                }
                is DataState.Error -> {
                    Log.i(TAG, "subscribeObservers: ${dataState.exception}")
                }
            }
        })
    }

    private fun setListener() {
        signInBtn.setOnClickListener(this)
        signUpTV.setOnClickListener(this)
    }


    override fun onClick(view: View?) {
        when(view?.id) {

            R.id.signInBtn -> {
                val email = emailET.text.toString()
                val password =  passwordET.text.toString()
                if (email.trim() != "") {
                    if (password.trim() != "") {
                        viewModel.setStateEvent(UserStateEvent.SignIn(email, password))
                    } else {
                        Toast.makeText(requireContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.signUpTV -> {
                navController.navigate(R.id.action_signInFragment_to_signUpFragment)
            }
        }
    }
}