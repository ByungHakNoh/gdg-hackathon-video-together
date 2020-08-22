package org.personal.videotogether.view.fragments.home.nestonhome

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_youtube.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.YoutubeData
import org.personal.videotogether.domianmodel.YoutubePageData
import org.personal.videotogether.util.DataState
import org.personal.videotogether.util.InfiniteScrollListener
import org.personal.videotogether.view.adapter.ItemClickListener
import org.personal.videotogether.view.adapter.YoutubeAdapter
import org.personal.videotogether.viewmodel.YoutubeStateEvent
import org.personal.videotogether.viewmodel.YoutubeViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class YoutubeFragment : Fragment(R.layout.fragment_youtube), ItemClickListener, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private val TAG by lazy { javaClass.name }

    private lateinit var homeNavController: NavController
    private lateinit var backPressCallback: OnBackPressedCallback // 뒤로가기 버튼 콜백

    private val youtubeViewModel: YoutubeViewModel by lazy { ViewModelProvider(requireActivity())[YoutubeViewModel::class.java] }

    private val youtubeList by lazy { ArrayList<YoutubeData>() }
    private val youtubeAdapter by lazy { YoutubeAdapter(this, youtubeList, this) }
    private lateinit var scrollListener: InfiniteScrollListener

    private var nextPageUrl: String? = null
    private var nextPageToken: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeNavController = Navigation.findNavController(view)

        setBackPressCallback()
        subscribeObservers()
        setListener()
        buildRecyclerView()

        youtubeViewModel.setStateEvent(YoutubeStateEvent.GetYoutubeDefaultPage("googledevelopers"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backPressCallback.remove()
    }

    private fun setBackPressCallback() {
        // 비디오 모션 레이아웃 관련 뒤로가기 버튼은 VideoPlayFragment 에서 관리
        // VideoPlayFragment 뒤로가기 콜백이 disable 되면 실행됨
        backPressCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            homeNavController.popBackStack()
        }
    }

    private fun subscribeObservers() {
        youtubeViewModel.youtubeDefaultPage.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Loading -> {
                    swipeRefreshSR.isRefreshing = true
                }
                is DataState.Success<YoutubePageData?> -> {
                    swipeRefreshSR.isRefreshing = false

                    nextPageUrl = dataState.data!!.nextPageUrl
                    nextPageToken = dataState.data.nextPageToken

                    dataState.data.youtubeDataList.forEach { youtubeData -> youtubeList.add(youtubeData) }
                    youtubeAdapter.notifyDataSetChanged()
                }
                is DataState.NoData -> {
                    swipeRefreshSR.isRefreshing = false
                }
                is DataState.Error -> {
                    swipeRefreshSR.isRefreshing = false
                }
            }
        })
    }

    private fun setListener() {
        swipeRefreshSR.setOnRefreshListener(this)
        backBtn.setOnClickListener(this)
        youtubeSearchBtn.setOnClickListener(this)
    }

    private fun buildRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())

        youtubePreviewRV.setHasFixedSize(true)
        youtubePreviewRV.layoutManager = layoutManager
        youtubePreviewRV.adapter = youtubeAdapter

        scrollListener = object : InfiniteScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                Log.i(TAG, "onLoadMore: real?")
                youtubeViewModel.setStateEvent(
                    YoutubeStateEvent.GetNextYoutubeDefaultPage(
                        nextPageUrl!!, nextPageToken!!, youtubeList[0].channelTitle, youtubeList[0].channelThumbnail
                    )
                )
            }
        }
        youtubePreviewRV.addOnScrollListener(scrollListener)
    }

    // ------------------ 클릭 리스너 메소드 모음 ------------------
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.backBtn -> requireActivity().onBackPressed()
            R.id.youtubeSearchBtn -> homeNavController.navigate(R.id.action_youtubeFragment_to_youtubeSearchFragment)
        }
    }

    // ------------------ 리사이클러뷰 아이템 클릭 리스너 메소드 모음 ------------------
    override fun onItemClick(view: View?, itemPosition: Int) {
        val youtubeData = youtubeList[itemPosition]
        youtubeViewModel.setStateEvent(YoutubeStateEvent.SetFrontPlayer(youtubeData))
    }

    // ------------------ 리사이클러뷰 새로고침 리스너 ------------------
    override fun onRefresh() {
        youtubeList.clear()
        scrollListener.resetState()
        youtubeViewModel.setStateEvent(YoutubeStateEvent.GetYoutubeDefaultPage("googledevelopers"))
    }
}