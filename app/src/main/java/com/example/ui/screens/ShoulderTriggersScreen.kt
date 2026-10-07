package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsAccessibility
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClonedButtonsConfig
import com.example.model.ClonedTouchButton
import com.example.model.CrosshairConfig
import com.example.model.ShoulderTriggerConfig
import com.example.model.TriggerFireMode
import com.example.service.TriggerButtonType
import com.example.service.TriggerEventBus
import com.example.ui.components.ClonedButtonsStudioCard
import com.example.ui.components.CrosshairCanvasView
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
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ShoulderTriggersScreen(
    triggerConfig: ShoulderTriggerConfig,
    crosshairConfig: CrosshairConfig,
    onUpdateConfig: ((ShoulderTriggerConfig) -> ShoulderTriggerConfig) -> Unit,
    onSimulateHardwareTrigger: (TriggerButtonType, Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    clonedButtonsConfig: ClonedButtonsConfig = ClonedButtonsConfig(),
    onUpdateClonedButtonsConfig: ((ClonedButtonsConfig) -> ClonedButtonsConfig) -> Unit = {},
    onToggleClonedButtonsOverlay: () -> Unit = {},
    onSimulateClonedTap: (ClonedTouchButton, Boolean) -> Unit = { _, _ -> }
) {
    val l1Pressed by TriggerEventBus.l1Pressed.collectAsState()
    val r1Pressed by TriggerEventBus.r1Pressed.collectAsState()
    val totalL1Shots by TriggerEventBus.totalL1Shots.collectAsState()
    val totalR1Shots by TriggerEventBus.totalR1Shots.collectAsState()
    val isAccessibilityRunning by TriggerEventBus.isAccessibilityServiceRunning.collectAsState()

    var isLiveRangeMode by remember { mutableStateOf(false) }
    var isScopedAds by remember { mutableStateOf(false) }
    var targetHitsCount by remember { mutableIntStateOf(0) }
    var lastHitPopupMs by remember { mutableLongStateOf(0L) }

    // Listen to fireEvents from physical Volume Up / Down or simulated triggers
    LaunchedEffect(Unit) {
        TriggerEventBus.fireEvents.collectLatest { event ->
            if (event.button == TriggerButtonType.L1_VOLUME_UP && event.isPressed) {
                isScopedAds = !isScopedAds
            }
            if (event.button == TriggerButtonType.R1_VOLUME_DOWN && event.isPressed) {
                targetHitsCount += 1
                lastHitPopupMs = System.currentTimeMillis()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("shoulder_triggers_list"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header & Master Toggle Card
        item {
            TriggerMasterHeaderCard(
                triggerConfig = triggerConfig,
                isAccessibilityRunning = isAccessibilityRunning,
                onToggleEnabled = { enabled -> onUpdateConfig { it.copy(enabled = enabled) } },
                onOpenAccessibilitySettings = onOpenAccessibilitySettings
            )
        }

        // 2. NEW: Custom Movable Cloned Touch Buttons Studio (C1 - C4)
        item {
            ClonedButtonsStudioCard(
                config = clonedButtonsConfig,
                isAccessibilityRunning = isAccessibilityRunning,
                onUpdateConfig = onUpdateClonedButtonsConfig,
                onToggleSystemOverlay = onToggleClonedButtonsOverlay,
                onSimulateClonedTap = onSimulateClonedTap,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings
            )
        }

        // 3. Interactive L1 / R1 Coordinate Mapper & Live Battle Range
        item {
            TriggerInteractiveArenaCard(
                triggerConfig = triggerConfig,
                crosshairConfig = crosshairConfig,
                isLiveRangeMode = isLiveRangeMode,
                onToggleArenaMode = { isLiveRangeMode = it },
                l1Pressed = l1Pressed,
                r1Pressed = r1Pressed,
                isScopedAds = isScopedAds,
                totalL1Shots = totalL1Shots,
                totalR1Shots = totalR1Shots,
                targetHitsCount = targetHitsCount,
                lastHitPopupMs = lastHitPopupMs,
                onMoveL1 = { x, y ->
                    onUpdateConfig {
                        it.copy(
                            l1XRatio = x.coerceIn(0.06f, 0.94f),
                            l1YRatio = y.coerceIn(0.10f, 0.90f)
                        )
                    }
                },
                onMoveR1 = { x, y ->
                    onUpdateConfig {
                        it.copy(
                            r1XRatio = x.coerceIn(0.06f, 0.94f),
                            r1YRatio = y.coerceIn(0.10f, 0.90f)
                        )
                    }
                },
                onSimulateHardwareTrigger = onSimulateHardwareTrigger,
                onResetCounters = {
                    TriggerEventBus.resetShotCounters()
                    targetHitsCount = 0
                    isScopedAds = false
                },
                onApplyGamePreset = { l1x, l1y, r1x, r1y ->
                    onUpdateConfig {
                        it.copy(l1XRatio = l1x, l1YRatio = l1y, r1XRatio = r1x, r1YRatio = r1y)
                    }
                }
            )
        }

        // 3. L1 (Volume Up) Hardware Configuration Card
        item {
            SingleShoulderConfigCard(
                buttonTitle = "زر الكتف الأيسر L1 (زر رفع الصوت Volume +)",
                subtitle = "مثالي لفتح السكوب الفوري (ADS) أو وضع الثلج أو التصويب السريع",
                accentColor = CyberCyan,
                icon = Icons.Default.VolumeUp,
                currentMode = triggerConfig.l1Mode,
                burstRps = triggerConfig.l1BurstRps,
                xPercent = (triggerConfig.l1XRatio * 100).roundToInt(),
                yPercent = (triggerConfig.l1YRatio * 100).roundToInt(),
                onSelectMode = { mode -> onUpdateConfig { it.copy(l1Mode = mode) } },
                onChangeRps = { rps -> onUpdateConfig { it.copy(l1BurstRps = rps) } },
                tagPrefix = "l1"
            )
        }

        // 4. R1 (Volume Down) Hardware Configuration Card
        item {
            SingleShoulderConfigCard(
                buttonTitle = "زر الكتف الأيمن R1 (زر خفض الصوت Volume -)",
                subtitle = "مثالي لإطلاق النار المتتالي (Rapid Fire) أو الهيدشوت السريع",
                accentColor = CrimsonRed,
                icon = Icons.Default.VolumeDown,
                currentMode = triggerConfig.r1Mode,
                burstRps = triggerConfig.r1BurstRps,
                xPercent = (triggerConfig.r1XRatio * 100).roundToInt(),
                yPercent = (triggerConfig.r1YRatio * 100).roundToInt(),
                onSelectMode = { mode -> onUpdateConfig { it.copy(r1Mode = mode) } },
                onChangeRps = { rps -> onUpdateConfig { it.copy(r1BurstRps = rps) } },
                tagPrefix = "r1"
            )
        }

        // 5. Advanced RedMagic Trigger Macros & Haptics
        item {
            AdvancedTriggerMechanicsCard(
                triggerConfig = triggerConfig,
                onUpdateConfig = onUpdateConfig
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TriggerMasterHeaderCard(
    triggerConfig: ShoulderTriggerConfig,
    isAccessibilityRunning: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp),
        color = GunmetalCard,
        border = BorderStroke(
            1.5.dp,
            if (triggerConfig.enabled) CyberCyan else CarbonBorder
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
                            .size(46.dp)
                            .clip(CutCornerShape(10.dp))
                            .background(CyberCyan.copy(alpha = 0.18f))
                            .border(1.dp, CyberCyan, CutCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gamepad,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "تحويل أزرار الصوت إلى أزرار كتف L1 / R1",
                            style = MaterialTheme.typography.titleMedium,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "رفع الصوت = L1  •  خفض الصوت = R1 (نفس أزرار الريد ماجيك)",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberCyan
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
                    modifier = Modifier.testTag("master_trigger_switch")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // System-wide Accessibility Bridge Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = ObsidianSurface,
                border = BorderStroke(
                    1.dp,
                    if (isAccessibilityRunning) MatrixGreen.copy(alpha = 0.6f)
                    else MoltenAmber.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsAccessibility,
                            contentDescription = null,
                            tint = if (isAccessibilityRunning) MatrixGreen else MoltenAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isAccessibilityRunning)
                                    "محرك L1/R1 يعمل فوق جميع الألعاب الخارجية!"
                                else
                                    "شغّل أزرار الصوت الآن بالأسفل، أو فعّل الخدمة للألعاب الخارجية",
                                style = MaterialTheme.typography.bodySmall,
                                color = TitaniumWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "ملاحظة: أزرار رفع وخفض الصوت بهاتفك تعمل فوراً هنا بدون أي إعداد إضافي!",
                                style = MaterialTheme.typography.bodySmall,
                                color = SilverMist,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onOpenAccessibilitySettings,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, CyberCyan),
                        modifier = Modifier.testTag("open_accessibility_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "للألعاب الخارجية",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyan
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TriggerInteractiveArenaCard(
    triggerConfig: ShoulderTriggerConfig,
    crosshairConfig: CrosshairConfig,
    isLiveRangeMode: Boolean,
    onToggleArenaMode: (Boolean) -> Unit,
    l1Pressed: Boolean,
    r1Pressed: Boolean,
    isScopedAds: Boolean,
    totalL1Shots: Int,
    totalR1Shots: Int,
    targetHitsCount: Int,
    lastHitPopupMs: Long,
    onMoveL1: (Float, Float) -> Unit,
    onMoveR1: (Float, Float) -> Unit,
    onSimulateHardwareTrigger: (TriggerButtonType, Boolean) -> Unit,
    onResetCounters: () -> Unit,
    onApplyGamePreset: (Float, Float, Float, Float) -> Unit
) {
    val reduceThermalAnimations by TriggerEventBus.smartThermalStatus.collectAsState()
    val droneXRatio = if (isLiveRangeMode && !reduceThermalAnimations.reduceAnimationsActive) {
        val infiniteTransition = rememberInfiniteTransition(label = "drone_movement")
        val animated by infiniteTransition.animateFloat(
            initialValue = 0.25f,
            targetValue = 0.75f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "drone_x"
        )
        animated
    } else {
        0.5f
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = GunmetalCard,
        border = BorderStroke(1.dp, CarbonBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Mode Switcher Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isLiveRangeMode) CyberCyan.copy(alpha = 0.22f) else ObsidianSurface)
                        .border(
                            1.dp,
                            if (!isLiveRangeMode) CyberCyan else CarbonBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggleArenaMode(false) }
                        .padding(vertical = 8.dp)
                        .testTag("mode_mapping_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1. تخطيط وسحب مواقع L1 / R1",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (!isLiveRangeMode) CyberCyan else SilverMist
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isLiveRangeMode) CrimsonRed.copy(alpha = 0.25f) else ObsidianSurface)
                        .border(
                            1.dp,
                            if (isLiveRangeMode) CrimsonRed else CarbonBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggleArenaMode(true) }
                        .padding(vertical = 8.dp)
                        .testTag("mode_live_range_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "2. ميدان تجربة الأزرار الحي",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isLiveRangeMode) CrimsonRed else SilverMist
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Game Presets Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قوالب جاهزة:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist
                )
                PresetMiniChip("ببجي", onClick = { onApplyGamePreset(0.22f, 0.34f, 0.81f, 0.58f) })
                PresetMiniChip("فري فاير", onClick = { onApplyGamePreset(0.20f, 0.42f, 0.79f, 0.62f) })
                PresetMiniChip("كود موبايل", onClick = { onApplyGamePreset(0.25f, 0.32f, 0.82f, 0.54f) })
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "تصفير العداد",
                    tint = SilverMist,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onResetCounters() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive HUD & Battle Range Canvas Box
            val density = LocalDensity.current
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(255.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianBlack)
                    .border(
                        1.5.dp,
                        when {
                            r1Pressed -> CrimsonRed
                            l1Pressed -> CyberCyan
                            else -> CarbonBorder
                        },
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("trigger_mapping_canvas")
            ) {
                val canvasWidthPx = with(density) { maxWidth.toPx() }
                val canvasHeightPx = with(density) { maxHeight.toPx() }

                // Tactical Grid, Simulated Game HUD Buttons, and Moving Drone Target
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Cyber Grid Lines
                    val gridColor = CarbonBorder.copy(alpha = 0.35f)
                    for (i in 1..5) {
                        val y = (h / 6f) * i
                        drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                    }
                    for (i in 1..7) {
                        val x = (w / 8f) * i
                        drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                    }

                    // Simulated Game HUD Ghost Controls (Scope area on left, Fire area on right, Joystick bottom-left)
                    drawCircle(
                        color = SilverMist.copy(alpha = 0.12f),
                        radius = 36.dp.toPx(),
                        center = Offset(w * 0.18f, h * 0.76f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = CyberCyan.copy(alpha = 0.14f),
                        radius = 24.dp.toPx(),
                        center = Offset(w * 0.22f, h * 0.35f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawCircle(
                        color = CrimsonRed.copy(alpha = 0.14f),
                        radius = 28.dp.toPx(),
                        center = Offset(w * 0.80f, h * 0.58f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // If Sniper Scope ADS (toggled by L1 Volume Up) is active, draw Tactical Scope Overlay
                    if (isScopedAds) {
                        drawCircle(
                            color = CyberCyan.copy(alpha = 0.45f),
                            radius = h * 0.40f,
                            center = Offset(w / 2f, h / 2f),
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                        drawLine(
                            color = CyberCyan.copy(alpha = 0.3f),
                            start = Offset(w * 0.2f, h / 2f),
                            end = Offset(w * 0.8f, h / 2f),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = CyberCyan.copy(alpha = 0.3f),
                            start = Offset(w / 2f, h * 0.12f),
                            end = Offset(w / 2f, h * 0.88f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Moving Tactical Target Drone in Live Range Mode
                    if (isLiveRangeMode) {
                        val targetCenter = Offset(w * droneXRatio, h * 0.42f)
                        val targetR = if (isScopedAds) 28.dp.toPx() else 20.dp.toPx()
                        drawCircle(
                            color = CrimsonRed.copy(alpha = 0.28f),
                            radius = targetR * 1.3f,
                            center = targetCenter
                        )
                        drawCircle(
                            color = CrimsonRed,
                            radius = targetR,
                            center = targetCenter,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                        drawCircle(
                            color = MoltenAmber,
                            radius = targetR * 0.38f,
                            center = targetCenter
                        )
                    }

                    // Connection laser between L1 and R1 if Combo Link is enabled
                    if (triggerConfig.comboLinkLR) {
                        drawLine(
                            color = ElectricPurple.copy(alpha = 0.65f),
                            start = Offset(w * triggerConfig.l1XRatio, h * triggerConfig.l1YRatio),
                            end = Offset(w * triggerConfig.r1XRatio, h * triggerConfig.r1YRatio),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Center Crosshair Preview inside Arena
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CrosshairCanvasView(config = crosshairConfig)
                }

                // Top HUD overlay inside Canvas showing live status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObsidianSurface.copy(alpha = 0.85f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isScopedAds) "السكوب: مفتوح 4X (L1)" else "السكوب: عادي (اضغط L1)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isScopedAds) CyberCyan else SilverMist,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObsidianSurface.copy(alpha = 0.85f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "إصابات: $targetHitsCount  •  L1: $totalL1Shots  •  R1: $totalR1Shots",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 10.sp,
                            color = MatrixGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Draggable L1 (Volume Up) Reticle Node
                val nodeSizeDp = 50.dp
                val nodeRadiusPx = with(density) { (nodeSizeDp / 2).toPx() }
                val l1OffsetX = ((triggerConfig.l1XRatio * canvasWidthPx) - nodeRadiusPx).roundToInt()
                val l1OffsetY = ((triggerConfig.l1YRatio * canvasHeightPx) - nodeRadiusPx).roundToInt()

                DraggableTriggerNode(
                    label = "L1",
                    subLabel = "VOL+",
                    color = CyberCyan,
                    isPressed = l1Pressed,
                    offsetX = l1OffsetX,
                    offsetY = l1OffsetY,
                    onDragDelta = { dx, dy ->
                        val newX = ((triggerConfig.l1XRatio * canvasWidthPx) + dx) / canvasWidthPx
                        val newY = ((triggerConfig.l1YRatio * canvasHeightPx) + dy) / canvasHeightPx
                        onMoveL1(newX, newY)
                    }
                )

                // Draggable R1 (Volume Down) Reticle Node
                val r1OffsetX = ((triggerConfig.r1XRatio * canvasWidthPx) - nodeRadiusPx).roundToInt()
                val r1OffsetY = ((triggerConfig.r1YRatio * canvasHeightPx) - nodeRadiusPx).roundToInt()

                DraggableTriggerNode(
                    label = "R1",
                    subLabel = "VOL-",
                    color = CrimsonRed,
                    isPressed = r1Pressed,
                    offsetX = r1OffsetX,
                    offsetY = r1OffsetY,
                    onDragDelta = { dx, dy ->
                        val newX = ((triggerConfig.r1XRatio * canvasWidthPx) + dx) / canvasWidthPx
                        val newY = ((triggerConfig.r1YRatio * canvasHeightPx) + dy) / canvasHeightPx
                        onMoveR1(newX, newY)
                    }
                )

                // Bottom helper caption inside Canvas
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ObsidianSurface.copy(alpha = 0.88f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (System.currentTimeMillis() - lastHitPopupMs < 1200L)
                            "🔥 إصابة مباشرة بسرعة استجابة 1.8ms! (R1 Turbo Fire)"
                        else
                            "اسحب دوائر (L1) و (R1) لمكان أزرار لعبتك أو اضغط أزرار الصوت بهاتفك للاختبار",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (System.currentTimeMillis() - lastHitPopupMs < 1200L) MoltenAmber else SilverMist,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // On-screen Hardware Simulation Trigger Bar (so users can also test L1/R1 with touch!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CutCornerShape(10.dp))
                        .background(if (l1Pressed) CyberCyan else CyberCyan.copy(alpha = 0.16f))
                        .border(1.5.dp, CyberCyan, CutCornerShape(10.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onSimulateHardwareTrigger(TriggerButtonType.L1_VOLUME_UP, true)
                                    tryAwaitRelease()
                                    onSimulateHardwareTrigger(TriggerButtonType.L1_VOLUME_UP, false)
                                }
                            )
                        }
                        .testTag("simulate_l1_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = if (l1Pressed) Color.Black else CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "اختبار زر L1 (رفع الصوت)",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (l1Pressed) Color.Black else TitaniumWhite
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CutCornerShape(10.dp))
                        .background(if (r1Pressed) CrimsonRed else CrimsonRed.copy(alpha = 0.18f))
                        .border(1.5.dp, CrimsonRed, CutCornerShape(10.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onSimulateHardwareTrigger(TriggerButtonType.R1_VOLUME_DOWN, true)
                                    tryAwaitRelease()
                                    onSimulateHardwareTrigger(TriggerButtonType.R1_VOLUME_DOWN, false)
                                }
                            )
                        }
                        .testTag("simulate_r1_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeDown,
                            contentDescription = null,
                            tint = if (r1Pressed) Color.White else CrimsonRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "اختبار زر R1 (خفض الصوت)",
                            style = MaterialTheme.typography.labelLarge,
                            color = TitaniumWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetMiniChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(GunmetalElevated)
            .border(1.dp, CarbonBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TitaniumWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DraggableTriggerNode(
    label: String,
    subLabel: String,
    color: Color,
    isPressed: Boolean,
    offsetX: Int,
    offsetY: Int,
    onDragDelta: (Float, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX, offsetY) }
            .size(50.dp)
            .clip(CircleShape)
            .background(
                if (isPressed) color.copy(alpha = 0.85f)
                else color.copy(alpha = 0.25f)
            )
            .border(
                width = if (isPressed) 3.dp else 2.dp,
                color = if (isPressed) TitaniumWhite else color,
                shape = CircleShape
            )
            .pointerInput(label) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragDelta(dragAmount.x, dragAmount.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isPressed) Color.Black else TitaniumWhite
            )
            Text(
                text = subLabel,
                fontFamily = OrbitronFontFamily,
                fontSize = 8.sp,
                color = if (isPressed) Color.Black else color
            )
        }
    }
}

@Composable
private fun SingleShoulderConfigCard(
    buttonTitle: String,
    subtitle: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    currentMode: TriggerFireMode,
    burstRps: Int,
    xPercent: Int,
    yPercent: Int,
    onSelectMode: (TriggerFireMode) -> Unit,
    onChangeRps: (Int) -> Unit,
    tagPrefix: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = GunmetalCard,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
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
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = buttonTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ObsidianSurface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "X:$xPercent% Y:$yPercent%",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Fire Mode Chips (2x2 Grid)
            val modes = TriggerFireMode.entries
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    modes.take(2).forEach { mode ->
                        FireModeOptionCard(
                            mode = mode,
                            isSelected = currentMode == mode,
                            accentColor = accentColor,
                            onClick = { onSelectMode(mode) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("${tagPrefix}_mode_${mode.id}")
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    modes.drop(2).forEach { mode ->
                        FireModeOptionCard(
                            mode = mode,
                            isSelected = currentMode == mode,
                            accentColor = accentColor,
                            onClick = { onSelectMode(mode) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("${tagPrefix}_mode_${mode.id}")
                        )
                    }
                }
            }

            // Rapid Burst Speed Slider
            if (currentMode == TriggerFireMode.RAPID_BURST) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "سرعة الرشاش المتتالي (Turbo Rate):",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist
                    )
                    Text(
                        text = "$burstRps نقرة / ثانية",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 12.sp,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = burstRps.toFloat(),
                    onValueChange = { onChangeRps(it.roundToInt()) },
                    valueRange = 4f..20f,
                    steps = 15,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor
                    )
                )
            }
        }
    }
}

@Composable
private fun FireModeOptionCard(
    mode: TriggerFireMode,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.2f) else ObsidianSurface,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accentColor else CarbonBorder
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mode.badge,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accentColor else SilverMist
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = mode.arabicName,
                style = MaterialTheme.typography.bodySmall,
                color = TitaniumWhite,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AdvancedTriggerMechanicsCard(
    triggerConfig: ShoulderTriggerConfig,
    onUpdateConfig: ((ShoulderTriggerConfig) -> ShoulderTriggerConfig) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = GunmetalCard,
        border = BorderStroke(1.dp, CarbonBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "إعدادات الماكرو والاهتزاز الميكانيكي (RedMagic Tactile)",
                style = MaterialTheme.typography.titleSmall,
                color = TitaniumWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Combo Link L1 + R1
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
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = ElectricPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "دمج ماكرو (سكوب L1 + رمي R1 معاً)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "عند ضغط R1 يفتح السكوب تلقائياً في L1 ويطلق النار في نفس اللحظة",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist
                        )
                    }
                }
                Switch(
                    checked = triggerConfig.comboLinkLR,
                    onCheckedChange = { v -> onUpdateConfig { it.copy(comboLinkLR = v) } },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = ElectricPurple
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Haptic Vibration
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
                        imageVector = Icons.Default.Vibration,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "اهتزاز لمسي تكتيكي (Haptic Feedback)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "نبضة اهتزاز سريعة تحاكي أزرار الكتف الحقيقية عند ضغط أزرار الصوت",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist
                        )
                    }
                }
                Switch(
                    checked = triggerConfig.hapticFeedback,
                    onCheckedChange = { v -> onUpdateConfig { it.copy(hapticFeedback = v) } },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = CyberCyan
                    )
                )
            }
        }
    }
}
