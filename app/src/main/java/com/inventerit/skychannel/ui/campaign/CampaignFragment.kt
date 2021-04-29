package com.inventerit.skychannel.ui.campaign

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.inventerit.skychannel.R
import com.inventerit.skychannel.databinding.FragmentCampaignBinding
import com.inventerit.skychannel.databinding.FragmentSubscribeBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

class CampaignFragment : Fragment() {


    private lateinit var binding: FragmentCampaignBinding

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.to_bottom_anim) }

    private var isClicked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCampaignBinding.inflate(layoutInflater)

        binding.like.setOnClickListener {
            Toast.makeText(context,"Coming Soon",Toast.LENGTH_LONG).show()
        }

        binding.subscribe.setOnClickListener {
            Toast.makeText(context,"Coming Soon",Toast.LENGTH_LONG).show()
        }

        binding.view.setOnClickListener {
            Toast.makeText(context,"Coming Soon",Toast.LENGTH_LONG).show()
        }

        binding.mainFab.setOnClickListener {
            onMainFabClicked()
        }
        return binding.root
    }

    private fun onMainFabClicked() {
        setVisibility(isClicked)
        setAnimation(isClicked)
        setClickable(isClicked)
        isClicked = !isClicked
    }

    private fun setAnimation(clicked: Boolean) {
        if(!clicked){
            binding.like.startAnimation(fromBottom)
            binding.subscribe.startAnimation(fromBottom)
            binding.view.startAnimation(fromBottom)
            binding.mainFab.startAnimation(rotateOpen)
        }else{

            binding.like.startAnimation(toBottom)
            binding.subscribe.startAnimation(toBottom)
            binding.view.startAnimation(toBottom)
            binding.mainFab.startAnimation(rotateClose)
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if(!clicked){
            binding.like.visibility = View.VISIBLE
            binding.subscribe.visibility = View.VISIBLE
            binding.view.visibility = View.VISIBLE
        }else{

            binding.like.visibility = View.INVISIBLE
            binding.subscribe.visibility = View.INVISIBLE
            binding.view.visibility = View.INVISIBLE
        }
    }

    private fun setClickable(clicked: Boolean){
        if(!clicked){
            binding.like.isClickable = true
            binding.subscribe.isClickable = true
            binding.view.isClickable = true
        }else{

            binding.like.isClickable = false
            binding.subscribe.isClickable = false
            binding.view.isClickable = false
        }
    }
}