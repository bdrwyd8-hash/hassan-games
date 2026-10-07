package com.example.ui.components

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdvisorPresetMode
import com.example.model.AutoSettingsRecommendation
import com.example.model.GamePrepReport
import com.example.model.GameSpaceProfile
import com.example.model.InstalledAppCandidate
import com.example.model.LastSessionGameInfo
import com.example.model.NotificationShieldState
import com.example.model.PerformanceMode
import com.example.model.QuickReconnectState
import com.example.model.SmartThermalMode
import com.example.model.SmartThermalStatus
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

/**
 * 0. UNIFIED MASTER START / STOP & EXIT BANNER
 */
@Composable
fun MasterStartStopAppBanner(
    isMasterRunning: Boolean,
    onToggleStartOrExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (isMasterRunning) CrimsonRed else MatrixGreen
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GunmetalCard,
        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
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
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.18f))
                        .border(1.dp, accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isMasterRunning) "محرك Hassan Games نشط" else "المحرك متوقف حالياً",
                        style = MaterialTheme.typography.titleSmall,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (isMasterRunning)
                            "اضغط (إنهاء وإغلاق) عند الانتهاء من اللعب لإيقاف جميع خدمات الخلفية تماماً"
                        else
                            "اضغط (بدء التشغيل) لتنشيط مركز الألعاب وأدوات التسريع",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onToggleStartOrExit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = if (isMasterRunning) Color.White else ObsidianBlack
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.testTag("master_start_stop_exit_button")
            ) {
                Icon(
                    imageVector = if (isMasterRunning) Icons.Default.PowerSettingsNew else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isMasterRunning) "إنهاء وإغلاق" else "بدء التشغيل",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * 1. ONE-TAP GAME PREPARATION CARD (تجهيز اللعبة بضغطة واحدة)
 */
@Composable
fun OneTapGamePreparationCard(
    isPreparing: Boolean,
    lastReport: GamePrepReport?,
    activeGameTitle: String?,
    onExecuteOneTapPrep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (lastReport != null) MatrixGreen else CrimsonRed

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = GunmetalCard,
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.16f),
                            GunmetalCard,
                            ObsidianSurface
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                        if (isPreparing) {
                            CircularProgressIndicator(
                                color = CyberCyan,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (lastReport != null) Icons.Default.CheckCircle else Icons.Default.Bolt,
                                contentDescription = "تجهيز اللعبة بضغطة واحدة",
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "تجهيز اللعبة بضغطة واحدة (One-Tap Prep)",
                            style = MaterialTheme.typography.titleSmall,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (!activeGameTitle.isNullOrBlank()) {
                                "اللعبة المستهدفة: $activeGameTitle"
                            } else {
                                "تفريغ الرام + ضبط الأداء والحرارة + درع الإشعارات + فحص البنق"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .border(1.dp, accentColor.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            isPreparing -> "PREPARING..."
                            lastReport != null -> "GAME READY"
                            else -> "ONE-TAP"
                        },
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            AnimatedVisibility(visible = lastReport != null) {
                lastReport?.let { report ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ObsidianSurface)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "✓ تقرير التجهيز الفعلي (${report.targetGameTitle}):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MatrixGreen,
                            fontWeight = FontWeight.ExtraBold
                        )
                        report.stepsCompletedAr.forEach { step ->
                            Text(
                                text = "• $step",
                                style = MaterialTheme.typography.bodySmall,
                                color = TitaniumWhite,
                                fontSize = 11.sp
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MatrixGreen.copy(alpha = 0.14f))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "الرام الحر: ${report.ramBeforeMb}MB ← ${report.ramAfterMb}MB (+${report.freedRamMb}MB)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MatrixGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${report.pingMs}ms • ${report.batteryTempCelsius}°C",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onExecuteOneTapPrep,
                enabled = !isPreparing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (lastReport != null) MatrixGreen else CrimsonRed,
                    contentColor = if (lastReport != null) ObsidianBlack else Color.White
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
                        isPreparing -> "جاري تجهيز الجهاز للعب..."
                        lastReport != null -> "⚡ إعادة تجهيز الجهاز الآن (GAME READY)"
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
 * 2. QUICK GAME RECONNECT CARD (العودة السريعة للعبة النشطة أو الأخيرة)
 */
@Composable
fun QuickGameReconnectCard(
    lastSessionInfo: LastSessionGameInfo?,
    onQuickReconnect: () -> Unit,
    onOpenMyGames: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (lastSessionInfo == null) return
    val state = lastSessionInfo.reconnectState
    val badgeColor = Color(state.colorHex)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GunmetalCard,
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "العودة السريعة: ${lastSessionInfo.title}",
                        style = MaterialTheme.typography.titleSmall,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = lastSessionInfo.statusDetailAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist,
                        fontSize = 11.sp
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
                        style = MaterialTheme.typography.bodySmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
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
                        containerColor = badgeColor,
                        contentColor = ObsidianBlack
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(46.dp)
                        .testTag("quick_reconnect_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تشغيل / عودة سريعة الآن",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutlinedButton(
                    onClick = onOpenMyGames,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(46.dp)
                        .testTag("open_profiles_from_reconnect_button")
                ) {
                    Text(
                        text = "ألعابي MY GAMES",
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
 * 3. SMART THERMAL MODE CARD (الوضع الحراري الذكي لمنع السخونة واللاق)
 */
@Composable
fun SmartThermalModeCard(
    thermalStatus: SmartThermalStatus,
    onSelectMode: (SmartThermalMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val levelColor = Color(thermalStatus.thermalLevel.statusColorHex)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = GunmetalCard,
        border = BorderStroke(1.dp, levelColor.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                            text = "حرارة البطارية: ${thermalStatus.batteryTempCelsius}°C • دورة القراءة: كل ${thermalStatus.effectivePollIntervalMs / 1000} ثوانٍ",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist,
                            fontSize = 11.sp
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
                        text = thermalStatus.thermalLevel.labelAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = levelColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmartThermalMode.entries.forEach { mode ->
                    val selected = thermalStatus.mode == mode
                    val pillColor = Color(mode.accentHex)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) pillColor.copy(alpha = 0.22f) else ObsidianSurface)
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) pillColor else CarbonBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectMode(mode) }
                            .padding(vertical = 10.dp, horizontal = 6.dp)
                            .testTag("thermal_mode_${mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = mode.badgeAr,
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                color = if (selected) pillColor else SilverMist,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = mode.titleAr.substringBefore(" ("),
                                style = MaterialTheme.typography.bodySmall,
                                color = TitaniumWhite,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianSurface)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = thermalStatus.summaryExplanationAr,
                    style = MaterialTheme.typography.bodySmall,
                    color = TitaniumWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                thermalStatus.activeActionsAr.forEach { action ->
                    Text(
                        text = "• $action",
                        style = MaterialTheme.typography.bodySmall,
                        color = levelColor,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * 4. NOTIFICATION SHIELD BANNER (درع عزل الإشعارات أثناء اللعب)
 */
@Composable
fun NotificationShieldBanner(
    shieldState: NotificationShieldState,
    onToggleShield: () -> Unit,
    onRequestDndPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GunmetalCard,
        border = BorderStroke(
            1.dp,
            if (shieldState.enabled) MatrixGreen.copy(alpha = 0.65f) else CarbonBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                        tint = if (shieldState.enabled) MatrixGreen else SilverMist
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
                            style = MaterialTheme.typography.bodySmall,
                            color = if (shieldState.enabled) MatrixGreen else SilverMist,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = shieldState.enabled,
                    onCheckedChange = { onToggleShield() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = MatrixGreen
                    ),
                    modifier = Modifier.testTag("notification_shield_toggle")
                )
            }

            if (!shieldState.hasDndPermission) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "لتفعيل وضع عدم الإزعاج الرسمي بالنظام (DND) تلقائياً عند بدء اللعب:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MoltenAmber,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onRequestDndPermission,
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
}

/**
 * 5. AUTO SETTINGS ADVISOR CARD (مستشار الإعدادات الذكي حسب عتاد الجهاز)
 */
@Composable
fun AutoSettingsAdvisorCard(
    recommendation: AutoSettingsRecommendation,
    onSelectMode: (AdvisorPresetMode) -> Unit,
    onApplyRecommendedTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = Color(recommendation.mode.accentHex)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = GunmetalCard,
        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
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
                        text = "تصنيف العتاد الفعلي: ${recommendation.deviceTierAr}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdvisorPresetMode.entries.forEach { preset ->
                    val selected = recommendation.mode == preset
                    val tabColor = Color(preset.accentHex)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) tabColor.copy(alpha = 0.22f) else ObsidianSurface)
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) tabColor else CarbonBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectMode(preset) }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                            .testTag("advisor_mode_${preset.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = preset.badgeText,
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                color = if (selected) tabColor else SilverMist,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = preset.titleAr.substringBefore(" ("),
                                style = MaterialTheme.typography.bodySmall,
                                color = TitaniumWhite,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianSurface)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AdvisorMetricRow("معدل الإطارات المتوقع (FPS)", recommendation.expectedFpsText, MatrixGreen)
                AdvisorMetricRow("جودة الجرافيك (Graphics)", recommendation.graphicsQualityText, TitaniumWhite)
                AdvisorMetricRow("الظلال (Shadows)", recommendation.shadowsRecommendation, CyberCyan)
                AdvisorMetricRow("المؤثرات (Effects)", recommendation.effectsRecommendation, TitaniumWhite)
                AdvisorMetricRow("دقة العرض (Resolution)", recommendation.resolutionRecommendation, MoltenAmber)
                AdvisorMetricRow("جودة الخامات (Textures)", recommendation.textureRecommendation, TitaniumWhite)
                AdvisorMetricRow("مضاد التعرج (Anti-Aliasing)", recommendation.antiAliasingRecommendation, CyberCyan)
            }

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
                    style = MaterialTheme.typography.bodySmall,
                    color = TitaniumWhite,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onApplyRecommendedTools,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = ObsidianBlack
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("apply_advisor_recommendations_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "⚡ تطبيق إعدادات المحرك الموصى بها (${recommendation.mode.badgeText})",
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
            style = MaterialTheme.typography.bodySmall,
            color = SilverMist,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

/**
 * 6. PER-GAME PROFILE SETTINGS SHEET (إعدادات اللعبة المخصصة في MY GAMES)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerGameProfileSheet(
    profile: GameSpaceProfile,
    onDismiss: () -> Unit,
    onSaveProfile: (GameSpaceProfile) -> Unit,
    onLaunchWithProfile: (GameSpaceProfile) -> Unit,
    onDeleteFromMyGames: (String) -> Unit
) {
    var selectedMode by remember(profile.id) { mutableStateOf(profile.recommendedMode) }
    var targetFps by remember(profile.id) { mutableIntStateOf(profile.recommendedFps) }
    var advisorMode by remember(profile.id) { mutableStateOf(profile.preferredAdvisorMode) }
    var autoCleanRam by remember(profile.id) { mutableStateOf(profile.autoCleanRamBeforeLaunch) }
    var notifShield by remember(profile.id) { mutableStateOf(profile.notificationShieldEnabled) }
    var sidebarOverlay by remember(profile.id) { mutableStateOf(profile.sidebarOverlayEnabled) }
    var fpsOverlay by remember(profile.id) { mutableStateOf(profile.fpsOverlayEnabled) }
    var crosshair by remember(profile.id) { mutableStateOf(profile.crosshairEnabled) }
    var brightnessLock by remember(profile.id) { mutableStateOf(profile.brightnessLockEnabled) }
    var touchGuard by remember(profile.id) { mutableStateOf(profile.touchGuardEnabled) }
    var wifiPriority by remember(profile.id) { mutableStateOf(profile.wiFiPriorityEnabled) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val accent = Color(profile.accentHex)

    fun buildUpdated(): GameSpaceProfile = profile.copy(
        recommendedMode = selectedMode,
        recommendedFps = targetFps,
        preferredAdvisorMode = advisorMode,
        autoCleanRamBeforeLaunch = autoCleanRam,
        notificationShieldEnabled = notifShield,
        sidebarOverlayEnabled = sidebarOverlay,
        fpsOverlayEnabled = fpsOverlay,
        crosshairEnabled = crosshair,
        brightnessLockEnabled = brightnessLock,
        touchGuardEnabled = touchGuard,
        wiFiPriorityEnabled = wifiPriority
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accent.copy(alpha = 0.2f))
                            .border(1.dp, accent, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = accent
                        )
                    }
                    Column {
                        Text(
                            text = "إعدادات اللعبة: ${profile.title}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = profile.packageName,
                            fontFamily = OrbitronFontFamily,
                            fontSize = 10.sp,
                            color = SilverMist
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SilverMist)
                }
            }

            // Performance Profile Selector
            Text(
                text = "1. ملف الأداء (Performance Profile):",
                style = MaterialTheme.typography.labelLarge,
                color = CyberCyan,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PerformanceMode.entries.forEach { mode ->
                    val sel = selectedMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (sel) mode.primaryColor.copy(alpha = 0.24f) else GunmetalCard)
                            .border(
                                1.5.dp,
                                if (sel) mode.primaryColor else CarbonBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedMode = mode }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = mode.badgeText,
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                color = if (sel) mode.primaryColor else SilverMist,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = mode.titleAr.substringBefore(" "),
                                style = MaterialTheme.typography.bodySmall,
                                color = TitaniumWhite,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Target FPS Selector
            Text(
                text = "2. معدل الإطارات المستهدف (Target FPS):",
                style = MaterialTheme.typography.labelLarge,
                color = CyberCyan,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(60, 90, 120, 144).forEach { fps ->
                    val sel = targetFps == fps
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (sel) MatrixGreen.copy(alpha = 0.22f) else GunmetalCard)
                            .border(1.dp, if (sel) MatrixGreen else CarbonBorder, RoundedCornerShape(8.dp))
                            .clickable { targetFps = fps }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$fps FPS",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sel) MatrixGreen else TitaniumWhite
                        )
                    }
                }
            }

            // Advisor Preset Mode
            Text(
                text = "3. أولوية مستشار الجرافيك (Graphics Advisor):",
                style = MaterialTheme.typography.labelLarge,
                color = CyberCyan,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AdvisorPresetMode.entries.forEach { adv ->
                    val sel = advisorMode == adv
                    val c = Color(adv.accentHex)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (sel) c.copy(alpha = 0.22f) else GunmetalCard)
                            .border(1.dp, if (sel) c else CarbonBorder, RoundedCornerShape(8.dp))
                            .clickable { advisorMode = adv }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = adv.titleAr.substringBefore(" ("),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (sel) c else TitaniumWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Per-Game Gaming Mode Switches
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = GunmetalCard,
                border = BorderStroke(1.dp, CarbonBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    ProfileToggleRow("تنظيف الرام والكاش تلقائياً عند الضغط على START", autoCleanRam) {
                        autoCleanRam = it
                    }
                    ProfileToggleRow("تفعيل الشريط الجانبي للأدوات داخل اللعبة (Sidebar)", sidebarOverlay) {
                        sidebarOverlay = it
                    }
                    ProfileToggleRow("إظهار شريط الفريمات والحرارة العائم (FPS HUD)", fpsOverlay) {
                        fpsOverlay = it
                    }
                    ProfileToggleRow("درع عزل الإشعارات والمكالمات أثناء اللعب (DND)", notifShield) {
                        notifShield = it
                    }
                    ProfileToggleRow("تفعيل مؤشر التصويب المخصص (Crosshair)", crosshair) {
                        crosshair = it
                    }
                    ProfileToggleRow("تثبيت السطوع التلقائي أثناء اللعب", brightnessLock) {
                        brightnessLock = it
                    }
                    ProfileToggleRow("حماية أطراف الشاشة من اللمس الخاطئ", touchGuard) {
                        touchGuard = it
                    }
                    ProfileToggleRow("أولوية قصوى لحزم الشبكة وتقليل البنق", wifiPriority) {
                        wifiPriority = it
                    }
                }
            }

            // Action Buttons: START with Profile + Save Settings + Remove from MY GAMES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val updated = buildUpdated()
                        onSaveProfile(updated)
                        onLaunchWithProfile(updated)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MatrixGreen,
                        contentColor = ObsidianBlack
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("sheet_start_game_button")
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ وبدء اللعب START", fontWeight = FontWeight.ExtraBold)
                }

                OutlinedButton(
                    onClick = {
                        onSaveProfile(buildUpdated())
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CyberCyan),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(48.dp)
                        .testTag("sheet_save_profile_button")
                ) {
                    Text("حفظ فقط", color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = {
                    onDeleteFromMyGames(profile.id)
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sheet_remove_game_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = CrimsonRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "إزالة اللعبة من قائمة ألعابي (MY GAMES)",
                    color = CrimsonRed,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun ProfileToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = TitaniumWhite,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianBlack,
                checkedTrackColor = CyberCyan
            )
        )
    }
}

/**
 * 7. UNIVERSAL "+ ADD GAME" INSTALLED APPS & CUSTOM GAME PICKER SHEET
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGamePickerSheet(
    candidates: List<InstalledAppCandidate>,
    onRefreshCandidates: () -> Unit,
    onAddCandidate: (InstalledAppCandidate) -> Unit,
    onAddCustomGame: (String, String, String, PerformanceMode) -> Unit,
    onLoadAppIcon: (String) -> ImageBitmap?,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var onlyDetectedGames by remember { mutableStateOf(false) }
    var showCustomCreator by remember { mutableStateOf(false) }
    var customTitle by remember { mutableStateOf("") }
    var customPackage by remember { mutableStateOf("") }
    var customGenre by remember { mutableStateOf("أكشن وتنافس • eSports") }
    var customMode by remember { mutableStateOf(PerformanceMode.DIABLO) }

    val filteredCandidates = remember(candidates, searchQuery, onlyDetectedGames) {
        candidates.filter { candidate ->
            val matchesQuery = searchQuery.isBlank() ||
                candidate.title.contains(searchQuery, ignoreCase = true) ||
                candidate.packageName.contains(searchQuery, ignoreCase = true)
            val matchesFilter = !onlyDetectedGames || candidate.isLikelyGame
            matchesQuery && matchesFilter
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "+ ADD GAME • إضافة لعبة إلى MY GAMES",
                            style = MaterialTheme.typography.titleMedium,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "اختر أي لعبة أو تطبيق مثبت على جهازك لتشغيله وإدارته داخل Gaming Mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist,
                            fontSize = 11.sp
                        )
                    }
                }
                Row {
                    IconButton(onClick = onRefreshCandidates) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث", tint = CyberCyan)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SilverMist)
                    }
                }
            }

            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_game_search_input"),
                singleLine = true,
                placeholder = {
                    Text("ابحث باسم اللعبة أو اسم الحزمة (Package)...", color = SilverMist, fontSize = 12.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CarbonBorder,
                    focusedTextColor = TitaniumWhite,
                    unfocusedTextColor = TitaniumWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Filter Row + Toggle Custom Game Form
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onlyDetectedGames = !onlyDetectedGames },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (onlyDetectedGames) MatrixGreen else CarbonBorder)
                ) {
                    Text(
                        text = if (onlyDetectedGames) "✓ الألعاب المكتشفة فقط" else "عرض كل التطبيقات (${candidates.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (onlyDetectedGames) MatrixGreen else SilverMist,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { showCustomCreator = !showCustomCreator },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_custom_game_creator_btn"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (showCustomCreator) CrimsonRed else CyberCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = if (showCustomCreator) CrimsonRed else CyberCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "إضافة لعبة يدوياً",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (showCustomCreator) CrimsonRed else CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Collapsible Custom Game Creator
            AnimatedVisibility(visible = showCustomCreator) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = GunmetalCard,
                    border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "إضافة ملف لعبة مخصص إلى MY GAMES",
                            style = MaterialTheme.typography.labelLarge,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = customTitle,
                            onValueChange = { customTitle = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_game_title_input"),
                            singleLine = true,
                            label = { Text("اسم اللعبة (مثال: Genshin Impact / eFootball)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonRed,
                                unfocusedBorderColor = CarbonBorder,
                                focusedTextColor = TitaniumWhite,
                                unfocusedTextColor = TitaniumWhite
                            )
                        )
                        OutlinedTextField(
                            value = customPackage,
                            onValueChange = { customPackage = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_game_package_input"),
                            singleLine = true,
                            label = { Text("اسم الحزمة اختياري (Package Name)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CarbonBorder,
                                focusedTextColor = TitaniumWhite,
                                unfocusedTextColor = TitaniumWhite
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PerformanceMode.entries.forEach { mode ->
                                val sel = customMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (sel) mode.primaryColor.copy(alpha = 0.22f) else ObsidianSurface)
                                        .border(1.dp, if (sel) mode.primaryColor else CarbonBorder, RoundedCornerShape(8.dp))
                                        .clickable { customMode = mode }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode.badgeText,
                                        fontFamily = OrbitronFontFamily,
                                        fontSize = 10.sp,
                                        color = if (sel) mode.primaryColor else SilverMist,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Button(
                            onClick = {
                                if (customTitle.isNotBlank()) {
                                    onAddCustomGame(customTitle, customPackage, customGenre, customMode)
                                    customTitle = ""
                                    customPackage = ""
                                    showCustomCreator = false
                                }
                            },
                            enabled = customTitle.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_add_custom_game_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MatrixGreen,
                                contentColor = ObsidianBlack
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("حفظ وإضافة إلى MY GAMES", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // Installed Launchable Apps List
            Text(
                text = "التطبيقات والألعاب المثبتة على الجهاز (${filteredCandidates.size}):",
                style = MaterialTheme.typography.labelLarge,
                color = CyberCyan,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredCandidates.forEach { candidate ->
                    val iconBmp = remember(candidate.packageName) {
                        onLoadAppIcon(candidate.packageName)
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("installed_candidate_${candidate.packageName}"),
                        shape = RoundedCornerShape(12.dp),
                        color = GunmetalCard,
                        border = BorderStroke(
                            1.dp,
                            if (candidate.isAddedToMyGames) MatrixGreen.copy(alpha = 0.55f)
                            else if (candidate.isLikelyGame) CyberCyan.copy(alpha = 0.45f)
                            else CarbonBorder
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
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ObsidianBlack)
                                        .border(1.dp, Color(candidate.accentHex), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (iconBmp != null) {
                                        Image(
                                            bitmap = iconBmp,
                                            contentDescription = candidate.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Text(
                                            text = candidate.title.take(2).uppercase(),
                                            fontFamily = OrbitronFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TitaniumWhite
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = candidate.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TitaniumWhite,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${candidate.genreAr} • ${candidate.packageName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SilverMist,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            if (candidate.isAddedToMyGames) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MatrixGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "مضافة",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MatrixGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { onAddCandidate(candidate) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CyberCyan,
                                        contentColor = ObsidianBlack
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("add_candidate_btn_${candidate.packageName}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "إضافة",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

