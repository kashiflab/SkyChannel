package com.sidhow.skychannel.model

data class Snippet(
    val categoryId: String,
    val channelId: String,
    val channelTitle: String,
    val defaultAudioLanguage: String,
    val description: String,
    val liveBroadcastContent: String,
    val publishedAt: String,
    val title: String,
    val thumbnail: Thumbnail
)