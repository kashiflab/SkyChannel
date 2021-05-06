package com.inventerit.skychannel.ui.view

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.inventerit.skychannel.TimeHelper
import com.inventerit.skychannel.constant.PrefKeys
import com.inventerit.skychannel.databinding.FragmentViewBinding
import com.inventerit.skychannel.interfaces.LikeListener
import com.inventerit.skychannel.interfaces.OnCampaignStatus
import com.inventerit.skychannel.interfaces.OnGetCampaign
import com.inventerit.skychannel.model.Campaign
import com.inventerit.skychannel.viewModel.MainViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.tramsun.libs.prefcompat.Pref
import kotlinx.android.synthetic.main.fragment_more.*
import java.lang.Exception

class ViewFragment : Fragment() {

    private lateinit var binding: FragmentViewBinding

    private lateinit var mainViewModel: MainViewModel
    private var currentNumber = 0;

    private var campaign: List<Campaign>? = null
    private var player: YouTubePlayer? = null

    private var coinsAdded = false
    private var campaignUpdated = false

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        mainViewModel.getViewsCampaigns(object: OnGetCampaign<List<Campaign>>{
            override fun onGetCampaign(status: Boolean, result: List<Campaign>) {
                try {
                    campaign = ArrayList()
                    if (status) {
                        campaign = result
                        binding.videoTitle.text = campaign?.get(0)?.video_title.toString()
                        binding.duration.text = campaign?.get(0)?.total_time.toString()
                        startVideo(campaign?.get(0)?.video_id.toString())
                    } else {
                        Toast.makeText(context, "No video found", Toast.LENGTH_LONG).show()
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        })

        return binding.root
    }

    fun startVideo(videoId: String){
        lifecycle.addObserver(binding.video)
        binding.video.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {

                if(videoId.isNotEmpty()) {
                    youTubePlayer.loadVideo(videoId, 0f)
                    player = youTubePlayer
                }

                binding.nextBtn.setOnClickListener {
                    if (currentNumber < campaign?.size!! - 1) {
                        currentNumber++
                        binding.videoTitle.text = campaign?.get(currentNumber)?.video_title
                        binding.duration.text = campaign?.get(currentNumber)?.total_time.toString()
                        youTubePlayer.loadVideo(campaign?.get(currentNumber)?.video_id!!, 0f)
                        player = youTubePlayer
                    } else {
                        Toast.makeText(context, "No more video found", Toast.LENGTH_LONG).show()
                    }
                }

            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)

                val updateNumber: Int = campaign?.get(currentNumber)?.total_time?.toInt()?.minus(second)?.toInt()!!

                if (updateNumber >= 0) {
                    binding.duration.text = updateNumber.toString()
                    if(updateNumber==1) {
                        if(!coinsAdded) {
                            coinsAdded = true
                            updateUserCoin()
                        }
                        if(!campaignUpdated) {
                            campaignUpdated = true
                            updateCampaignStatus()
                        }
                    }
                }
            }
        })
    }
    private fun updateCampaignStatus() {
        val campaignId = campaign?.get(currentNumber)?.id.toString()
        val campaignNumber = (campaign?.get(currentNumber)?.current_number?.toInt()?.plus(1)).toString()

        mainViewModel.updateCampaignStatus(campaignId,campaignNumber,object: OnCampaignStatus {
            override fun onCampaignStatus(status: Boolean) {
                if(status){
                    campaignUpdated = true
                    Log.i("Coins","Campaign status increased")
                }else{
                    campaignUpdated = false
                    Log.i("Coins","Campaign Status not increased")
                }
            }
        })

    }
    fun updateUserCoin(){

        val updatedCoins = Pref.getString(PrefKeys.coins,"0").toInt() + 32
        mainViewModel.updateCoins(updatedCoins.toString(), object : LikeListener {
            override fun onLikeStarted() {
                coinsAdded = true
                campaignUpdated = true
                Log.i("Coins","Coins adding")
            }

            override fun onLikedSuccess() {
                coinsAdded = true
                Toast.makeText(context, "Coins Added", Toast.LENGTH_LONG).show()
            }

            override fun onLikedFailed() {
                coinsAdded = false
                Log.i("Coins","Coins are not updated")
            }
        })

    }

    override fun onPause() {
        super.onPause()
        if(player!=null){
            player?.pause()
        }
    }
}