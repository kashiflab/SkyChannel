package com.sidhow.skychannel.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.sidhow.skychannel.Utils
import com.sidhow.skychannel.constant.PrefKeys
import com.sidhow.skychannel.databinding.ActivityAddCampaignBinding
import com.sidhow.skychannel.interfaces.OnCampaignAdded
import com.sidhow.skychannel.interfaces.OnVideoDetails
import com.sidhow.skychannel.model.Api
import com.sidhow.skychannel.model.Campaign
import com.sidhow.skychannel.model.ContentDetails
import com.sidhow.skychannel.model.Snippet
import com.sidhow.skychannel.viewModel.MainViewModel
import com.tramsun.libs.prefcompat.Pref
import kotlinx.android.synthetic.main.fragment_campaign.*
import java.util.*


class AddCampaignActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCampaignBinding

    private var link: String = ""
    private var type: String = ""

    private var snippet: Snippet? = null
    private var duration = ""
    val myTimer = Timer()
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
                    Toast.makeText(this@AddCampaignActivity, "Server under maintenance", Toast.LENGTH_LONG).show()
                    onBackPressed()
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
                if(coins.toInt()<binding.coins.text.toString().toString().split(" ")[0].toInt()){
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
                            campaign.paused = "0"
                            campaign.coins = getCoins(time.toInt())
                            campaign.channelTitle = snippet?.channelTitle.toString()
                            campaign.channelImage = snippet?.thumbnail?.default?.url.toString()

//                            Toast.makeText(this,"Coins: ${campaign.coins}",Toast.LENGTH_LONG).show()

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
        when (totalCoins) {
            !in 20 downTo 0 -> {
                coins = genRandomNumber(30,65).toString()
            }
            !in 40 downTo 20 -> {
                coins = genRandomNumber(150,200).toString()
            }
            !in 60 downTo 40 -> {
                coins = genRandomNumber(350,600).toString()
            }
            !in 80 downTo 60 -> {
                coins = genRandomNumber(650,740).toString()
            }
            !in 100 downTo 80 -> {
                coins = genRandomNumber(740,990).toString()
            }
            !in 120 downTo 100 -> {
                coins = genRandomNumber(990,1220).toString()
            }
            !in 140 downTo 120 -> {
                coins = genRandomNumber(1220,1450).toString()
            }
            !in 160 downTo 140 -> {
                coins = genRandomNumber(1450,1780).toString()
            }
            !in 180 downTo 160 -> {
                coins = genRandomNumber(1780,1920).toString()
            }
            !in 200 downTo 180 -> {
                coins = genRandomNumber(1950,2250).toString()
            }
            !in 240 downTo 200 -> {
                coins = genRandomNumber(2250,2450).toString()
            }
            !in 280 downTo 240 -> {
                coins = genRandomNumber(2450,2750).toString()
            }
            !in 320 downTo 280 -> {
                coins = genRandomNumber(2750,2950).toString()
            }
            !in 360 downTo 320 -> {
                coins = genRandomNumber(2950,3250).toString()
            }
            !in 400 downTo 360 -> {
                coins = genRandomNumber(3250,3450).toString()
            }
            !in 420 downTo 400 -> {
                coins = genRandomNumber(3450,3650).toString()
            }
            !in 460 downTo 420 -> {
                coins = genRandomNumber(3650,3950).toString()
            }
            !in 500 downTo 460 -> {
                coins = genRandomNumber(3950,4250).toString()
            }
            !in 540 downTo 500 -> {
                coins = genRandomNumber(4250,4550).toString()
            }
            else -> {
                coins = "5000"
            }
        }
        return coins
    }

    private fun genRandomNumber(min: Int, max: Int): Int {
        val rand = Random()
        return rand.nextInt(max - min + 1) + min
    }

    override fun onPause() {
        myTimer.cancel()
        super.onPause()
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
        if(Pref.getString(PrefKeys.isVIP)=="false") {
            var coins = totalNumber.toInt() * totalTime.toInt()
            return coins.toString()
        }else{
            var coins = (totalNumber.toInt() * totalTime.toInt()*80)/100
            return coins.toString()
        }
    }
}