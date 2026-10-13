package com.example.appletvremote.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

/*
 * Thin-stroke vector glyphs. Each is drawn in a g x g box centred on c; coordinates
 * below are fractions of that box (0..1).
 */

private class GlyphBox(val c: Offset, val g: Float) {
    fun pt(x: Float, y: Float) = Offset(c.x + (x - 0.5f) * g, c.y + (y - 0.5f) * g)
}

private fun Path.moveTo(o: Offset) = moveTo(o.x, o.y)
private fun Path.lineTo(o: Offset) = lineTo(o.x, o.y)

private fun DrawScope.glyphStroke(width: Float = 1.5.dp.toPx()) =
    Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)

private fun DrawScope.polyline(b: GlyphBox, color: Color, close: Boolean, vararg xy: Float) {
    val path = Path().apply {
        moveTo(b.pt(xy[0], xy[1]))
        for (i in 2 until xy.size step 2) lineTo(b.pt(xy[i], xy[i + 1]))
        if (close) close()
    }
    drawPath(path, color, style = glyphStroke())
}

private fun DrawScope.segment(b: GlyphBox, color: Color, x0: Float, y0: Float, x1: Float, y1: Float) =
    drawLine(color, b.pt(x0, y0), b.pt(x1, y1), 1.5.dp.toPx(), StrokeCap.Round)

fun DrawScope.drawBackGlyph(c: Offset, g: Float, color: Color = Color.White) =
    polyline(GlyphBox(c, g), color, close = false, 0.62f, 0.16f, 0.30f, 0.5f, 0.62f, 0.84f)

fun DrawScope.drawTvGlyph(c: Offset, g: Float, color: Color = Color.White) {
    val b = GlyphBox(c, g)
    drawRoundRect(
        color, topLeft = b.pt(0.04f, 0.16f), size = Size(0.92f * g, 0.58f * g),
        cornerRadius = CornerRadius(0.08f * g), style = glyphStroke(),
    )
    segment(b, color, 0.30f, 0.88f, 0.70f, 0.88f)
}

fun DrawScope.drawPlayPauseGlyph(c: Offset, g: Float, color: Color = Color.White) {
    val b = GlyphBox(c, g)
    polyline(b, color, close = true, 0.02f, 0.24f, 0.02f, 0.76f, 0.44f, 0.5f)
    segment(b, color, 0.68f, 0.26f, 0.68f, 0.74f)
    segment(b, color, 0.90f, 0.26f, 0.90f, 0.74f)
}

fun DrawScope.drawMuteGlyph(c: Offset, g: Float, color: Color = Color.White) {
    val b = GlyphBox(c, g)
    polyline(b, color, close = true,
        0.04f, 0.38f, 0.22f, 0.38f, 0.46f, 0.16f, 0.46f, 0.84f, 0.22f, 0.62f, 0.04f, 0.62f)
    segment(b, color, 0.64f, 0.38f, 0.94f, 0.68f)
    segment(b, color, 0.94f, 0.38f, 0.64f, 0.68f)
}

fun DrawScope.drawPlusGlyph(c: Offset, g: Float, color: Color = Color.White) {
    val b = GlyphBox(c, g)
    segment(b, color, 0.12f, 0.5f, 0.88f, 0.5f)
    segment(b, color, 0.5f, 0.12f, 0.5f, 0.88f)
}

fun DrawScope.drawMinusGlyph(c: Offset, g: Float, color: Color = Color.White) =
    segment(GlyphBox(c, g), color, 0.12f, 0.5f, 0.88f, 0.5f)

/** Open chevron; [degrees] 0 = up, 90 = right, 180 = down, -90 = left. */
fun DrawScope.drawChevron(c: Offset, g: Float, color: Color, degrees: Float, strokeWidth: Float = 3.dp.toPx()) {
    val path = Path().apply {
        moveTo(c.x - 0.50f * g, c.y + 0.18f * g)
        lineTo(c.x, c.y - 0.32f * g)
        lineTo(c.x + 0.50f * g, c.y + 0.18f * g)
    }
    rotate(degrees, c) {
        drawPath(path, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

fun DrawScope.drawPowerGlyph(c: Offset, g: Float, color: Color = Color.White) {
    val b = GlyphBox(c, g)
    // Gap centred at 12 o'clock (-90°): start 30° past it and sweep the remaining 300°.
    drawArc(
        color, startAngle = -60f, sweepAngle = 300f, useCenter = false,
        topLeft = b.pt(0.12f, 0.14f), size = Size(0.76f * g, 0.76f * g), style = glyphStroke(),
    )
    segment(b, color, 0.5f, 0.04f, 0.5f, 0.46f)
}

fun DrawScope.drawMicGlyph(c: Offset, g: Float, color: Color, strokeWidth: Float) {
    val b = GlyphBox(c, g)
    val stroke = glyphStroke(strokeWidth)
    drawRoundRect(
        color, topLeft = b.pt(0.36f, 0.04f), size = Size(0.28f * g, 0.52f * g),
        cornerRadius = CornerRadius(0.14f * g), style = stroke,
    )
    drawArc(
        color, startAngle = 0f, sweepAngle = 180f, useCenter = false,
        topLeft = b.pt(0.20f, 0.18f), size = Size(0.60f * g, 0.58f * g), style = stroke,
    )
    drawLine(color, b.pt(0.5f, 0.76f), b.pt(0.5f, 0.94f), strokeWidth, StrokeCap.Round)
}
