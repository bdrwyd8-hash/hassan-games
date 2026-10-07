package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdvisorPresetMode
import com.example.model.AutoSettingsRecommendation
import com.example.model.ClonedButtonsConfig
import com.example.model.CrosshairConfig
import com.example.model.GamePrepReport
import com.example.model.GameSpaceProfile
import com.example.model.HardwareTelemetry
import com.example.model.LastSessionGameInfo
import com.example.model.LowEndDeviceConfig
import com.example.model.NotificationShieldState
import com.example.model.PerformanceMode
import com.example.model.SmartThermalMode
import com.example.model.SmartThermalStatus
import com.example.ui.components.AutoSettingsAdvisorCard
import com.example.ui.components.MasterStartStopAppBanner
import com.example.ui.components.NotificationShieldBanner
import com.example.ui.components.OneTapGamePreparationCard
import com.example.ui.components.QuickGameReconnectCard
import com.example.ui.components.RotatingCoolingTurbine
import com.example.ui.components.SmartThermalModeCard
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
import kotlin.math.roundToInt

@Composable
fun CommandCenterScreen(
    telemetry: HardwareTelemetry,
    fpsHistory: List<Int>,
    performanceMode: PerformanceMode,
    games: List<GameSpaceProfile>,
    selectedGameId: String,
    crosshairConfig: CrosshairConfig,
    clonedButtonsConfig: ClonedButtonsConfig,
    lowEndConfig: LowEndDeviceConfig,
    fpsPillVisible: Boolean,
    isBoosting: Boolean,
    isAccessibilityRunning: Boolean,
    canDrawOverlays: Boolean,
    isSystemFloatingBarRunning: Boolean,
    isPreparingGame: Boolean,
    lastGamePrepReport: GamePrepReport?,
    smartThermalStatus: SmartThermalStatus,
    advisorPresetMode: AdvisorPresetMode,
    autoSettingsRecommendation: AutoSettingsRecommendation,
    lastSessionInfo: LastSessionGameInfo?,
    notificationShieldState: NotificationShieldState,
    onSelectPerformanceMode: (PerformanceMode) -> Unit,
    onQuickBoostRam: () -> Unit,
    onQuickLaunchSelectedGame: () -> Unit,
    onOpenMyGamesTab: () -> Unit,
    onToggleFpsPill: (Boolean) -> Unit,
    onToggleCrosshairOverlay: (Boolean) -> Unit,
    onToggleClonedButtonsOverlay: () -> Unit,
    onLaunchSystemFloatingSidebar: () -> Unit,
    onRunOneTapGamePreparation: (Boolean) -> Unit,
    onSelectSmartThermalMode: (SmartThermalMode) -> Unit,
    onSelectAdvisorPreset: (AdvisorPresetMode) -> Unit,
    onApplyAdvisorPreset: () -> Unit,
    onQuickReconnectLastGame: () -> Unit,
    onClearLastSessionRecord: () -> Unit,
    onToggleNotificationShield: (Boolean) -> Unit,
    onOpenDndPermissionSettings: () -> Unit,
    onShutdownAndExitApp: () -> Unit
) {
    val selectedGame = games.find { it.id == selectedGameId } ?: games.firstOrNull()
    val anyBackgroundActive = isSystemFloatingBarRunning ||
        fpsPillVisible ||
        crosshairConfig.enabled ||
        clonedButtonsConfig.enabled

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("command_center_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Master Start / Stop App Lifecycle Control Banner
        MasterStartStopAppBanner(
            isMasterRunning = anyBackgroundActive,
            onToggleStartOrExit = {
                if (anyBackgroundActive) {
                    onShutdownAndExitApp()
                } else {
                    onRunOneTapGamePreparation(false)
                }
            }
        )

        // 2. Quick Reconnect Banner (if a game session was recently active)
        if (lastSessionInfo != null) {
            QuickGameReconnectCard(
                lastSessionInfo = lastSessionInfo,
                onQuickReconnect = onQuickReconnectLastGame,
                onOpenMyGames = onOpenMyGamesTab
            )
        }

        // 3. One-Tap Game Preparation System Card
        OneTapGamePreparationCard(
            isPreparing = isPreparingGame,
            lastReport = lastGamePrepReport,
            activeGameTitle = selectedGame?.title,
            onExecuteOneTapPrep = { onRunOneTapGamePreparation(false) }
        )

        // 4. Live FPS Reactor & Real Hardware Telemetry Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hardware_gauges_card"),
            shape = CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp, topEnd = 8.dp, bottomStart = 8.dp),
            color = GunmetalCard,
            border = BorderStroke(1.5.dp, performanceMode.primaryColor.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RotatingCoolingTurbine(
                            rpm = telemetry.cpuFreqMhz,
                            accentColor = performanceMode.primaryColor,
                            isTurboBoosting = performanceMode == PerformanceMode.DIABLO || isBoosting,
                            size = 56.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "مفاعل الفريمات والعتاد الحي",
                                style = MaterialTheme.typography.titleMedium,
                                color = TitaniumWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${performanceMode.badgeText} • شاشة ${telemetry.displayRefreshRateHz}Hz • زمن الإطار ${telemetry.frameTimeMs}ms",
                                style = MaterialTheme.typography.bodySmall,
                                color = SilverMist,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${telemetry.liveFps}",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MatrixGreen
                        )
                        Text(
                            text = "LIVE FPS",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 9.sp,
                            color = SilverMist
                        )
                    }
                }

                // Circular Gauges Row (CPU / GPU / RAM)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HardwareCircularGauge(
                        title = "المعالج CPU",
                        percentage = telemetry.cpuLoadPercent,
                        valueLabel = "${telemetry.cpuLoadPercent}%",
                        subLabel = "${telemetry.cpuFreqMhz} MHz",
                        accentColor = CrimsonRed
                    )
                    HardwareCircularGauge(
                        title = "الرسوميات GPU",
                        percentage = telemetry.gpuEstLoadPercent,
                        valueLabel = "${telemetry.gpuEstLoadPercent}%",
                        subLabel = "${telemetry.displayRefreshRateHz}Hz",
                        accentColor = CyberCyan
                    )
                    HardwareCircularGauge(
                        title = "الذاكرة RAM",
                        percentage = telemetry.ramUsagePercent,
                        valueLabel = "${telemetry.ramUsagePercent}%",
                        subLabel = "${telemetry.ramAvailableMb}MB Free",
                        accentColor = MatrixGreen
                    )
                }

                // Secondary Live Hardware Strip (Battery Temp, Ping, Storage)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianSurface)
                        .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = null,
                            tint = MoltenAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${telemetry.batteryTempCelsius.roundToInt()}°C (${telemetry.batteryPercent}%)",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = null,
                            tint = MatrixGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PING ${telemetry.pingMs}ms",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            color = MatrixGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FREE ${telemetry.storageFreeGb.roundToInt()}GB",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 5. Performance Mode Selector (Balanced / Performance / DIABLO)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("performance_modes_card"),
            shape = RoundedCornerShape(14.dp),
            color = GunmetalCard,
            border = BorderStroke(1.dp, CarbonBorder)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = performanceMode.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "محرك الأداء النشط (Performance Profile)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                PerformanceMode.entries.forEach { mode ->
                    val isSelected = mode == performanceMode
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPerformanceMode(mode) }
                            .testTag("perf_mode_option_${mode.name}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) mode.primaryColor.copy(alpha = 0.16f) else ObsidianSurface,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) mode.primaryColor else CarbonBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = mode.titleAr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TitaniumWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${mode.badgeText} • ${mode.targetFps} FPS",
                                        fontFamily = OrbitronFontFamily,
                                        fontSize = 9.sp,
                                        color = mode.primaryColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = mode.subtitleAr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverMist,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "مفعّل",
                                    tint = mode.primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Quick In-Game Overlays & Sidebar Control Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quick_overlays_control_card"),
            shape = RoundedCornerShape(14.dp),
            color = GunmetalCard,
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "أدوات اللعب العائمة والشريط الجانبي (Gaming Overlays)",
                            style = MaterialTheme.typography.titleSmall,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تعمل عند التفعيل فقط وتغلق فوراً عند الإيقاف لتوفير الرام والبطارية",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist,
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = onLaunchSystemFloatingSidebar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("cmd_launch_floating_sidebar_btn"),
                    shape = CutCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSystemFloatingBarRunning) MatrixGreen else CyberCyan,
                        contentColor = ObsidianBlack
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSystemFloatingBarRunning)
                            "✓ الشريط الجانبي العائم نشط فوق الألعاب"
                        else
                            "تفعيل الشريط الجانبي العائم فوق الألعاب (Game Sidebar)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                QuickOverlayToggleRow(
                    icon = Icons.Default.Speed,
                    title = "عداد الفريمات والحرارة العائم (FPS Pill)",
                    subtitle = "يعرض الفريمات والحرارة والرام في كبسولة قابلة للسحب فوق اللعبة",
                    checked = fpsPillVisible,
                    accent = MatrixGreen,
                    onCheckedChange = onToggleFpsPill
                )

                QuickOverlayToggleRow(
                    icon = Icons.Default.GpsFixed,
                    title = "مؤشر التصويب المركزي (Crosshair)",
                    subtitle = "نقطة أو صليب تصويب ثابت وسط الشاشة بدون حجب اللمس",
                    checked = crosshairConfig.enabled,
                    accent = CrimsonRed,
                    onCheckedChange = onToggleCrosshairOverlay
                )

                QuickOverlayToggleRow(
                    icon = Icons.Default.ControlCamera,
                    title = "الأزرار المنسوخة القابلة للتحريك (C1–C4)",
                    subtitle = "أزرار لمس إضافية عائمة على الشاشة تضغط الأزرار الثابتة باللعبة",
                    checked = clonedButtonsConfig.enabled,
                    accent = CyberCyan,
                    onCheckedChange = { onToggleClonedButtonsOverlay() }
                )
            }
        }

        // 7. Smart Thermal Control System Card
        SmartThermalModeCard(
            thermalStatus = smartThermalStatus,
            onSelectMode = onSelectSmartThermalMode
        )

        // 8. Auto Game Settings Advisor Card
        AutoSettingsAdvisorCard(
            recommendation = autoSettingsRecommendation,
            onSelectMode = onSelectAdvisorPreset,
            onApplyRecommendedTools = onApplyAdvisorPreset
        )

        // 9. Notification Shield Control Card
        NotificationShieldBanner(
            shieldState = notificationShieldState,
            onToggleShield = { onToggleNotificationShield(!notificationShieldState.enabled) },
            onRequestDndPermission = onOpenDndPermissionSettings
        )

        // 10. Quick Shortcut to MY GAMES
        OutlinedButton(
            onClick = onOpenMyGamesTab,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("cmd_open_my_games_btn"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.2.dp, CrimsonRed)
        ) {
            Icon(
                imageVector = Icons.Default.SportsEsports,
                contentDescription = null,
                tint = CrimsonRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "فتح مركز ألعابي (MY GAMES • + ADD GAME)",
                style = MaterialTheme.typography.labelLarge,
                color = TitaniumWhite,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HardwareCircularGauge(
    title: String,
    percentage: Int,
    valueLabel: String,
    subLabel: String,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(82.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(76.dp)) {
                val strokeWidth = 7.dp.toPx()
                drawArc(
                    color = CarbonBorder,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(size.width, size.height),
                    topLeft = Offset.Zero
                )
                val sweep = (percentage.coerceIn(0, 100) / 100f) * 270f
                drawArc(
                    color = accentColor,
                    startAngle = 135f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(size.width, size.height),
                    topLeft = Offset.Zero
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = valueLabel,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TitaniumWhite
                )
                Text(
                    text = subLabel,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 8.sp,
                    color = accentColor
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = SilverMist,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QuickOverlayToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    accent: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ObsidianSurface)
            .border(1.dp, if (checked) accent.copy(alpha = 0.6f) else CarbonBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = TitaniumWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist,
                    fontSize = 10.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianBlack,
                checkedTrackColor = accent
            )
        )
    }
}
