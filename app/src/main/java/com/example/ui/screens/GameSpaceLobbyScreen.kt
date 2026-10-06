package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.model.GameSpaceProfile
import com.example.model.InstalledAppProcess
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GunmetalCard
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.SilverMist
import com.example.ui.theme.TitaniumWhite

@Composable
fun GameSpaceLobbyScreen(
    gameCatalog: List<GameSpaceProfile>,
    installedApps: List<InstalledAppProcess>,
    activeGameProfile: GameSpaceProfile?,
    isBoosting: Boolean,
    onBoostAndLaunchGame: (GameSpaceProfile) -> Unit,
    onBoostAndLaunchInstalledApp: (InstalledAppProcess) -> Unit
) {
    val userApps = installedApps.filter { !it.isSystemApp }.take(12)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("game_space_lobby_list"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp),
                color = GunmetalCard,
                border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CutCornerShape(10.dp))
                            .background(CrimsonRed.copy(alpha = 0.2f))
                            .border(1.dp, CrimsonRed, CutCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gamepad,
                            contentDescription = null,
                            tint = CrimsonRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "منصة ألعاب الريد ماجيك (Game Space Launcher)",
                            style = MaterialTheme.typography.titleMedium,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "اختر لعبتك لتفريغ الرام تلقائياً وضبط أزرار الكتف L1/R1 وتشغيل وضع القوة القصوى",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist
                        )
                    }
                }
            }
        }

        items(
            items = gameCatalog,
            key = { it.id }
        ) { profile ->
            GameSpaceProfileCard(
                profile = profile,
                isActiveProfile = activeGameProfile?.id == profile.id,
                isBoosting = isBoosting,
                onBoostAndLaunch = { onBoostAndLaunchGame(profile) }
            )
        }

        if (userApps.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "تطبيقات وألعاب مثبتة على جهازك (${userApps.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = TitaniumWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            items(
                items = userApps,
                key = { "installed_${it.packageName}" }
            ) { app ->
                InstalledDeviceGameRow(
                    app = app,
                    onLaunchWithBoost = { onBoostAndLaunchInstalledApp(app) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GameSpaceProfileCard(
    profile: GameSpaceProfile,
    isActiveProfile: Boolean,
    isBoosting: Boolean,
    onBoostAndLaunch: () -> Unit
) {
    val accentColor = Color(profile.accentHex)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp, topEnd = 4.dp, bottomStart = 4.dp),
        color = GunmetalCard,
        border = BorderStroke(
            width = if (isActiveProfile) 1.8.dp else 1.dp,
            color = if (isActiveProfile) accentColor else accentColor.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = profile.genreAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .border(1.dp, accentColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${profile.targetFps} FPS",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(profile.recommendedMode.color.copy(alpha = 0.2f))
                            .border(1.dp, profile.recommendedMode.color, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = profile.recommendedMode.englishBadge.replace(" MODE", ""),
                            fontFamily = OrbitronFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = profile.recommendedMode.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pre-mapped L1 & R1 Actions for this game
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurface)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "زر L1 (رفع الصوت)",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 9.sp,
                            color = CyberCyan
                        )
                        Text(
                            text = profile.l1ActionAr,
                            style = MaterialTheme.typography.bodySmall,
                            color = TitaniumWhite,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurface)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeDown,
                        contentDescription = null,
                        tint = CrimsonRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "زر R1 (خفض الصوت)",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 9.sp,
                            color = CrimsonRed
                        )
                        Text(
                            text = profile.r1ActionAr,
                            style = MaterialTheme.typography.bodySmall,
                            color = TitaniumWhite,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onBoostAndLaunch,
                enabled = !isBoosting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("launch_game_${profile.id}"),
                shape = CutCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = ObsidianBlack
                )
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (profile.isInstalledOnDevice)
                        "تقوية قصوى وتشغيل اللعبة الآن"
                    else
                        "تفعيل بروفايل L1/R1 وتقوية الجهاز للعبة",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InstalledDeviceGameRow(
    app: InstalledAppProcess,
    onLaunchWithBoost: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = GunmetalCard,
        border = BorderStroke(1.dp, CarbonBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleSmall,
                    color = TitaniumWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onLaunchWithBoost,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MatrixGreen,
                    contentColor = ObsidianBlack
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "تقوية وتشغيل",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
