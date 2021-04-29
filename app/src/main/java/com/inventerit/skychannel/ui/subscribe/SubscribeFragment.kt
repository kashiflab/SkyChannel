package com.inventerit.skychannel.ui.subscribe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.inventerit.skychannel.databinding.FragmentSubscribeBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener


class SubscribeFragment : Fragment() {

    val DEVELOPER_KEY: String = "AIzaSyDUqZdrWnKoGlKVF3cMllPqybWi2TO9NIg"
    private lateinit var binding: FragmentSubscribeBinding

    private val TAG: String = "SubscribeFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubscribeBinding.inflate(layoutInflater)

//        binding.video.initialize(DEVELOPER_KEY,this)

        lifecycle.addObserver(binding.video)

        binding.video.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                val videoId = "u50sJ7b_ZFo"
                youTubePlayer.loadVideo(videoId, 0f)
            }
        })


        return binding.root
    }

//    override fun onInitializationSuccess(
//        provider: YouTubePlayer.Provider?, youTubePlayer: YouTubePlayer?,
//        wasRestored: Boolean
//    ) {
//        Log.d(TAG, "onInitializationSuccess: provider is ${provider?.javaClass}")
//        Log.d(TAG, "onInitializationSuccess: youTubePlayer is ${youTubePlayer?.javaClass}")
//        Toast.makeText(context, "Initialized Youtube Player successfully", Toast.LENGTH_SHORT).show()
//
//
//        if (!wasRestored) {
//            youTubePlayer?.cueVideo("u50sJ7b_ZFo")
//        }
//    }
//
//    override fun onInitializationFailure(
//        provider: YouTubePlayer.Provider?,
//        youTubeInitializationResult: YouTubeInitializationResult?
//    ) {
//        val REQUEST_CODE = 0
//
//        if (youTubeInitializationResult?.isUserRecoverableError == true) {
//            youTubeInitializationResult.getErrorDialog(requireActivity(), REQUEST_CODE).show()
//        } else {
//            val errorMessage = "There was an error initializing the YoutubePlayer ($youTubeInitializationResult)"
//            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
//        }
//    }
}