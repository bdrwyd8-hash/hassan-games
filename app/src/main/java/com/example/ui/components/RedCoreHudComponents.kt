package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CrosshairConfig
import com.example.model.CrosshairStyle
import com.example.model.HardwareTelemetry
import com.example.model.PerformanceMode
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

@Composable
fun RedCoreTopStatusBar(
    telemetry: HardwareTelemetry,
    performanceMode: PerformanceMode,
    crosshairEnabled: Boolean,
    isMasterRunning: Boolean,
    onQuickAddGameClick: () -> Unit,
    onQuickCrosshairClick: () -> Unit,
    onQuickStartOrStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        color = ObsidianSurface.copy(alpha = 0.95f),
        shape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp, topEnd = 4.dp, bottomStart = 4.dp),
        border = BorderStroke(1.dp, performanceMode.primaryColor.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand & Active Mode Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(performanceMode.primaryColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HASSAN GAMES",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TitaniumWhite
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(performanceMode.primaryColor.copy(alpha = 0.2f))
                            .border(1.dp, performanceMode.primaryColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = performanceMode.badgeText,
                            fontFamily = OrbitronFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = performanceMode.primaryColor
                        )
                    }
                }

                // Quick Gaming Center Actions: + ADD GAME, Crosshair, Start/Stop
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberCyan.copy(alpha = 0.2f))
                            .border(1.dp, CyberCyan, RoundedCornerShape(6.dp))
                            .clickable { onQuickAddGameClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("top_bar_add_game_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "إضافة لعبة",
                                tint = CyberCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "ADD GAME",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (crosshairEnabled) CrimsonRed.copy(alpha = 0.25f)
                                else GunmetalElevated
                            )
                            .border(
                                1.dp,
                                if (crosshairEnabled) CrimsonRed else CarbonBorder,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onQuickCrosshairClick() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("top_crosshair_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "تبديل مؤشر التصويب",
                            tint = if (crosshairEnabled) CrimsonRed else SilverMist,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isMasterRunning) CrimsonRed.copy(alpha = 0.22f)
                                else MatrixGreen.copy(alpha = 0.22f)
                            )
                            .border(
                                1.dp,
                                if (isMasterRunning) CrimsonRed else MatrixGreen,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onQuickStartOrStopClick() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("top_start_stop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "تشغيل أو إنهاء التطبيق",
                            tint = if (isMasterRunning) CrimsonRed else MatrixGreen,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Live Hardware Telemetry Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TelemetryMiniChip(
                    icon = Icons.Default.Speed,
                    label = "FPS",
                    value = "${telemetry.liveFps}",
                    tint = MatrixGreen
                )
                TelemetryMiniChip(
                    icon = Icons.Default.Memory,
                    label = "RAM",
                    value = "${telemetry.ramUsagePercent}%",
                    tint = if (telemetry.ramUsagePercent > 80) CrimsonRed else CyberCyan
                )
                TelemetryMiniChip(
                    icon = Icons.Default.Thermostat,
                    label = "الحرارة",
                    value = "${telemetry.batteryTempCelsius}°C",
                    tint = if (telemetry.batteryTempCelsius > 41f) CrimsonRed else MoltenAmber
                )
                TelemetryMiniChip(
                    icon = Icons.Default.NetworkCheck,
                    label = "PING",
                    value = "${telemetry.pingMs}ms",
                    tint = if (telemetry.pingMs <= 45) MatrixGreen else MoltenAmber
                )
            }
        }
    }
}

@Composable
private fun TelemetryMiniChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = SilverMist,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = value,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            color = TitaniumWhite,
            fontSize = 11.sp
        )
    }
}

@Composable
fun StatusToastBanner(
    message: String?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            color = GunmetalElevated,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CyberCyan)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = SilverMist,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RotatingCoolingTurbine(
    rpm: Int,
    accentColor: Color,
    isTurboBoosting: Boolean,
    size: Dp = 76.dp
) {
    val angle = if (isTurboBoosting) {
        val infiniteTransition = rememberInfiniteTransition(label = "turbine_rotation")
        val animated by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 480, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "blade_angle"
        )
        animated
    } else {
        (rpm % 360).toFloat()
    }

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val radius = this.size.minDimension / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            drawCircle(
                color = accentColor.copy(alpha = 0.22f),
                radius = radius,
                center = center,
                style = Stroke(width = 6f)
            )
            drawCircle(
                color = accentColor,
                radius = radius * 0.9f,
                center = center,
                style = Stroke(width = 2.5f)
            )

            rotate(degrees = angle, pivot = center) {
                for (i in 0 until 8) {
                    rotate(degrees = i * 45f, pivot = center) {
                        val path = Path().apply {
                            moveTo(center.x, center.y - radius * 0.18f)
                            lineTo(center.x + radius * 0.22f, center.y - radius * 0.78f)
                            lineTo(center.x - radius * 0.08f, center.y - radius * 0.82f)
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(
                                colors = listOf(accentColor, accentColor.copy(alpha = 0.3f))
                            )
                        )
                    }
                }
            }

            drawCircle(
                color = GunmetalCard,
                radius = radius * 0.28f,
                center = center
            )
            drawCircle(
                color = accentColor,
                radius = radius * 0.14f,
                center = center
            )
        }
    }
}

@Composable
fun CrosshairCanvasView(
    config: CrosshairConfig,
    modifier: Modifier = Modifier
) {
    val drawSize = (config.sizeDp.coerceIn(16f, 80f)).dp
    Canvas(modifier = modifier.size(drawSize)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.minDimension / 2f
        val baseColor = Color(config.colorHex).copy(alpha = config.opacity.coerceIn(0.2f, 1f))
        val strokePx = config.strokeWidthDp.dp.toPx()

        when (config.style) {
            CrosshairStyle.RED_DOT -> {
                drawCircle(
                    color = ObsidianBlack.copy(alpha = 0.5f),
                    radius = (radius * 0.36f).coerceAtLeast(6f),
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = baseColor,
                    radius = (radius * 0.28f).coerceAtLeast(4f),
                    center = Offset(cx, cy)
                )
            }
            CrosshairStyle.TACTICAL_CROSS -> {
                val gap = radius * 0.25f
                drawLine(baseColor, Offset(cx - radius, cy), Offset(cx - gap, cy), strokePx, StrokeCap.Round)
                drawLine(baseColor, Offset(cx + gap, cy), Offset(cx + radius, cy), strokePx, StrokeCap.Round)
                drawLine(baseColor, Offset(cx, cy - radius), Offset(cx, cy - gap), strokePx, StrokeCap.Round)
                drawLine(baseColor, Offset(cx, cy + gap), Offset(cx, cy + radius), strokePx, StrokeCap.Round)
                drawCircle(baseColor, radius = strokePx * 0.9f, center = Offset(cx, cy))
            }
            CrosshairStyle.SNIPER_CIRCLE -> {
                drawCircle(
                    color = baseColor,
                    radius = radius * 0.7f,
                    center = Offset(cx, cy),
                    style = Stroke(width = strokePx)
                )
                drawCircle(baseColor, radius = strokePx * 1.1f, center = Offset(cx, cy))
                drawLine(baseColor, Offset(cx - radius, cy), Offset(cx - radius * 0.45f, cy), strokePx)
                drawLine(baseColor, Offset(cx + radius * 0.45f, cy), Offset(cx + radius, cy), strokePx)
                drawLine(baseColor, Offset(cx, cy - radius), Offset(cx, cy - radius * 0.45f), strokePx)
                drawLine(baseColor, Offset(cx, cy + radius * 0.45f), Offset(cx, cy + radius), strokePx)
            }
            CrosshairStyle.CHEVRON_PRO -> {
                val path = Path().apply {
                    moveTo(cx - radius * 0.65f, cy + radius * 0.45f)
                    lineTo(cx, cy)
                    lineTo(cx + radius * 0.65f, cy + radius * 0.45f)
                }
                drawPath(path, baseColor, style = Stroke(width = strokePx, cap = StrokeCap.Round))
                drawCircle(baseColor, radius = strokePx, center = Offset(cx, cy - radius * 0.22f))
            }
        }
    }
}
