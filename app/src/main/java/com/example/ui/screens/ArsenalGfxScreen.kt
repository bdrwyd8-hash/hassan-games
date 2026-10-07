package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AdvisorPresetMode
import com.example.model.AutoSettingsRecommendation
import com.example.model.ClonedButtonsConfig
import com.example.model.ClonedTouchButton
import com.example.model.CrosshairConfig
import com.example.model.CrosshairStyle
import com.example.model.GfxResolutionPreset
import com.example.model.HardwareTelemetry
import com.example.model.LowEndDeviceConfig
import com.example.model.SmartThermalMode
import com.example.model.SmartThermalStatus
import com.example.model.TouchSamplingRate
import com.example.ui.components.AutoSettingsAdvisorCard
import com.example.ui.components.ClonedButtonsStudioCard
import com.example.ui.components.CrosshairCanvasView
import com.example.ui.components.CustomAppIconStudioCard
import com.example.ui.components.GameBoxEnvironmentCard
import com.example.ui.components.SmartThermalModeCard
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
import kotlin.math.roundToInt

@Composable
fun ArsenalGfxScreen(
    crosshairConfig: CrosshairConfig,
    clonedButtonsConfig: ClonedButtonsConfig,
    gfxResolution: GfxResolutionPreset,
    touchSamplingRate: TouchSamplingRate,
    lowEndConfig: LowEndDeviceConfig,
    telemetry: HardwareTelemetry,
    smartThermalStatus: SmartThermalStatus,
    advisorPresetMode: AdvisorPresetMode,
    autoSettingsRecommendation: AutoSettingsRecommendation,
    canDrawOverlays: Boolean,
    isAccessibilityRunning: Boolean,
    isSystemFloatingBarRunning: Boolean,
    fpsPillVisible: Boolean,
    onUpdateCrosshair: ((CrosshairConfig) -> CrosshairConfig) -> Unit,
    onUpdateClonedButtons: ((ClonedButtonsConfig) -> ClonedButtonsConfig) -> Unit,
    onToggleClonedButtonsOverlay: () -> Unit,
    onSimulateClonedTap: (ClonedTouchButton, Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onSelectResolution: (GfxResolutionPreset) -> Unit,
    onSelectTouchRate: (TouchSamplingRate) -> Unit,
    onUpdateLowEnd: ((LowEndDeviceConfig) -> LowEndDeviceConfig) -> Unit,
    onSelectSmartThermalMode: (SmartThermalMode) -> Unit,
    onSelectAdvisorPreset: (AdvisorPresetMode) -> Unit,
    onApplyAdvisorPreset: () -> Unit,
    onToggleFpsPill: (Boolean) -> Unit,
    onLaunchSystemFloatingSidebar: () -> Unit,
    onOpenOverlayPermissionSettings: () -> Unit,
    onTestNetworkPing: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("arsenal_gfx_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Movable Cloned Touch Buttons Studio (C1-C4 On-Screen Touch Cloner)
        ClonedButtonsStudioCard(
            config = clonedButtonsConfig,
            isAccessibilityRunning = isAccessibilityRunning,
            onUpdateConfig = onUpdateClonedButtons,
            onToggleSystemOverlay = onToggleClonedButtonsOverlay,
            onSimulateClonedTap = onSimulateClonedTap,
            onOpenAccessibilitySettings = onOpenAccessibilitySettings
        )

        // 2. Tactical Crosshair Studio Card
        CrosshairStudioCard(
            config = crosshairConfig,
            canDrawOverlays = canDrawOverlays,
            onUpdateCrosshair = onUpdateCrosshair,
            onOpenOverlayPermissionSettings = onOpenOverlayPermissionSettings
        )

        // 3. Auto Game Settings Advisor Card
        AutoSettingsAdvisorCard(
            recommendation = autoSettingsRecommendation,
            onSelectMode = onSelectAdvisorPreset,
            onApplyRecommendedTools = onApplyAdvisorPreset
        )

        // 4. GFX Resolution & Touch Sampling Rate Card
        GfxAndTouchTunerCard(
            gfxResolution = gfxResolution,
            touchSamplingRate = touchSamplingRate,
            lowEndConfig = lowEndConfig,
            fpsPillVisible = fpsPillVisible,
            isSystemFloatingBarRunning = isSystemFloatingBarRunning,
            onSelectResolution = onSelectResolution,
            onSelectTouchRate = onSelectTouchRate,
            onUpdateLowEnd = onUpdateLowEnd,
            onToggleFpsPill = onToggleFpsPill,
            onLaunchSystemFloatingSidebar = onLaunchSystemFloatingSidebar
        )

        // 5. Smart Thermal Control Card
        SmartThermalModeCard(
            thermalStatus = smartThermalStatus,
            onSelectMode = onSelectSmartThermalMode
        )

        // 6. Gaming Environment & Network Card
        GameBoxEnvironmentCard(
            lowEndConfig = lowEndConfig,
            onUpdateLowEnd = onUpdateLowEnd,
            onTestNetworkPing = onTestNetworkPing
        )

        // 7. Custom App Icon Studio Card
        CustomAppIconStudioCard(
            defaultDrawableRes = R.drawable.ic_launcher_foreground
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CrosshairStudioCard(
    config: CrosshairConfig,
    canDrawOverlays: Boolean,
    onUpdateCrosshair: ((CrosshairConfig) -> CrosshairConfig) -> Unit,
    onOpenOverlayPermissionSettings: () -> Unit
) {
    val colorOptions = listOf(
        0xFFFF1744L to "أحمر ليزر",
        0xFF00E676L to "أخضر نيون",
        0xFF00E5FFL to "سماوي سيبراني",
        0xFFFFEA00L to "أصفر ذهبي",
        0xFFFFFFFFL to "أبيض نقي"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("crosshair_studio_card"),
        shape = CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp, topEnd = 8.dp, bottomStart = 8.dp),
        color = GunmetalCard,
        border = BorderStroke(1.5.dp, CrimsonRed.copy(alpha = 0.75f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CutCornerShape(10.dp))
                            .background(CrimsonRed.copy(alpha = 0.16f))
                            .border(1.dp, CrimsonRed, CutCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = CrimsonRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "استوديو مؤشر التصويب العائم (Crosshair Overlay)",
                            style = MaterialTheme.typography.titleSmall,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "يظهر فوق الألعاب في منتصف الشاشة بدون أن يمنع لمس اللعبة",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = config.enabled,
                    onCheckedChange = { enabled ->
                        onUpdateCrosshair { it.copy(enabled = enabled) }
                    },
                    modifier = Modifier.testTag("crosshair_master_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = CrimsonRed
                    )
                )
            }

            // Live Crosshair Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianBlack)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                CrosshairCanvasView(config = config)

                Text(
                    text = "${config.style.titleAr} • ${config.sizeDp.roundToInt()}dp",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 10.sp,
                    color = SilverMist,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }

            // Crosshair Style Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CrosshairStyle.entries.forEach { style ->
                    val selected = config.style == style
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) CrimsonRed.copy(alpha = 0.2f) else ObsidianSurface)
                            .border(
                                1.dp,
                                if (selected) CrimsonRed else CarbonBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onUpdateCrosshair { it.copy(style = style) } }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = style.titleAr,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) TitaniumWhite else SilverMist,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Color Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "لون المؤشر:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    colorOptions.forEach { (hex, name) ->
                        val selected = config.colorHex == hex
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .border(
                                    width = if (selected) 2.5.dp else 1.dp,
                                    color = if (selected) TitaniumWhite else CarbonBorder,
                                    shape = CircleShape
                                )
                                .clickable { onUpdateCrosshair { it.copy(colorHex = hex) } }
                        )
                    }
                }
            }

            // Size & Opacity Sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "حجم المؤشر وشفافيته:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist
                )
                Text(
                    text = "${config.sizeDp.roundToInt()} DP • ${(config.opacity * 100).roundToInt()}%",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 11.sp,
                    color = CrimsonRed,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = config.sizeDp,
                onValueChange = { sz -> onUpdateCrosshair { it.copy(sizeDp = sz) } },
                valueRange = 16f..56f,
                colors = SliderDefaults.colors(
                    thumbColor = CrimsonRed,
                    activeTrackColor = CrimsonRed
                )
            )

            // Reset Offset Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "إزاحة المركز: X=${config.offsetXDp.roundToInt()}dp, Y=${config.offsetYDp.roundToInt()}dp",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 10.sp,
                    color = SilverMist
                )
                OutlinedButton(
                    onClick = {
                        onUpdateCrosshair { it.copy(offsetXDp = 0f, offsetYDp = 0f) }
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "توسيط",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                }
            }

            if (!canDrawOverlays) {
                OutlinedButton(
                    onClick = onOpenOverlayPermissionSettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MoltenAmber)
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = MoltenAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "منح إذن الظهور فوق التطبيقات لعرض المؤشر داخل الألعاب",
                        style = MaterialTheme.typography.bodySmall,
                        color = MoltenAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun GfxAndTouchTunerCard(
    gfxResolution: GfxResolutionPreset,
    touchSamplingRate: TouchSamplingRate,
    lowEndConfig: LowEndDeviceConfig,
    fpsPillVisible: Boolean,
    isSystemFloatingBarRunning: Boolean,
    onSelectResolution: (GfxResolutionPreset) -> Unit,
    onSelectTouchRate: (TouchSamplingRate) -> Unit,
    onUpdateLowEnd: ((LowEndDeviceConfig) -> LowEndDeviceConfig) -> Unit,
    onToggleFpsPill: (Boolean) -> Unit,
    onLaunchSystemFloatingSidebar: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gfx_and_touch_tuner_card"),
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
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "مُحسّن الرسوميات واستجابة اللمس (GFX & Touch Engine)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تخصيص دقة الرندر وسرعة استجابة اللمس وتخفيف الحمل للأجهزة الضعيفة",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist,
                        fontSize = 11.sp
                    )
                }
            }

            // Resolution Presets
            Text(
                text = "دقة الرندر الموصى بها:",
                style = MaterialTheme.typography.bodySmall,
                color = SilverMist
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                GfxResolutionPreset.entries.forEach { preset ->
                    val selected = preset == gfxResolution
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) CyberCyan.copy(alpha = 0.16f) else ObsidianSurface)
                            .border(
                                1.dp,
                                if (selected) CyberCyan else CarbonBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectResolution(preset) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = preset.label,
                                fontFamily = OrbitronFontFamily,
                                fontSize = 11.sp,
                                color = TitaniumWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${preset.scaleText} (توفير GPU: ${preset.gpuLoadReductionPercent}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = SilverMist,
                                fontSize = 10.sp
                            )
                        }
                        if (selected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Touch Sampling Rate
            Text(
                text = "معدل استجابة اللمس (Touch Sampling):",
                style = MaterialTheme.typography.bodySmall,
                color = SilverMist
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TouchSamplingRate.entries.forEach { rate ->
                    val selected = rate == touchSamplingRate
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) MatrixGreen.copy(alpha = 0.18f) else ObsidianSurface)
                            .border(
                                1.dp,
                                if (selected) MatrixGreen else CarbonBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectTouchRate(rate) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = if (selected) MatrixGreen else SilverMist,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${rate.hz}Hz",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) MatrixGreen else TitaniumWhite
                            )
                            Text(
                                text = "${rate.responseMs}ms",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 9.sp,
                                color = SilverMist
                            )
                        }
                    }
                }
            }

            // Low-End Boost Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObsidianSurface)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = MoltenAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "تسريع الأجهزة الضعيفة والمتوسطة (Low-End Stabilizer)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تخفيف المؤثرات الثقيلة وتثبيت الفريمات لمنع التقطيع المفاجئ",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist,
                            fontSize = 10.sp
                        )
                    }
                }
                Switch(
                    checked = lowEndConfig.lowEndBoostEnabled,
                    onCheckedChange = { v ->
                        onUpdateLowEnd { it.copy(lowEndBoostEnabled = v, disableHeavyShaders = v) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = MoltenAmber
                    )
                )
            }

            // Floating Sidebar + FPS Counter Quick Launch Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLaunchSystemFloatingSidebar,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = CutCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSystemFloatingBarRunning) MatrixGreen else CyberCyan,
                        contentColor = ObsidianBlack
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSystemFloatingBarRunning) "الشريط الجانبي نشط" else "تفعيل الشريط الجانبي",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { onToggleFpsPill(!fpsPillVisible) },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = CutCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (fpsPillVisible) MatrixGreen else CarbonBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = if (fpsPillVisible) MatrixGreen else SilverMist,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (fpsPillVisible) "عداد FPS: نشط" else "إظهار عداد FPS",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (fpsPillVisible) MatrixGreen else TitaniumWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
