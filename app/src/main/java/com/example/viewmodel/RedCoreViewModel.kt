package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HardwareTelemetryEngine
import com.example.data.RedCorePreferencesRepository
import com.example.data.SystemBoosterManager
import com.example.model.AudioRadarPreset
import com.example.model.BoostResult
import com.example.model.CrosshairColorOption
import com.example.model.CrosshairConfig
import com.example.model.CrosshairStyle
import com.example.model.EdgeSidebarConfig
import com.example.model.GameSpaceProfile
import com.example.model.HardwareTelemetry
import com.example.model.InstalledAppProcess
import com.example.model.LowEndOptimizerConfig
import com.example.model.PerformanceMode
import com.example.model.ScreenVisionFilter
import com.example.model.ShoulderTriggerConfig
import com.example.model.TriggerFireMode
import com.example.model.VoiceModPreset
import com.example.service.TriggerButtonType
import com.example.service.TriggerEventBus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RedCoreTab(val route: String, val titleAr: String) {
    COMMAND_CENTER("command_center", "مركز القيادة"),
    SHOULDER_TRIGGERS("shoulder_triggers", "أزرار L/R"),
    RAM_BOOSTER("ram_booster", "مُقوّي الرام"),
    ARSENAL_GFX("arsenal_gfx", "الترسانة"),
    GAME_SPACE("game_space", "الألعاب")
}

class RedCoreViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = RedCorePreferencesRepository(application.applicationContext)
    private val telemetryEngine = HardwareTelemetryEngine(application.applicationContext)
    private val boosterManager = SystemBoosterManager(application.applicationContext)

    private val _selectedTab = MutableStateFlow(RedCoreTab.COMMAND_CENTER)
    val selectedTab: StateFlow<RedCoreTab> = _selectedTab.asStateFlow()

    private val _performanceMode = MutableStateFlow(PerformanceMode.DIABLO)
    val performanceMode: StateFlow<PerformanceMode> = _performanceMode.asStateFlow()

    private val _triggerConfig = MutableStateFlow(ShoulderTriggerConfig())
    val triggerConfig: StateFlow<ShoulderTriggerConfig> = _triggerConfig.asStateFlow()

    private val _crosshairConfig = MutableStateFlow(CrosshairConfig())
    val crosshairConfig: StateFlow<CrosshairConfig> = _crosshairConfig.asStateFlow()

    private val _edgeSidebarConfig = MutableStateFlow(EdgeSidebarConfig())
    val edgeSidebarConfig: StateFlow<EdgeSidebarConfig> = _edgeSidebarConfig.asStateFlow()

    private val _lowEndConfig = MutableStateFlow(LowEndOptimizerConfig())
    val lowEndConfig: StateFlow<LowEndOptimizerConfig> = _lowEndConfig.asStateFlow()

    private val _telemetry = MutableStateFlow(HardwareTelemetry())
    val telemetry: StateFlow<HardwareTelemetry> = _telemetry.asStateFlow()

    private val _backgroundApps = MutableStateFlow<List<InstalledAppProcess>>(emptyList())
    val backgroundApps: StateFlow<List<InstalledAppProcess>> = _backgroundApps.asStateFlow()

    private val _whitelistedPackages = MutableStateFlow<Set<String>>(emptySet())
    val whitelistedPackages: StateFlow<Set<String>> = _whitelistedPackages.asStateFlow()

    private val _gameCatalog = MutableStateFlow<List<GameSpaceProfile>>(emptyList())
    val gameCatalog: StateFlow<List<GameSpaceProfile>> = _gameCatalog.asStateFlow()

    private val _isBoosting = MutableStateFlow(false)
    val isBoosting: StateFlow<Boolean> = _isBoosting.asStateFlow()

    private val _boostStageText = MutableStateFlow("")
    val boostStageText: StateFlow<String> = _boostStageText.asStateFlow()

    private val _boostProgress = MutableStateFlow(0f)
    val boostProgress: StateFlow<Float> = _boostProgress.asStateFlow()

    private val _lastBoostResult = MutableStateFlow<BoostResult?>(null)
    val lastBoostResult: StateFlow<BoostResult?> = _lastBoostResult.asStateFlow()

    private val _boostHistory = MutableStateFlow<List<BoostResult>>(emptyList())
    val boostHistory: StateFlow<List<BoostResult>> = _boostHistory.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _activeGameProfile = MutableStateFlow<GameSpaceProfile?>(null)
    val activeGameProfile: StateFlow<GameSpaceProfile?> = _activeGameProfile.asStateFlow()

    private val _canDrawOverlays = MutableStateFlow(boosterManager.canDrawSystemOverlays())
    val canDrawOverlays: StateFlow<Boolean> = _canDrawOverlays.asStateFlow()

    private var l1InAppBurstJob: Job? = null
    private var r1InAppBurstJob: Job? = null
    private var l1KeyHeldDown = false
    private var r1KeyHeldDown = false

    init {
        telemetryEngine.startFrameMonitor()
        observePreferences()
        observeOverlayRequests()
        startTelemetryLoop()
        startAutoBackgroundCleanerLoop()
        refreshInstalledAppsAndGames()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            prefsRepo.performanceModeFlow.collectLatest { mode ->
                _performanceMode.value = mode
                TriggerEventBus.updatePerformanceMode(mode)
            }
        }
        viewModelScope.launch {
            prefsRepo.triggerConfigFlow.collectLatest { config ->
                _triggerConfig.value = config
                TriggerEventBus.updateTriggerConfig(config)
            }
        }
        viewModelScope.launch {
            prefsRepo.crosshairConfigFlow.collectLatest { config ->
                _crosshairConfig.value = config
                TriggerEventBus.updateCrosshairConfig(config)
            }
        }
        viewModelScope.launch {
            prefsRepo.edgeSidebarConfigFlow.collectLatest { config ->
                _edgeSidebarConfig.value = config
                TriggerEventBus.updateEdgeSidebarConfig(config)
            }
        }
        viewModelScope.launch {
            prefsRepo.lowEndOptimizerFlow.collectLatest { config ->
                _lowEndConfig.value = config
                TriggerEventBus.updateLowEndConfig(config)
            }
        }
        viewModelScope.launch {
            prefsRepo.whitelistedPackagesFlow.collectLatest { set ->
                _whitelistedPackages.value = set
                _backgroundApps.value = _backgroundApps.value.map { app ->
                    app.copy(isWhitelisted = set.contains(app.packageName))
                }
            }
        }
    }

    private fun observeOverlayRequests() {
        viewModelScope.launch {
            TriggerEventBus.quickBoostRequests.collectLatest {
                runSuperBoostNow()
            }
        }
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            var tick = 0
            while (isActive) {
                val shouldPing = (tick % 4 == 0)
                val snapshot = telemetryEngine.sampleTelemetry(
                    activeMode = _performanceMode.value,
                    measureNetworkPing = shouldPing
                )
                _telemetry.value = snapshot
                TriggerEventBus.updateTelemetry(snapshot)
                _canDrawOverlays.value = boosterManager.canDrawSystemOverlays()
                tick++
                delay(1500L)
            }
        }
    }

    private fun startAutoBackgroundCleanerLoop() {
        viewModelScope.launch {
            while (isActive) {
                val cfg = _lowEndConfig.value
                val waitSec = cfg.autoCleanIntervalSec.coerceIn(20, 180)
                delay(waitSec * 1000L)
                if (cfg.autoCleanInBackground && !_isBoosting.value) {
                    val currentRamPct = _telemetry.value.ramUsagePercent
                    if (currentRamPct >= cfg.ramThresholdPercent || _telemetry.value.cacheEstimatedMb > 45f) {
                        val result = boosterManager.executeSuperBoost(
                            apps = _backgroundApps.value,
                            whitelistedPackages = _whitelistedPackages.value,
                            isAutoBoost = true
                        )
                        telemetryEngine.notifyCacheCleaned(result.cleanedCacheMb)
                        _lastBoostResult.value = result
                        _boostHistory.value = (listOf(result) + _boostHistory.value).take(10)
                        refreshInstalledAppsAndGames()
                    }
                }
            }
        }
    }

    fun selectTab(tab: RedCoreTab) {
        _selectedTab.value = tab
    }

    fun dismissBanner() {
        _statusBannerMessage.value = null
    }

    fun showBanner(msg: String) {
        _statusBannerMessage.value = msg
    }

    fun setInAppSidebarExpanded(expanded: Boolean) {
        TriggerEventBus.setInAppSidebarOpen(expanded)
    }

    fun setPerformanceMode(mode: PerformanceMode) {
        viewModelScope.launch {
            _performanceMode.value = mode
            TriggerEventBus.updatePerformanceMode(mode)
            prefsRepo.savePerformanceMode(mode)
            boosterManager.playTacticalFeedback(isMajorBoost = mode == PerformanceMode.DIABLO)
            showBanner("تم تفعيل ${mode.arabicTitle} (${mode.englishBadge}) بنجاح!")
        }
    }

    fun refreshInstalledAppsAndGames() {
        viewModelScope.launch {
            val whitelist = prefsRepo.whitelistedPackagesFlow.first()
            val apps = boosterManager.scanBackgroundApps(whitelist)
            _backgroundApps.value = apps
            _gameCatalog.value = boosterManager.buildGameSpaceCatalog()
        }
    }

    fun runSuperBoostNow(onComplete: (() -> Unit)? = null) {
        if (_isBoosting.value) return
        viewModelScope.launch {
            _isBoosting.value = true
            _boostProgress.value = 0.12f
            _boostStageText.value = "جاري فحص العمليات النشطة بالخلفية..."
            delay(320L)

            _boostProgress.value = 0.42f
            _boostStageText.value = "إيقاف البرامج المستهلكة للرام والمعالج..."
            delay(360L)

            _boostProgress.value = 0.76f
            _boostStageText.value = "مسح ذاكرة التخزين المؤقت (Cache) وتفريغ الرام..."
            val result = boosterManager.executeSuperBoost(
                apps = _backgroundApps.value,
                whitelistedPackages = _whitelistedPackages.value,
                isAutoBoost = false
            )
            telemetryEngine.notifyCacheCleaned(result.cleanedCacheMb)
            delay(320L)

            _boostProgress.value = 1.0f
            _boostStageText.value = "اكتملت التقوية القصوى! تم تحرير ${result.freedRamMb} MB"
            _lastBoostResult.value = result
            _boostHistory.value = (listOf(result) + _boostHistory.value).take(10)

            refreshInstalledAppsAndGames()
            val updatedSnap = telemetryEngine.sampleTelemetry(_performanceMode.value, measureNetworkPing = false)
            _telemetry.value = updatedSnap
            TriggerEventBus.updateTelemetry(updatedSnap)

            delay(450L)
            _isBoosting.value = false
            showBanner("تم مسح ${result.cleanedCacheMb} MB كاش وإيقاف ${result.stoppedAppsCount} تطبيقات بالخلفية (+${result.estimatedFpsBoost} FPS)")
            onComplete?.invoke()
        }
    }

    fun stopSingleBackgroundApp(app: InstalledAppProcess) {
        viewModelScope.launch {
            boosterManager.stopSingleAppBackground(app.packageName)
            telemetryEngine.notifyCacheCleaned(app.cacheSizeMb)
            _backgroundApps.value = _backgroundApps.value.map {
                if (it.packageName == app.packageName) {
                    it.copy(isStopped = true, estimatedRamMb = 0, cacheSizeMb = 0f)
                } else {
                    it
                }
            }
            showBanner("تم إيقاف ${app.appName} بالخلفية ومسح الكاش المؤقت الخاص به")
        }
    }

    fun toggleAppWhitelist(packageName: String) {
        viewModelScope.launch {
            prefsRepo.toggleWhitelistPackage(packageName)
        }
    }

    fun openSystemAppDetails(packageName: String) {
        boosterManager.openAppDetailsSettings(packageName)
    }

    fun openAccessibilitySettings() {
        boosterManager.openAccessibilitySettings()
    }

    fun activateOrToggleSystemFloatingSidebar() {
        val hasPerm = boosterManager.canDrawSystemOverlays()
        _canDrawOverlays.value = hasPerm
        if (!hasPerm) {
            showBanner("من فضلك فعّل إذن 'الظهور فوق التطبيقات الأخرى' ليظهر الشريط الجانبي العائم داخل جميع الألعاب")
            boosterManager.openOverlayPermissionSettings()
            return
        }

        val currentlyRunning = TriggerEventBus.isOverlayServiceRunning.value
        val nextState = !currentlyRunning
        updateEdgeSidebarConfig { it.copy(systemFloatingEnabled = nextState) }
        val started = boosterManager.startOrStopFloatingSidebarService(nextState)
        if (started) {
            showBanner("تم تشغيل الشريط الجانبي العائم فوق جميع التطبيقات والألعاب! اسحب من طرف الشاشة في أي وقت")
        } else {
            showBanner("تم إيقاف الشريط العائم الخارجي (الشريط الداخلي لا يزال متاحاً)")
        }
    }

    fun updateTriggerConfig(transform: (ShoulderTriggerConfig) -> ShoulderTriggerConfig) {
        viewModelScope.launch {
            val updated = transform(_triggerConfig.value)
            _triggerConfig.value = updated
            TriggerEventBus.updateTriggerConfig(updated)
            prefsRepo.saveTriggerConfig(updated)
        }
    }

    fun handleInAppVolumeTrigger(button: TriggerButtonType, isDown: Boolean): Boolean {
        val cfg = _triggerConfig.value
        if (!cfg.enabled) return false

        if (TriggerEventBus.isAccessibilityServiceRunning.value) {
            return true
        }

        if (button == TriggerButtonType.L1_VOLUME_UP) {
            if (isDown) {
                if (!l1KeyHeldDown) {
                    l1KeyHeldDown = true
                    TriggerEventBus.onTriggerKeyStateChanged(TriggerButtonType.L1_VOLUME_UP, true)
                    if (cfg.hapticFeedback || cfg.soundEffect) {
                        boosterManager.playTacticalFeedback(isMajorBoost = false)
                    }
                    startInAppTriggerLoop(TriggerButtonType.L1_VOLUME_UP, cfg.l1Mode, cfg.l1BurstRps)
                }
            } else {
                l1KeyHeldDown = false
                l1InAppBurstJob?.cancel()
                TriggerEventBus.onTriggerKeyStateChanged(TriggerButtonType.L1_VOLUME_UP, false)
            }
            return true
        } else {
            if (isDown) {
                if (!r1KeyHeldDown) {
                    r1KeyHeldDown = true
                    TriggerEventBus.onTriggerKeyStateChanged(TriggerButtonType.R1_VOLUME_DOWN, true)
                    if (cfg.hapticFeedback || cfg.soundEffect) {
                        boosterManager.playTacticalFeedback(isMajorBoost = false)
                    }
                    startInAppTriggerLoop(TriggerButtonType.R1_VOLUME_DOWN, cfg.r1Mode, cfg.r1BurstRps)
                }
            } else {
                r1KeyHeldDown = false
                r1InAppBurstJob?.cancel()
                TriggerEventBus.onTriggerKeyStateChanged(TriggerButtonType.R1_VOLUME_DOWN, false)
            }
            return true
        }
    }

    private fun startInAppTriggerLoop(
        button: TriggerButtonType,
        mode: TriggerFireMode,
        burstRps: Int
    ) {
        when (mode) {
            TriggerFireMode.SINGLE_TAP, TriggerFireMode.HOLD_PRESS -> {
            }
            TriggerFireMode.DOUBLE_TAP -> {
                viewModelScope.launch {
                    delay(70L)
                    TriggerEventBus.emitBurstPulse(button)
                }
            }
            TriggerFireMode.RAPID_BURST -> {
                val safeRps = burstRps.coerceIn(4, 20)
                val intervalMs = (1000L / safeRps).coerceAtLeast(45L)
                val job = viewModelScope.launch {
                    while (isActive) {
                        delay(intervalMs)
                        if (isActive) {
                            TriggerEventBus.emitBurstPulse(button)
                        }
                    }
                }
                if (button == TriggerButtonType.L1_VOLUME_UP) {
                    l1InAppBurstJob?.cancel()
                    l1InAppBurstJob = job
                } else {
                    r1InAppBurstJob?.cancel()
                    r1InAppBurstJob = job
                }
            }
        }
    }

    fun updateCrosshairConfig(transform: (CrosshairConfig) -> CrosshairConfig) {
        viewModelScope.launch {
            val updated = transform(_crosshairConfig.value)
            _crosshairConfig.value = updated
            TriggerEventBus.updateCrosshairConfig(updated)
            prefsRepo.saveCrosshairConfig(updated)
        }
    }

    fun updateEdgeSidebarConfig(transform: (EdgeSidebarConfig) -> EdgeSidebarConfig) {
        viewModelScope.launch {
            val updated = transform(_edgeSidebarConfig.value)
            _edgeSidebarConfig.value = updated
            TriggerEventBus.updateEdgeSidebarConfig(updated)
            prefsRepo.saveEdgeSidebarConfig(updated)
        }
    }

    fun updateLowEndConfig(transform: (LowEndOptimizerConfig) -> LowEndOptimizerConfig) {
        viewModelScope.launch {
            val updated = transform(_lowEndConfig.value)
            _lowEndConfig.value = updated
            TriggerEventBus.updateLowEndConfig(updated)
            prefsRepo.saveLowEndOptimizerConfig(updated)
        }
    }

    fun selectAudioRadarPreset(preset: AudioRadarPreset) {
        boosterManager.applyAudioRadarPreset(preset)
        updateLowEndConfig { it.copy(audioRadarPreset = preset) }
        showBanner("تم تفعيل الصوت التكتيكي: ${preset.arabicName}")
    }

    fun selectVoiceModPreset(preset: VoiceModPreset) {
        boosterManager.playVoiceChangerSample(preset)
        updateLowEndConfig { it.copy(voiceModPreset = preset) }
        showBanner("تم تفعيل فلتر مغير الصوت: ${preset.arabicName}")
    }

    fun cycleVisionFilter() {
        val entries = ScreenVisionFilter.entries
        val next = entries[(_lowEndConfig.value.visionFilter.ordinal + 1) % entries.size]
        updateLowEndConfig { it.copy(visionFilter = next) }
        showBanner("فلتر الرؤية: ${next.arabicName}")
    }

    fun cycleAudioRadar() {
        val entries = AudioRadarPreset.entries
        val next = entries[(_lowEndConfig.value.audioRadarPreset.ordinal + 1) % entries.size]
        selectAudioRadarPreset(next)
    }

    fun testLivePingNow() {
        viewModelScope.launch {
            val (ping, jitter) = telemetryEngine.forceMeasureNetworkPing()
            val updated = _telemetry.value.copy(
                pingMs = ping,
                jitterMs = jitter,
                networkStatus = if (ping <= 40) "مثالي للألعاب (${ping}ms Ultra)" else "مستقر (${ping}ms)"
            )
            _telemetry.value = updated
            TriggerEventBus.updateTelemetry(updated)
            showBanner("تم تحديث مسار الشبكة! البينج الحالي: ${ping}ms (التذبذب: ${jitter}ms)")
        }
    }

    fun boostAndLaunchGame(profile: GameSpaceProfile) {
        _activeGameProfile.value = profile
        setPerformanceMode(profile.recommendedMode)
        updateTriggerConfig {
            it.copy(
                enabled = true,
                l1XRatio = profile.l1X,
                l1YRatio = profile.l1Y,
                l1ActionName = profile.l1ActionAr,
                r1XRatio = profile.r1X,
                r1YRatio = profile.r1Y,
                r1ActionName = profile.r1ActionAr
            )
        }
        runSuperBoostNow {
            val launched = boosterManager.launchPackageOrOpenSettings(profile.packageName)
            if (!launched) {
                _selectedTab.value = RedCoreTab.SHOULDER_TRIGGERS
                showBanner("تم تطبيق إعدادات ${profile.title} وتفريغ الرام! جرب أزرار L1/R1 الآن في ميدان التدريب")
            }
        }
    }

    override fun onCleared() {
        telemetryEngine.stopFrameMonitor()
        l1InAppBurstJob?.cancel()
        r1InAppBurstJob?.cancel()
        super.onCleared()
    }
}
