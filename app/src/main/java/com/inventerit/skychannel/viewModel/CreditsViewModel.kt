package com.inventerit.skychannel.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.inventerit.skychannel.interfaces.OnCampaignCompleted
import com.inventerit.skychannel.interfaces.OnComplete
import com.inventerit.skychannel.interfaces.OnFetchSkuDetails
import com.inventerit.skychannel.model.User
import com.inventerit.skychannel.repo.MainRepository

class CreditsViewModel(application: Application): AndroidViewModel(application) {

    private var myRepo: MainRepository = MainRepository.getRepo()

    fun fetchSkuDetails(onFetchSkuDetails: OnFetchSkuDetails){
        myRepo.fetchSkuDetails(onFetchSkuDetails)
    }
    fun getData(onCompleteListener: OnComplete<User>) {
        myRepo.getUserData(onCompleteListener)
    }

    fun endCompletedCampaigns(onCampaignCompleted: OnCampaignCompleted){
        myRepo.endAllCompletedCampaigns(onCampaignCompleted)
    }


}