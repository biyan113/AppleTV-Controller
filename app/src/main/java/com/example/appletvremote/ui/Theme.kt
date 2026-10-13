package com.example.appletvremote.ui

import android.view.HapticFeedbackConstants
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import com.example.appletvremote.TvButton

object RemoteColors {
    val Background = Color(0xFF000000)
    val Body = Color(0xFF1C1C1E)
    val Key = Color(0xFF2C2C2E)
    val KeyPressed = Color(0xFF48484A)
    val Pad = Color(0xFF2C2C2E)
    val PadHighlight = Color(0xFF3A3A3C)
    val Icon = Color(0xFFF2F2F7)
    val Secondary = Color(0xFF98989F)
    val Power = Color(0xFFFF453A)
}

private val scheme = darkColorScheme(
    primary = Color(0xFF0A84FF),
    background = RemoteColors.Background,
    surface = RemoteColors.Body,
    onBackground = RemoteColors.Icon,
    onSurface = RemoteColors.Icon,
)

@Composable
fun RemoteTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}

/** A short key-click haptic. VIRTUAL_KEY works on API 24 and honours the system setting. */
@Composable
fun rememberHaptic(): () -> Unit {
    val view = LocalView.current
    return remember(view) { { view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) } }
}

val TvButton.label: String
    get() = when (this) {
        TvButton.UP -> "Up"
        TvButton.DOWN -> "Down"
        TvButton.LEFT -> "Left"
        TvButton.RIGHT -> "Right"
        TvButton.SELECT -> "Select"
        TvButton.MENU -> "Back"
        TvButton.TV_HOME -> "TV / Home"
        TvButton.PLAY_PAUSE -> "Play / Pause"
        TvButton.VOL_UP -> "Volume up"
        TvButton.VOL_DOWN -> "Volume down"
        TvButton.MUTE -> "Mute"
        TvButton.POWER -> "Power"
    }

/** Arrows and volume repeat while held; everything else is a single press. */
val TvButton.repeats: Boolean
    get() = this == TvButton.UP || this == TvButton.DOWN || this == TvButton.LEFT ||
        this == TvButton.RIGHT || this == TvButton.VOL_UP || this == TvButton.VOL_DOWN
