package com.example.model

enum class NetworkConnectionType(
    val labelAr: String,
    val labelEn: String
) {
    WIFI("واي فاي (Wi-Fi)", "Wi-Fi"),
    CELLULAR("بيانات الهاتف (Mobile Data)", "Mobile Data"),
    ETHERNET("شبكة سلكية (Ethernet)", "Ethernet"),
    VPN_ACTIVE("اتصال عبر VPN نشط", "Active VPN"),
    OFFLINE("غير متصل بالإنترنت", "Offline"),
    UNKNOWN("شبكة غير محددة", "Unknown Network")
}

enum class NetworkStabilityGrade(
    val titleAr: String,
    val titleEn: String,
    val colorHex: Long
) {
    EXCELLENT("ممتاز ومستقر جداً للعب التنافسي", "Excellent & Stable", 0xFF00E676),
    STABLE("مستقر ومناسب للعب", "Stable for Gaming", 0xFF00E5FF),
    MODERATE_JITTER("تذبذب متوسط في الاستجابة (Jitter)", "Moderate Jitter", 0xFFFFEA00),
    UNSTABLE_SPIKES("تقلبات مفاجئة في Ping (Lag Spikes)", "Latency Spikes Detected", 0xFFFF9100),
    PACKET_LOSS_DETECTED("فقدان حزم مرصود (Packet Loss)", "Packet Loss Detected", 0xFFFF1744),
    OFFLINE("لا يوجد اتصال بالإنترنت", "No Internet Connection", 0xFF90A4AE)
}

data class EndpointProbeResult(
    val endpointName: String,
    val regionLabelAr: String,
    val hostIp: String,
    val port: Int,
    val reachable: Boolean,
    val avgRttMs: Int?,
    val minRttMs: Int?,
    val maxRttMs: Int?,
    val jitterMs: Int?,
    val packetLossPercent: Int,
    val samplesCount: Int,
    val isOptimalCandidate: Boolean = false
)

data class NetworkCheckReport(
    val id: String,
    val timestampMs: Long,
    val connectionType: NetworkConnectionType,
    val networkSubtypeLabel: String,
    val isConnected: Boolean,
    val testedServerName: String,
    val testedServerHost: String,
    val avgPingMs: Int?,
    val minPingMs: Int?,
    val maxPingMs: Int?,
    val jitterMs: Int?,
    val packetLossPercent: Int?,
    val packetLossSupported: Boolean,
    val spikeDetected: Boolean,
    val disconnectsRecorded: Int,
    val stabilityScore: Int,
    val stabilityGrade: NetworkStabilityGrade,
    val pingSamplesMs: List<Int>,
    val detectedIssuesAr: List<String>,
    val smartRecommendationsAr: List<String>,
    val pingDifferenceExplanationAr: String,
    val endpointComparisons: List<EndpointProbeResult>,
    val aiNetworkSummaryAr: String
)

data class RouteOptimizationAssessment(
    val selectedGameTitle: String,
    val selectedGamePackage: String,
    val isGameInstalled: Boolean,
    val testedEndpoints: List<EndpointProbeResult>,
    val bestPublicEndpoint: EndpointProbeResult?,
    val baselinePingMs: Int?,
    val bestEndpointPingMs: Int?,
    val vpnRelayBackendAvailable: Boolean = false,
    val statusHeadlineAr: String,
    val architecturalNoteAr: String
)

enum class HudShortcutTool(
    val titleAr: String,
    val titleEn: String
) {
    NETWORK_CHECK("فحص الشبكة السريع", "Network Check"),
    HASSAN_AI_QUICK("مساعد Hassan AI", "Hassan AI Assist"),
    RAM_BOOST("تنظيف الرام", "RAM Boost"),
    CROSSHAIR("مؤشر التصويب", "Crosshair"),
    FPS_PILL("عداد الإطارات والحرارة", "HUD Pill"),
    CLONED_BUTTONS("الأزرار المنسوخة C1-C4", "Cloned Buttons"),
    NOTIFICATION_SHIELD("درع الإشعارات", "DND Shield"),
    THERMAL_COOL("التبريد الذكي", "Smart Thermal")
}

enum class AiActionType {
    OPEN_MY_GAMES,
    OPEN_ADD_GAME_PICKER,
    OPEN_NETWORK_BOOSTER,
    RUN_NETWORK_CHECK_NOW,
    OPEN_GAMING_TOOLS,
    OPEN_COMMAND_CENTER,
    OPEN_SESSION_REPORTS,
    REQUEST_OVERLAY_PERMISSION,
    REQUEST_ACCESSIBILITY_PERMISSION,
    REQUEST_DND_PERMISSION,
    RUN_RAM_BOOST_NOW,
    TOGGLE_FLOATING_HUD
}

data class AiQuickActionChip(
    val labelAr: String,
    val labelEn: String,
    val actionType: AiActionType
)

data class AiChatMessage(
    val id: String,
    val senderIsUser: Boolean,
    val text: String,
    val timestampMs: Long,
    val sourceBadge: String,
    val suggestedActions: List<AiQuickActionChip> = emptyList(),
    val isOfflineOrFallback: Boolean = false
)

data class AiSuggestionPrompt(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val queryAr: String,
    val queryEn: String
)

data class AiGameSetupAdvice(
    val gameTitle: String,
    val packageName: String,
    val isInstalled: Boolean,
    val canVerifyInGameSettings: Boolean = false,
    val deviceSummaryAr: String,
    val recommendedPerformanceMode: PerformanceMode,
    val recommendedThermalMode: SmartThermalMode,
    val recommendedInGameGraphicsAr: String,
    val recommendedFpsCapAr: String,
    val reasoningAr: String,
    val honestyDisclaimerAr: String
)

data class GamingSessionReport(
    val id: String,
    val gameTitle: String,
    val packageName: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationMinutes: Int,
    val durationFormattedAr: String,
    val avgPingMs: Int?,
    val minPingMs: Int?,
    val maxPingMs: Int?,
    val jitterMs: Int?,
    val stabilityLabelAr: String,
    val disconnectsCount: Int,
    val networkTypeLabelAr: String,
    val batteryTempStartC: Float,
    val batteryTempEndC: Float,
    val ramAvailableStartMb: Long,
    val ramAvailableEndMb: Long,
    val batteryPercentEnd: Int,
    val appliedPerformanceMode: PerformanceMode,
    val sufficientEvidenceForLagCause: Boolean,
    val measuredFactsAr: List<String>,
    val aiNextSessionRecommendationsAr: List<String>
)
