package com.inventerit.skychannel.interfaces

import com.inventerit.skychannel.model.Campaign

interface OnGetCampaign<T> {
    fun onGetCampaign(status: Boolean, result: List<Campaign>)
}