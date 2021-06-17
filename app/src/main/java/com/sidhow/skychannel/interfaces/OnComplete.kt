package com.sidhow.skychannel.interfaces

import com.sidhow.skychannel.model.User

interface OnComplete<T> {
    fun onCompleteTask(status: Boolean,result: User)
}