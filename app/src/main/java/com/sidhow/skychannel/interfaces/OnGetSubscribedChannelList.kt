package com.sidhow.skychannel.interfaces

import com.google.api.services.youtube.model.Subscription

interface OnGetSubscribedChannelList {
    fun onGetSubscribedChannelList(status: Boolean, subscribedChannels: List<Subscription>)
}