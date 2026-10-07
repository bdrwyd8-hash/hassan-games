package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.HardwareTelemetryEngine
import com.example.data.RedCorePreferencesRepository
import com.example.data.SystemBoosterManager
import com.example.model.AdvisorPresetMode
import com.example.model.ClonedButtonMode
import com.example.model.CrosshairStyle
import com.example.model.HardwareTelemetry
import com.example.model.InstalledAppCandidate
import com.example.model.PerformanceMode
import com.example.model.SmartThermalMode
import com.example.model.ThermalStateLevel
import com.example.service.TriggerEventBus
import com.example.viewmodel.RedCoreTab
import com.example.viewmodel.RedCoreViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var app: Application
    private lateinit var prefsRepo: RedCorePreferencesRepository
    private lateinit var telemetryEngine: HardwareTelemetryEngine
    private lateinit var boosterManager: SystemBoosterManager

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        prefsRepo = RedCorePreferencesRepository(app)
        telemetryEngine = HardwareTelemetryEngine(app)
        boosterManager = SystemBoosterManager(app)
        TriggerEventBus.resetShotCounters()
        TriggerEventBus.setMasterEngineRunning(true)
    }

    @Test
    fun universalGamingCenter_addUpdateAndRemoveGames_persistsCleanly() {
        val viewModel = RedCoreViewModel(app)

        // Add an installed candidate game to MY GAMES
        val candidate = InstalledAppCandidate(
            packageName = "com.example.testgame.arena",
            title = "Test Arena Pro",
            genreAr = "أكشن وتنافس • eSports",
            isLikelyGame = true,
            accentHex = 0xFF00E5FFL,
            recommendedMode = PerformanceMode.DIABLO,
            recommendedFps = 120
        )
        viewModel.addInstalledCandidateToMyGames(candidate)

        val gamesAfterAdd = viewModel.gameSpaceCatalog.value
        val addedGame = gamesAfterAdd.firstOrNull { it.packageName == "com.example.testgame.arena" }
        assertNotNull("Added candidate game must exist in MY GAMES", addedGame)
        assertEquals("Test Arena Pro", addedGame!!.title)
        assertEquals(PerformanceMode.DIABLO, addedGame.recommendedMode)

        // Update per-game profile settings
        val updatedProfile = addedGame.copy(
            recommendedMode = PerformanceMode.BALANCED,
            recommendedFps = 60,
            crosshairEnabled = true,
            sidebarOverlayEnabled = true,
            fpsOverlayEnabled = true
        )
        viewModel.updateGameProfile(updatedProfile)

        val savedAfterUpdate = viewModel.gameSpaceCatalog.value.first { it.id == addedGame.id }
        assertEquals(PerformanceMode.BALANCED, savedAfterUpdate.recommendedMode)
        assertTrue(savedAfterUpdate.crosshairEnabled)
        assertTrue(savedAfterUpdate.sidebarOverlayEnabled)

        // Add a custom game manually
        viewModel.addCustomGameToMyGames(
            title = "Custom Racing X",
            packageName = "com.custom.racingx",
            genreAr = "سباقات • Racing",
            recommendedMode = PerformanceMode.PERFORMANCE,
            recommendedFps = 90
        )
        assertTrue(
            viewModel.gameSpaceCatalog.value.any { it.packageName == "com.custom.racingx" }
        )

        // Remove game from MY GAMES
        viewModel.removeGameFromMyGames(addedGame.id)
        assertFalse(
            viewModel.gameSpaceCatalog.value.any { it.id == addedGame.id }
        )
    }

    @Test
    fun startGameWorkflow_appliesProfileAndRecordsSession() {
        val viewModel = RedCoreViewModel(app)
        viewModel.addCustomGameToMyGames(
            title = "Cyber Strike 2099",
            packageName = "com.cyber.strike2099",
            genreAr = "تصويب تكتيكي",
            recommendedMode = PerformanceMode.DIABLO,
            recommendedFps = 120
        )

        val profile = viewModel.gameSpaceCatalog.value.first {
            it.packageName == "com.cyber.strike2099"
        }.copy(
            crosshairEnabled = true,
            fpsOverlayEnabled = true,
            sidebarOverlayEnabled = true,
            brightnessLockEnabled = true
        )
        viewModel.updateGameProfile(profile)

        // Trigger START workflow
        viewModel.applyProfileWithoutLaunching(profile)

        assertEquals(PerformanceMode.DIABLO, viewModel.performanceMode.value)
        assertTrue(viewModel.crosshairConfig.value.enabled)
        assertTrue(viewModel.fpsHudOverlayEnabled.value)
        assertTrue(viewModel.lowEndConfig.value.brightnessLockEnabled)
        assertEquals("Cyber Strike 2099", TriggerEventBus.activeGameTitle.value)
    }

    @Test
    fun smartThermalAndAdvisorEngine_evaluatesAccurately() {
        val hotTelemetry = HardwareTelemetry(
            liveFps = 58,
            displayRefreshRateHz = 120,
            ramUsedMb = 6200,
            ramTotalMb = 8192,
            ramAvailableMb = 1992,
            ramUsagePercent = 84,
            cpuLoadPercent = 76,
            batteryTempCelsius = 43.0f,
            isCharging = true,
            pingMs = 24
        )

        val status = telemetryEngine.evaluateSmartThermalStatus(
            telemetry = hotTelemetry,
            mode = SmartThermalMode.AUTO_ADAPTIVE
        )
        assertEquals(ThermalStateLevel.HIGH_LOAD, status.thermalLevel)
        assertTrue(status.reduceAnimationsActive)
        assertTrue(status.backgroundTrimActive)
        assertTrue(status.effectivePollIntervalMs >= 10000L)

        val recommendation = telemetryEngine.buildAutoSettingsRecommendation(
            telemetry = hotTelemetry,
            mode = AdvisorPresetMode.BEST_PERFORMANCE,
            gameTitle = "Any Universal Game"
        )
        assertTrue(recommendation.expectedFpsText.isNotBlank())
        assertTrue(recommendation.whyChosenExplanationAr.contains("Any Universal Game"))
    }

    @Test
    fun clonedTouchButtonsAndCompleteShutdown_operateCleanly() {
        val viewModel = RedCoreViewModel(app)
        viewModel.selectTab(RedCoreTab.ARSENAL_GFX)
        assertEquals(RedCoreTab.ARSENAL_GFX, viewModel.selectedTab.value)

        // Enable On-Screen Cloned Buttons (C1-C4)
        viewModel.toggleClonedButtonsMaster(true)
        assertTrue(viewModel.clonedButtonsConfig.value.enabled)

        viewModel.setClonedButtonMode(1, ClonedButtonMode.TURBO_HOLD)
        val c1 = viewModel.clonedButtonsConfig.value.buttons.first { it.id == 1 }
        assertEquals(ClonedButtonMode.TURBO_HOLD, c1.mode)

        viewModel.emitClonedTap(c1, isDown = true)
        assertEquals(1, TriggerEventBus.totalClonedTaps.value)
        viewModel.emitClonedTap(c1, isDown = false)

        // Update Crosshair Studio
        viewModel.toggleCrosshair(true)
        viewModel.selectCrosshairStyle(CrosshairStyle.SNIPER_CIRCLE)
        assertEquals(CrosshairStyle.SNIPER_CIRCLE, viewModel.crosshairConfig.value.style)

        // Execute Complete Shutdown & Exit
        var exited = false
        viewModel.stopAllBackgroundWorkAndExit {
            exited = true
        }
        assertTrue(exited)
        assertFalse(TriggerEventBus.isMasterEngineRunning.value)
        assertFalse(viewModel.clonedButtonsConfig.value.enabled)
        assertFalse(viewModel.crosshairConfig.value.enabled)
    }
}
