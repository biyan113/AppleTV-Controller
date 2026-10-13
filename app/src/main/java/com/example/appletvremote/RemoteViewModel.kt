package com.example.appletvremote

import android.app.Application
import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class Screen { NO_IR, PAIRING, REMOTE }

data class RemoteUiState(
    val screen: Screen,
    /** Last button sent while pairing, so the user can see which code went out. */
    val lastPairingSend: TvButton? = null,
    val lastSendFailed: Boolean = false,
)

class RemoteViewModel(app: Application) : AndroidViewModel(app) {

    private val tv = AppleTvIr(app)
    private val prefs = app.getSharedPreferences("remote", Context.MODE_PRIVATE)

    // ConsumerIrManager.transmit() blocks for the whole frame; one thread keeps frames in order.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val irDispatcher = Dispatchers.IO.limitedParallelism(1)

    private val _state = MutableStateFlow(
        RemoteUiState(
            screen = when {
                !tv.isSupported -> Screen.NO_IR
                prefs.getBoolean(KEY_PAIRED, false) -> Screen.REMOTE
                else -> Screen.PAIRING
            }
        )
    )
    val state: StateFlow<RemoteUiState> = _state.asStateFlow()

    private var repeatJob: Job? = null

    /** Sends a single frame. */
    fun tap(button: TvButton) {
        viewModelScope.launch(irDispatcher) { transmit(button) }
    }

    /**
     * Sends immediately, then keeps resending every [REPEAT_INTERVAL_MS] until [stopRepeat].
     * A short initial delay stops an ordinary tap from registering twice.
     */
    fun startRepeat(button: TvButton) {
        stopRepeat()
        // Sent separately so an immediate stopRepeat() (quick tap) can't cancel the first frame.
        tap(button)
        repeatJob = viewModelScope.launch(irDispatcher) {
            delay(REPEAT_INITIAL_DELAY_MS)
            while (isActive) {
                // transmit() itself takes ~68 ms, so only wait out the rest of the period.
                val start = SystemClock.elapsedRealtime()
                transmit(button)
                delay((REPEAT_INTERVAL_MS - (SystemClock.elapsedRealtime() - start)).coerceAtLeast(0))
            }
        }
    }

    fun stopRepeat() {
        repeatJob?.cancel()
        repeatJob = null
    }

    fun openPairing() {
        stopRepeat()
        _state.update { it.copy(screen = Screen.PAIRING, lastPairingSend = null) }
    }

    fun finishPairing() {
        prefs.edit().putBoolean(KEY_PAIRED, true).apply()
        _state.update { it.copy(screen = Screen.REMOTE, lastPairingSend = null) }
    }

    private fun transmit(button: TvButton) {
        val ok = tv.send(button)
        _state.update {
            it.copy(
                lastPairingSend = if (it.screen == Screen.PAIRING) button else it.lastPairingSend,
                lastSendFailed = !ok,
            )
        }
    }

    override fun onCleared() {
        stopRepeat()
    }

    companion object {
        private const val KEY_PAIRED = "paired"
        const val REPEAT_INITIAL_DELAY_MS = 300L
        const val REPEAT_INTERVAL_MS = 110L
    }
}
