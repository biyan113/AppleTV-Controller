package com.example.appletvremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appletvremote.ui.NoIrScreen
import com.example.appletvremote.ui.PairingScreen
import com.example.appletvremote.ui.RemoteScreen
import com.example.appletvremote.ui.RemoteTheme
import com.example.appletvremote.ui.rememberHaptic
import com.example.appletvremote.ui.repeats

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RemoteTheme {
                val vm: RemoteViewModel = viewModel()
                val state by vm.state.collectAsStateWithLifecycle()
                val haptic = rememberHaptic()

                when (state.screen) {
                    Screen.NO_IR -> NoIrScreen()
                    Screen.PAIRING -> PairingScreen(
                        lastSent = state.lastPairingSend,
                        sendFailed = state.lastSendFailed,
                        // Learn Remote needs a held signal, so every pairing tile repeats.
                        onDown = { haptic(); vm.startRepeat(it) },
                        onUp = vm::stopRepeat,
                        onDone = vm::finishPairing,
                    )
                    Screen.REMOTE -> RemoteScreen(
                        sendFailed = state.lastSendFailed,
                        onDown = { haptic(); if (it.repeats) vm.startRepeat(it) else vm.tap(it) },
                        onUp = vm::stopRepeat,
                        onOpenPairing = vm::openPairing,
                    )
                }
            }
        }
    }
}
