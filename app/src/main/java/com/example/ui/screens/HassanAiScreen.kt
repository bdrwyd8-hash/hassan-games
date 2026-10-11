package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiActionType
import com.example.model.AiChatMessage
import com.example.model.AiGameSetupAdvice
import com.example.model.AiSuggestionPrompt
import com.example.model.GameSpaceProfile
import com.example.model.GamingSessionReport
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HassanAiScreen(
    messages: List<AiChatMessage>,
    suggestionPrompts: List<AiSuggestionPrompt>,
    isAiThinking: Boolean,
    selectedGame: GameSpaceProfile?,
    smartGameSetupAdvice: AiGameSetupAdvice?,
    sessionReports: List<GamingSessionReport>,
    onSendQuestion: (String) -> Unit,
    onExecuteAiAction: (AiActionType) -> Unit,
    onRequestSmartGameSetup: () -> Unit,
    onApplySmartGameSetup: (AiGameSetupAdvice) -> Unit,
    onClearChatHistory: () -> Unit,
    onClearSessionReports: () -> Unit
) {
    var inputQuery by remember { mutableStateOf("") }
    var useEnglishSuggestions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("hassan_ai_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hassan AI Hero Header Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hassan_ai_hero_card"),
            shape = CutCornerShape(topStart = 20.dp, bottomEnd = 20.dp, topEnd = 8.dp, bottomStart = 8.dp),
            color = GunmetalCard,
            border = BorderStroke(1.5.dp, CyberCyan.copy(alpha = 0.85f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                                .background(CyberCyan.copy(alpha = 0.18f))
                                .border(1.dp, CyberCyan, CutCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "HASSAN AI • المساعد الذكي للألعاب",
                                style = MaterialTheme.typography.titleMedium,
                                color = TitaniumWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "يفهم ميزات التطبيق وقراءات جهازك والشبكة فعلياً (عربي / English)",
                                style = MaterialTheme.typography.bodySmall,
                                color = SilverMist,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Language Switcher Chip for Quick Suggestions
                    Surface(
                        modifier = Modifier
                            .clickable { useEnglishSuggestions = !useEnglishSuggestions }
                            .testTag("ai_language_toggle_btn"),
                        shape = RoundedCornerShape(8.dp),
                        color = ObsidianSurface,
                        border = BorderStroke(1.dp, CyberCyan)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (useEnglishSuggestions) "AR عربي" else "EN",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                        }
                    }
                }

                // AI Gaming Optimizer 3-in-1 Quick Diagnostic Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OptimizerQuickButton(
                        title = if (useEnglishSuggestions) "Network Advisor" else "مستشار الشبكة",
                        icon = Icons.Default.NetworkCheck,
                        accent = CyberCyan,
                        testTag = "ai_run_network_advisor_btn",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onSendQuestion(
                                if (useEnglishSuggestions) "Check my connection issue and analyze the latest Network Booster result."
                                else "افحص مشكلة الاتصال وحلل آخر نتيجة في Network Booster."
                            )
                        }
                    )
                    OptimizerQuickButton(
                        title = if (useEnglishSuggestions) "Perf Advisor" else "مستشار الأداء",
                        icon = Icons.Default.Memory,
                        accent = MatrixGreen,
                        testTag = "ai_run_performance_advisor_btn",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onSendQuestion(
                                if (useEnglishSuggestions) "Help me fix lag or stutter and analyze my RAM and thermal status."
                                else "ساعدني في حل مشكلة التقطيع وحلل حالة الرام والحرارة."
                            )
                        }
                    )
                    OptimizerQuickButton(
                        title = if (useEnglishSuggestions) "Smart Setup" else "إعداد اللعبة",
                        icon = Icons.Default.SportsEsports,
                        accent = MoltenAmber,
                        testTag = "ai_run_smart_game_setup_btn",
                        modifier = Modifier.weight(1f),
                        onClick = onRequestSmartGameSetup
                    )
                }

                // Ready-Made Suggestion Prompts (Section 5.1)
                Text(
                    text = if (useEnglishSuggestions) "Quick Questions (Tap to Ask):" else "أسئلة جاهزة سريعة (اضغط للسؤال):",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.testTag("ai_suggestion_chips_row")
                ) {
                    suggestionPrompts.forEach { prompt ->
                        Surface(
                            modifier = Modifier
                                .clickable {
                                    val q = if (useEnglishSuggestions) prompt.queryEn else prompt.queryAr
                                    onSendQuestion(q)
                                }
                                .testTag("ai_prompt_chip_${prompt.id}"),
                            shape = RoundedCornerShape(16.dp),
                            color = ObsidianSurface,
                            border = BorderStroke(1.dp, CarbonBorder)
                        ) {
                            Text(
                                text = if (useEnglishSuggestions) prompt.titleEn else prompt.titleAr,
                                style = MaterialTheme.typography.labelSmall,
                                color = TitaniumWhite,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. AI Smart Game Setup Card (Section 6.3 — shown when requested or for active game)
        if (smartGameSetupAdvice != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_smart_game_setup_card"),
                shape = RoundedCornerShape(14.dp),
                color = GunmetalCard,
                border = BorderStroke(1.dp, MoltenAmber.copy(alpha = 0.75f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MoltenAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "AI Smart Game Setup • إعداد ذكي للعبة (${smartGameSetupAdvice.gameTitle})",
                                style = MaterialTheme.typography.titleSmall,
                                color = TitaniumWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = smartGameSetupAdvice.deviceSummaryAr,
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberCyan,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = "• وضع الأداء المقترح: ${smartGameSetupAdvice.recommendedPerformanceMode.titleAr}\n" +
                            "• وضع التبريد المقترح: ${smartGameSetupAdvice.recommendedThermalMode.titleAr}\n" +
                            "• إعدادات الجرافيك المقترحة داخل اللعبة: ${smartGameSetupAdvice.recommendedInGameGraphicsAr}\n" +
                            "• الإطارات المستهدفة: ${smartGameSetupAdvice.recommendedFpsCapAr}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TitaniumWhite,
                        fontSize = 11.sp
                    )

                    Text(
                        text = "السبب: ${smartGameSetupAdvice.reasoningAr}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MatrixGreen,
                        fontSize = 11.sp
                    )

                    Text(
                        text = smartGameSetupAdvice.honestyDisclaimerAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverMist,
                        fontSize = 10.sp
                    )

                    Button(
                        onClick = { onApplySmartGameSetup(smartGameSetupAdvice) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("apply_ai_game_setup_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MoltenAmber,
                            contentColor = ObsidianBlack
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تطبيق توصيات وضع الأداء والتبريد للعبة (${smartGameSetupAdvice.gameTitle})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 3. Conversation / Chat Box Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hassan_ai_chat_card"),
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
                    Text(
                        text = "محادثة المساعد الذكي (Hassan Assistant Chat)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TitaniumWhite,
                        fontWeight = FontWeight.Bold
                    )
                    if (messages.size > 1) {
                        Text(
                            text = "مسح المحادثة",
                            style = MaterialTheme.typography.labelSmall,
                            color = CrimsonRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onClearChatHistory() }
                                .testTag("clear_ai_chat_btn")
                        )
                    }
                }

                // Input Box at Top of Chat for easy reach on all screen sizes & tests
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hassan_ai_input_field"),
                        placeholder = {
                            Text(
                                text = if (useEnglishSuggestions) "Ask Hassan AI anything about the app or lag..."
                                else "اكتب سؤالك هنا بالعربية أو الإنجليزية...",
                                fontSize = 12.sp,
                                color = SilverMist
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TitaniumWhite,
                            unfocusedTextColor = TitaniumWhite,
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CarbonBorder,
                            focusedContainerColor = ObsidianSurface,
                            unfocusedContainerColor = ObsidianSurface
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            val trimmed = inputQuery.trim()
                            if (trimmed.isNotEmpty() && !isAiThinking) {
                                onSendQuestion(trimmed)
                                inputQuery = ""
                            }
                        },
                        enabled = !isAiThinking,
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("hassan_ai_send_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = ObsidianBlack
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        if (isAiThinking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = ObsidianBlack,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "إرسال السؤال",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Messages List
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hassan_ai_chat_list")
                ) {
                    messages.forEach { msg ->
                        AiMessageBubble(
                            message = msg,
                            useEnglishLabels = useEnglishSuggestions,
                            onExecuteAction = onExecuteAiAction
                        )
                    }
                }
            }
        }

        // 4. Smart Gaming Session Reports Card (Section 6.4 & Section 7)
        SmartGamingSessionReportsCard(
            reports = sessionReports,
            onClearReports = onClearSessionReports
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiMessageBubble(
    message: AiChatMessage,
    useEnglishLabels: Boolean,
    onExecuteAction: (AiActionType) -> Unit
) {
    val bgColor = if (message.senderIsUser) {
        CrimsonRed.copy(alpha = 0.16f)
    } else {
        ObsidianSurface
    }
    val borderColor = if (message.senderIsUser) {
        CrimsonRed.copy(alpha = 0.6f)
    } else {
        CyberCyan.copy(alpha = 0.5f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (message.senderIsUser) "ai_user_msg_${message.id}" else "ai_bot_msg_${message.id}"),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (message.senderIsUser) CrimsonRed else MatrixGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (message.senderIsUser) "أنت (User)" else message.sourceBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.senderIsUser) CrimsonRed else CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = message.text,
                style = MaterialTheme.typography.bodySmall,
                color = TitaniumWhite,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            // Direct Action Chips (Section 5.3)
            if (!message.senderIsUser && message.suggestedActions.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    message.suggestedActions.forEach { chip ->
                        Surface(
                            modifier = Modifier
                                .clickable { onExecuteAction(chip.actionType) }
                                .testTag("ai_action_chip_${chip.actionType.name}"),
                            shape = RoundedCornerShape(8.dp),
                            color = CyberCyan.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, CyberCyan)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (useEnglishLabels) chip.labelEn else chip.labelAr,
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
    }
}

@Composable
fun SmartGamingSessionReportsCard(
    reports: List<GamingSessionReport>,
    onClearReports: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("session_reports_card"),
        shape = RoundedCornerShape(14.dp),
        color = GunmetalCard,
        border = BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.65f))
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
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Smart Gaming Report • تقارير جلسات اللعب (${reports.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = TitaniumWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "تحليل ما بعد الجلسة بناءً على القراءات الفعلية (محفوظ محلياً)",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMist,
                            fontSize = 10.sp
                        )
                    }
                }

                if (reports.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onClearReports,
                        modifier = Modifier.testTag("clear_session_reports_btn"),
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
                            text = "حذف السجل",
                            style = MaterialTheme.typography.labelSmall,
                            color = CrimsonRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (reports.isEmpty()) {
                Text(
                    text = "لا توجد تقارير جلسات مسجلة بعد. عند تشغيل لعبة من (MY GAMES) ثم الضغط على (إنهاء الجلسة END) أو إيقاف Gaming Mode، سيظهر تقرير الجلسة الفعلي هنا.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverMist,
                    fontSize = 11.sp
                )
            } else {
                reports.take(4).forEach { report ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianSurface)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${report.gameTitle} • ${report.durationFormattedAr}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TitaniumWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = report.appliedPerformanceMode.badgeText,
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                color = report.appliedPerformanceMode.primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = report.stabilityLabelAr,
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        report.measuredFactsAr.forEach { fact ->
                            Text(
                                text = "• $fact",
                                style = MaterialTheme.typography.bodySmall,
                                color = SilverMist,
                                fontSize = 10.sp
                            )
                        }

                        report.aiNextSessionRecommendationsAr.forEach { rec ->
                            Text(
                                text = "🤖 $rec",
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

@Composable
private fun OptimizerQuickButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TitaniumWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}
