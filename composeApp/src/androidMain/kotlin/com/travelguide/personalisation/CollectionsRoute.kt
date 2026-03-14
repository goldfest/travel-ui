package com.travelguide.personalisation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.personalisation.CollectionsScreen

@Composable
fun CollectionsRoute(
    container: AppContainer,
    onBackClick: () -> Unit,
    onEditCollection: (Int) -> Unit
) {
    val vm: CollectionsViewModel = viewModel(
        factory = SimpleViewModelFactory {
            CollectionsViewModel(container.collectionRepository)
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    CollectionsScreen(
        collections = state.collections,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onBackClick = onBackClick,
        onCreateCollection = { name, description ->
            vm.createCollection(name, description)
        },
        onDeleteCollection = { collectionId ->
            vm.deleteCollection(collectionId)
        },
        onEditCollection = onEditCollection,
        onRetry = { vm.load() }
    )
}