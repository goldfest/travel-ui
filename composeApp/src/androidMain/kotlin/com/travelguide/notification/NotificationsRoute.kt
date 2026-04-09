package com.travelguide.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.notifications.NotificationsScreen

@Composable
fun NotificationsRoute(
    container: AppContainer,
    onBackClick: () -> Unit,
    onOpenRoute: (Int) -> Unit
) {
    val vm: NotificationViewModel = viewModel(
        factory = SimpleViewModelFactory { NotificationViewModel(container.notificationRepository) }
    )
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    NotificationsScreen(
        notifications = state.notifications,
        isLoading = state.isLoading,
        unreadOnly = state.unreadOnly,
        unreadCount = state.unreadCount,
        errorMessage = state.errorMessage,
        onBackClick = onBackClick,
        onRetry = vm::load,
        onToggleUnreadOnly = vm::toggleUnreadOnly,
        onMarkAsRead = vm::markAsRead,
        onMarkAllAsRead = vm::markAllAsRead,
        onDeleteNotification = vm::deleteNotification,
        onDeleteAll = vm::deleteAll,
        onOpenRoute = onOpenRoute
    )
}
