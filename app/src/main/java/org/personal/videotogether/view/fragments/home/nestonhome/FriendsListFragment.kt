package org.personal.videotogether.view.fragments.home.nestonhome

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_friend_list.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.util.DataState
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.view.adapter.FriendListAdapter
import org.personal.videotogether.view.adapter.ItemClickListener
import org.personal.videotogether.view.fragments.home.nestonhomedetail.HomeDetailBlankFragmentDirections
import org.personal.videotogether.viewmodel.FriendStateEvent
import org.personal.videotogether.viewmodel.FriendViewModel
import org.personal.videotogether.viewmodel.UserViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FriendsListFragment
constructor(
    private val dataStateHandler: DataStateHandler
) : Fragment(R.layout.fragment_friend_list), ItemClickListener, View.OnClickListener {

    private val TAG = javaClass.name

    // 네비게이션 + 뷰모델
    private lateinit var homeDetailNavController: NavController
    private val friendViewModel: FriendViewModel by lazy { ViewModelProvider(requireActivity())[FriendViewModel::class.java] }
    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }

    // 리사이클러 뷰
    private val friendList by lazy { ArrayList<FriendData>() }
    private val friendListAdapter by lazy { FriendListAdapter(requireContext(), friendList, false, this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homeDetailFragmentContainer: FragmentContainerView = view.rootView.findViewById(R.id.homeDetailFragmentContainer)
        homeDetailNavController = Navigation.findNavController(homeDetailFragmentContainer)

        subscribeObservers()
        setListener()
        buildRecyclerView()
    }

    private fun subscribeObservers() {
        // 이미 가져온 유저 데이터 사용
        userViewModel.userData.observe(viewLifecycleOwner, Observer { userData ->
            Glide.with(requireContext()).load(userData!!.profileImageUrl).into(profileIV)
            nameTV.text = userData.name
        })

        // 로컬에서 친구 목록 불러오기
        friendViewModel.friendList.observe(viewLifecycleOwner, Observer { localFriendList ->
            if (localFriendList == null) {
                friendViewModel.setStateEvent(FriendStateEvent.GetFriendListFromServer(userViewModel.userData.value!!.id))
            } else {
                if (localFriendList.isEmpty()) {
                    friendViewModel.setStateEvent(FriendStateEvent.GetFriendListFromServer(userViewModel.userData.value!!.id))
                } else {
                    friendList.clear()
                    localFriendList.forEach { friendData ->
                        friendList.add(friendData)
                    }
                    friendListAdapter.notifyDataSetChanged()
                }
            }
        })

        // 로컬에 데이터가 없을 때 서버에서 친구 데이터 요청 (새로 로그인 시)
        friendViewModel.updatedFriendList.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Loading -> Log.i(TAG, "updatedFriendList: Loading")
                is DataState.NoData -> Log.i(TAG, "updatedFriendList: No Data")
                is DataState.Error -> Log.i(TAG, "updatedFriendList: Error - ${dataState.exception}")
            }
        })

        // 서버에서 데이터 가져온 데이터 -> 성공 시 friendList 업데이트
        friendViewModel.updatedFriendList.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Loading -> {
                    Log.i(TAG, "updatedFriendList: 로딩")
                }
                is DataState.Success<List<FriendData>?> -> {
                    Log.i(TAG, "updatedFriendList: 성공")
                }
                is DataState.NoData -> {
                    // TODO: 데이터가 없으면 친구 추가 문구 띄워주기
                }
                is DataState.Error -> {
                    Log.i(TAG, "updatedFriendList: 에러 발생")
                    friendViewModel.setStateEvent(FriendStateEvent.GetFriendListFromLocal)
                }
            }
        })
    }

    private fun setListener() {
        myProfileContainerCL.setOnClickListener(this)
    }

    private fun buildRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())

        friendsListRV.setHasFixedSize(true)
        friendsListRV.layoutManager = layoutManager
        friendsListRV.adapter = friendListAdapter
    }

    // ------------------ 클릭 리스너 메소드 모음 ------------------
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.myProfileContainerCL -> homeDetailNavController.navigate(R.id.action_homeDetailBlankFragment_to_profileMineFragment)
        }
    }

    // ------------------ 리사이클러 뷰 아이템 클릭 리스너 메소드 모음 ------------------
    override fun onItemClick(view: View?, itemPosition: Int) {
        val selectedFriendData = friendList[itemPosition]
        val action = HomeDetailBlankFragmentDirections.actionHomeDetailBlankFragmentToProfileFriendFragment(selectedFriendData)
        homeDetailNavController.navigate(action)
    }
}