package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.GameSpaceProfile
import com.example.service.TriggerButtonType
import com.example.service.TriggerEventBus
import com.example.ui.components.EdgeSwipeGameGenieOverlay
import com.example.ui.components.RedCoreTopStatusBar
import com.example.ui.components.StatusToastBanner
import com.example.ui.screens.ArsenalGfxScreen
import com.example.ui.screens.CommandCenterScreen
import com.example.ui.screens.GameSpaceLobbyScreen
import com.example.ui.screens.RamBoosterScreen
import com.example.ui.screens.ShoulderTriggersScreen
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GunmetalCard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.SilverMist
import com.example.ui.theme.TitaniumWhite
import com.example.viewmodel.RedCoreTab
import com.example.viewmodel.RedCoreViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RedCoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                RedCoreGameSpaceApp(viewModel = viewModel)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (viewModel.handleInAppVolumeTrigger(TriggerButtonType.L1_VOLUME_UP, isDown = true)) {
                    return true
                }
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (viewModel.handleInAppVolumeTrigger(TriggerButtonType.R1_VOLUME_DOWN, isDown = true)) {
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (viewModel.handleInAppVolumeTrigger(TriggerButtonType.L1_VOLUME_UP, isDown = false)) {
                    return true
                }
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (viewModel.handleInAppVolumeTrigger(TriggerButtonType.R1_VOLUME_DOWN, isDown = false)) {
                    return true
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}

@Composable
fun RedCoreGameSpaceApp(viewModel: RedCoreViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val performanceMode by viewModel.performanceMode.collectAsStateWithLifecycle()
    val triggerConfig by viewModel.triggerConfig.collectAsStateWithLifecycle()
    val crosshairConfig by viewModel.crosshairConfig.collectAsStateWithLifecycle()
    val edgeSidebarConfig by viewModel.edgeSidebarConfig.collectAsStateWithLifecycle()
    val lowEndConfig by viewModel.lowEndConfig.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val backgroundApps by viewModel.backgroundApps.collectAsStateWithLifecycle()
    val gameCatalog by viewModel.gameCatalog.collectAsStateWithLifecycle()
    val activeGameProfile by viewModel.activeGameProfile.collectAsStateWithLifecycle()
    val isBoosting by viewModel.isBoosting.collectAsStateWithLifecycle()
    val boostProgress by viewModel.boostProgress.collectAsStateWithLifecycle()
    val boostStageText by viewModel.boostStageText.collectAsStateWithLifecycle()
    val lastBoostResult by viewModel.lastBoostResult.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val canDrawOverlays by viewModel.canDrawOverlays.collectAsStateWithLifecycle()

    val l1Pressed by TriggerEventBus.l1Pressed.collectAsState()
    val r1Pressed by TriggerEventBus.r1Pressed.collectAsState()
    val isInAppSidebarOpen by TriggerEventBus.isInAppSidebarOpen.collectAsState()
    val isSystemOverlayRunning by TriggerEventBus.isOverlayServiceRunning.collectAsState()

    // Ensure Back press closes the floating sidebar first, or returns to Command Center
    if (isInAppSidebarOpen) {
        BackHandler {
            viewModel.setInAppSidebarExpanded(false)
        }
    } else if (selectedTab != RedCoreTab.COMMAND_CENTER) {
        BackHandler {
            viewModel.selectTab(RedCoreTab.COMMAND_CENTER)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ObsidianBlack,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useRail) {
                    RedCoreBottomNavigationBar(
                        selectedTab = selectedTab,
                        onSelectTab = viewModel::selectTab
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (useRail) {
                        RedCoreSideNavigationRail(
                            selectedTab = selectedTab,
                            onSelectTab = viewModel::selectTab
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        RedCoreTopStatusBar(
                            telemetry = telemetry,
                            performanceMode = performanceMode,
                            triggersEnabled = triggerConfig.enabled,
                            crosshairEnabled = crosshairConfig.enabledInApp,
                            l1Pressed = l1Pressed,
                            r1Pressed = r1Pressed,
                            onQuickTriggerClick = { viewModel.selectTab(RedCoreTab.SHOULDER_TRIGGERS) },
                            onQuickCrosshairClick = {
                                viewModel.updateCrosshairConfig { it.copy(enabledInApp = !it.enabledInApp) }
                            }
                        )

                        StatusToastBanner(
                            message = statusBannerMessage,
                            onDismiss = viewModel::dismissBanner
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            when (selectedTab) {
                                RedCoreTab.COMMAND_CENTER -> {
                                    CommandCenterScreen(
                                        telemetry = telemetry,
                                        performanceMode = performanceMode,
                                        triggerConfig = triggerConfig,
                                        crosshairConfig = crosshairConfig,
                                        lowEndConfig = lowEndConfig,
                                        isBoosting = isBoosting,
                                        boostProgress = boostProgress,
                                        boostStageText = boostStageText,
                                        lastBoostResult = lastBoostResult,
                                        onSelectMode = viewModel::setPerformanceMode,
                                        onRunSuperBoost = { viewModel.runSuperBoostNow() },
                                        onToggleVolumeTriggers = { enabled ->
                                            viewModel.updateTriggerConfig { it.copy(enabled = enabled) }
                                        },
                                        onToggleAutoClean = { enabled ->
                                            viewModel.updateLowEndConfig { it.copy(autoCleanInBackground = enabled) }
                                        },
                                        onToggleCrosshair = { enabled ->
                                            viewModel.updateCrosshairConfig { it.copy(enabledInApp = enabled) }
                                        },
                                        onNavigateToTab = viewModel::selectTab,
                                        onTestNetworkPing = viewModel::testLivePingNow,
                                        onOpenEdgeSidebar = { viewModel.setInAppSidebarExpanded(true) },
                                        onEnableSystemFloatingBar = viewModel::activateOrToggleSystemFloatingSidebar,
                                        isSystemOverlayRunning = isSystemOverlayRunning
                                    )
                                }

                                RedCoreTab.SHOULDER_TRIGGERS -> {
                                    ShoulderTriggersScreen(
                                        triggerConfig = triggerConfig,
                                        crosshairConfig = crosshairConfig,
                                        onUpdateConfig = viewModel::updateTriggerConfig,
                                        onSimulateHardwareTrigger = { btn, isDown ->
                                            viewModel.handleInAppVolumeTrigger(btn, isDown)
                                        },
                                        onOpenAccessibilitySettings = viewModel::openAccessibilitySettings
                                    )
                                }

                                RedCoreTab.RAM_BOOSTER -> {
                                    RamBoosterScreen(
                                        telemetry = telemetry,
                                        lowEndConfig = lowEndConfig,
                                        backgroundApps = backgroundApps,
                                        isBoosting = isBoosting,
                                        boostProgress = boostProgress,
                                        boostStageText = boostStageText,
                                        lastBoostResult = lastBoostResult,
                                        onRunSuperBoost = { viewModel.runSuperBoostNow() },
                                        onStopSingleApp = viewModel::stopSingleBackgroundApp,
                                        onToggleWhitelist = viewModel::toggleAppWhitelist,
                                        onOpenSystemAppInfo = viewModel::openSystemAppDetails,
                                        onRefreshApps = viewModel::refreshInstalledAppsAndGames,
                                        onUpdateLowEndConfig = viewModel::updateLowEndConfig
                                    )
                                }

                                RedCoreTab.ARSENAL_GFX -> {
                                    ArsenalGfxScreen(
                                        crosshairConfig = crosshairConfig,
                                        edgeSidebarConfig = edgeSidebarConfig,
                                        lowEndConfig = lowEndConfig,
                                        canDrawSystemOverlays = canDrawOverlays,
                                        isSystemOverlayRunning = isSystemOverlayRunning,
                                        onUpdateCrosshair = viewModel::updateCrosshairConfig,
                                        onUpdateEdgeSidebar = viewModel::updateEdgeSidebarConfig,
                                        onUpdateLowEnd = viewModel::updateLowEndConfig,
                                        onOpenInAppSidebarPreview = { viewModel.setInAppSidebarExpanded(true) },
                                        onEnableSystemFloatingSidebar = viewModel::activateOrToggleSystemFloatingSidebar,
                                        onSelectAudioRadar = viewModel::selectAudioRadarPreset,
                                        onSelectVoiceMod = viewModel::selectVoiceModPreset,
                                        onOpenAccessibilityForOverlay = viewModel::openAccessibilitySettings
                                    )
                                }

                                RedCoreTab.GAME_SPACE -> {
                                    GameSpaceLobbyScreen(
                                        gameCatalog = gameCatalog,
                                        installedApps = backgroundApps,
                                        activeGameProfile = activeGameProfile,
                                        isBoosting = isBoosting,
                                        onBoostAndLaunchGame = viewModel::boostAndLaunchGame,
                                        onBoostAndLaunchInstalledApp = { app ->
                                            val dynamicProfile = GameSpaceProfile(
                                                id = app.packageName,
                                                title = app.appName,
                                                packageName = app.packageName,
                                                genreAr = "تطبيق مثبت على الجهاز",
                                                recommendedMode = performanceMode,
                                                l1ActionAr = triggerConfig.l1ActionName,
                                                r1ActionAr = triggerConfig.r1ActionName,
                                                l1X = triggerConfig.l1XRatio,
                                                l1Y = triggerConfig.l1YRatio,
                                                r1X = triggerConfig.r1XRatio,
                                                r1Y = triggerConfig.r1YRatio,
                                                targetFps = telemetry.displayRefreshRateHz,
                                                isInstalledOnDevice = true
                                            )
                                            viewModel.boostAndLaunchGame(dynamicProfile)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Floating Edge-Swipe Game Genie / Game Turbo Sidebar & Vision Filter & AFK Black Screen
                EdgeSwipeGameGenieOverlay(
                    sidebarConfig = edgeSidebarConfig,
                    isPanelExpanded = isInAppSidebarOpen,
                    onSetPanelExpanded = viewModel::setInAppSidebarExpanded,
                    telemetry = telemetry,
                    performanceMode = performanceMode,
                    triggerConfig = triggerConfig,
                    crosshairConfig = crosshairConfig,
                    lowEndConfig = lowEndConfig,
                    canDrawSystemOverlays = canDrawOverlays,
                    isSystemOverlayRunning = isSystemOverlayRunning,
                    onRunInstantBoost = { viewModel.runSuperBoostNow() },
                    onSelectPerformanceMode = viewModel::setPerformanceMode,
                    onToggleTriggers = {
                        viewModel.updateTriggerConfig { it.copy(enabled = !it.enabled) }
                    },
                    onToggleCrosshair = {
                        viewModel.updateCrosshairConfig { it.copy(enabledInApp = !it.enabledInApp) }
                    },
                    onCycleVisionFilter = viewModel::cycleVisionFilter,
                    onCycleAudioRadar = viewModel::cycleAudioRadar,
                    onToggleMistouch = {
                        viewModel.updateLowEndConfig { it.copy(mistouchPrevention = !it.mistouchPrevention) }
                    },
                    onToggleAfkBlackScreen = { afk ->
                        viewModel.updateLowEndConfig { it.copy(afkBlackScreenSaver = afk) }
                    },
                    onSwapSidebarEdge = {
                        viewModel.updateEdgeSidebarConfig { it.copy(isRightEdge = !it.isRightEdge) }
                    },
                    onActivateSystemFloatingBar = viewModel::activateOrToggleSystemFloatingSidebar
                )
            }
        }
    }
}

private fun getTabIcons(tab: RedCoreTab): Pair<ImageVector, ImageVector> {
    return when (tab) {
        RedCoreTab.COMMAND_CENTER -> Icons.Filled.Dashboard to Icons.Outlined.Dashboard
        RedCoreTab.SHOULDER_TRIGGERS -> Icons.Filled.VolumeUp to Icons.Outlined.VolumeUp
        RedCoreTab.RAM_BOOSTER -> Icons.Filled.Memory to Icons.Outlined.Memory
        RedCoreTab.ARSENAL_GFX -> Icons.Filled.GpsFixed to Icons.Outlined.GpsFixed
        RedCoreTab.GAME_SPACE -> Icons.Filled.Gamepad to Icons.Outlined.Gamepad
    }
}

@Composable
private fun RedCoreBottomNavigationBar(
    selectedTab: RedCoreTab,
    onSelectTab: (RedCoreTab) -> Unit
) {
    NavigationBar(
        containerColor = ObsidianSurface,
        contentColor = TitaniumWhite,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_navigation_bar")
    ) {
        RedCoreTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            val (filledIcon, outlinedIcon) = getTabIcons(tab)
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) filledIcon else outlinedIcon,
                        contentDescription = tab.titleAr
                    )
                },
                label = {
                    Text(
                        text = tab.titleAr,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TitaniumWhite,
                    selectedTextColor = CrimsonRed,
                    indicatorColor = CrimsonRed.copy(alpha = 0.25f),
                    unselectedIconColor = SilverMist,
                    unselectedTextColor = SilverMist
                ),
                modifier = Modifier.testTag("nav_tab_${tab.route}")
            )
        }
    }
}

@Composable
private fun RedCoreSideNavigationRail(
    selectedTab: RedCoreTab,
    onSelectTab: (RedCoreTab) -> Unit
) {
    NavigationRail(
        containerColor = ObsidianSurface,
        contentColor = TitaniumWhite,
        modifier = Modifier.fillMaxHeight()
    ) {
        RedCoreTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            val (filledIcon, outlinedIcon) = getTabIcons(tab)
            NavigationRailItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) filledIcon else outlinedIcon,
                        contentDescription = tab.titleAr
                    )
                },
                label = {
                    Text(
                        text = tab.titleAr,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = TitaniumWhite,
                    selectedTextColor = CrimsonRed,
                    indicatorColor = GunmetalCard,
                    unselectedIconColor = SilverMist,
                    unselectedTextColor = SilverMist
                ),
                modifier = Modifier.testTag("rail_tab_${tab.route}")
            )
        }
    }
}
