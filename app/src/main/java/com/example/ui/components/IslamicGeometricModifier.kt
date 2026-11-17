package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.ui.theme.RadiantGold

/**
 * Draws subtle concentric 8-point Islamic geometric star medallions and halos
 * in the top-right backdrop of hero cards without cluttering foreground text.
 */
fun Modifier.islamicGeometricWatermark(
  tint: Color = RadiantGold,
  alpha: Float = 0.14f
): Modifier = this.drawBehind {
  val cx = size.width * 0.88f
  val cy = size.height * 0.22f
  val outerRadius = minOf(size.width, size.height) * 0.36f
  val innerRadius = outerRadius * 0.68f
  val strokeWidth = 1.5.dp.toPx()

  drawCircle(
    color = tint.copy(alpha = alpha * 0.7f),
    radius = outerRadius * 1.18f,
    center = Offset(cx, cy),
    style = Stroke(width = strokeWidth * 0.8f)
  )

  val squareSide = outerRadius * 1.15f
  val topLeft = Offset(cx - squareSide / 2f, cy - squareSide / 2f)
  val boxSize = Size(squareSide, squareSide)

  drawRect(
    color = tint.copy(alpha = alpha),
    topLeft = topLeft,
    size = boxSize,
    style = Stroke(width = strokeWidth)
  )

  rotate(degrees = 45f, pivot = Offset(cx, cy)) {
    drawRect(
      color = tint.copy(alpha = alpha),
      topLeft = topLeft,
      size = boxSize,
      style = Stroke(width = strokeWidth)
    )
  }

  drawCircle(
    color = tint.copy(alpha = alpha * 0.85f),
    radius = innerRadius * 0.55f,
    center = Offset(cx, cy),
    style = Stroke(width = strokeWidth * 0.9f)
  )
}

@Composable
fun GoldHairlineDivider(
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(1.dp)
      .background(
        Brush.horizontalGradient(
          colors = listOf(
            Color.Transparent,
            RadiantGold.copy(alpha = 0.55f),
            Color.Transparent
          )
        )
      )
  )
}
