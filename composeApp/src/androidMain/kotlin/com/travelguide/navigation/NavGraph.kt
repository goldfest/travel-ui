package com.travelguide.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.travelguide.AppContainer
import com.travelguide.BootRoute
import com.travelguide.auth.LoginRoute
import com.travelguide.auth.RegisterRoute
import com.travelguide.city.CityInfoRoute
import com.travelguide.city.CityListRoute
import com.travelguide.city.CityPoiMapRoute
import com.travelguide.favorite.FavoritesRoute
import com.travelguide.notification.NotificationsRoute
import com.travelguide.personalisation.CollectionEditRoute
import com.travelguide.personalisation.CollectionsRoute
import com.travelguide.poi.POIListRoute
import com.travelguide.poi.PoiDetailRoute
import com.travelguide.profile.ChangePasswordRoute
import com.travelguide.profile.DeleteAccountRoute
import com.travelguide.profile.EditProfileRoute
import com.travelguide.profile.ProfileRoute
import com.travelguide.review.CreateReportRoute
import com.travelguide.review.CreateReviewRoute
import com.travelguide.review.EditReviewRoute
import com.travelguide.review.MyReviewsRoute
import com.travelguide.review.ReviewsRoute
import com.travelguide.route.OfflineRouteDetailRoute
import com.travelguide.route.RouteDetailRoute
import com.travelguide.route.RouteEditorRoute
import com.travelguide.route.RouteListFilter
import com.travelguide.route.RouteListRoute
import com.travelguide.route.RouteMapRoute
import com.travelguide.route.SelectRouteForPoiRoute
import com.travelguide.search.SearchRoute
import com.travelguide.admin.AdminRoute

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
                onLoginSuccess = {
                    navController.navigate("cityList") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterRoute(
                container = container,
                onRegisterSuccess = {
                    navController.navigate("cityList") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onLoginClick = { navController.popBackStack() }
            )
        }

        composable("cityList") {
            CityListRoute(
                container = container,
                onCityClick = { cityId -> navController.navigate("cityinfo/$cityId") },
                onProfileClick = { navController.navigate("profile") },
                onNotificationsClick = { navController.navigate("notifications") },
                onSearchClick = { navController.navigate("search") }
            )
        }

        composable("notifications") {
            NotificationsRoute(
                container = container,
                onBackClick = { navController.popBackStack() },
                onOpenRoute = { routeId ->
                    navController.navigate("routeDetail/$routeId")
                }
            )
        }

        composable("cityinfo/{cityId}") { backStackEntry ->
            val cityId = backStackEntry.arguments?.getString("cityId")?.toIntOrNull() ?: 1

            CityInfoRoute(
                container = container,
                cityId = cityId,
                onPOIClick = { poiId -> navController.navigate("poiDetail/$poiId") },
                onOpenCityMap = { selectedCityId -> navController.navigate("cityPoiMap/$selectedCityId") },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("cityPoiMap/{cityId}") { backStackEntry ->
            val cityId = backStackEntry.arguments?.getString("cityId")?.toIntOrNull() ?: 1

            CityPoiMapRoute(
                container = container,
                cityId = cityId,
                onBackClick = { navController.popBackStack() },
                onOpenPoi = { poiId -> navController.navigate("poiDetail/$poiId") }
            )
        }

        composable("poiDetail/{poiId}") { backStackEntry ->
            val poiId = backStackEntry.arguments?.getString("poiId")?.toIntOrNull() ?: 1

            PoiDetailRoute(
                container = container,
                poiId = poiId,
                onBackClick = { navController.popBackStack() },
                onCreateNewRoute = { cityId, selectedPoiId ->
                    navController.navigate("routeEditor?cityId=$cityId&poiId=$selectedPoiId")
                },
                onChooseExistingRoute = { cityId, selectedPoiId ->
                    navController.navigate("selectRouteForPoi/$cityId/$selectedPoiId")
                },
                onAddToFavorite = { },
                onWriteReview = { navController.navigate("createReview/$poiId") },
                onViewReviews = { navController.navigate("reviews/$poiId") },
                onReportProblem = { navController.navigate("createReport/$poiId") }
            )
        }

        composable("selectRouteForPoi/{cityId}/{poiId}") { backStackEntry ->
            val cityId = backStackEntry.arguments?.getString("cityId")?.toIntOrNull() ?: 1
            val poiId = backStackEntry.arguments?.getString("poiId")?.toIntOrNull() ?: 1

            SelectRouteForPoiRoute(
                container = container,
                cityId = cityId,
                poiId = poiId,
                onBackClick = { navController.popBackStack() },
                onAdded = { routeId ->
                    navController.navigate("routeDetail/$routeId") {
                        popUpTo("poiDetail/$poiId") { inclusive = false }
                    }
                }
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

        composable("reviews/{poiId}") { backStackEntry ->
            val poiId = backStackEntry.arguments?.getString("poiId")?.toIntOrNull() ?: 1
            ReviewsRoute(
                container = container,
                poiId = poiId,
                onBackClick = { navController.popBackStack() },
                onWriteReview = { navController.navigate("createReview/$poiId") },
                onReportReview = { }
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

        composable("search") {
            SearchRoute(
                container = container,
                onBackClick = { navController.popBackStack() },
                onPOIClick = { poiId -> navController.navigate("poiDetail/$poiId") }
            )
        }

        composable("routes") {
            RouteListRoute(
                container = container,
                onBackClick = { navController.popBackStack() },
                onRouteClick = { routeId, filter ->
                    if (filter == RouteListFilter.OFFLINE) {
                        navController.navigate("offlineRouteDetail/$routeId")
                    } else {
                        navController.navigate("routeDetail/$routeId")
                    }
                },
                onCreateRoute = { navController.navigate("routeEditor?cityId=&poiId=") }
            )
        }

        composable("offlineRouteDetail/{routeId}") { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId")?.toIntOrNull() ?: 1

            OfflineRouteDetailRoute(
                container = container,
                routeId = routeId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.navigate("routeEditor/edit/$routeId?offline=true") },
                onViewMap = { navController.navigate("routeMap/$routeId?offline=true") }
            )
        }

        composable("routeEditor?cityId={cityId}&poiId={poiId}") { backStackEntry ->
            val cityId = backStackEntry.arguments?.getString("cityId")?.toIntOrNull()
            val poiId = backStackEntry.arguments?.getString("poiId")?.toIntOrNull()

            RouteEditorRoute(
                container = container,
                routeId = null,
                cityId = cityId,
                initialPoiId = poiId,
                onBackClick = { navController.popBackStack() },
                onSaved = { routeId ->
                    navController.navigate("routeDetail/$routeId") {
                        popUpTo("routeEditor?cityId={cityId}&poiId={poiId}") { inclusive = true }
                    }
                }
            )
        }

        composable("routeEditor/edit/{routeId}?offline={offline}") { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId")?.toIntOrNull() ?: return@composable
            val offlineSource = backStackEntry.arguments?.getString("offline") == "true"

            RouteEditorRoute(
                container = container,
                routeId = routeId,
                cityId = null,
                initialPoiId = null,
                onBackClick = { navController.popBackStack() },
                onSaved = { savedId ->
                    val target = if (offlineSource) "offlineRouteDetail/$savedId" else "routeDetail/$savedId"
                    navController.navigate(target) {
                        popUpTo("routeEditor/edit/$routeId?offline=$offlineSource") { inclusive = true }
                    }
                }
            )
        }

        composable("routeDetail/{routeId}") { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId")?.toIntOrNull() ?: 1

            RouteDetailRoute(
                container = container,
                routeId = routeId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.navigate("routeEditor/edit/$routeId?offline=false") },
                onViewMap = { navController.navigate("routeMap/$routeId?offline=false") },
                onViewList = { }
            )
        }

        composable("routeMap/{routeId}?offline={offline}") { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId")?.toIntOrNull() ?: 1
            val offlineMode = backStackEntry.arguments?.getString("offline") == "true"

            RouteMapRoute(
                container = container,
                routeId = routeId,
                onBackClick = { navController.popBackStack() },
                onViewList = { navController.popBackStack() },
                onOpenPoi = { poiId -> navController.navigate("poiDetail/$poiId") },
                showOpenPoiAction = !offlineMode
            )
        }

        composable("favorites") {
            FavoritesRoute(
                container = container,
                onBackClick = { navController.popBackStack() },
                onPOIClick = { poiId -> navController.navigate("poiDetail/$poiId") }
            )
        }

        composable("collections") {
            CollectionsRoute(
                container = container,
                onBackClick = { navController.popBackStack() },
                onEditCollection = { collectionId ->
                    navController.navigate("collectionEdit/$collectionId")
                }
            )
        }

        composable("collectionEdit/{collectionId}") { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getString("collectionId")?.toIntOrNull() ?: 1

            CollectionEditRoute(
                container = container,
                collectionId = collectionId,
                onBackClick = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() },
                onCollectionUpdated = { }
            )
        }

        composable("profile") {
            ProfileRoute(
                container = container,
                onBackClick = { navController.popBackStack() },
                onLogoutNavigate = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
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
                onSaved = { navController.popBackStack() }
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
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("admin") {
            AdminRoute(
                container = container,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}