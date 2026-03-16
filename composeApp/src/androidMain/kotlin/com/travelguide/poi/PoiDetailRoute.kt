package com.travelguide.poi

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.personalisation.CollectionPickerViewModel
import com.travelguide.ui.screens.personalisation.AddToCollectionDialog
import com.travelguide.ui.screens.personalisation.CollectionActionDialog
import com.travelguide.ui.screens.personalisation.CreateCollectionDialog
import com.travelguide.ui.screens.poi.POIDetailScreen
import kotlinx.coroutines.launch

@Composable
fun PoiDetailRoute(
    container: AppContainer,
    poiId: Int,
    onBackClick: () -> Unit,
    onAddToRoute: () -> Unit,
    onAddToFavorite: (Boolean) -> Unit,
    onWriteReview: () -> Unit,
    onViewReviews: () -> Unit,
    onReportProblem: () -> Unit
) {
    val poiVm: PoiViewModel = viewModel(
        key = "poi-$poiId",
        factory = SimpleViewModelFactory {
            PoiViewModel(
                repository = container.poiRepository,
                favoriteRepository = container.favoriteRepository,
                reviewRepository = container.reviewRepository
            )
        }
    )

    val collectionVm: CollectionPickerViewModel = viewModel(
        key = "collection-picker-$poiId",
        factory = SimpleViewModelFactory {
            CollectionPickerViewModel(container.collectionRepository)
        }
    )

    val poiState by poiVm.detailsState.collectAsState()
    val collectionState by collectionVm.state.collectAsState()

    var showActionDialog by remember { mutableStateOf(false) }
    var showCollectionDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(poiId) {
        poiVm.loadPoi(poiId)
    }


    POIDetailScreen(
        poi = poiState.poi,
        isLoading = poiState.isLoading,
        isFavorite = poiState.isFavorite,
        averageRating = poiState.averageRating,
        reviewCount = poiState.reviewCount,
        errorMessage = poiState.errorMessage,
        onRetry = { poiVm.loadPoi(poiId) },
        onBackClick = onBackClick,
        onAddToRoute = onAddToRoute,
        onAddToCollection = {
            showActionDialog = true
        },
        onAddToFavorite = {
            poiVm.toggleFavorite()
        },
        onWriteReview = onWriteReview,
        onViewReviews = onViewReviews,
        onReportProblem = onReportProblem,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    )


    if (showActionDialog) {
        CollectionActionDialog(
            onDismiss = { showActionDialog = false },
            onAddToExisting = {
                showActionDialog = false
                collectionVm.loadCollections()
                showCollectionDialog = true
            },
            onCreateNew = {
                showActionDialog = false
                showCreateDialog = true
            }
        )
    }

    if (showCollectionDialog) {
        AddToCollectionDialog(
            isLoading = collectionState.isLoading,
            collections = collectionState.collections,
            errorMessage = collectionState.errorMessage,
            onDismiss = {
                showCollectionDialog = false
                collectionVm.clearMessages()
            },
            onSelectCollection = { collection ->
                collectionVm.addPoiToCollection(
                    collectionId = collection.id,
                    poiId = poiId,
                    onSuccess = {
                        showCollectionDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Объект добавлен в коллекцию")
                        }
                    }
                )
            }
        )
    }

    if (showCreateDialog) {
        CreateCollectionDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, description ->
                scope.launch {
                    runCatching {
                        val created = container.collectionRepository.createCollection(
                            name = name,
                            description = description
                        )
                        container.collectionRepository.addPoiToCollection(
                            collectionId = created.id,
                            poiId = poiId
                        )
                    }.onSuccess {
                        showCreateDialog = false
                        snackbarHostState.showSnackbar("Коллекция создана, объект добавлен")
                    }.onFailure { e ->
                        snackbarHostState.showSnackbar(
                            e.message ?: "Не удалось создать коллекцию"
                        )
                    }
                }
            }
        )
    }
}