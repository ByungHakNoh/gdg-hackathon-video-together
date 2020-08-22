package org.personal.videotogether.view.fragments.home.nestonhomedetail

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_profile_mine.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.viewmodel.UserViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProfileMineFragment : Fragment(R.layout.fragment_profile_mine), View.OnClickListener {

    private val TAG = javaClass.name

    private lateinit var homeDetailNavController: NavController
    private lateinit var backPressCallback: OnBackPressedCallback // 뒤로가기 버튼 콜백

    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeDetailNavController = Navigation.findNavController(view)

        setBackPressCallback()
        setInitView()
        setListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backPressCallback.remove()
    }

    private fun setBackPressCallback() {
        // 비디오 모션 레이아웃 관련 뒤로가기 버튼은 VideoPlayFragment 에서 관리
        // VideoPlayFragment 뒤로가기 콜백이 disable 되면 실행됨
        backPressCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            homeDetailNavController.popBackStack()
        }
    }

    private fun setInitView() {
        val userData = userViewModel.userData.value!!
        Glide.with(requireContext()).load(userData.profileImageUrl).into(profileIV)
        nameTV.text = userData.name
    }

    private fun setListener() {
        closeBtn.setOnClickListener(this)
        editProfileBtn.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.closeBtn -> requireActivity().onBackPressed()
            // TODO : 프로필 수정 구현하기
            // R.id.editProfileBtn ->
        }
    }

}