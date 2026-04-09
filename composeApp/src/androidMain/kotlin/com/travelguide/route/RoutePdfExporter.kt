package com.travelguide.route

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.RouteMap
import java.io.ByteArrayOutputStream
import kotlin.math.min

class RoutePdfExporter(
    private val context: Context
) {
    fun export(route: Route, routeMap: RouteMap, mapBitmap: Bitmap?): ByteArray {
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas

        val title = Paint().apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val text = Paint().apply { textSize = 11f }
        val section = Paint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        var y = 40f
        canvas.drawText(route.name, 32f, y, title)
        y += 24f

        route.description?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText(it.take(90), 32f, y, text)
            y += 18f
        }

        canvas.drawText("Транспорт: ${route.transportModeText()}", 32f, y, text); y += 16f
        canvas.drawText("Дней: ${route.days.size}", 32f, y, text); y += 16f
        canvas.drawText("Точек: ${route.points.size}", 32f, y, text); y += 16f
        route.distanceKm?.let { canvas.drawText("Дистанция: %.2f км".format(it), 250f, y - 32f, text) }
        route.durationMin?.let { canvas.drawText("Длительность: ${it} мин", 250f, y - 16f, text) }

        mapBitmap?.let {
            val left = 32f
            val top = y + 8f
            val maxWidth = 531f
            val maxHeight = 220f
            val scale = min(maxWidth / it.width, maxHeight / it.height)
            val drawWidth = it.width * scale
            val drawHeight = it.height * scale
            canvas.drawBitmap(it, null, android.graphics.RectF(left, top, left + drawWidth, top + drawHeight), null)
            y = top + drawHeight + 24f
        }

        canvas.drawText("План маршрута", 32f, y, section)
        y += 18f

        route.days.sortedBy { it.dayNumber }.forEach { day ->
            if (y > 780f) return@forEach
            canvas.drawText("День ${day.dayNumber}", 32f, y, section)
            y += 16f

            day.points.sortedBy { it.orderIndex }.forEach { point ->
                if (y > 790f) return@forEach
                val name = point.poiName ?: point.poi?.name ?: "Точка"
                canvas.drawText("${point.orderIndex}. ${name.take(70)}", 42f, y, text)
                y += 14f
            }
            y += 8f
        }

        document.finishPage(page)
        val output = ByteArrayOutputStream()
        document.writeTo(output)
        document.close()
        return output.toByteArray()
    }
}
