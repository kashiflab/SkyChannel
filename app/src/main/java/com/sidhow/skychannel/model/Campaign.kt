package com.sidhow.skychannel.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "campaign")
class Campaign: Serializable {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("pId")
    @ColumnInfo(name = "PID")
    var pId: Int = 0

    @SerializedName("id")
    @ColumnInfo(name = "id")
    var id: String = ""

    @SerializedName("user_id")
    @ColumnInfo(name = "user_id")
    var user_id: String = ""

    @SerializedName("created_at")
    @ColumnInfo(name = "created_at")
    var created_at: String = ""

    @SerializedName("video_id")
    @ColumnInfo(name = "video_id")
    var video_id: String = ""

    @SerializedName("video_title")
    @ColumnInfo(name = "video_title")
    var video_title: String = ""

    @SerializedName("channel_id")
    @ColumnInfo(name = "channel_id")
    var channel_id: String = ""

    @SerializedName("total_number")
    @ColumnInfo(name = "total_number")
    var total_number: String = ""

    @SerializedName("current_number")
    @ColumnInfo(name = "current_number")
    var current_number: String = ""

    @SerializedName("total_time")
    @ColumnInfo(name = "total_time")
    var total_time: String = ""

    @SerializedName("total_coins")
    @ColumnInfo(name = "total_coins")
    var total_coins: String = ""

    @SerializedName("status")
    @ColumnInfo(name = "status")
    var status: String = "" //0 for uncompleted 1 for completed 2 for terminated

    @SerializedName("type")
    @ColumnInfo(name = "type")
    var type: String = "" //0 for subscribe 1 for like 2 for views 3 for comments

    @SerializedName("isPaused")
    @ColumnInfo(name = "isPaused")
    var paused: String = "" //0 for resumed 1 for paused

    @SerializedName("coins")
    @ColumnInfo(name = "coins")
    var coins: String = ""

    @SerializedName("channelTitle")
    @ColumnInfo(name = "channelTitle")
    var channelTitle: String = ""

    @SerializedName("channelImage")
    @ColumnInfo(name = "channelImage")
    var channelImage: String = ""



}