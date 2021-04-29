package com.inventerit.skychannel.`interface`

import com.inventerit.skychannel.model.User

interface OnComplete<T> {
    fun onCompleteTask(status: Boolean,result: User)
}