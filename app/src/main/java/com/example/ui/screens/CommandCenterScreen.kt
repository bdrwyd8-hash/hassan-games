package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BoostResult
import com.example.model.CrosshairConfig
import com.example.model.HardwareTelemetry
import com.example.model.LowEndOptimizerConfig
import com.example.model.PerformanceMode
import com.example.model.ShoulderTriggerConfig
import com.example.ui.components.RotatingCoolingTurbine
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GunmetalCard
import com.example.ui.theme.GunmetalElevated
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MoltenAmber
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.SilverMist
import com.example.ui.theme.TitaniumWhite
import com.example.viewmodel.RedCoreTab

@Composable
fun CommandCenterScreen(
    telemetry: HardwareTelemetry,
    performanceMode: PerformanceMode,
    triggerConfig: ShoulderTriggerConfig,
    crosshairConfig: CrosshairConfig,
    lowEndConfig: LowEndOptimizerConfig,
    isBoosting: Boolean,
    boostProgress: Float,
    boostStageText: String,
    lastBoostResult: BoostResult?,
    onSelectMode: (PerformanceMode) -> Unit,
    onRunSuperBoost: () -> Unit,
    onToggleVolumeTriggers: (Boolean) -> Unit,
    onToggleAutoClean: (Boolean) -> Unit,
    onToggleCrosshair: (Boolean) -> Unit,
    onNavigateToTab: (RedCoreTab) -> Unit,
    onTestNetworkPing: () -> Unit,
    onOpenEdgeSidebar: () -> Unit = {},
    onEnableSystemFloatingBar: () -> Unit = {},
    isSystemOverlayRunning: Boolean = false
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("command_center_list"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero RedCore Turbine & Super Boost Card
        item {
            HeroTurbineDashboardCard(
                telemetry = telemetry,
                performanceMode = performanceMode,
                isBoosting = isBoosting,
                boostProgress = boostProgress,
                boostStageText = boostStageText,
                lastBoostResult = lastBoostResult,
                onRunSuperBoost = onRunSuperBoost
            )
        }

        // 2. Floating Side-Edge Swipe Game Bar Banner (Game Genie / Game Turbo)
        item {
            FloatingEdgeGameBarDashboardBanner(
                isSystemOverlayRunning = isSystemOverlayRunning,
                onOpenEdgeSidebar = onOpenEdgeSidebar,
                onEnableSystemFloatingBar = onEnableSystemFloatingBar
            )
        }

        // 3. RedMagic Performance Modes (Diablo / Rise / Balance / Eco)
        item {
            PerformanceModesSection(
                activeMode = performanceMode,
                onSelectMode = onSelectMode
            )
        }

        // 3. Volume L1/R1 Shoulder Trigger Spotlight Banner
        item {
            VolumeTriggerSpotlightBanner(
                triggerConfig = triggerConfig,
                onToggleEnabled = onToggleVolumeTriggers,
                onOpenTriggerStudio = { onNavigateToTab(RedCoreTab.SHOULDER_TRIGGERS) }
            )
        }

        // 4. Quick Hardware & Low-End Device Boosters Grid
        item {
            QuickGamingTogglesSection(
                lowEndConfig = lowEndConfig,
                crosshairConfig = crosshairConfig,
                telemetry = telemetry,
                onToggleAutoClean = onToggleAutoClean,
                onToggleCrosshair = onToggleCrosshair,
                onOpenRamBooster = { onNavigateToTab(RedCoreTab.RAM_BOOSTER) },
                onOpenArsenal = { onNavigateToTab(RedCoreTab.ARSENAL_GFX) },
                onTestNetworkPing = onTestNetworkPing
            )
        }

        // 5. Real-time Hardware Cores & Memory Detail Card
        item {
            HardwareDeepSpecsCard(
                telemetry = telemetry,
                performanceMode = performanceMode,
                onOpenGamesLobby = { onNavigateToTab(RedCoreTab.GAME_SPACE) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroTurbineDashboardCard(
    telemetry: HardwareTelemetry,
    performanceMode: PerformanceMode,
    isBoosting: Boolean,
    boostProgress: Float,
    boostStageText: String,
    lastBoostResult: BoostResult?,
    onRunSuperBoost: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp, topEnd = 6.dp, bottomStart = 6.dp),
        color = GunmetalCard,
        border = BorderStroke(1.5.dp, performanceMode.color.copy(alpha = 0.75f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Generated Hero Art Background
            Image(
                painter = painterResource(id = R.drawable.img_redcore_hero_1791322212254),
                contentDescription = "خلفية مركز قيادة الألعاب",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize(),
                alpha = 0.28f
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ObsidianBlack.copy(alpha = 0.65f),
                                ObsidianSurface.copy(alpha = 0.88f),
                                ObsidianBlack.copy(alpha = 0.96f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(performanceMode.color)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = performanceMode.englishBadge,
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${performanceMode.targetFanRpm} RPM",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 11.sp,
                                color = CyberCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = performanceMode.arabicTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = performanceMode.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    RotatingCoolingTurbine(
                        rpm = performanceMode.targetFanRpm,
                        accentColor = performanceMode.color,
                        isTurboBoosting = isBoosting,
                        size = 80.dp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4 Live Hardware Gauges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LiveCoreMetricBox(
                        title = "المعالج CPU",
                        value = "${telemetry.cpuFreqMhz}",
                        unit = "MHz",
                        subValue = "استهلاك ${telemetry.cpuLoadPercent}%",
                        accent = performanceMode.color,
                        modifier = Modifier.weight(1f)
                    )
                    LiveCoreMetricBox(
                        title = "الرام المتاح",
                        value = "${telemetry.ramAvailableMb}",
                        unit = "MB",
                        subValue = "مستخدم ${telemetry.ramUsagePercent}%",
                        accent = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                    LiveCoreMetricBox(
                        title = "الفريمات FPS",
                        value = "${telemetry.liveFps}",
                        unit = "${telemetry.displayRefreshRateHz}Hz",
                        subValue = "${telemetry.frameTimeMs}ms زمن الإطار",
                        accent = MatrixGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Super Boost Progress or Action Button
                AnimatedVisibility(visible = isBoosting) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = boostStageText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(boostProgress * 100).toInt()}%",
                                fontFamily = OrbitronFontFamily,
                                color = TitaniumWhite,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { boostProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = CrimsonRed,
                            trackColor = GunmetalElevated
                        )
                    }
                }

                Button(
                    onClick = onRunSuperBoost,
                    enabled = !isBoosting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("super_boost_button"),
                    shape = CutCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonRed,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBoosting) "جاري تنظيف الخلفية وتفريغ الرام..."
                        else "تقوية خارقة الآن (مسح الكاش + إيقاف الخلفية)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (lastBoostResult != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MatrixGreen.copy(alpha = 0.12f))
                            .border(1.dp, MatrixGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "آخر تقوية: تم تحرير ${lastBoostResult.freedRamMb} MB رام ومسح ${lastBoostResult.cleanedCacheMb} MB كاش",
                            style = MaterialTheme.typography.bodySmall,
                            color = MatrixGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "+${lastBoostResult.estimatedFpsBoost} FPS",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TitaniumWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveCoreMetricBox(
    title: String,
    value: String,
    unit: String,
    subValue: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ObsidianBlack.copy(alpha = 0.78f))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = SilverMist,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TitaniumWhite
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = unit,
                fontFamily = OrbitronFontFamily,
                fontSize = 10.sp,
                color = accent
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subValue,
            style = MaterialTheme.typography.bodySmall,
            color = accent,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun PerformanceModesSection(
    activeMode: PerformanceMode,
    onSelectMode: (PerformanceMode) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "أوضاع القوة وكسر السرعة (RedMagic Modes)",
                style = MaterialTheme.typography.titleMedium,
                color = TitaniumWhite
            )
            Text(
                text = activeMode.englishBadge,
                fontFamily = OrbitronFontFamily,
                fontSize = 11.sp,
                color = activeMode.color
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PerformanceMode.entries.forEach { mode ->
                val isSelected = mode == activeMode
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectMode(mode) }
                        .testTag("mode_card_${mode.id}"),
                    shape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
                    color = if (isSelected) mode.color.copy(alpha = 0.22f) else GunmetalCard,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) mode.color else CarbonBorder
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) mode.color else SilverMist.copy(alpha = 0.4f))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = mode.englishBadge.replace(" MODE", ""),
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isSelected) mode.color else TitaniumWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = mode.arabicTitle.substringBefore(" "),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) TitaniumWhite else SilverMist,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VolumeTriggerSpotlightBanner(
    triggerConfig: ShoulderTriggerConfig,
    onToggleEnabled: (Boolean) -> Unit,
    onOpenTriggerStudio: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenTriggerStudio() }
            .testTag("spotlight_volume_triggers_card"),
        shape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp),
        color = GunmetalCard,
        border = BorderStroke(
            1.5.dp,
            if (triggerConfig.enabled) CyberCyan.copy(alpha = 0.8f) else CarbonBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CutCornerShape(8.dp))
                            .background(CyberCyan.copy(alpha = 0.16f))
                            .border(1.dp, CyberCyan, CutCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "أزرار الكتف L1 / R1 بأزرار الصوت",
                                style = MaterialTheme.typography.titleMedium,
                                color = TitaniumWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "حَوّل زر رفع الصوت إلى (L1) وزر خفض الصوت إلى (R1) للرمي السريع والسكوب",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist
                        )
                    }
                }

                Switch(
                    checked = triggerConfig.enabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = CyberCyan
                    ),
                    modifier = Modifier.testTag("dashboard_trigger_switch")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurface)
                        .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "L1 (زر رفع الصوت +)",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${triggerConfig.l1Mode.arabicName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TitaniumWhite
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurface)
                        .border(1.dp, CrimsonRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "R1 (زر خفض الصوت -)",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            color = CrimsonRed,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${triggerConfig.r1Mode.arabicName} (${triggerConfig.r1BurstRps}/ث)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TitaniumWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "اضغط هنا لتخصيص مواقع L1/R1 على الشاشة وتجربتها فوراً ←",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyberCyan
                )
            }
        }
    }
}

@Composable
private fun QuickGamingTogglesSection(
    lowEndConfig: LowEndOptimizerConfig,
    crosshairConfig: CrosshairConfig,
    telemetry: HardwareTelemetry,
    onToggleAutoClean: (Boolean) -> Unit,
    onToggleCrosshair: (Boolean) -> Unit,
    onOpenRamBooster: () -> Unit,
    onOpenArsenal: () -> Unit,
    onTestNetworkPing: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "مسرعات الأجهزة الضعيفة الفورية",
            style = MaterialTheme.typography.titleMedium,
            color = TitaniumWhite
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickFeatureCard(
                icon = Icons.Default.CleaningServices,
                title = "مسح الكاش بالخلفية",
                subtitle = if (lowEndConfig.autoCleanInBackground)
                    "نشط تلقائياً عند ${lowEndConfig.ramThresholdPercent}% رام"
                else "متوقف حالياً",
                badgeText = "${telemetry.cacheEstimatedMb} MB",
                accentColor = MatrixGreen,
                isActive = lowEndConfig.autoCleanInBackground,
                onCardClick = onOpenRamBooster,
                onToggle = onToggleAutoClean,
                modifier = Modifier.weight(1f)
            )

            QuickFeatureCard(
                icon = Icons.Default.GpsFixed,
                title = "مؤشر التصويب Crosshair",
                subtitle = crosshairConfig.style.arabicName,
                badgeText = if (crosshairConfig.enabledInApp) "ON" else "OFF",
                accentColor = CrimsonRed,
                isActive = crosshairConfig.enabledInApp,
                onCardClick = onOpenArsenal,
                onToggle = onToggleCrosshair,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickFeatureCard(
                icon = Icons.Default.TouchApp,
                title = "حساسية لمس تيربو",
                subtitle = "استجابة فورية للأجهزة الضعيفة",
                badgeText = "${lowEndConfig.touchSamplingRateHz}Hz",
                accentColor = MoltenAmber,
                isActive = true,
                onCardClick = onOpenArsenal,
                onToggle = null,
                modifier = Modifier.weight(1f)
            )

            QuickFeatureCard(
                icon = Icons.Default.NetworkCheck,
                title = "تسريع البينج والشبكة",
                subtitle = telemetry.networkStatus,
                badgeText = "${telemetry.pingMs}ms",
                accentColor = CyberCyan,
                isActive = lowEndConfig.zeroLagNetworkMode,
                onCardClick = onTestNetworkPing,
                onToggle = null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickFeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String,
    accentColor: Color,
    isActive: Boolean,
    onCardClick: () -> Unit,
    onToggle: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        color = GunmetalCard,
        border = BorderStroke(
            1.dp,
            if (isActive) accentColor.copy(alpha = 0.55f) else CarbonBorder
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(19.dp)
                    )
                }

                if (onToggle != null) {
                    Switch(
                        checked = isActive,
                        onCheckedChange = onToggle,
                        modifier = Modifier.height(24.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBlack,
                            checkedTrackColor = accentColor
                        )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontFamily = OrbitronFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TitaniumWhite,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SilverMist,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HardwareDeepSpecsCard(
    telemetry: HardwareTelemetry,
    performanceMode: PerformanceMode,
    onOpenGamesLobby: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = GunmetalCard,
        border = BorderStroke(1.dp, CarbonBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = CrimsonRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "قراءات العتاد الحية (Real Device Telemetry)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${telemetry.cpuCores} أنوية نشطة",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 11.sp,
                    color = CyberCyan
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الذاكرة العشوائية (RAM): ${telemetry.ramUsedMb} / ${telemetry.ramTotalMb} MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist
                )
                Text(
                    text = "المساحة الحرة: ${telemetry.storageFreeGb} GB",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "البطارية: ${telemetry.batteryPercent}% (${telemetry.batteryVoltageMv} mV)",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist
                )
                Text(
                    text = "رسوميات GPU: ${telemetry.gpuEstLoadPercent}% (${performanceMode.englishBadge})",
                    style = MaterialTheme.typography.bodySmall,
                    color = performanceMode.color
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onOpenGamesLobby,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_game_lobby_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GunmetalElevated,
                    contentColor = CyberCyan
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Gamepad,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "فتح منصة تشغيل الألعاب (Game Space Lobby)",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun FloatingEdgeGameBarDashboardBanner(
    isSystemOverlayRunning: Boolean,
    onOpenEdgeSidebar: () -> Unit,
    onEnableSystemFloatingBar: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_edge_sidebar_card"),
        shape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp),
        color = GunmetalCard,
        border = BorderStroke(1.5.dp, MatrixGreen.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CutCornerShape(8.dp))
                            .background(MatrixGreen.copy(alpha = 0.16f))
                            .border(1.dp, MatrixGreen, CutCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MatrixGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "الشريط الجانبي العائم فوق الألعاب (Edge Swipe)",
                            style = MaterialTheme.typography.titleMedium,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "اسحب من جنب الجوال داخل أي لعبة لفتح أدوات Hassan Games بدون الخروج من اللعبة!",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenEdgeSidebar,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_open_sidebar_btn"),
                    shape = CutCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MatrixGreen,
                        contentColor = ObsidianBlack
                    )
                ) {
                    Text(
                        text = "فتح الشريط الجانبي الآن",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onEnableSystemFloatingBar,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_pin_overlay_btn"),
                    shape = CutCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GunmetalElevated,
                        contentColor = if (isSystemOverlayRunning) MatrixGreen else CyberCyan
                    )
                ) {
                    Text(
                        text = if (isSystemOverlayRunning) "عائم فوق الألعاب ✓" else "تفعيله فوق التطبيقات",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

