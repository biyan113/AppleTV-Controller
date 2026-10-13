package com.example.appletvremote.ui

import com.example.appletvremote.TvButton
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClickPadZoneTest {
    private val size = 300f
    private val selectRadius = 60f
    private fun at(x: Float, y: Float) = padZoneAt(x, y, size, size, selectRadius)

    @Test
    fun centreIsSelect() {
        assertEquals(TvButton.SELECT, at(150f, 150f))
        assertEquals(TvButton.SELECT, at(150f + selectRadius - 1f, 150f))
        assertEquals(TvButton.RIGHT, at(150f + selectRadius + 1f, 150f))
    }

    @Test
    fun ringMapsToDirections() {
        assertEquals(TvButton.UP, at(150f, 20f))
        assertEquals(TvButton.DOWN, at(150f, 280f))
        assertEquals(TvButton.LEFT, at(20f, 150f))
        assertEquals(TvButton.RIGHT, at(280f, 150f))
        // Off-axis but still within the up wedge
        assertEquals(TvButton.UP, at(190f, 30f))
    }

    @Test
    fun cornersOutsideCircleAreIgnored() {
        assertNull(at(5f, 5f))
        assertNull(at(295f, 295f))
    }
}
