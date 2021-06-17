package com.sidhow.skychannel.interfaces

import com.sidhow.skychannel.model.User

interface OnGetUsers {
    fun onGetUsers(status: Boolean, user: List<User>)
}