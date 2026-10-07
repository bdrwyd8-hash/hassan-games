package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.DoNotTouch
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwipeLeft
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WifiTethering
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdvisorPresetMode
import com.example.model.AudioRadarPreset
import com.example.model.AutoSettingsRecommendation
import com.example.model.CrosshairColorOption
import com.example.model.CrosshairConfig
import com.example.model.CrosshairStyle
import com.example.model.EdgeSidebarConfig
import com.example.model.LowEndOptimizerConfig
import com.example.model.NotificationShieldState
import com.example.model.ScreenVisionFilter
import com.example.model.SmartThermalMode
import com.example.model.SmartThermalStatus
import com.example.model.VoiceModPreset
import com.example.ui.components.AutoSettingsAdvisorCard
import com.example.ui.components.CrosshairCanvasView
import com.example.ui.components.GameBoxEnvironmentCard
import com.example.ui.components.SmartThermalAndShieldCard
import com.example.ui.components.StudioVoiceChangerCard
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
    edgeSidebarConfig: EdgeSidebarConfig,
    lowEndConfig: LowEndOptimizerConfig,
    canDrawSystemOverlays: Boolean,
    isSystemOverlayRunning: Boolean,
    onUpdateCrosshair: ((CrosshairConfig) -> CrosshairConfig) -> Unit,
    onUpdateEdgeSidebar: ((EdgeSidebarConfig) -> EdgeSidebarConfig) -> Unit,
    onUpdateLowEnd: ((LowEndOptimizerConfig) -> LowEndOptimizerConfig) -> Unit,
    onOpenInAppSidebarPreview: () -> Unit,
    onEnableSystemFloatingSidebar: () -> Unit,
    onSelectAudioRadar: (AudioRadarPreset) -> Unit,
    onSelectVoiceMod: (VoiceModPreset) -> Unit,
    onOpenAccessibilityForOverlay: () -> Unit,
    isLiveMicActive: Boolean = false,
    isRecordingClip: Boolean = false,
    isPlayingClip: Boolean = false,
    micInputLevel: Float = 0f,
    voiceStatusText: String = "",
    noiseGateEnabled: Boolean = true,
    hasMicPermission: () -> Boolean = { false },
    onToggleLiveMic: () -> Unit = {},
    onStartOrStopClipTest: () -> Unit = {},
    onReplayRecordedClip: () -> Unit = {},
    onToggleNoiseGate: (Boolean) -> Unit = {},
    selectedDemoPhraseIndex: Int = 0,
    onPlayReadyDemoWithoutMic: (VoiceModPreset, Int) -> Unit = { preset, _ -> onSelectVoiceMod(preset) },
    smartThermalStatus: SmartThermalStatus = SmartThermalStatus(),
    notificationShieldState: NotificationShieldState = NotificationShieldState(),
    onSelectThermalMode: (SmartThermalMode) -> Unit = {},
    onToggleNotificationShield: () -> Unit = {},
    onOpenDndPermissionSettings: () -> Unit = {},
    onToggleHudMetric: (fps: Boolean?, temp: Boolean?, ram: Boolean?, magnifier: Boolean?) -> Unit = { _, _, _, _ -> },
    onUpdateMagnifierZoom: (Float) -> Unit = {},
    autoSettingsRecommendation: AutoSettingsRecommendation = AutoSettingsRecommendation(),
    onSelectAdvisorMode: (AdvisorPresetMode) -> Unit = {},
    onApplyAdvisorRecommendations: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("arsenal_gfx_list"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 0. Auto Settings Advisor Card (Hardware-driven Graphics & FPS Advisor)
        item {
            AutoSettingsAdvisorCard(
                recommendation = autoSettingsRecommendation,
                onSelectMode = onSelectAdvisorMode,
                onApplyRecommendedTools = onApplyAdvisorRecommendations
            )
        }

        // 0b. Smart Thermal Mode + Notification Shield + Floating HUD & Magnifier
        item {
            SmartThermalAndShieldCard(
                thermalStatus = smartThermalStatus,
                shieldState = notificationShieldState,
                lowEndConfig = lowEndConfig,
                onSelectThermalMode = onSelectThermalMode,
                onToggleNotificationShield = onToggleNotificationShield,
                onOpenDndPermissionSettings = onOpenDndPermissionSettings,
                onToggleHudMetric = onToggleHudMetric,
                onUpdateMagnifierZoom = onUpdateMagnifierZoom
            )
        }

        // 1. Floating Edge-Swipe Game Bar Card (Game Genie / Game Turbo Sidebar)
        item {
            FloatingEdgeSidebarSettingsCard(
                sidebarConfig = edgeSidebarConfig,
                canDrawSystemOverlays = canDrawSystemOverlays,
                isSystemOverlayRunning = isSystemOverlayRunning,
                onUpdateEdgeSidebar = onUpdateEdgeSidebar,
                onOpenInAppSidebarPreview = onOpenInAppSidebarPreview,
                onEnableSystemFloatingSidebar = onEnableSystemFloatingSidebar
            )
        }

        // 2. Studio HD Microphone Voice Changer Card (Robot / Squirrel / Female / Male)
        item {
            StudioVoiceChangerCard(
                activeVoicePreset = lowEndConfig.voiceModPreset,
                isLiveMicActive = isLiveMicActive,
                isRecordingClip = isRecordingClip,
                isPlayingClip = isPlayingClip,
                micInputLevel = micInputLevel,
                voiceStatusText = voiceStatusText,
                noiseGateEnabled = noiseGateEnabled,
                hasMicPermission = hasMicPermission,
                onSelectVoicePreset = onSelectVoiceMod,
                onToggleLiveMic = onToggleLiveMic,
                onStartOrStopClipTest = onStartOrStopClipTest,
                onReplayRecordedClip = onReplayRecordedClip,
                onToggleNoiseGate = onToggleNoiseGate,
                selectedDemoPhraseIndex = selectedDemoPhraseIndex,
                onPlayReadyDemoWithoutMic = onPlayReadyDemoWithoutMic
            )
        }

        // 3. Multi-Brand Screen Vision Filters (ROG Night Hunter / Black Shark / POCO HDR)
        item {
            ScreenVisionFiltersCard(
                activeFilter = lowEndConfig.visionFilter,
                onSelectFilter = { filter ->
                    onUpdateLowEnd { it.copy(visionFilter = filter) }
                }
            )
        }

        // 3. 3D Footsteps Audio Radar & Voice Changer Card (POCO Game Turbo & RedMagic)
        item {
            AudioRadarAndVoiceChangerCard(
                activeAudioPreset = lowEndConfig.audioRadarPreset,
                activeVoicePreset = lowEndConfig.voiceModPreset,
                onSelectAudioRadar = onSelectAudioRadar,
                onSelectVoiceMod = onSelectVoiceMod
            )
        }

        // 4. Pro Crosshair Studio Card
        item {
            ProCrosshairStudioCard(
                config = crosshairConfig,
                onUpdateCrosshair = onUpdateCrosshair,
                onOpenAccessibilityForOverlay = onOpenAccessibilityForOverlay
            )
        }

        // 5. GG GameBox Pro Environment & DNS Optimizer Card
        item {
            GameBoxEnvironmentCard(
                lowEndConfig = lowEndConfig,
                onUpdateLowEnd = onUpdateLowEnd
            )
        }

        // 6. Low-End Resolution & Touch Sampling Booster Card
        item {
            LowEndResolutionAndTouchCard(
                lowEndConfig = lowEndConfig,
                onUpdateLowEnd = onUpdateLowEnd
            )
        }

        // 6. GPU, Mistouch Shield, Bypass Charging & Black-Screen AFK Tweaks Card
        item {
            GpuAndSystemTweaksCard(
                lowEndConfig = lowEndConfig,
                onUpdateLowEnd = onUpdateLowEnd
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FloatingEdgeSidebarSettingsCard(
    sidebarConfig: EdgeSidebarConfig,
    canDrawSystemOverlays: Boolean,
    isSystemOverlayRunning: Boolean,
    onUpdateEdgeSidebar: ((EdgeSidebarConfig) -> EdgeSidebarConfig) -> Unit,
    onOpenInAppSidebarPreview: () -> Unit,
    onEnableSystemFloatingSidebar: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp),
        color = GunmetalCard,
        border = BorderStroke(1.5.dp, CyberCyan.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .size(44.dp)
                            .clip(CutCornerShape(10.dp))
                            .background(CyberCyan.copy(alpha = 0.18f))
                            .border(1.dp, CyberCyan, CutCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwipeLeft,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "الشريط الجانبي العائم فوق الألعاب (Edge Game Bar)",
                            style = MaterialTheme.typography.titleMedium,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "اسحب من جنب الجوال أثناء اللعب لفتح لوحة التقوية بدون الخروج من اللعبة!",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberCyan
                        )
                    }
                }
                Switch(
                    checked = sidebarConfig.enabledInApp,
                    onCheckedChange = { enabled ->
                        onUpdateEdgeSidebar { it.copy(enabledInApp = enabled) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = CyberCyan
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Side Edge Selector (Right vs Left)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (sidebarConfig.isRightEdge) CyberCyan.copy(alpha = 0.2f) else ObsidianSurface)
                        .border(
                            1.dp,
                            if (sidebarConfig.isRightEdge) CyberCyan else CarbonBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onUpdateEdgeSidebar { it.copy(isRightEdge = true) } }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "مقبض السحب على اليمين",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (sidebarConfig.isRightEdge) CyberCyan else SilverMist,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!sidebarConfig.isRightEdge) CyberCyan.copy(alpha = 0.2f) else ObsidianSurface)
                        .border(
                            1.dp,
                            if (!sidebarConfig.isRightEdge) CyberCyan else CarbonBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onUpdateEdgeSidebar { it.copy(isRightEdge = false) } }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "مقبض السحب على اليسار",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (!sidebarConfig.isRightEdge) CyberCyan else SilverMist,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenInAppSidebarPreview,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("preview_edge_sidebar_btn"),
                    shape = CutCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = ObsidianBlack
                    )
                ) {
                    Text(
                        text = "تجربة فتح الشريط الآن",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onEnableSystemFloatingSidebar,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("system_overlay_sidebar_btn"),
                    shape = CutCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSystemOverlayRunning) MatrixGreen else CrimsonRed
                    )
                ) {
                    Icon(
                        imageVector = if (canDrawSystemOverlays) Icons.Default.Layers else Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = if (isSystemOverlayRunning) MatrixGreen else CrimsonRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            isSystemOverlayRunning -> "نشط فوق التطبيقات ✓"
                            canDrawSystemOverlays -> "تثبيت فوق الألعاب"
                            else -> "إذن الظهور العائم"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSystemOverlayRunning) MatrixGreen else TitaniumWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenVisionFiltersCard(
    activeFilter: ScreenVisionFilter,
    onSelectFilter: (ScreenVisionFilter) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = GunmetalCard,
        border = BorderStroke(1.dp, MatrixGreen.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = MatrixGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "فلاتر الرؤية الليلية وكشف الأعداء (ROG Scout & Black Shark)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تفتيح الزوايا المظلمة والعشب وإبراز حركة الأعداء بوضوح عالٍ",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ScreenVisionFilter.entries.forEach { filter ->
                    val selected = filter == activeFilter
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectFilter(filter) }
                            .testTag("vision_filter_${filter.id}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) MatrixGreen.copy(alpha = 0.16f) else ObsidianSurface,
                        border = BorderStroke(1.dp, if (selected) MatrixGreen else CarbonBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = filter.arabicName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TitaniumWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = filter.brandOrigin,
                                        fontFamily = OrbitronFontFamily,
                                        fontSize = 9.sp,
                                        color = if (selected) MatrixGreen else CyberCyan
                                    )
                                }
                                Text(
                                    text = filter.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverMist,
                                    fontSize = 11.sp
                                )
                            }

                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MatrixGreen)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ON",
                                        fontFamily = OrbitronFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ObsidianBlack
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioRadarAndVoiceChangerCard(
    activeAudioPreset: AudioRadarPreset,
    activeVoicePreset: VoiceModPreset,
    onSelectAudioRadar: (AudioRadarPreset) -> Unit,
    onSelectVoiceMod: (VoiceModPreset) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = GunmetalCard,
        border = BorderStroke(1.dp, MoltenAmber.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = MoltenAmber,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "رادار تضخيم صوت الخطوات 3D ومُغيّر الصوت (POCO & RedMagic)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "معالج صوتي حقيقي لتضخيم خطوات الأقدام والطلقات + فلاتر نبرة المايك",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "وضع تضخيم الصوت التكتيكي (Acoustic Footsteps Radar):",
                style = MaterialTheme.typography.bodySmall,
                color = SilverMist
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AudioRadarPreset.entries.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { preset ->
                            val selected = preset == activeAudioPreset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) MoltenAmber.copy(alpha = 0.2f) else ObsidianSurface)
                                    .border(
                                        1.dp,
                                        if (selected) MoltenAmber else CarbonBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectAudioRadar(preset) }
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = preset.arabicName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (selected) MoltenAmber else TitaniumWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (preset.boostDb > 0) "+${preset.boostDb} dB Boost" else "Standard",
                                        fontFamily = OrbitronFontFamily,
                                        fontSize = 9.sp,
                                        color = SilverMist
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = ElectricPurple,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "مُغيّر الصوت أثناء الدردشة (Voice Changer - اضغط للتجربة):",
                    style = MaterialTheme.typography.bodySmall,
                    color = TitaniumWhite,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VoiceModPreset.entries.forEach { voice ->
                    val selected = voice == activeVoicePreset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) ElectricPurple.copy(alpha = 0.25f) else ObsidianSurface)
                            .border(
                                1.dp,
                                if (selected) ElectricPurple else CarbonBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectVoiceMod(voice) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = voice.arabicName.substringBefore(" ("),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selected) ElectricPurple else SilverMist,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProCrosshairStudioCard(
    config: CrosshairConfig,
    onUpdateCrosshair: ((CrosshairConfig) -> CrosshairConfig) -> Unit,
    onOpenAccessibilityForOverlay: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp),
        color = GunmetalCard,
        border = BorderStroke(1.5.dp, config.colorOption.color.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = config.colorOption.color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "مؤشر التصويب الاحترافي (Pro Crosshair)",
                            style = MaterialTheme.typography.titleMedium,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ثبّت إيم القناص في منتصف الشاشة بدقة ليزر للسلاح بدون سكوب",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist
                        )
                    }
                }
                Switch(
                    checked = config.enabledInApp,
                    onCheckedChange = { enabled ->
                        onUpdateCrosshair { it.copy(enabledInApp = enabled) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = config.colorOption.color
                    ),
                    modifier = Modifier.testTag("arsenal_crosshair_switch")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianBlack)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    drawLine(CarbonBorder.copy(alpha = 0.45f), Offset(0f, cy), Offset(size.width, cy), 1f)
                    drawLine(CarbonBorder.copy(alpha = 0.45f), Offset(cx, 0f), Offset(cx, size.height), 1f)
                    drawCircle(
                        color = CarbonBorder.copy(alpha = 0.45f),
                        radius = 45.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                CrosshairCanvasView(config = config)

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(ObsidianSurface.copy(alpha = 0.85f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = config.style.arabicName,
                        style = MaterialTheme.typography.bodySmall,
                        color = config.colorOption.color,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "اختر شكل المؤشر (8 أشكال ريد ماجيك):",
                style = MaterialTheme.typography.bodySmall,
                color = SilverMist
            )
            Spacer(modifier = Modifier.height(6.dp))

            val styles = CrosshairStyle.entries
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                styles.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { style ->
                            val isSelected = config.style == style
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) config.colorOption.color.copy(alpha = 0.2f)
                                        else ObsidianSurface
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) config.colorOption.color else CarbonBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        onUpdateCrosshair { it.copy(style = style) }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                    .testTag("crosshair_style_${style.id}")
                            ) {
                                Text(
                                    text = style.arabicName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) TitaniumWhite else SilverMist,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "لون الليزر:",
                style = MaterialTheme.typography.bodySmall,
                color = SilverMist
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CrosshairColorOption.entries.forEach { colorOpt ->
                    val selected = config.colorOption == colorOpt
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colorOpt.color)
                            .border(
                                width = if (selected) 3.dp else 1.dp,
                                color = if (selected) TitaniumWhite else ObsidianBlack,
                                shape = CircleShape
                            )
                            .clickable {
                                onUpdateCrosshair { it.copy(colorOption = colorOpt) }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "حجم المؤشر:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist
                )
                Text(
                    text = "${config.sizeDp.roundToInt()} DP",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 11.sp,
                    color = config.colorOption.color
                )
            }
            Slider(
                value = config.sizeDp,
                onValueChange = { sz -> onUpdateCrosshair { it.copy(sizeDp = sz) } },
                valueRange = 18f..64f,
                colors = SliderDefaults.colors(
                    thumbColor = config.colorOption.color,
                    activeTrackColor = config.colorOption.color
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObsidianSurface)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "إظهار المؤشر عائماً فوق الألعاب الخارجية",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "يعرض مؤشر التصويب في منتصف الشاشة فوق ببجي وفري فاير وكود",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist
                    )
                }
                Switch(
                    checked = config.systemOverlayEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateCrosshair { it.copy(systemOverlayEnabled = enabled) }
                        if (enabled) {
                            onOpenAccessibilityForOverlay()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = CrimsonRed
                    )
                )
            }
        }
    }
}

@Composable
private fun LowEndResolutionAndTouchCard(
    lowEndConfig: LowEndOptimizerConfig,
    onUpdateLowEnd: ((LowEndOptimizerConfig) -> LowEndOptimizerConfig) -> Unit
) {
    val resPresets = listOf(
        "540p Ultra FPS (أقصى فريمات للأجهزة الضعيفة جداً)",
        "720p Esports (موصى به للأجهزة الضعيفة)",
        "900p Sharp HD (توازن الدقة والسرعة)",
        "1080p Native FHD (الدقة الكاملة)"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = GunmetalCard,
        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DisplaySettings,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "مُحسّن الجرافيك واللمس للأجهزة الضعيفة (GFX & Touch)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تقليل عبء المعالج الرسومي GPU ورفع استجابة اللمس الفورية",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ملف تحجيم الدقة والفريمات (Resolution & Frame Target):",
                style = MaterialTheme.typography.bodySmall,
                color = SilverMist
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                resPresets.forEach { preset ->
                    val selected = lowEndConfig.resolutionScalePreset == preset
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) CyberCyan.copy(alpha = 0.18f) else ObsidianSurface)
                            .border(
                                1.dp,
                                if (selected) CyberCyan else CarbonBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onUpdateLowEnd { it.copy(resolutionScalePreset = preset) }
                            }
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selected) TitaniumWhite else SilverMist,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (selected) {
                                Text(
                                    text = "ACTIVE",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 10.sp,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = MoltenAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "معدل استجابة اللمس (Touch Sampling):",
                        style = MaterialTheme.typography.bodySmall,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "${lowEndConfig.touchSamplingRateHz}Hz",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 12.sp,
                    color = MoltenAmber,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(240, 360, 480, 960).forEach { hz ->
                    val selected = lowEndConfig.touchSamplingRateHz == hz
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) MoltenAmber.copy(alpha = 0.22f) else ObsidianSurface)
                            .border(
                                1.dp,
                                if (selected) MoltenAmber else CarbonBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onUpdateLowEnd { it.copy(touchSamplingRateHz = hz) }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${hz}Hz",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) MoltenAmber else SilverMist
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GpuAndSystemTweaksCard(
    lowEndConfig: LowEndOptimizerConfig,
    onUpdateLowEnd: ((LowEndOptimizerConfig) -> LowEndOptimizerConfig) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = GunmetalCard,
        border = BorderStroke(1.dp, CarbonBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "مميزات الجوالات الاحترافية (ROG / Samsung / Black Shark / POCO)",
                style = MaterialTheme.typography.titleSmall,
                color = TitaniumWhite,
                fontWeight = FontWeight.Bold
            )

            SystemTweakRow(
                icon = Icons.Default.DoNotTouch,
                title = "منع اللمس الخاطئ للحواف والإيماءات (Mistouch Shield)",
                subtitle = "يمنع خروج اللعبة بالخطأ عند لمس راحة اليد لأطراف الشاشة أثناء القتال",
                checked = lowEndConfig.mistouchPrevention,
                accent = ElectricPurple,
                onCheckedChange = { v -> onUpdateLowEnd { it.copy(mistouchPrevention = v) } }
            )

            SystemTweakRow(
                icon = Icons.Default.BatterySaver,
                title = "الحماية الحرارية أثناء الشحن (Bypass Thermal Guard)",
                subtitle = "يقلل الحرارة الناتجة عن الشحن أثناء اللعب لحماية البطارية والمعالج من الهبوط",
                checked = lowEndConfig.bypassChargingGuard,
                accent = MatrixGreen,
                onCheckedChange = { v -> onUpdateLowEnd { it.copy(bypassChargingGuard = v) } }
            )

            SystemTweakRow(
                icon = Icons.Default.Speed,
                title = "تعطيل تنعيم الحواف الثقيل (Disable 4x MSAA)",
                subtitle = "يرفع الفريمات بنسبة تصل إلى 25% في الأجهزة الضعيفة والمتوسطة",
                checked = lowEndConfig.gpuForce4xMsaaOff,
                accent = MatrixGreen,
                onCheckedChange = { v -> onUpdateLowEnd { it.copy(gpuForce4xMsaaOff = v) } }
            )

            SystemTweakRow(
                icon = Icons.Default.DisplaySettings,
                title = "تخفيف الظلال والضباب الدخاني (Shadow Downscale)",
                subtitle = "يمنع هبوط الفريمات المفاجئ (Frame Drop) أثناء المواجهات القريبة",
                checked = lowEndConfig.shadowDownscale,
                accent = CyberCyan,
                onCheckedChange = { v -> onUpdateLowEnd { it.copy(shadowDownscale = v) } }
            )

            SystemTweakRow(
                icon = Icons.Default.WifiTethering,
                title = "أولوية قصوى لحزم الشبكة (Zero-Lag Network Socket)",
                subtitle = "يمنع تحديثات الخلفية من سحب الإنترنت أثناء اللعب لتثبيت البينج",
                checked = lowEndConfig.zeroLagNetworkMode,
                accent = MoltenAmber,
                onCheckedChange = { v -> onUpdateLowEnd { it.copy(zeroLagNetworkMode = v) } }
            )

            SystemTweakRow(
                icon = Icons.Default.NotificationsOff,
                title = "حظر الإشعارات والمكالمات المزعجة (DND Gaming Shield)",
                subtitle = "يمنع ظهور الإشعارات العلوية التي تغطي أزرار اللعب",
                checked = lowEndConfig.blockHeadsUpNotifications,
                accent = CrimsonRed,
                onCheckedChange = { v -> onUpdateLowEnd { it.copy(blockHeadsUpNotifications = v) } }
            )
        }
    }
}

@Composable
private fun SystemTweakRow(
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
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TitaniumWhite,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist,
                    fontSize = 11.sp
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
