package com.sidhow.skychannel.interfaces

import com.sidhow.skychannel.model.Api

interface OnVideoDetails<T> {
    fun onVideoDetails(status: Boolean, result: Api?)
}