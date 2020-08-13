package org.personal.videotogether.view.fragments.home.nestonhome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_youtube.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.YoutubeData
import org.personal.videotogether.util.DataState
import org.personal.videotogether.view.adapter.YoutubeAdapter
import org.personal.videotogether.viewmodel.YoutubeStateEvent
import org.personal.videotogether.viewmodel.YoutubeViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class YoutubeFragment : Fragment(R.layout.fragment_youtube), YoutubeAdapter.ItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private val TAG by lazy { javaClass.name }

    private val youtubeViewModel: YoutubeViewModel by lazy { ViewModelProvider(requireActivity())[YoutubeViewModel::class.java] }

    private val youtubeList by lazy { ArrayList<YoutubeData>() }
    private val youtubeAdapter by lazy { YoutubeAdapter(this, youtubeList, this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeObservers()
        buildRecyclerView()
        swipeRefreshSR.setOnRefreshListener(this)
        youtubeViewModel.setStateEvent(YoutubeStateEvent.GetDefaultYoutubeVideos("googledevelopers"))
    }

    private fun subscribeObservers() {
        // 친구 검색하기
        youtubeViewModel.youtubeData.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Loading -> {
                    swipeRefreshSR.isRefreshing = true
                }
                is DataState.Success<List<YoutubeData>?> -> {
                    swipeRefreshSR.isRefreshing = false
                    dataState.data!!.forEach { youtubeData -> youtubeList.add(youtubeData) }
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

    private fun buildRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())

        youtubePreviewRV.setHasFixedSize(true)
        youtubePreviewRV.layoutManager = layoutManager
        youtubePreviewRV.adapter = youtubeAdapter
    }

    override fun onItemClick(view: View?, itemPosition: Int) {

    }

    override fun onRefresh() {
        youtubeViewModel.setStateEvent(YoutubeStateEvent.GetDefaultYoutubeVideos("googledevelopers"))
    }
}