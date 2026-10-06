package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoNotTouch
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
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
import com.example.model.AudioRadarPreset
import com.example.model.CrosshairConfig
import com.example.model.EdgeSidebarConfig
import com.example.model.HardwareTelemetry
import com.example.model.LowEndOptimizerConfig
import com.example.model.PerformanceMode
import com.example.model.ScreenVisionFilter
import com.example.model.ShoulderTriggerConfig
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.GunmetalCard
import com.example.ui.theme.GunmetalElevated
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

@Composable
fun EdgeSwipeGameGenieOverlay(
    sidebarConfig: EdgeSidebarConfig,
    isPanelExpanded: Boolean,
    onSetPanelExpanded: (Boolean) -> Unit,
    telemetry: HardwareTelemetry,
    performanceMode: PerformanceMode,
    triggerConfig: ShoulderTriggerConfig,
    crosshairConfig: CrosshairConfig,
    lowEndConfig: LowEndOptimizerConfig,
    canDrawSystemOverlays: Boolean,
    isSystemOverlayRunning: Boolean,
    onRunInstantBoost: () -> Unit,
    onSelectPerformanceMode: (PerformanceMode) -> Unit,
    onToggleTriggers: () -> Unit,
    onToggleCrosshair: () -> Unit,
    onCycleVisionFilter: () -> Unit,
    onCycleAudioRadar: () -> Unit,
    onToggleMistouch: () -> Unit,
    onToggleAfkBlackScreen: (Boolean) -> Unit,
    onSwapSidebarEdge: () -> Unit,
    onActivateSystemFloatingBar: () -> Unit
) {
    var handleOffsetY by remember { mutableFloatStateOf(0f) }

    // Tactical Stopwatch state inside the Game Genie sidebar (for timing Boss respawns / Airdrops)
    var timerRunning by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(timerRunning) {
        while (timerRunning) {
            delay(1000L)
            timerSeconds += 1
        }
    }

    // 1. Vision Filter Screen Tint Overlay (Night Hunter / HDR Vivid / Predator / Eye Care)
    if (lowEndConfig.visionFilter != ScreenVisionFilter.NONE) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(lowEndConfig.visionFilter.overlayColor)
        )
    }

    // 2. Center Floating Crosshair Preview when enabled in-app
    if (crosshairConfig.enabledInApp) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CrosshairCanvasView(config = crosshairConfig)
        }
    }

    // 3. Collapsed Glowing Side-Edge Swipe Handle
    if (sidebarConfig.enabledInApp && !isPanelExpanded && !lowEndConfig.afkBlackScreenSaver) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = if (sidebarConfig.isRightEdge) Alignment.CenterStart else Alignment.CenterEnd
        ) {
            Surface(
                modifier = Modifier
                    .offset { IntOffset(0, handleOffsetY.roundToInt()) }
                    .width(28.dp)
                    .height(118.dp)
                    .pointerInput(sidebarConfig.isRightEdge) {
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
                shape = if (sidebarConfig.isRightEdge) {
                    RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp)
                } else {
                    RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                },
                color = ObsidianSurface.copy(alpha = sidebarConfig.handleOpacity),
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
                        imageVector = if (sidebarConfig.isRightEdge) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                        contentDescription = "فتح الشريط الجانبي العائم",
                        tint = TitaniumWhite,
                        modifier = Modifier.size(18.dp)
                    )

                    if (sidebarConfig.showFpsBadgeOnHandle) {
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
    }

    // 4. Expanded Floating Game Genie / Game Turbo Side Panel
    AnimatedVisibility(
        visible = isPanelExpanded && !lowEndConfig.afkBlackScreenSaver,
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
            contentAlignment = if (sidebarConfig.isRightEdge) Alignment.CenterStart else Alignment.CenterEnd
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
                                    text = "HASSAN GAME TURBO DOCK",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 9.sp,
                                    color = CyberCyan
                                )
                            }
                        }

                        Row {
                            IconButton(
                                onClick = onSwapSidebarEdge,
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
                        FloatingMetricItem("TEMP", "${telemetry.batteryTempCelsius}°", MoltenAmber)
                        FloatingMetricItem("PING", "${telemetry.pingMs}ms", MatrixGreen)
                    }

                    // 1-Tap Instant RAM & Cache Purge inside Floating Bar
                    Button(
                        onClick = onRunInstantBoost,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("floating_instant_boost_btn"),
                        shape = CutCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonRed,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مسح الكاش وتسريع الرام فوراً",
                            style = MaterialTheme.typography.labelLarge
                        )
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
                                    .background(if (selected) mode.color.copy(alpha = 0.25f) else GunmetalCard)
                                    .border(1.dp, if (selected) mode.color else CarbonBorder, RoundedCornerShape(6.dp))
                                    .clickable { onSelectPerformanceMode(mode) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.englishBadge.replace(" MODE", ""),
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) mode.color else SilverMist
                                )
                            }
                        }
                    }

                    // Quick Tactical Grid Tools (2x3)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FloatingDockTile(
                                icon = Icons.Default.VolumeUp,
                                title = "أزرار L1/R1",
                                status = if (triggerConfig.enabled) "مفعّل ON" else "متوقف",
                                active = triggerConfig.enabled,
                                accent = CyberCyan,
                                onClick = onToggleTriggers,
                                modifier = Modifier.weight(1f)
                            )
                            FloatingDockTile(
                                icon = Icons.Default.GpsFixed,
                                title = "مؤشر التصويب",
                                status = if (crosshairConfig.enabledInApp) crosshairConfig.style.id.take(6) else "متوقف",
                                active = crosshairConfig.enabledInApp,
                                accent = CrimsonRed,
                                onClick = onToggleCrosshair,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FloatingDockTile(
                                icon = Icons.Default.Visibility,
                                title = "كاشف الظلام",
                                status = lowEndConfig.visionFilter.arabicName.substringBefore(" "),
                                active = lowEndConfig.visionFilter != ScreenVisionFilter.NONE,
                                accent = MatrixGreen,
                                onClick = onCycleVisionFilter,
                                modifier = Modifier.weight(1f)
                            )
                            FloatingDockTile(
                                icon = Icons.Default.GraphicEq,
                                title = "رادار الخطوات",
                                status = lowEndConfig.audioRadarPreset.arabicName.substringBefore(" "),
                                active = lowEndConfig.audioRadarPreset != AudioRadarPreset.NORMAL,
                                accent = MoltenAmber,
                                onClick = onCycleAudioRadar,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FloatingDockTile(
                                icon = Icons.Default.DoNotTouch,
                                title = "منع اللمس الخاطئ",
                                status = if (lowEndConfig.mistouchPrevention) "محمي" else "عادي",
                                active = lowEndConfig.mistouchPrevention,
                                accent = ElectricPurple,
                                onClick = onToggleMistouch,
                                modifier = Modifier.weight(1f)
                            )
                            FloatingDockTile(
                                icon = Icons.Default.BatterySaver,
                                title = "الشاشة السوداء",
                                status = "توفير طاقة AFK",
                                active = lowEndConfig.afkBlackScreenSaver,
                                accent = CyberCyan,
                                onClick = {
                                    onSetPanelExpanded(false)
                                    onToggleAfkBlackScreen(true)
                                },
                                modifier = Modifier.weight(1f)
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
                                        text = "مؤقت تكتيكي (Airdrop / Boss)",
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
                                isSystemOverlayRunning -> "✓ الشريط العائم نشط فوق جميع الألعاب الخارجية"
                                canDrawSystemOverlays -> "تشغيل الشريط العائم فوق جميع الألعاب الآن"
                                else -> "منح إذن الظهور العائم فوق الألعاب (Settings)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSystemOverlayRunning) MatrixGreen else CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // 5. Black-Screen AFK Idle Mode (ROG / Samsung / Black Shark feature to save battery & cool phone while game runs)
    AnimatedVisibility(
        visible = lowEndConfig.afkBlackScreenSaver,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { onToggleAfkBlackScreen(false) }
                    )
                }
                .testTag("afk_black_screen_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BatterySaver,
                    contentDescription = null,
                    tint = CyberCyan.copy(alpha = 0.65f),
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "وضع الشاشة المظلمة النشط (AFK Cooling Mode)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TitaniumWhite.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "اللعبة والتنظيف التلقائي يعملان بأقصى كفاءة مع توفير 80% من طاقة الشاشة وتبريد البطارية (${telemetry.batteryTempCelsius}°C)",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(18.dp))
                OutlinedButton(
                    onClick = { onToggleAfkBlackScreen(false) },
                    border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = "انقر مرتين على الشاشة أو اضغط هنا للعودة",
                        style = MaterialTheme.typography.labelLarge,
                        color = CrimsonRed
                    )
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
