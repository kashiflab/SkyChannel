package com.sidhow.skychannel.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.sidhow.skychannel.TimeHelper
import com.sidhow.skychannel.constant.Constants
import com.sidhow.skychannel.constant.PrefKeys
import com.sidhow.skychannel.databinding.ActivityViewsBinding
import com.sidhow.skychannel.interfaces.LikeListener
import com.sidhow.skychannel.interfaces.OnCampaignStatus
import com.sidhow.skychannel.interfaces.OnGetCampaign
import com.sidhow.skychannel.model.Campaign
import com.sidhow.skychannel.room.VideosDatabase
import com.sidhow.skychannel.room.model.Videos
import com.sidhow.skychannel.viewModel.MainViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.tramsun.libs.prefcompat.Pref
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ViewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewsBinding
    private lateinit var mainViewModel: MainViewModel
    private var currentNumber = 0;

    private var campaign: List<Campaign>? = null
    private var player: YouTubePlayer? = null

    private var coinsAdded = false
    private var campaignUpdated = false

    private lateinit var videosDatabase: VideosDatabase

    private var mUser = FirebaseAuth.getInstance().currentUser

    private lateinit var viewed: MutableList<Videos>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        videosDatabase = VideosDatabase.getInstance(this)

        mainViewModel.getViewsCampaigns(object: OnGetCampaign<List<Campaign>> {
            override fun onGetCampaign(status: Boolean, result: List<Campaign>) {
                try {
                    campaign = ArrayList()
                    if (status) {
                        campaign = result
                        binding.videoTitle.text = campaign?.get(0)?.video_title.toString()
                        binding.duration.text = campaign?.get(0)?.total_time.toString()
                        startVideo(campaign?.get(0)?.video_id.toString())
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@ViewsActivity, "No video found", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        })

    }
    fun startVideo(videoId: String){
        lifecycle.addObserver(binding.video)
        binding.video.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {

                if(videoId.isNotEmpty()) {
                    youTubePlayer.loadVideo(videoId, 0f)
                    player = youTubePlayer
                    binding.coins.text = campaign!![currentNumber].coins
                }

                binding.nextBtn.setOnClickListener {
                    startNextVideo(youTubePlayer)
                }

            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)

                val updateNumber: Int = campaign?.get(currentNumber)?.total_time?.toInt()?.minus(second)?.toInt()!!

                if (updateNumber >= 0) {
                    binding.duration.text = updateNumber.toString()
                    if(updateNumber==1) {
                        addView()
                        try {
                            youTubePlayer.pause()
                        }catch (e: Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }
        })
    }

    private fun startNextVideo(youTubePlayer: YouTubePlayer){
        if (currentNumber < campaign?.size!! - 1) {
            currentNumber +=1

            binding.videoTitle.text = campaign?.get(currentNumber)?.video_title
            binding.duration.text = campaign?.get(currentNumber)?.total_time.toString()
            binding.coins.text = campaign!![currentNumber].coins
            youTubePlayer.loadVideo(campaign?.get(currentNumber)?.video_id!!, 0f)

            player = youTubePlayer
        } else {
            Toast.makeText(this, "No more video found", Toast.LENGTH_LONG).show()
        }
    }

    private fun addView(){
        if (!coinsAdded) {
            val videos = Videos()
            videos.videoId = campaign?.get(currentNumber)?.video_id.toString()
            videos.created_at = TimeHelper.getDate()
            videos.type = "2"
            videos.userId = mUser.uid
            videos.channelId = campaign?.get(currentNumber)?.channel_id.toString()

            saveIntoFirebase(videos)
            Thread {
                try {
                    videosDatabase.videosDao().insert(videos)
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }.start()
            coinsAdded = true
            updateUserCoin()
        }
        if (!campaignUpdated) {
            campaignUpdated = true
            updateCampaignStatus()
        }
    }
    private fun saveIntoFirebase(videos: Videos) {
        val database: DatabaseReference = FirebaseDatabase.getInstance().reference
        val map: HashMap<String,Any> = HashMap()
        val id = UUID.randomUUID().toString()
        map[Constants.id] = id
        map[Constants.video_id] = videos.videoId
        map[Constants.created_at] = videos.created_at
        map[Constants.type] = videos.type
        map[Constants.user_id] = videos.userId
        map[Constants.channel_id] = videos.channelId

        database.child(Constants.users).child(mUser.uid).child(Constants.history).child(id).setValue(map)
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

        addUser(campaignId,mUser.uid)
    }

    private fun addUser(campaignId: String, uid: String) {
        mainViewModel.addUser(campaignId,uid)
    }

    fun updateUserCoin(){

        val updatedCoins = Pref.getString(PrefKeys.coins,"0").toInt() + campaign?.get(currentNumber)?.coins?.toInt()!!
        mainViewModel.updateCoins(updatedCoins.toString(), object : LikeListener {
            override fun onLikeStarted() {
                coinsAdded = true
                campaignUpdated = true
                Log.i("Coins","Coins adding")
            }

            override fun onLikedSuccess() {
                coinsAdded = true
                Toast.makeText(this@ViewsActivity, "Coins Added", Toast.LENGTH_LONG).show()
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