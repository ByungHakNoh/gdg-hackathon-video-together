package org.personal.videotogether.view.fragments.home.nestonhomedetail

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_profile_friend.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProfileFriendFragment : Fragment(R.layout.fragment_profile_friend), View.OnClickListener {

    private val TAG = javaClass.name

    private lateinit var homeDetailNavController: NavController
    private lateinit var backPressCallback: OnBackPressedCallback // 뒤로가기 버튼 콜백

    private val argument: ProfileFriendFragmentArgs by navArgs()
    private val friendData  by lazy { argument.friendData }

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
        Glide.with(requireContext()).load(friendData.profileImageUrl).into(profileIV)
        nameTV.text = friendData.name
    }

    private fun setListener() {
        closeBtn.setOnClickListener(this)
        chatBtn.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.closeBtn -> requireActivity().onBackPressed()
            //TODO : 1 대 1 채팅으로 넘어가기
//            R.id.chatBtn -> mainNavController.navigate(R.id.action_profileFriendFragment_to_chattingFragment)

        }
    }
}