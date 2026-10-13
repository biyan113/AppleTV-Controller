package com.example.appletvremote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appletvremote.TvButton

/** Order tvOS asks for in Learn Remote first, then the rest. */
private val PairingOrder = listOf(
    TvButton.UP, TvButton.DOWN, TvButton.LEFT,
    TvButton.RIGHT, TvButton.SELECT, TvButton.MENU,
    TvButton.PLAY_PAUSE, TvButton.TV_HOME, TvButton.POWER,
    TvButton.VOL_UP, TvButton.VOL_DOWN, TvButton.MUTE,
)

private fun TvButton.hex() = "0x%02X".format(command)

@Composable
fun PairingScreen(
    lastSent: TvButton?,
    sendFailed: Boolean,
    onDown: (TvButton) -> Unit,
    onUp: () -> Unit,
    onDone: () -> Unit,
) {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(RemoteColors.Background)
            .safeDrawingPadding()
    ) {
        val compact = maxHeight < 560.dp || maxWidth < 400.dp
        val gutter = if (compact) 10.dp else 20.dp
        val tileH = if (compact) 52.dp else 80.dp
        val gap = if (compact) 8.dp else 12.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(gutter),
            verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 16.dp),
        ) {
            Text(
                "Pairing mode",
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (compact) {
                    "Apple TV: Settings › Remotes and Devices › Learn Remote. " +
                        "Hold each tile until the TV accepts it. Point the IR window from 5–8 cm."
                } else {
                    "1. On the Apple TV open Settings › Remotes and Devices › Learn Remote, then Start.\n" +
                        "2. Point the top edge of this phone at the Apple TV from 5–8 cm (2–3 in).\n" +
                        "3. For each button the TV names, press and hold the matching tile below until " +
                        "the progress bar fills. A tap sends the code once; holding keeps sending it."
                },
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge,
                color = RemoteColors.Secondary,
            )

            Text(
                text = when {
                    lastSent == null -> "Nothing sent yet."
                    sendFailed -> "Couldn't send ${lastSent.label} (${lastSent.hex()})."
                    else -> "Sent ${lastSent.label} · NEC code ${lastSent.hex()}"
                },
                color = if (sendFailed) RemoteColors.Power else RemoteColors.Icon,
                style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(RemoteColors.Body)
                    .padding(if (compact) 10.dp else 16.dp),
            )

            for (row in PairingOrder.chunked(3)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    for (button in row) {
                        KeyButton(
                            label = "Send ${button.label}",
                            onDown = { onDown(button) },
                            onUp = onUp,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(tileH),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(button.label, style = MaterialTheme.typography.titleSmall)
                                if (!compact) {
                                    Text(
                                        button.hex(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RemoteColors.Secondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(if (compact) 48.dp else 56.dp)) {
                Text("Done — open remote", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
