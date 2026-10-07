package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClonedButtonMode
import com.example.model.ClonedButtonsConfig
import com.example.model.ClonedTouchButton
import com.example.model.LowEndDeviceConfig
import com.example.service.TriggerEventBus
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

/**
 * Studio card for Custom Cloned Touch Buttons (ميزة الأزرار المنسوخة القابلة للتحريك C1–C4).
 * Lets the user place a movable button anywhere on screen that remotely taps an unmovable game button.
 */
@Composable
fun ClonedButtonsStudioCard(
    config: ClonedButtonsConfig,
    isAccessibilityRunning: Boolean,
    onUpdateConfig: ((ClonedButtonsConfig) -> ClonedButtonsConfig) -> Unit,
    onToggleSystemOverlay: () -> Unit,
    onSimulateClonedTap: (ClonedTouchButton, Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit
) {
    val totalClonedTaps by TriggerEventBus.totalClonedTaps.collectAsState()
    val lastActiveId by TriggerEventBus.lastClonedActiveId.collectAsState()
    var selectedButtonId by remember { mutableIntStateOf(1) }
    var lastPulseTimeMs by remember { mutableLongStateOf(0L) }

    val selectedButton = config.buttons.find { it.id == selectedButtonId } ?: config.buttons.first()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cloned_buttons_studio_card"),
        shape = CutCornerShape(topStart = 18.dp, bottomEnd = 18.dp, topEnd = 8.dp, bottomStart = 8.dp),
        color = GunmetalCard,
        border = BorderStroke(1.5.dp, CyberCyan.copy(alpha = 0.75f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .size(42.dp)
                            .clip(CutCornerShape(10.dp))
                            .background(CyberCyan.copy(alpha = 0.16f))
                            .border(1.dp, CyberCyan, CutCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ControlCamera,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "الأزرار المنسوخة القابلة للتحريك",
                                style = MaterialTheme.typography.titleMedium,
                                color = TitaniumWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CrimsonRed)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PRO TOUCH",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            text = "انسخ أي زر ثابت باللعبة وحرّكه لأي مكان مريح لإصبعك على الشاشة",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist
                        )
                    }
                }

                Switch(
                    checked = config.enabled,
                    onCheckedChange = { enabled ->
                        onUpdateConfig { it.copy(enabled = enabled) }
                    },
                    modifier = Modifier.testTag("cloned_buttons_master_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = CyberCyan
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Step-by-step explanation box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = ObsidianSurface,
                border = BorderStroke(1.dp, CarbonBorder)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "💡 كيف تعمل الأزرار المنسوخة على الشاشة؟",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1️⃣ ضع دائرة الهدف الصغير (🎯 C1) فوق الزر الأصلي الثابت باللعبة.\n" +
                            "2️⃣ اسحب الزر المنسوخ الكبير (🔘 C1) وضعه في المكان المريح لإصبعك.\n" +
                            "3️⃣ اضغط (🔒 قفل للعب): كلما لمست (C1) سيضغط البرنامج تلقائياً على موقع (🎯 C1).",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mode Toggle: Edit / Move Mode vs Lock & Play Test Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onUpdateConfig { it.copy(editPositionsLocked = false) } },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("cloned_mode_edit_btn"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!config.editPositionsLocked) MoltenAmber else ObsidianSurface,
                        contentColor = if (!config.editPositionsLocked) ObsidianBlack else SilverMist
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🔓 تحريك الأزرار والأهداف",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { onUpdateConfig { it.copy(editPositionsLocked = true) } },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("cloned_mode_lock_play_btn"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (config.editPositionsLocked) MatrixGreen else ObsidianSurface,
                        contentColor = if (config.editPositionsLocked) ObsidianBlack else SilverMist
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🔒 قفل وتجربة الضغط",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Simulator Arena for Cloned Buttons
            val density = LocalDensity.current
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(255.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianBlack)
                    .border(
                        width = 1.5.dp,
                        color = if (config.editPositionsLocked) MatrixGreen.copy(alpha = 0.7f) else MoltenAmber.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .testTag("cloned_buttons_arena")
            ) {
                val arenaW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
                val arenaH = constraints.maxHeight.toFloat().coerceAtLeast(1f)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val step = w / 8f
                    for (i in 1..7) {
                        drawLine(
                            color = CarbonBorder.copy(alpha = 0.35f),
                            start = Offset(i * step, 0f),
                            end = Offset(i * step, h),
                            strokeWidth = 1f
                        )
                    }
                    val vStep = h / 5f
                    for (j in 1..4) {
                        drawLine(
                            color = CarbonBorder.copy(alpha = 0.35f),
                            start = Offset(0f, j * vStep),
                            end = Offset(w, j * vStep),
                            strokeWidth = 1f
                        )
                    }

                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    for (btn in config.buttons.filter { it.enabled }) {
                        val btnColor = Color(btn.colorHex)
                        val startPt = Offset(btn.sourceX * w, btn.sourceY * h)
                        val targetPt = Offset(btn.targetX * w, btn.targetY * h)
                        val isFiringNow = (lastActiveId == btn.id) ||
                            (selectedButtonId == btn.id && System.currentTimeMillis() - lastPulseTimeMs < 500L)

                        if (!config.editPositionsLocked || isFiringNow) {
                            drawLine(
                                color = if (isFiringNow) MatrixGreen else btnColor.copy(alpha = 0.75f),
                                start = startPt,
                                end = targetPt,
                                strokeWidth = if (isFiringNow) 3.5.dp.toPx() else 2.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = if (isFiringNow) null else dashEffect
                            )
                        }

                        if (isFiringNow) {
                            drawCircle(
                                color = btnColor.copy(alpha = 0.35f),
                                radius = 26.dp.toPx(),
                                center = targetPt
                            )
                            drawCircle(
                                color = TitaniumWhite,
                                radius = 14.dp.toPx(),
                                center = targetPt,
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }
                    }
                }

                FixedGameButtonHint(
                    label = "⚙️ زر إعدادات ثابت",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 34.dp, end = 14.dp)
                )
                FixedGameButtonHint(
                    label = "🎒 زر حقيبة ثابت",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 34.dp, start = 14.dp)
                )
                FixedGameButtonHint(
                    label = "⚡ زر مهارة ثابت",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 32.dp, end = 14.dp)
                )

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
                            .background(ObsidianSurface.copy(alpha = 0.88f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (config.editPositionsLocked)
                                "🔒 وضع اللعب: اضغط على (C1/C2) ليضغط تلقائياً على الهدف!"
                            else
                                "🔓 وضع التعديل: اسحب (C1) للمكان المريح واسحب (🎯C1) للزر الثابت",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (config.editPositionsLocked) MatrixGreen else MoltenAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObsidianSurface.copy(alpha = 0.88f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "ضغطات منسوخة: $totalClonedTaps",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 10.sp,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val targetSizeDp = 36.dp
                val targetRadiusPx = with(density) { (targetSizeDp / 2).toPx() }

                config.buttons.filter { it.enabled }.forEach { btn ->
                    val btnColor = Color(btn.colorHex)
                    val btnSizeDp = btn.buttonSizeDp.coerceIn(38, 78).dp
                    val btnRadiusPx = with(density) { (btnSizeDp / 2).toPx() }
                    val isFiring = (lastActiveId == btn.id) ||
                        (selectedButtonId == btn.id && System.currentTimeMillis() - lastPulseTimeMs < 450L)

                    val targetOffsetX = ((btn.targetX * arenaW) - targetRadiusPx).roundToInt()
                    val targetOffsetY = ((btn.targetY * arenaH) - targetRadiusPx).roundToInt()

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(targetOffsetX, targetOffsetY) }
                            .size(targetSizeDp)
                            .clip(CircleShape)
                            .background(
                                if (isFiring) MatrixGreen.copy(alpha = 0.85f)
                                else ObsidianSurface.copy(alpha = 0.85f)
                            )
                            .border(
                                width = if (isFiring) 2.5.dp else 1.5.dp,
                                color = if (isFiring) TitaniumWhite else btnColor,
                                shape = CircleShape
                            )
                            .pointerInput(btn.id, config.editPositionsLocked) {
                                if (!config.editPositionsLocked) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        selectedButtonId = btn.id
                                        val nx = (((btn.targetX * arenaW) + dragAmount.x) / arenaW)
                                            .coerceIn(0.06f, 0.94f)
                                        val ny = (((btn.targetY * arenaH) + dragAmount.y) / arenaH)
                                            .coerceIn(0.12f, 0.88f)
                                        onUpdateConfig { cur ->
                                            cur.copy(
                                                buttons = cur.buttons.map {
                                                    if (it.id == btn.id) it.copy(targetX = nx, targetY = ny) else it
                                                }
                                            )
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isFiring) "💥" else "🎯${btn.label}",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFiring) ObsidianBlack else btnColor
                        )
                    }

                    val btnOffsetX = ((btn.sourceX * arenaW) - btnRadiusPx).roundToInt()
                    val btnOffsetY = ((btn.sourceY * arenaH) - btnRadiusPx).roundToInt()

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(btnOffsetX, btnOffsetY) }
                            .size(btnSizeDp)
                            .clip(CircleShape)
                            .background(
                                if (isFiring) btnColor.copy(alpha = 0.9f)
                                else btnColor.copy(alpha = 0.25f)
                            )
                            .border(
                                width = if (selectedButtonId == btn.id) 2.5.dp else 1.8.dp,
                                color = if (isFiring) TitaniumWhite else btnColor,
                                shape = CircleShape
                            )
                            .pointerInput(btn.id, config.editPositionsLocked) {
                                if (!config.editPositionsLocked) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        selectedButtonId = btn.id
                                        val nx = (((btn.sourceX * arenaW) + dragAmount.x) / arenaW)
                                            .coerceIn(0.06f, 0.94f)
                                        val ny = (((btn.sourceY * arenaH) + dragAmount.y) / arenaH)
                                            .coerceIn(0.12f, 0.88f)
                                        onUpdateConfig { cur ->
                                            cur.copy(
                                                buttons = cur.buttons.map {
                                                    if (it.id == btn.id) it.copy(sourceX = nx, sourceY = ny) else it
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    detectTapGestures(
                                        onPress = {
                                            selectedButtonId = btn.id
                                            lastPulseTimeMs = System.currentTimeMillis()
                                            onSimulateClonedTap(btn, true)
                                            tryAwaitRelease()
                                            onSimulateClonedTap(btn, false)
                                        }
                                    )
                                }
                            }
                            .testTag("cloned_btn_node_${btn.label}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = btn.label,
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isFiring) ObsidianBlack else TitaniumWhite
                            )
                            Text(
                                text = if (config.editPositionsLocked) "اضغطني" else "حركني",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 8.sp,
                                color = if (isFiring) ObsidianBlack else btnColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selector Row for C1, C2, C3, C4 + Enable/Disable each cloned button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الأزرار المنسوخة المتاحة (اضغط لتفعيل أو تخصيص الزر):",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            onUpdateConfig { it.copy(buttons = ClonedButtonsConfig.defaultClonedButtons()) }
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "إعادة ضبط المواقع",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberCyan,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                config.buttons.forEach { btn ->
                    val btnColor = Color(btn.colorHex)
                    val isSelected = btn.id == selectedButtonId
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedButtonId = btn.id
                                if (!btn.enabled) {
                                    onUpdateConfig { cur ->
                                        cur.copy(
                                            buttons = cur.buttons.map {
                                                if (it.id == btn.id) it.copy(enabled = true) else it
                                            }
                                        )
                                    }
                                }
                            }
                            .testTag("select_cloned_slot_${btn.label}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) btnColor.copy(alpha = 0.22f) else ObsidianSurface,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (btn.enabled) btnColor else CarbonBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = btn.label,
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (btn.enabled) btnColor else SilverMist
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (btn.enabled) "مفعّل ON" else "+ إضافة",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = if (btn.enabled) MatrixGreen else SilverMist,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Cloned Button Detail Settings
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = ObsidianSurface,
                border = BorderStroke(1.dp, Color(selectedButton.colorHex).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${selectedButton.actionTitleAr} (${selectedButton.label})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TitaniumWhite,
                                fontWeight = FontWeight.Bold
                            )
                            val bx = (selectedButton.sourceX * 100).roundToInt()
                            val by = (selectedButton.sourceY * 100).roundToInt()
                            val tx = (selectedButton.targetX * 100).roundToInt()
                            val ty = (selectedButton.targetY * 100).roundToInt()
                            Text(
                                text = "موقع الزر: ($bx%, $by%)  ⬅️  يضغط الهدف: ($tx%, $ty%)",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                color = Color(selectedButton.colorHex)
                            )
                        }

                        Switch(
                            checked = selectedButton.enabled,
                            onCheckedChange = { en ->
                                onUpdateConfig { cur ->
                                    cur.copy(
                                        buttons = cur.buttons.map {
                                            if (it.id == selectedButton.id) it.copy(enabled = en) else it
                                        }
                                    )
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBlack,
                                checkedTrackColor = Color(selectedButton.colorHex)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mode Selector for this Cloned Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ClonedButtonMode.entries.forEach { mode ->
                            val active = selectedButton.mode == mode
                            val btnAccent = Color(selectedButton.colorHex)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) btnAccent.copy(alpha = 0.22f) else GunmetalCard)
                                    .border(1.dp, if (active) btnAccent else CarbonBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        onUpdateConfig { cur ->
                                            cur.copy(
                                                buttons = cur.buttons.map {
                                                    if (it.id == selectedButton.id) it.copy(mode = mode) else it
                                                }
                                            )
                                        }
                                    }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.titleAr.substringBefore(" ("),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) btnAccent else SilverMist
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Button Size Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "حجم الزر المنسوخ وشفافيته:",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist
                        )
                        Text(
                            text = "${selectedButton.buttonSizeDp} DP  •  ${(selectedButton.opacity * 100).roundToInt()}%",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = selectedButton.buttonSizeDp.toFloat(),
                        onValueChange = { sz ->
                            onUpdateConfig { cur ->
                                cur.copy(
                                    buttons = cur.buttons.map {
                                        if (it.id == selectedButton.id) it.copy(buttonSizeDp = sz.roundToInt()) else it
                                    }
                                )
                            }
                        },
                        valueRange = 38f..78f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Launch Cloned Buttons System Overlay Over Games Button
            Button(
                onClick = onToggleSystemOverlay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("launch_cloned_buttons_overlay_btn"),
                shape = CutCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (config.enabled) MatrixGreen else CrimsonRed,
                    contentColor = if (config.enabled) ObsidianBlack else Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (config.enabled)
                        "✓ الأزرار المنسوخة عائمة الآن فوق الألعاب (اضغط لإخفائها)"
                    else
                        "🔘 إظهار الأزرار المنسوخة (C1-C4) عائمـة فوق الألعاب الآن",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!isAccessibilityRunning) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onOpenAccessibilitySettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MoltenAmber)
                ) {
                    Icon(
                        imageVector = Icons.Default.AdsClick,
                        contentDescription = null,
                        tint = MoltenAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تنبيه: فعّل خدمة إمكانية الوصول لكي تضغط الأزرار المنسوخة داخل الألعاب",
                        style = MaterialTheme.typography.bodySmall,
                        color = MoltenAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun FixedGameButtonHint(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(GunmetalElevated.copy(alpha = 0.72f))
            .border(1.dp, CarbonBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SilverMist.copy(alpha = 0.85f),
            fontSize = 10.sp
        )
    }
}

/**
 * GG GameBox Pro Environment & Network Optimizer Card.
 */
@Composable
fun GameBoxEnvironmentCard(
    lowEndConfig: LowEndDeviceConfig,
    onUpdateLowEnd: ((LowEndDeviceConfig) -> LowEndDeviceConfig) -> Unit,
    onTestNetworkPing: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gamebox_environment_card"),
        shape = RoundedCornerShape(14.dp),
        color = GunmetalCard,
        border = BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    tint = ElectricPurple,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "مركز بيئة اللعب والشبكة الذكي (Gaming Environment)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "أولوية حزم الشبكة للألعاب + قفل السطوع المستقل + حماية اللمس",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Wi-Fi Low Latency Priority Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObsidianSurface)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "أولوية الشبكة وتقليل البنج (Wi-Fi Low-Latency)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "يمنع التحديثات الخلفية من سحب سرعة الاتصال أثناء اللعب",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist,
                            fontSize = 11.sp
                        )
                    }
                }
                Switch(
                    checked = lowEndConfig.wiFiPriorityEnabled,
                    onCheckedChange = { v ->
                        onUpdateLowEnd { it.copy(wiFiPriorityEnabled = v) }
                        onTestNetworkPing()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = CyberCyan
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Independent Game Brightness Lock Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BrightnessHigh,
                        contentDescription = null,
                        tint = MoltenAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "قفل سطوع الشاشة الثابت أثناء اللعب:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TitaniumWhite
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${lowEndConfig.lockedBrightnessPercent}%",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 12.sp,
                        color = MoltenAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = lowEndConfig.brightnessLockEnabled,
                        onCheckedChange = { v -> onUpdateLowEnd { it.copy(brightnessLockEnabled = v) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBlack,
                            checkedTrackColor = MoltenAmber
                        )
                    )
                }
            }
            Slider(
                value = lowEndConfig.lockedBrightnessPercent.toFloat(),
                onValueChange = { v -> onUpdateLowEnd { it.copy(lockedBrightnessPercent = v.roundToInt()) } },
                valueRange = 25f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = MoltenAmber,
                    activeTrackColor = MoltenAmber
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Edge Touch Reject Protection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObsidianSurface)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MatrixGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "حماية حواف الشاشة من اللمس الخاطئ",
                            style = MaterialTheme.typography.bodySmall,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "يمنع الخروج غير المقصود من اللعبة عند مسك الهاتف بالعرض",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist,
                            fontSize = 11.sp
                        )
                    }
                }
                Switch(
                    checked = lowEndConfig.touchEdgeRejectEnabled,
                    onCheckedChange = { v -> onUpdateLowEnd { it.copy(touchEdgeRejectEnabled = v) } },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = MatrixGreen
                    )
                )
            }
        }
    }
}

/**
 * Custom App Icon & Logo Studio Card.
 */
@Composable
fun CustomAppIconStudioCard(
    defaultDrawableRes: Int
) {
    val context = LocalContext.current
    var customBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) {
                        customBitmap = bmp.asImageBitmap()
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_app_icon_studio_card"),
        shape = RoundedCornerShape(14.dp),
        color = GunmetalCard,
        border = BorderStroke(1.dp, MoltenAmber.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianSurface)
                    .border(1.5.dp, MoltenAmber, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                val bmp = customBitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp,
                        contentDescription = "أيقونة البرنامج المخصصة",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = defaultDrawableRes),
                        contentDescription = "أيقونة البرنامج الحالية",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "تخصيص شعار وأيقونة البرنامج (Hassan Icon Studio)",
                    style = MaterialTheme.typography.titleSmall,
                    color = TitaniumWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "اختر صورة من استوديو جوالك لمعاينتها كشعار داخل مركز الألعاب",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.testTag("pick_custom_logo_btn"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MoltenAmber),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = null,
                        tint = MoltenAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (customBitmap != null) "تغيير صورة الشعار المختارة" else "اختيار صورة شعار من جوالك",
                        style = MaterialTheme.typography.labelMedium,
                        color = MoltenAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
