package com.sidhow.skychannel.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.sidhow.skychannel.interfaces.OnComplete
import com.sidhow.skychannel.interfaces.OnGetCampaign
import com.sidhow.skychannel.model.Campaign
import com.sidhow.skychannel.model.User
import com.sidhow.skychannel.repo.MainRepository
import com.sidhow.skychannel.room.VideosDatabase

class SaveCampaignViewModel(application: Application): AndroidViewModel(application) {

    private var myRepo: MainRepository = MainRepository.getRepo()
    private var database = VideosDatabase.getInstance(application)

    fun saveSubsCampaigns(onComplete: OnComplete<User>){

        Thread {
            val db = database.videosDao().getSubs("0")
            val channelId: MutableList<String> = ArrayList()
            for(camp in db){
                channelId.add(camp.channelId)
            }

            myRepo.getSubsCampaigns(object : OnGetCampaign<List<Campaign>> {
                override fun onGetCampaign(status: Boolean, result: List<Campaign>) {
                    for (campaign in result) {
                        if (db.isNotEmpty()) {
                            if(!channelId.contains(campaign.channel_id)){
                                Thread {
                                    database.campaignDAO().insertCampaign(campaign)
                                    Log.i("RoomDB", "Campaign Saved")
                                }.start()
                            }
                        } else {
                            Thread {
                                database.campaignDAO().insertCampaign(campaign)
                                Log.i("RoomDB", "Campaign Saved")
                            }.start()
                        }
                    }
                    onComplete.onCompleteTask(true, User())
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

            myRepo.getViewsCampaigns(object : OnGetCampaign<List<Campaign>> {
                override fun onGetCampaign(status: Boolean, result: List<Campaign>) {
                    for (campaign in result) {
                        if (db.isNotEmpty()) {
                            if(!videoId.contains(campaign.video_id)){
                                Thread {
                                    database.campaignDAO().insertCampaign(campaign)
                                }.start()
                                Log.i("RoomDB", "Campaign Saved")
                            }
                        } else {
                            Thread {
                                database.campaignDAO().insertCampaign(campaign)
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

            myRepo.getLikesCampaigns(object : OnGetCampaign<List<Campaign>> {
                override fun onGetCampaign(status: Boolean, result: List<Campaign>) {
                    for (campaign in result) {
                        if (db.isNotEmpty()) {
                            if(!videoId.contains(campaign.video_id)){
                                Thread {
                                    database.campaignDAO().insertCampaign(campaign)
                                }.start()
                                Log.i("RoomDB", "Campaign Saved")
                            }
                        } else {
                            Thread {
                                database.campaignDAO().insertCampaign(campaign)
                            }.start()
                            Log.i("RoomDB", "Campaign Saved")
                        }
                    }
                }
            })
        }.start()
    }

}