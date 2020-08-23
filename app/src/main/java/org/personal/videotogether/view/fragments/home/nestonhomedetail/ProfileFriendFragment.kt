package org.personal.videotogether.view.fragments.home.nestonhomedetail

import android.os.Bundle
import android.view.View
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

    private val argument: ProfileFriendFragmentArgs by navArgs()
    private val friendData  by lazy { argument.friendData }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeDetailNavController = Navigation.findNavController(view)
        setInitView()
        setListener()
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