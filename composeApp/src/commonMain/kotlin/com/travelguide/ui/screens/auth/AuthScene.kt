package com.travelguide.ui.screens.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection




@Composable
fun ExplorerWelcomeScreen(
    title: String,
    headline: String,
    onPrimaryAction: () -> Unit,
    primaryActionLabel: String,
    secondaryLabel: String,
    onSecondaryAction: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundPainter: Painter? = null,
    topBadge: String = "лого"
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF071A12))
    ) {
        ExplorerMediaCard(
            modifier = Modifier.fillMaxSize(),
            backgroundPainter = backgroundPainter,
            shape = RoundedCornerShape(0.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.10f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.45f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = topBadge,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = headline,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(16.dp))

            ExplorerPrimaryButton(
                text = primaryActionLabel,
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            ExplorerLinkText(
                text = secondaryLabel,
                onClick = onSecondaryAction,
                color = Color.White.copy(alpha = 0.88f)
            )
        }
    }
}

@Composable
fun ExplorerAuthScreen(
    title: String,
    modifier: Modifier = Modifier,
    backgroundPainter: Painter? = null,
    formContent: @Composable ColumnScope.() -> Unit,
    bottomContent: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF04150E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(18.dp))

            ExplorerMediaPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                backgroundPainter = backgroundPainter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp, vertical = 26.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(120.dp))
                    formContent()
                    Spacer(Modifier.height(28.dp))
                    bottomContent()
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ExplorerPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailing: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            trailing?.invoke()
        }
    }
}

@Composable
fun ExplorerLinkText(
    text: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    TextButton(onClick = onClick) {
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

@Composable
private fun ExplorerMediaPanel(
    modifier: Modifier = Modifier,
    backgroundPainter: Painter? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(AuthHeroShape)
    ) {
        ExplorerMediaCard(
            modifier = Modifier.fillMaxSize(),
            backgroundPainter = backgroundPainter,
            shape = AuthHeroShape
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Black.copy(alpha = 0.18f),
                            Color.Black.copy(alpha = 0.38f)
                        )
                    )
                )
        )

        content()
    }
}

@Composable
private fun ExplorerMediaCard(
    modifier: Modifier,
    backgroundPainter: Painter?,
    shape: Shape
) {
    Box(modifier = modifier.clip(shape)) {
        if (backgroundPainter != null) {
            Image(
                painter = backgroundPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            ScenicNatureBackdrop(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun ScenicNatureBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFFC9CFCC),
                    Color(0xFF98A19B),
                    Color(0xFF435147),
                    Color(0xFF0E1712)
                )
            )
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.10f),
            radius = size.minDimension * 0.18f,
            center = Offset(size.width * 0.84f, size.height * 0.14f)
        )

        val fog = Path().apply {
            moveTo(0f, size.height * 0.24f)
            cubicTo(
                size.width * 0.18f, size.height * 0.16f,
                size.width * 0.42f, size.height * 0.30f,
                size.width, size.height * 0.20f
            )
            lineTo(size.width, size.height * 0.34f)
            cubicTo(
                size.width * 0.70f, size.height * 0.27f,
                size.width * 0.40f, size.height * 0.38f,
                0f, size.height * 0.30f
            )
            close()
        }
        drawPath(fog, color = Color.White.copy(alpha = 0.14f))

        val mountainBack = Path().apply {
            moveTo(0f, size.height * 0.52f)
            lineTo(size.width * 0.16f, size.height * 0.36f)
            lineTo(size.width * 0.28f, size.height * 0.49f)
            lineTo(size.width * 0.46f, size.height * 0.26f)
            lineTo(size.width * 0.61f, size.height * 0.50f)
            lineTo(size.width * 0.78f, size.height * 0.30f)
            lineTo(size.width, size.height * 0.48f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            mountainBack,
            brush = Brush.verticalGradient(listOf(Color(0xFF6B7471), Color(0xFF2A3530)))
        )

        val mountainFront = Path().apply {
            moveTo(0f, size.height * 0.62f)
            lineTo(size.width * 0.18f, size.height * 0.44f)
            lineTo(size.width * 0.32f, size.height * 0.60f)
            lineTo(size.width * 0.48f, size.height * 0.34f)
            lineTo(size.width * 0.68f, size.height * 0.62f)
            lineTo(size.width * 0.84f, size.height * 0.47f)
            lineTo(size.width, size.height * 0.58f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            mountainFront,
            brush = Brush.verticalGradient(listOf(Color(0xFF434C48), Color(0xFF151C17)))
        )

        val river = Path().apply {
            moveTo(size.width * 0.12f, size.height)
            cubicTo(
                size.width * 0.18f, size.height * 0.84f,
                size.width * 0.48f, size.height * 0.78f,
                size.width * 0.42f, size.height * 0.60f
            )
            cubicTo(
                size.width * 0.34f, size.height * 0.42f,
                size.width * 0.12f, size.height * 0.36f,
                size.width * 0.04f, size.height * 0.20f
            )
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            river,
            brush = Brush.linearGradient(
                listOf(Color.White.copy(alpha = 0.68f), Color(0xFFDDE8E2).copy(alpha = 0.14f)),
                start = Offset(size.width * 0.18f, size.height),
                end = Offset(size.width * 0.08f, size.height * 0.20f)
            )
        )

        clipPath(Path().apply { addRect(androidx.compose.ui.geometry.Rect(Offset.Zero, size)) }) {
            drawRect(Color.Black.copy(alpha = 0.05f), blendMode = BlendMode.Multiply)
        }

        repeat(5) { index ->
            val x = size.width * (0.14f + index * 0.15f)
            drawRoundRect(
                color = Color(0xFF0E1612).copy(alpha = 0.35f),
                topLeft = Offset(x, size.height * 0.67f),
                size = Size(size.width * 0.03f, size.height * (0.13f + 0.04f * (index % 2))),
                cornerRadius = CornerRadius(10f, 10f)
            )
        }
    }
}

object AuthHeroShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, size.height * 0.18f)

            quadraticBezierTo(
                size.width * 0.18f, size.height * 0.02f,
                size.width * 0.72f, size.height * 0.00f
            )

            quadraticBezierTo(
                size.width * 0.92f, size.height * 0.00f,
                size.width, size.height * 0.00f
            )

            lineTo(size.width, size.height)

            quadraticBezierTo(
                size.width * 0.72f, size.height * 0.98f,
                size.width * 0.22f, size.height * 1.00f
            )

            quadraticBezierTo(
                size.width * 0.02f, size.height * 0.78f,
                0f, size.height * 0.62f
            )

            close()
        }
        return Outline.Generic(path)
    }
}