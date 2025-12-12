package com.example.travelguide

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform