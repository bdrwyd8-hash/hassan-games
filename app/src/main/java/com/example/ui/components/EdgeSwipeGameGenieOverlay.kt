package com.example.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClonedButtonsConfig
import com.example.model.CrosshairConfig
import com.example.model.GameSpaceProfile
import com.example.model.HardwareTelemetry
import com.example.model.HudShortcutTool
import com.example.model.LowEndDeviceConfig
import com.example.model.NetworkCheckReport
import com.example.model.PerformanceMode
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GunmetalCard
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MoltenAmber
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.SilverMist
import com.example.ui.theme.TitaniumWhite
import java.util.Locale

@Composable
fun EdgeSwipeGameGenieOverlay(
    sidebarEnabledInApp: Boolean,
    isPanelExpanded: Boolean,
    onSetPanelExpanded: (Boolean) -> Unit,
    telemetry: HardwareTelemetry,
    performanceMode: PerformanceMode,
    crosshairConfig: CrosshairConfig,
    lowEndConfig: LowEndDeviceConfig,
    clonedButtonsConfig: ClonedButtonsConfig,
    fpsPillVisible: Boolean,
    notificationShieldActive: Boolean,
    canDrawSystemOverlays: Boolean,
    isSystemOverlayRunning: Boolean,
    games: List<GameSpaceProfile> = emptyList(),
    latestNetworkReport: NetworkCheckReport? = null,
    isCheckingNetwork: Boolean = false,
    visibleHudTools: Set<HudShortcutTool> = HudShortcutTool.entries.toSet(),
    activeSessionGamePackage: String? = null,
    onRunInstantBoost: () -> Unit,
    onQuickNetworkCheck: () -> Unit = {},
    onSelectPerformanceMode: (PerformanceMode) -> Unit,
    onToggleCrosshair: () -> Unit,
    onToggleFpsPill: () -> Unit,
    onToggleNotificationShield: () -> Unit,
    onToggleMistouch: () -> Unit = {},
    onToggleBrightnessLock: () -> Unit = {},
    onToggleClonedButtonsOverlay: () -> Unit = {},
    onToggleClonedButtonsLock: () -> Unit = {},
    onToggleHudToolVisibility: (HudShortcutTool) -> Unit = {},
    onOpenAiAdvisor: () -> Unit = {},
    onEndGamingSession: () -> Unit = {},
    onLaunchGame: (GameSpaceProfile) -> Unit = {},
    onActivateSystemFloatingBar: () -> Unit = {},
    onRunCoolDown: () -> Unit = {},
    onShutdownAndExitApp: () -> Unit = {}
) {
    if (!sidebarEnabledInApp) return

    Box(modifier = Modifier.fillMaxSize()) {
        TopFpsPillOverlay(
            visible = fpsPillVisible,
            telemetry = telemetry,
            performanceMode = performanceMode,
            latestNetworkReport = latestNetworkReport,
            onClose = onToggleFpsPill,
            onQuickNetworkCheck = onQuickNetworkCheck,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
        )

        EdgeSwipeGameGenieHandle(
            isOpen = isPanelExpanded,
            activeMode = performanceMode,
            onOpenDrawer = { onSetPanelExpanded(true) },
            modifier = Modifier.align(Alignment.CenterEnd)
        )

        EdgeSwipeGameGenieDrawer(
            isOpen = isPanelExpanded,
            telemetry = telemetry,
            performanceMode = performanceMode,
            crosshairEnabled = crosshairConfig.enabled,
            fpsPillEnabled = fpsPillVisible,
            bypassChargingEnabled = lowEndConfig.brightnessLockEnabled,
            notificationShieldEnabled = notificationShieldActive,
            games = games,
            latestNetworkReport = latestNetworkReport,
            isCheckingNetwork = isCheckingNetwork,
            visibleHudTools = visibleHudTools,
            activeSessionGamePackage = activeSessionGamePackage,
            onClose = { onSetPanelExpanded(false) },
            onSelectMode = onSelectPerformanceMode,
            onQuickRamPurge = onRunInstantBoost,
            onQuickNetworkCheck = onQuickNetworkCheck,
            onToggleCrosshair = onToggleCrosshair,
            onToggleFpsPill = onToggleFpsPill,
            onToggleBypassCharging = onToggleBrightnessLock,
            onToggleNotificationShield = onToggleNotificationShield,
            onToggleHudToolVisibility = onToggleHudToolVisibility,
            onOpenAiAdvisor = onOpenAiAdvisor,
            onEndGamingSession = onEndGamingSession,
            onLaunchGame = onLaunchGame
        )
    }
}

@Composable
fun EdgeSwipeGameGenieHandle(
    isOpen: Boolean,
    activeMode: PerformanceMode,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOpen) return

    Box(
        modifier = modifier
            .width(24.dp)
            .height(118.dp)
            .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        activeMode.primaryColor,
                        CrimsonRed.copy(alpha = 0.9f),
                        ObsidianSurface
                    )
                )
            )
            .border(
                width = 1.5.dp,
                color = TitaniumWhite.copy(alpha = 0.6f),
                shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -8f) {
                        onOpenDrawer()
                    }
                }
            }
            .clickable { onOpenDrawer() }
            .testTag("edge_game_genie_handle"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "اسحب لفتح شريط أدوات اللعب",
                tint = TitaniumWhite,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(34.dp)
                    .clip(CircleShape)
                    .background(TitaniumWhite.copy(alpha = 0.9f))
            )
        }
    }
}

@Composable
fun TopFpsPillOverlay(
    visible: Boolean,
    telemetry: HardwareTelemetry,
    performanceMode: PerformanceMode = PerformanceMode.PERFORMANCE,
    latestNetworkReport: NetworkCheckReport? = null,
    onClose: () -> Unit,
    onQuickNetworkCheck: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        val pingValue = latestNetworkReport?.avgPingMs ?: telemetry.pingMs
        val pingColor = when {
            pingValue <= 0 -> SilverMist
            pingValue < 55 -> MatrixGreen
            pingValue < 100 -> MoltenAmber
            else -> CrimsonRed
        }
        val netLabel = latestNetworkReport?.connectionType?.labelEn ?: "NET"

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ObsidianBlack.copy(alpha = 0.90f),
            border = BorderStroke(1.2.dp, CyberCyan.copy(alpha = 0.8f)),
            modifier = Modifier
                .widthIn(max = 360.dp)
                .testTag("floating_fps_pill_overlay")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(pingColor)
                    )
                    Text(
                        text = if (pingValue > 0) "${pingValue}ms ($netLabel)" else "بدون اتصال",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = pingColor
                    )
                    Text(
                        text = "•",
                        color = SilverMist,
                        fontSize = 10.sp
                    )
                    Text(
                        text = String.format(Locale.US, "BAT %.1f°C", telemetry.batteryTempCelsius),
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        color = if (telemetry.batteryTempCelsius > 42f) CrimsonRed else TitaniumWhite
                    )
                    Text(
                        text = "•",
                        color = SilverMist,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "RAM ${telemetry.ramUsagePercent}%",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        color = CyberCyan
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "توسيع معلومات HUD",
                        tint = TitaniumWhite,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { expanded = !expanded }
                            .testTag("hud_pill_expand_toggle")
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق شريط القياس",
                        tint = SilverMist,
                        modifier = Modifier
                            .size(15.dp)
                            .clickable { onClose() }
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HorizontalDivider(color = CarbonBorder)
                        Text(
                            text = "المستشعر: حرارة البطارية الفعلية • البطارية: ${telemetry.batteryPercent}% ${if (telemetry.isCharging) "(شحن)" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "الذاكرة المتاحة: ${telemetry.ramAvailableMb}MB من ${telemetry.ramTotalMb}MB • وضع: ${performanceMode.titleAr}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "ملاحظة تقنية: لا يعرض النظام رقم FPS وهمي؛ الألعاب المستقلة لا تسمح بقراءة SurfaceFlinger بدون صلاحيات نظام خاصة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MoltenAmber,
                            fontSize = 9.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "فحص الشبكة الآن ⚡",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onQuickNetworkCheck() }
                                    .padding(vertical = 2.dp, horizontal = 4.dp)
                                    .testTag("hud_pill_quick_net_check")
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EdgeSwipeGameGenieDrawer(
    isOpen: Boolean,
    telemetry: HardwareTelemetry,
    performanceMode: PerformanceMode = PerformanceMode.PERFORMANCE,
    crosshairEnabled: Boolean,
    fpsPillEnabled: Boolean,
    bypassChargingEnabled: Boolean,
    notificationShieldEnabled: Boolean,
    games: List<GameSpaceProfile>,
    latestNetworkReport: NetworkCheckReport? = null,
    isCheckingNetwork: Boolean = false,
    visibleHudTools: Set<HudShortcutTool> = HudShortcutTool.entries.toSet(),
    activeSessionGamePackage: String? = null,
    onClose: () -> Unit,
    onSelectMode: (PerformanceMode) -> Unit,
    onQuickRamPurge: () -> Unit,
    onQuickNetworkCheck: () -> Unit = {},
    onToggleCrosshair: () -> Unit,
    onToggleFpsPill: () -> Unit,
    onToggleBypassCharging: () -> Unit,
    onToggleNotificationShield: () -> Unit,
    onToggleHudToolVisibility: (HudShortcutTool) -> Unit = {},
    onOpenAiAdvisor: () -> Unit = {},
    onEndGamingSession: () -> Unit = {},
    onLaunchGame: (GameSpaceProfile) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var showMiniExpandedDetails by rememberSaveable { mutableStateOf(false) }
    var showToolCustomizer by rememberSaveable { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.50f))
                .clickable { onClose() }
                .testTag("edge_game_genie_backdrop"),
            contentAlignment = Alignment.CenterEnd
        ) {
            Surface(
                modifier = Modifier
                    .width(if (isLandscape) 360.dp else 320.dp)
                    .fillMaxHeight(if (isLandscape) 0.94f else 0.90f)
                    .clickable(enabled = false) { }
                    .testTag("edge_game_genie_drawer"),
                shape = CutCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                color = ObsidianSurface.copy(alpha = 0.97f),
                border = BorderStroke(1.5.dp, performanceMode.primaryColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "HASSAN SMART GAMING HUD",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = performanceMode.primaryColor
                            )
                            Text(
                                text = if (isLandscape) "شريط اللعب الجانبي (وضع أفقي)" else "شريط اللعب الجانبي الذكي",
                                style = MaterialTheme.typography.bodySmall,
                                color = SilverMist
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { showToolCustomizer = !showToolCustomizer },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(GunmetalCard)
                                    .testTag("hud_customize_tools_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "تخصيص أدوات الشريط",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = onClose,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(GunmetalCard)
                                    .testTag("close_game_genie_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق الشريط الجانبي",
                                    tint = TitaniumWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Expandable Mini Telemetry & Network Panel
                    val pingMs = latestNetworkReport?.avgPingMs ?: telemetry.pingMs
                    val connTypeLabel = latestNetworkReport?.connectionType?.labelAr ?: "فحص مباشر"
                    val pingColor = when {
                        pingMs <= 0 -> SilverMist
                        pingMs < 55 -> MatrixGreen
                        pingMs < 100 -> MoltenAmber
                        else -> CrimsonRed
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GunmetalCard,
                        border = BorderStroke(1.dp, CarbonBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMiniExpandedDetails = !showMiniExpandedDetails }
                            .testTag("hud_expandable_telemetry_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GenieStatBadge(
                                    icon = Icons.Default.Wifi,
                                    label = "PING",
                                    value = if (pingMs > 0) "${pingMs}ms" else "N/A",
                                    color = pingColor
                                )
                                GenieStatBadge(
                                    icon = Icons.Default.Thermostat,
                                    label = "حرارة البطارية",
                                    value = String.format(Locale.US, "%.1f°C", telemetry.batteryTempCelsius),
                                    color = if (telemetry.batteryTempCelsius > 42f) CrimsonRed else MoltenAmber
                                )
                                GenieStatBadge(
                                    icon = Icons.Default.Memory,
                                    label = "الذاكرة RAM",
                                    value = "${telemetry.ramUsagePercent}%",
                                    color = CyberCyan
                                )
                                GenieStatBadge(
                                    icon = Icons.Default.BatteryChargingFull,
                                    label = "البطارية",
                                    value = "${telemetry.batteryPercent}%",
                                    color = MatrixGreen
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الشبكة: $connTypeLabel",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverMist,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = if (showMiniExpandedDetails) "إخفاء التفاصيل ▲" else "توسيع القراءات ▼",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    fontSize = 10.sp
                                )
                            }

                            AnimatedVisibility(visible = showMiniExpandedDetails) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    HorizontalDivider(color = CarbonBorder)
                                    Text(
                                        text = "• المستشعر الحراري: مستشعر البطارية الفعلي (Battery Sensor) = ${String.format(Locale.US, "%.1f°C", telemetry.batteryTempCelsius)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TitaniumWhite,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "• الذاكرة العشوائية: متاح ${telemetry.ramAvailableMb} MB من إجمالي ${telemetry.ramTotalMb} MB",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TitaniumWhite,
                                        fontSize = 10.sp
                                    )
                                    if (latestNetworkReport != null) {
                                        Text(
                                            text = "• آخر خادم فحص: ${latestNetworkReport.testedServerName} • التذبذب (Jitter): ${latestNetworkReport.jitterMs ?: 0}ms • الفقدان: ${latestNetworkReport.packetLossPercent ?: 0}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CyberCyan,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Text(
                                        text = "• شفافية القياس: لا يعرض الشريط رقم FPS وهمي للعبة.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MoltenAmber,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    // Tool Customizer Panel
                    AnimatedVisibility(visible = showToolCustomizer) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GunmetalCard,
                            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("hud_tool_customizer_panel")
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "اختر الأدوات التي تظهر في الشريط الجانبي:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    HudShortcutTool.entries.forEach { tool ->
                                        val selected = visibleHudTools.contains(tool)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (selected) MatrixGreen.copy(alpha = 0.2f) else ObsidianSurface)
                                                .border(
                                                    1.dp,
                                                    if (selected) MatrixGreen else CarbonBorder,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { onToggleHudToolVisibility(tool) }
                                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                                .testTag("hud_toggle_tool_${tool.name}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (selected) "✓ ${tool.titleAr}" else "+ ${tool.titleAr}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (selected) MatrixGreen else SilverMist,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Performance Mode Selector
                    Text(
                        text = "وضع الأداء الفوري",
                        style = MaterialTheme.typography.titleMedium,
                        color = TitaniumWhite,
                        fontSize = 13.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PerformanceMode.entries.forEach { mode ->
                            val selected = mode == performanceMode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CutCornerShape(8.dp))
                                    .background(
                                        if (selected) mode.primaryColor.copy(alpha = 0.22f)
                                        else GunmetalCard
                                    )
                                    .border(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) mode.primaryColor else CarbonBorder,
                                        shape = CutCornerShape(8.dp)
                                    )
                                    .clickable { onSelectMode(mode) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.badgeText,
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) mode.primaryColor else SilverMist
                                )
                            }
                        }
                    }

                    // Quick Toggles Grid
                    Text(
                        text = "اختصارات الأدوات النشطة",
                        style = MaterialTheme.typography.titleMedium,
                        color = TitaniumWhite,
                        fontSize = 13.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (visibleHudTools.contains(HudShortcutTool.NETWORK_CHECK)) {
                            GenieActionTile(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("hud_quick_network_check_tile"),
                                icon = Icons.Default.NetworkCheck,
                                label = if (isCheckingNetwork) "جاري فحص الشبكة..." else "فحص الشبكة الفوري (${if (pingMs > 0) "${pingMs}ms" else "اختبار"})",
                                active = true,
                                activeColor = CyberCyan,
                                onClick = onQuickNetworkCheck
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (visibleHudTools.contains(HudShortcutTool.RAM_BOOST)) {
                                GenieActionTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Bolt,
                                    label = "تنظيف RAM",
                                    active = true,
                                    activeColor = MatrixGreen,
                                    onClick = onQuickRamPurge
                                )
                            }
                            if (visibleHudTools.contains(HudShortcutTool.CROSSHAIR)) {
                                GenieActionTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.GpsFixed,
                                    label = "Crosshair",
                                    active = crosshairEnabled,
                                    activeColor = CrimsonRed,
                                    onClick = onToggleCrosshair
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (visibleHudTools.contains(HudShortcutTool.FPS_PILL)) {
                                GenieActionTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Speed,
                                    label = "شريط القياس HUD",
                                    active = fpsPillEnabled,
                                    activeColor = CyberCyan,
                                    onClick = onToggleFpsPill
                                )
                            }
                            if (visibleHudTools.contains(HudShortcutTool.THERMAL_COOL)) {
                                GenieActionTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.BatteryChargingFull,
                                    label = "تثبيت السطوع/الحرارة",
                                    active = bypassChargingEnabled,
                                    activeColor = MoltenAmber,
                                    onClick = onToggleBypassCharging
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (visibleHudTools.contains(HudShortcutTool.NOTIFICATION_SHIELD)) {
                                GenieActionTile(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.NotificationsOff,
                                    label = "منع الإزعاج",
                                    active = notificationShieldEnabled,
                                    activeColor = MatrixGreen,
                                    onClick = onToggleNotificationShield
                                )
                            }
                            if (visibleHudTools.contains(HudShortcutTool.HASSAN_AI_QUICK)) {
                                GenieActionTile(
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("hud_open_ai_advisor_tile"),
                                    icon = Icons.Default.Psychology,
                                    label = "مساعد Hassan AI",
                                    active = true,
                                    activeColor = MoltenAmber,
                                    onClick = {
                                        onClose()
                                        onOpenAiAdvisor()
                                    }
                                )
                            }
                        }
                    }

                    if (games.isNotEmpty()) {
                        HorizontalDivider(color = CarbonBorder)

                        Text(
                            text = "تشغيل سريع للألعاب",
                            style = MaterialTheme.typography.titleMedium,
                            color = TitaniumWhite,
                            fontSize = 13.sp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            games.take(3).forEach { game ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(GunmetalCard)
                                        .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            onLaunchGame(game)
                                            onClose()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SportsEsports,
                                            contentDescription = null,
                                            tint = Color(game.accentHex),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = game.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TitaniumWhite,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = if (game.isInstalled) "تشغيل" else "تجهيز",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CyberCyan
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = CarbonBorder)

                    // Return / End Gaming Mode Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onClose,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("hud_return_to_app_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CyberCyan)
                        ) {
                            Text(
                                text = "الرجوع للتطبيق",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                onEndGamingSession()
                                onClose()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("hud_end_gaming_mode_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonRed,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (!activeSessionGamePackage.isNullOrBlank()) "إنهاء الجلسة" else "إيقاف الوضع",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenieStatBadge(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = value,
            fontFamily = OrbitronFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TitaniumWhite
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SilverMist,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun GenieActionTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) activeColor.copy(alpha = 0.16f) else GunmetalCard)
            .border(
                width = 1.dp,
                color = if (active) activeColor else CarbonBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) activeColor else SilverMist,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (active) TitaniumWhite else SilverMist,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
