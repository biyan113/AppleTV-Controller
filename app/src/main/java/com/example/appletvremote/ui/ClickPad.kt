package com.example.appletvremote.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.appletvremote.TvButton
import kotlin.math.abs
import kotlin.math.hypot

/** Visual diameter of the Select disc relative to the whole pad. */
internal const val CENTER_FRACTION = 0.42f

/** Extra touch area around the visible pad edge. */
private val PadTouchMargin = 10.dp

/**
 * Maps a touch at ([x], [y]) inside a [width] x [height] touch area to a button: within
 * [selectRadius] of the centre is SELECT, the rest of the inscribed circle is split into four
 * 90° wedges. Touches outside the circle return null.
 */
internal fun padZoneAt(x: Float, y: Float, width: Float, height: Float, selectRadius: Float): TvButton? {
    val dx = x - width / 2f
    val dy = y - height / 2f
    val distance = hypot(dx, dy)
    return when {
        distance > minOf(width, height) / 2f -> null
        distance <= selectRadius -> TvButton.SELECT
        abs(dx) > abs(dy) -> if (dx > 0) TvButton.RIGHT else TvButton.LEFT
        else -> if (dy > 0) TvButton.DOWN else TvButton.UP
    }
}

private fun TvButton.unitVector(): Offset = when (this) {
    TvButton.UP -> Offset(0f, -1f)
    TvButton.DOWN -> Offset(0f, 1f)
    TvButton.LEFT -> Offset(-1f, 0f)
    else -> Offset(1f, 0f)
}

private val Directions = listOf(TvButton.UP, TvButton.DOWN, TvButton.LEFT, TvButton.RIGHT)

/** The glossy black click pad, centred at ([cx], [cy]) in its parent. */
@Composable
fun ClickPad(diameter: Dp, cx: Dp, cy: Dp, onDown: (TvButton) -> Unit, onUp: () -> Unit) {
    var active by remember { mutableStateOf<TvButton?>(null) }
    // Kept after release so the glow fades out on the direction that was pressed.
    var glowDirection by remember { mutableStateOf(TvButton.UP) }
    val glow by animateFloatAsState(
        if (active != null && active != TvButton.SELECT) 1f else 0f, tween(PRESS_ANIM_MS), label = "glow",
    )
    val select by animateFloatAsState(
        if (active == TvButton.SELECT) 1f else 0f, tween(PRESS_ANIM_MS), label = "select",
    )
    val down by rememberUpdatedState(onDown)
    val up by rememberUpdatedState(onUp)
    val touchSize = diameter + PadTouchMargin * 2

    fun a11yPress(button: TvButton): Boolean {
        down(button); up(); return true
    }

    Box(
        Modifier
            .offset(cx - touchSize / 2, cy - touchSize / 2)
            .size(touchSize)
            .clearAndSetSemantics {
                contentDescription = "Click pad"
                onClick(label = TvButton.SELECT.label) { a11yPress(TvButton.SELECT) }
                customActions = Directions.map { b -> CustomAccessibilityAction(b.label) { a11yPress(b) } }
            }
            .pointerInput(diameter) {
                // Select keeps a 48dp target even when the visible disc is smaller.
                val selectRadius = maxOf(diameter.toPx() * CENTER_FRACTION / 2f, MinTouch.toPx() / 2f)
                detectTapGestures(onPress = { pos ->
                    val button = padZoneAt(pos.x, pos.y, size.width.toFloat(), size.height.toFloat(), selectRadius)
                        ?: return@detectTapGestures
                    active = button
                    if (button != TvButton.SELECT) glowDirection = button
                    down(button)
                    try {
                        tryAwaitRelease()
                    } finally {
                        up()
                        active = null
                    }
                })
            }
            .drawBehind { drawPad(diameter.toPx() / 2f, glowDirection, glow, select) }
    )
}

private fun DrawScope.drawPad(r: Float, glowDirection: TvButton, glow: Float, select: Float) {
    val c = center

    // Soft shadow where the pad sits in the aluminium.
    drawCircle(
        Brush.radialGradient(
            0.88f to Color.Black.copy(alpha = 0.35f), 1f to Color.Transparent,
            center = c + Offset(0f, 1.5.dp.toPx()), radius = r * 1.08f,
        ),
        radius = r * 1.08f, center = c + Offset(0f, 1.5.dp.toPx()),
    )
    // Machined chamfer: dark on top, catching light at the bottom, so the pad reads as inset.
    drawCircle(
        Brush.verticalGradient(listOf(Color(0xFF7C7C82), Color(0xFFF4F4F6)), c.y - r, c.y + r),
        radius = r + 1.2.dp.toPx(), center = c,
    )
    // Glossy black glass with an off-centre radial highlight.
    drawCircle(
        Brush.radialGradient(
            listOf(Color(0xFF34343A), Color(0xFF121214), Color(0xFF050506)),
            center = c + Offset(-0.28f * r, -0.38f * r), radius = r * 1.45f,
        ),
        radius = r, center = c,
    )
    // Specular sheen across the upper half.
    drawOval(
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.13f), Color.Transparent), c.y - r, c.y - 0.1f * r),
        topLeft = Offset(c.x - 0.78f * r, c.y - 0.96f * r), size = Size(1.56f * r, 0.86f * r),
    )

    if (glow > 0f) {
        val at = c + glowDirection.unitVector() * (0.68f * r)
        drawCircle(
            Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.30f * glow), Color.Transparent), center = at, radius = 0.42f * r,
            ),
            radius = 0.42f * r, center = at,
        )
    }

    for (d in Directions) {
        drawCircle(Color.White.copy(alpha = 0.9f), radius = 0.028f * r, center = c + d.unitVector() * (0.82f * r))
    }

    // Recessed Select disc.
    val cr = r * CENTER_FRACTION
    scale(1f - 0.04f * select, pivot = c) {
        drawCircle(
            Brush.verticalGradient(listOf(Color.Black, Color(0xFF3A3A3E)), c.y - cr, c.y + cr),
            radius = cr + 1.dp.toPx(), center = c,
        )
        drawCircle(
            Brush.radialGradient(
                listOf(lerp(Color(0xFF1E1E21), Color.Black, select), lerp(Color(0xFF09090A), Color.Black, select)),
                center = c + Offset(0f, 0.35f * cr), radius = 1.2f * cr,
            ),
            radius = cr, center = c,
        )
        // Inner shadow along the top rim of the recess.
        drawCircle(
            Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent), c.y - cr, c.y - 0.2f * cr),
            radius = cr, center = c,
        )
    }

    drawCircle(Color.White.copy(alpha = 0.08f), radius = r, center = c, style = Stroke(0.75.dp.toPx()))
}
