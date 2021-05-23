package com.inventerit.skychannel.interfaces

import com.inventerit.skychannel.model.SkuProducts

interface OnFetchSkuDetails {
    fun onFetchSkuDetails(status: Boolean, skuDetails: List<SkuProducts>)
}