package com.example.appletvremote.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.appletvremote.TvButton
import kotlin.random.Random

/** Body height / width. The real 2nd-gen remote is closer to 3.8; 6 is the requested look. */
const val BODY_ASPECT = 6f
/** Below this width the 48dp touch targets would start to overlap. */
val MinRemoteWidth = 120.dp
val MinTouch = 48.dp
const val PRESS_ANIM_MS = 80

private const val CORNER_FRACTION = 0.12f
private const val CAP_FRACTION = 0.10f
private const val KEY_FRACTION = 0.32f
private const val LEFT_X = 0.27f
private const val RIGHT_X = 0.73f

private val KeyTop = Color(0xFF3A3A3E)
private val KeyTopPressed = Color(0xFF1B1B1E)
private val KeyBottom = Color(0xFF0D0D0F)

/**
 * The Siri Remote, drawn entirely with Canvas and gradients. Every position and size is a
 * fraction of [width], so it scales with the screen; touch areas never shrink below 48dp.
 */
@Composable
fun SiriRemote(width: Dp, onDown: (TvButton) -> Unit, onUp: () -> Unit, modifier: Modifier = Modifier) {
    val w = width
    val key = w * KEY_FRACTION

    Box(
        modifier
            .size(w, w * BODY_ASPECT)
            .shadow(28.dp, RoundedCornerShape(w * CORNER_FRACTION), clip = false,
                ambientColor = Color.Black, spotColor = Color.Black)
            .drawWithCache {
                val body = Path().apply {
                    addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(size.width * CORNER_FRACTION)))
                }
                val brushed = brushedStreaks(size, 0.75.dp.toPx())
                onDrawBehind { drawBody(body, brushed) }
            }
    ) {
        // Drawn first so the buttons below win any overlap in hit testing.
        ClickPad(diameter = w * 0.86f, cx = w * 0.5f, cy = w * 0.95f, onDown = onDown, onUp = onUp)

        Key(TvButton.POWER, w * 0.78f, w * 0.20f, w * 0.18f, w * 0.18f, onDown, onUp) { vs, p ->
            drawPowerKey(vs, p)
        }
        Key(TvButton.MENU, w * LEFT_X, w * 1.66f, key, key, onDown, onUp) { vs, p ->
            drawBlackKey(vs, p, corner = vs.width * 0.26f) { c, g -> drawBackGlyph(c, g) }
        }
        Key(TvButton.TV_HOME, w * RIGHT_X, w * 1.66f, key, key, onDown, onUp) { vs, p ->
            drawBlackKey(vs, p, corner = vs.width * 0.26f) { c, g -> drawTvGlyph(c, g) }
        }
        Key(TvButton.PLAY_PAUSE, w * LEFT_X, w * 2.12f, key, key, onDown, onUp) { vs, p ->
            drawBlackKey(vs, p, corner = vs.width / 2f) { c, g -> drawPlayPauseGlyph(c, g) }
        }
        Key(TvButton.MUTE, w * LEFT_X, w * 2.60f, key, key, onDown, onUp) { vs, p ->
            drawBlackKey(vs, p, corner = vs.width / 2f) { c, g -> drawMuteGlyph(c, g) }
        }
        // Spans from the top of Play/Pause to the bottom of Mute.
        VolumePill(cx = w * RIGHT_X, top = w * 2.12f - key / 2, pillWidth = key,
            pillHeight = w * 0.48f + key, onDown = onDown, onUp = onUp)
    }
}

/** A pressable control centred at ([cx], [cy]); the visual is [vw] x [vh], the touch area at least 48dp. */
@Composable
private fun Key(
    button: TvButton,
    cx: Dp, cy: Dp, vw: Dp, vh: Dp,
    onDown: (TvButton) -> Unit,
    onUp: () -> Unit,
    draw: DrawScope.(visual: Size, pressed: Float) -> Unit,
) {
    val tw = maxOf(vw, MinTouch)
    val th = maxOf(vh, MinTouch)
    var pressed by remember { mutableStateOf(false) }
    val p by animateFloatAsState(if (pressed) 1f else 0f, tween(PRESS_ANIM_MS), label = button.name)
    Box(
        Modifier
            .offset(cx - tw / 2, cy - th / 2)
            .size(tw, th)
            .holdToSend(button.label, { onDown(button) }, onUp) { pressed = it }
            .drawBehind {
                val vs = Size(vw.toPx(), vh.toPx())
                translate((size.width - vs.width) / 2f, (size.height - vs.height) / 2f) { draw(vs, p) }
            }
    )
}

@Composable
private fun VolumePill(
    cx: Dp, top: Dp, pillWidth: Dp, pillHeight: Dp,
    onDown: (TvButton) -> Unit,
    onUp: () -> Unit,
) {
    var pressedHalf by remember { mutableStateOf<TvButton?>(null) }
    // Kept after release so the darkening fades out on the half that was pressed.
    var lastHalf by remember { mutableStateOf(TvButton.VOL_UP) }
    val p by animateFloatAsState(if (pressedHalf != null) 1f else 0f, tween(PRESS_ANIM_MS), label = "volume")
    val tw = maxOf(pillWidth, MinTouch)

    Box(
        Modifier
            .offset(cx - tw / 2, top)
            .size(tw, pillHeight)
            .drawBehind {
                val vs = Size(pillWidth.toPx(), pillHeight.toPx())
                translate((size.width - vs.width) / 2f, 0f) { drawVolumePill(vs, p, lastHalf) }
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            for (half in listOf(TvButton.VOL_UP, TvButton.VOL_DOWN)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .holdToSend(half.label, { onDown(half) }, onUp) { down ->
                            pressedHalf = if (down) half else null
                            if (down) lastHalf = half
                        }
                )
            }
        }
    }
}

// ---- Drawing ----

/** Horizontal hairlines at random offsets and lengths, bucketed by tone so they draw as 3 paths. */
private fun brushedStreaks(size: Size, step: Float): List<Path> {
    val rnd = Random(42)
    val paths = List(3) { Path() }
    var y = 0f
    while (y < size.height) {
        repeat(3) {
            val x0 = rnd.nextFloat() * size.width * 1.2f - size.width * 0.2f
            val len = size.width * (0.15f + rnd.nextFloat() * 0.85f)
            paths[rnd.nextInt(3)].apply { moveTo(x0, y); lineTo(x0 + len, y) }
        }
        y += step
    }
    return paths
}

private val StreakColors = listOf(
    Color.White.copy(alpha = 0.10f), Color.Black.copy(alpha = 0.045f), Color(0xFF8E8E93).copy(alpha = 0.06f),
)

private fun DrawScope.drawBody(body: Path, brushed: List<Path>) {
    val w = size.width
    val cap = w * CAP_FRACTION

    // Siri button: a thin silver strip standing proud of the right edge.
    drawSideButton(top = w * 0.62f, bottom = w * 1.18f)

    clipPath(body) {
        drawRect(
            Brush.verticalGradient(listOf(Color(0xFFEDEDEF), Color(0xFFD6D7DA), Color(0xFFC2C3C7), Color(0xFFB4B5BA)))
        )
        val hairline = 0.6.dp.toPx()
        brushed.forEachIndexed { i, path -> drawPath(path, StreakColors[i], style = Stroke(hairline)) }

        // Bright highlight on the left edge, gentle falloff on the right.
        drawRect(
            Brush.horizontalGradient(
                0f to Color.White.copy(alpha = 0.95f), 0.35f to Color.White.copy(alpha = 0.35f), 1f to Color.Transparent,
                endX = w * 0.07f,
            ),
            size = Size(w * 0.07f, size.height),
        )
        drawRect(
            Brush.horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.14f)), startX = w * 0.88f, endX = w),
            topLeft = Offset(w * 0.88f, 0f), size = Size(w * 0.12f, size.height),
        )

        // Glossy black top cap with a microphone hole.
        drawRect(Brush.verticalGradient(listOf(Color(0xFF2C2C30), Color(0xFF060607)), endY = cap), size = Size(w, cap))
        drawRect(
            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.20f), Color.Transparent), endY = cap * 0.55f),
            size = Size(w, cap * 0.55f),
        )
        drawLine(Color.Black.copy(alpha = 0.55f), Offset(0f, cap), Offset(w, cap), 1.dp.toPx())
        drawLine(Color.White.copy(alpha = 0.6f), Offset(0f, cap + 1.dp.toPx()), Offset(w, cap + 1.dp.toPx()), 0.5.dp.toPx())
        val mic = Offset(w / 2f, cap * 0.5f)
        drawCircle(Color.Black, radius = w * 0.014f, center = mic)
        drawCircle(Color.White.copy(alpha = 0.18f), radius = w * 0.014f, center = mic, style = Stroke(0.5.dp.toPx()))
    }
    drawPath(body, Color.Black.copy(alpha = 0.35f), style = Stroke(1.dp.toPx()))
}

private fun DrawScope.drawSideButton(top: Float, bottom: Float) {
    val w = size.width
    val left = w - 0.015f * w
    val stripWidth = 0.06f * w
    drawRoundRect(
        Brush.horizontalGradient(
            listOf(Color(0xFFB8B9BE), Color(0xFFF6F6F8), Color(0xFF9C9DA2)), startX = left, endX = left + stripWidth,
        ),
        topLeft = Offset(left, top), size = Size(stripWidth, bottom - top), cornerRadius = CornerRadius(stripWidth / 2f),
    )
    drawRoundRect(
        Color.Black.copy(alpha = 0.25f), topLeft = Offset(left, top), size = Size(stripWidth, bottom - top),
        cornerRadius = CornerRadius(stripWidth / 2f), style = Stroke(0.5.dp.toPx()),
    )
    drawMicGlyph(
        Offset(left + stripWidth / 2f, (top + bottom) / 2f), g = stripWidth * 0.9f,
        color = Color(0xFF5A5A5F), strokeWidth = 0.8.dp.toPx(),
    )
}

/** A soft shadow under a key that tightens as it is pressed ([p] = 0..1). */
private fun DrawScope.drawKeyShadow(vs: Size, p: Float, corner: Float) {
    val k = 1f - 0.6f * p
    for (i in 1..3) {
        val grow = i * 1.1.dp.toPx() * k
        drawRoundRect(
            Color.Black.copy(alpha = 0.09f * k),
            topLeft = Offset(-grow, -grow + 2.dp.toPx() * k),
            size = Size(vs.width + 2 * grow, vs.height + 2 * grow),
            cornerRadius = CornerRadius(corner + grow),
        )
    }
}

private fun DrawScope.drawBlackKeyFace(vs: Size, p: Float, corner: Float) {
    val radius = CornerRadius(corner)
    drawRoundRect(
        Brush.verticalGradient(listOf(lerp(KeyTop, KeyTopPressed, p), lerp(KeyBottom, Color.Black, p)), 0f, vs.height),
        size = vs, cornerRadius = radius,
    )
    drawRoundRect(
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.18f * (1f - 0.6f * p)), Color.Transparent), 0f, vs.height * 0.55f,
        ),
        size = vs, cornerRadius = radius,
    )
    drawRoundRect(Color.White.copy(alpha = 0.10f), size = vs, cornerRadius = radius, style = Stroke(0.75.dp.toPx()))
}

private fun DrawScope.drawBlackKey(
    vs: Size, p: Float, corner: Float,
    glyph: DrawScope.(center: Offset, g: Float) -> Unit,
) {
    val c = Offset(vs.width / 2f, vs.height / 2f)
    drawKeyShadow(vs, p, corner)
    scale(1f - 0.04f * p, pivot = c) {
        drawBlackKeyFace(vs, p, corner)
        glyph(c, minOf(vs.width, vs.height) * 0.40f)
    }
}

private fun DrawScope.drawPowerKey(vs: Size, p: Float) {
    val c = Offset(vs.width / 2f, vs.height / 2f)
    val r = vs.width / 2f
    drawKeyShadow(vs, p, r)
    scale(1f - 0.04f * p, pivot = c) {
        drawCircle(
            Brush.verticalGradient(
                listOf(lerp(Color(0xFFFAFAFB), Color(0xFFBDBDC2), p), lerp(Color(0xFF8E8E93), Color(0xFF6C6C70), p)),
                0f, vs.height,
            ),
            radius = r, center = c,
        )
        drawCircle(
            Brush.verticalGradient(listOf(lerp(KeyTop, KeyTopPressed, p), lerp(KeyBottom, Color.Black, p)), 0f, vs.height),
            radius = r * 0.80f, center = c,
        )
        drawPowerGlyph(c, r * 0.95f)
    }
}

private fun DrawScope.drawVolumePill(vs: Size, p: Float, pressedHalf: TvButton) {
    val corner = vs.width / 2f
    val c = Offset(vs.width / 2f, vs.height / 2f)
    drawKeyShadow(vs, p, corner)
    scale(1f - 0.04f * p, pivot = c) {
        drawBlackKeyFace(vs, 0f, corner)
        // Darken only the half being pressed.
        val halfTop = if (pressedHalf == TvButton.VOL_UP) 0f else vs.height / 2f
        clipRect(top = halfTop, bottom = halfTop + vs.height / 2f, right = vs.width) {
            drawRoundRect(Color.Black.copy(alpha = 0.45f * p), size = vs, cornerRadius = CornerRadius(corner))
        }
        val g = vs.width * 0.40f
        drawPlusGlyph(Offset(c.x, vs.width * 0.62f), g)
        drawMinusGlyph(Offset(c.x, vs.height - vs.width * 0.62f), g)
    }
}
