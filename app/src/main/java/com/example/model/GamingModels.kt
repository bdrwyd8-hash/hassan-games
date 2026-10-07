package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MoltenAmber
import com.example.ui.theme.TitaniumWhite

enum class PerformanceMode(
    val id: String,
    val arabicTitle: String,
    val englishBadge: String,
    val subtitle: String,
    val color: Color,
    val targetFanRpm: Int,
    val clockBoostRatio: Float
) {
    ECO(
        id = "ECO",
        arabicTitle = "وضع التوفير الذكي",
        englishBadge = "ECO MODE",
        subtitle = "استهلاك طاقة منخفض وحرارة باردة للألعاب الخفيفة",
        color = MatrixGreen,
        targetFanRpm = 4500,
        clockBoostRatio = 0.72f
    ),
    BALANCE(
        id = "BALANCE",
        arabicTitle = "الوضع المتوازن",
        englishBadge = "BALANCE",
        subtitle = "توازن مثالي بين ثبات الفريمات وحرارة المعالج",
        color = CyberCyan,
        targetFanRpm = 11000,
        clockBoostRatio = 0.85f
    ),
    RISE(
        id = "RISE",
        arabicTitle = "وضع الصعود الهجومي",
        englishBadge = "RISE MODE",
        subtitle = "تقوية المعالج وتنظيف مستمر للرام للألعاب التنافسية",
        color = MoltenAmber,
        targetFanRpm = 16500,
        clockBoostRatio = 0.94f
    ),
    DIABLO(
        id = "DIABLO",
        arabicTitle = "الوضع الشيطاني الخارق",
        englishBadge = "DIABLO MODE",
        subtitle = "أقصى قوة ريد ماجيك و ROG! تفريغ كامل للخلفية + أقصى استجابة لمس وفريمات",
        color = CrimsonRed,
        targetFanRpm = 20000,
        clockBoostRatio = 1.0f
    )
}

enum class TriggerFireMode(
    val id: String,
    val arabicName: String,
    val description: String,
    val badge: String
) {
    SINGLE_TAP(
        id = "SINGLE_TAP",
        arabicName = "ضغطة مفردة (قنص / سكوب)",
        description = "ينفذ نقرة فورية واحدة عند ضغط زر الصوت",
        badge = "1-TAP"
    ),
    RAPID_BURST(
        id = "RAPID_BURST",
        arabicName = "رمي ناري متتالي (Turbo Fire)",
        description = "ينفذ رشاش نقرات سريعة جداً طالما زر الصوت مضغوط",
        badge = "BURST"
    ),
    HOLD_PRESS(
        id = "HOLD_PRESS",
        arabicName = "ضغط مطول مستمر (Hold)",
        description = "يحافظ على اللمس مضغوطاً حتى ترفع إصبعك عن زر الصوت",
        badge = "HOLD"
    ),
    DOUBLE_TAP(
        id = "DOUBLE_TAP",
        arabicName = "نقرة مزدوجة تكتيكية (Double)",
        description = "ينفذ نقرتين متتاليتين بسرعة البرق بضغطة واحدة",
        badge = "2X TAP"
    )
}

data class ShoulderTriggerConfig(
    val enabled: Boolean = true,
    // L1 = Volume Up (زر رفع الصوت)
    val l1XRatio: Float = 0.24f,
    val l1YRatio: Float = 0.36f,
    val l1Mode: TriggerFireMode = TriggerFireMode.SINGLE_TAP,
    val l1BurstRps: Int = 10,
    val l1ActionName: String = "فتح سكوب / تصويب (ADS)",
    // R1 = Volume Down (زر خفض الصوت)
    val r1XRatio: Float = 0.78f,
    val r1YRatio: Float = 0.56f,
    val r1Mode: TriggerFireMode = TriggerFireMode.RAPID_BURST,
    val r1BurstRps: Int = 14,
    val r1ActionName: String = "إطلاق نار (FIRE)",
    // Extra RedMagic trigger mechanics
    val hapticFeedback: Boolean = true,
    val soundEffect: Boolean = false,
    val comboLinkLR: Boolean = false
)

/**
 * Custom Cloned Touch Button (الأزرار المنسوخة القابلة للتحريك)
 * Allows the user to place a movable floating button (buttonXRatio, buttonYRatio) anywhere on screen,
 * which automatically taps an unmovable game button at (targetXRatio, targetYRatio).
 */
data class ClonedTouchButton(
    val id: Int,
    val badge: String,
    val labelAr: String,
    val enabled: Boolean,
    val buttonXRatio: Float,
    val buttonYRatio: Float,
    val targetXRatio: Float,
    val targetYRatio: Float,
    val fireMode: TriggerFireMode = TriggerFireMode.SINGLE_TAP,
    val burstRps: Int = 12,
    val colorHex: Long = 0xFF00F0FF
)

fun defaultClonedTouchButtons(): List<ClonedTouchButton> = listOf(
    ClonedTouchButton(
        id = 1,
        badge = "C1",
        labelAr = "زر منسوخ 1 (قفز / مهارة)",
        enabled = true,
        buttonXRatio = 0.34f,
        buttonYRatio = 0.56f,
        targetXRatio = 0.88f,
        targetYRatio = 0.76f,
        fireMode = TriggerFireMode.SINGLE_TAP,
        colorHex = 0xFF00F0FF
    ),
    ClonedTouchButton(
        id = 2,
        badge = "C2",
        labelAr = "زر منسوخ 2 (إطلاق / تعبئة)",
        enabled = true,
        buttonXRatio = 0.66f,
        buttonYRatio = 0.56f,
        targetXRatio = 0.84f,
        targetYRatio = 0.24f,
        fireMode = TriggerFireMode.RAPID_BURST,
        colorHex = 0xFFFF1E38
    ),
    ClonedTouchButton(
        id = 3,
        badge = "C3",
        labelAr = "زر منسوخ 3 (انبطاح / ثلج)",
        enabled = false,
        buttonXRatio = 0.26f,
        buttonYRatio = 0.72f,
        targetXRatio = 0.90f,
        targetYRatio = 0.86f,
        fireMode = TriggerFireMode.SINGLE_TAP,
        colorHex = 0xFF00E676
    ),
    ClonedTouchButton(
        id = 4,
        badge = "C4",
        labelAr = "زر منسوخ 4 (حقيبة / خريطة)",
        enabled = false,
        buttonXRatio = 0.74f,
        buttonYRatio = 0.72f,
        targetXRatio = 0.12f,
        targetYRatio = 0.22f,
        fireMode = TriggerFireMode.SINGLE_TAP,
        colorHex = 0xFFFFB300
    )
)

data class ClonedButtonsConfig(
    val masterEnabled: Boolean = true,
    val systemOverlayEnabled: Boolean = false,
    val isLockedForPlay: Boolean = false,
    val buttonSizeDp: Float = 52f,
    val buttonOpacity: Float = 0.86f,
    val showLaserLinkInEdit: Boolean = true,
    val buttons: List<ClonedTouchButton> = defaultClonedTouchButtons()
)

enum class CrosshairStyle(
    val id: String,
    val arabicName: String
) {
    RED_DOT("RED_DOT", "نقطة ليزر حمراء (Red Dot)"),
    TACTICAL_CROSS("TACTICAL_CROSS", "تقاطع تكتيكي (Pro Cross)"),
    CIRCLE_DOT("CIRCLE_DOT", "حلقة قنص مركزية (Circle Dot)"),
    SNIPER_CHEVRON("SNIPER_CHEVRON", "سهم القناص (Chevron)"),
    CYBER_DIAMOND("CYBER_DIAMOND", "معين ريد ماجيك (Cyber Diamond)"),
    PREDATOR_TRI("PREDATOR_TRI", "ليزر ثلاثي (Predator Tri)"),
    HOLLOW_RING("HOLLOW_RING", "دائرة مفرغة (Hollow Ring)"),
    PULSE_CORE("PULSE_CORE", "نواة نابضة (Pulse Core)")
}

enum class CrosshairColorOption(
    val id: String,
    val arabicName: String,
    val color: Color,
    val androidColorInt: Int
) {
    CRIMSON("CRIMSON", "أحمر قرمزي", CrimsonRed, 0xFFFF1E38.toInt()),
    CYAN("CYAN", "سماوي ليزر", CyberCyan, 0xFF00F0FF.toInt()),
    GREEN("GREEN", "أخضر ماتريكس", MatrixGreen, 0xFF00E676.toInt()),
    AMBER("AMBER", "أصفر ذهبي", MoltenAmber, 0xFFFFB300.toInt()),
    PURPLE("PURPLE", "بنفسجي نيون", ElectricPurple, 0xFFB388FF.toInt()),
    WHITE("WHITE", "أبيض نقي", TitaniumWhite, 0xFFF5F7FA.toInt())
}

data class CrosshairConfig(
    val enabledInApp: Boolean = true,
    val systemOverlayEnabled: Boolean = false,
    val style: CrosshairStyle = CrosshairStyle.CIRCLE_DOT,
    val colorOption: CrosshairColorOption = CrosshairColorOption.CRIMSON,
    val sizeDp: Float = 34f,
    val strokeWidthDp: Float = 2.2f,
    val opacity: Float = 0.92f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)

enum class ScreenVisionFilter(
    val id: String,
    val arabicName: String,
    val brandOrigin: String,
    val description: String,
    val overlayColor: Color,
    val androidTintHex: Int
) {
    NONE(
        id = "NONE",
        arabicName = "الرؤية القياسية (Standard)",
        brandOrigin = "Default",
        description = "ألوان الشاشة الطبيعية بدون فلاتر إضافية",
        overlayColor = Color.Transparent,
        androidTintHex = 0x00000000
    ),
    NIGHT_HUNTER(
        id = "NIGHT_HUNTER",
        arabicName = "كاشف الظلام (Night Hunter)",
        brandOrigin = "ROG Scout / Black Shark",
        description = "يفتح المناطق المظلمة والزوايا لكشف الأعداء المختبئين بالعشب والغرف",
        overlayColor = Color(0x2400E676),
        androidTintHex = 0x2200E676
    ),
    HDR_VIVID(
        id = "HDR_VIVID",
        arabicName = "تباين فائق (HDR Pro Vivid)",
        brandOrigin = "POCO Game Turbo HDR",
        description = "يعزز وضوح الألوان الباهتة ويبرز حركة الهدف من مسافة بعيدة",
        overlayColor = Color(0x1FFFB300),
        androidTintHex = 0x1EFFB300
    ),
    PREDATOR_THERMAL(
        id = "PREDATOR_THERMAL",
        arabicName = "طيف الصياد (Predator Mode)",
        brandOrigin = "RedMagic Hunter",
        description = "فلتر قرمزي تكتيكي يزيد تركيز العين على منتصف الشاشة والحركة",
        overlayColor = Color(0x22FF1E38),
        androidTintHex = 0x20FF1E38
    ),
    EYE_SHIELD(
        id = "EYE_SHIELD",
        arabicName = "درع حماية العين (Eye Care)",
        brandOrigin = "Samsung Game Booster",
        description = "يخفف الأشعة الزرقاء المجهدة للعين أثناء جلسات اللعب الطويلة ليلاً",
        overlayColor = Color(0x26FF9800),
        androidTintHex = 0x24FF9800
    )
}

enum class AudioRadarPreset(
    val id: String,
    val arabicName: String,
    val description: String,
    val boostDb: Int
) {
    NORMAL("NORMAL", "صوت متوازن قياسي", "الترددات الصوتية الطبيعية للعبة", 0),
    FOOTSTEPS_PRO("FOOTSTEPS_PRO", "رادار تضخيم الخطوات 3D", "يرفع ترددات خطوات الأقدام (2kHz-4kHz) لتحديد مكان العدو بدقة", 8),
    SNIPER_CLARITY("SNIPER_CLARITY", "وضوح الطلقات والارتداد", "يقلل ضجيج الانفجارات ويركز على اتجاه إطلاق النار", 6),
    BASS_SURROUND("BASS_SURROUND", "محيطي سينمائي (7.1 Virtual)", "عمق صوتي عالي للألعاب القصصية والسباقات", 5)
}

enum class VoiceModPreset(
    val id: String,
    val arabicName: String,
    val subtitleAr: String,
    val pitchFactor: Float,
    val speechRate: Float,
    val isRobotModulated: Boolean,
    val toneCode: Int
) {
    FEMALE_NATURAL(
        id = "FEMALE_NATURAL",
        arabicName = "أنثى طبيعي (Female)",
        subtitleAr = "نبرة أنثوية ناعمة وطبيعية وصافية 100% بدون أي تقطيع أو تشويش",
        pitchFactor = 1.28f,
        speechRate = 1.02f,
        isRobotModulated = false,
        toneCode = 32
    ),
    MALE_DEEP(
        id = "MALE_DEEP",
        arabicName = "رجل فخم (Male)",
        subtitleAr = "نبرة رجولية عميقة وواضحة جداً مع تضخيم دافئ للصوت",
        pitchFactor = 0.76f,
        speechRate = 0.98f,
        isRobotModulated = false,
        toneCode = 18
    ),
    SQUIRREL_FUN(
        id = "SQUIRREL_FUN",
        arabicName = "سنجاب (Squirrel)",
        subtitleAr = "صوت سنجاب مرح وسريع مع الحفاظ على وضوح الكلمات بالكامل",
        pitchFactor = 1.54f,
        speechRate = 1.06f,
        isRobotModulated = false,
        toneCode = 35
    ),
    CYBER_ROBOT(
        id = "CYBER_ROBOT",
        arabicName = "روبوت آلي (Robot)",
        subtitleAr = "صوت روبوت مستقبلي متقن بتردد رقمي صافٍ بدون ضجيج",
        pitchFactor = 0.92f,
        speechRate = 0.96f,
        isRobotModulated = true,
        toneCode = 28
    ),
    ORIGINAL(
        id = "ORIGINAL",
        arabicName = "طبيعي صافي (HD Mic)",
        subtitleAr = "صوتك الأصلي مع تفعيل عزل الضجيج وتصفية المايكروفون",
        pitchFactor = 1.0f,
        speechRate = 1.0f,
        isRobotModulated = false,
        toneCode = 24
    )
}

data class EdgeSidebarConfig(
    val enabledInApp: Boolean = true,
    val systemFloatingEnabled: Boolean = false,
    val isRightEdge: Boolean = true,
    val showFpsBadgeOnHandle: Boolean = true,
    val handleOpacity: Float = 0.90f,
    val autoCollapseAfterAction: Boolean = false
)

data class LowEndOptimizerConfig(
    val autoCleanInBackground: Boolean = true,
    val autoCleanIntervalSec: Int = 120,
    val ramThresholdPercent: Int = 82,
    val resolutionScalePreset: String = "720p Esports (موصى به للأجهزة الضعيفة)",
    val touchSamplingRateHz: Int = 480,
    val gpuForce4xMsaaOff: Boolean = true,
    val shadowDownscale: Boolean = true,
    val zeroLagNetworkMode: Boolean = true,
    val blockHeadsUpNotifications: Boolean = true,
    val lockScreenBrightness: Boolean = true,
    val coolingFanSpeedLevel: Int = 3,
    // Multi-brand flagship features (ROG / Black Shark / POCO / Samsung)
    val visionFilter: ScreenVisionFilter = ScreenVisionFilter.NONE,
    val audioRadarPreset: AudioRadarPreset = AudioRadarPreset.NORMAL,
    val voiceModPreset: VoiceModPreset = VoiceModPreset.ORIGINAL,
    val mistouchPrevention: Boolean = true,
    val bypassChargingGuard: Boolean = true,
    val afkBlackScreenSaver: Boolean = false,
    // GG GameBox / Game Space Environment Auto-Tuner
    val customBrightnessLevel: Int = 85,
    val gameMediaVolumePercent: Int = 90,
    val dnsServerPreset: String = "Cloudflare 1.1.1.1 (أسرع استجابة بنج)",
    val autoRestoreSettingsOnExit: Boolean = true,
    // Smart Thermal & Overlays & Notification Shield
    val smartThermalMode: SmartThermalMode = SmartThermalMode.AUTO_ADAPTIVE,
    val notificationShieldEnabled: Boolean = false,
    val fpsOverlayEnabled: Boolean = false,
    val tempOverlayEnabled: Boolean = false,
    val ramOverlayEnabled: Boolean = false,
    val magnifierEnabled: Boolean = false,
    val magnifierZoom: Float = 2.0f,
    val advisorPresetMode: AdvisorPresetMode = AdvisorPresetMode.BALANCED
)

enum class SmartThermalMode(
    val id: String,
    val arabicTitle: String,
    val badge: String,
    val descriptionAr: String
) {
    OFF(
        id = "OFF",
        arabicTitle = "إيقاف (قياسي)",
        badge = "OFF",
        descriptionAr = "عمل الأدوات بمعدل التحديث الطبيعي بدون تدخل تلقائي"
    ),
    AUTO_ADAPTIVE(
        id = "AUTO_ADAPTIVE",
        arabicTitle = "ذكي متكيّف (Auto Smart)",
        badge = "SMART AUTO",
        descriptionAr = "يراقب حرارة البطارية وحالة النظام ويقلل استهلاك الموارد والخلفية تلقائياً عند الحاجة"
    ),
    ECO_STABILITY(
        id = "ECO_STABILITY",
        arabicTitle = "أقصى استقرار وكفاءة (Ultra Light)",
        badge = "MAX STABILITY",
        descriptionAr = "يوقف الحركات الرسومية ويبطئ قراءة الحساسات لأدنى حد للحفاظ على ثبات الجهاز في الجلسات الطويلة"
    )
}

enum class ThermalStateLevel(
    val id: String,
    val arabicLabel: String,
    val englishBadge: String,
    val recommendedIntervalMs: Long,
    val color: Color
) {
    OPTIMAL(
        id = "OPTIMAL",
        arabicLabel = "مستقر ومثالي",
        englishBadge = "OPTIMAL",
        recommendedIntervalMs = 5000L,
        color = MatrixGreen
    ),
    WARM(
        id = "WARM",
        arabicLabel = "دافئ طبيعي للعب",
        englishBadge = "WARM",
        recommendedIntervalMs = 7500L,
        color = CyberCyan
    ),
    ELEVATED(
        id = "ELEVATED",
        arabicLabel = "حمل حراري مرتفع",
        englishBadge = "ELEVATED LOAD",
        recommendedIntervalMs = 11000L,
        color = MoltenAmber
    ),
    HIGH_LOAD(
        id = "HIGH_LOAD",
        arabicLabel = "حمل عالٍ — تفعيل حماية الاستقرار",
        englishBadge = "THERMAL GUARD",
        recommendedIntervalMs = 15000L,
        color = CrimsonRed
    )
}

data class SmartThermalStatus(
    val mode: SmartThermalMode = SmartThermalMode.AUTO_ADAPTIVE,
    val thermalLevel: ThermalStateLevel = ThermalStateLevel.OPTIMAL,
    val batteryTempCelsius: Float = 35.5f,
    val systemThermalCode: Int = 0,
    val isCharging: Boolean = false,
    val effectivePollIntervalMs: Long = 5000L,
    val reduceAnimationsActive: Boolean = false,
    val backgroundTrimActive: Boolean = false,
    val audioDspThrottled: Boolean = false,
    val activeActionsAr: List<String> = emptyList(),
    val summaryExplanationAr: String = "حالة الجهاز مستقرة — استهلاك Hassan Games في الخلفية شبه معدوم (~0%)"
)

enum class AdvisorPresetMode(
    val id: String,
    val englishTitle: String,
    val arabicTitle: String,
    val badgeColor: Color
) {
    BEST_PERFORMANCE(
        id = "BEST_PERFORMANCE",
        englishTitle = "BEST PERFORMANCE",
        arabicTitle = "أفضل أداء وأعلى FPS",
        badgeColor = CrimsonRed
    ),
    BALANCED(
        id = "BALANCED",
        englishTitle = "BALANCED",
        arabicTitle = "توازن السلاسة والجودة",
        badgeColor = CyberCyan
    ),
    BEST_VISUAL_QUALITY(
        id = "BEST_VISUAL_QUALITY",
        englishTitle = "BEST VISUAL QUALITY",
        arabicTitle = "أعلى دقة وجودة بصرية",
        badgeColor = MatrixGreen
    )
}

data class AutoSettingsRecommendation(
    val mode: AdvisorPresetMode = AdvisorPresetMode.BALANCED,
    val deviceTierAr: String = "فئة متوسطة قوية (Mid-High Tier)",
    val screenResolutionLabel: String = "1080x2400",
    val expectedFpsText: String = "60 FPS ثابت ومستقر",
    val graphicsQualityText: String = "Smooth / Balanced (سلسة - متوازنة)",
    val shadowsRecommendation: String = "إيقاف (Disabled) — يوفر ~18% من حمل المعالج الرسومي GPU",
    val effectsRecommendation: String = "منخفضة (Low) — لمنع هبوط الفريمات أثناء الدخان والانفجارات",
    val resolutionRecommendation: String = "720p Esports / 900p — استجابة لمس أسرع وحرارة أقل",
    val textureRecommendation: String = "متوسطة إلى عالية (Medium-High) — بناءً على الرام المتاح حالياً",
    val antiAliasingRecommendation: String = "إيقاف 4x MSAA — يقلل استهلاك البطارية والحرارة بنسبة كبيرة",
    val whyChosenExplanationAr: String = "تم بناء هذه التوصيات بناءً على مواصفات جهازك الفعلية وحالة الذاكرة والحرارة الحالية."
)

enum class PrepStepState {
    PENDING,
    RUNNING,
    COMPLETED
}

data class OneTapPrepStep(
    val id: String,
    val titleAr: String,
    val detailAr: String,
    val state: PrepStepState = PrepStepState.PENDING
)

enum class OneTapPrepOverallState {
    IDLE,
    PREPARING,
    GAME_READY,
    BOOST_READY
}

data class OneTapGamePrepStatus(
    val overallState: OneTapPrepOverallState = OneTapPrepOverallState.IDLE,
    val badgeText: String = "READY TO PREPARE",
    val targetGameTitle: String = "جميع الألعاب (الوضع العام)",
    val steps: List<OneTapPrepStep> = emptyList(),
    val freedRamMb: Long = 0L,
    val availableRamMb: Long = 2048L,
    val thermalStateLabel: String = "مستقر",
    val appliedProfileTitle: String = "الوضع المتوازن",
    val timestampMs: Long = 0L
)

enum class QuickReconnectState(
    val id: String,
    val badgeAr: String,
    val descriptionAr: String,
    val actionButtonAr: String
) {
    NO_SESSION(
        id = "NO_SESSION",
        badgeAr = "اختر لعبة للبدء",
        descriptionAr = "اختر أي لعبة من قسم البروفايلات لتفعيل العودة السريعة بضغطة واحدة",
        actionButtonAr = "اختيار لعبة"
    ),
    IN_BACKGROUND_READY(
        id = "IN_BACKGROUND_READY",
        badgeAr = "مفتوحة بالخلفية • جاهزة فوراً",
        descriptionAr = "اللعبة ما زالت نشطة في الذاكرة — اضغط للعودة إليها فوراً بدون إعادة تحميل",
        actionButtonAr = "⚡ عودة فورية للعبة الآن"
    ),
    CLOSED_NEEDS_RELAUNCH(
        id = "CLOSED_NEEDS_RELAUNCH",
        badgeAr = "تم إغلاقها • جاهزة لإعادة الفتح",
        descriptionAr = "سيتم تطبيق بروفايل اللعبة المخصص وإعادة تشغيلها فوراً",
        actionButtonAr = "🚀 إعادة فتح اللعبة مع البروفايل"
    ),
    PROFILE_READY_SIMULATION(
        id = "PROFILE_READY_SIMULATION",
        badgeAr = "البروفايل مطبّق ونشط",
        descriptionAr = "جميع إعدادات البروفايل مفعّلة الآن — يمكنك فتح اللعبة أو تجربة الأدوات",
        actionButtonAr = "🎮 استئناف جلسة البروفايل"
    )
}

data class ActiveGameSession(
    val profileId: String = "",
    val gameTitle: String = "",
    val packageName: String = "",
    val startedAtMs: Long = 0L,
    val isInstalledOnDevice: Boolean = false,
    val reconnectState: QuickReconnectState = QuickReconnectState.NO_SESSION
)

data class NotificationShieldState(
    val enabled: Boolean = false,
    val hasDndPolicyPermission: Boolean = false,
    val systemDndActive: Boolean = false,
    val notificationVolumeMuted: Boolean = false,
    val statusLabelAr: String = "متوقف (الإشعارات طبيعية)"
)

data class HardwareTelemetry(
    val liveFps: Int = 60,
    val frameTimeMs: Float = 16.6f,
    val displayRefreshRateHz: Int = 60,
    val screenWidthPx: Int = 1080,
    val screenHeightPx: Int = 2400,
    val ramUsedMb: Long = 2048,
    val ramTotalMb: Long = 4096,
    val ramAvailableMb: Long = 2048,
    val ramUsagePercent: Int = 50,
    val cpuLoadPercent: Int = 32,
    val cpuFreqMhz: Int = 1800,
    val cpuMaxFreqMhz: Int = 2400,
    val cpuCores: Int = 8,
    val gpuEstLoadPercent: Int = 28,
    val batteryTempCelsius: Float = 36.5f,
    val batteryPercent: Int = 85,
    val batteryVoltageMv: Int = 4120,
    val isCharging: Boolean = false,
    val systemThermalStatus: Int = 0,
    val pingMs: Int = 28,
    val jitterMs: Int = 4,
    val networkStatus: String = "مستقر (Ultra Low Latency)",
    val storageFreeGb: Float = 18.4f,
    val storageTotalGb: Float = 64.0f,
    val cacheEstimatedMb: Float = 142.5f
)

data class InstalledAppProcess(
    val packageName: String,
    val appName: String,
    val estimatedRamMb: Int,
    val cacheSizeMb: Float,
    val isSystemApp: Boolean,
    val isWhitelisted: Boolean = false,
    val isStopped: Boolean = false,
    val categoryTag: String = "خلفية"
)

data class BoostResult(
    val timestampMillis: Long,
    val freedRamMb: Long,
    val cleanedCacheMb: Float,
    val stoppedAppsCount: Int,
    val stoppedAppNames: List<String>,
    val estimatedFpsBoost: Int,
    val coolingDropCelsius: Float,
    val isAutoBoost: Boolean = false
)

/**
 * Full Per-Game Profile (نظام البروفايلات المخصصة لكل لعبة)
 * Stores every customizable gaming tool & mode per game, persisted in DataStore.
 */
data class GameSpaceProfile(
    val id: String,
    val title: String,
    val packageName: String,
    val genreAr: String,
    val recommendedMode: PerformanceMode,
    val l1ActionAr: String,
    val r1ActionAr: String,
    val l1X: Float,
    val l1Y: Float,
    val r1X: Float,
    val r1Y: Float,
    val targetFps: Int,
    val isInstalledOnDevice: Boolean = false,
    val accentHex: Long = 0xFFFF1E38,
    // Per-Game Customizable Tool States
    val smartThermalMode: SmartThermalMode = SmartThermalMode.AUTO_ADAPTIVE,
    val notificationShieldEnabled: Boolean = true,
    val magnifierEnabled: Boolean = false,
    val magnifierZoom: Float = 2.0f,
    val crosshairEnabled: Boolean = true,
    val crosshairStyle: CrosshairStyle = CrosshairStyle.CIRCLE_DOT,
    val touchProtectionEnabled: Boolean = true,
    val gamingSidebarEnabled: Boolean = true,
    val fpsOverlayEnabled: Boolean = true,
    val tempOverlayEnabled: Boolean = false,
    val ramOverlayEnabled: Boolean = false,
    val triggersEnabled: Boolean = true,
    val clonedButtonsEnabled: Boolean = false,
    val advisorMode: AdvisorPresetMode = AdvisorPresetMode.BALANCED,
    val isCustomAdded: Boolean = false
)
