package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.service.TriggerButtonType
import com.example.service.TriggerEventBus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Hassan Games", appName)
    }

    @Test
    fun `volume trigger L1 and R1 events increment shot counters`() {
        TriggerEventBus.resetShotCounters()
        TriggerEventBus.onTriggerKeyStateChanged(TriggerButtonType.L1_VOLUME_UP, isDown = true)
        assertTrue(TriggerEventBus.l1Pressed.value)
        assertEquals(1, TriggerEventBus.totalL1Shots.value)
        TriggerEventBus.onTriggerKeyStateChanged(TriggerButtonType.L1_VOLUME_UP, isDown = false)

        TriggerEventBus.onTriggerKeyStateChanged(TriggerButtonType.R1_VOLUME_DOWN, isDown = true)
        assertTrue(TriggerEventBus.r1Pressed.value)
        assertEquals(1, TriggerEventBus.totalR1Shots.value)
        TriggerEventBus.onTriggerKeyStateChanged(TriggerButtonType.R1_VOLUME_DOWN, isDown = false)
    }
}
