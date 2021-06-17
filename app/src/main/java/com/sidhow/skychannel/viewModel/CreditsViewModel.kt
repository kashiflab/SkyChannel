package com.sidhow.skychannel.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.sidhow.skychannel.interfaces.OnFetchSkuDetails
import com.sidhow.skychannel.repo.MainRepository

class CreditsViewModel(application: Application): AndroidViewModel(application) {

    private var myRepo: MainRepository = MainRepository.getRepo()



    fun fetchSkuDetails(onFetchSkuDetails: OnFetchSkuDetails){
        myRepo.fetchSkuDetails(onFetchSkuDetails)
    }
}