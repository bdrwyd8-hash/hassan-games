package com.example

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.robolectric.shadows.ShadowSettings
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

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

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

    @Test
    fun gamingToolsTab_openAndClickAllTools_withoutPermissions_neverCrashesAndShowsClearMessage() {
        ShadowSettings.setCanDrawOverlays(false)

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_tab_ARSENAL_GFX").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("arsenal_gfx_screen").assertIsDisplayed()

        // 1. Cloned Touch Buttons Studio (C1-C4)
        composeTestRule.onNodeWithTag("cloned_buttons_master_switch").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("cloned_mode_lock_play_btn").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("cloned_mode_edit_btn").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("select_cloned_slot_C2").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("select_cloned_slot_C3").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("select_cloned_slot_C4").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("select_cloned_slot_C1").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("cloned_mode_chip_TURBO_HOLD").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("cloned_mode_chip_AUTO_LOCK").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("cloned_mode_chip_SINGLE_TAP").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("reset_cloned_positions_btn").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("launch_cloned_buttons_overlay_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 2. Tactical Crosshair Studio
        composeTestRule.onNodeWithTag("crosshair_master_switch").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("crosshair_style_RED_DOT").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("crosshair_style_SNIPER_CIRCLE").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("crosshair_style_CHEVRON_PRO").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("crosshair_style_TACTICAL_CROSS").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("crosshair_color_1").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("crosshair_color_2").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("crosshair_reset_offset_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 3. Auto Settings Advisor
        composeTestRule.onNodeWithTag("advisor_mode_best_performance").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("advisor_mode_best_visual_quality").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("advisor_mode_balanced").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("apply_advisor_recommendations_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 4. GFX & Touch Sampling Tuner + Floating Sidebar + FPS Counter
        composeTestRule.onNodeWithTag("gfx_res_RES_720P").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("gfx_res_RES_900P").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("gfx_res_RES_2K").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("gfx_res_RES_1080P").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("touch_rate_HZ_240").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("touch_rate_HZ_960").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("touch_rate_HZ_480").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("low_end_stabilizer_switch").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("gfx_launch_sidebar_btn").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("gfx_toggle_fps_pill_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 5. Smart Thermal Mode
        composeTestRule.onNodeWithTag("thermal_mode_eco_stability").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("thermal_mode_off").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("thermal_mode_auto_adaptive").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 6. Gaming Environment & Network
        composeTestRule.onNodeWithTag("wifi_priority_switch").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("brightness_lock_switch").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("edge_touch_reject_switch").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 7. Custom App Icon Studio (previously caused Fatal IllegalArgumentException crash!)
        composeTestRule.onNodeWithTag("custom_app_icon_studio_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("pick_custom_logo_btn").performScrollTo().assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

    @Test
    fun gamingToolsTab_navigationTransitionsAndOverlayPermissionGranted_worksCleanly() {
        ShadowSettings.setCanDrawOverlays(true)

        composeTestRule.waitForIdle()
        // Navigate across all tabs and back to Arsenal GFX multiple times
        composeTestRule.onNodeWithTag("nav_tab_ARSENAL_GFX").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("arsenal_gfx_screen").assertIsDisplayed()

        // Toggle tools On with overlay permission granted
        composeTestRule.onNodeWithTag("cloned_buttons_master_switch").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("crosshair_master_switch").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("gfx_toggle_fps_pill_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Switch to Command Center -> RAM Booster -> My Games -> back to Arsenal GFX
        composeTestRule.onNodeWithTag("nav_tab_COMMAND_CENTER").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_tab_RAM_BOOSTER").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_tab_GAME_SPACE").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_tab_ARSENAL_GFX").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("arsenal_gfx_screen").assertIsDisplayed()

        // Toggle tools Off and On again after returning
        composeTestRule.onNodeWithTag("cloned_buttons_master_switch").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("crosshair_master_switch").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("gfx_toggle_fps_pill_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    @Config(sdk = [34], qualifiers = "w800dp-h400dp-land")
    fun gamingToolsTab_landscapeAndTabletOrientation_rendersAndInteractsWithoutCrash() {
        ShadowSettings.setCanDrawOverlays(false)

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_tab_ARSENAL_GFX").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("arsenal_gfx_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("cloned_buttons_master_switch").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("crosshair_master_switch").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("apply_advisor_recommendations_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("custom_app_icon_studio_card").performScrollTo().assertIsDisplayed()
        composeTestRule.waitForIdle()
    }
}
