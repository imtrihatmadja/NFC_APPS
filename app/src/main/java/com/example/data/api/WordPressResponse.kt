package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WPPostResponse(
    val id: Int,
    val date: String,
    val title: WPRendered,
    val content: WPRendered,
    val excerpt: WPRendered,
    val link: String,
    @Json(name = "jetpack_featured_media_url") val featuredImageUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class WPRendered(
    val rendered: String
)
