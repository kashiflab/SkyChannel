package com.inventerit.skychannel.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.inventerit.skychannel.MainRepository
import com.inventerit.skychannel.interfaces.*
import com.inventerit.skychannel.model.Api
import com.inventerit.skychannel.model.Campaign
import com.inventerit.skychannel.model.User

class MainViewModel(application: Application): AndroidViewModel(application) {

    private var myRepo: MainRepository = MainRepository.getRepo()

    fun getData(onCompleteListener: OnComplete<User>) {
        myRepo.getUserData(onCompleteListener)
    }

    fun saveCampaign(campaign: Campaign, onCampaignAdded: OnCampaignAdded){
        myRepo.saveCampaign(campaign,onCampaignAdded)
    }

    fun getSubsCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        myRepo.getSubsCampaigns(onGetCampaign)
    }

    fun getLikesCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        myRepo.getLikesCampaigns(onGetCampaign)
    }

    fun getViewsCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        myRepo.getViewsCampaigns(onGetCampaign)
    }

    fun getCommentsCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        myRepo.getSubsCampaigns(onGetCampaign)
    }

    fun getAllCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        myRepo.getAllCampaigns(onGetCampaign)
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
}