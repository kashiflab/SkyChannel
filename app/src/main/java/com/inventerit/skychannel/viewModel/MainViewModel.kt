package com.inventerit.skychannel.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.inventerit.skychannel.MainRepository
import com.inventerit.skychannel.`interface`.OnComplete
import com.inventerit.skychannel.model.User

class MainViewModel(application: Application): AndroidViewModel(application) {

    private var myRepo: MainRepository

    init {
        myRepo = MainRepository.getRepo()
    }

    fun getData(onCompleteListener: OnComplete<User>) {
        myRepo.getUserData(onCompleteListener)
    }
}