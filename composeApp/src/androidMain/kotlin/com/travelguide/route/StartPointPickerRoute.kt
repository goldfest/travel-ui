package com.travelguide.route

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.travelguide.AppContainer
import com.travelguide.core.toUserMessage
import com.travelguide.domain.models.POI
import com.travelguide.domain.models.TransportMode
import com.travelguide.theme.TravelAccent
import com.travelguide.theme.TravelDark
import com.travelguide.theme.TravelPanel
import com.travelguide.ui.map.TravelMapTileSources
import com.travelguide.ui.screens.route.applyMapTheme
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartPointPickerRoute(
    container: AppContainer,
    poiId: Int,
    onBackClick: () -> Unit,
    onStartSelected: (latitude: Double, longitude: Double, transportMode: TransportMode) -> Unit
) {
    var poi by remember { mutableStateOf<POI?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedLatitude by remember { mutableStateOf<Double?>(null) }
    var selectedLongitude by remember { mutableStateOf<Double?>(null) }
    var transportMode by remember { mutableStateOf(TransportMode.WALK) }

    LaunchedEffect(poiId) {
        isLoading = true
        errorMessage = null
        runCatching { container.poiRepository.getPoiById(poiId) }
            .onSuccess { loadedPoi ->
                poi = loadedPoi
                selectedLatitude = loadedPoi.latitude
                selectedLongitude = loadedPoi.longitude
                if (loadedPoi.latitude == null || loadedPoi.longitude == null) {
                    errorMessage = "У объекта нет координат, поэтому карту выбора открыть нельзя"
                }
            }
            .onFailure { error ->
                errorMessage = error.toUserMessage("Не удалось загрузить объект")
            }
        isLoading = false
    }

    Scaffold(
        containerColor = TravelDark,
        topBar = {
            TopAppBar(
                title = { Text("Точка старта", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TravelDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TravelAccent)
                }
            }

            !errorMessage.isNullOrBlank() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBackClick) { Text("Назад") }
                }
            }

            poi?.latitude != null && poi?.longitude != null -> {
                StartPointPickerContent(
                    poi = poi,
                    initialLatitude = poi?.latitude ?: 0.0,
                    initialLongitude = poi?.longitude ?: 0.0,
                    selectedLatitude = selectedLatitude,
                    selectedLongitude = selectedLongitude,
                    transportMode = transportMode,
                    onTransportModeChanged = { transportMode = it },
                    onCenterChanged = { latitude, longitude ->
                        selectedLatitude = latitude
                        selectedLongitude = longitude
                    },
                    onConfirm = {
                        val latitude = selectedLatitude ?: return@StartPointPickerContent
                        val longitude = selectedLongitude ?: return@StartPointPickerContent
                        onStartSelected(latitude, longitude, transportMode)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }
}

@Composable
private fun StartPointPickerContent(
    poi: POI?,
    initialLatitude: Double,
    initialLongitude: Double,
    selectedLatitude: Double?,
    selectedLongitude: Double?,
    transportMode: TransportMode,
    onTransportModeChanged: (TransportMode) -> Unit,
    onCenterChanged: (Double, Double) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        StartPointPickerMapView(
            initialLatitude = initialLatitude,
            initialLongitude = initialLongitude,
            onCenterChanged = onCenterChanged,
            modifier = Modifier.fillMaxSize()
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = TravelPanel.copy(alpha = 0.96f),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Двигайте карту под пином",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Старт будет взят из центра экрана, как в Яндекс.Картах",
                    color = Color.White.copy(alpha = 0.76f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = TravelAccent,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-24).dp)
                .size(56.dp)
        )

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 16.dp)
                .size(10.dp),
            color = Color.White,
            shape = CircleShape
        ) {}

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = TravelPanel),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Маршрут до: ${poi?.name ?: "объекта"}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Широта: ${selectedLatitude.formatCoordinate()}",
                        color = Color.White.copy(alpha = 0.88f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Долгота: ${selectedLongitude.formatCoordinate()}",
                        color = Color.White.copy(alpha = 0.88f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(TransportMode.WALK, TransportMode.CAR).forEach { mode ->
                        FilterChip(
                            selected = transportMode == mode,
                            onClick = { onTransportModeChanged(mode) },
                            label = { Text(mode.label()) }
                        )
                    }
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedLatitude != null && selectedLongitude != null
                ) {
                    Icon(Icons.Default.Route, contentDescription = null)
                    Text("  Выбрать эту точку и построить")
                }
            }
        }
    }
}

@Composable
private fun StartPointPickerMapView(
    initialLatitude: Double,
    initialLongitude: Double,
    onCenterChanged: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val darkTheme = isSystemInDarkTheme()
    val mapView = rememberPickerMapViewWithLifecycle(context, lifecycleOwner)
    var initialized by remember(initialLatitude, initialLongitude) { mutableStateOf(false) }

    DisposableEffect(mapView) {
        val listener = object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                val center = mapView.mapCenter
                onCenterChanged(center.latitude, center.longitude)
                return false
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                val center = mapView.mapCenter
                onCenterChanged(center.latitude, center.longitude)
                return false
            }
        }
        mapView.addMapListener(listener)
        onDispose { mapView.removeMapListener(listener) }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { mv ->
            mv.setTileSource(TravelMapTileSources.CartoPositron)
            applyMapTheme(mv, darkTheme)
            if (!initialized) {
                val point = GeoPoint(initialLatitude, initialLongitude)
                mv.controller.setZoom(16.2)
                mv.controller.setCenter(point)
                onCenterChanged(initialLatitude, initialLongitude)
                initialized = true
            }
        }
    )
}

@Composable
private fun rememberPickerMapViewWithLifecycle(
    context: Context,
    lifecycleOwner: LifecycleOwner
): MapView {
    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TravelMapTileSources.CartoPositron)
            setMultiTouchControls(true)
            controller.setZoom(16.2)
            minZoomLevel = 4.0
            maxZoomLevel = 20.0
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return mapView
}

private fun Double?.formatCoordinate(): String =
    this?.let { String.format(Locale.US, "%.6f", it) } ?: "—"

