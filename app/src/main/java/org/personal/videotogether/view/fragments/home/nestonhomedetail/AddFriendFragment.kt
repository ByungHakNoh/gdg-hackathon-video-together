package org.personal.videotogether.view.fragments.home.nestonhomedetail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_friend.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.util.DataState
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.util.view.ImageHandler
import org.personal.videotogether.util.view.ViewHandler
import org.personal.videotogether.viewmodel.FriendStateEvent
import org.personal.videotogether.viewmodel.FriendViewModel
import org.personal.videotogether.viewmodel.UserViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddFriendFragment
constructor(
    private val dataStateHandler: DataStateHandler,
    private val imageHandler: ImageHandler,
    private val viewHandler: ViewHandler
) : Fragment(R.layout.fragment_add_friend), View.OnClickListener, TextWatcher, View.OnKeyListener {

    private val TAG = javaClass.name

    private lateinit var homeDetailNavController: NavController
    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val friendViewModel: FriendViewModel by lazy { ViewModelProvider(requireActivity())[FriendViewModel::class.java] }

    // 검색이 성공해서 서버로부터 받아온 친구 데이터
    private lateinit var friendUserData: FriendData

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeDetailNavController = Navigation.findNavController(view)
        subscribeObservers()
        setListener()
    }

    private fun subscribeObservers() {
        // 친구 검색하기
        friendViewModel.searchFriend.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Loading -> {
                    dataStateHandler.displayLoadingDialog(true, childFragmentManager)
                }
                is DataState.Success<FriendData?> -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    friendUserData = dataState.data!!
                    displayFriend()
                }
                is DataState.DuplicatedData -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    friendInfoContainerCL.visibility = View.GONE
                    Toast.makeText(requireContext(), "이미 친구 입니다.", Toast.LENGTH_SHORT).show()
                }
                is DataState.NoData -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    friendInfoContainerCL.visibility = View.GONE
                    Toast.makeText(requireContext(), "사용자를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                }
                is DataState.Error -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    friendInfoContainerCL.visibility = View.GONE
                    Log.i(TAG, "subscribeObservers: ${dataState.exception}")
                }
            }
        })

        // 친구 추가하기
        friendViewModel.addFriend.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<Boolean?> -> {
                    friendViewModel.setStateEvent(FriendStateEvent.GetFriendListFromServer(userViewModel.userData.value!!.id))
                    requireActivity().onBackPressed()
                }
                is DataState.NoData -> {
                    friendInfoContainerCL.visibility = View.GONE
                    // TODO : UI로 보여주기
                    Toast.makeText(requireContext(), "사용자를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                }
                is DataState.Error -> {
                    Log.i(TAG, "subscribeObservers: ${dataState.exception}")
                }
            }
        })
    }

    private fun setListener() {
        backBtn.setOnClickListener(this)
        clearTextIB.setOnClickListener(this)
        addFriendBtn.setOnClickListener(this)
        friendEmailET.addTextChangedListener(this)
        friendEmailET.setOnKeyListener(this)
    }

    private fun displayFriend() {
        friendNameTV.text = friendUserData.name
        Glide.with(requireContext()).load(friendUserData.profileImageUrl).into(friendProfileIV)
        friendInfoContainerCL.visibility = View.VISIBLE
    }

    // ------------------ 클릭 이벤트 메소드 모음 ------------------
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backBtn -> requireActivity().onBackPressed()
            R.id.clearTextIB -> friendEmailET.text = null
            R.id.addFriendBtn -> {
                // 유저 id 를 가져오고 room 에서 가져오고 서버에 친구 추가 요청
                val userId = userViewModel.userData.value!!.id
                friendViewModel.setStateEvent(FriendStateEvent.AddFriend(userId, friendUserData))
            }
        }
    }

    // ------------------ Edit Text 리스너 메소드 모음 ------------------
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun afterTextChanged(text: Editable?) {
        viewHandler.handleClearTextBtn(text.toString().count(), clearTextIB)
    }

    override fun onKey(view: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (keyEvent?.action == KeyEvent.ACTION_UP) {
                val friendEmail = friendEmailET.text.toString()

                if (friendEmail.trim() == "") {
                    viewHandler.handleWarningText(noInputWarningTV)
                } else {
                    val userId = userViewModel.userData.value!!.id
                    friendViewModel.setStateEvent(FriendStateEvent.SearchFriend(userId, friendEmailET.text.toString()))
                }
                return true
            }
        }
        return false
    }
}