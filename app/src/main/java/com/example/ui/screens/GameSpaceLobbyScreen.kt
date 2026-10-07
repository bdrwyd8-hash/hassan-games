package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.CrosshairStyle
import com.example.model.GameSpaceProfile
import com.example.model.OneTapGamePrepStatus
import com.example.model.PerformanceMode
import com.example.model.SmartThermalMode
import com.example.ui.components.OneTapGamePrepCard
import com.example.ui.components.QuickGameReconnectBanner
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

@Composable
fun GameSpaceLobbyScreen(
    gameCatalog: List<GameSpaceProfile>,
    activeProfile: GameSpaceProfile?,
    activeSession: ActiveGameSession = ActiveGameSession(),
    oneTapPrepStatus: OneTapGamePrepStatus = OneTapGamePrepStatus(),
    onLaunchWithProfile: (GameSpaceProfile) -> Unit,
    onApplyProfileOnly: (GameSpaceProfile) -> Unit = {},
    onSaveOrUpdateProfile: (GameSpaceProfile) -> Unit = {},
    onAddCustomGame: (title: String, pkg: String, genre: String, mode: PerformanceMode, fps: Int) -> Unit = { _, _, _, _, _ -> },
    onDuplicateProfile: (GameSpaceProfile) -> Unit = {},
    onResetProfile: (String) -> Unit = {},
    onRunOneTapPrep: () -> Unit = {},
    onQuickReconnect: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var editingProfile by remember { mutableStateOf<GameSpaceProfile?>(null) }
    var showAddGameDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("game_space_lobby_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header + Add Custom Game Profile Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                CrimsonRed.copy(alpha = 0.22f),
                                CarbonCard,
                                CyberCyan.copy(alpha = 0.14f)
                            )
                        )
                    )
                    .border(1.dp, CrimsonRed.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
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
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CrimsonRed.copy(alpha = 0.2f))
                                    .border(1.dp, CrimsonRed, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = "مدير بروفايلات الألعاب",
                                    tint = CrimsonRed
                                )
                            }
                            Column {
                                Text(
                                    text = "نظام البروفايلات المخصصة لكل لعبة (Per-Game Profiles)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TitaniumWhite,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "يتعرف تلقائياً على اللعبة ويطبق إعداداتها الخاصة (الأداء، الحرارة، الإشعارات، والعدادات)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SilverMuted
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (activeProfile != null) {
                            Text(
                                text = "✓ النشط حالياً: ${activeProfile.title}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MatrixGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Button(
                            onClick = { showAddGameDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_custom_game_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إضافة لعبة مخصصة",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // 2. Quick Game Reconnect & One-Tap Prep for Active Game
        item {
            QuickGameReconnectBanner(
                session = activeSession,
                onQuickReconnect = onQuickReconnect,
                onOpenGameProfiles = {
                    activeProfile?.let { editingProfile = it }
                }
            )
        }

        item {
            OneTapGamePrepCard(
                prepStatus = oneTapPrepStatus,
                activeGameTitle = activeProfile?.title,
                onRunOneTapPrep = onRunOneTapPrep
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "قائمة بروفايلات الألعاب (${gameCatalog.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = TitaniumWhite,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "يمكنك تعديل إعدادات كل لعبة، حفظها، نسخها، أو إعادة ضبطها بضغطة واحدة",
                    style = MaterialTheme.typography.labelSmall,
                    color = SilverMuted
                )
            }
        }

        items(gameCatalog, key = { it.id }) { profile ->
            val isActive = activeProfile?.id == profile.id
            PerGameProfileCard(
                profile = profile,
                isActive = isActive,
                onPrepareAndLaunch = { onLaunchWithProfile(profile) },
                onApplyOnly = { onApplyProfileOnly(profile) },
                onEditProfile = { editingProfile = profile },
                onDuplicateProfile = { onDuplicateProfile(profile) },
                onResetProfile = { onResetProfile(profile.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Editor for Per-Game Profile
    editingProfile?.let { target ->
        PerGameProfileEditorDialog(
            initialProfile = target,
            onDismiss = { editingProfile = null },
            onSaveProfile = { updated ->
                onSaveOrUpdateProfile(updated)
                editingProfile = null
            }
        )
    }

    // Add Custom Game Profile Dialog
    if (showAddGameDialog) {
        AddCustomGameDialog(
            onDismiss = { showAddGameDialog = false },
            onConfirmAdd = { title, pkg, genre, mode, fps ->
                onAddCustomGame(title, pkg, genre, mode, fps)
                showAddGameDialog = false
            }
        )
    }
}

@Composable
private fun PerGameProfileCard(
    profile: GameSpaceProfile,
    isActive: Boolean,
    onPrepareAndLaunch: () -> Unit,
    onApplyOnly: () -> Unit,
    onEditProfile: () -> Unit,
    onDuplicateProfile: () -> Unit,
    onResetProfile: () -> Unit
) {
    val accent = Color(profile.accentHex)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CarbonCard)
            .border(
                width = if (isActive) 1.6.dp else 1.dp,
                color = if (isActive) MatrixGreen else accent.copy(alpha = 0.45f),
                shape = RoundedCornerShape(18.dp)
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
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Column {
                    Text(
                        text = profile.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${profile.genreAr} • ${profile.packageName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SilverMuted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(alpha = 0.16f))
                    .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${profile.targetFps} FPS • ${profile.recommendedMode.englishBadge}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = accent,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Enabled Per-Game Tools Summary Chips
        val enabledBadges = buildList {
            add("الحرارة: ${profile.smartThermalMode.badge}")
            if (profile.notificationShieldEnabled) add("🛡️ درع الإشعارات")
            if (profile.crosshairEnabled) add("🎯 تصويب")
            if (profile.magnifierEnabled) add("🔍 عدسة ${(profile.magnifierZoom * 10).roundToInt() / 10f}x")
            if (profile.touchProtectionEnabled) add("🔒 حماية اللمس")
            if (profile.gamingSidebarEnabled) add("⚡ الشريط الجانبي")
            if (profile.fpsOverlayEnabled) add("📊 FPS")
            if (profile.tempOverlayEnabled) add("🌡️ Temp")
            if (profile.ramOverlayEnabled) add("💾 RAM")
            if (profile.triggersEnabled) add("🎮 L1/R1")
            if (profile.clonedButtonsEnabled) add("🔘 أزرار منسوخة")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CarbonSurfaceVariant)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "الأدوات المفعّلة لهذا البروفايل:",
                style = MaterialTheme.typography.labelSmall,
                color = CyberCyan,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = enabledBadges.joinToString("  •  "),
                style = MaterialTheme.typography.labelSmall,
                color = TitaniumWhite
            )
            Text(
                text = "L1: ${profile.l1ActionAr} (${(profile.l1X * 100).toInt()}%, ${(profile.l1Y * 100).toInt()}%)  |  R1: ${profile.r1ActionAr}",
                style = MaterialTheme.typography.labelSmall,
                color = SilverMuted
            )
        }

        // Primary Actions: Prepare & Launch + Apply Profile
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onPrepareAndLaunch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.4f)
                    .height(48.dp)
                    .testTag("launch_game_${profile.id}")
            ) {
                Icon(
                    imageVector = if (profile.isInstalledOnDevice) Icons.Default.PlayArrow else Icons.Default.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (profile.isInstalledOnDevice) "تجهيز وتشغيل اللعبة" else "تجهيز وتجربة البروفايل",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            OutlinedButton(
                onClick = onApplyOnly,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(0.9f)
                    .height(48.dp)
                    .testTag("apply_profile_${profile.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isActive) MatrixGreen else CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isActive) "مطبّق ✓" else "تطبيق فقط",
                    style = MaterialTheme.typography.labelSmall,
                    color = TitaniumWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Profile Management Toolbar: Edit / Duplicate / Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onEditProfile,
                modifier = Modifier.testTag("edit_profile_${profile.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "تخصيص الإعدادات",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = onDuplicateProfile,
                modifier = Modifier.testTag("duplicate_profile_${profile.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = TitaniumWhite,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "نسخ البروفايل",
                    style = MaterialTheme.typography.labelSmall,
                    color = TitaniumWhite
                )
            }

            TextButton(
                onClick = onResetProfile,
                modifier = Modifier.testTag("reset_profile_${profile.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = MoltenAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (profile.isCustomAdded) "حذف" else "إعادة ضبط",
                    style = MaterialTheme.typography.labelSmall,
                    color = MoltenAmber
                )
            }
        }
    }
}

@Composable
private fun PerGameProfileEditorDialog(
    initialProfile: GameSpaceProfile,
    onDismiss: () -> Unit,
    onSaveProfile: (GameSpaceProfile) -> Unit
) {
    var mode by remember { mutableStateOf(initialProfile.recommendedMode) }
    var thermalMode by remember { mutableStateOf(initialProfile.smartThermalMode) }
    var advisorMode by remember { mutableStateOf(initialProfile.advisorMode) }
    var notifShield by remember { mutableStateOf(initialProfile.notificationShieldEnabled) }
    var magnifier by remember { mutableStateOf(initialProfile.magnifierEnabled) }
    var magZoom by remember { mutableFloatStateOf(initialProfile.magnifierZoom) }
    var crosshair by remember { mutableStateOf(initialProfile.crosshairEnabled) }
    var touchProtect by remember { mutableStateOf(initialProfile.touchProtectionEnabled) }
    var sidebar by remember { mutableStateOf(initialProfile.gamingSidebarEnabled) }
    var fpsOverlay by remember { mutableStateOf(initialProfile.fpsOverlayEnabled) }
    var tempOverlay by remember { mutableStateOf(initialProfile.tempOverlayEnabled) }
    var ramOverlay by remember { mutableStateOf(initialProfile.ramOverlayEnabled) }
    var triggers by remember { mutableStateOf(initialProfile.triggersEnabled) }
    var clonedBtns by remember { mutableStateOf(initialProfile.clonedButtonsEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = CyberCyan)
                Text(
                    text = "تخصيص بروفايل: ${initialProfile.title}",
                    style = MaterialTheme.typography.titleSmall,
                    color = TitaniumWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "1. وضع الأداء المفضّل للعبة (Performance Mode):",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PerformanceMode.entries.forEach { m ->
                            val sel = mode == m
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) m.color.copy(alpha = 0.25f) else CarbonSurfaceVariant)
                                    .border(1.dp, if (sel) m.color else SteelBorder, RoundedCornerShape(8.dp))
                                    .clickable { mode = m }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = m.englishBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (sel) m.color else TitaniumWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "2. الوضع الحراري الذكي (Smart Thermal Mode):",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SmartThermalMode.entries.forEach { tm ->
                            val sel = thermalMode == tm
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) MatrixGreen.copy(alpha = 0.22f) else CarbonSurfaceVariant)
                                    .border(1.dp, if (sel) MatrixGreen else SteelBorder, RoundedCornerShape(8.dp))
                                    .clickable { thermalMode = tm }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tm.badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (sel) MatrixGreen else TitaniumWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    ProfileSwitchRow("🛡️ درع منع الإشعارات (Notification Shield)", notifShield) { notifShield = it }
                    ProfileSwitchRow("🔍 عدسة التكبير التكتيكية (Magnifier)", magnifier) { magnifier = it }
                    AnimatedVisibility(visible = magnifier) {
                        Slider(
                            value = magZoom,
                            onValueChange = { magZoom = it },
                            valueRange = 1.5f..3.5f,
                            colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                        )
                    }
                    ProfileSwitchRow("🎯 مؤشر التصويب (Crosshair)", crosshair) { crosshair = it }
                    ProfileSwitchRow("🔒 حماية اللمس الخاطئ (Touch Protection)", touchProtect) { touchProtect = it }
                    ProfileSwitchRow("⚡ الشريط الجانبي للألعاب (Gaming Sidebar)", sidebar) { sidebar = it }
                    ProfileSwitchRow("📊 عداد الفريمات العائم (FPS Overlay)", fpsOverlay) { fpsOverlay = it }
                    ProfileSwitchRow("🌡️ عداد الحرارة العائم (Temp Overlay)", tempOverlay) { tempOverlay = it }
                    ProfileSwitchRow("💾 عداد الرام العائم (RAM Overlay)", ramOverlay) { ramOverlay = it }
                    ProfileSwitchRow("🎮 أزرار الكتف الصوتية (L1/R1 Triggers)", triggers) { triggers = it }
                    ProfileSwitchRow("🔘 الأزرار المنسوخة المتحركة (Cloned Buttons)", clonedBtns) { clonedBtns = it }
                }

                item {
                    Text(
                        text = "3. توصية مستشار الجرافيك لهذه اللعبة:",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AdvisorPresetMode.entries.forEach { adv ->
                            val sel = advisorMode == adv
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) adv.badgeColor.copy(alpha = 0.22f) else CarbonSurfaceVariant)
                                    .border(1.dp, if (sel) adv.badgeColor else SteelBorder, RoundedCornerShape(8.dp))
                                    .clickable { advisorMode = adv }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = adv.arabicTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TitaniumWhite,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveProfile(
                        initialProfile.copy(
                            recommendedMode = mode,
                            smartThermalMode = thermalMode,
                            advisorMode = advisorMode,
                            notificationShieldEnabled = notifShield,
                            magnifierEnabled = magnifier,
                            magnifierZoom = magZoom,
                            crosshairEnabled = crosshair,
                            touchProtectionEnabled = touchProtect,
                            gamingSidebarEnabled = sidebar,
                            fpsOverlayEnabled = fpsOverlay,
                            tempOverlayEnabled = tempOverlay,
                            ramOverlayEnabled = ramOverlay,
                            triggersEnabled = triggers,
                            clonedButtonsEnabled = clonedBtns
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen, contentColor = Color.Black),
                modifier = Modifier.testTag("save_profile_dialog_button")
            ) {
                Text("حفظ وتطبيق البروفايل", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = SilverMuted)
            }
        }
    )
}

@Composable
private fun ProfileSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TitaniumWhite,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyberCyan,
                checkedTrackColor = CyberCyan.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun AddCustomGameDialog(
    onDismiss: () -> Unit,
    onConfirmAdd: (title: String, pkg: String, genre: String, mode: PerformanceMode, fps: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var pkg by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("أكشن / تنافسي") }
    var selectedMode by remember { mutableStateOf(PerformanceMode.DIABLO) }
    var targetFps by remember { mutableIntStateOf(90) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Text(
                text = "إضافة لعبة جديدة إلى البروفايلات",
                style = MaterialTheme.typography.titleSmall,
                color = TitaniumWhite,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم اللعبة (مثال: Brawl Stars / Minecraft)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_game_title_input")
                )
                OutlinedTextField(
                    value = pkg,
                    onValueChange = { pkg = it },
                    label = { Text("اسم الحزمة الاختياري (Package Name)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("نوع اللعبة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "الهدف الإطاري: $targetFps FPS",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyberCyan
                )
                Slider(
                    value = targetFps.toFloat(),
                    onValueChange = { targetFps = it.roundToInt() },
                    valueRange = 60f..120f,
                    steps = 1
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmAdd(title, pkg, genre, selectedMode, targetFps) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                modifier = Modifier.testTag("confirm_add_custom_game_button")
            ) {
                Text("إضافة وحفظ البروفايل", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = SilverMuted)
            }
        }
    )
}
