package com.travelguide

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.travelguide.auth.AuthRepository
import com.travelguide.auth.TokenStorage
import com.travelguide.city.CityRepository
import com.travelguide.favorite.FavoriteRepository
import com.travelguide.network.HttpClientFactory
import com.travelguide.network.auth.AuthApi
import com.travelguide.network.city.CityApi
import com.travelguide.network.personalization.CollectionApi
import com.travelguide.network.personalization.FavoriteApi
import com.travelguide.network.personalization.SearchHistoryApi
import com.travelguide.network.poi.PoiApi
import com.travelguide.network.review.ReportApi
import com.travelguide.network.review.ReviewApi
import com.travelguide.network.route.RouteApi
import com.travelguide.network.user.UserApi
import com.travelguide.personalisation.CollectionRepository
import com.travelguide.poi.PoiRepository
import com.travelguide.profile.UserRepository
import com.travelguide.review.ReportRepository
import com.travelguide.review.ReviewRepository
import com.travelguide.route.RouteRepository
import com.travelguide.search.SearchHistoryRepository
import com.travelguide.session.SessionManager

class AppContainer(context: Context) {

    val sessionManager = SessionManager()

    private val settings: Settings = SharedPreferencesSettings(
        context.getSharedPreferences("travelguide_settings", Context.MODE_PRIVATE)
    )

    private val authHostUrl = "http://192.168.1.9:8084"
    private val authApiBaseUrl = "$authHostUrl/api"

    private val cityBaseUrl = "http://192.168.1.9:8082/api/cities"
    private val poiBaseUrl = "http://192.168.1.9:8081/api/poi"
    private val reviewBaseUrl = "http://192.168.1.9:8083/api/reviews"

    private val personalizationBaseUrl = "http://192.168.1.9:8085/api/personalization"

    private val routeBaseUrl = "http://192.168.1.9:8087/api/routes"

    val tokenStorage = TokenStorage(settings)

    private val httpClient = HttpClientFactory().create(authApiBaseUrl, tokenStorage)

    private val authApi = AuthApi(httpClient, authApiBaseUrl)
    val authRepository = AuthRepository(authApi, tokenStorage)

    private val userApi = UserApi(httpClient, authApiBaseUrl)
    val userRepository = UserRepository(userApi, authHostUrl)

    private val cityApi = CityApi(httpClient, cityBaseUrl)
    val cityRepository = CityRepository(cityApi)

    private val poiApi = PoiApi(httpClient, poiBaseUrl)
    val poiRepository = PoiRepository(poiApi)

    private val reviewApi = ReviewApi(httpClient, reviewBaseUrl)
    val reviewRepository = ReviewRepository(reviewApi)

    private val reportApi = ReportApi(httpClient, reviewBaseUrl)
    val reportRepository = ReportRepository(reportApi)

    private val favoriteApi = FavoriteApi(httpClient, personalizationBaseUrl)
    val favoriteRepository = FavoriteRepository(favoriteApi, poiRepository)

    private val collectionApi = CollectionApi(httpClient, personalizationBaseUrl)
    val collectionRepository = CollectionRepository(collectionApi, poiRepository)

    private val searchHistoryApi = SearchHistoryApi(httpClient, personalizationBaseUrl)
    val searchHistoryRepository = SearchHistoryRepository(searchHistoryApi)



    private val routeApi = RouteApi(httpClient, routeBaseUrl)
    val routeRepository = RouteRepository(routeApi, poiRepository)

}