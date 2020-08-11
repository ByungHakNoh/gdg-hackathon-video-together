package org.personal.videotogether.view.fragments.user

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.util.DataState
import org.personal.videotogether.util.DataStateHandler
import org.personal.videotogether.viewmodel.UserStateEvent
import org.personal.videotogether.viewmodel.UserViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SignUpFragment
constructor(
    private val dataStateHandler: DataStateHandler
) : Fragment(R.layout.fragment_sign_up), View.OnClickListener, TextWatcher {

    private val TAG = javaClass.name

    private lateinit var navController: NavController
    private val viewModel: UserViewModel by viewModels()

    // 이메일, 패스워드 유효한지 확인 -> 회원가입 재확인할 때 사용
    private var isEmailValid = false
    private var isPasswordValid = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        subscribeObservers()
        setListener()
    }

    // 옵저버 등록 (이메일 중복 검사 관련)
    private fun subscribeObservers() {
        // live data : 이메일 중복 확인
        viewModel.validationDataState.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<Boolean?> -> {
                    checkDuplicatedEmail(dataState.data!!)
                }
                is DataState.Error -> {
                    displayError(dataState.exception.message)
                }
            }
        })
        // live data : 회원 정보 서버로 업로드 확인
        viewModel.uploadUserDataState.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<Boolean?> -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    navController.navigate(R.id.action_signUpFragment_to_setProfileFragment)
                }
                is DataState.ResponseError -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    Log.i(TAG, "subscribeObservers: ${dataState.serverError}")
                }
                is DataState.Error -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    displayError(dataState.exception.message)
                }
                is DataState.Loading -> {
                    dataStateHandler.displayLoadingDialog(true, childFragmentManager)
                }
            }
        })
    }

    // 이메일 중복 검사결과를 서버에서 받아 상태 업데이트
    private fun checkDuplicatedEmail(isValid: Boolean) {

        isEmailValid = if (isValid) {
            changeValidationStyle(emailValidationTV, R.string.valid, R.color.green)
            true
        } else {
            changeValidationStyle(emailValidationTV, R.string.email_duplicated, R.color.red)
            false
        }
    }

    // 서버통신에 에러가 발생했을 때 로그만 찍어준다 -> product 에서는 사용자에게 보여줄 수 있도록...
    private fun displayError(message: String?) {
        if (message == null) {
            Log.i(TAG, "displayError: unknown error")
        } else {
            Log.i(TAG, "displayError: $message")
        }
    }

    // 리스너 등록
    private fun setListener() {
        emailET.addTextChangedListener(this)
        passwordET.addTextChangedListener(this)
        signUpBtn.setOnClickListener(this)
    }

    // ------------------ 클릭 이벤트 메소드 모음 ------------------
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.signUpBtn -> {

                if (isEmailValid) {
                    if (isPasswordValid) {
                        val userEmail = emailET.text.toString()
                        val userPassword = passwordET.text.toString()

                        // 서버에 유저 업로드 요청
                        viewModel.setStateEvent(UserStateEvent.UploadUser(userEmail, userPassword))
                    } else {
                        Toast.makeText(requireContext(), getText(R.string.check_password), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), getText(R.string.check_email), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ------------------ 이메일 및 비밀번호 메소드 모음 ------------------
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    // 이메일과 패스워드 유효성 검사
    override fun afterTextChanged(s: Editable?) {
        when (s.hashCode()) {
            emailET.text.hashCode() -> {
                val isEmailFormatValid = android.util.Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()
                emailValidationTV.visibility = View.VISIBLE

                if (isEmailFormatValid) {
                    viewModel.setStateEvent(UserStateEvent.CheckDuplicatedEmail(s.toString()))
                } else {
                    changeValidationStyle(emailValidationTV, R.string.email_invalid, R.color.red)
                }
            }

            passwordET.text.hashCode() -> {
                val regex = Regex("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@!%*#?&]).{8,15}.\$")
                PWValidationTV.visibility = View.VISIBLE

                isPasswordValid = if (s.toString().matches(regex)) {
                    changeValidationStyle(PWValidationTV, R.string.valid, R.color.green)
                    true
                } else {
                    changeValidationStyle(PWValidationTV, R.string.password_validation, R.color.red)
                    false
                }
            }
        }
    }

    // 사용자에게 이메일, 비밀번호 유효성 검사 결과를 알려주는 TextView 스타일 변경
    private fun changeValidationStyle(textView: TextView, text: Int, color: Int) {
        textView.text = getText(text)
        textView.setTextColor(getColor(resources, color, null))
    }
}