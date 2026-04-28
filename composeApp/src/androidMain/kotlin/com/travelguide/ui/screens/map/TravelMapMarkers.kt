package com.travelguide.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.travelguide.domain.models.POI

object TravelMapMarkers {

    fun poiMarker(
        context: Context,
        poi: POI,
        selected: Boolean = false
    ): Drawable {
        val typeCode = poi.poiType?.code.orEmpty().lowercase()
        val typeName = poi.poiType?.name.orEmpty().lowercase()

        val emoji = when {
            "restaurant" in typeCode || "кафе" in typeName || "ресторан" in typeName -> "🍽"
            "museum" in typeCode || "музей" in typeName -> "🏛"
            "park" in typeCode || "парк" in typeName -> "🌳"
            "hotel" in typeCode || "отель" in typeName || "гостиниц" in typeName -> "🏨"
            "landmark" in typeCode || "достопримеч" in typeName -> "📍"
            "theatre" in typeCode || "театр" in typeName -> "🎭"
            "shop" in typeCode || "магазин" in typeName -> "🛍"
            "church" in typeCode || "храм" in typeName || "церковь" in typeName -> "⛪"
            "monument" in typeCode || "памятник" in typeName -> "🗿"
            "gallery" in typeCode || "галерея" in typeName -> "🖼"
            "beach" in typeCode || "пляж" in typeName -> "🏖"
            "viewpoint" in typeCode || "смотров" in typeName -> "👁"
            else -> "📍"
        }

        val fillColor = when {
            selected -> 0xFFFFB74D.toInt()
            "restaurant" in typeCode || "кафе" in typeName || "ресторан" in typeName -> 0xFFFF8A3D.toInt()
            "museum" in typeCode || "музей" in typeName -> 0xFF7E57C2.toInt()
            "park" in typeCode || "парк" in typeName -> 0xFF2EAD67.toInt()
            "hotel" in typeCode || "отель" in typeName || "гостиниц" in typeName -> 0xFF3F7DFF.toInt()
            "landmark" in typeCode || "достопримеч" in typeName -> 0xFFEF5350.toInt()
            "theatre" in typeCode || "театр" in typeName -> 0xFFD65DB1.toInt()
            "shop" in typeCode || "магазин" in typeName -> 0xFF26A69A.toInt()
            "church" in typeCode || "храм" in typeName || "церковь" in typeName -> 0xFF8D6E63.toInt()
            "monument" in typeCode || "памятник" in typeName -> 0xFF607D8B.toInt()
            "gallery" in typeCode || "галерея" in typeName -> 0xFF5C6BC0.toInt()
            "beach" in typeCode || "пляж" in typeName -> 0xFF29B6F6.toInt()
            "viewpoint" in typeCode || "смотров" in typeName -> 0xFFFFB300.toInt()
            else -> 0xFF2D9B63.toInt()
        }

        return buildPinDrawable(
            context = context,
            fillColor = fillColor,
            label = emoji,
            selected = selected
        )
    }

    fun singlePoiMarker(
        context: Context
    ): Drawable {
        return buildPinDrawable(
            context = context,
            fillColor = 0xFF2D9B63.toInt(),
            label = "📍",
            selected = true
        )
    }

    private fun buildPinDrawable(
        context: Context,
        fillColor: Int,
        label: String,
        selected: Boolean
    ): Drawable {
        val sizePx = if (selected) 118 else 104
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val centerX = sizePx / 2f
        val circleY = sizePx * 0.39f
        val circleRadius = if (selected) 32f else 28f

        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x55000000
            style = Paint.Style.FILL
        }

        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fillColor
            style = Paint.Style.FILL
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x22000000
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = if (selected) 31f else 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val shadowRect = RectF(
            centerX - 25f,
            sizePx - 22f,
            centerX + 25f,
            sizePx - 10f
        )
        canvas.drawOval(shadowRect, shadowPaint)

        val tailPath = Path().apply {
            moveTo(centerX, sizePx - 14f)

            cubicTo(
                centerX - 8f,
                circleY + 38f,
                centerX - 30f,
                circleY + 24f,
                centerX - circleRadius,
                circleY + 5f
            )

            lineTo(centerX + circleRadius, circleY + 5f)

            cubicTo(
                centerX + 30f,
                circleY + 24f,
                centerX + 8f,
                circleY + 38f,
                centerX,
                sizePx - 14f
            )

            close()
        }

        canvas.drawPath(tailPath, whitePaint)
        canvas.drawCircle(centerX, circleY, circleRadius + 8f, whitePaint)

        canvas.drawPath(tailPath, fillPaint)
        canvas.drawCircle(centerX, circleY, circleRadius, fillPaint)

        canvas.drawPath(tailPath, strokePaint)
        canvas.drawCircle(centerX, circleY, circleRadius, strokePaint)

        val textY = circleY - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(label, centerX, textY, textPaint)

        return BitmapDrawable(context.resources, bitmap)
    }
}