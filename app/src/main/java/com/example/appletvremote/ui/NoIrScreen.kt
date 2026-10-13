package com.example.appletvremote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SettingsRemote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun NoIrScreen() {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(RemoteColors.Background)
            .safeDrawingPadding()
    ) {
        val compact = maxHeight < 560.dp || maxWidth < 400.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (compact) 16.dp else 32.dp),
            verticalArrangement = Arrangement.spacedBy(
                if (compact) 12.dp else 20.dp,
                Alignment.CenterVertically,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Outlined.SettingsRemote,
                contentDescription = null,
                tint = RemoteColors.Power,
                modifier = Modifier.size(if (compact) 48.dp else 96.dp),
            )
            Text(
                "No IR blaster found",
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = RemoteColors.Icon,
                textAlign = TextAlign.Center,
            )
            Text(
                if (compact) {
                    "This device has no IR transmitter, so it can't control an Apple TV."
                } else {
                    "This phone doesn't have an infrared transmitter, so it can't control an Apple TV. " +
                        "You need a phone with a built-in IR blaster, such as many Xiaomi, Redmi, POCO " +
                        "and Huawei models. Google Pixel phones don't have one."
                },
                style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                color = RemoteColors.Secondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
