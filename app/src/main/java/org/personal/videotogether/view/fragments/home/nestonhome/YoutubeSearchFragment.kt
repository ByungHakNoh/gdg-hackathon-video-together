package org.personal.videotogether.view.fragments.home.nestonhome

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_youtube.swipeRefreshSR
import kotlinx.android.synthetic.main.fragment_youtube.youtubePreviewRV
import kotlinx.android.synthetic.main.fragment_youtube_search.*
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
import java.util.*
import kotlin.collections.ArrayList

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class YoutubeSearchFragment : Fragment(R.layout.fragment_youtube_search), View.OnClickListener, View.OnKeyListener, ItemClickListener {

    private val TAG by lazy { javaClass.name }

    private val youtubeViewModel: YoutubeViewModel by lazy { ViewModelProvider(requireActivity())[YoutubeViewModel::class.java] }

    private val youtubeList by lazy { ArrayList<YoutubeData>() }
    private val youtubeAdapter by lazy { YoutubeAdapter(this, youtubeList, this) }
    private lateinit var scrollListener: InfiniteScrollListener

    private var nextPageUrl: String? = null
    private var nextPageToken: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeObservers()
        setListener()
        buildRecyclerView()
    }

    private fun subscribeObservers() {
        youtubeViewModel.youtubeSearchedPage.observe(viewLifecycleOwner, Observer { dataState ->
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
                    Toast.makeText(requireContext(), "검색한 채널이 없습니다", Toast.LENGTH_SHORT).show()
                }
                is DataState.Error -> {
                    swipeRefreshSR.isRefreshing = false
                }
            }
        })
    }

    private fun setListener() {
        backBtn.setOnClickListener(this)
        searchET.setOnKeyListener(this)
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
                    YoutubeStateEvent.GetNextYoutubeSearchedPage(
                        nextPageUrl!!, nextPageToken!!, youtubeList[0].channelTitle, youtubeList[0].channelThumbnail
                    )
                )
            }
        }
        youtubePreviewRV.addOnScrollListener(scrollListener)
    }

    // ------------------ 클릭 리스너 메소드 모음 ------------------
    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.backBtn -> requireActivity().onBackPressed()
        }
    }

    // ------------------ 검색 키 리스너 메소드 모음 ------------------
    override fun onKey(view: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (keyEvent?.action == KeyEvent.ACTION_UP) {
                val searchText = searchET.text.toString()

                if (searchText.trim() == "") {
                    Toast.makeText(requireContext(), "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
                } else {
                    youtubeViewModel.setStateEvent(YoutubeStateEvent.GetYoutubeSearchedPage(searchET.text.toString()))
                    youtubeList.clear()
                    youtubeAdapter.notifyDataSetChanged()
                }
                return true
            }
        }
        return false
    }

    // ------------------ 아이템 클릭 리스너 메소드 모음 ------------------
    override fun onItemClick(view: View?, itemPosition: Int) {
        val youtubeData = youtubeList[itemPosition]
        youtubeViewModel.setStateEvent(YoutubeStateEvent.SetFrontPlayer(youtubeData))
    }
}