package com.inventerit.skychannel.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.inventerit.skychannel.MainRepository
import com.inventerit.skychannel.interfaces.OnCampaignAdded
import com.inventerit.skychannel.interfaces.OnComplete
import com.inventerit.skychannel.interfaces.OnGetCampaign
import com.inventerit.skychannel.interfaces.OnVideoDetails
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

    fun getAllCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        myRepo.getAllCampaigns(onGetCampaign)
    }

    fun getUserCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        myRepo.getUserCampaigns(onGetCampaign)
    }

    fun getVideoDetails(video_id: String, onVideoDetails: OnVideoDetails<Api>){
        myRepo.getVideoDetails(video_id,onVideoDetails)
    }
}