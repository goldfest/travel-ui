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
import com.travelguide.ui.screens.route.RouteDetailScreen
import kotlinx.coroutines.launch

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

    RouteDetailScreen(
        route = state.route,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onRetry = { vm.loadRoute(routeId) },
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        onViewMap = onViewMap,
        onViewList = onViewList,
        onOptimizeClick = { vm.optimize(routeId) },
        onDownloadOfflineClick = {
            scope.launch {
                runCatching {
                    val bytes = container.routeRepository.downloadOfflineRoute(routeId)
                    RouteFileSaver.saveToDownloads(
                        context = context,
                        fileName = "route_${routeId}_offline.zip",
                        mimeType = "application/zip",
                        bytes = bytes
                    )
                }.onSuccess {
                    Toast.makeText(context, "Оффлайн-архив сохранён в Downloads/TravelGuide", Toast.LENGTH_LONG).show()
                }.onFailure {
                    Toast.makeText(context, it.message ?: "Не удалось сохранить оффлайн-архив", Toast.LENGTH_LONG).show()
                }
            }
        },
        onExportPdfClick = {
            scope.launch {
                runCatching {
                    val bytes = container.routeRepository.exportRoutePdf(routeId)
                    RouteFileSaver.saveToDownloads(
                        context = context,
                        fileName = "route_${routeId}.pdf",
                        mimeType = "application/pdf",
                        bytes = bytes
                    )
                }.onSuccess {
                    Toast.makeText(context, "PDF сохранён в Downloads/TravelGuide", Toast.LENGTH_LONG).show()
                }.onFailure {
                    Toast.makeText(context, it.message ?: "Не удалось экспортировать PDF", Toast.LENGTH_LONG).show()
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
                    Toast.makeText(context, "GPX сохранён в Downloads/TravelGuide", Toast.LENGTH_LONG).show()
                }.onFailure {
                    Toast.makeText(context, it.message ?: "Не удалось экспортировать GPX", Toast.LENGTH_LONG).show()
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
                    Toast.makeText(context, "JSON сохранён в Downloads/TravelGuide", Toast.LENGTH_LONG).show()
                }.onFailure {
                    Toast.makeText(context, it.message ?: "Не удалось экспортировать JSON", Toast.LENGTH_LONG).show()
                }
            }
        }
    )
}
