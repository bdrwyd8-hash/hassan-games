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
    val soundEffect: Boolean = true,
    val comboLinkLR: Boolean = false
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
    val pitchFactor: Float,
    val toneCode: Int
) {
    ORIGINAL("ORIGINAL", "صوت طبيعي", 1.0f, 24),
    CYBER_ROBOT("CYBER_ROBOT", "آلي سايبر (Robot)", 0.82f, 28),
    GIANT_COMMANDER("GIANT_COMMANDER", "قائد عملاق (Deep)", 0.65f, 18),
    RADIO_TACTICAL("RADIO_TACTICAL", "لاسلكي عسكري (Radio)", 1.05f, 44),
    HELIUM_CARTOON("HELIUM_CARTOON", "صوت كرتوني (Helium)", 1.45f, 35)
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
    val autoCleanIntervalSec: Int = 45,
    val ramThresholdPercent: Int = 75,
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
    val audioRadarPreset: AudioRadarPreset = AudioRadarPreset.FOOTSTEPS_PRO,
    val voiceModPreset: VoiceModPreset = VoiceModPreset.ORIGINAL,
    val mistouchPrevention: Boolean = true,
    val bypassChargingGuard: Boolean = true,
    val afkBlackScreenSaver: Boolean = false
)

data class HardwareTelemetry(
    val liveFps: Int = 60,
    val frameTimeMs: Float = 16.6f,
    val displayRefreshRateHz: Int = 60,
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
    val accentHex: Long = 0xFFFF1E38
)
