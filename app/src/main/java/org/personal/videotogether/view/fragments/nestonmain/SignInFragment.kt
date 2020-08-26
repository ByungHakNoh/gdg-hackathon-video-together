package org.personal.videotogether.view.fragments.nestonmain

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.withContext
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.UserData
import org.personal.videotogether.util.DataState
import org.personal.videotogether.util.SharedPreferenceHelper
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.util.view.ViewHandler
import org.personal.videotogether.viewmodel.SocketStateEvent
import org.personal.videotogether.viewmodel.SocketViewModel
import org.personal.videotogether.viewmodel.UserStateEvent
import org.personal.videotogether.viewmodel.UserViewModel
import java.lang.Error

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SignInFragment
constructor(
    private val dataStateHandler: DataStateHandler,
    private val viewHandler: ViewHandler,
    private val sharedPreferenceHelper: SharedPreferenceHelper
) : Fragment(R.layout.fragment_sign_in), View.OnClickListener {

    private val TAG = javaClass.name

    private lateinit var mainNavController: NavController
    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val socketViewModel by lazy { ViewModelProvider(requireActivity())[SocketViewModel::class.java] }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainNavController = Navigation.findNavController(view)
        setBackPressCallback()
        checkAutoSignIn()
        subscribeObservers()
        setListener()
    }

    private fun setBackPressCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            socketViewModel.setStateEvent(SocketStateEvent.DisconnectFromTCPServer)
            remove()
            requireActivity().finish()
        }
    }

    // 자동 로그인 체크
    private fun checkAutoSignIn() {
        val isAutoSignIn = sharedPreferenceHelper.getBoolean(requireContext(), getString(R.string.auto_sign_in_key))

        if (isAutoSignIn) {
            mainNavController.navigate(R.id.action_signInFragment_to_mainHomeFragment)
        }
    }

    private fun subscribeObservers() {
        // live data : 로그인 확인
        userViewModel.signInState.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Loading -> {
                    dataStateHandler.displayLoadingDialog(true, childFragmentManager)
                }
                // 다이얼로그는 파이어베이스 업로드 완료 후 제거
                is DataState.Success<UserData?> -> {
                    val userId = dataState.data!!.id
                    val firebaseToken = sharedPreferenceHelper.getString(requireContext(), getString(R.string.firebase_messaging_token))

                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    sharedPreferenceHelper.setBoolean(requireContext(), getString(R.string.auto_sign_in_key), true)
                    userViewModel.setStateEvent(UserStateEvent.UploadFirebaseToken(userId, firebaseToken!!))
                    mainNavController.navigate(R.id.action_signInFragment_to_mainHomeFragment)
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

        userViewModel.uploadFirebaseToken.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<Boolean?> -> {
                    Log.i(TAG, "subscribeObservers: 파이어베이스 토큰 업로드 - 새로운 유저")
                }
                is DataState.DuplicatedData -> {
                    Log.i(TAG, "subscribeObservers: 파이어베이스 토큰 업로드 - 이미 존재 Or ")
                }
                is DataState.Error -> {
                    Log.i(TAG, "subscribeObservers: 파이어베이스 토큰 업로드 - 에러 발생")
                }
            }
        })
    }

    private fun setListener() {
        signInBtn.setOnClickListener(this)
        signUpTV.setOnClickListener(this)
    }

    // ------------------ 클릭 이벤트 리스너 ------------------
    override fun onClick(view: View?) {
        when (view?.id) {

            R.id.signInBtn -> {
                val email = emailET.text.toString()
                val password = passwordET.text.toString()
                if (email.trim() != "") {
                    if (password.trim() != "") {
                        userViewModel.setStateEvent(UserStateEvent.SignIn(email, password))
                    } else {
                        Toast.makeText(requireContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.signUpTV -> {
                mainNavController.navigate(R.id.action_signInFragment_to_signUpFragment)
            }
        }
    }
}