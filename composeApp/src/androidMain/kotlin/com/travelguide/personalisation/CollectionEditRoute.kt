package com.travelguide.personalisation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.personalisation.CollectionEditScreen

@Composable
fun CollectionEditRoute(
    container: AppContainer,
    collectionId: Int,
    onBackClick: () -> Unit,
    onDeleted: () -> Unit,
    onCollectionUpdated: (com.travelguide.domain.models.Collection) -> Unit
) {
    val vm: CollectionEditViewModel = viewModel(
        key = "collection-edit-$collectionId",
        factory = SimpleViewModelFactory {
            CollectionEditViewModel(container.collectionRepository)
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(collectionId) {
        vm.load(collectionId)
    }

    CollectionEditScreen(
        isLoading = state.isLoading,
        collectionName = state.collection?.name ?: "",
        collectionDescription = state.collection?.description.orEmpty(),
        pois = state.pois,
        errorMessage = state.errorMessage,
        successMessage = state.successMessage,
        onBackClick = onBackClick,
        onSave = { name, description ->
            vm.updateCollection(
                collectionId = collectionId,
                name = name,
                description = description,
                onSuccess = onCollectionUpdated
            )
        },
        onRemovePoi = { poiId ->
            vm.removePoi(
                collectionId = collectionId,
                poiId = poiId,
                onCollectionChanged = onCollectionUpdated
            )
        },
        onDeleteCollection = {
            vm.deleteCollection(
                collectionId = collectionId,
                onSuccess = onDeleted
            )
        }
    )
}