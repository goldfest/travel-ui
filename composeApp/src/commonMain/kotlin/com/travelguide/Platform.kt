package com.travelguide

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform