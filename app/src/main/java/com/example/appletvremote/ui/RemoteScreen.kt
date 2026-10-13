package com.example.appletvremote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SettingsRemote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.appletvremote.TvButton

private val ScreenPadding = 12.dp

@Composable
fun RemoteScreen(
    sendFailed: Boolean,
    onDown: (TvButton) -> Unit,
    onUp: () -> Unit,
    onOpenPairing: () -> Unit,
) {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .safeDrawingPadding()
    ) {
        // Phones and the AB02 case get a pad + grid. The aluminium replica only
        // appears when there is tablet-sized room for it.
        val hardwareLook = maxWidth >= 500.dp && maxHeight >= 900.dp
        if (hardwareLook) {
            val fitWidth = minOf(maxWidth * 0.6f, (maxHeight - ScreenPadding * 2) / BODY_ASPECT)
            val width = maxOf(fitWidth, MinRemoteWidth)
            val needsScroll = width * BODY_ASPECT + ScreenPadding * 2 > maxHeight
            Box(
                Modifier
                    .fillMaxWidth()
                    .then(if (needsScroll) Modifier.verticalScroll(rememberScrollState()) else Modifier.fillMaxSize())
                    .padding(vertical = ScreenPadding),
                contentAlignment = Alignment.Center,
            ) {
                SiriRemote(width, onDown, onUp)
            }
        } else {
            MobileRemote(onDown, onUp, onOpenPairing, Modifier.fillMaxSize())
        }

        if (hardwareLook) {
            IconButton(onClick = onOpenPairing, modifier = Modifier.align(Alignment.TopStart).padding(4.dp)) {
                Icon(Icons.Outlined.SettingsRemote, "Pairing mode", tint = RemoteColors.Secondary)
            }
        }

        if (sendFailed) {
            val compact = maxHeight < 560.dp || maxWidth < 400.dp
            Text(
                if (compact) "IR send failed." else "The IR blaster didn't accept the last command.",
                color = RemoteColors.Icon,
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(if (compact) 8.dp else 16.dp)
                    .background(Color(0xE6311A1A), RoundedCornerShape(12.dp))
                    .padding(horizontal = if (compact) 10.dp else 16.dp, vertical = if (compact) 6.dp else 10.dp),
            )
        }
    }
}
