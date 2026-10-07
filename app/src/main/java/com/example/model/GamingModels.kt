package com.example.model

import androidx.compose.ui.graphics.Color

enum class PerformanceMode(
    val titleAr: String,
    val subtitleAr: String,
    val badgeText: String,
    val primaryColor: Color,
    val targetFps: Int,
    val clockBoostRatio: Float
) {
    BALANCED(
        titleAr = "الوضع المتوازن",
        subtitleAr = "استقرار حراري وتوفير للطاقة",
        badgeText = "ECO CORE",
        primaryColor = Color(0xFF00E676),
        targetFps = 60,
        clockBoostRatio = 0.72f
    ),
    PERFORMANCE(
        titleAr = "وضع الأداء الفائق",
        subtitleAr = "أولوية قصوى للمعالج والشبكة",
        badgeText = "HYPER BOOST",
        primaryColor = Color(0xFFFF6D00),
        targetFps = 90,
        clockBoostRatio = 0.88f
    ),
    DIABLO(
        titleAr = "الوضع الشيطاني DIABLO",
        subtitleAr = "فتح كامل لترددات CPU/GPU واستجابة لمس 960Hz",
        badgeText = "DIABLO MAX",
        primaryColor = Color(0xFFFF1744),
        targetFps = 120,
        clockBoostRatio = 1.0f
    )
}

enum class ClonedButtonMode(
    val titleAr: String,
    val subtitleAr: String,
    val intervalMs: Long
) {
    SINGLE_TAP(
        titleAr = "نقرة فورية (Single Tap)",
        subtitleAr = "تضغط زر اللعبة الأصلي فور لمس الزر المستنسخ",
        intervalMs = 0L
    ),
    TURBO_HOLD(
        titleAr = "رشاش مستمر (Turbo Hold)",
        subtitleAr = "نقر متكرر فائق السرعة طالما إصبعك يلمس الزر المستنسخ",
        intervalMs = 85L
    ),
    AUTO_LOCK(
        titleAr = "قفل تلقائي (Auto-Lock Toggle)",
        subtitleAr = "لمسة واحدة تبدأ النقر المستمر ولمسة ثانية توقفه",
        intervalMs = 110L
    )
}

data class ClonedTouchButton(
    val id: Int,
    val label: String,
    val actionTitleAr: String,
    val enabled: Boolean,
    val mode: ClonedButtonMode = ClonedButtonMode.SINGLE_TAP,
    val sourceX: Float,
    val sourceY: Float,
    val targetX: Float,
    val targetY: Float,
    val buttonSizeDp: Int = 52,
    val opacity: Float = 0.88f,
    val colorHex: Long = 0xFFFF1744
)

data class ClonedButtonsConfig(
    val enabled: Boolean = false,
    val showConnectionLines: Boolean = true,
    val editPositionsLocked: Boolean = false,
    val hapticFeedback: Boolean = true,
    val buttons: List<ClonedTouchButton> = defaultClonedButtons()
) {
    companion object {
        fun defaultClonedButtons(): List<ClonedTouchButton> = listOf(
            ClonedTouchButton(
                id = 1,
                label = "C1",
                actionTitleAr = "إطلاق نار سريع",
                enabled = true,
                mode = ClonedButtonMode.TURBO_HOLD,
                sourceX = 0.18f,
                sourceY = 0.36f,
                targetX = 0.82f,
                targetY = 0.72f,
                buttonSizeDp = 54,
                opacity = 0.90f,
                colorHex = 0xFFFF1744
            ),
            ClonedTouchButton(
                id = 2,
                label = "C2",
                actionTitleAr = "فتح سكوب / تصويب",
                enabled = true,
                mode = ClonedButtonMode.SINGLE_TAP,
                sourceX = 0.82f,
                sourceY = 0.36f,
                targetX = 0.88f,
                targetY = 0.54f,
                buttonSizeDp = 52,
                opacity = 0.90f,
                colorHex = 0xFF00E5FF
            ),
            ClonedTouchButton(
                id = 3,
                label = "C3",
                actionTitleAr = "ثلج / انحناء تكتيكي",
                enabled = false,
                mode = ClonedButtonMode.SINGLE_TAP,
                sourceX = 0.18f,
                sourceY = 0.54f,
                targetX = 0.74f,
                targetY = 0.82f,
                buttonSizeDp = 48,
                opacity = 0.86f,
                colorHex = 0xFF00E676
            ),
            ClonedTouchButton(
                id = 4,
                label = "C4",
                actionTitleAr = "قفز / مهارة خاصة",
                enabled = false,
                mode = ClonedButtonMode.SINGLE_TAP,
                sourceX = 0.82f,
                sourceY = 0.54f,
                targetX = 0.90f,
                targetY = 0.78f,
                buttonSizeDp = 48,
                opacity = 0.86f,
                colorHex = 0xFFFFEA00
            )
        )
    }
}

enum class CrosshairStyle(
    val titleAr: String,
    val descriptionAr: String
) {
    RED_DOT("نقطة ليزر حمراء", "نقطة دقيقة للتصويب السريع بالرشاشات"),
    TACTICAL_CROSS("صليب تكتيكي", "خطوط متقاطعة مع فجوة رؤية مركزية"),
    SNIPER_CIRCLE("حلقة قنص", "دائرة محيطية مع نقطة مركزية للقناصات"),
    CHEVRON_PRO("سهم احترافي ^", "مؤشر ارتداد علوي للمسافات البعيدة")
}

data class CrosshairConfig(
    val enabled: Boolean = false,
    val style: CrosshairStyle = CrosshairStyle.TACTICAL_CROSS,
    val sizeDp: Float = 28f,
    val strokeWidthDp: Float = 2.2f,
    val opacity: Float = 0.9f,
    val colorHex: Long = 0xFFFF1744,
    val offsetXDp: Float = 0f,
    val offsetYDp: Float = 0f
)

enum class GfxResolutionPreset(
    val label: String,
    val scaleText: String,
    val gpuLoadReductionPercent: Int
) {
    RES_720P("1280×720 (Esports Smooth)", "أعلى ثبات للفريمات", 32),
    RES_900P("1600×900 (Balanced Sharp)", "توازن بين الوضوح والسرعة", 18),
    RES_1080P("1920×1080 (FHD Native)", "دقة كاملة", 8),
    RES_2K("2560×1440 (Ultra HDR)", "أقصى تفاصيل بصرية", 0)
}

enum class TouchSamplingRate(
    val hz: Int,
    val labelAr: String,
    val responseMs: Float
) {
    HZ_240(240, "240Hz قياسي", 4.1f),
    HZ_480(480, "480Hz احترافي", 2.0f),
    HZ_960(960, "960Hz فائق الاستجابة", 1.04f)
}

enum class SmartThermalMode(
    val titleAr: String,
    val subtitleAr: String,
    val badgeAr: String,
    val accentHex: Long
) {
    OFF(
        titleAr = "إيقاف (قياسي)",
        subtitleAr = "معدل تحديث طبيعي دون تدخل حراري",
        badgeAr = "STANDARD",
        accentHex = 0xFF90A4AE
    ),
    AUTO_ADAPTIVE(
        titleAr = "تكيّف حراري ذكي (Auto Smart)",
        subtitleAr = "يراقب الحرارة ويخفف العمليات الخلفية ومعدل التحديث تلقائياً عند السخونة",
        badgeAr = "SMART THERMAL",
        accentHex = 0xFF00E5FF
    ),
    ECO_STABILITY(
        titleAr = "ثبات بارد أقصى (Cool Stability)",
        subtitleAr = "يمنع ارتفاع الحرارة منذ البداية ويقلل استهلاك المعالج والبطارية لأدنى حد",
        badgeAr = "ICE CORE",
        accentHex = 0xFF00E676
    )
}

enum class ThermalStateLevel(
    val labelAr: String,
    val statusColorHex: Long,
    val recommendedIntervalMs: Long
) {
    OPTIMAL("بارد ومثالي", 0xFF00E676, 5000L),
    WARM("دافئ ومستقر", 0xFFFFEA00, 8000L),
    ELEVATED("حرارة مرتفعة — تهدئة نشطة", 0xFFFF9100, 11000L),
    HIGH_LOAD("سخونة عالية — حماية قصوى للأداء", 0xFFFF1744, 15000L)
}

data class SmartThermalStatus(
    val mode: SmartThermalMode = SmartThermalMode.AUTO_ADAPTIVE,
    val thermalLevel: ThermalStateLevel = ThermalStateLevel.OPTIMAL,
    val batteryTempCelsius: Float = 34.5f,
    val systemThermalCode: Int = 0,
    val isCharging: Boolean = false,
    val effectivePollIntervalMs: Long = 5000L,
    val reduceAnimationsActive: Boolean = false,
    val backgroundTrimActive: Boolean = false,
    val activeActionsAr: List<String> = emptyList(),
    val summaryExplanationAr: String = "النظام مستقر حرارياً ولا يوجد حمل إضافي."
)

enum class AdvisorPresetMode(
    val titleAr: String,
    val subtitleAr: String,
    val badgeText: String,
    val accentHex: Long
) {
    BEST_PERFORMANCE(
        titleAr = "أفضل أداء (Best Performance)",
        subtitleAr = "أعلى وأثبت FPS ممكن مع أقل تأخير لمس وحرارة منخفضة",
        badgeText = "MAX FPS",
        accentHex = 0xFFFF1744
    ),
    BALANCED(
        titleAr = "متوازن (Balanced)",
        subtitleAr = "توازن ذكي بين سلاسة الفريمات ووضوح الرؤية واستقرار الحرارة",
        badgeText = "SMART BALANCE",
        accentHex = 0xFF00E5FF
    ),
    BEST_VISUAL_QUALITY(
        titleAr = "أفضل جودة بصرية (Best Visuals)",
        subtitleAr = "أعلى دقة وتفاصيل وإضاءة يدعمها جهازك بأمان",
        badgeText = "ULTRA HD",
        accentHex = 0xFFFFEA00
    )
}

data class AutoSettingsRecommendation(
    val mode: AdvisorPresetMode = AdvisorPresetMode.BALANCED,
    val deviceTierAr: String = "جهاز متوسط الأداء",
    val screenResolutionLabel: String = "1080x2400",
    val expectedFpsText: String = "60 FPS مستقر",
    val graphicsQualityText: String = "Smooth / Balanced (سلسة إلى متوازنة)",
    val shadowsRecommendation: String = "إيقاف (OFF) لتوفير 18% من المعالج الرسومي",
    val effectsRecommendation: String = "منخفضة (Low) لمنع هبوط الفريمات أثناء الاشتباكات",
    val resolutionRecommendation: String = "900p Sharp (توازن مثالي بين الوضوح والسرعة)",
    val textureRecommendation: String = "متوسطة (Medium) لتناسب حجم الرام المتاح",
    val antiAliasingRecommendation: String = "إيقاف (OFF) لتقليل الحرارة واستهلاك البطارية",
    val whyChosenExplanationAr: String = "تم اختيار هذه الإعدادات بناءً على قراءة الرام والمعالج ومعدل تحديث الشاشة الفعلي لجهازك."
)

data class LowEndDeviceConfig(
    val lowEndBoostEnabled: Boolean = false,
    val smartThermalMode: SmartThermalMode = SmartThermalMode.AUTO_ADAPTIVE,
    val thermalGuardEnabled: Boolean = true,
    val disableHeavyShaders: Boolean = false,
    val touchEdgeRejectEnabled: Boolean = false,
    val brightnessLockEnabled: Boolean = false,
    val lockedBrightnessPercent: Int = 82,
    val wiFiPriorityEnabled: Boolean = true,
    val blockHeadsUpNotifications: Boolean = false,
    val ramAutoPurgeThresholdPercent: Int = 80
)

data class HardwareTelemetry(
    val liveFps: Int = 60,
    val frameTimeMs: Float = 16.6f,
    val displayRefreshRateHz: Int = 60,
    val screenWidthPx: Int = 1080,
    val screenHeightPx: Int = 2400,
    val ramUsedMb: Long = 3200,
    val ramTotalMb: Long = 6144,
    val ramAvailableMb: Long = 2944,
    val ramUsagePercent: Int = 52,
    val cpuLoadPercent: Int = 34,
    val cpuFreqMhz: Int = 1800,
    val cpuMaxFreqMhz: Int = 2840,
    val cpuCores: Int = 8,
    val gpuEstLoadPercent: Int = 28,
    val batteryTempCelsius: Float = 35.0f,
    val batteryPercent: Int = 85,
    val batteryVoltageMv: Int = 4120,
    val isCharging: Boolean = false,
    val systemThermalStatus: Int = 0,
    val pingMs: Int = 28,
    val jitterMs: Int = 3,
    val networkStatus: String = "مستقر (Ultra Low Latency)",
    val storageFreeGb: Float = 24.5f,
    val storageTotalGb: Float = 64.0f,
    val cacheEstimatedMb: Float = 42.0f
)

data class BoostProcessItem(
    val packageName: String,
    val appTitle: String,
    val memoryMb: Int,
    val isWhitelisted: Boolean = false,
    val wasCleaned: Boolean = false,
    val categoryAr: String = "خلفية"
)

data class BoostResultReport(
    val timestampMs: Long,
    val freedRamMb: Long,
    val cleanedCacheMb: Float,
    val stoppedProcessesCount: Int,
    val ramBeforePercent: Int,
    val ramAfterPercent: Int,
    val pingBeforeMs: Int,
    val pingAfterMs: Int,
    val summaryMessageAr: String = ""
)

data class GamePrepReport(
    val timestampMs: Long,
    val targetGameTitle: String,
    val ramBeforeMb: Long,
    val ramAfterMb: Long,
    val freedRamMb: Long,
    val cleanedCacheMb: Float,
    val trimmedAppsCount: Int,
    val appliedPerformanceMode: PerformanceMode,
    val appliedThermalMode: SmartThermalMode,
    val notificationShieldActive: Boolean,
    val pingMs: Int,
    val batteryTempCelsius: Float,
    val stepsCompletedAr: List<String>
)

data class InstalledAppCandidate(
    val packageName: String,
    val title: String,
    val genreAr: String,
    val isLikelyGame: Boolean,
    val isAddedToMyGames: Boolean = false,
    val accentHex: Long = 0xFFFF1744,
    val recommendedMode: PerformanceMode = PerformanceMode.PERFORMANCE,
    val recommendedFps: Int = 90
)

data class GameSpaceProfile(
    val id: String,
    val title: String,
    val packageName: String,
    val genreAr: String,
    val recommendedMode: PerformanceMode,
    val recommendedFps: Int,
    val accentHex: Long,
    val isInstalled: Boolean,
    val isUserAdded: Boolean = true,
    val autoCleanRamBeforeLaunch: Boolean = true,
    val notificationShieldEnabled: Boolean = true,
    val crosshairEnabled: Boolean = false,
    val fpsOverlayEnabled: Boolean = true,
    val sidebarOverlayEnabled: Boolean = true,
    val brightnessLockEnabled: Boolean = false,
    val touchGuardEnabled: Boolean = false,
    val wiFiPriorityEnabled: Boolean = true,
    val preferredAdvisorMode: AdvisorPresetMode = AdvisorPresetMode.BEST_PERFORMANCE
)

enum class QuickReconnectState(
    val badgeAr: String,
    val colorHex: Long
) {
    ACTIVE_IN_MEMORY("نشطة بالذاكرة — عودة فورية", 0xFF00E676),
    READY_TO_LAUNCH("مثبتة وجاهزة للتشغيل السريع", 0xFF00E5FF),
    PROFILE_READY_SIMULATION("ملف الإعدادات جاهز للتطبيق", 0xFFFFEA00)
}

data class LastSessionGameInfo(
    val gameId: String,
    val title: String,
    val packageName: String,
    val lastLaunchedTimestampMs: Long,
    val appliedMode: PerformanceMode,
    val reconnectState: QuickReconnectState,
    val statusDetailAr: String
)

data class NotificationShieldState(
    val enabled: Boolean = false,
    val hasDndPermission: Boolean = false,
    val systemDndActive: Boolean = false,
    val statusLabelAr: String = "درع الإشعارات متوقف — الإشعارات تعمل بشكل طبيعي"
)
