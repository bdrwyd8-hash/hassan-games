package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ui.components.EdgeSwipeGameGenieOverlay
import com.example.ui.components.RedCoreTopStatusBar
import com.example.ui.screens.ArsenalGfxScreen
import com.example.ui.screens.CommandCenterScreen
import com.example.ui.screens.GameSpaceLobbyScreen
import com.example.ui.screens.RamBoosterScreen
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GunmetalCard
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.OrbitronFontFamily
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
                HassanGamesRootApp(
                    viewModel = viewModel,
                    onExitApp = {
                        try {
                            finishAndRemoveTask()
                        } catch (_: Exception) {
                            finishAffinity()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HassanGamesRootApp(
    viewModel: RedCoreViewModel,
    onExitApp: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.setAppInForeground(true)
                Lifecycle.Event.ON_STOP -> viewModel.setAppInForeground(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val selectedTab by viewModel.selectedTab.collectAsState()
    val performanceMode by viewModel.performanceMode.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val smartThermalStatus by viewModel.smartThermalStatus.collectAsState()
    val advisorPresetMode by viewModel.advisorPresetMode.collectAsState()
    val autoSettingsRecommendation by viewModel.autoSettingsRecommendation.collectAsState()
    val clonedButtonsConfig by viewModel.clonedButtonsConfig.collectAsState()
    val crosshairConfig by viewModel.crosshairConfig.collectAsState()
    val lowEndConfig by viewModel.lowEndConfig.collectAsState()
    val gfxResolution by viewModel.gfxResolution.collectAsState()
    val touchSamplingRate by viewModel.touchSamplingRate.collectAsState()
    val optimizableProcesses by viewModel.optimizableProcesses.collectAsState()
    val isBoosting by viewModel.isBoosting.collectAsState()
    val lastBoostReport by viewModel.lastBoostReport.collectAsState()
    val isPreparingGame by viewModel.isPreparingGame.collectAsState()
    val lastGamePrepReport by viewModel.lastGamePrepReport.collectAsState()
    val games by viewModel.gameSpaceCatalog.collectAsState()
    val installedCandidates by viewModel.installedAppCandidates.collectAsState()
    val showAddGameSheet by viewModel.showAddGameSheet.collectAsState()
    val lastSessionInfo by viewModel.lastSessionGameInfo.collectAsState()
    val notificationShieldState by viewModel.notificationShieldState.collectAsState()
    val fpsHudOverlayEnabled by viewModel.fpsHudOverlayEnabled.collectAsState()
    val edgeGenieExpanded by viewModel.edgeGenieExpanded.collectAsState()
    val overlayPermissionGranted by viewModel.overlayPermissionGranted.collectAsState()
    val accessibilityServiceEnabled by viewModel.accessibilityServiceEnabled.collectAsState()
    val floatingOverlayRunning by viewModel.floatingOverlayRunning.collectAsState()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsState()
    val isMasterEngineRunning by viewModel.isMasterEngineRunning.collectAsState()

    var selectedGameId by remember(games) {
        mutableStateOf(games.firstOrNull()?.id ?: "")
    }
    if (selectedGameId.isBlank() && games.isNotEmpty()) {
        selectedGameId = games.first().id
    }

    val fpsHistory = remember(telemetry.liveFps) {
        val base = telemetry.liveFps
        List(18) { idx -> (base + ((idx % 3) - 1)).coerceIn(24, 144) }
    }

    // Back navigation handling for secondary tabs and expanded side panel
    BackHandler(enabled = edgeGenieExpanded || selectedTab != RedCoreTab.GAME_SPACE) {
        if (edgeGenieExpanded) {
            viewModel.setEdgeGenieExpanded(false)
        } else {
            viewModel.selectTab(RedCoreTab.GAME_SPACE)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ObsidianBlack,
            contentWindowInsets = WindowInsets.systemBars,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    RedCoreTopStatusBar(
                        telemetry = telemetry,
                        performanceMode = performanceMode,
                        crosshairEnabled = crosshairConfig.enabled,
                        isMasterRunning = isMasterEngineRunning,
                        onQuickAddGameClick = {
                            viewModel.selectTab(RedCoreTab.GAME_SPACE)
                            viewModel.openAddGameSheet()
                        },
                        onQuickCrosshairClick = {
                            viewModel.toggleCrosshair(!crosshairConfig.enabled)
                        },
                        onQuickStartOrStopClick = {
                            viewModel.toggleMasterEngineOrExit(onExitRequested = onExitApp)
                        }
                    )

                    AnimatedVisibility(
                        visible = statusBannerMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        statusBannerMessage?.let { msg ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .testTag("status_toast_banner"),
                                shape = RoundedCornerShape(10.dp),
                                color = GunmetalCard,
                                border = BorderStroke(1.dp, MatrixGreen)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MatrixGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = msg,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TitaniumWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.dismissStatusBanner() },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "إغلاق التنبيه",
                                            tint = SilverMist,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                HassanGamingBottomBar(
                    selectedTab = selectedTab,
                    accentColor = performanceMode.primaryColor,
                    myGamesCount = games.size,
                    onSelectTab = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    RedCoreTab.GAME_SPACE -> {
                        GameSpaceLobbyScreen(
                            games = games,
                            selectedGameId = selectedGameId,
                            installedCandidates = installedCandidates,
                            showAddGamePicker = showAddGameSheet,
                            lastSessionInfo = lastSessionInfo,
                            isPreparingGame = isPreparingGame,
                            lastGamePrepReport = lastGamePrepReport,
                            notificationShieldState = notificationShieldState,
                            onSelectGame = { selectedGameId = it },
                            onLaunchGame = { profile ->
                                selectedGameId = profile.id
                                viewModel.launchOrSimulateGame(profile)
                            },
                            onUpdateGameProfile = { gameId, transform ->
                                val existing = games.find { it.id == gameId }
                                if (existing != null) {
                                    viewModel.updateGameProfile(transform(existing))
                                }
                            },
                            onRemoveGame = { gameId ->
                                viewModel.removeGameFromMyGames(gameId)
                            },
                            onOpenAddGamePicker = { viewModel.openAddGameSheet() },
                            onCloseAddGamePicker = { viewModel.closeAddGameSheet() },
                            onRefreshInstalledApps = { viewModel.refreshSystemPermissionsAndLists() },
                            onAddInstalledCandidate = { candidate ->
                                viewModel.addInstalledCandidateToMyGames(candidate)
                            },
                            onAddCustomGame = { title, pkg, genre, mode ->
                                viewModel.addCustomGameToMyGames(
                                    title = title,
                                    packageName = pkg,
                                    genreAr = genre,
                                    recommendedMode = mode,
                                    recommendedFps = mode.targetFps
                                )
                            },
                            onRunOneTapGamePreparation = { launchAfter ->
                                val selectedProfile = games.find { it.id == selectedGameId } ?: games.firstOrNull()
                                if (launchAfter && selectedProfile != null) {
                                    viewModel.launchOrSimulateGame(selectedProfile)
                                } else {
                                    viewModel.executeOneTapGamePreparation(selectedProfile)
                                }
                            },
                            onQuickReconnectLastGame = { viewModel.quickReconnectLastGame() },
                            onClearLastSessionRecord = { viewModel.clearLastSessionRecord() },
                            onToggleNotificationShield = { viewModel.toggleNotificationShield() },
                            onOpenDndPermissionSettings = { viewModel.openDndPermissionSettings() },
                            onLoadAppIcon = { pkg -> viewModel.getAppIconBitmap(pkg) }
                        )
                    }

                    RedCoreTab.COMMAND_CENTER -> {
                        CommandCenterScreen(
                            telemetry = telemetry,
                            fpsHistory = fpsHistory,
                            performanceMode = performanceMode,
                            games = games,
                            selectedGameId = selectedGameId,
                            crosshairConfig = crosshairConfig,
                            clonedButtonsConfig = clonedButtonsConfig,
                            lowEndConfig = lowEndConfig,
                            fpsPillVisible = fpsHudOverlayEnabled,
                            isBoosting = isBoosting,
                            isAccessibilityRunning = accessibilityServiceEnabled,
                            canDrawOverlays = overlayPermissionGranted,
                            isSystemFloatingBarRunning = floatingOverlayRunning,
                            isPreparingGame = isPreparingGame,
                            lastGamePrepReport = lastGamePrepReport,
                            smartThermalStatus = smartThermalStatus,
                            advisorPresetMode = advisorPresetMode,
                            autoSettingsRecommendation = autoSettingsRecommendation,
                            lastSessionInfo = lastSessionInfo,
                            notificationShieldState = notificationShieldState,
                            onSelectPerformanceMode = { viewModel.selectPerformanceMode(it) },
                            onQuickBoostRam = { viewModel.executeSuperBoost() },
                            onQuickLaunchSelectedGame = {
                                val target = games.find { it.id == selectedGameId } ?: games.firstOrNull()
                                if (target != null) {
                                    viewModel.launchOrSimulateGame(target)
                                }
                            },
                            onOpenMyGamesTab = { viewModel.selectTab(RedCoreTab.GAME_SPACE) },
                            onToggleFpsPill = { viewModel.toggleFpsHudOverlay(it) },
                            onToggleCrosshairOverlay = { viewModel.toggleCrosshair(it) },
                            onToggleClonedButtonsOverlay = {
                                viewModel.toggleClonedButtonsMaster(!clonedButtonsConfig.enabled)
                            },
                            onLaunchSystemFloatingSidebar = {
                                if (floatingOverlayRunning) {
                                    viewModel.stopFloatingSidebarOverlay()
                                } else if (overlayPermissionGranted) {
                                    viewModel.startFloatingSidebarOverlay()
                                } else {
                                    viewModel.openOverlayPermissionSettings()
                                }
                            },
                            onRunOneTapGamePreparation = { launchAfter ->
                                val selectedProfile = games.find { it.id == selectedGameId } ?: games.firstOrNull()
                                if (launchAfter && selectedProfile != null) {
                                    viewModel.launchOrSimulateGame(selectedProfile)
                                } else {
                                    viewModel.executeOneTapGamePreparation(selectedProfile)
                                }
                            },
                            onSelectSmartThermalMode = { viewModel.setSmartThermalMode(it) },
                            onSelectAdvisorPreset = { viewModel.selectAdvisorPresetMode(it) },
                            onApplyAdvisorPreset = { viewModel.applyAdvisorRecommendationToEngine() },
                            onQuickReconnectLastGame = { viewModel.quickReconnectLastGame() },
                            onClearLastSessionRecord = { viewModel.clearLastSessionRecord() },
                            onToggleNotificationShield = { viewModel.toggleNotificationShield() },
                            onOpenDndPermissionSettings = { viewModel.openDndPermissionSettings() },
                            onShutdownAndExitApp = {
                                viewModel.stopAllBackgroundWorkAndExit(onExitRequested = onExitApp)
                            }
                        )
                    }

                    RedCoreTab.RAM_BOOSTER -> {
                        RamBoosterScreen(
                            telemetry = telemetry,
                            processes = optimizableProcesses,
                            isBoosting = isBoosting,
                            lastBoostReport = lastBoostReport,
                            onExecuteDeepBoost = { viewModel.executeSuperBoost() },
                            onToggleProcessWhitelist = { viewModel.toggleProcessWhitelist(it) },
                            onTestNetworkPing = { viewModel.testNetworkPingNow() }
                        )
                    }

                    RedCoreTab.ARSENAL_GFX -> {
                        ArsenalGfxScreen(
                            crosshairConfig = crosshairConfig,
                            clonedButtonsConfig = clonedButtonsConfig,
                            gfxResolution = gfxResolution,
                            touchSamplingRate = touchSamplingRate,
                            lowEndConfig = lowEndConfig,
                            telemetry = telemetry,
                            smartThermalStatus = smartThermalStatus,
                            advisorPresetMode = advisorPresetMode,
                            autoSettingsRecommendation = autoSettingsRecommendation,
                            canDrawOverlays = overlayPermissionGranted,
                            isAccessibilityRunning = accessibilityServiceEnabled,
                            isSystemFloatingBarRunning = floatingOverlayRunning,
                            fpsPillVisible = fpsHudOverlayEnabled,
                            onUpdateCrosshair = { viewModel.updateCrosshairTransform(it) },
                            onUpdateClonedButtons = { viewModel.updateClonedButtonsTransform(it) },
                            onToggleClonedButtonsOverlay = {
                                viewModel.toggleClonedButtonsMaster(!clonedButtonsConfig.enabled)
                            },
                            onSimulateClonedTap = { btn, isDown ->
                                viewModel.emitClonedTap(btn, isDown)
                            },
                            onOpenAccessibilitySettings = { viewModel.openAccessibilitySettings() },
                            onSelectResolution = { viewModel.selectGfxResolution(it) },
                            onSelectTouchRate = { viewModel.selectTouchSamplingRate(it) },
                            onUpdateLowEnd = { viewModel.updateLowEndTransform(it) },
                            onSelectSmartThermalMode = { viewModel.setSmartThermalMode(it) },
                            onSelectAdvisorPreset = { viewModel.selectAdvisorPresetMode(it) },
                            onApplyAdvisorPreset = { viewModel.applyAdvisorRecommendationToEngine() },
                            onToggleFpsPill = { viewModel.toggleFpsHudOverlay(it) },
                            onLaunchSystemFloatingSidebar = {
                                viewModel.toggleFloatingSidebarOrInApp()
                            },
                            onOpenOverlayPermissionSettings = { viewModel.openOverlayPermissionSettings() },
                            onTestNetworkPing = { viewModel.testNetworkPingNow() },
                            onShowMessage = { viewModel.showStatusMessage(it) }
                        )
                    }
                }
            }
        }

        // In-App Edge Swipe Game Genie Overlay & Dock
        EdgeSwipeGameGenieOverlay(
            sidebarEnabledInApp = true,
            isPanelExpanded = edgeGenieExpanded,
            onSetPanelExpanded = { viewModel.setEdgeGenieExpanded(it) },
            telemetry = telemetry,
            performanceMode = performanceMode,
            crosshairConfig = crosshairConfig,
            lowEndConfig = lowEndConfig,
            clonedButtonsConfig = clonedButtonsConfig,
            fpsPillVisible = fpsHudOverlayEnabled,
            notificationShieldActive = notificationShieldState.enabled,
            canDrawSystemOverlays = overlayPermissionGranted,
            isSystemOverlayRunning = floatingOverlayRunning,
            onRunInstantBoost = { viewModel.executeSuperBoost() },
            onSelectPerformanceMode = { viewModel.selectPerformanceMode(it) },
            onToggleCrosshair = { viewModel.toggleCrosshair(!crosshairConfig.enabled) },
            onToggleFpsPill = { viewModel.toggleFpsHudOverlay(!fpsHudOverlayEnabled) },
            onToggleNotificationShield = { viewModel.toggleNotificationShield() },
            onToggleMistouch = { viewModel.toggleTouchEdgeReject(!lowEndConfig.touchEdgeRejectEnabled) },
            onToggleBrightnessLock = { viewModel.toggleBrightnessLock(!lowEndConfig.brightnessLockEnabled) },
            onToggleClonedButtonsOverlay = {
                viewModel.toggleClonedButtonsMaster(!clonedButtonsConfig.enabled)
            },
            onToggleClonedButtonsLock = { viewModel.toggleClonedPositionsLocked() },
            onActivateSystemFloatingBar = {
                if (floatingOverlayRunning) {
                    viewModel.stopFloatingSidebarOverlay()
                    viewModel.showStatusMessage("تم إيقاف الشريط العائم الخارجي")
                } else if (overlayPermissionGranted) {
                    viewModel.startFloatingSidebarOverlay()
                    viewModel.showStatusMessage("تم تفعيل الشريط العائم فوق جميع الألعاب!")
                } else {
                    viewModel.openOverlayPermissionSettings()
                }
            },
            onRunCoolDown = {
                viewModel.setSmartThermalMode(com.example.model.SmartThermalMode.ECO_STABILITY)
            },
            onShutdownAndExitApp = {
                viewModel.stopAllBackgroundWorkAndExit(onExitRequested = onExitApp)
            }
        )
    }
}

@Composable
private fun HassanGamingBottomBar(
    selectedTab: RedCoreTab,
    accentColor: Color,
    myGamesCount: Int,
    onSelectTab: (RedCoreTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("bottom_navigation_bar"),
        color = ObsidianSurface,
        border = BorderStroke(1.dp, CarbonBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavTabItem(
                tab = RedCoreTab.GAME_SPACE,
                icon = Icons.Default.SportsEsports,
                label = "MY GAMES ($myGamesCount)",
                selected = selectedTab == RedCoreTab.GAME_SPACE,
                accentColor = accentColor,
                testTag = "nav_tab_GAME_SPACE",
                onClick = { onSelectTab(RedCoreTab.GAME_SPACE) }
            )
            BottomNavTabItem(
                tab = RedCoreTab.COMMAND_CENTER,
                icon = Icons.Default.Speed,
                label = "مركز القيادة",
                selected = selectedTab == RedCoreTab.COMMAND_CENTER,
                accentColor = accentColor,
                testTag = "nav_tab_COMMAND_CENTER",
                onClick = { onSelectTab(RedCoreTab.COMMAND_CENTER) }
            )
            BottomNavTabItem(
                tab = RedCoreTab.ARSENAL_GFX,
                icon = Icons.Default.Tune,
                label = "أدوات اللعب",
                selected = selectedTab == RedCoreTab.ARSENAL_GFX,
                accentColor = accentColor,
                testTag = "nav_tab_ARSENAL_GFX",
                onClick = { onSelectTab(RedCoreTab.ARSENAL_GFX) }
            )
            BottomNavTabItem(
                tab = RedCoreTab.RAM_BOOSTER,
                icon = Icons.Default.Memory,
                label = "الرام والشبكة",
                selected = selectedTab == RedCoreTab.RAM_BOOSTER,
                accentColor = accentColor,
                testTag = "nav_tab_RAM_BOOSTER",
                onClick = { onSelectTab(RedCoreTab.RAM_BOOSTER) }
            )
        }
    }
}

@Composable
private fun BottomNavTabItem(
    tab: RedCoreTab,
    icon: ImageVector,
    label: String,
    selected: Boolean,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) accentColor.copy(alpha = 0.16f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tab.titleAr,
            tint = if (selected) accentColor else SilverMist,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (selected) TitaniumWhite else SilverMist
        )
    }
}
