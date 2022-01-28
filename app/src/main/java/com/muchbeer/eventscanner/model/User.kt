package com.muchbeer.eventscanner.model

data class User(
    val id: String,
    val location: String,
    val name: String,
    val total_likes: Int,
    val username: String,
    val links : UserLinks
)