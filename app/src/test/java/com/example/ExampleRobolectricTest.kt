package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.HardwareTelemetryEngine
import com.example.data.RedCorePreferencesRepository
import com.example.data.SystemBoosterManager
import com.example.model.AdvisorPresetMode
import com.example.model.HardwareTelemetry
import com.example.model.PerformanceMode
import com.example.model.QuickReconnectState
import com.example.model.SmartThermalMode
import com.example.model.ThermalStateLevel
import com.example.model.VoiceModPreset
import com.example.service.TriggerButtonType
import com.example.service.TriggerEventBus
import com.example.viewmodel.RedCoreTab
import com.example.viewmodel.RedCoreViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

    @Test
    fun `cloned touch buttons C1 to C4 emit remote target taps accurately`() {
        TriggerEventBus.resetShotCounters()
        val c1Button = TriggerEventBus.clonedButtonsConfig.value.buttons.first()
        TriggerEventBus.emitClonedButtonTap(c1Button, isDown = true)
        assertEquals(1, TriggerEventBus.totalClonedTaps.value)
        assertEquals(c1Button.id, TriggerEventBus.lastClonedActiveId.value)
        TriggerEventBus.emitClonedButtonTap(c1Button, isDown = false)
    }

    @Test
    fun `voice changer presets and unified start stop state work properly`() {
        assertEquals(5, VoiceModPreset.entries.size)
        TriggerEventBus.setMasterEngineRunning(true)
        assertTrue(TriggerEventBus.isMasterEngineRunning.value)
        TriggerEventBus.requestCompleteShutdown()
        assertFalse(TriggerEventBus.isMasterEngineRunning.value)
    }

    @Test
    fun `smart thermal mode adapts polling interval and load reduction realistically`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = HardwareTelemetryEngine(context)

        val coolSnap = HardwareTelemetry(batteryTempCelsius = 35.0f, systemThermalStatus = 0)
        val coolStatus = engine.evaluateSmartThermalStatus(coolSnap, SmartThermalMode.AUTO_ADAPTIVE)
        assertEquals(ThermalStateLevel.OPTIMAL, coolStatus.thermalLevel)
        assertEquals(5000L, coolStatus.effectivePollIntervalMs)
        assertFalse(coolStatus.reduceAnimationsActive)

        val hotSnap = HardwareTelemetry(batteryTempCelsius = 43.2f, systemThermalStatus = 3)
        val hotStatus = engine.evaluateSmartThermalStatus(hotSnap, SmartThermalMode.AUTO_ADAPTIVE)
        assertEquals(ThermalStateLevel.HIGH_LOAD, hotStatus.thermalLevel)
        assertEquals(15000L, hotStatus.effectivePollIntervalMs)
        assertTrue(hotStatus.reduceAnimationsActive)
        assertTrue(hotStatus.backgroundTrimActive)
        assertTrue(hotStatus.audioDspThrottled)

        val ecoStatus = engine.evaluateSmartThermalStatus(coolSnap, SmartThermalMode.ECO_STABILITY)
        assertTrue(ecoStatus.reduceAnimationsActive)
        assertTrue(ecoStatus.effectivePollIntervalMs >= 12000L)
    }

    @Test
    fun `auto settings advisor generates hardware-aware recommendations for all 3 modes`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = HardwareTelemetryEngine(context)
        val sampleHw = HardwareTelemetry(
            ramTotalMb = 8192,
            ramAvailableMb = 4200,
            cpuCores = 8,
            cpuMaxFreqMhz = 2800,
            displayRefreshRateHz = 120,
            screenWidthPx = 1080,
            screenHeightPx = 2400,
            batteryTempCelsius = 36.0f
        )

        val perfRec = engine.buildAutoSettingsRecommendation(
            telemetry = sampleHw,
            mode = AdvisorPresetMode.BEST_PERFORMANCE,
            gameTitle = "PUBG MOBILE"
        )
        assertEquals(AdvisorPresetMode.BEST_PERFORMANCE, perfRec.mode)
        assertTrue(perfRec.expectedFpsText.contains("120 FPS"))
        assertTrue(perfRec.whyChosenExplanationAr.contains("PUBG MOBILE"))

        val balRec = engine.buildAutoSettingsRecommendation(
            telemetry = sampleHw,
            mode = AdvisorPresetMode.BALANCED
        )
        assertEquals(AdvisorPresetMode.BALANCED, balRec.mode)

        val visualRec = engine.buildAutoSettingsRecommendation(
            telemetry = sampleHw,
            mode = AdvisorPresetMode.BEST_VISUAL_QUALITY
        )
        assertEquals(AdvisorPresetMode.BEST_VISUAL_QUALITY, visualRec.mode)
        assertTrue(visualRec.graphicsQualityText.contains("HDR"))
    }

    @Test
    fun `notification shield applies and restores state cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = SystemBoosterManager(context)

        val enabledState = manager.applyNotificationShield(true)
        assertTrue(enabledState.enabled)
        assertTrue(enabledState.statusLabelAr.contains("درع الإشعارات نشط"))

        val disabledState = manager.applyNotificationShield(false)
        assertFalse(disabledState.enabled)
    }

    @Test
    fun `per-game profiles catalog and quick reconnect state work properly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = SystemBoosterManager(context)
        val repo = RedCorePreferencesRepository(context)

        val catalog = manager.buildGameSpaceCatalog()
        assertTrue(catalog.size >= 6)
        val pubg = catalog.first { it.id == "pubg_mobile" }
        assertEquals(PerformanceMode.DIABLO, pubg.recommendedMode)
        assertTrue(pubg.notificationShieldEnabled)

        val merged = repo.mergeSavedProfilesWithCatalog("", catalog) { false }
        assertEquals(catalog.size, merged.size)

        val reconnectState = manager.inspectQuickReconnectState("com.nonexistent.game")
        assertEquals(QuickReconnectState.PROFILE_READY_SIMULATION, reconnectState)
    }

    @Test
    fun `viewmodel handles tabs, profile application, thermal mode, and clean shutdown`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = RedCoreViewModel(app)

        vm.setAppInForeground(true)
        vm.selectTab(RedCoreTab.GAME_SPACE)
        assertEquals(RedCoreTab.GAME_SPACE, vm.selectedTab.value)

        vm.setSmartThermalMode(SmartThermalMode.ECO_STABILITY)
        assertEquals(SmartThermalMode.ECO_STABILITY, vm.lowEndConfig.value.smartThermalMode)

        vm.selectAdvisorPresetMode(AdvisorPresetMode.BEST_PERFORMANCE)
        assertEquals(AdvisorPresetMode.BEST_PERFORMANCE, vm.autoSettingsRecommendation.value.mode)

        vm.toggleNotificationShield()
        assertTrue(vm.notificationShieldState.value.enabled)

        var exited = false
        vm.stopAllBackgroundWorkAndExit { exited = true }
        assertTrue(exited)
        assertFalse(TriggerEventBus.isMasterEngineRunning.value)
        assertFalse(vm.notificationShieldState.value.enabled)
    }

    @Test
    fun `no-mic voice demo plays ready phrases for all voice presets without speaking`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = RedCoreViewModel(app)

        vm.playReadyVoiceSampleWithoutMic(VoiceModPreset.FEMALE_NATURAL, phraseIndex = 0)
        assertEquals(VoiceModPreset.FEMALE_NATURAL, vm.lowEndConfig.value.voiceModPreset)
        assertEquals(0, vm.selectedVoiceDemoPhraseIndex.value)

        vm.playReadyVoiceSampleWithoutMic(VoiceModPreset.CYBER_ROBOT, phraseIndex = 1)
        assertEquals(VoiceModPreset.CYBER_ROBOT, vm.lowEndConfig.value.voiceModPreset)
        assertEquals(1, vm.selectedVoiceDemoPhraseIndex.value)

        vm.playReadyVoiceSampleWithoutMic(VoiceModPreset.SQUIRREL_FUN, phraseIndex = 2)
        assertEquals(VoiceModPreset.SQUIRREL_FUN, vm.lowEndConfig.value.voiceModPreset)
        assertEquals(2, vm.selectedVoiceDemoPhraseIndex.value)
    }
}

