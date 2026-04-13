package com.travelguide.route

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.core.toUserMessage
import com.travelguide.ui.screens.route.RouteDetailScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RouteDetailRoute(
    container: AppContainer,
    routeId: Int,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onViewMap: () -> Unit,
    onViewList: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val vm: RouteDetailViewModel = viewModel(
        key = "route-detail-$routeId",
        factory = SimpleViewModelFactory {
            RouteDetailViewModel(container.routeRepository)
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(routeId) {
        vm.loadRoute(routeId)
    }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    RouteDetailScreen(
        route = state.route,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onRetry = { vm.loadRoute(routeId) },
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        onViewMap = onViewMap,
        onViewList = onViewList,
        isOfflineMode = false,
        isOfflineAvailable = state.isOfflineAvailable,
        onOptimizeClick = { request -> vm.optimize(routeId, request) },
        onDownloadOfflineClick = {
            scope.launch {
                runCatching {
                    container.routeRepository.saveRouteOffline(routeId)
                }.onSuccess {
                    toast("Маршрут сохранён для офлайн-доступа")
                    vm.loadRoute(routeId)
                }.onFailure {
                    toast(it.toUserMessage("Не удалось подготовить маршрут для офлайн-доступа"))
                }
            }
        },
        onExportPdfClick = {
            scope.launch {
                runCatching {
                    val route = state.route ?: container.routeRepository.getRouteById(routeId)
                    val routeMap = container.routeRepository.getRouteMap(routeId)
                    val mapBitmap = withContext(Dispatchers.Default) {
                        routeMap.days.firstOrNull()?.let { RouteMapSnapshotRenderer(context).render(it) }
                    }
                    val bytes = RoutePdfExporter(context).export(route, routeMap, mapBitmap)
                    RouteFileSaver.saveToDownloads(
                        context = context,
                        fileName = "route_${routeId}.pdf",
                        mimeType = "application/pdf",
                        bytes = bytes
                    )
                }.onSuccess {
                    toast("PDF сохранён в Downloads/TravelGuide")
                }.onFailure {
                    toast(it.toUserMessage("Не удалось экспортировать PDF"))
                }
            }
        },
        onExportGpxClick = {
            scope.launch {
                runCatching {
                    val bytes = container.routeRepository.exportRouteGpx(routeId)
                    RouteFileSaver.saveToDownloads(
                        context = context,
                        fileName = "route_${routeId}.gpx",
                        mimeType = "application/gpx+xml",
                        bytes = bytes
                    )
                }.onSuccess {
                    toast("GPX сохранён в Downloads/TravelGuide")
                }.onFailure {
                    toast(it.toUserMessage("Не удалось экспортировать GPX"))
                }
            }
        },
        onExportJsonClick = {
            scope.launch {
                runCatching {
                    val bytes = container.routeRepository.exportRouteJson(routeId)
                    RouteFileSaver.saveToDownloads(
                        context = context,
                        fileName = "route_${routeId}.json",
                        mimeType = "application/json",
                        bytes = bytes
                    )
                }.onSuccess {
                    toast("JSON сохранён в Downloads/TravelGuide")
                }.onFailure {
                    toast(it.toUserMessage("Не удалось экспортировать JSON"))
                }
            }
        },
        onExportOfflineArchiveClick = {
            scope.launch {
                runCatching {
                    val local = container.routeRepository.getOfflineArchive(routeId)
                    val bytes = local ?: container.routeRepository.downloadOfflineRoute(routeId)
                    RouteFileSaver.saveToDownloads(
                        context = context,
                        fileName = "route_${routeId}_offline.zip",
                        mimeType = "application/zip",
                        bytes = bytes
                    )
                }.onSuccess {
                    toast("Офлайн-архив сохранён в Downloads/TravelGuide")
                }.onFailure {
                    toast(it.toUserMessage("Не удалось экспортировать офлайн-архив"))
                }
            }
        }
    )
}
