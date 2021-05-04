package com.inventerit.skychannel.model

data class Api(
    val etag: String,
    val items: List<Item>,
    val kind: String,
)