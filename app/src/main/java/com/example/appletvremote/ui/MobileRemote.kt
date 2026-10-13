package com.example.appletvremote.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.appletvremote.TvButton

private val CompactHeight = 560.dp
private val IconTint = Color(0xFFE8E8ED)

/** Phone remote matching a full-screen pad + circular keys, not a hardware replica. */
@Composable
fun MobileRemote(
    onDown: (TvButton) -> Unit,
    onUp: () -> Unit,
    onOpenPairing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val compact = maxHeight < CompactHeight
        val gutter = if (compact) 10.dp else 18.dp
        val topBtn = if (compact) 40.dp else 48.dp
        val sideBtn = if (compact) 52.dp else 68.dp
        val backBtn = if (compact) 64.dp else 88.dp

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = gutter)
                .padding(bottom = if (compact) 8.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 16.dp),
        ) {
            TopBar(topBtn, compact, onDown, onUp, onOpenPairing)

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                TouchPad(onDown, onUp, Modifier.fillMaxSize())
                PadIcon(
                    TvButton.VOL_UP, onDown, onUp,
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = if (compact) 10.dp else 18.dp)
                        .size(if (compact) 36.dp else 44.dp),
                ) { c, g -> drawPlusGlyph(c, g, IconTint) }
                PadIcon(
                    TvButton.VOL_DOWN, onDown, onUp,
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (compact) 10.dp else 18.dp)
                        .size(if (compact) 36.dp else 44.dp),
                ) { c, g -> drawMinusGlyph(c, g, IconTint) }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (compact) 8.dp else 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircleKey(TvButton.PLAY_PAUSE, sideBtn, onDown, onUp) { c, g ->
                    drawPlayPauseGlyph(c, g, IconTint)
                }
                CircleKey(TvButton.MENU, backBtn, onDown, onUp) { c, g ->
                    drawBackGlyph(c, g, IconTint)
                }
                CircleKey(TvButton.TV_HOME, sideBtn, onDown, onUp) { c, g ->
                    drawTvGlyph(c, g, IconTint)
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    size: Dp,
    compact: Boolean,
    onDown: (TvButton) -> Unit,
    onUp: () -> Unit,
    onOpenPairing: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(size),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CircleKey(TvButton.MUTE, size, onDown, onUp) { c, g -> drawMuteGlyph(c, g, IconTint) }
        DevicePill(
            compact = compact,
            onClick = onOpenPairing,
            modifier = Modifier.weight(1f).height(size),
        )
        CircleKey(TvButton.POWER, size, onDown, onUp) { c, g -> drawPowerGlyph(c, g, IconTint) }
    }
}

@Composable
private fun DevicePill(
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(RemoteColors.Key)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Canvas(Modifier.size(if (compact) 16.dp else 18.dp)) {
            drawTvGlyph(center, minOf(size.width, size.height), IconTint)
        }
        Spacer(Modifier.size(8.dp))
        Text(
            "Apple TV",
            color = IconTint,
            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            maxLines = 1,
        )
        Text("  ›", color = RemoteColors.Secondary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CircleKey(
    button: TvButton,
    size: Dp,
    onDown: (TvButton) -> Unit,
    onUp: () -> Unit,
    glyph: DrawScope.(Offset, Float) -> Unit,
) {
    KeyButton(
        label = button.label,
        onDown = { onDown(button) },
        onUp = onUp,
        shape = CircleShape,
        modifier = Modifier.size(size),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val s = this.size
            glyph(center, minOf(s.width, s.height) * 0.40f)
        }
    }
}

@Composable
private fun PadIcon(
    button: TvButton,
    onDown: (TvButton) -> Unit,
    onUp: () -> Unit,
    modifier: Modifier,
    glyph: DrawScope.(Offset, Float) -> Unit,
) {
    Box(
        modifier.holdToSend(button.label, { onDown(button) }, onUp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            glyph(center, minOf(size.width, size.height) * 0.42f)
        }
    }
}
