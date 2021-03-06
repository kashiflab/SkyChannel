package com.sidhow.skychannel.model

data class Item(
    val etag: String,
    val id: String,
    val kind: String,
    val snippet: Snippet,
    val contentDetails: ContentDetails
)