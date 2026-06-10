package com.travelguide.personalisation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.personalisation.CollectionDetailScreen

@Composable
fun CollectionDetailRoute(
    container: AppContainer,
    collectionId: Int,
    onBackClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onDeleted: () -> Unit,
    onOpenPoi: (Int) -> Unit
) {
    val vm: CollectionEditViewModel = viewModel(
        key = "collection-detail-$collectionId",
        factory = SimpleViewModelFactory {
            CollectionEditViewModel(container.collectionRepository)
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(collectionId) {
        vm.load(collectionId)
    }

    CollectionDetailScreen(
        isLoading = state.isLoading,
        isDeleting = state.isDeleting,
        collection = state.collection,
        pois = state.pois,
        errorMessage = state.errorMessage,
        onBackClick = onBackClick,
        onEditClick = { onEditClick(collectionId) },
        onDeleteClick = {
            vm.deleteCollection(
                collectionId = collectionId,
                onSuccess = onDeleted
            )
        },
        onRetry = { vm.load(collectionId) },
        onPoiClick = onOpenPoi
    )
}
