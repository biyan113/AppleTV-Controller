package com.example.appletvremote

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppleTvIrTest {

    /** Decodes a pattern back into its 32 data bits (LSB first), asserting NEC structure. */
    private fun decode(pattern: IntArray): Long {
        assertEquals("header + 32 bits + trailing mark", 2 + 64 + 1, pattern.size)
        assertEquals(9000, pattern[0])
        assertEquals(4500, pattern[1])
        var data = 0L
        for (i in 0 until 32) {
            val mark = pattern[2 + i * 2]
            val space = pattern[3 + i * 2]
            assertEquals("bit $i mark", 560, mark)
            when (space) {
                1690 -> data = data or (1L shl i)
                560 -> Unit
                else -> throw AssertionError("bit $i has invalid space $space")
            }
        }
        assertEquals("trailing mark", 560, pattern.last())
        return data
    }

    private fun bytesOf(data: Long) = IntArray(4) { ((data shr (it * 8)) and 0xFF).toInt() }

    @Test
    fun upButtonProducesExactNecFrame() {
        // Address 0x00, ~0xFF, command 0x01, ~0xFE, each byte LSB first.
        val bits = "00000000" + "11111111" + "10000000" + "01111111"
        val expected = mutableListOf(9000, 4500)
        for (c in bits) {
            expected += 560
            expected += if (c == '1') 1690 else 560
        }
        expected += 560

        assertArrayEquals(expected.toIntArray(), AppleTvIr.encodeNec(0x00, TvButton.UP.command))
    }

    @Test
    fun everyButtonRoundTripsWithInvertedBytes() {
        for (button in TvButton.values()) {
            val bytes = bytesOf(decode(AppleTvIr.encodeNec(0x00, button.command)))
            assertEquals(button.name, 0x00, bytes[0])
            assertEquals(button.name, 0xFF, bytes[1])
            assertEquals(button.name, button.command, bytes[2])
            assertEquals(button.name, button.command xor 0xFF, bytes[3])
        }
    }

    @Test
    fun commandCodesMatchSpec() {
        val expected = mapOf(
            TvButton.UP to 0x01, TvButton.DOWN to 0x02, TvButton.LEFT to 0x03,
            TvButton.RIGHT to 0x04, TvButton.SELECT to 0x05, TvButton.MENU to 0x06,
            TvButton.TV_HOME to 0x07, TvButton.PLAY_PAUSE to 0x08, TvButton.VOL_UP to 0x09,
            TvButton.VOL_DOWN to 0x0A, TvButton.MUTE to 0x0B, TvButton.POWER to 0x0C,
        )
        assertEquals(expected.keys, TvButton.values().toSet())
        expected.forEach { (button, code) -> assertEquals(button.name, code, button.command) }
    }

    @Test
    fun frameDurationIsConstant67980us() {
        // Header + 16 zeros + 16 ones (address/command are sent with their inverses) + trailing mark.
        for (button in TvButton.values()) {
            assertEquals(button.name, 13_500 + 16 * 1_120 + 16 * 2_250 + 560, AppleTvIr.encodeNec(0x00, button.command).sum())
        }
    }

    @Test
    fun nonZeroAddressAndHighCommandBitsAreEncoded() {
        val bytes = bytesOf(decode(AppleTvIr.encodeNec(0xA5, 0xFE)))
        assertArrayEquals(intArrayOf(0xA5, 0x5A, 0xFE, 0x01), bytes)
    }

    @Test
    fun inputsAreMaskedToOneByte() {
        assertArrayEquals(AppleTvIr.encodeNec(0x12, 0x34), AppleTvIr.encodeNec(0x312, 0x734))
    }

    @Test
    fun allDurationsArePositive() {
        assertTrue(AppleTvIr.encodeNec(0x00, 0x0C).all { it > 0 })
    }
}
