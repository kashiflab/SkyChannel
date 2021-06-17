package com.sidhow.skychannel.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.sidhow.skychannel.Adapter.CampaignUserAdapter
import com.sidhow.skychannel.R
import com.sidhow.skychannel.constant.Constants
import com.sidhow.skychannel.databinding.ActivityCampaignDetailBinding
import com.sidhow.skychannel.interfaces.OnGetUsers
import com.sidhow.skychannel.model.Campaign
import com.sidhow.skychannel.model.User
import com.sidhow.skychannel.viewModel.MainViewModel

class CampaignDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCampaignDetailBinding
    private lateinit var campaign: Campaign
    private var item: MenuItem? = null

    private var TAG = "CampaignDetailActivity"

    private var adapter: CampaignUserAdapter? = null

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCampaignDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        campaign = intent.getSerializableExtra("campaign") as Campaign

        if(campaign.paused=="1"){
            binding.view2.setBackgroundColor(ContextCompat.getColor(this,R.color.light_red))
        }else{
            binding.view2.setBackgroundColor(ContextCompat.getColor(this,R.color.black))
        }

        binding.userRV.layoutManager = LinearLayoutManager(this)
        binding.userRV.setHasFixedSize(true)

        mainViewModel.getCampaignUsers(campaign.id,object :OnGetUsers{
            override fun onGetUsers(status: Boolean, user: List<User>) {
                if(status){
                    adapter = CampaignUserAdapter(this@CampaignDetailActivity,user)
                    binding.userRV.adapter = adapter
                    if(user.isEmpty()){
                        binding.nodata.visibility = View.VISIBLE
                    }
                }else{
                    binding.nodata.visibility = View.VISIBLE
                    Log.i(TAG,"Some error occurred")
                }
            }
        })
        val imageUrl = "https://img.youtube.com/vi/${campaign.video_id}/0.jpg"

        Glide.with(this).load(imageUrl).into(binding.cpImage)
        binding.time.text = campaign.total_time
        binding.progress.max = campaign.total_number.toInt()
        binding.progress.progress = campaign.current_number.toInt()
        binding.videoTitle.text = campaign.video_title

        var view = ""
        when (campaign.type) {
            "0" -> {
                view = "Subscriber(s)"
            }
            "1" -> {
                view = "Like(s)"
            }
            "2" -> {
                view = "View(s)"
            }
        }
        binding.views.text = "${campaign.current_number}/${campaign.total_number} $view"

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        Handler(Looper.myLooper()!!).postDelayed(Runnable {
            item = menu?.findItem(R.id.paused)
            if(campaign?.paused=="0"){
                item?.title = "Pause"
            }else{
                item?.title = "Resume"
            }
        },1500)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.paused) {
            if (campaign?.paused=="0"){
                updateCampaign("1")
            }else{
                updateCampaign("0")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateCampaign(s: String) {
        val ref = FirebaseDatabase.getInstance().reference
        ref.child(Constants.campaigns).child(campaign.id).child(Constants.is_paused).setValue(s)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    if(s=="1"){
                        item?.title = "Resume"
                        campaign.paused = "1"
                        Toast.makeText(this,"Paused",Toast.LENGTH_SHORT).show()
                    }else{
                        item?.title = "Pause"
                        campaign.paused = "0"
                        Toast.makeText(this,"Resumed",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(this,"Some error occurred",Toast.LENGTH_SHORT).show()
                }
            }
    }


}