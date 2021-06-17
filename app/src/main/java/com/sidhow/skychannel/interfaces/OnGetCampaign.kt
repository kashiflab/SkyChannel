package com.sidhow.skychannel.interfaces

import com.sidhow.skychannel.model.Campaign

interface OnGetCampaign<T> {
    fun onGetCampaign(status: Boolean, result: List<Campaign>)
}