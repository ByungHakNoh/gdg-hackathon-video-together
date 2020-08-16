package org.personal.videotogether.view.fragments.home.nestonhome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_youtube.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class YoutubeSearchFragment : Fragment(R.layout.fragment_youtube_search), View.OnClickListener {

    private val TAG by lazy { javaClass.name }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListener()
    }

    private fun setListener() {
        backBtn.setOnClickListener(this)
    }

    // ------------------ 클릭 리스너 메소드 모음 ------------------
    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.backBtn -> requireActivity().onBackPressed()
        }
    }
}