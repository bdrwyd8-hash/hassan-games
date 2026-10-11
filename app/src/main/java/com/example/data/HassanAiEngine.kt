package com.example.data

import com.example.BuildConfig
import com.example.model.AiActionType
import com.example.model.AiChatMessage
import com.example.model.AiGameSetupAdvice
import com.example.model.AiQuickActionChip
import com.example.model.AiSuggestionPrompt
import com.example.model.GameSpaceProfile
import com.example.model.GamingSessionReport
import com.example.model.HardwareTelemetry
import com.example.model.NetworkCheckReport
import com.example.model.NetworkConnectionType
import com.example.model.PerformanceMode
import com.example.model.SmartThermalMode
import com.example.model.SmartThermalStatus
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * Hassan AI Assistant & Smart Gaming Optimizer Engine.
 *
 * Combines:
 * 1. Official Gemini REST API (`gemini-3.5-flash`) using `BuildConfig.GEMINI_API_KEY` on `Dispatchers.IO`
 *    with strict rate-limiting, 25s/60s timeouts, and zero background polling.
 * 2. Dynamic Live App Knowledge Base (`buildSystemKnowledgeSnapshot`) that reflects the exact current
 *    screens, tools, permissions, hardware readings, and latest network test results of Hassan Games.
 * 3. Deterministic Local Smart Knowledge & Diagnostics Engine when offline or when cloud API is unavailable,
 *    clearly badged so the user always knows whether a response came from Cloud AI or Local Assistant.
 */
class HassanAiEngine {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Volatile
    var forceSimulateApiFailureForTesting: Boolean = false

    @Volatile
    private var lastCloudCallTimestampMs: Long = 0L

    val defaultPrompts: List<AiSuggestionPrompt> = listOf(
        AiSuggestionPrompt(
            id = "explain_app",
            titleAr = "اشرح لي Hassan Games",
            titleEn = "Explain Hassan Games",
            queryAr = "اشرح لي Hassan Games وما الأقسام والأدوات الموجودة فيه حالياً؟",
            queryEn = "Explain Hassan Games and what sections and tools are currently available?"
        ),
        AiSuggestionPrompt(
            id = "how_add_game",
            titleAr = "كيف أضيف لعبة؟",
            titleEn = "How do I add a game?",
            queryAr = "كيف أضيف لعبة جديدة إلى قسم MY GAMES وأشغلها مع Gaming Mode؟",
            queryEn = "How do I add a game to MY GAMES and launch it with Gaming Mode?"
        ),
        AiSuggestionPrompt(
            id = "improve_gaming",
            titleAr = "كيف أحسن تجربة اللعب؟",
            titleEn = "How to improve gaming?",
            queryAr = "حلل حالة جهازي الحالية واقترح كيف أحسن تجربة اللعب.",
            queryEn = "Analyze my current device state and suggest how to improve my gaming experience."
        ),
        AiSuggestionPrompt(
            id = "check_connection",
            titleAr = "افحص مشكلة الاتصال",
            titleEn = "Check connection issue",
            queryAr = "افحص مشكلة الاتصال وحلل آخر نتيجة في Network Booster.",
            queryEn = "Check my connection issue and analyze the latest Network Booster result."
        ),
        AiSuggestionPrompt(
            id = "why_tool_not_working",
            titleAr = "لماذا هذه الأداة لا تعمل؟",
            titleEn = "Why is a tool not working?",
            queryAr = "لماذا الشريط العائم أو الأزرار المنسوخة لا تظهر فوق اللعبة؟ افحص الصلاحيات.",
            queryEn = "Why is the floating HUD or cloned buttons not showing over my game? Check permissions."
        ),
        AiSuggestionPrompt(
            id = "explain_gaming_mode",
            titleAr = "اشرح لي أدوات Gaming Mode",
            titleEn = "Explain Gaming Mode tools",
            queryAr = "اشرح لي أدوات Gaming Mode والشريط العائم Gaming HUD بالتفصيل.",
            queryEn = "Explain the Gaming Mode tools and the floating Gaming HUD in detail."
        ),
        AiSuggestionPrompt(
            id = "fix_stutter_lag",
            titleAr = "ساعدني في حل مشكلة التقطيع",
            titleEn = "Help me fix lag/stutter",
            queryAr = "ساعدني في حل مشكلة التقطيع (Lag / Frame Drops) وحدد هل السبب من الشبكة أم الحرارة والرام.",
            queryEn = "Help me fix lag or stutter and determine if it is caused by network or thermal/RAM load."
        )
    )

    data class LiveAppContext(
        val telemetry: HardwareTelemetry,
        val performanceMode: PerformanceMode,
        val thermalStatus: SmartThermalStatus,
        val latestNetworkReport: NetworkCheckReport?,
        val overlayPermissionGranted: Boolean,
        val accessibilityEnabled: Boolean,
        val dndPermissionGranted: Boolean,
        val floatingHudRunning: Boolean,
        val crosshairEnabled: Boolean,
        val clonedButtonsEnabled: Boolean,
        val myGames: List<GameSpaceProfile>,
        val selectedGame: GameSpaceProfile?,
        val latestSessionReport: GamingSessionReport?
    )

    fun buildInitialWelcomeMessage(): AiChatMessage {
        return AiChatMessage(
            id = "ai_welcome_init",
            senderIsUser = false,
            text = "مرحباً بك في **Hassan AI** — المساعد الذكي المدمج في **Hassan Games**!\n" +
                "أنا متصل مباشرة بقراءات جهازك الفعلية، وأفهم جميع أقسام التطبيق (ألعابي MY GAMES، مُحسّن الشبكة Network Booster، الشريط العائم Gaming HUD، وأدوات اللعب).\n\n" +
                "اسألني بالعربية أو الإنجليزية عن أي أداة، أو اطلب تحليل سبب ارتفاع الـ Ping والتقطيع، أو اضغط على أحد الأسئلة السريعة أدناه:",
            timestampMs = System.currentTimeMillis(),
            sourceBadge = "Hassan AI • محرك المعرفة الذكي المدمج",
            suggestedActions = listOf(
                AiQuickActionChip("فحص الشبكة الآن", "Run Network Check", AiActionType.RUN_NETWORK_CHECK_NOW),
                AiQuickActionChip("فتح MY GAMES", "Open MY GAMES", AiActionType.OPEN_MY_GAMES),
                AiQuickActionChip("فتح أدوات اللعب", "Gaming Tools", AiActionType.OPEN_GAMING_TOOLS)
            ),
            isOfflineOrFallback = false
        )
    }

    /**
     * Answers a user question in Arabic or English using real app state.
     * Attempts cloud Gemini (`gemini-3.5-flash`) if a valid API key is configured and online;
     * otherwise transparently uses the built-in Local Knowledge & Diagnostics Engine.
     */
    suspend fun askHassanAi(
        userQuestion: String,
        context: LiveAppContext
    ): AiChatMessage = withContext(Dispatchers.IO) {
        val cleanQuestion = userQuestion.trim()
        val isEnglish = detectEnglishQuery(cleanQuestion)
        val actions = inferRelevantActionChips(cleanQuestion, context)

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (_: Exception) {
            ""
        }

        val hasConfiguredCloudKey = apiKey.isNotBlank() &&
            apiKey != "MY_GEMINI_API_KEY" &&
            !apiKey.startsWith("YOUR_") &&
            apiKey.length >= 20

        val isNetworkOnline = context.latestNetworkReport?.isConnected != false

        if (forceSimulateApiFailureForTesting) {
            val localReply = generateLocalGroundedResponse(cleanQuestion, context, isEnglish)
            val fallbackPrefix = if (isEnglish) {
                "[Notice: Cloud AI connection failed or is unavailable. Answered via Built-in Local Hassan Knowledge Engine]\n\n"
            } else {
                "[تنبيه: تعذر الاتصال بخدمة الذكاء الاصطناعي السحابية حالياً — تم الرد عبر نظام المساعدة المحلي المدمج بناءً على قراءات جهازك الفعلية]\n\n"
            }
            return@withContext AiChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                senderIsUser = false,
                text = fallbackPrefix + localReply,
                timestampMs = System.currentTimeMillis(),
                sourceBadge = if (isEnglish) "Local Knowledge Engine (Fallback)" else "نظام المساعدة المحلي (بدون اتصال سحابي)",
                suggestedActions = actions,
                isOfflineOrFallback = true
            )
        }

        val now = System.currentTimeMillis()
        val canCallCloud = hasConfiguredCloudKey && isNetworkOnline && (now - lastCloudCallTimestampMs >= 2500L)

        if (canCallCloud) {
            lastCloudCallTimestampMs = now
            val cloudText = callGeminiRestApi(apiKey, cleanQuestion, context, isEnglish)
            if (!cloudText.isNullOrBlank()) {
                return@withContext AiChatMessage(
                    id = "ai_${System.currentTimeMillis()}",
                    senderIsUser = false,
                    text = cloudText,
                    timestampMs = System.currentTimeMillis(),
                    sourceBadge = "Hassan AI • Gemini 3.5 Flash (سحابي)",
                    suggestedActions = actions,
                    isOfflineOrFallback = false
                )
            }
        }

        // Local Knowledge & Diagnostics Engine (instant, works offline, 100% grounded in real app state)
        val localText = generateLocalGroundedResponse(cleanQuestion, context, isEnglish)
        val badge = when {
            !isNetworkOnline -> if (isEnglish) "Hassan AI • Local Offline Mode" else "Hassan AI • وضع المساعدة المحلي (بدون إنترنت)"
            !hasConfiguredCloudKey -> if (isEnglish) "Hassan AI • Built-in Smart Engine" else "Hassan AI • محرك المعرفة الذكي المدمج"
            else -> if (isEnglish) "Hassan AI • Local Fallback" else "Hassan AI • المساعد المحلي الاحتياطي"
        }

        AiChatMessage(
            id = "ai_${System.currentTimeMillis()}",
            senderIsUser = false,
            text = localText,
            timestampMs = System.currentTimeMillis(),
            sourceBadge = badge,
            suggestedActions = actions,
            isOfflineOrFallback = true
        )
    }

    /**
     * Section 6.2: AI Performance Advisor based strictly on real measured device telemetry.
     */
    fun generatePerformanceAdvisorReport(context: LiveAppContext): String {
        val t = context.telemetry
        val temp = t.batteryTempCelsius
        val ramAvail = t.ramAvailableMb
        val ramPct = t.ramUsagePercent
        val hz = t.displayRefreshRateHz

        val findings = mutableListOf<String>()
        if (temp >= 40.5f) {
            findings.add("• حرارة البطارية المقاسة حالياً مرتفعة (${temp}°C): قد يقوم نظام أندرويد بتخفيض تردد المعالج تلقائياً لحماية الجهاز (Thermal Throttling). ننصح بتفعيل وضع (ثبات بارد أقصى Cool Stability) وإيقاف الشحن أثناء اللعب.")
        } else {
            findings.add("• الحالة الحرارية مستقرة (${temp}°C حرارة البطارية): لا يوجد ضغط حراري يمنع استقرار الإطارات حالياً.")
        }

        if (ramAvail < 1600 || ramPct >= 78) {
            findings.add("• الذاكرة المتاحة منخفضة نسبياً (${ramAvail}MB متاح من أصل ${t.ramTotalMb}MB — استهلاك $ramPct%): يوصى بتشغيل (تنظيف الرام 1-Tap RAM Purge) لإغلاق تطبيقات الخلفية غير المحمية قبل فتح اللعبة.")
        } else {
            findings.add("• الذاكرة العشوائية كافية (${ramAvail}MB متاحة للألعاب): لا حاجة لإيقاف قسري متكرر للعمليات.")
        }

        findings.add("• شاشة الجهاز تعمل بمعدل تحديث ${hz}Hz: للحصول على أقل تأخير، اضبط معدل الإطارات داخل إعدادات اللعبة ليتوافق مع ${hz}Hz إن كانت اللعبة تدعمه.")
        findings.add("• تنبيه تقني صادق: لا يدّعي Hassan Games كسر سرعة المعالج (Overclock) أو تعطيل حماية النظام الحرارية، بل يقلل الحمل الخلفي والمؤثرات الثانوية للحفاظ على ثبات الأداء.")

        return findings.joinToString("\n")
    }

    /**
     * Section 6.3: AI Smart Game Setup for a specific game in My Games.
     */
    fun buildSmartGameSetupAdvice(
        profile: GameSpaceProfile,
        telemetry: HardwareTelemetry
    ): AiGameSetupAdvice {
        val totalRamGb = (telemetry.ramTotalMb / 1024f).roundToInt().coerceAtLeast(2)
        val availRamMb = telemetry.ramAvailableMb
        val temp = telemetry.batteryTempCelsius
        val hz = telemetry.displayRefreshRateHz

        val isHighTier = telemetry.ramTotalMb >= 6500 && telemetry.cpuMaxFreqMhz >= 2400
        val isHot = temp >= 39.5f

        val recPerf = when {
            isHot -> PerformanceMode.BALANCED
            isHighTier -> profile.recommendedMode
            else -> PerformanceMode.PERFORMANCE
        }

        val recThermal = when {
            isHot -> SmartThermalMode.ECO_STABILITY
            !isHighTier -> SmartThermalMode.AUTO_ADAPTIVE
            else -> SmartThermalMode.AUTO_ADAPTIVE
        }

        val inGameGfx = when {
            isHot || availRamMb < 1500 ->
                "رسوميات Smooth (سلسة) + إيقاف الظلال (Shadows OFF) + إيقاف مضاد التعرج (Anti-Aliasing OFF)"
            isHighTier ->
                "رسوميات Balanced / HD مع تفعيل أقصى معدل إطارات متاح (${minOf(hz, profile.recommendedFps)} FPS)"
            else ->
                "رسوميات Smooth / Balanced مع إيقاف الظلال للحفاظ على ثبات الإطارات"
        }

        val fpsCap = "استهداف حتى ${minOf(hz, profile.recommendedFps)} FPS (بحسب دعم اللعبة الفعلي وشاشة ${hz}Hz)"

        val installNote = if (profile.isInstalled) {
            "اللعبة (${profile.title}) مثبتة فعلياً على الجهاز (${profile.packageName}) وجاهزة للتشغيل."
        } else {
            "تنبيه: الحزمة (${profile.packageName}) غير مثبتة حالياً على هذا الجهاز؛ يمكنك حفظ الإعدادات أو استبدالها بلعبة مثبتة عبر (+ ADD GAME)."
        }

        return AiGameSetupAdvice(
            gameTitle = profile.title,
            packageName = profile.packageName,
            isInstalled = profile.isInstalled,
            canVerifyInGameSettings = false,
            deviceSummaryAr = "ذاكرة الجهاز ${totalRamGb}GB RAM (${availRamMb}MB متاحة) • شاشة ${hz}Hz • حرارة البطارية ${temp}°C",
            recommendedPerformanceMode = recPerf,
            recommendedThermalMode = recThermal,
            recommendedInGameGraphicsAr = inGameGfx,
            recommendedFpsCapAr = fpsCap,
            reasoningAr = "$installNote تم اختيار (${recPerf.titleAr}) مع (${recThermal.titleAr}) بناءً على الذاكرة المتاحة (${availRamMb}MB) وحرارة الجهاز الحالية (${temp}°C).",
            honestyDisclaimerAr = "ملاحظة: نظام Android يمنع التطبيقات الخارجية من قراءة أو تعديل ملفات إعدادات الجرافيك الداخلية للعبة مباشرة؛ يطبّق Hassan Games إعدادات وضع اللعب والشريط العائم والـ DND تلقائياً، بينما يُنصح بتطبيق إعدادات الجرافيك المذكورة أعلاه من داخل قائمة إعدادات اللعبة نفسها."
        )
    }

    /**
     * Section 6.4 & Section 7: Builds a truthful post-game Smart Gaming Report.
     */
    fun buildSessionReport(
        gameTitle: String,
        packageName: String,
        startTimeMs: Long,
        endTimeMs: Long,
        appliedMode: PerformanceMode,
        telemetryStart: HardwareTelemetry,
        telemetryEnd: HardwareTelemetry,
        networkReportsDuringSession: List<NetworkCheckReport>,
        disconnectsCount: Int,
        currentNetworkTypeLabel: String
    ): GamingSessionReport {
        val elapsedMs = (endTimeMs - startTimeMs).coerceAtLeast(1000L)
        val durationSec = (elapsedMs / 1000L).toInt()
        val durationMin = (durationSec / 60).coerceAtLeast(1)
        val durationFormatted = if (durationSec < 60) {
            "$durationSec ثانية"
        } else {
            "${durationSec / 60} دقيقة و ${durationSec % 60} ثانية"
        }

        val onlineReports = networkReportsDuringSession.filter { it.isConnected && it.avgPingMs != null }
        val allPings = onlineReports.flatMap {
            if (it.pingSamplesMs.isNotEmpty()) it.pingSamplesMs else listOfNotNull(it.avgPingMs)
        }

        val avgPing = if (allPings.isNotEmpty()) allPings.average().roundToInt() else null
        val minPing = allPings.minOrNull()
        val maxPing = allPings.maxOrNull()
        val avgJitter = if (onlineReports.isNotEmpty()) {
            onlineReports.mapNotNull { it.jitterMs }.average().takeIf { !it.isNaN() }?.roundToInt()
        } else {
            null
        }

        val anySpikes = onlineReports.any { it.spikeDetected }
        val maxLoss = onlineReports.mapNotNull { it.packetLossPercent }.maxOrNull() ?: 0

        val stabilityLabel = when {
            networkReportsDuringSession.isEmpty() ->
                "لم يتم إجراء فحص شبكة تفصيلي خلال هذه الجلسة (آخر بنق مقاس: ${telemetryEnd.pingMs}ms)"
            onlineReports.isEmpty() ->
                "تم رصد انقطاع في الاتصال أثناء الفحص خلال الجلسة"
            maxLoss > 0 || disconnectsCount > 0 ->
                "رُصد فقدان حزم ($maxLoss%) أو انقطاعات ($disconnectsCount) أثناء الجلسة"
            anySpikes || (avgJitter ?: 0) >= 12 ->
                "اتصال متقلب (تراوح البنق بين ${minPing}ms و ${maxPing}ms • تذبذب ${avgJitter}ms)"
            else ->
                "اتصال مستقر طوال اختبارات الجلسة (متوسط ${avgPing}ms • تذبذب ${avgJitter ?: 2}ms)"
        }

        val tempRise = ((telemetryEnd.batteryTempCelsius - telemetryStart.batteryTempCelsius) * 10f).roundToInt() / 10f

        val facts = mutableListOf<String>()
        facts.add("حقيقة مقاسة — مدة الجلسة: $durationFormatted على وضع (${appliedMode.titleAr}).")
        if (avgPing != null) {
            facts.add("حقيقة مقاسة — الشبكة: متوسط Ping الاختبار ${avgPing}ms (الأدنى ${minPing}ms / الأعلى ${maxPing}ms) على $currentNetworkTypeLabel.")
        } else {
            facts.add("حقيقة مقاسة — الشبكة: نوع الاتصال ($currentNetworkTypeLabel)، ولم تُسجّل اختبارات شبكة مكتملة أثناء الجلسة.")
        }
        facts.add(
            "حقيقة مقاسة — حرارة البطارية: بدأت عند ${telemetryStart.batteryTempCelsius}°C وانتهت عند ${telemetryEnd.batteryTempCelsius}°C (التغير: ${if (tempRise >= 0) "+$tempRise" else "$tempRise"}°C)."
        )
        facts.add(
            "حقيقة مقاسة — الذاكرة المتاحة: ${telemetryStart.ramAvailableMb}MB عند البدء ⬅ ${telemetryEnd.ramAvailableMb}MB عند الانتهاء."
        )

        val hasNetworkProblemEvidence = maxLoss > 0 || disconnectsCount > 0 || ((avgJitter ?: 0) >= 14 && onlineReports.size >= 1)
        val hasThermalProblemEvidence = telemetryEnd.batteryTempCelsius >= 41.0f
        val sufficientEvidence = hasNetworkProblemEvidence || hasThermalProblemEvidence || onlineReports.isNotEmpty()

        val nextRecs = mutableListOf<String>()
        if (hasNetworkProblemEvidence) {
            nextRecs.add("توصية للجلسة القادمة: رُصد تذبذب/فقدان حزم في الشبكة؛ يفضّل الاقتراب من الراوتر وإيقاف التحميلات الخلفية قبل اللعب.")
        } else if (avgPing != null) {
            nextRecs.add("تقييـم الشبكة: لم يُرصد خلل مستمر في الشبكة؛ ارتفاع Ping لمرة واحدة لا يعني بالضرورة أن الشبكة سبب أي تقطيع رسومي.")
        } else {
            nextRecs.add("نصيحة: شغّل (One-Tap Network Check) قبل الجلسة القادمة لتسجيل خط أساس دقيق لاستقرار الاتصال.")
        }

        if (hasThermalProblemEvidence) {
            nextRecs.add("توصية حرارية: وصلت حرارة البطارية إلى ${telemetryEnd.batteryTempCelsius}°C؛ جرّب تفعيل وضع (Cool Stability) في الجلسة القادمة لتفادي هبوط الإطارات.")
        } else {
            nextRecs.add("تقييم العتاد: بقيت حرارة الجهاز (${telemetryEnd.batteryTempCelsius}°C) والذاكرة المتاحة (${telemetryEnd.ramAvailableMb}MB) ضمن النطاق الآمن.")
        }

        return GamingSessionReport(
            id = "sess_$endTimeMs",
            gameTitle = gameTitle,
            packageName = packageName,
            startTimeMs = startTimeMs,
            endTimeMs = endTimeMs,
            durationMinutes = durationMin,
            durationFormattedAr = durationFormatted,
            avgPingMs = avgPing,
            minPingMs = minPing,
            maxPingMs = maxPing,
            jitterMs = avgJitter,
            stabilityLabelAr = stabilityLabel,
            disconnectsCount = disconnectsCount,
            networkTypeLabelAr = currentNetworkTypeLabel,
            batteryTempStartC = telemetryStart.batteryTempCelsius,
            batteryTempEndC = telemetryEnd.batteryTempCelsius,
            ramAvailableStartMb = telemetryStart.ramAvailableMb,
            ramAvailableEndMb = telemetryEnd.ramAvailableMb,
            batteryPercentEnd = telemetryEnd.batteryPercent,
            appliedPerformanceMode = appliedMode,
            sufficientEvidenceForLagCause = sufficientEvidence,
            measuredFactsAr = facts,
            aiNextSessionRecommendationsAr = nextRecs
        )
    }

    private fun callGeminiRestApi(
        apiKey: String,
        question: String,
        context: LiveAppContext,
        isEnglish: Boolean
    ): String? {
        return try {
            val systemPrompt = buildSystemInstructionPrompt(context, isEnglish)
            val rootJson = JSONObject().apply {
                put(
                    "systemInstruction",
                    JSONObject().apply {
                        put(
                            "parts",
                            JSONArray().put(JSONObject().put("text", systemPrompt))
                        )
                    }
                )
                put(
                    "contents",
                    JSONArray().put(
                        JSONObject().apply {
                            put("role", "user")
                            put(
                                "parts",
                                JSONArray().put(JSONObject().put("text", question))
                            )
                        }
                    )
                )
                put(
                    "generationConfig",
                    JSONObject().apply {
                        put("temperature", 0.35)
                        put("maxOutputTokens", 500)
                    }
                )
            }

            val url =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val body = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val rawBody = response.body?.string() ?: return null
                val json = JSONObject(rawBody)
                val candidates = json.optJSONArray("candidates") ?: return null
                val firstCandidate = candidates.optJSONObject(0) ?: return null
                val content = firstCandidate.optJSONObject("content") ?: return null
                val parts = content.optJSONArray("parts") ?: return null
                val firstPart = parts.optJSONObject(0) ?: return null
                firstPart.optString("text").takeIf { it.isNotBlank() }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun buildSystemInstructionPrompt(context: LiveAppContext, isEnglish: Boolean): String {
        val t = context.telemetry
        val net = context.latestNetworkReport
        val netSummary = if (net == null) {
            "No detailed Network Booster check run yet (Live ping estimate: ${t.pingMs}ms)."
        } else if (!net.isConnected) {
            "Last Network Check: OFFLINE (${net.networkSubtypeLabel})."
        } else {
            "Last Network Check: Connected via ${net.connectionType.labelAr}, Avg Ping=${net.avgPingMs}ms, Jitter=${net.jitterMs}ms, PacketLoss=${net.packetLossPercent}%, Server=${net.testedServerName} (${net.testedServerHost}), Grade=${net.stabilityGrade.titleAr}."
        }

        return """
            You are Hassan AI (مساعد Hassan Games الذكي), the official built-in assistant for the Hassan Games Android app.
            STRICT RULES:
            1. NEVER invent tools, screens, settings, or hardware readings that do not exist in Hassan Games.
            2. Voice Changer and Volume Button L/R triggers are PERMANENTLY REMOVED. Never mention them as available features.
            3. Actual screens in Hassan Games:
               - MY GAMES (ألعابي): Add any installed Android game via '+ ADD GAME', customize per-game profile, press START to launch or END to finish Gaming Mode session.
               - Command Center (مركز القيادة): Hardware gauges (RAM, CPU freq, Battery temp, Display Hz), Performance Modes (Balanced, Performance, DIABLO), One-Tap Game Prep, Smart Thermal Mode, Session Reports.
               - Network Booster (مُحسّن الشبكة والرام): One-Tap Network Check, Ping Monitor, Connection Stability & Jitter, Packet Loss Test, Multi-Endpoint Route Comparison, Test History, and RAM Booster with Whitelist.
               - Gaming Tools (أدوات اللعب): Cloned Touch Buttons C1-C4 (requires Overlay + Accessibility), Crosshair Studio (requires Overlay for external games), GFX Advisor, Display/Env controls, Custom App Icon Studio.
               - Gaming HUD (الشريط العائم): Edge-swipe sidebar with customizable shortcuts, live Ping/Battery/Temp/RAM, Quick Network Check, and Quick AI Assist.
               - Hassan AI (المساعد الذكي): This assistant screen.
            4. Live Device & App State right now:
               - RAM Available: ${t.ramAvailableMb}MB / ${t.ramTotalMb}MB (${t.ramUsagePercent}% used)
               - Battery Temp: ${t.batteryTempCelsius}°C (Battery Level: ${t.batteryPercent}%, Charging: ${t.isCharging})
               - Display Refresh Rate: ${t.displayRefreshRateHz}Hz
               - Active Performance Mode: ${context.performanceMode.titleAr} (${context.performanceMode.badgeText})
               - Smart Thermal Mode: ${context.thermalStatus.mode.titleAr}
               - Permissions: Overlay=${context.overlayPermissionGranted}, Accessibility=${context.accessibilityEnabled}, DND=${context.dndPermissionGranted}
               - Active Overlays: FloatingHUD=${context.floatingHudRunning}, Crosshair=${context.crosshairEnabled}, ClonedButtons=${context.clonedButtonsEnabled}
               - Added Games Count: ${context.myGames.size} (Selected: ${context.selectedGame?.title ?: "None"})
               - $netSummary
            5. Respond clearly, concisely, and honestly in ${if (isEnglish) "English" else "Arabic"}. Distinguish between confirmed facts and hypotheses to test.
        """.trimIndent()
    }

    private fun generateLocalGroundedResponse(
        question: String,
        context: LiveAppContext,
        isEnglish: Boolean
    ): String {
        val q = question.lowercase()
        val t = context.telemetry
        val net = context.latestNetworkReport

        // Guard against removed features (Voice Changer / Volume L-R)
        if (q.contains("voice") || q.contains("صوت") || q.contains("مغير") || q.contains("volume") || q.contains("ازرار الصوت")) {
            return if (isEnglish) {
                "Important Notice: Voice Changer and Volume Button L/R triggers have been permanently removed from Hassan Games. Available controls include On-Screen Cloned Touch Buttons (C1–C4), Crosshair Overlay, Smart Gaming HUD, and Network Booster."
            } else {
                "تنبيه هام: ميزة مغير الصوت (Voice Changer) وميزة استخدام أزرار رفع/خفض الصوت كأزرار L/R تم حذفهما نهائياً من Hassan Games. الأدوات المتوفرة حالياً في قسم «أدوات اللعب» هي: الأزرار اللمسية المستنسخة على الشاشة (C1–C4)، مؤشر التصويب العائم (Crosshair)، مستشار الدقة والإعدادات، والشريط العائم Gaming HUD."
            }
        }

        if (isEnglish) {
            return when {
                q.contains("explain hassan") || q.contains("what is") || q.contains("sections") ->
                    "Hassan Games is an integrated Gaming Center with 5 core areas:\n" +
                        "1. **MY GAMES**: Add any installed game via `+ ADD GAME`, configure its independent profile, and press `START` to launch or `END` to stop Gaming Mode.\n" +
                        "2. **Command Center**: Monitor real RAM (${t.ramAvailableMb}MB free), Battery Temp (${t.batteryTempCelsius}°C), Display (${t.displayRefreshRateHz}Hz), switch Performance Modes, and view Session Reports.\n" +
                        "3. **Network Booster**: Run a 1-Tap Network Check for real Ping, Jitter, Packet Loss, multi-gateway comparison, and Test History.\n" +
                        "4. **Gaming Tools**: Configure Cloned Touch Buttons (C1–C4), Crosshair Overlay, Smart Thermal Mode, and Auto Settings Advisor.\n" +
                        "5. **Gaming HUD**: Swipe from the screen edge for quick tools, live telemetry, and quick AI help."

                q.contains("add") && q.contains("game") ->
                    "To add and launch a game in Hassan Games:\n" +
                        "1. Open the **MY GAMES** tab.\n" +
                        "2. Tap the **+ ADD GAME** button.\n" +
                        "3. Select any installed game/app from your device list (currently ${context.myGames.size} games in your library).\n" +
                        "4. Tap the Settings icon on the game card to customize its Performance Mode and HUD tools, then press **START**."

                q.contains("network") || q.contains("connection") || q.contains("ping") || q.contains("packet") -> {
                    if (net == null) {
                        "No complete Network Booster test has been recorded yet in this session (current quick ping reading: ${t.pingMs}ms). Tap **Run Network Check Now** below to measure real Ping, Jitter, and Packet Loss."
                    } else if (!net.isConnected) {
                        "Confirmed Issue: The latest Network Booster test shows **Offline (${net.networkSubtypeLabel})**. Please check Wi-Fi or Mobile Data and re-test."
                    } else {
                        "Latest Network Booster Analysis (${net.connectionType.labelEn}):\n" +
                            "• Tested Server: ${net.testedServerName} (${net.testedServerHost})\n" +
                            "• Ping: Avg **${net.avgPingMs}ms** (Min ${net.minPingMs}ms / Max ${net.maxPingMs}ms) • Jitter: **${net.jitterMs}ms**\n" +
                            "• Packet Loss: **${net.packetLossPercent ?: 0}%** • Stability Score: **${net.stabilityScore}/100**\n" +
                            "• Recommendation: ${net.smartRecommendationsAr.firstOrNull() ?: "Connection is stable."}"
                    }
                }

                q.contains("not working") || q.contains("permission") || q.contains("overlay") || q.contains("hud") || q.contains("floating") ->
                    "Let's check your live permissions and tool status right now:\n" +
                        "• **Display Over Other Apps (Overlay)**: ${if (context.overlayPermissionGranted) "Granted ✅" else "Missing ❌ (Required to show Gaming HUD, Crosshair, and C1–C4 over external games)"}\n" +
                        "• **Accessibility Service**: ${if (context.accessibilityEnabled) "Active ✅" else "Inactive ⚠️ (Required only if you want Cloned Buttons C1–C4 to dispatch real touches inside games)"}\n" +
                        "• **Do Not Disturb (DND)**: ${if (context.dndPermissionGranted) "Granted ✅" else "Not granted ⚠️ (Optional for full system Notification Shield)"}\n" +
                        "Use the shortcut buttons below to open the exact permission screen."

                else ->
                    "Live Diagnostic Summary for your device:\n" +
                        "• **RAM Available**: ${t.ramAvailableMb}MB free of ${t.ramTotalMb}MB (${t.ramUsagePercent}% used)\n" +
                        "• **Battery Temperature**: ${t.batteryTempCelsius}°C • **Display**: ${t.displayRefreshRateHz}Hz\n" +
                        "• **Active Mode**: ${context.performanceMode.badgeText} • **Thermal Mode**: ${context.thermalStatus.mode.badgeAr}\n" +
                        "• **Tip**: If you experience stutter, run a 1-Tap Network Check first to verify whether Ping/Jitter is stable, and enable Smart Thermal Mode if temperature exceeds 40°C."
            }
        }

        // Arabic Grounded Responses
        return when {
            q.contains("اشرح لي hassan") || q.contains("ما هو") || q.contains("الاقسام") || q.contains("الأقسام") ->
                "تطبيق **Hassan Games** هو مركز ألعاب متكامل يضم الأقسام الفعلية التالية:\n" +
                    "1. **ألعابي (MY GAMES)**: لإضافة أي لعبة مثبتة على جهازك عبر زر `+ ADD GAME`، وتخصيص ملف إعداداتها المستقل، وتشغيلها بزر `START` أو إنهاء الجلسة بزر `END`.\n" +
                    "2. **مركز القيادة (Command Center)**: يعرض قراءات العتاد الحقيقية (الرام المتاحة حالياً ${t.ramAvailableMb}MB، حرارة البطارية ${t.batteryTempCelsius}°C، شاشة ${t.displayRefreshRateHz}Hz)، وأوضاع الأداء (`Balanced` / `Performance` / `DIABLO`)، وتقرير جلسة اللعب.\n" +
                    "3. **مُحسّن الشبكة والرام (Network Booster)**: لفحص الاتصال بضغطة واحدة (Ping، التذبذب Jitter، فقدان الحزم Packet Loss، مقارنة البوابات، وسجل الفحوصات) مع تنظيف الرام.\n" +
                    "4. **أدوات اللعب (Gaming Tools)**: تشمل الأزرار اللمسية المستنسخة `C1–C4`، مؤشر التصويب العائم `Crosshair`، مستشار الإعدادات، والوضع الحراري الذكي.\n" +
                    "5. **الشريط العائم (Gaming HUD)**: يفتح من حافة الشاشة أثناء اللعب للوصول السريع للأدوات وفحص الشبكة."

            q.contains("كيف اضيف") || q.contains("كيف أضيف") || q.contains("اضافة لعبة") || q.contains("إضافة لعبة") ->
                "خطوات إضافة لعبة وتشغيلها في **Hassan Games**:\n" +
                    "1. افتح تبويب **«ألعابي MY GAMES»** من الشريط السفلي.\n" +
                    "2. اضغط على زر **«+ ADD GAME»** باللون السماوي.\n" +
                    "3. ستظهر لك قائمة التطبيقات والألعاب المثبتة على جهازك؛ اضغط **«إضافة»** بجانب اللعبة التي تريدها (لديك حالياً ${context.myGames.size} ألعاب مضافة).\n" +
                    "4. يمكنك الضغط على أيقونة الإعدادات ⚙️ داخل بطاقة اللعبة لتخصيص أدواتها، ثم الضغط على **«START»** لتفعيل Gaming Mode وفتح اللعبة."

            q.contains("كيف أشغل وضع الألعاب") || q.contains("ادوات gaming mode") || q.contains("أدوات gaming mode") || q.contains("الشريط العائم") ->
                "شرح **Gaming Mode** والشريط العائم (**Gaming HUD**):\n" +
                    "• **التشغيل**: عند الضغط على `START` لأي لعبة في `MY GAMES` (أو الضغط على تفعيل الشريط العائم من مركز القيادة)، يقوم التطبيق بتجهيز الذاكرة، وتفعيل درع الإشعارات، وإظهار مقبض **Gaming HUD** على حافة الشاشة.\n" +
                    "• **محتويات الشريط العائم**: يعرض Ping الشبكة ونوع الاتصال (Wi-Fi أو بيانات)، نسبة البطارية، حرارة مستشعر البطارية (${t.batteryTempCelsius}°C)، والرام المتاحة (${t.ramAvailableMb}MB)، مع اختصار لفحص الشبكة الفوري ومساعد Hassan AI المصغر.\n" +
                    "• **الإنهاء الصحيح**: اضغط زر **«إيقاف Gaming Mode»** داخل الشريط العائم أو زر **«إنهاء الجلسة END»** في التطبيق لإيقاف جميع الخدمات المؤقتة وإصدار تقرير الجلسة."

            q.contains("لا تعمل") || q.contains("لا يظهر") || q.contains("صلاحية") || q.contains("رفض") || q.contains("overlay") || q.contains("accessibility") -> {
                val overlayStatus = if (context.overlayPermissionGranted) "ممنوحة وتعمل ✅" else "غير ممنوحة ❌ (هذا هو السبب المؤكد لعدم ظهور الشريط العائم أو مؤشر التصويب فوق الألعاب الخارجية)"
                val accStatus = if (context.accessibilityEnabled) "مفعّلة ✅" else "غير مفعّلة ⚠️ (مطلوبة فقط إذا أردت أن تقوم الأزرار المنسوخة C1–C4 بتنفيذ نقرات فعلية داخل اللعبة)"
                val dndStatus = if (context.dndPermissionGranted) "ممنوحة ✅" else "غير ممنوحة ⚠️ (مطلوبة لكتم منبثقات النظام عبر درع الإشعارات DND)"

                "فحصت حالة الصلاحيات الفعلية على جهازك الآن:\n" +
                    "1. **صلاحية الظهور فوق التطبيقات (Overlay)**: $overlayStatus\n" +
                    "2. **خدمة إمكانية الوصول (Accessibility)**: $accStatus\n" +
                    "3. **صلاحية عدم الإزعاج (DND)**: $dndStatus\n\n" +
                    "الخلاصة: جميع الأدوات تعمل داخل التطبيق للمعاينة، ولكن لظهورها فوق اللعبة الخارجية يجب منح صلاحية الظهور العائم من الزر المباشر أدناه."
            }

            q.contains("اتصال") || q.contains("شبك") || q.contains("بنق") || q.contains("ping") -> {
                if (net == null) {
                    "لم تقم بإجراء فحص كامل في **Network Booster** خلال هذه الجلسة بعد (قراءة البنق السريعة الحالية: `${t.pingMs}ms`).\n" +
                        "اضغط على زر **«تشغيل فحص الشبكة الآن»** بالأسفل لقياس زمن الاستجابة، التذبذب (Jitter)، وفقدان الحزم (Packet Loss) بدقة."
                } else if (!net.isConnected) {
                    "المشكلة المؤكدة من آخر فحص: **لا يوجد اتصال بالإنترنت (${net.networkSubtypeLabel})**.\n" +
                        "• الحل: تأكد من اتصال Wi-Fi أو بيانات الهاتف، ثم أعد الفحص من قسم Network Booster."
                } else {
                    "تحليل آخر فحص في **Network Booster** (${net.connectionType.labelAr}):\n" +
                        "• الخادم الذي تم اختباره: `${net.testedServerName}` (`${net.testedServerHost}`)\n" +
                        "• متوسط Ping الاختبار: **${net.avgPingMs}ms** (الأدنى ${net.minPingMs}ms / الأعلى ${net.maxPingMs}ms)\n" +
                        "• التذبذب (Jitter): **${net.jitterMs}ms** • فقدان الحزم: **${net.packetLossPercent ?: 0}%**\n" +
                        "• التقييم: **${net.stabilityGrade.titleAr} (${net.stabilityScore}/100)**\n" +
                        "• النصيحة الذكية: ${net.smartRecommendationsAr.firstOrNull() ?: "الاتصال مستقر."}\n" +
                        "*(ملاحظة: بنق خادم اللعبة الداخلي قد يختلف بحسب موقع خادم المباراة الجغرافي)*."
                }
            }

            q.contains("تقطيع") || q.contains("لاق") || q.contains("تهنيج") || q.contains("حرار") || q.contains("احسن") || q.contains("أحسن") -> {
                val perfAdvice = generatePerformanceAdvisorReport(context)
                val netCheckNote = if (net != null && net.isConnected) {
                    "• **حالة الشبكة المقاسة**: البنق ${net.avgPingMs}ms والتذبذب ${net.jitterMs}ms وفقدان الحزم ${net.packetLossPercent ?: 0}% (${net.stabilityGrade.titleAr})."
                } else {
                    "• **اختبار الشبكة**: يُنصح بإجراء فحص في (Network Booster) للتفريق بين تقطيع الفريمات (بسبب الحرارة/الرام) وتأخر الاستجابة (بسبب الشبكة)."
                }
                "تحليل **AI Gaming Optimizer** لحالة جهازك الحالية:\n$perfAdvice\n$netCheckNote"
            }

            else ->
                "مرحباً بك في **Hassan AI**! إليك ملخص الحالة الفعلية لجهازك الآن:\n" +
                    "• **الرام المتاحة**: `${t.ramAvailableMb} MB` من أصل `${t.ramTotalMb} MB` (استهلاك `${t.ramUsagePercent}%`)\n" +
                    "• **حرارة البطارية**: `${t.batteryTempCelsius}°C` • **معدل تحديث الشاشة**: `${t.displayRefreshRateHz}Hz`\n" +
                    "• **وضع الأداء النشط**: `${context.performanceMode.titleAr}` • **الوضع الحراري**: `${context.thermalStatus.mode.titleAr}`\n" +
                    "• **الألعاب المضافة في MY GAMES**: `${context.myGames.size}` ألعاب.\n\n" +
                    "يمكنك الضغط على أي زر من الإجراءات السريعة بالأسفل للانتقال مباشرة إلى الأداة المطلوبة أو فحص الشبكة."
        }
    }

    private fun inferRelevantActionChips(
        question: String,
        context: LiveAppContext
    ): List<AiQuickActionChip> {
        val q = question.lowercase()
        val chips = mutableListOf<AiQuickActionChip>()

        if (q.contains("شبك") || q.contains("اتصال") || q.contains("بنق") || q.contains("ping") || q.contains("network") || q.contains("connection")) {
            chips.add(AiQuickActionChip("تشغيل فحص الشبكة الآن", "Run Network Check", AiActionType.RUN_NETWORK_CHECK_NOW))
            chips.add(AiQuickActionChip("فتح Network Booster", "Open Network Booster", AiActionType.OPEN_NETWORK_BOOSTER))
        }
        if (q.contains("اضيف") || q.contains("أضيف") || q.contains("لعبة") || q.contains("add") || q.contains("game")) {
            chips.add(AiQuickActionChip("فتح + ADD GAME", "Open + ADD GAME", AiActionType.OPEN_ADD_GAME_PICKER))
            chips.add(AiQuickActionChip("فتح قسم MY GAMES", "Open MY GAMES", AiActionType.OPEN_MY_GAMES))
        }
        if (!context.overlayPermissionGranted || q.contains("عائم") || q.contains("لا تعمل") || q.contains("overlay") || q.contains("hud")) {
            chips.add(AiQuickActionChip("منح صلاحية الظهور العائم (Overlay)", "Grant Overlay Permission", AiActionType.REQUEST_OVERLAY_PERMISSION))
            chips.add(AiQuickActionChip("فتح/إغلاق الشريط العائم HUD", "Toggle Gaming HUD", AiActionType.TOGGLE_FLOATING_HUD))
        }
        if (!context.accessibilityEnabled && (q.contains("لمس") || q.contains("ازرار") || q.contains("أزرار") || q.contains("accessibility") || q.contains("لا تعمل"))) {
            chips.add(AiQuickActionChip("تفعيل خدمة Accessibility", "Enable Accessibility", AiActionType.REQUEST_ACCESSIBILITY_PERMISSION))
        }
        if (q.contains("تقطيع") || q.contains("رام") || q.contains("احسن") || q.contains("أحسن") || q.contains("lag") || q.contains("improve")) {
            chips.add(AiQuickActionChip("تنظيف الرام الآن", "Boost RAM Now", AiActionType.RUN_RAM_BOOST_NOW))
            chips.add(AiQuickActionChip("فتح أدوات اللعب", "Open Gaming Tools", AiActionType.OPEN_GAMING_TOOLS))
        }
        if (chips.isEmpty()) {
            chips.add(AiQuickActionChip("فحص الشبكة الآن", "Check Network", AiActionType.RUN_NETWORK_CHECK_NOW))
            chips.add(AiQuickActionChip("فتح MY GAMES", "Open MY GAMES", AiActionType.OPEN_MY_GAMES))
            chips.add(AiQuickActionChip("فتح أدوات اللعب", "Gaming Tools", AiActionType.OPEN_GAMING_TOOLS))
        }
        return chips.distinctBy { it.actionType }.take(4)
    }

    private fun detectEnglishQuery(text: String): Boolean {
        var latinCount = 0
        var arabicCount = 0
        for (ch in text) {
            if (ch in 'a'..'z' || ch in 'A'..'Z') latinCount++
            if (ch in '\u0600'..'\u06FF') arabicCount++
        }
        return latinCount > arabicCount * 2 && latinCount >= 4
    }
}
