package org.personal.videotogether.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.YoutubeData

class YoutubeAdapter
constructor(
    val fragment: Fragment,
    private val youtubeList: ArrayList<YoutubeData>,
    private val itemClickListener: ItemClickListener
) : RecyclerView.Adapter<YoutubeAdapter.ViewHolder>() {

    class ViewHolder(itemView: View, private val itemClickListener: ItemClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        val youtubeVideo: YouTubePlayerView = itemView.findViewById(R.id.youtubePlayerYP)
        val channelThumbnailIV: ImageView = itemView.findViewById(R.id.channelThumbnailIV)
        val videoTitleTV : TextView = itemView.findViewById(R.id.videoTitleTV)
        val channelTitleTV: TextView = itemView.findViewById(R.id.channelTitleTV)

        override fun onClick(view: View?) {

            if (adapterPosition != RecyclerView.NO_POSITION) {

                itemClickListener.onItemClick(view, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_youtube, parent, false)
        return ViewHolder(view, itemClickListener)
    }

    override fun getItemCount(): Int {
        return youtubeList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val youtubeData = youtubeList[position]
        val youtubeVideo = holder.youtubeVideo

        fragment.lifecycle.addObserver(youtubeVideo)
        youtubeVideo.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                super.onReady(youTubePlayer)
                youTubePlayer.loadVideo(youtubeData.videoId, 0f)
            }
        })
        Glide.with(fragment.requireContext()).load(youtubeData.channelThumbnail).into(holder.channelThumbnailIV)
        holder.videoTitleTV.text = youtubeData.title
        holder.channelTitleTV.text = youtubeData.channelTitle
    }
}