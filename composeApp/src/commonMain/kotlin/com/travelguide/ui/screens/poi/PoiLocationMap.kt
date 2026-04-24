package com.travelguide.ui.screens.poi

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PoiLocationMap(
    latitude: Double,
    longitude: Double,
    title: String,
    modifier: Modifier = Modifier
)
