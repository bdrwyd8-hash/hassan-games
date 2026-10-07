package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.READY_VOICE_DEMO_PHRASES
import com.example.model.VoiceModPreset
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

/**
 * Unified Start / Stop & Exit Button Banner (زر البدء والإنهاء بنفس الزر).
 * - When isMasterRunning == false: Shows "▶️ بدء تشغيل التطبيق والأدوات" (Start).
 * - When isMasterRunning == true: The exact same button transforms into "⏹️ إنهاء وإغلاق التطبيق بالكامل"
 *   which stops all background services, overlays, mic, and closes the app so nothing stays running in background.
 */
@Composable
fun MasterStartStopAppBanner(
    isMasterRunning: Boolean,
    onToggleStartOrExit: () -> Unit
) {
    val activeAccent = if (isMasterRunning) CrimsonRed else MatrixGreen

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("master_start_stop_banner"),
        shape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp, topEnd = 6.dp, bottomStart = 6.dp),
        color = GunmetalCard,
        border = BorderStroke(1.8.dp, activeAccent)
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
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isMasterRunning) MatrixGreen else SilverMist)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isMasterRunning)
                                "حالة النظام: شغّال ونشط الآن (ACTIVE)"
                            else
                                "حالة النظام: متوقف بالخلفية (READY TO START)",
                            style = MaterialTheme.typography.titleSmall,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isMasterRunning)
                                "عند الانتهاء من اللعب اضغط على الزر بالأسفل لإيقاف الخلفية وإغلاق التطبيق نهائياً"
                            else
                                "لا يستهلك أي بطارية بالخلفية حالياً — اضغط (بدء التشغيل) لتفعيل جميع المميزات",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Single Unified Start / Stop & Exit Button
            Button(
                onClick = onToggleStartOrExit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("unified_start_stop_exit_button"),
                shape = CutCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMasterRunning) CrimsonRed else MatrixGreen,
                    contentColor = if (isMasterRunning) Color.White else ObsidianBlack
                )
            ) {
                Icon(
                    imageVector = if (isMasterRunning) Icons.Default.PowerSettingsNew else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isMasterRunning)
                        "⏹️ إنهاء وإغلاق التطبيق بالكامل (إيقاف الخلفية)"
                    else
                        "▶️ بدء تشغيل التطبيق وتفعيل الأدوات الآن",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * Studio HD Microphone Voice Changer Card (مغير الصوت الحقيقي بالمايك بدون أخطاء وصافي 100%).
 * Supports Female, Male, Squirrel, Robot, and Crystal-Clear HD Original voice presets.
 */
@Composable
fun StudioVoiceChangerCard(
    activeVoicePreset: VoiceModPreset,
    isLiveMicActive: Boolean,
    isRecordingClip: Boolean,
    isPlayingClip: Boolean,
    micInputLevel: Float,
    voiceStatusText: String,
    noiseGateEnabled: Boolean,
    hasMicPermission: () -> Boolean,
    onSelectVoicePreset: (VoiceModPreset) -> Unit,
    onToggleLiveMic: () -> Unit,
    onStartOrStopClipTest: () -> Unit,
    onReplayRecordedClip: () -> Unit,
    onToggleNoiseGate: (Boolean) -> Unit,
    selectedDemoPhraseIndex: Int = 0,
    onPlayReadyDemoWithoutMic: (VoiceModPreset, Int) -> Unit = { preset, _ -> onSelectVoicePreset(preset) }
) {
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onStartOrStopClipTest()
        }
    }

    val liveMicPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onToggleLiveMic()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("studio_voice_changer_card"),
        shape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp, topEnd = 8.dp, bottomStart = 8.dp),
        color = GunmetalCard,
        border = BorderStroke(1.5.dp, ElectricPurple.copy(alpha = 0.8f))
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
                            .background(ElectricPurple.copy(alpha = 0.2f))
                            .border(1.dp, ElectricPurple, CutCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = ElectricPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مُغيّر الصوت الاحترافي (Studio HD)",
                                style = MaterialTheme.typography.titleSmall,
                                color = TitaniumWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MatrixGreen)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "تجربة بدون مايك 🔊",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ObsidianBlack
                                )
                            }
                        }
                        Text(
                            text = "اسمع صوت (أنثى / رجل / سنجاب / روبوت) فوراً بدون ما تتكلم، أو شغل المايك المباشر للألعاب!",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. INSTANT NO-MIC VOICE PREVIEW BOX (سماع الصوت بدون ما تتكلم)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("no_mic_voice_demo_box"),
                shape = RoundedCornerShape(12.dp),
                color = ObsidianSurface,
                border = BorderStroke(1.2.dp, MatrixGreen.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = MatrixGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🔊 اسمع وجرّب الصوت بدون ما تتكلم (مقاطع جاهزة):",
                                style = MaterialTheme.typography.labelLarge,
                                color = MatrixGreen,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اختر جملة جاهزة واضغط لسماع كيف يطلع صوت (${activeVoicePreset.arabicName}) فوراً بدون فتح المايك:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3 Ready-Made Gaming Phrases Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        READY_VOICE_DEMO_PHRASES.forEach { phrase ->
                            val isSelectedPhrase = selectedDemoPhraseIndex == phrase.index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelectedPhrase) CyberCyan.copy(alpha = 0.22f)
                                        else GunmetalCard
                                    )
                                    .border(
                                        width = if (isSelectedPhrase) 1.5.dp else 1.dp,
                                        color = if (isSelectedPhrase) CyberCyan else CarbonBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        onPlayReadyDemoWithoutMic(activeVoicePreset, phrase.index)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                                    .testTag("ready_voice_phrase_${phrase.index}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = phrase.shortLabelAr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelectedPhrase) CyberCyan else TitaniumWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Big One-Tap "Hear Selected Voice Now Without Speaking" Button
                    Button(
                        onClick = {
                            onPlayReadyDemoWithoutMic(activeVoicePreset, selectedDemoPhraseIndex)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("hear_voice_without_speaking_btn"),
                        shape = CutCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatrixGreen,
                            contentColor = ObsidianBlack
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPlayingClip)
                                "🔊 جاري تشغيل (${activeVoicePreset.arabicName})... اضغط للإعادة"
                            else
                                "🔊 اسمع الصوت الآن بدون ما تتكلم (${activeVoicePreset.arabicName})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5 Crystal-Clear Voice Presets Grid (Each row plays immediately on tap!)
            Text(
                text = "اختر نبرة الصوت (مجرد الضغط على أي صوت يسمّعك إياه فوراً بدون مايك):",
                style = MaterialTheme.typography.bodySmall,
                color = SilverMist
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VoiceModPreset.entries.forEach { preset ->
                    val selected = preset == activeVoicePreset
                    val presetColor = when (preset) {
                        VoiceModPreset.FEMALE_NATURAL -> CrimsonRed
                        VoiceModPreset.MALE_DEEP -> CyberCyan
                        VoiceModPreset.SQUIRREL_FUN -> MoltenAmber
                        VoiceModPreset.CYBER_ROBOT -> ElectricPurple
                        VoiceModPreset.ORIGINAL -> MatrixGreen
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPlayReadyDemoWithoutMic(preset, selectedDemoPhraseIndex)
                            }
                            .testTag("voice_preset_option_${preset.id}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) presetColor.copy(alpha = 0.20f) else ObsidianSurface,
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) presetColor else CarbonBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.arabicName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selected) presetColor else TitaniumWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = preset.subtitleAr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverMist,
                                    fontSize = 11.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (selected) presetColor else presetColor.copy(alpha = 0.18f)
                                        )
                                        .clickable {
                                            onPlayReadyDemoWithoutMic(preset, selectedDemoPhraseIndex)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                        .testTag("listen_preset_btn_${preset.id}")
                                ) {
                                    Text(
                                        text = if (selected) "🔊 اسمع مرة أخرى" else "🔊 اسمع الصوت",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) ObsidianBlack else presetColor,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Audio Level & Status Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = ObsidianSurface,
                border = BorderStroke(1.dp, CarbonBorder)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = if (isLiveMicActive || isRecordingClip || isPlayingClip) MatrixGreen else CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = voiceStatusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = TitaniumWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Audio Level Meter Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(GunmetalCard)
                    ) {
                        val barFraction = micInputLevel.coerceIn(0.04f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(barFraction)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(
                                    if (isLiveMicActive || isRecordingClip || isPlayingClip) MatrixGreen else ElectricPurple
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "🎙️ أو استخدم المايكروفون لتغيير صوتك الحقيقي أثناء اللعب:",
                style = MaterialTheme.typography.labelMedium,
                color = SilverMist,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons: 1. Record & Hear Your Own Voice Cleanly, 2. Replay
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (hasMicPermission()) {
                            onStartOrStopClipTest()
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(46.dp)
                        .testTag("voice_record_test_btn"),
                    shape = CutCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecordingClip) CrimsonRed else ElectricPurple,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isRecordingClip) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRecordingClip) "إيقاف وسماع صوتك الآن" else "🎙️ سجل بصوتك وجرّب",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onReplayRecordedClip,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(46.dp)
                        .testTag("voice_replay_sample_btn"),
                    shape = CutCornerShape(10.dp),
                    border = BorderStroke(1.dp, CyberCyan),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "إعادة السماع",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Live Mic Stream Toggle Button (For Headphones / Live Game Chat)
            Button(
                onClick = {
                    if (hasMicPermission()) {
                        onToggleLiveMic()
                    } else {
                        liveMicPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("voice_live_mic_stream_btn"),
                shape = CutCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLiveMicActive) CrimsonRed else ObsidianSurface,
                    contentColor = if (isLiveMicActive) Color.White else MatrixGreen
                ),
                border = BorderStroke(1.2.dp, if (isLiveMicActive) CrimsonRed else MatrixGreen)
            ) {
                Icon(
                    imageVector = if (isLiveMicActive) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isLiveMicActive)
                        "🔴 إيقاف المايك المباشر المستمر (يعمل الآن)"
                    else
                        "🎙️ فتح المايك المباشر المستمر للألعاب (Live Mic Stream)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // HD Noise Gate & Anti-Glitch Filter Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianSurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "مانع التشويش وتصفية المايك التلقائي (HD Pure Voice)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "يعزل هواء المايك والضجيج ليخرج صوت (الأنثى/الرجل/السنجاب/الروبوت) صافياً 100%",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist,
                        fontSize = 10.sp
                    )
                }
                Switch(
                    checked = noiseGateEnabled,
                    onCheckedChange = onToggleNoiseGate,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = MatrixGreen
                    )
                )
            }
        }
    }
}
