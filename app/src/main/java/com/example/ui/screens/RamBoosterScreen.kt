package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BoostResult
import com.example.model.HardwareTelemetry
import com.example.model.InstalledAppProcess
import com.example.model.LowEndOptimizerConfig
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
import kotlin.math.roundToInt

@Composable
fun RamBoosterScreen(
    telemetry: HardwareTelemetry,
    lowEndConfig: LowEndOptimizerConfig,
    backgroundApps: List<InstalledAppProcess>,
    isBoosting: Boolean,
    boostProgress: Float,
    boostStageText: String,
    lastBoostResult: BoostResult?,
    onRunSuperBoost: () -> Unit,
    onStopSingleApp: (InstalledAppProcess) -> Unit,
    onToggleWhitelist: (String) -> Unit,
    onOpenSystemAppInfo: (String) -> Unit,
    onRefreshApps: () -> Unit,
    onUpdateLowEndConfig: ((LowEndOptimizerConfig) -> LowEndOptimizerConfig) -> Unit
) {
    var filterTab by remember { mutableIntStateOf(0) } // 0 = All, 1 = Active, 2 = Whitelisted

    val filteredApps = remember(backgroundApps, filterTab) {
        when (filterTab) {
            1 -> backgroundApps.filter { !it.isStopped && !it.isWhitelisted }
            2 -> backgroundApps.filter { it.isWhitelisted }
            else -> backgroundApps
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ram_booster_list"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. RAM & Cache Overview + Kill All & Purge Action Card
        item {
            RamAndCacheSummaryCard(
                telemetry = telemetry,
                backgroundApps = backgroundApps,
                isBoosting = isBoosting,
                boostProgress = boostProgress,
                boostStageText = boostStageText,
                lastBoostResult = lastBoostResult,
                onRunSuperBoost = onRunSuperBoost
            )
        }

        // 2. Auto Background Cache & Process Cleaner Configuration Card
        item {
            AutoBackgroundCleanerCard(
                lowEndConfig = lowEndConfig,
                onUpdateLowEndConfig = onUpdateLowEndConfig
            )
        }

        // 3. Background Apps Filter Header
        item {
            BackgroundAppsListHeader(
                totalCount = backgroundApps.size,
                activeCount = backgroundApps.count { !it.isStopped && !it.isWhitelisted },
                whitelistedCount = backgroundApps.count { it.isWhitelisted },
                selectedFilter = filterTab,
                onSelectFilter = { filterTab = it },
                onRefresh = onRefreshApps
            )
        }

        // 4. Individual Installed / Running Apps List
        items(
            items = filteredApps,
            key = { it.packageName }
        ) { app ->
            BackgroundAppItemCard(
                app = app,
                onStopApp = { onStopSingleApp(app) },
                onToggleWhitelist = { onToggleWhitelist(app.packageName) },
                onOpenAppInfo = { onOpenSystemAppInfo(app.packageName) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RamAndCacheSummaryCard(
    telemetry: HardwareTelemetry,
    backgroundApps: List<InstalledAppProcess>,
    isBoosting: Boolean,
    boostProgress: Float,
    boostStageText: String,
    lastBoostResult: BoostResult?,
    onRunSuperBoost: () -> Unit
) {
    val activeBgCount = backgroundApps.count { !it.isStopped && !it.isWhitelisted }
    val reclaimableRamMb = backgroundApps
        .filter { !it.isStopped && !it.isWhitelisted }
        .sumOf { (it.estimatedRamMb * 0.65).toInt() }
        .coerceAtLeast(85)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp),
        color = GunmetalCard,
        border = BorderStroke(1.5.dp, CrimsonRed.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CutCornerShape(10.dp))
                            .background(CrimsonRed.copy(alpha = 0.18f))
                            .border(1.dp, CrimsonRed, CutCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = CrimsonRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "مُفرّغ الرام وذاكرة التخزين المؤقت (Cache)",
                            style = MaterialTheme.typography.titleMedium,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "إيقاف البرامج بالخلفية لتحرير المعالج والرام للأجهزة الضعيفة",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Metric Boxes: Used RAM, Cache to Clean, Reclaimable RAM
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MemoryStatBox(
                    label = "الرام المستخدم",
                    value = "${telemetry.ramUsedMb} MB",
                    sub = "من أصل ${telemetry.ramTotalMb} MB",
                    color = if (telemetry.ramUsagePercent > 80) CrimsonRed else CyberCyan,
                    modifier = Modifier.weight(1f)
                )
                MemoryStatBox(
                    label = "الكاش المؤقت",
                    value = "${telemetry.cacheEstimatedMb} MB",
                    sub = "ملفات خلفية مؤقتة",
                    color = MoltenAmber,
                    modifier = Modifier.weight(1f)
                )
                MemoryStatBox(
                    label = "قابل للتحرير",
                    value = "+$reclaimableRamMb MB",
                    sub = "$activeBgCount تطبيق بالخلفية",
                    color = MatrixGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // RAM Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "نسبة إشغال الذاكرة العشوائية (RAM)",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist
                )
                Text(
                    text = "${telemetry.ramUsagePercent}% (${telemetry.ramAvailableMb} MB حر)",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 11.sp,
                    color = TitaniumWhite,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (telemetry.ramUsagePercent / 100f).coerceIn(0.05f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (telemetry.ramUsagePercent > 80) CrimsonRed else CyberCyan,
                trackColor = ObsidianBlack
            )

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(visible = isBoosting) {
                Column(modifier = Modifier.padding(bottom = 10.dp)) {
                    Text(
                        text = boostStageText,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { boostProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MatrixGreen,
                        trackColor = ObsidianBlack
                    )
                }
            }

            Button(
                onClick = onRunSuperBoost,
                enabled = !isBoosting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("ram_screen_purge_button"),
                shape = CutCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonRed,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "مسح الكاش وإيقاف جميع برامج الخلفية الآن",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (lastBoostResult != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✓ تم إيقاف ${lastBoostResult.stoppedAppsCount} تطبيقات وتحرير ${lastBoostResult.freedRamMb} MB رام ومسح ${lastBoostResult.cleanedCacheMb} MB كاش",
                    style = MaterialTheme.typography.bodySmall,
                    color = MatrixGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MemoryStatBox(
    label: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ObsidianSurface)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SilverMist,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = color
        )
        Text(
            text = sub,
            style = MaterialTheme.typography.bodySmall,
            color = TitaniumWhite,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun AutoBackgroundCleanerCard(
    lowEndConfig: LowEndOptimizerConfig,
    onUpdateLowEndConfig: ((LowEndOptimizerConfig) -> LowEndOptimizerConfig) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = GunmetalCard,
        border = BorderStroke(
            1.dp,
            if (lowEndConfig.autoCleanInBackground) MatrixGreen.copy(alpha = 0.6f) else CarbonBorder
        )
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
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = null,
                        tint = MatrixGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "المسح التلقائي للكاش والرام بالخلفية",
                            style = MaterialTheme.typography.titleSmall,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ينظف الذاكرة المؤقتة ويمنع التقطيع تلقائياً أثناء اللعب بدون إزعاج",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist
                        )
                    }
                }
                Switch(
                    checked = lowEndConfig.autoCleanInBackground,
                    onCheckedChange = { enabled ->
                        onUpdateLowEndConfig { it.copy(autoCleanInBackground = enabled) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = MatrixGreen
                    ),
                    modifier = Modifier.testTag("auto_clean_bg_switch")
                )
            }

            if (lowEndConfig.autoCleanInBackground) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "بدء التنظيف التلقائي إذا تجاوز استهلاك الرام:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist
                    )
                    Text(
                        text = "${lowEndConfig.ramThresholdPercent}%",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 12.sp,
                        color = MatrixGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = lowEndConfig.ramThresholdPercent.toFloat(),
                    onValueChange = { v ->
                        onUpdateLowEndConfig { it.copy(ramThresholdPercent = v.roundToInt()) }
                    },
                    valueRange = 60f..90f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = MatrixGreen,
                        activeTrackColor = MatrixGreen
                    )
                )

                // Interval chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "دورة الفحص:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist
                    )
                    listOf(30 to "30 ثانية", 45 to "45 ثانية", 60 to "دقيقة", 120 to "دقيقتان").forEach { (sec, label) ->
                        val selected = lowEndConfig.autoCleanIntervalSec == sec
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (selected) MatrixGreen.copy(alpha = 0.22f) else ObsidianSurface
                                )
                                .border(
                                    1.dp,
                                    if (selected) MatrixGreen else CarbonBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    onUpdateLowEndConfig { it.copy(autoCleanIntervalSec = sec) }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selected) MatrixGreen else SilverMist,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BackgroundAppsListHeader(
    totalCount: Int,
    activeCount: Int,
    whitelistedCount: Int,
    selectedFilter: Int,
    onSelectFilter: (Int) -> Unit,
    onRefresh: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "البرامج والعمليات بالخلفية ($totalCount)",
                style = MaterialTheme.typography.titleMedium,
                color = TitaniumWhite,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "تحديث القائمة",
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterTabPill(
                text = "الكل ($totalCount)",
                selected = selectedFilter == 0,
                onClick = { onSelectFilter(0) },
                modifier = Modifier.weight(1f)
            )
            FilterTabPill(
                text = "نشط بالخلفية ($activeCount)",
                selected = selectedFilter == 1,
                onClick = { onSelectFilter(1) },
                modifier = Modifier.weight(1f)
            )
            FilterTabPill(
                text = "محمي ($whitelistedCount)",
                selected = selectedFilter == 2,
                onClick = { onSelectFilter(2) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FilterTabPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) CyberCyan.copy(alpha = 0.2f) else GunmetalCard)
            .border(1.dp, if (selected) CyberCyan else CarbonBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) CyberCyan else SilverMist,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BackgroundAppItemCard(
    app: InstalledAppProcess,
    onStopApp: () -> Unit,
    onToggleWhitelist: () -> Unit,
    onOpenAppInfo: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = GunmetalCard,
        border = BorderStroke(
            1.dp,
            when {
                app.isStopped -> MatrixGreen.copy(alpha = 0.35f)
                app.isWhitelisted -> CyberCyan.copy(alpha = 0.45f)
                else -> CarbonBorder
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (app.isStopped) MatrixGreen.copy(alpha = 0.15f)
                            else CrimsonRed.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.appName.take(1).uppercase(),
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (app.isStopped) MatrixGreen else CrimsonRed
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleSmall,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ObsidianSurface)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = app.categoryTag,
                                style = MaterialTheme.typography.bodySmall,
                                color = SilverMist,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    if (app.isStopped) {
                        Text(
                            text = "✓ تم إيقاف العمليات بالخلفية ومسح الكاش",
                            style = MaterialTheme.typography.bodySmall,
                            color = MatrixGreen,
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = "استهلاك الرام: ~${app.estimatedRamMb} MB  •  كاش: ${app.cacheSizeMb} MB",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Whitelist Shield Toggle
                IconButton(
                    onClick = onToggleWhitelist,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (app.isWhitelisted) Icons.Default.Shield else Icons.Default.Security,
                        contentDescription = "حماية من الإيقاف",
                        tint = if (app.isWhitelisted) CyberCyan else SilverMist.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // System App Info Button
                IconButton(
                    onClick = onOpenAppInfo,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "معلومات التطبيق بالنظام",
                        tint = SilverMist,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Stop Background Process Action
                if (app.isStopped) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "متوقف",
                        tint = MatrixGreen,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(22.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CrimsonRed.copy(alpha = 0.2f))
                            .border(1.dp, CrimsonRed, RoundedCornerShape(8.dp))
                            .clickable { onStopApp() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("stop_app_${app.packageName}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = CrimsonRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "إيقاف",
                                style = MaterialTheme.typography.labelMedium,
                                color = TitaniumWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
