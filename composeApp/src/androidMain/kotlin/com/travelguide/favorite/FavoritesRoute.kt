package com.travelguide.favorite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.personalisation.FavoritesScreen

@Composable
fun FavoritesRoute(
    container: AppContainer,
    onBackClick: () -> Unit,
    onPOIClick: (Int) -> Unit
) {
    val vm: FavoritesViewModel = viewModel(
        factory = SimpleViewModelFactory {
            FavoritesViewModel(container.favoriteRepository)
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    FavoritesScreen(
        favorites = state.favorites,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onBackClick = onBackClick,
        onPOIClick = onPOIClick,
        onRemoveFavorite = { poiId -> vm.removeFromFavorites(poiId) },
        onRetry = { vm.load() }
    )
}