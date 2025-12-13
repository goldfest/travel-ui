package com.travelguide.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.travelguide.ui.screens.auth.LoginScreen
import com.travelguide.ui.screens.auth.RegisterScreen
import com.travelguide.ui.screens.city.CityListScreen
import com.travelguide.ui.screens.city.CityInfoScreen
//import com.travelguide.ui.screens.notifications.NotificationsScreen
import com.travelguide.ui.screens.personalisation.FavoritesScreen
import com.travelguide.ui.screens.personalisation.CollectionsScreen
import com.travelguide.ui.screens.poi.POIDetailScreen
import com.travelguide.ui.screens.poi.POIListScreen
import com.travelguide.ui.screens.profile.ProfileScreen
import com.travelguide.ui.screens.review.CreateReportScreen
import com.travelguide.ui.screens.review.CreateReviewScreen
import com.travelguide.ui.screens.review.ReviewsScreen
import com.travelguide.ui.screens.route.RouteDetailScreen
import com.travelguide.ui.screens.route.RouteListScreen
import com.travelguide.ui.screens.search.SearchScreen
import com.travelguide.ui.screens.admin.AdminScreen
import com.travelguide.ui.screens.route.CreateRouteScreen
import com.travelguide.ui.screens.route.RouteMapScreen
import com.travelguide.domain.models.Route

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = "cityList"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("cityList") },
                onRegisterClick = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("cityList") },
                onLoginClick = { navController.navigate("login") }
            )
        }

        // Main Flow
        composable("cityList") {
            CityListScreen(
                onCityClick = { cityId ->
                    navController.navigate("cityinfo/$cityId")
                },
                onProfileClick = { navController.navigate("profile") },
                onNotificationsClick = { navController.navigate("notifications") },
            )
        }

        // POI Flow
        composable("cityinfo/{cityId}") { backStackEntry ->
            val cityId = backStackEntry.arguments?.getString("cityId")?.toIntOrNull() ?: 1
            CityInfoScreen(
                cityId = cityId,
                onPOIClick = { poiId ->
                    navController.navigate("poiDetail/$poiId")
                },
                onBackClick = { navController.popBackStack() },
                onFilterClick = { /* TODO */ }
            )
        }

        composable("poiDetail/{poiId}") { backStackEntry ->
            val poiId = backStackEntry.arguments?.getString("poiId")?.toIntOrNull() ?: 1
            POIDetailScreen(
                poiId = poiId,
                onBackClick = { navController.popBackStack() },
                onAddToRoute = { /* TODO */ },
                onAddToFavorite = { /* TODO */ },
                onWriteReview = { navController.navigate("createReview/$poiId") },
                onViewReviews = { navController.navigate("reviews/$poiId") },
                onReportProblem = { navController.navigate("createReport/$poiId") }
            )
        }

        // Reviews
        composable("reviews/{poiId}") { backStackEntry ->
            val poiId = backStackEntry.arguments?.getString("poiId")?.toIntOrNull() ?: 1
            ReviewsScreen(
                poiId = poiId,
                onBackClick = { navController.popBackStack() },
                onWriteReview = { navController.navigate("createReview/$poiId") }
            )
        }

        composable("createReview/{poiId}") { backStackEntry ->
            val poiId = backStackEntry.arguments?.getString("poiId")?.toIntOrNull() ?: 1
            CreateReviewScreen(
                poiId = poiId,
                onBackClick = { navController.popBackStack() },
                onSubmit = { navController.popBackStack() }
            )
        }

        composable("createReport/{poiId}") { backStackEntry ->
            val poiId = backStackEntry.arguments?.getString("poiId")?.toIntOrNull() ?: 1
            CreateReportScreen(
                poiId = poiId,
                onBackClick = { navController.popBackStack() },
                onSubmit = { navController.popBackStack() }
            )
        }

        // Search
        composable("search") {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onPOIClick = { poiId -> navController.navigate("poiDetail/$poiId") }
            )
        }

        // Routes
        composable("routes") {
            RouteListScreen(
                onBackClick = { navController.popBackStack() },
                onRouteClick = { routeId -> navController.navigate("routeDetail/$routeId") },
                onCreateRoute = { navController.navigate("createRoute") }
            )
        }


        // Personalization
        composable("favorites") {
            FavoritesScreen(
                onBackClick = { navController.popBackStack() },
                onPOIClick = { poiId -> navController.navigate("poiDetail/$poiId") }
            )
        }

        composable("collections") {
            CollectionsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Profile & Notifications
        composable("profile") {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogout = {
                    // TODO: очистить данные пользователя
                    navController.navigate("login") {
                        popUpTo(0) // Очистить весь стек
                    }
                },
                onFavoritesClick = { navController.navigate("favorites") },
                onRoutesClick = { navController.navigate("routes") },
                onCollectionsClick = { navController.navigate("collections") },
                onAdminClick = { navController.navigate("admin") },
                isAdmin = true // Определять по роли пользователя
            )
        }
//
//        composable("notifications") {
//            NotificationsScreen(
//                onBackClick = { navController.popBackStack() }
//            )
//        }

        // В NavGraph.kt добавим:
        composable("admin") {
            AdminScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("favorites") {
            FavoritesScreen(
                onBackClick = { navController.popBackStack() },
                onPOIClick = { poiId -> navController.navigate("poiDetail/$poiId") }
            )
        }

        composable("collections") {
            CollectionsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("routes") {
            RouteListScreen(
                onBackClick = { navController.popBackStack() },
                onRouteClick = { routeId -> navController.navigate("routeDetail/$routeId") },
                onCreateRoute = { navController.navigate("createRoute") }
            )
        }

        composable("routeMap/{routeId}") { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId")?.toIntOrNull() ?: 1

            // RouteMapScreen теперь принимает routeId, а не route
            RouteMapScreen(
                routeId = routeId, // ПЕРЕДАЕМ routeId
                onBackClick = { navController.popBackStack() },
                onViewList = { navController.popBackStack() }
            )
        }

        composable("createRoute") {
            CreateRouteScreen(
                onBackClick = { navController.popBackStack() },
                onSubmit = { routeId ->
                    navController.popBackStack()
                    navController.navigate("routeDetail/$routeId")
                }
            )
        }

        composable("routeDetail/{routeId}") { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId")?.toIntOrNull() ?: 1
            RouteDetailScreen(
                routeId = routeId,
                onBackClick = { navController.popBackStack() },
                onEditClick = {
                    // TODO: переход на экран редактирования маршрута
                    // navController.navigate("editRoute/$routeId")
                },
                onViewMap = {
                    // Переход на экран карты маршрута
                    navController.navigate("routeMap/$routeId")
                },
                onViewList = {
                    // Уже на списке, ничего не делаем
                }
            )
        }

//        composable("notifications") {
//            NotificationsScreen(
//                onBackClick = { navController.popBackStack() }
//            )
//        }

        // Auth экраны
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("cityList") {
                        popUpTo(0) // Очистить весь стек
                    }
                },
                onRegisterClick = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("cityList") {
                        popUpTo(0) // Очистить весь стек
                    }
                },
                onLoginClick = { navController.popBackStack() }
            )
        }
    }
}