package org.personal.videotogether.view.fragments.nestonmain

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.util.DataState
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.viewmodel.UserStateEvent
import org.personal.videotogether.viewmodel.UserViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SignUpFragment
constructor(
    private val dataStateHandler: DataStateHandler
) : Fragment(R.layout.fragment_sign_up), View.OnClickListener, TextWatcher {

    private val TAG = javaClass.name

    private lateinit var mainNavController: NavController
    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }

    // 이메일, 패스워드 유효한지 확인 -> 회원가입 재확인할 때 사용
    private var isEmailValid = false
    private var isPasswordValid = false
    private var isPasswordCheckValid = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainNavController = Navigation.findNavController(view)
        subscribeObservers()
        setListener()
    }

    // 옵저버 등록 (이메일 중복 검사 관련)
    private fun subscribeObservers() {
        // live data : 이메일 중복 확인
        userViewModel.validationDataState.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Loading -> {
                    changeValidationStyle(emailValidationTV, R.string.email_check_loading, R.color.black)
                }
                is DataState.Success<Boolean?> -> {
                    checkDuplicatedEmail(dataState.data!!)
                }
                is DataState.Error -> {
                    dataStateHandler.displayError(dataState.exception.message)
                }
            }
        })
        // live data : 회원 정보 서버로 업로드 확인
        userViewModel.uploadUserDataState.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<Boolean?> -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    mainNavController.navigate(R.id.action_signUpFragment_to_setProfileFragment)
                }
                is DataState.NoData -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    Log.i(TAG, "subscribeObservers: ${dataState.serverError}")
                }
                is DataState.Error -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    dataStateHandler.displayError(dataState.exception.message)
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

    // 리스너 등록
    private fun setListener() {
        emailET.addTextChangedListener(this)
        passwordET.addTextChangedListener(this)
        passwordCheckET.addTextChangedListener(this)
        signUpBtn.setOnClickListener(this)
    }

    // ------------------ 클릭 이벤트 메소드 모음 ------------------
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.signUpBtn -> {

                if (isEmailValid) {
                    if (isPasswordValid) {
                        if (isPasswordCheckValid) {
                            val userEmail = emailET.text.toString()
                            val userPassword = passwordET.text.toString()

                            // 서버에 유저 업로드 요청
                            userViewModel.setStateEvent(UserStateEvent.UploadUser(userEmail, userPassword))
                        } else {
                            Toast.makeText(requireContext(), "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                        }
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
    override fun afterTextChanged(editor: Editable?) {
        when (editor.hashCode()) {
            emailET.text.hashCode() -> {
                val isEmailFormatValid = android.util.Patterns.EMAIL_ADDRESS.matcher(editor.toString()).matches()
                emailValidationTV.visibility = View.VISIBLE

                if (isEmailFormatValid) {
                    userViewModel.setStateEvent(UserStateEvent.CheckDuplicatedEmail(editor.toString()))
                } else {
                    changeValidationStyle(emailValidationTV, R.string.email_invalid, R.color.red)
                }
            }

            passwordET.text.hashCode() -> {
                val regex = Regex("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@!%*#?&]).{8,15}.\$")
                PWValidationTV.visibility = View.VISIBLE

                isPasswordValid = if (editor.toString().matches(regex)) {
                    changeValidationStyle(PWValidationTV, R.string.valid, R.color.green)
                    true
                } else {
                    changeValidationStyle(PWValidationTV, R.string.password_validation, R.color.red)
                    false
                }
                checkPasswordCheck(editor, passwordCheckET)
            }

            passwordCheckET.text.hashCode() -> {
                PWCheckValidationTV.visibility = View.VISIBLE
                checkPasswordCheck(editor, passwordET)
            }
        }
    }

    private fun checkPasswordCheck(editor: Editable?, textView: TextView) {
        isPasswordCheckValid = if (editor.toString() == textView.text.toString()) {
            changeValidationStyle(PWCheckValidationTV, R.string.valid, R.color.green)
            true
        } else {
            changeValidationStyle(PWCheckValidationTV, R.string.password_check_invalid, R.color.red)
            false
        }
    }

    // 사용자에게 이메일, 비밀번호 유효성 검사 결과를 알려주는 TextView 스타일 변경
    private fun changeValidationStyle(textView: TextView, text: Int, color: Int) {
        textView.text = getText(text)
        textView.setTextColor(getColor(resources, color, null))
    }
}