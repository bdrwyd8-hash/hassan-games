package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.example.model.GameSpaceProfile
import com.example.model.NetworkCheckReport
import com.example.model.NetworkConnectionType
import com.example.model.RouteOptimizationAssessment
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NetworkBoosterHeroCard(
    latestReport: NetworkCheckReport?,
    isCheckingNetwork: Boolean,
    liveConnectionType: NetworkConnectionType,
    liveNetworkLabel: String,
    liveQuickPingMs: Int,
    onRunOneTapCheck: () -> Unit,
    onAskAiAboutNetwork: () -> Unit
) {
    val gradeColor = latestReport?.let { Color(it.stabilityGrade.colorHex) } ?: CyberCyan

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("network_booster_hero_card"),
        shape = CutCornerShape(topStart = 20.dp, bottomEnd = 20.dp, topEnd = 8.dp, bottomStart = 8.dp),
        color = GunmetalCard,
        border = BorderStroke(1.5.dp, gradeColor.copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
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
                            .size(46.dp)
                            .clip(CutCornerShape(12.dp))
                            .background(gradeColor.copy(alpha = 0.18f))
                            .border(1.dp, gradeColor, CutCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (liveConnectionType) {
                                NetworkConnectionType.OFFLINE -> Icons.Default.WifiOff
                                NetworkConnectionType.CELLULAR -> Icons.Default.SignalCellularAlt
                                else -> Icons.Default.Wifi
                            },
                            contentDescription = null,
                            tint = gradeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "NETWORK BOOSTER • فاحص ومحلل الشبكة",
                            style = MaterialTheme.typography.titleMedium,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = latestReport?.networkSubtypeLabel ?: liveNetworkLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (liveConnectionType == NetworkConnectionType.OFFLINE) CrimsonRed else CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Stability Score Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ObsidianSurface,
                    border = BorderStroke(1.dp, gradeColor)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (latestReport != null) "${latestReport.stabilityScore}/100" else "${liveQuickPingMs}ms",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = gradeColor
                        )
                        Text(
                            text = if (latestReport != null) "الاستقرار" else "بنق سريع",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = SilverMist
                        )
                    }
                }
            }

            // One-Tap Network Check Primary CTA Button
            Button(
                onClick = onRunOneTapCheck,
                enabled = !isCheckingNetwork,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("one_tap_network_check_btn"),
                shape = CutCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = ObsidianBlack
                )
            ) {
                if (isCheckingNetwork) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ObsidianBlack,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "جاري فحص Ping والتذبذب وفقدان الحزم والمسارات...",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "One-Tap Network Check • فحص شامل للشبكة الآن",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Detailed Results if a check has been executed
            if (latestReport != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianSurface)
                        .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .testTag("network_check_result_panel"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = latestReport.stabilityGrade.titleAr,
                            style = MaterialTheme.typography.bodyMedium,
                            color = gradeColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "الخادم: ${latestReport.testedServerName} (${latestReport.testedServerHost})",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist,
                            fontSize = 10.sp
                        )
                    }

                    // 4 Core Metrics Row: Ping, Jitter, Packet Loss, Disconnects
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        NetworkMetricBox(
                            label = "متوسط Ping",
                            value = latestReport.avgPingMs?.let { "${it}ms" } ?: "غير متاح",
                            sub = if (latestReport.minPingMs != null && latestReport.maxPingMs != null) {
                                "${latestReport.minPingMs}–${latestReport.maxPingMs}ms"
                            } else {
                                "—"
                            },
                            color = if ((latestReport.avgPingMs ?: 999) <= 60) MatrixGreen else MoltenAmber
                        )
                        NetworkMetricBox(
                            label = "التذبذب Jitter",
                            value = latestReport.jitterMs?.let { "${it}ms" } ?: "غير متاح",
                            sub = if (latestReport.spikeDetected) "تقلب مفاجئ!" else "ثابت",
                            color = if (latestReport.spikeDetected) CrimsonRed else CyberCyan
                        )
                        NetworkMetricBox(
                            label = "فقدان الحزم",
                            value = if (latestReport.packetLossSupported && latestReport.packetLossPercent != null) {
                                "${latestReport.packetLossPercent}%"
                            } else {
                                "غير متاح"
                            },
                            sub = if ((latestReport.packetLossPercent ?: 0) == 0 && latestReport.isConnected) "سليم 100%" else "تحقق من الشبكة",
                            color = if ((latestReport.packetLossPercent ?: 0) == 0 && latestReport.isConnected) MatrixGreen else CrimsonRed
                        )
                        NetworkMetricBox(
                            label = "الانقطاعات",
                            value = "${latestReport.disconnectsRecorded}",
                            sub = "مرصودة بالجلسة",
                            color = if (latestReport.disconnectsRecorded == 0) MatrixGreen else CrimsonRed
                        )
                    }

                    // Honest explanation of Test Ping vs Game Server Ping
                    Text(
                        text = latestReport.pingDifferenceExplanationAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist,
                        fontSize = 11.sp
                    )
                }

                // AI Network Advisor Summary Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_network_advisor_card"),
                    shape = RoundedCornerShape(10.dp),
                    color = ElectricPurple.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.65f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تحليل AI Network Advisor",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TitaniumWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "اسأل Hassan AI ⬅",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onAskAiAboutNetwork() }
                                    .testTag("ask_ai_from_network_btn")
                            )
                        }
                        Text(
                            text = latestReport.aiNetworkSummaryAr,
                            style = MaterialTheme.typography.bodySmall,
                            color = TitaniumWhite,
                            fontSize = 11.sp
                        )
                    }
                }

                // Detected Issues & Smart Recommendations
                if (latestReport.detectedIssuesAr.isNotEmpty() || latestReport.smartRecommendationsAr.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianSurface)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                            .testTag("smart_network_recommendations_card"),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "المشكلات المكتشفة والتوصيات الذكية (Smart Recommendations)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MoltenAmber,
                            fontWeight = FontWeight.Bold
                        )
                        latestReport.detectedIssuesAr.forEach { issue ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = if (latestReport.isConnected && (latestReport.packetLossPercent ?: 0) == 0 && !latestReport.spikeDetected) {
                                        Icons.Default.CheckCircle
                                    } else {
                                        Icons.Default.ErrorOutline
                                    },
                                    contentDescription = null,
                                    tint = gradeColor,
                                    modifier = Modifier
                                        .size(15.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = issue,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TitaniumWhite,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        latestReport.smartRecommendationsAr.forEach { rec ->
                            Text(
                                text = "💡 $rec",
                                style = MaterialTheme.typography.bodySmall,
                                color = MatrixGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
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
fun RouteOptimizationDiagnosticCard(
    games: List<GameSpaceProfile>,
    selectedGameId: String,
    routeAssessment: RouteOptimizationAssessment,
    onSelectGame: (String) -> Unit,
    onRunRouteProbe: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("route_optimization_card"),
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
                    imageVector = Icons.Default.Hub,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "تحليل المسارات ونقاط الاتصال (Route & Gateway Analyzer)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "مقارنة حقيقية لزمن الاستجابة عبر البوابات العالمية للعبة المحددة",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist,
                        fontSize = 11.sp
                    )
                }
            }

            // Game Selector Chips
            if (games.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    games.take(6).forEach { game ->
                        val selected = game.id == selectedGameId
                        Surface(
                            modifier = Modifier
                                .clickable { onSelectGame(game.id) }
                                .testTag("route_game_chip_${game.id}"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) CyberCyan.copy(alpha = 0.2f) else ObsidianSurface,
                            border = BorderStroke(1.dp, if (selected) CyberCyan else CarbonBorder)
                        ) {
                            Text(
                                text = game.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) CyberCyan else SilverMist,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = routeAssessment.statusHeadlineAr,
                style = MaterialTheme.typography.bodySmall,
                color = MatrixGreen,
                fontWeight = FontWeight.Bold
            )

            // Endpoint Probe Comparison Rows
            if (routeAssessment.testedEndpoints.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    routeAssessment.testedEndpoints.forEach { ep ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ObsidianSurface)
                                .border(
                                    1.dp,
                                    if (ep.isOptimalCandidate) MatrixGreen else CarbonBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Router,
                                    contentDescription = null,
                                    tint = if (ep.isOptimalCandidate) MatrixGreen else CyberCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${ep.endpointName} (${ep.hostIp})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TitaniumWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = ep.regionLabelAr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SilverMist,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (ep.reachable && ep.avgRttMs != null) "${ep.avgRttMs} ms" else "غير متاح",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        !ep.reachable -> CrimsonRed
                                        ep.isOptimalCandidate -> MatrixGreen
                                        else -> CyberCyan
                                    }
                                )
                                Text(
                                    text = if (ep.isOptimalCandidate) "الأفضل استجابة ✓" else "فقدان ${ep.packetLossPercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = if (ep.isOptimalCandidate) MatrixGreen else SilverMist
                                )
                            }
                        }
                    }
                }
            }

            // Honest Architecture Disclosure
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ObsidianSurface,
                border = BorderStroke(1.dp, MoltenAmber.copy(alpha = 0.45f))
            ) {
                Text(
                    text = routeAssessment.architecturalNoteAr,
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            OutlinedButton(
                onClick = onRunRouteProbe,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("run_route_probe_btn"),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, CyberCyan)
            ) {
                Text(
                    text = "إعادة فحص ومقارنة البوابات للعبة (${routeAssessment.selectedGameTitle})",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun NetworkTestHistoryCard(
    history: List<NetworkCheckReport>,
    onClearHistory: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("network_test_history_card"),
        shape = RoundedCornerShape(14.dp),
        color = GunmetalCard,
        border = BorderStroke(1.dp, CarbonBorder)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MoltenAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "سجل اختبارات الشبكة والمقارنة (${history.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "محفوظ محلياً على جهازك للمقارنة بين الجلسات",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist,
                            fontSize = 10.sp
                        )
                    }
                }

                if (history.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onClearHistory,
                        modifier = Modifier.testTag("clear_network_history_btn"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CrimsonRed),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = CrimsonRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "مسح السجل",
                            style = MaterialTheme.typography.labelSmall,
                            color = CrimsonRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (history.isEmpty()) {
                Text(
                    text = "لا توجد فحوصات محفوظة بعد. اضغط على (One-Tap Network Check) بالأعلى لحفظ أول نتيجة ومقارنتها لاحقاً.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist,
                    fontSize = 11.sp
                )
            } else {
                val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                history.take(5).forEachIndexed { idx, item ->
                    val timeLabel = try {
                        timeFmt.format(Date(item.timestampMs))
                    } catch (_: Exception) {
                        "#${idx + 1}"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianSurface)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${item.connectionType.labelAr} • $timeLabel",
                                style = MaterialTheme.typography.bodySmall,
                                color = TitaniumWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${item.testedServerName} • ${item.stabilityGrade.titleAr}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(item.stabilityGrade.colorHex),
                                fontSize = 10.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = item.avgPingMs?.let { "${it}ms" } ?: "Offline",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isConnected) MatrixGreen else CrimsonRed
                            )
                            Text(
                                text = "تذبذب: ${item.jitterMs ?: 0}ms | فقدان: ${item.packetLossPercent ?: 0}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = SilverMist
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkMetricBox(
    label: String,
    value: String,
    sub: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = OrbitronFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = TitaniumWhite,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = sub,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = SilverMist
        )
    }
}
