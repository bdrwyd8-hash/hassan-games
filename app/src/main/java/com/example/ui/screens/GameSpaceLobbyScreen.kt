package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.GamePrepReport
import com.example.model.GameSpaceProfile
import com.example.model.InstalledAppCandidate
import com.example.model.LastSessionGameInfo
import com.example.model.NotificationShieldState
import com.example.model.PerformanceMode
import com.example.ui.components.AddGamePickerSheet
import com.example.ui.components.NotificationShieldBanner
import com.example.ui.components.OneTapGamePreparationCard
import com.example.ui.components.PerGameProfileSheet
import com.example.ui.components.QuickGameReconnectCard
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameSpaceLobbyScreen(
    games: List<GameSpaceProfile>,
    selectedGameId: String,
    installedCandidates: List<InstalledAppCandidate>,
    showAddGamePicker: Boolean,
    lastSessionInfo: LastSessionGameInfo?,
    isPreparingGame: Boolean,
    lastGamePrepReport: GamePrepReport?,
    notificationShieldState: NotificationShieldState,
    onSelectGame: (String) -> Unit,
    onLaunchGame: (GameSpaceProfile) -> Unit,
    onUpdateGameProfile: (String, (GameSpaceProfile) -> GameSpaceProfile) -> Unit,
    onRemoveGame: (String) -> Unit,
    onOpenAddGamePicker: () -> Unit,
    onCloseAddGamePicker: () -> Unit,
    onRefreshInstalledApps: () -> Unit,
    onAddInstalledCandidate: (InstalledAppCandidate) -> Unit,
    onAddCustomGame: (String, String, String, PerformanceMode) -> Unit,
    onRunOneTapGamePreparation: (Boolean) -> Unit,
    onQuickReconnectLastGame: () -> Unit,
    onClearLastSessionRecord: () -> Unit,
    onToggleNotificationShield: (Boolean) -> Unit,
    onOpenDndPermissionSettings: () -> Unit,
    onLoadAppIcon: (String) -> ImageBitmap?
) {
    val selectedGame = games.find { it.id == selectedGameId } ?: games.firstOrNull()
    var editingGameProfileId by remember { mutableStateOf<String?>(null) }
    val editingProfile = games.find { it.id == editingGameProfileId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("game_space_lobby_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Universal Gaming Center Hero Banner (MY GAMES + ADD GAME)
        UniversalMyGamesHeroHeader(
            totalGamesCount = games.size,
            installedCount = games.count { it.isInstalled },
            selectedGame = selectedGame,
            onOpenAddGamePicker = onOpenAddGamePicker,
            onStartSelectedGame = {
                if (selectedGame != null) {
                    onLaunchGame(selectedGame)
                }
            },
            onOpenSelectedSettings = {
                if (selectedGame != null) {
                    editingGameProfileId = selectedGame.id
                }
            },
            onLoadAppIcon = onLoadAppIcon
        )

        // 2. Quick Reconnect Banner (if a game session was recently active)
        if (lastSessionInfo != null) {
            QuickGameReconnectCard(
                lastSessionInfo = lastSessionInfo,
                onQuickReconnect = onQuickReconnectLastGame,
                onOpenMyGames = onClearLastSessionRecord
            )
        }

        // 3. MY GAMES Section Header + "+ ADD GAME" Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = CrimsonRed,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "MY GAMES • ألعابي المضافة (${games.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "اختر أي لعبة لتخصيص إعداداتها أو اضغط START لتشغيلها مع Gaming Mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist,
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = onOpenAddGamePicker,
                modifier = Modifier
                    .height(40.dp)
                    .testTag("my_games_add_game_btn"),
                shape = CutCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = ObsidianBlack
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+ ADD GAME",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // 4. MY GAMES Cards List (Supports ANY game added by user)
        if (games.isEmpty()) {
            EmptyMyGamesCard(onOpenAddGamePicker = onOpenAddGamePicker)
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.testTag("my_games_list")
            ) {
                games.forEach { game ->
                    val isSelected = game.id == selectedGame?.id
                    val appIconBitmap = remember(game.packageName, game.isInstalled) {
                        onLoadAppIcon(game.packageName)
                    }
                    MyGameCenterCard(
                        game = game,
                        isSelected = isSelected,
                        appIconBitmap = appIconBitmap,
                        onSelect = { onSelectGame(game.id) },
                        onStartGame = {
                            onSelectGame(game.id)
                            onLaunchGame(game)
                        },
                        onOpenSettings = {
                            onSelectGame(game.id)
                            editingGameProfileId = game.id
                        },
                        onSelectPerformanceMode = { mode ->
                            onUpdateGameProfile(game.id) { profile ->
                                profile.copy(
                                    recommendedMode = mode,
                                    recommendedFps = mode.targetFps
                                )
                            }
                        },
                        onRemoveGame = { onRemoveGame(game.id) }
                    )
                }
            }
        }

        // 5. Big Add Another Game Button at bottom of MY GAMES list
        OutlinedButton(
            onClick = onOpenAddGamePicker,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("add_more_games_bottom_btn"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, CyberCyan.copy(alpha = 0.85f))
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "+ ADD GAME • إضافة لعبة من تطبيقات الجهاز أو لعبة مخصصة",
                style = MaterialTheme.typography.labelLarge,
                color = CyberCyan,
                fontWeight = FontWeight.Bold
            )
        }

        // 6. One-Tap Game Preparation Card for the currently selected game
        OneTapGamePreparationCard(
            isPreparing = isPreparingGame,
            lastReport = lastGamePrepReport,
            activeGameTitle = selectedGame?.title,
            onExecuteOneTapPrep = { onRunOneTapGamePreparation(false) }
        )

        // 7. Notification Shield (DND) Card
        NotificationShieldBanner(
            shieldState = notificationShieldState,
            onToggleShield = { onToggleNotificationShield(!notificationShieldState.enabled) },
            onRequestDndPermission = onOpenDndPermissionSettings
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Per-Game Profile Settings Sheet
    if (editingProfile != null) {
        PerGameProfileSheet(
            profile = editingProfile,
            onDismiss = { editingGameProfileId = null },
            onSaveProfile = { updated ->
                onUpdateGameProfile(editingProfile.id) { updated }
            },
            onLaunchWithProfile = { updated ->
                editingGameProfileId = null
                onLaunchGame(updated)
            },
            onDeleteFromMyGames = { targetId ->
                editingGameProfileId = null
                onRemoveGame(targetId)
            }
        )
    }

    // "+ ADD GAME" Installed Apps & Custom Game Picker Sheet
    if (showAddGamePicker) {
        AddGamePickerSheet(
            candidates = installedCandidates,
            onRefreshCandidates = onRefreshInstalledApps,
            onAddCandidate = onAddInstalledCandidate,
            onAddCustomGame = { title, pkg, genre, mode ->
                onAddCustomGame(title, pkg, genre, mode)
                onCloseAddGamePicker()
            },
            onLoadAppIcon = onLoadAppIcon,
            onDismiss = onCloseAddGamePicker
        )
    }
}

@Composable
private fun UniversalMyGamesHeroHeader(
    totalGamesCount: Int,
    installedCount: Int,
    selectedGame: GameSpaceProfile?,
    onOpenAddGamePicker: () -> Unit,
    onStartSelectedGame: () -> Unit,
    onOpenSelectedSettings: () -> Unit,
    onLoadAppIcon: (String) -> ImageBitmap?
) {
    val accent = selectedGame?.let { Color(it.accentHex) } ?: CrimsonRed
    val selectedIcon = remember(selectedGame?.packageName, selectedGame?.isInstalled) {
        selectedGame?.let { onLoadAppIcon(it.packageName) }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("universal_my_games_hero"),
        shape = CutCornerShape(topStart = 20.dp, bottomEnd = 20.dp, topEnd = 8.dp, bottomStart = 8.dp),
        color = GunmetalCard,
        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.85f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.22f),
                            ObsidianSurface,
                            ObsidianBlack
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HASSAN GAMES • UNIVERSAL GAMING CENTER",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                        Text(
                            text = "مركز الألعاب الشامل — يدعم تشغيل وإدارة أي لعبة على جهازك",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ObsidianSurface,
                        border = BorderStroke(1.dp, MatrixGreen.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "$installedCount/$totalGamesCount جاهزة",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatrixGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (selectedGame != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ObsidianSurface.copy(alpha = 0.9f))
                            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Game Icon
                        GameIconAvatar(
                            title = selectedGame.title,
                            accent = accent,
                            iconBitmap = selectedIcon,
                            sizeDp = 56
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedGame.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TitaniumWhite,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background( selectedGame.recommendedMode.primaryColor.copy(alpha = 0.2f))
                                        .border(
                                            1.dp,
                                            selectedGame.recommendedMode.primaryColor,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${selectedGame.recommendedMode.badgeText} • ${selectedGame.recommendedFps}FPS",
                                        fontFamily = OrbitronFontFamily,
                                        fontSize = 9.sp,
                                        color = selectedGame.recommendedMode.primaryColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "${selectedGame.genreAr} • ${if (selectedGame.isInstalled) "مثبتة وجاهزة للتشغيل الفوري" else "ملف إعدادات مخصص (${selectedGame.packageName})"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selectedGame.isInstalled) MatrixGreen else SilverMist,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onStartSelectedGame,
                            modifier = Modifier
                                .weight(1.4f)
                                .height(46.dp)
                                .testTag("hero_start_selected_game_btn"),
                            shape = CutCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MatrixGreen,
                                contentColor = ObsidianBlack
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "START • بدء اللعبة الآن",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenSelectedSettings,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("hero_settings_selected_game_btn"),
                            shape = CutCornerShape(10.dp),
                            border = BorderStroke(1.2.dp, CyberCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إعدادات اللعبة",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MyGameCenterCard(
    game: GameSpaceProfile,
    isSelected: Boolean,
    appIconBitmap: ImageBitmap?,
    onSelect: () -> Unit,
    onStartGame: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectPerformanceMode: (PerformanceMode) -> Unit,
    onRemoveGame: () -> Unit
) {
    val accent = Color(game.accentHex)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("my_game_card_${game.id}"),
        shape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp, topEnd = 6.dp, bottomStart = 6.dp),
        color = if (isSelected) GunmetalElevated else GunmetalCard,
        border = BorderStroke(
            width = if (isSelected) 1.8.dp else 1.dp,
            color = if (isSelected) accent else CarbonBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Game Icon + Name + Status + Settings/Remove Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameIconAvatar(
                    title = game.title,
                    accent = accent,
                    iconBitmap = appIconBitmap,
                    sizeDp = 52
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = game.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (game.isInstalled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "مثبتة",
                                tint = MatrixGreen,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Text(
                        text = "${game.genreAr}  •  ${game.packageName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("game_settings_btn_${game.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "إعدادات ملف اللعبة",
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onRemoveGame,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("game_remove_btn_${game.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف من ألعابي",
                        tint = SilverMist.copy(alpha = 0.75f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Performance Profile Quick Selector Row (Per-Game Performance Profile)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الأداء:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist,
                    fontSize = 11.sp
                )
                PerformanceMode.entries.forEach { mode ->
                    val active = game.recommendedMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (active) mode.primaryColor.copy(alpha = 0.22f) else ObsidianSurface)
                            .border(
                                1.dp,
                                if (active) mode.primaryColor else CarbonBorder,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onSelectPerformanceMode(mode) }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.badgeText,
                            fontFamily = OrbitronFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) mode.primaryColor else SilverMist
                        )
                    }
                }
            }

            // Active Game Profile Features Summary Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (game.autoCleanRamBeforeLaunch) {
                    MiniFeatureChip("تنظيف RAM تلقائي", MatrixGreen)
                }
                if (game.sidebarOverlayEnabled) {
                    MiniFeatureChip("الشريط الجانبي", CyberCyan)
                }
                if (game.fpsOverlayEnabled) {
                    MiniFeatureChip("عداد ${game.recommendedFps} FPS", MatrixGreen)
                }
                if (game.notificationShieldEnabled) {
                    MiniFeatureChip("منع الإزعاج", MoltenAmber)
                }
                if (game.crosshairEnabled) {
                    MiniFeatureChip("Crosshair", CrimsonRed)
                }
                if (game.wiFiPriorityEnabled) {
                    MiniFeatureChip("أولوية الشبكة", CyberCyan)
                }
            }

            // Action Buttons: START + SETTINGS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartGame,
                    modifier = Modifier
                        .weight(1.45f)
                        .height(44.dp)
                        .testTag("start_game_btn_${game.id}"),
                    shape = CutCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (game.isInstalled) MatrixGreen else accent,
                        contentColor = if (game.isInstalled) ObsidianBlack else Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (game.isInstalled) "START • تشغيل اللعبة" else "START • تجهيز وتشغيل",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("settings_game_btn_${game.id}"),
                    shape = CutCornerShape(10.dp),
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PROFILE • الإعدادات",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun GameIconAvatar(
    title: String,
    accent: Color,
    iconBitmap: ImageBitmap?,
    sizeDp: Int
) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.32f), ObsidianBlack)
                )
            )
            .border(1.5.dp, accent, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        } else {
            val initials = title
                .trim()
                .split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.take(1).uppercase() }
                .ifBlank { "G" }
            Text(
                text = initials,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = (sizeDp / 3).sp,
                color = TitaniumWhite
            )
        }
    }
}

@Composable
private fun MiniFeatureChip(
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyMyGamesCard(
    onOpenAddGamePicker: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_my_games_card"),
        shape = RoundedCornerShape(16.dp),
        color = GunmetalCard,
        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SportsEsports,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = "قائمة ألعابك فارغة حالياً",
                style = MaterialTheme.typography.titleMedium,
                color = TitaniumWhite,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "اضغط على (+ ADD GAME) لاختيار أي لعبة أو تطبيق مثبت على جهازك وإضافته إلى MY GAMES مع ملف أداء مستقل.",
                style = MaterialTheme.typography.bodySmall,
                color = SilverMist
            )
            Button(
                onClick = onOpenAddGamePicker,
                shape = CutCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = ObsidianBlack
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "+ ADD GAME • إضافة أول لعبة",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
