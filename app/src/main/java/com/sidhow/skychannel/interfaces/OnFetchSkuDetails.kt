package com.sidhow.skychannel.interfaces

import com.sidhow.skychannel.model.SkuProducts

interface OnFetchSkuDetails {
    fun onFetchSkuDetails(status: Boolean, skuDetails: List<SkuProducts>)
}