package com.inventerit.skychannel.interfaces

import com.inventerit.skychannel.model.Api

interface OnVideoDetails<T> {
    fun onVideoDetails(status: Boolean, result: Api?)
}