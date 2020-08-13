package org.personal.videotogether.view.fragments.nestonmain

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.UserData
import org.personal.videotogether.util.DataState
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.util.view.ViewHandler
import org.personal.videotogether.viewmodel.UserStateEvent
import org.personal.videotogether.viewmodel.UserViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SignInFragment
constructor(
    private val dataStateHandler: DataStateHandler,
    private val viewHandler: ViewHandler
) : Fragment(R.layout.fragment_sign_in), View.OnClickListener {

    private val TAG = javaClass.name

    private lateinit var navController: NavController
    private val viewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
//        navController.navigate(R.id.action_signInFragment_to_mainHomeFragment) // TODO : 테스트 용
        subscribeObservers()
        setListener()
    }

    private fun subscribeObservers() {
        // live data : 로그인 확인
        viewModel.signInState.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Loading -> {
                    dataStateHandler.displayLoadingDialog(true, childFragmentManager)

                }
                is DataState.Success<UserData?> -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    navController.navigate(R.id.action_signInFragment_to_mainHomeFragment)
                }
                is DataState.NoData -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    viewHandler.handleWarningText(checkInputTV)
                }
                is DataState.Error -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
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
        when (view?.id) {

            R.id.signInBtn -> {
                val email = emailET.text.toString()
                val password = passwordET.text.toString()
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