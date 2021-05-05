package com.inventerit.skychannel.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.inventerit.skychannel.Utils
import com.inventerit.skychannel.interfaces.OnCampaignAdded
import com.inventerit.skychannel.interfaces.OnVideoDetails
import com.inventerit.skychannel.databinding.ActivityAddCampaignBinding
import com.inventerit.skychannel.model.Api
import com.inventerit.skychannel.model.Campaign
import com.inventerit.skychannel.model.Snippet
import com.inventerit.skychannel.viewModel.MainViewModel

class AddCampaignActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCampaignBinding

    private var link: String = ""
    private var type: String = ""

    private var snippet: Snippet? = null

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCampaignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        link = intent.getStringExtra("link").toString()
        type = intent.getStringExtra("type").toString()

        val videoId = link.split("/")[3]

        val imageUrl = "https://img.youtube.com/vi/${videoId}/0.jpg"

        mainViewModel.getVideoDetails(videoId,object : OnVideoDetails<Api>{
            override fun onVideoDetails(status: Boolean, result: Api?) {
                if(status){
                    snippet = result?.items?.get(0)?.snippet
                    binding.videoTitle.text = snippet?.title.toString()
                }else{
                    Toast.makeText(this@AddCampaignActivity,"Some error occurred",Toast.LENGTH_LONG).show()
                }
            }
        })

        Glide.with(this).load(imageUrl).into(binding.imageView)

        when (type) {
            "0" -> {
                binding.tv.text = "Numbers of Subscribers"
            }
            "1" -> {
                binding.tv.text = "Numbers of Likes"
            }
            "2" -> {
                binding.tv.text = "Numbers of Views"
            }
        }

        runOnUiThread {
            val number = binding.number.text.toString()
            val time = binding.time.text.toString()

            if(number.isNotEmpty() && number.toInt() in 5001 downTo 9){
                binding.number.error = "Invalid Number"
            }
            if(time.isNotEmpty() && time.toInt() in 5001 downTo 9){
                binding.time.error = "Invalid Number"
            }
        }

        binding.createBtn.setOnClickListener{
            val number = binding.number.text.toString()
            val time = binding.time.text.toString()
            val campaign =  Campaign()

            if(number.isEmpty() || time.isEmpty()) {
                Toast.makeText(
                    this@AddCampaignActivity,
                    "All fields are required",
                    Toast.LENGTH_LONG
                ).show()
            }else {
                val mUser = FirebaseAuth.getInstance().currentUser

                campaign.video_id = videoId
                campaign.video_title = "Video Title: ${snippet?.title.toString()}"
                campaign.user_id = mUser.uid
                campaign.current_number = "0"
                campaign.total_number = number
                campaign.total_coins = getTotalCoins(number, time)
                campaign.total_time = time
                campaign.channel_id = snippet?.channelId.toString()
                campaign.status = "0"
                campaign.created_at = Utils.getDateTime()
                campaign.type = type

                mainViewModel.saveCampaign(campaign, object : OnCampaignAdded {
                    override fun onCampaignAdded(status: Boolean) {
                        if (status) {
                            Toast.makeText(
                                this@AddCampaignActivity,
                                "Campaign Started",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@AddCampaignActivity,
                                "Some error occurred. Please try again. ",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })
            }
        }
    }

    private fun getTotalCoins(totalNumber: String, totalTime: String): String{
        val coins = totalNumber.toInt() * totalTime.toInt()
        return coins.toString()
    }
}