package com.muchbeer.eventscanner.model

data class SplashApiItem(
    val created_at: String,
    val description: String,
    val id: String,
    val likes: Int,
    val updated_at: String,
    val urls: Urls,
    val user: User,

)