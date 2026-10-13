package com.example.appletvremote

import android.content.Context
import android.hardware.ConsumerIrManager

/*
 * Manifest requirements (AndroidManifest.xml):
 *
 *   <uses-permission android:name="android.permission.TRANSMIT_IR" />
 *   <uses-feature android:name="android.hardware.consumerir" android:required="false" />
 *
 * TRANSMIT_IR is a normal permission, so no runtime prompt is needed.
 * The phone must have a physical IR blaster (many Samsung/Xiaomi/Huawei models; Pixels do not).
 * The feature is declared not-required so phones without one can still install the app
 * and get a clear "no IR blaster" message (see isSupported).
 *
 * Pairing with Apple TV (one time):
 *   Settings > Remotes and Devices > Learn Remote > Start,
 *   then for each button tvOS names, press and hold the matching button in the app's
 *   pairing screen until the progress bar fills (it keeps resending while held).
 *   Hold the phone's IR window about 2-3 inches from the Apple TV.
 */

enum class TvButton(val command: Int) {
    UP(0x01),
    DOWN(0x02),
    LEFT(0x03),
    RIGHT(0x04),
    SELECT(0x05),
    MENU(0x06),        // Back
    TV_HOME(0x07),
    PLAY_PAUSE(0x08),
    VOL_UP(0x09),
    VOL_DOWN(0x0A),
    MUTE(0x0B),
    POWER(0x0C)
}

class AppleTvIr(context: Context, private val address: Int = 0x00) {

    private val ir: ConsumerIrManager? =
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

    val isSupported: Boolean get() = ir?.hasIrEmitter() == true

    /** Blocks for the length of the frame (~68 ms), so call it off the main thread. */
    fun send(button: TvButton): Boolean {
        val manager = ir ?: return false
        if (!manager.hasIrEmitter()) return false
        return try {
            manager.transmit(CARRIER_HZ, encodeNec(address, button.command))
            true
        } catch (e: RuntimeException) {
            // Some vendor HALs throw (e.g. unsupported carrier or a busy emitter).
            false
        }
    }

    companion object {
        private const val CARRIER_HZ = 38_000

        // Standard NEC timings in microseconds
        private const val HEADER_MARK = 9000
        private const val HEADER_SPACE = 4500
        private const val BIT_MARK = 560
        private const val ZERO_SPACE = 560
        private const val ONE_SPACE = 1690

        /** Builds an NEC frame: address, ~address, command, ~command, each sent LSB first. */
        fun encodeNec(address: Int, command: Int): IntArray {
            val data = (address and 0xFF) or
                (((address xor 0xFF) and 0xFF) shl 8) or
                ((command and 0xFF) shl 16) or
                (((command xor 0xFF) and 0xFF) shl 24)

            val pattern = ArrayList<Int>(67)
            pattern += HEADER_MARK
            pattern += HEADER_SPACE
            for (i in 0 until 32) {
                pattern += BIT_MARK
                pattern += if ((data shr i) and 1 == 1) ONE_SPACE else ZERO_SPACE
            }
            pattern += BIT_MARK // trailing mark
            return pattern.toIntArray()
        }
    }
}
