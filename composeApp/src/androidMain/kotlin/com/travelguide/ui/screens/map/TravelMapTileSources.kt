package com.travelguide.ui.map

import org.osmdroid.tileprovider.tilesource.XYTileSource

object TravelMapTileSources {

    val CartoPositron = XYTileSource(
        "CARTO Positron",
        0,
        20,
        256,
        ".png",
        arrayOf(
            "https://a.basemaps.cartocdn.com/light_all/",
            "https://b.basemaps.cartocdn.com/light_all/",
            "https://c.basemaps.cartocdn.com/light_all/",
            "https://d.basemaps.cartocdn.com/light_all/"
        ),
        "© OpenStreetMap contributors © CARTO"
    )
}