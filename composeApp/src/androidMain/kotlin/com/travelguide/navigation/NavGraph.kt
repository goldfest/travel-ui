package com.travelguide.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.travelguide.AppContainer
import com.travelguide.BootRoute
import com.travelguide.auth.LoginRoute
import com.travelguide.auth.RegisterRoute
import com.travelguide.profile.ChangePasswordRoute
import com.travelguide.profile.DeleteAccountRoute
import com.travelguide.profile.EditProfileRoute
import com.travelguide.profile.ProfileRoute
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
import com.travelguide.city.CityInfoRoute
import com.travelguide.city.CityListRoute
import com.travelguide.poi.POIListRoute
import com.travelguide.poi.PoiDetailRoute
import com.travelguide.review.CreateReportRoute
import com.travelguide.review.CreateReviewRoute
import com.travelguide.review.EditReviewRoute
import com.travelguide.review.MyReviewsRoute
import com.travelguide.review.ReviewsRoute
import com.travelguide.search.SearchRoute

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    container: AppContainer
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("boot") {
            BootRoute(
                container = container,
                onGoLogin = {
                    navController.navigate("login") { popUpTo("boot") { inclusive = true } }
                },
                onGoMain = {
                    navController.navigate("cityList") { popUpTo("boot") { inclusive = true } }
                }
            )
        }

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
            CityListRoute(
                container = container,
                onCityClick = { cityId -> navController.navigate("cityinfo/$cityId") },
                onProfileClick = { navController.navigate("profile") },
                onNotificationsClick = { },
                onSearchClick = { navController.navigate("search") }
            )
        }

        composable("cityinfo/{cityId}") { backStackEntry ->
            val cityId = backStackEntry.arguments?.getString("cityId")?.toIntOrNull() ?: 1

            CityInfoRoute(
                container = container,
                cityId = cityId,
                onPOIClick = { poiId -> navController.navigate("poiDetail/$poiId") },
                onBackClick = { navController.popBackStack() }
            )
        }

        // POI
        composable("poiDetail/{poiId}") { backStackEntry ->
            val poiId = backStackEntry.arguments?.getString("poiId")?.toIntOrNull() ?: 1

            PoiDetailRoute(
                container = container,
                poiId = poiId,
                onBackClick = { navController.popBackStack() },
                onAddToRoute = { },
                onAddToFavorite = { },
                onWriteReview = { navController.navigate("createReview/$poiId") },
                onViewReviews = { navController.navigate("reviews/$poiId") },
                onReportProblem = { navController.navigate("createReport/$poiId") }
            )
        }

        composable("poiList/{cityId}") { backStackEntry ->
            val cityId = backStackEntry.arguments?.getString("cityId")?.toIntOrNull() ?: 1

            POIListRoute(
                container = container,
                cityId = cityId,
                onPOIClick = { poiId -> navController.navigate("poiDetail/$poiId") },
                onBackClick = { navController.popBackStack() },
                onFilterClick = { }
            )
        }

        // Reviews
        composable("reviews/{poiId}") { backStackEntry ->
            val poiId = backStackEntry.arguments?.getString("poiId")?.toIntOrNull() ?: 1
            ReviewsRoute(
                container = container,
                poiId = poiId,
                onBackClick = { navController.popBackStack() },
                onWriteReview = { navController.navigate("createReview/$poiId") },
                onReportReview = { /* позже можно route для review report */ }
            )
        }

        composable("createReview/{poiId}") { backStackEntry ->
            val poiId = backStackEntry.arguments?.getString("poiId")?.toIntOrNull() ?: 1
            CreateReviewRoute(
                container = container,
                poiId = poiId,
                onBackClick = { navController.popBackStack() },
                onSubmitted = { navController.popBackStack() }
            )
        }

        composable("createReport/{poiId}") { backStackEntry ->
            val poiId = backStackEntry.arguments?.getString("poiId")?.toIntOrNull() ?: 1
            CreateReportRoute(
                container = container,
                poiId = poiId,
                onBackClick = { navController.popBackStack() },
                onSubmitted = { navController.popBackStack() }
            )
        }

        composable("myReviews") {
            MyReviewsRoute(
                container = container,
                onBackClick = { navController.popBackStack() },
                onEditReview = { reviewId -> navController.navigate("editReview/$reviewId") }
            )
        }

        composable("editReview/{reviewId}") { backStackEntry ->
            val reviewId = backStackEntry.arguments?.getString("reviewId")?.toIntOrNull() ?: 1
            EditReviewRoute(
                container = container,
                reviewId = reviewId,
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        // Search
        composable("search") {
            SearchRoute(
                container = container,
                onBackClick = { navController.popBackStack() },
                onPOIClick = { poiId -> navController.navigate("poiDetail/$poiId") },
                onCityClick = { cityId -> navController.navigate("cityinfo/$cityId") }
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
            CollectionsScreen(
                onBackClick = { navController.popBackStack() })
        }

        // Profile
        composable("profile") {
            ProfileRoute(
                container = container,
                onBackClick = { navController.popBackStack() },
                onLogoutNavigate = {
                    navController.navigate("login") { popUpTo("cityList") { inclusive = true } }
                },
                onFavoritesClick = { navController.navigate("favorites") },
                onRoutesClick = { navController.navigate("routes") },
                onCollectionsClick = { navController.navigate("collections") },
                onEditClick = { navController.navigate("editProfile") },
                onAdminClick = { navController.navigate("admin") },
                onChangePasswordClick = { navController.navigate("changePassword") },
                onDeleteAccountClick = { navController.navigate("deleteAccount") },
                onMyReviewsClick = { navController.navigate("myReviews") }
            )
        }

        composable("editProfile") {
            EditProfileRoute(
                container = container,
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() } // вернуться в профиль
            )
        }

        composable("changePassword") {
            ChangePasswordRoute(
                container = container,
                onBackClick = { navController.popBackStack() },
                onDone = { navController.popBackStack() }
            )
        }

        composable("deleteAccount") {
            DeleteAccountRoute(
                container = container,
                onBackClick = { navController.popBackStack() },
                onDeleted = {
                    navController.navigate("login") { popUpTo("cityList") { inclusive = true } }
                }
            )
        }

        composable("admin") {
            AdminScreen(onBackClick = { navController.popBackStack() })
        }
    }
}