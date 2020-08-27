package org.personal.videotogether.view.fragments.home.nestonhome

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
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
class YoutubeFragment : Fragment(R.layout.fragment_youtube), ItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private val TAG by lazy { javaClass.name }

    private lateinit var homeNavController: NavController

    private val youtubeViewModel: YoutubeViewModel by lazy { ViewModelProvider(requireActivity())[YoutubeViewModel::class.java] }

    // 유투브 리사이클러 뷰 관련
    private val youtubeList by lazy { ArrayList<YoutubeData>() }
    private val youtubeAdapter by lazy { YoutubeAdapter(this, youtubeList, this) }
    private lateinit var scrollListener: InfiniteScrollListener

    private var nextPageUrl: String? = null
    private var nextPageToken: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeNavController = Navigation.findNavController(view)

        subscribeObservers()
        setListener()
        buildRecyclerView()

        youtubeViewModel.setStateEvent(YoutubeStateEvent.GetYoutubeDefaultPage("googledevelopers"))
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

    // ------------------ 리사이클러뷰 아이템 클릭 리스너 메소드 모음 ------------------
    override fun onItemClick(view: View?, itemPosition: Int) {
        val isVideoTogetherOn = youtubeViewModel.setVideoTogether.value

        if (isVideoTogetherOn == null || !isVideoTogetherOn) {
            val youtubeData = youtubeList[itemPosition]
            youtubeViewModel.setStateEvent(YoutubeStateEvent.SetFrontPlayer(youtubeData))
        } else {
            Toast.makeText(requireContext(), "유투브 같이보기 실행중 입니다", Toast.LENGTH_SHORT).show()
        }
    }

    // ------------------ 리사이클러뷰 새로고침 리스너 ------------------
    override fun onRefresh() {
        youtubeList.clear()
        scrollListener.resetState()
        youtubeViewModel.setStateEvent(YoutubeStateEvent.GetYoutubeDefaultPage("googledevelopers"))
    }
}