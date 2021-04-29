package com.inventerit.skychannel.ui.like

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.inventerit.skychannel.R
import com.inventerit.skychannel.databinding.FragmentLikeBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

class LikeFragment : Fragment() {

    private lateinit var binding: FragmentLikeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLikeBinding.inflate(layoutInflater)

        lifecycle.addObserver(binding.video)

        binding.video.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                val videoId = "u50sJ7b_ZFo"
                youTubePlayer.loadVideo(videoId, 0f)
            }
        })

        return binding.root
    }
}