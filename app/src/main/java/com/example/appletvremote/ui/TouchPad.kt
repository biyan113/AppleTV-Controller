package com.example.appletvremote.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.unit.dp
import com.example.appletvremote.TvButton

val PadCorner = 36.dp
private val Directions = listOf(TvButton.UP, TvButton.DOWN, TvButton.LEFT, TvButton.RIGHT)
private val ChevronColor = Color(0xFFD8D8DE)
private val RingColor = Color(0xFFD8D8DE)
private val PadFill = Color(0xFF1C1C21)
private val GlowFill = Color.White.copy(alpha = 0.14f)

/** Top/bottom fraction reserved for volume overlays so they don't fire Up/Down. */
internal const val PAD_VOLUME_BAND = 0.13f
/** Chevron distance from centre, as a fraction of half the short side. */
private const val CHEVRON_INSET = 0.58f
/** Select ring radius, as a fraction of half the short side. */
private const val RING_FRACTION = 0.18f

/**
 * Tall rounded pad: D-pad chevrons + hollow Select ring, centred in the card.
 */
@Composable
fun TouchPad(
    onDown: (TvButton) -> Unit,
    onUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var active by remember { mutableStateOf<TvButton?>(null) }
    var glowDirection by remember { mutableStateOf(TvButton.UP) }
    val glow by animateFloatAsState(
        if (active != null && active != TvButton.SELECT) 1f else 0f,
        tween(PRESS_ANIM_MS),
        label = "touch-glow",
    )
    val select by animateFloatAsState(
        if (active == TvButton.SELECT) 1f else 0f,
        tween(PRESS_ANIM_MS),
        label = "touch-select",
    )
    val down by rememberUpdatedState(onDown)
    val up by rememberUpdatedState(onUp)

    fun a11yPress(button: TvButton): Boolean {
        down(button); up(); return true
    }

    Box(
        modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(PadCorner))
            .clearAndSetSemantics {
                contentDescription = "Touch pad"
                onClick(label = TvButton.SELECT.label) { a11yPress(TvButton.SELECT) }
                customActions = Directions.map { b ->
                    CustomAccessibilityAction(b.label) { a11yPress(b) }
                }
            }
            .pointerInput(Unit) {
                val band = size.height * PAD_VOLUME_BAND
                val r = minOf(size.width, size.height) / 2f
                val selectRadius = maxOf(r * RING_FRACTION * 1.35f, MinTouch.toPx() / 2f)
                detectTapGestures(onPress = { pos ->
                    if (pos.y < band || pos.y > size.height - band) return@detectTapGestures
                    val button = padZoneAt(
                        pos.x, pos.y, size.width.toFloat(), size.height.toFloat(), selectRadius,
                    ) ?: return@detectTapGestures
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
            .drawBehind { drawTouchPad(glowDirection, glow, select) }
    )
}

private fun DrawScope.drawTouchPad(glowDirection: TvButton, glow: Float, select: Float) {
    val c = center
    val r = size.minDimension / 2f
    val corner = CornerRadius(PadCorner.toPx())

    drawRoundRect(PadFill, size = size, cornerRadius = corner)

    val chevron = r * 0.28f
    val inset = r * CHEVRON_INSET
    if (glow > 0f) {
        val at = c + glowDirection.unitVector() * inset
        drawCircle(GlowFill.copy(alpha = GlowFill.alpha * glow), radius = r * 0.30f, center = at)
    }

    val stroke = 3.5.dp.toPx()
    drawChevron(c + Offset(0f, -inset), chevron, ChevronColor, 0f, stroke)
    drawChevron(c + Offset(0f, inset), chevron, ChevronColor, 180f, stroke)
    drawChevron(c + Offset(-inset, 0f), chevron, ChevronColor, -90f, stroke)
    drawChevron(c + Offset(inset, 0f), chevron, ChevronColor, 90f, stroke)

    val ringR = r * RING_FRACTION
    scale(1f - 0.06f * select, pivot = c) {
        if (select > 0f) {
            drawCircle(Color.White.copy(alpha = 0.12f * select), radius = ringR, center = c)
        }
        drawCircle(RingColor, radius = ringR, center = c, style = Stroke(3.25.dp.toPx()))
    }
}

private fun TvButton.unitVector(): Offset = when (this) {
    TvButton.UP -> Offset(0f, -1f)
    TvButton.DOWN -> Offset(0f, 1f)
    TvButton.LEFT -> Offset(-1f, 0f)
    else -> Offset(1f, 0f)
}
