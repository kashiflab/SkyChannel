package com.sidhow.skychannel.ui.subscriptions

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sidhow.skychannel.Adapter.SubscriptionAdapter
import com.sidhow.skychannel.constant.Constants
import com.sidhow.skychannel.databinding.FragmentSubsBinding
import com.sidhow.skychannel.interfaces.OnGetCampaign
import com.sidhow.skychannel.model.Campaign
import com.sidhow.skychannel.room.VideosDatabase
import com.sidhow.skychannel.room.model.Videos
import com.sidhow.skychannel.viewModel.MainViewModel


class SubsFragment : Fragment() {

    private lateinit var binding: FragmentSubsBinding
    private var adapter: SubscriptionAdapter? = null

    private lateinit var mainViewModel: MainViewModel
    private lateinit var database: VideosDatabase
    private var subs: List<Videos> = ArrayList()

    private lateinit var campaignList: MutableList<Campaign>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSubsBinding.inflate(layoutInflater)

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        binding.subs.layoutManager = LinearLayoutManager(context)
        binding.subs.setHasFixedSize(true)

        val subscribed : MutableList<Videos> = ArrayList()
        Thread{
            database = VideosDatabase.getInstance(context)
            subs = database.videosDao().getSubs("0")
            Log.i("RoomDB",subs.size.toString())

        }.start()

        mainViewModel.getAllCampaigns(object: OnGetCampaign<List<Campaign>>{
            override fun onGetCampaign(status: Boolean, result: List<Campaign>) {
                campaignList = ArrayList()
                val channelId: MutableList<String> = ArrayList()
                Handler(Looper.myLooper()!!).postDelayed({
                    if(subs.isNotEmpty()) {
                        for (sub in subs) {
                            for (campaign in result){
                                if(sub.channelId.toString() == campaign.channel_id &&
                                    campaign.type== Constants.SUBSCRIBE_TYPE.toString()
                                    && !channelId.contains(campaign.video_id)
                                ){
                                    channelId.add(campaign.video_id)
                                    campaignList.add(campaign)
                                }
                            }
                        }
                        adapter = SubscriptionAdapter(context!!,campaignList)
                        binding.subs.adapter = adapter
                    }
                },500)
            }
        })


        return binding.root
    }

}