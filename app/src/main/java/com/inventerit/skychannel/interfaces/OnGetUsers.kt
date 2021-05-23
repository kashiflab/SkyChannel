package com.inventerit.skychannel.interfaces

import com.inventerit.skychannel.model.User

interface OnGetUsers {
    fun onGetUsers(status: Boolean, user: List<User>)
}