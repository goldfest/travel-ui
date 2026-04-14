package com.travelguide.core

object NetworkConfig {

    const val BASE_URL = "https://turban-financial-penholder.ngrok-free.dev"

    const val AUTH_HOST = BASE_URL
    const val AUTH_API = "$BASE_URL/api"
    const val CITY_API = "$BASE_URL/api/cities"
    const val POI_API = "$BASE_URL/api/poi"
    const val REVIEW_API = "$BASE_URL/api/reviews"
    const val PERSONALIZATION_API = "$BASE_URL/api/personalization"
    const val ROUTE_API = "$BASE_URL/api/routes"
    const val NOTIFICATION_API = "$BASE_URL/api/notifications"
}