package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoNotTouch
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClonedButtonsConfig
import com.example.model.CrosshairConfig
import com.example.model.HardwareTelemetry
import com.example.model.LowEndDeviceConfig
import com.example.model.PerformanceMode
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.GunmetalCard
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MoltenAmber
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.SilverMist
import com.example.ui.theme.TitaniumWhite
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/**
 * In-App Game Genie / Gaming Mode Side Dock Overlay.
 * Clean, lightweight, and responsive in-game control dock.
 */
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
    onRunInstantBoost: () -> Unit,
    onSelectPerformanceMode: (PerformanceMode) -> Unit,
    onToggleCrosshair: () -> Unit,
    onToggleFpsPill: () -> Unit,
    onToggleNotificationShield: () -> Unit,
    onToggleMistouch: () -> Unit,
    onToggleBrightnessLock: () -> Unit,
    onToggleClonedButtonsOverlay: () -> Unit,
    onToggleClonedButtonsLock: () -> Unit,
    onActivateSystemFloatingBar: () -> Unit,
    onRunCoolDown: () -> Unit = {},
    onShutdownAndExitApp: () -> Unit = {}
) {
    var handleOffsetY by remember { mutableFloatStateOf(0f) }
    var isRightEdge by remember { mutableStateOf(true) }

    // Tactical Stopwatch state inside the Game Genie sidebar
    var timerRunning by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(timerRunning) {
        while (timerRunning) {
            delay(1000L)
            timerSeconds += 1
        }
    }

    // 1. Center Floating Crosshair Preview when enabled in-app
    if (crosshairConfig.enabled && !isSystemOverlayRunning) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CrosshairCanvasView(config = crosshairConfig)
        }
    }

    // 2. In-App Floating HUD Pill (FPS / Temp / RAM) when enabled
    if (fpsPillVisible && !isSystemOverlayRunning) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp, start = 16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ObsidianSurface.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, CyberCyan),
                modifier = Modifier.testTag("in_app_hud_overlay_pill")
            ) {
                Text(
                    text = "${telemetry.liveFps} FPS  •  ${telemetry.batteryTempCelsius.roundToInt()}°C  •  RAM ${telemetry.ramUsagePercent}%",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MatrixGreen,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }

    // 3. Collapsed Glowing Side-Edge Swipe Handle
    if (sidebarEnabledInApp && !isPanelExpanded) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = if (isRightEdge) Alignment.CenterStart else Alignment.CenterEnd
        ) {
            Surface(
                modifier = Modifier
                    .offset { IntOffset(0, handleOffsetY.roundToInt()) }
                    .width(28.dp)
                    .height(118.dp)
                    .pointerInput(isRightEdge) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            change.consume()
                            if (abs(dragAmount) > 6f) {
                                onSetPanelExpanded(true)
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            change.consume()
                            handleOffsetY = (handleOffsetY + dragAmount).coerceIn(-380f, 380f)
                        }
                    }
                    .clickable { onSetPanelExpanded(true) }
                    .testTag("edge_swipe_handle"),
                shape = if (isRightEdge) {
                    RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp)
                } else {
                    RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                },
                color = ObsidianSurface.copy(alpha = 0.88f),
                border = BorderStroke(1.5.dp, CrimsonRed)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(26.dp)
                            .clip(CircleShape)
                            .background(CyberCyan)
                    )

                    Icon(
                        imageVector = if (isRightEdge) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                        contentDescription = "فتح الشريط الجانبي العائم",
                        tint = TitaniumWhite,
                        modifier = Modifier.size(18.dp)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${telemetry.liveFps}",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = MatrixGreen
                        )
                        Text(
                            text = "FPS",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 7.sp,
                            color = SilverMist
                        )
                    }
                }
            }
        }
    }

    // 4. Expanded Floating Game Genie / Game Turbo Side Panel
    AnimatedVisibility(
        visible = isPanelExpanded,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBlack.copy(alpha = 0.58f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onSetPanelExpanded(false) })
                },
            contentAlignment = if (isRightEdge) Alignment.CenterStart else Alignment.CenterEnd
        ) {
            Surface(
                modifier = Modifier
                    .width(315.dp)
                    .fillMaxHeight(0.88f)
                    .padding(horizontal = 8.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { /* consume inside panel */ })
                    }
                    .testTag("floating_game_genie_panel"),
                shape = CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp, topEnd = 8.dp, bottomStart = 8.dp),
                color = ObsidianSurface.copy(alpha = 0.97f),
                border = BorderStroke(1.8.dp, CrimsonRed)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = CrimsonRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "شريط الألعاب العائم",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TitaniumWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "HASSAN GAMING SIDEBAR",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 9.sp,
                                    color = CyberCyan
                                )
                            }
                        }

                        Row {
                            IconButton(
                                onClick = { isRightEdge = !isRightEdge },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "تبديل جهة الشريط يمين/يسار",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { onSetPanelExpanded(false) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق الشريط",
                                    tint = SilverMist,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Live Telemetry Mini Strip inside Floating Panel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(GunmetalCard)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FloatingMetricItem("FPS", "${telemetry.liveFps}", MatrixGreen)
                        FloatingMetricItem("RAM", "${telemetry.ramUsagePercent}%", CyberCyan)
                        FloatingMetricItem("TEMP", "${telemetry.batteryTempCelsius.roundToInt()}°", MoltenAmber)
                        FloatingMetricItem("PING", "${telemetry.pingMs}ms", MatrixGreen)
                    }

                    // 1-Tap Instant RAM & Cache Purge + ICE Cooler inside Floating Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onRunInstantBoost,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("floating_instant_boost_btn"),
                            shape = CutCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonRed,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تسريع الرام",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onRunCoolDown,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("floating_cooldown_btn"),
                            shape = CutCornerShape(8.dp),
                            border = BorderStroke(1.dp, CyberCyan),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AcUnit,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تبريد ذكي",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Performance Mode Quick Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PerformanceMode.entries.forEach { mode ->
                            val selected = mode == performanceMode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) mode.primaryColor.copy(alpha = 0.25f) else GunmetalCard)
                                    .border(1.dp, if (selected) mode.primaryColor else CarbonBorder, RoundedCornerShape(6.dp))
                                    .clickable { onSelectPerformanceMode(mode) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.badgeText,
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) mode.primaryColor else SilverMist
                                )
                            }
                        }
                    }

                    // Quick Tactical Grid Tools (Clean Gaming Tools)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FloatingDockTile(
                                icon = Icons.Default.GpsFixed,
                                title = "مؤشر التصويب",
                                status = if (crosshairConfig.enabled) "مفعّل ON" else "متوقف",
                                active = crosshairConfig.enabled,
                                accent = CrimsonRed,
                                onClick = onToggleCrosshair,
                                modifier = Modifier.weight(1f)
                            )
                            FloatingDockTile(
                                icon = Icons.Default.Speed,
                                title = "عداد FPS العائم",
                                status = if (fpsPillVisible) "مفعّل ON" else "متوقف",
                                active = fpsPillVisible,
                                accent = MatrixGreen,
                                onClick = onToggleFpsPill,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FloatingDockTile(
                                icon = Icons.Default.ControlCamera,
                                title = "أزرار منسوخة",
                                status = if (clonedButtonsConfig.enabled) "عائمة ON" else "إظهار C1-C4",
                                active = clonedButtonsConfig.enabled,
                                accent = CyberCyan,
                                onClick = onToggleClonedButtonsOverlay,
                                modifier = Modifier.weight(1f)
                            )
                            FloatingDockTile(
                                icon = if (clonedButtonsConfig.editPositionsLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                title = "قفل المواقع",
                                status = if (clonedButtonsConfig.editPositionsLocked) "مقفول للعب 🔒" else "وضع تحريك 🔓",
                                active = clonedButtonsConfig.editPositionsLocked,
                                accent = MatrixGreen,
                                onClick = onToggleClonedButtonsLock,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FloatingDockTile(
                                icon = Icons.Default.NotificationsOff,
                                title = "درع الإشعارات",
                                status = if (notificationShieldActive) "صامت ON" else "عادي",
                                active = notificationShieldActive,
                                accent = ElectricPurple,
                                onClick = onToggleNotificationShield,
                                modifier = Modifier.weight(1f)
                            )
                            FloatingDockTile(
                                icon = Icons.Default.DoNotTouch,
                                title = "منع اللمس الخاطئ",
                                status = if (lowEndConfig.touchEdgeRejectEnabled) "محمي" else "عادي",
                                active = lowEndConfig.touchEdgeRejectEnabled,
                                accent = MoltenAmber,
                                onClick = onToggleMistouch,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FloatingDockTile(
                                icon = Icons.Default.BrightnessHigh,
                                title = "تثبيت السطوع",
                                status = if (lowEndConfig.brightnessLockEnabled) "${lowEndConfig.lockedBrightnessPercent}% ثابت" else "تلقائي",
                                active = lowEndConfig.brightnessLockEnabled,
                                accent = CyberCyan,
                                onClick = onToggleBrightnessLock,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Tactical Cooldown / Airdrop Stopwatch
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = GunmetalCard,
                        border = BorderStroke(1.dp, CarbonBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MoltenAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "مؤقت تكتيكي داخل اللعب",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TitaniumWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val mins = timerSeconds / 60
                                    val secs = timerSeconds % 60
                                    Text(
                                        text = "%02d:%02d".format(mins, secs),
                                        fontFamily = OrbitronFontFamily,
                                        fontSize = 13.sp,
                                        color = MoltenAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { timerRunning = !timerRunning },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "بدء/إيقاف المؤقت",
                                        tint = if (timerRunning) MatrixGreen else TitaniumWhite
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        timerRunning = false
                                        timerSeconds = 0
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "تصفير المؤقت",
                                        tint = SilverMist
                                    )
                                }
                            }
                        }
                    }

                    // System-Wide Overlay Pin Button (For External Games)
                    OutlinedButton(
                        onClick = onActivateSystemFloatingBar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("enable_system_floating_bar_btn"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSystemOverlayRunning) MatrixGreen else CyberCyan
                        ),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = if (isSystemOverlayRunning) MatrixGreen else CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isSystemOverlayRunning -> "✓ الشريط العائم نشط فوق الألعاب الخارجية"
                                canDrawSystemOverlays -> "تشغيل الشريط العائم فوق جميع الألعاب الآن"
                                else -> "منح إذن الظهور العائم فوق الألعاب (Settings)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSystemOverlayRunning) MatrixGreen else CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Complete Shutdown & Exit Button (Stops background & closes app)
                    Button(
                        onClick = onShutdownAndExitApp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sidebar_shutdown_exit_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonRed,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⏹️ إنهاء وإغلاق البرنامج بالكامل (إيقاف الخلفية)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingMetricItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = color
        )
        Text(
            text = label,
            fontFamily = OrbitronFontFamily,
            fontSize = 9.sp,
            color = SilverMist
        )
    }
}

@Composable
private fun FloatingDockTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    status: String,
    active: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (active) accent.copy(alpha = 0.18f) else GunmetalCard,
        border = BorderStroke(1.dp, if (active) accent else CarbonBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (active) accent else SilverMist,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = TitaniumWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (active) accent else SilverMist,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}
