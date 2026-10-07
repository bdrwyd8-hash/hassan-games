package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ActiveGameSession
import com.example.model.AdvisorPresetMode
import com.example.model.AutoSettingsRecommendation
import com.example.model.LowEndOptimizerConfig
import com.example.model.NotificationShieldState
import com.example.model.OneTapGamePrepStatus
import com.example.model.OneTapPrepOverallState
import com.example.model.PrepStepState
import com.example.model.QuickReconnectState
import com.example.model.SmartThermalMode
import com.example.model.SmartThermalStatus
import com.example.ui.theme.CarbonBorder as SteelBorder
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GunmetalCard as CarbonCard
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MoltenAmber
import com.example.ui.theme.ObsidianSurface as CarbonSurfaceVariant
import com.example.ui.theme.SilverMist as SilverMuted
import com.example.ui.theme.TitaniumWhite
import kotlin.math.roundToInt

/**
 * 1. ONE-TAP GAME PREPARATION CARD (نظام تجهيز اللعبة الذكي بضغطة واحدة)
 */
@Composable
fun OneTapGamePrepCard(
    prepStatus: OneTapGamePrepStatus,
    activeGameTitle: String?,
    onRunOneTapPrep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isReady = prepStatus.overallState == OneTapPrepOverallState.GAME_READY ||
        prepStatus.overallState == OneTapPrepOverallState.BOOST_READY
    val isPreparing = prepStatus.overallState == OneTapPrepOverallState.PREPARING

    val accentColor = when {
        isReady -> MatrixGreen
        isPreparing -> CyberCyan
        else -> CrimsonRed
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.18f),
                        CarbonCard,
                        Color(0xFF0A0D14)
                    )
                )
            )
            .border(1.5.dp, accentColor.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f))
                            .border(1.dp, accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.Bolt,
                            contentDescription = "تجهيز اللعبة بضغطة واحدة",
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "تجهيز اللعب بضغطة واحدة (One-Tap Prep)",
                            style = MaterialTheme.typography.titleSmall,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (!activeGameTitle.isNullOrBlank()) {
                                "البروفايل المستهدف: $activeGameTitle"
                            } else {
                                "تجهيز آمن وسريع للذاكرة والحرارة والإشعارات والأدوات"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = SilverMuted
                        )
                    }
                }

                // Status Badge (GAME READY / BOOST READY / PREPARING)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .border(1.dp, accentColor.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = prepStatus.badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Show preparation steps when preparing or completed
            AnimatedVisibility(visible = prepStatus.steps.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CarbonSurfaceVariant.copy(alpha = 0.85f))
                        .border(1.dp, SteelBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    prepStatus.steps.forEach { step ->
                        val stepColor = when (step.state) {
                            PrepStepState.COMPLETED -> MatrixGreen
                            PrepStepState.RUNNING -> CyberCyan
                            PrepStepState.PENDING -> SilverMuted
                        }
                        val stepSymbol = when (step.state) {
                            PrepStepState.COMPLETED -> "✓"
                            PrepStepState.RUNNING -> "⚡"
                            PrepStepState.PENDING -> "•"
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stepSymbol,
                                style = MaterialTheme.typography.titleSmall,
                                color = stepColor,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = step.titleAr,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TitaniumWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = step.detailAr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = stepColor
                                )
                            }
                        }
                    }

                    if (isReady) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MatrixGreen.copy(alpha = 0.14f))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✅ الحالة: ${prepStatus.badgeText} • رام متاح ${prepStatus.availableRamMb}MB (+${prepStatus.freedRamMb}MB) • حرارة: ${prepStatus.thermalStateLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MatrixGreen,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onRunOneTapPrep,
                enabled = !isPreparing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isReady) MatrixGreen else CrimsonRed,
                    contentColor = if (isReady) Color.Black else TitaniumWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("one_tap_game_prep_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        isPreparing -> "جاري تجهيز بيئة اللعب بذكاء..."
                        isReady -> "⚡ إعادة تحديث التجهيز الآن (${prepStatus.badgeText})"
                        else -> "⚡ تجهيز اللعبة بضغطة واحدة (ONE-TAP PREP)"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * 2. QUICK GAME RECONNECT BANNER (نظام العودة السريعة للعبة بضغطة واحدة)
 */
@Composable
fun QuickGameReconnectBanner(
    session: ActiveGameSession,
    onQuickReconnect: () -> Unit,
    onOpenGameProfiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = session.reconnectState
    val badgeColor = when (state) {
        QuickReconnectState.IN_BACKGROUND_READY -> MatrixGreen
        QuickReconnectState.CLOSED_NEEDS_RELAUNCH -> CyberCyan
        QuickReconnectState.PROFILE_READY_SIMULATION -> MoltenAmber
        QuickReconnectState.NO_SESSION -> SilverMuted
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CarbonCard)
            .border(1.dp, badgeColor.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (session.gameTitle.isNotBlank()) {
                            "العودة السريعة: ${session.gameTitle}"
                        } else {
                            "العودة السريعة للعبة (Quick Reconnect)"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = state.descriptionAr,
                        style = MaterialTheme.typography.labelSmall,
                        color = SilverMuted
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.16f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = state.badgeAr,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onQuickReconnect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state == QuickReconnectState.IN_BACKGROUND_READY) MatrixGreen else CyberCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .testTag("quick_reconnect_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = state.actionButtonAr,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutlinedButton(
                    onClick = onOpenGameProfiles,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(48.dp)
                        .testTag("open_profiles_from_reconnect_button")
                ) {
                    Text(
                        text = "تغيير اللعبة",
                        style = MaterialTheme.typography.labelMedium,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 3. SMART THERMAL MODE & NOTIFICATION SHIELD & HUD OVERLAYS CARD
 */
@Composable
fun SmartThermalAndShieldCard(
    thermalStatus: SmartThermalStatus,
    shieldState: NotificationShieldState,
    lowEndConfig: LowEndOptimizerConfig,
    onSelectThermalMode: (SmartThermalMode) -> Unit,
    onToggleNotificationShield: () -> Unit,
    onOpenDndPermissionSettings: () -> Unit,
    onToggleHudMetric: (fps: Boolean?, temp: Boolean?, ram: Boolean?, magnifier: Boolean?) -> Unit,
    onUpdateMagnifierZoom: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val levelColor = thermalStatus.thermalLevel.color

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CarbonCard)
            .border(1.dp, levelColor.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header: Smart Thermal Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = "الوضع الحراري الذكي",
                        tint = levelColor
                    )
                    Column {
                        Text(
                            text = "الوضع الحراري الذكي (Smart Thermal Mode)",
                            style = MaterialTheme.typography.titleSmall,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "حرارة البطارية الفعلية: ${thermalStatus.batteryTempCelsius}°C • القراءة كل ${thermalStatus.effectivePollIntervalMs / 1000} ث",
                            style = MaterialTheme.typography.labelSmall,
                            color = SilverMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(levelColor.copy(alpha = 0.18f))
                        .border(1.dp, levelColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${thermalStatus.thermalLevel.englishBadge} • ${thermalStatus.thermalLevel.arabicLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = levelColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // 3 Mode Pills for Smart Thermal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmartThermalMode.entries.forEach { mode ->
                    val selected = thermalStatus.mode == mode
                    val pillColor = when (mode) {
                        SmartThermalMode.OFF -> SilverMuted
                        SmartThermalMode.AUTO_ADAPTIVE -> CyberCyan
                        SmartThermalMode.ECO_STABILITY -> MatrixGreen
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) pillColor.copy(alpha = 0.22f) else CarbonSurfaceVariant)
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) pillColor else SteelBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectThermalMode(mode) }
                            .padding(vertical = 10.dp, horizontal = 6.dp)
                            .testTag("thermal_mode_${mode.id.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = mode.badge,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = if (selected) pillColor else SilverMuted,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = mode.arabicTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = TitaniumWhite,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Active Thermal Load Reduction Actions & Realistic Explanation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CarbonSurfaceVariant.copy(alpha = 0.7f))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = thermalStatus.summaryExplanationAr,
                    style = MaterialTheme.typography.labelSmall,
                    color = TitaniumWhite,
                    fontWeight = FontWeight.Bold
                )
                thermalStatus.activeActionsAr.forEach { action ->
                    Text(
                        text = "• $action",
                        style = MaterialTheme.typography.labelSmall,
                        color = levelColor
                    )
                }
            }

            // ━━━━━━━━━━━━━━━━━━ NOTIFICATION SHIELD SECTION ━━━━━━━━━━━━━━━━━━
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (shieldState.enabled) MatrixGreen.copy(alpha = 0.12f)
                        else CarbonSurfaceVariant
                    )
                    .border(
                        1.dp,
                        if (shieldState.enabled) MatrixGreen.copy(alpha = 0.65f) else SteelBorder,
                        RoundedCornerShape(14.dp)
                    )
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = "درع الإشعارات",
                                tint = if (shieldState.enabled) MatrixGreen else SilverMuted
                            )
                            Column {
                                Text(
                                    text = "درع عزل الإشعارات (Notification Shield)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TitaniumWhite,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = shieldState.statusLabelAr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (shieldState.enabled) MatrixGreen else SilverMuted
                                )
                            }
                        }

                        Switch(
                            checked = shieldState.enabled,
                            onCheckedChange = { onToggleNotificationShield() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MatrixGreen,
                                checkedTrackColor = MatrixGreen.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("notification_shield_toggle")
                        )
                    }

                    if (!shieldState.hasDndPolicyPermission) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "للعزل الكامل عبر نظام عدم الإزعاج الرسمي (DND) بجانب كتم الصوت:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MoltenAmber,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = onOpenDndPermissionSettings,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("grant_dnd_permission_button")
                            ) {
                                Text(
                                    text = "منح إذن DND",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ━━━━━━━━━━━━━━━━━━ FLOATING HUD OVERLAYS & MAGNIFIER STRIP ━━━━━━━━━━━━━━━━━━
            Text(
                text = "عدادات الشاشة العائمة وعدسة التكبير (Floating Overlays)",
                style = MaterialTheme.typography.labelMedium,
                color = CyberCyan,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OverlayQuickChip(
                    label = "FPS",
                    enabled = lowEndConfig.fpsOverlayEnabled,
                    onClick = { onToggleHudMetric(!lowEndConfig.fpsOverlayEnabled, null, null, null) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_fps_overlay_chip")
                )
                OverlayQuickChip(
                    label = "الحرارة °C",
                    enabled = lowEndConfig.tempOverlayEnabled,
                    onClick = { onToggleHudMetric(null, !lowEndConfig.tempOverlayEnabled, null, null) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_temp_overlay_chip")
                )
                OverlayQuickChip(
                    label = "الرام %",
                    enabled = lowEndConfig.ramOverlayEnabled,
                    onClick = { onToggleHudMetric(null, null, !lowEndConfig.ramOverlayEnabled, null) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_ram_overlay_chip")
                )
                OverlayQuickChip(
                    label = "عدسة ${(lowEndConfig.magnifierZoom * 10).roundToInt() / 10f}x",
                    enabled = lowEndConfig.magnifierEnabled,
                    onClick = { onToggleHudMetric(null, null, null, !lowEndConfig.magnifierEnabled) },
                    modifier = Modifier
                        .weight(1.15f)
                        .testTag("toggle_magnifier_overlay_chip")
                )
            }

            AnimatedVisibility(visible = lowEndConfig.magnifierEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "قوة عدسة التكبير: ${(lowEndConfig.magnifierZoom * 10).roundToInt() / 10f}x",
                        style = MaterialTheme.typography.labelSmall,
                        color = TitaniumWhite
                    )
                    Slider(
                        value = lowEndConfig.magnifierZoom,
                        onValueChange = onUpdateMagnifierZoom,
                        valueRange = 1.5f..3.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OverlayQuickChip(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (enabled) CyberCyan else SilverMuted
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) CyberCyan.copy(alpha = 0.18f) else CarbonSurfaceVariant)
            .border(1.dp, if (enabled) CyberCyan else SteelBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (enabled) "✓ $label" else label,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) color else TitaniumWhite,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 4. AUTO SETTINGS ADVISOR CARD (مستشار الإعدادات الذكي حسب عتاد الجهاز)
 */
@Composable
fun AutoSettingsAdvisorCard(
    recommendation: AutoSettingsRecommendation,
    onSelectMode: (AdvisorPresetMode) -> Unit,
    onApplyRecommendedTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = recommendation.mode.badgeColor

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CarbonCard)
            .border(1.5.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "مستشار الإعدادات الذكي",
                        tint = accent
                    )
                    Column {
                        Text(
                            text = "مستشار إعدادات الألعاب الذكي (Auto Settings Advisor)",
                            style = MaterialTheme.typography.titleSmall,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "تصنيف جهازك الفعلي: ${recommendation.deviceTierAr}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3 Mode Selector Tabs: BEST PERFORMANCE / BALANCED / BEST VISUAL QUALITY
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdvisorPresetMode.entries.forEach { preset ->
                    val selected = recommendation.mode == preset
                    val tabColor = preset.badgeColor
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) tabColor.copy(alpha = 0.22f) else CarbonSurfaceVariant)
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) tabColor else SteelBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectMode(preset) }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                            .testTag("advisor_mode_${preset.id.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = preset.englishTitle,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = if (selected) tabColor else SilverMuted,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = preset.arabicTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = TitaniumWhite,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Concrete In-Game Settings Table
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CarbonSurfaceVariant.copy(alpha = 0.85f))
                    .border(1.dp, SteelBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                AdvisorMetricRow("معدل الإطارات المتوقع (FPS)", recommendation.expectedFpsText, MatrixGreen)
                AdvisorMetricRow("جودة الجرافيك (Graphics)", recommendation.graphicsQualityText, TitaniumWhite)
                AdvisorMetricRow("الظلال (Shadows)", recommendation.shadowsRecommendation, CyberCyan)
                AdvisorMetricRow("المؤثرات (Effects)", recommendation.effectsRecommendation, TitaniumWhite)
                AdvisorMetricRow("دقة العرض (Resolution)", recommendation.resolutionRecommendation, MoltenAmber)
                AdvisorMetricRow("جودة الخامات (Textures)", recommendation.textureRecommendation, TitaniumWhite)
                AdvisorMetricRow("مضاد التعرج (Anti-Aliasing)", recommendation.antiAliasingRecommendation, CyberCyan)
            }

            // Why Chosen Explanation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.1f))
                    .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "💡 لماذا هذه الإعدادات؟ ${recommendation.whyChosenExplanationAr}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TitaniumWhite
                )
            }

            Button(
                onClick = onApplyRecommendedTools,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("apply_advisor_recommendations_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "⚡ تطبيق إعدادات المحرك الموصى بها (${recommendation.mode.arabicTitle})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun AdvisorMetricRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "• $label:",
            style = MaterialTheme.typography.labelSmall,
            color = SilverMuted,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = valueColor,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}
