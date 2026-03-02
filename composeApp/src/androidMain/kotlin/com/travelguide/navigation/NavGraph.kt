package com.travelguide.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.travelguide.AppContainer
import com.travelguide.auth.LoginRoute
import com.travelguide.auth.RegisterRoute
import com.travelguide.ui.screens.admin.AdminScreen
import com.travelguide.ui.screens.city.CityInfoScreen
import com.travelguide.ui.screens.city.CityListScreen
import com.travelguide.ui.screens.personalisation.CollectionsScreen
import com.travelguide.ui.screens.personalisation.FavoritesScreen
import com.travelguide.ui.screens.poi.POIDetailScreen
import com.travelguide.ui.screens.profile.ProfileScreen
import com.travelguide.ui.screens.review.CreateReportScreen
import com.travelguide.ui.screens.review.CreateReviewScreen
import com.travelguide.ui.screens.review.ReviewsScreen
import com.travelguide.ui.screens.route.CreateRouteScreen
import com.travelguide.ui.screens.route.RouteDetailScreen
import com.travelguide.ui.screens.route.RouteListScreen
import com.travelguide.ui.screens.route.RouteMapScreen
import com.travelguide.ui.screens.search.SearchScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    container: AppContainer
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            LoginRoute(
                container = container,
                onLoginSuccess = { navController.navigate("cityList") { popUpTo("login"){inclusive=true} } },
                onRegisterClick = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterRoute(
                container = container,
                onRegisterSuccess = { navController.navigate("cityList") { popUpTo("register"){inclusive=true} } },
                onLoginClick = { navController.popBackStack() }
            )
        }

        // Cities
        composable("cityList") {
            CityListScreen(
                onCityClick = { cityId -> navController.navigate("cityinfo/$cityId") },
                onProfileClick = { navController.navigate("profile") },
                onNotificationsClick = { navController.navigate("notifications") }, // пока закомментирован экран — лучше убрать/не вызывать
            )
        }

        composable("cityinfo/{cityId}") { backStackEntry ->
            val cityId = backStackEntry.arguments?.getString("cityId")?.toIntOrNull() ?: 1
            CityInfoScreen(
                cityId = cityId,
                onPOIClick = { poiId -> navController.navigate("poiDetail/$poiId") },
                onBackClick = { navController.popBackStack() },
                onFilterClick = { /* TODO */ }
            )
        }

        // POI
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
                onEditClick = { /* TODO */ },
                onViewMap = { navController.navigate("routeMap/$routeId") },
                onViewList = { /* already list */ }
            )
        }

        composable("routeMap/{routeId}") { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId")?.toIntOrNull() ?: 1
            RouteMapScreen(
                routeId = routeId,
                onBackClick = { navController.popBackStack() },
                onViewList = { navController.popBackStack() }
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
            CollectionsScreen(onBackClick = { navController.popBackStack() })
        }

        // Profile/Admin
        composable("profile") {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("cityList") { inclusive = true }
                    }
                },
                onFavoritesClick = { navController.navigate("favorites") },
                onRoutesClick = { navController.navigate("routes") },
                onCollectionsClick = { navController.navigate("collections") },
                onAdminClick = { navController.navigate("admin") },
                isAdmin = true
            )
        }

        composable("admin") {
            AdminScreen(onBackClick = { navController.popBackStack() })
        }
    }
}