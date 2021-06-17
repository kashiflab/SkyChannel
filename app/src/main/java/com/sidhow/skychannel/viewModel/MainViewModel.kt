package com.sidhow.skychannel.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.sidhow.skychannel.repo.MainRepository
import com.sidhow.skychannel.interfaces.*
import com.sidhow.skychannel.model.Api
import com.sidhow.skychannel.model.Campaign
import com.sidhow.skychannel.model.User
import com.sidhow.skychannel.room.VideosDatabase

class MainViewModel(application: Application): AndroidViewModel(application) {

    private var myRepo: MainRepository = MainRepository.getRepo()
    private var database = VideosDatabase.getInstance(application)

    fun saveCampaign(campaign: Campaign, previousCoins: String, onCampaignAdded: OnCampaignAdded){
        myRepo.saveCampaign(campaign,previousCoins, onCampaignAdded)
    }

    fun saveSubsCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){

        Thread {
            val db = database.videosDao().getSubs("0")
            val channelId: MutableList<String> = ArrayList()
            for(camp in db){
                channelId.add(camp.channelId)
                Log.i("SubscribedChannel: ",camp.channelId)
            }

            val list: MutableList<String> = ArrayList()
            val db2 = database.campaignDAO().getCampaignsByType("0")
            for(db1 in db2){
                list.add(db1.channel_id)
            }

            myRepo.getSubsCampaigns(object : OnGetCampaign<List<Campaign>> {
                override fun onGetCampaign(status: Boolean, result: List<Campaign>) {
                    for (campaign in result) {
                        if (db.isNotEmpty()) {
                            if(!channelId.contains(campaign.channel_id)){
                                Thread {
                                    try {
                                        if(!list.contains(campaign.channel_id)) {
                                            database.campaignDAO().insertCampaign(campaign)
                                            list.add(campaign.channel_id)
                                            Log.i(
                                                "SubscribedChannel: ",
                                                "Saved: ${campaign.channel_id}, ${campaign.channelTitle}"
                                            )
                                        }
                                    }catch (e:Exception){
                                        e.printStackTrace()
                                    }
                                }.start()
                            }
                        } else {

                            Thread {
                                try {
                                    if(!list.contains(campaign.channel_id)) {
                                        database.campaignDAO().insertCampaign(campaign)
                                        list.add(campaign.channel_id)
                                        Log.i(
                                            "SubscribedChannel: ",
                                            "Saved: ${campaign.channel_id}, ${campaign.channelTitle}"
                                        )
                                    }
                                }catch (e: Exception){
                                    e.printStackTrace()
                                }
                            }.start()
                        }
                    }
                    if(list.isNotEmpty()) {
                        Thread {
                            val savedCampaigns = database.campaignDAO().getCampaignsByType("0")
                            onGetCampaign.onGetCampaign(true, savedCampaigns)
                        }.start()
                    }else{
                        onGetCampaign.onGetCampaign(false, emptyList())
                    }
                }
            })
        }.start()
    }
    fun saveViewsCampaigns(){

        Thread {
            val db = database.videosDao().getSubs("2")
            val videoId: MutableList<String> = ArrayList()
            for(camp in db){
                videoId.add(camp.videoId)
            }
            val list: MutableList<String> = ArrayList()
            val db2 = database.campaignDAO().getCampaignsByType("2")
            for(db1 in db2){
                list.add(db1.video_id)
            }

            myRepo.getViewsCampaigns(object : OnGetCampaign<List<Campaign>> {
                override fun onGetCampaign(status: Boolean, result: List<Campaign>) {
                    for (campaign in result) {
                        if (db.isNotEmpty()) {
                            if(!videoId.contains(campaign.video_id)){
                                Thread {
                                    try {
                                        if(!list.contains(campaign.video_id)) {
                                            database.campaignDAO().insertCampaign(campaign)
                                            list.add(campaign.video_id)
                                            Log.i(
                                                "SubscribedChannel: ",
                                                "Saved: ${campaign.video_id}, ${campaign.channelTitle}"
                                            )
                                        }
                                    }catch (e:Exception){
                                        e.printStackTrace()
                                    }
                                }.start()
                                Log.i("RoomDB", "Campaign Saved")
                            }
                        } else {
                            Thread {
                                    try {
                                        if(!list.contains(campaign.video_id)) {
                                        database.campaignDAO().insertCampaign(campaign)
                                        list.add(campaign.video_id)
                                    }
                                }catch (e: Exception){
                                    e.printStackTrace()
                                }
                            }.start()

                            Log.i("RoomDB", "Campaign Saved")
                        }
                    }
                }
            })
        }.start()
    }
    fun saveLikesCampaigns(){

        Thread {
            val db = database.videosDao().getSubs("1")
            val videoId: MutableList<String> = ArrayList()
            for(camp in db){
                videoId.add(camp.videoId)
            }
            val list: MutableList<String> = ArrayList()
            val db2 = database.campaignDAO().getCampaignsByType("1")
            for(db1 in db2){
                list.add(db1.video_id)
            }

            myRepo.getLikesCampaigns(object : OnGetCampaign<List<Campaign>> {
                override fun onGetCampaign(status: Boolean, result: List<Campaign>) {
                    for (campaign in result) {
                        if (db.isNotEmpty()) {
                            if(!videoId.contains(campaign.video_id)){
                                Thread {
                                    try {if(!list.contains(campaign.video_id)) {
                                        database.campaignDAO().insertCampaign(campaign)
                                        list.add(campaign.video_id)
                                        Log.i("RoomDB", "Campaign Saved")
                                    }
                                    }catch (e:Exception){
                                        e.printStackTrace()
                                    }
                                }.start()
                            }
                        } else {
                            Thread {
                                try {
                                    if(!list.contains(campaign.video_id)) {
                                        database.campaignDAO().insertCampaign(campaign)
                                        list.add(campaign.video_id)
                                        Log.i("RoomDB", "Campaign Saved")
                                    }
                                }catch (e: Exception){
                                    e.printStackTrace()
                                }
                            }.start()
                        }
                    }
                }
            })
        }.start()
    }

    fun getSubsCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        Thread {
            // get all campaigns for subscription
            val campaign = database.campaignDAO().getCampaignsByType("0")
            // get all subscribed channels
            val alreadySubscribed = database.videosDao().getSubs("0")

            // save all subscribed channels into a list of string
            val subs: MutableList<String> = ArrayList()
            for (subscribed in alreadySubscribed){
                subs.add(subscribed.channelId)
            }

            val newList: MutableList<Campaign> = ArrayList()
            if (alreadySubscribed != null) {
                if (alreadySubscribed.size > 0) {

                    for (camp in campaign) {
                        if (!subs.contains(camp.channel_id)) {
                            newList.add(camp)
                        }
                    }

                    if (newList.size > 0) {
                        onGetCampaign.onGetCampaign(true, newList)
                    } else {
                        onGetCampaign.onGetCampaign(false, emptyList())
                    }

                } else {
                    onGetCampaign.onGetCampaign(true, campaign)
                }
            } else {
                onGetCampaign.onGetCampaign(true, campaign)
            }
        }.start()
    }

    fun getLikesCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        Thread {
            //get all like campaigns
            val campaign = database.campaignDAO().getCampaignsByType("1")

            //get all liked videos
            val alreadySubscribed = database.videosDao().getSubs("1")

            // save all liked videos into a list of string
            val subs: MutableList<String> = ArrayList()
            for (subscribed in alreadySubscribed){
                subs.add(subscribed.videoId)
            }

            val newList: MutableList<Campaign> = ArrayList()

            if (alreadySubscribed != null) {
                if (alreadySubscribed.size > 0) {
                    for (camp in campaign) {
                        if (!subs.contains(camp.video_id)) {
                            newList.add(camp)
                        }
                    }
                    if (newList.size > 0) {
                        onGetCampaign.onGetCampaign(true, newList)
                    } else {
                        onGetCampaign.onGetCampaign(false, emptyList())
                    }
                } else {
                    onGetCampaign.onGetCampaign(true, campaign)
                }
            } else {
                onGetCampaign.onGetCampaign(true, campaign)
            }
        }.start()
    }

    fun getViewsCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        Thread {
            val campaign = database.campaignDAO().getCampaignsByType("2")
            val alreadySubscribed = database.videosDao().getSubs("2")
            val subs: MutableList<String> = ArrayList()
            for (subscribed in alreadySubscribed){
                subs.add(subscribed.videoId)
            }
            val newList: MutableList<Campaign> = ArrayList()
            if (alreadySubscribed != null) {
                if (alreadySubscribed.size > 0) {
                    for (camp in campaign) {
                        if (!subs.contains(camp.video_id)) {
                            newList.add(camp)
                        }
                    }
                    if (newList.size > 0) {
                        onGetCampaign.onGetCampaign(true, newList)
                    } else {
                        onGetCampaign.onGetCampaign(false, emptyList())
                    }
                } else {
                    onGetCampaign.onGetCampaign(true, campaign)
                }
            } else {
                onGetCampaign.onGetCampaign(true, campaign)
            }
        }.start()
    }

    fun getCommentsCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        myRepo.getSubsCampaigns(onGetCampaign)
    }

    fun getAllCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        myRepo.getAllCampaigns(onGetCampaign)
    }

    fun addUser(campaignId: String, uid: String){
        myRepo.addUser(campaignId,uid)
    }

    fun getUserCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        myRepo.getUserCampaigns(onGetCampaign)
    }

    fun getVideoDetails(video_id: String, onVideoDetails: OnVideoDetails<Api>){
        myRepo.getVideoDetails(video_id,onVideoDetails)
    }

    fun updateCoins(coins: String, onLike: LikeListener){
        myRepo.updateCoins(coins,onLike)
    }

    fun updateCampaignStatus(campaignId: String, campaignNumber: String, onCampaignStatus: OnCampaignStatus){
        myRepo.updateCampaignStatus(campaignId,campaignNumber,onCampaignStatus)
    }

    fun getCampaignUsers(campaignId: String,onGetUsers: OnGetUsers){
        myRepo.getCampaignUsers(onGetUsers,campaignId)
    }

    fun getData(onCompleteListener: OnComplete<User>) {
        myRepo.getUserData(onCompleteListener)
    }

    fun endCompletedCampaigns(onCampaignCompleted: OnCampaignCompleted){
        myRepo.endAllCompletedCampaigns(onCampaignCompleted)
    }


}