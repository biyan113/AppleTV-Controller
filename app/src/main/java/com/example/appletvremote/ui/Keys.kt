package com.example.appletvremote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role

/**
 * Fires [onDown] the moment a finger lands and [onUp] when it lifts or the gesture is
 * cancelled, which is what hold-to-repeat needs (a plain clickable only fires on release).
 * Accessibility services get a regular click that does a down/up pair.
 */
@Composable
fun Modifier.holdToSend(
    label: String,
    onDown: () -> Unit,
    onUp: () -> Unit,
    onPressedChange: (Boolean) -> Unit = {},
): Modifier {
    val down by rememberUpdatedState(onDown)
    val up by rememberUpdatedState(onUp)
    val pressedChange by rememberUpdatedState(onPressedChange)
    return this
        .clearAndSetSemantics {
            role = Role.Button
            contentDescription = label
            onClick { down(); up(); true }
        }
        .pointerInput(Unit) {
            detectTapGestures(onPress = {
                pressedChange(true)
                down()
                try {
                    tryAwaitRelease()
                } finally {
                    up()
                    pressedChange(false)
                }
            })
        }
}

@Composable
fun KeyButton(
    label: String,
    onDown: () -> Unit,
    onUp: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (pressed) RemoteColors.KeyPressed else RemoteColors.Key)
            .holdToSend(label, onDown, onUp) { pressed = it },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
