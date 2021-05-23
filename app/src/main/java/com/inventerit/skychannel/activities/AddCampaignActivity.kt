package com.inventerit.skychannel.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.inventerit.skychannel.Utils
import com.inventerit.skychannel.constant.PrefKeys
import com.inventerit.skychannel.databinding.ActivityAddCampaignBinding
import com.inventerit.skychannel.interfaces.OnCampaignAdded
import com.inventerit.skychannel.interfaces.OnVideoDetails
import com.inventerit.skychannel.model.Api
import com.inventerit.skychannel.model.Campaign
import com.inventerit.skychannel.model.ContentDetails
import com.inventerit.skychannel.model.Snippet
import com.inventerit.skychannel.viewModel.MainViewModel
import com.tramsun.libs.prefcompat.Pref
import kotlinx.android.synthetic.main.fragment_campaign.*
import java.util.*


class AddCampaignActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCampaignBinding

    private var link: String = ""
    private var type: String = ""

    private var snippet: Snippet? = null
    private var duration = ""

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

        mainViewModel.getVideoDetails(videoId, object : OnVideoDetails<Api> {
            override fun onVideoDetails(status: Boolean, result: Api?) {
                if (status) {
                    snippet = result?.items?.get(0)?.snippet
                    binding.videoTitle.text = snippet?.title.toString()
                    getDuration(result?.items?.get(0)?.contentDetails!!)
                } else {
                    Toast.makeText(this@AddCampaignActivity, "Some error occurred", Toast.LENGTH_LONG).show()
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
        var number = "0"
        var time = "0"
        val myTimer = Timer()
        myTimer.schedule(object : TimerTask() {
            override fun run() {
                number = binding.number.text.toString()
                time = binding.time.text.toString()
                if (number.isNotEmpty() && time.isNotEmpty()) {
                    //your function
                    Log.i("runOnUiThread", "This is run")
                    runOnUiThread {
                        kotlin.run {
                            binding.coins.text = "${getTotalCoins(number, time)} Coins"
                        }
                    }
                }
            }
        }, 1000, 1000)



        binding.createBtn.setOnClickListener{
            val number = binding.number.text.toString()
            val time = binding.time.text.toString()
            val campaign =  Campaign()

            val coins = Pref.getString(PrefKeys.coins)
            if(number.isEmpty() || time.isEmpty()) {
                Toast.makeText(
                        this@AddCampaignActivity,
                        "All fields are required",
                        Toast.LENGTH_LONG
                ).show()
            }else{
                if(coins.toInt()<binding.coins.text.toString().toInt()){
                    AlertDialog.Builder(this)
                        .setTitle("Buy Coins")
                        .setMessage("You don't have sufficient coins to run a campaign. Do you want to buy some coins?")
                        .setPositiveButton("Buy Coins"){ dialog, _ ->
                            startActivity(Intent(this@AddCampaignActivity,BuyPointsActivity::class.java))
                            dialog.dismiss()
                        }.setNegativeButton("No"){ dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }else{
                    when {
                        time.toInt() > duration.toInt() ->{
                            Toast.makeText(this@AddCampaignActivity, "Time should be less than video duration", Toast.LENGTH_LONG).show()
                        }
                        number.toInt() !in 5001 downTo 9 -> {
                            binding.number.error = "Invalid Number"
                        }
                        time.toInt() !in 5001 downTo 9 -> {
                            binding.time.error = "Invalid Number"
                        }
                        else -> {
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
                            campaign.isPaused = "0"
                            campaign.coins = getCoins(campaign.total_coins.toInt())
                            campaign.channelTitle = snippet?.channelTitle.toString()
                            campaign.channelImage = snippet?.thumbnail?.default?.url.toString()

                            mainViewModel.saveCampaign(campaign, coins, object : OnCampaignAdded {
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
            }
        }
    }


    private fun getCoins(totalCoins: Int): String {
        var coins = "30"

        when {
            totalCoins !in 201 downTo 0 -> {
                coins = genRandomNumber(25,35).toString()
            }
            totalCoins !in 401 downTo 201 -> {
                coins = genRandomNumber(35,55).toString()
            }
            totalCoins !in 501 downTo 401 -> {
                coins = genRandomNumber(55,85).toString()
            }
            totalCoins !in 601 downTo 501 -> {
                coins = genRandomNumber(85,140).toString()
            }
            totalCoins !in 701 downTo 601 -> {
                coins = genRandomNumber(140,190).toString()
            }
            totalCoins !in 801 downTo 701 -> {
                coins = genRandomNumber(190,220).toString()
            }
            totalCoins !in 901 downTo 801 -> {
                coins = genRandomNumber(220,250).toString()
            }
            totalCoins !in 1001 downTo 901 -> {
                coins = genRandomNumber(250,280).toString()
            }
            totalCoins !in 1201 downTo 1001 -> {
                coins = genRandomNumber(280,320).toString()
            }
            totalCoins !in 1301 downTo 1201 -> {
                coins = genRandomNumber(350,450).toString()
            }
            else ->{
                coins = "500"
            }
        }
        return coins
    }

    private fun genRandomNumber(min: Int, max: Int){
        val rand = Random()
        val randomNum: Int = rand.nextInt(max - min + 1) + min
    }

    private fun getDuration(contentDetails: ContentDetails) {
        val time = contentDetails.duration
        val hour: String
        val min: String
        val second: String
        when {
            time.contains("H") -> {
                hour = time.split("H")[0].split("PT")[1]
                min = time.split("PT")[1].split("H")[1].split("M")[0]
                second = time.split("PT")[1].split("H")[1].split("M")[1].split("S")[0]
                duration = (hour.toInt() * 60 + min.toInt() * 60 + second.toInt()).toString()
            }
            time.contains("M") -> {
                min = time.split("PT")[1].split("M")[0]
                second = time.split("PT")[1].split("M")[1].split("S")[0]
                duration = (min.toInt() * 60 + second.toInt()).toString()
            }
            time.contains("S") -> {
                second = time.split("PT")[1].split("S")[0]
                duration = second
            }
        }
    }

    private fun getTotalCoins(totalNumber: String, totalTime: String): String{
        val coins = totalNumber.toInt() * totalTime.toInt()
        return coins.toString()
    }
}