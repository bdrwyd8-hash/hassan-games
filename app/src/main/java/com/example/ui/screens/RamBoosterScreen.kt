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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.model.BoostProcessItem
import com.example.model.BoostResultReport
import com.example.model.HardwareTelemetry
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
import kotlin.math.roundToInt

@Composable
fun RamBoosterScreen(
    telemetry: HardwareTelemetry,
    processes: List<BoostProcessItem>,
    isBoosting: Boolean,
    lastBoostReport: BoostResultReport?,
    onExecuteDeepBoost: () -> Unit,
    onToggleProcessWhitelist: (String) -> Unit,
    onTestNetworkPing: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("ram_booster_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. RAM & Cache Deep Booster Hero Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ram_booster_hero_card"),
            shape = CutCornerShape(topStart = 20.dp, bottomEnd = 20.dp, topEnd = 8.dp, bottomStart = 8.dp),
            color = GunmetalCard,
            border = BorderStroke(1.5.dp, CrimsonRed.copy(alpha = 0.8f))
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CutCornerShape(12.dp))
                                .background(CrimsonRed.copy(alpha = 0.18f))
                                .border(1.dp, CrimsonRed, CutCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = CrimsonRed,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "مُسرّع الرام ومنظف الكاش الفعلي",
                                style = MaterialTheme.typography.titleMedium,
                                color = TitaniumWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "تحرير الذاكرة العشوائية وإيقاف العمليات الخلفية غير المحمية",
                                style = MaterialTheme.typography.bodySmall,
                                color = SilverMist
                            )
                        }
                    }

                    Text(
                        text = "${telemetry.ramUsagePercent}%",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            telemetry.ramUsagePercent >= 80 -> CrimsonRed
                            telemetry.ramUsagePercent >= 65 -> MoltenAmber
                            else -> MatrixGreen
                        }
                    )
                }

                // RAM Breakdown Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianSurface)
                        .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RamMetricColumn("المستخدمة", "${telemetry.ramUsedMb} MB", CrimsonRed)
                    RamMetricColumn("المتاحة للألعاب", "${telemetry.ramAvailableMb} MB", MatrixGreen)
                    RamMetricColumn("الإجمالي", "${telemetry.ramTotalMb} MB", CyberCyan)
                    RamMetricColumn("كاش مؤقت", "${telemetry.cacheEstimatedMb.roundToInt()} MB", MoltenAmber)
                }

                Button(
                    onClick = onExecuteDeepBoost,
                    enabled = !isBoosting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("execute_deep_boost_btn"),
                    shape = CutCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonRed,
                        contentColor = Color.White
                    )
                ) {
                    if (isBoosting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "جاري تنظيف الرام والكاش وتحسين البنج...",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تنظيف عميق للرام والكاش الآن (1-Tap RAM Purge)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Last Boost Report Summary
                if (lastBoostReport != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("last_boost_report_banner"),
                        shape = RoundedCornerShape(10.dp),
                        color = MatrixGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, MatrixGreen.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MatrixGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "تم تحرير ${lastBoostReport.freedRamMb} MB رام + مسح ${lastBoostReport.cleanedCacheMb} MB كاش (${lastBoostReport.stoppedProcessesCount} عمليات)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TitaniumWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = lastBoostReport.summaryMessageAr.ifBlank {
                                        "الرام: ${lastBoostReport.ramBeforePercent}% ⬅ ${lastBoostReport.ramAfterPercent}% • البنج: ${lastBoostReport.pingAfterMs}ms"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MatrixGreen,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Live Network Ping & Jitter Analyzer Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("network_ping_card"),
            shape = RoundedCornerShape(14.dp),
            color = GunmetalCard,
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "فحص استجابة الشبكة للألعاب (Live Ping)",
                            style = MaterialTheme.typography.titleSmall,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${telemetry.networkStatus} • Ping: ${telemetry.pingMs}ms • Jitter: ${telemetry.jitterMs}ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MatrixGreen,
                            fontSize = 11.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = onTestNetworkPing,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CyberCyan),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "فحص الآن",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Background Processes & Whitelist Protection List
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("background_processes_card"),
            shape = RoundedCornerShape(14.dp),
            color = GunmetalCard,
            border = BorderStroke(1.dp, CarbonBorder)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MoltenAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "العمليات الخلفية وقائمة الحماية (Whitelist)",
                                style = MaterialTheme.typography.titleSmall,
                                color = TitaniumWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "اقفل التطبيقات المهمة لكي لا يتم إغلاقها أثناء تنظيف الرام قبل اللعب",
                                style = MaterialTheme.typography.bodySmall,
                                color = SilverMist,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                processes.forEach { proc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianSurface)
                            .border(
                                1.dp,
                                if (proc.isWhitelisted) CyberCyan.copy(alpha = 0.5f) else CarbonBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onToggleProcessWhitelist(proc.packageName) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = proc.appTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (proc.wasCleaned) SilverMist else TitaniumWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = proc.categoryAr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CyberCyan,
                                    fontSize = 10.sp
                                )
                            }
                            Text(
                                text = if (proc.wasCleaned) "✓ تم تفريغ ${proc.memoryMb} MB" else "استهلاك: ${proc.memoryMb} MB • ${proc.packageName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (proc.wasCleaned) MatrixGreen else SilverMist,
                                fontSize = 10.sp
                            )
                        }

                        IconButton(
                            onClick = { onToggleProcessWhitelist(proc.packageName) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = if (proc.isWhitelisted) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = if (proc.isWhitelisted) "محمي من الإغلاق" else "غير محمي",
                                tint = if (proc.isWhitelisted) CyberCyan else SilverMist,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RamMetricColumn(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = OrbitronFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            color = SilverMist
        )
    }
}
