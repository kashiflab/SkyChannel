package com.inventerit.skychannel.model

import com.google.gson.annotations.SerializedName

class Campaign {
    var id: String = ""
    var user_id: String = ""
    var created_at: String = ""
    var video_id: String = ""
    var video_title: String = ""
    var channel_id: String = ""
    var total_number: String = ""
    var current_number: String = ""
    var total_time: String = ""
    var total_coins: String = ""
    var status: String = "" //0 for uncompleted 1 for completed 2 for terminated
    var type: String = "" //0 for subscribe 1 for like 2 for views 3 for comments
}