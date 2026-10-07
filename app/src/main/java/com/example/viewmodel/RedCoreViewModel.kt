package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HardwareTelemetryEngine
import com.example.data.RealTimeVoiceChangerEngine
import com.example.data.RedCorePreferencesRepository
import com.example.data.SystemBoosterManager
import com.example.model.ActiveGameSession
import com.example.model.AdvisorPresetMode
import com.example.model.AudioRadarPreset
import com.example.model.AutoSettingsRecommendation
import com.example.model.BoostResult
import com.example.model.ClonedButtonsConfig
import com.example.model.ClonedTouchButton
import com.example.model.CrosshairColorOption
import com.example.model.CrosshairConfig
import com.example.model.CrosshairStyle
import com.example.model.EdgeSidebarConfig
import com.example.model.GameSpaceProfile
import com.example.model.HardwareTelemetry
import com.example.model.InstalledAppProcess
import com.example.model.LowEndOptimizerConfig
import com.example.model.NotificationShieldState
import com.example.model.OneTapGamePrepStatus
import com.example.model.OneTapPrepOverallState
import com.example.model.OneTapPrepStep
import com.example.model.PerformanceMode
import com.example.model.PrepStepState
import com.example.model.QuickReconnectState
import com.example.model.ScreenVisionFilter
import com.example.model.ShoulderTriggerConfig
import com.example.model.SmartThermalMode
import com.example.model.SmartThermalStatus
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
    private val voiceChangerEngine = RealTimeVoiceChangerEngine(application.applicationContext)

    val isMasterEngineRunning: StateFlow<Boolean> = TriggerEventBus.isMasterEngineRunning
    val isLiveMicActive: StateFlow<Boolean> = voiceChangerEngine.isLiveMicActive
    val isRecordingVoiceClip: StateFlow<Boolean> = voiceChangerEngine.isRecordingClip
    val isPlayingVoiceClip: StateFlow<Boolean> = voiceChangerEngine.isPlayingClip
    val micInputLevel: StateFlow<Float> = voiceChangerEngine.micInputLevel
    val voiceStatusText: StateFlow<String> = voiceChangerEngine.voiceStatusText
    val voiceNoiseGateEnabled: StateFlow<Boolean> = voiceChangerEngine.noiseGateEnabled
    val selectedVoiceDemoPhraseIndex: StateFlow<Int> = voiceChangerEngine.selectedPhraseIndex

    private val _selectedTab = MutableStateFlow(RedCoreTab.COMMAND_CENTER)
    val selectedTab: StateFlow<RedCoreTab> = _selectedTab.asStateFlow()

    private val _performanceMode = MutableStateFlow(PerformanceMode.BALANCE)
    val performanceMode: StateFlow<PerformanceMode> = _performanceMode.asStateFlow()

    private val _triggerConfig = MutableStateFlow(ShoulderTriggerConfig())
    val triggerConfig: StateFlow<ShoulderTriggerConfig> = _triggerConfig.asStateFlow()

    private val _crosshairConfig = MutableStateFlow(CrosshairConfig())
    val crosshairConfig: StateFlow<CrosshairConfig> = _crosshairConfig.asStateFlow()

    private val _edgeSidebarConfig = MutableStateFlow(EdgeSidebarConfig())
    val edgeSidebarConfig: StateFlow<EdgeSidebarConfig> = _edgeSidebarConfig.asStateFlow()

    private val _lowEndConfig = MutableStateFlow(LowEndOptimizerConfig())
    val lowEndConfig: StateFlow<LowEndOptimizerConfig> = _lowEndConfig.asStateFlow()

    private val _clonedButtonsConfig = MutableStateFlow(ClonedButtonsConfig())
    val clonedButtonsConfig: StateFlow<ClonedButtonsConfig> = _clonedButtonsConfig.asStateFlow()

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

    private val _activeGameSession = MutableStateFlow(ActiveGameSession())
    val activeGameSession: StateFlow<ActiveGameSession> = _activeGameSession.asStateFlow()

    private val _oneTapPrepStatus = MutableStateFlow(OneTapGamePrepStatus())
    val oneTapPrepStatus: StateFlow<OneTapGamePrepStatus> = _oneTapPrepStatus.asStateFlow()

    private val _smartThermalStatus = MutableStateFlow(SmartThermalStatus())
    val smartThermalStatus: StateFlow<SmartThermalStatus> = _smartThermalStatus.asStateFlow()

    private val _notificationShieldState = MutableStateFlow(NotificationShieldState())
    val notificationShieldState: StateFlow<NotificationShieldState> = _notificationShieldState.asStateFlow()

    private val _autoSettingsRecommendation = MutableStateFlow(AutoSettingsRecommendation())
    val autoSettingsRecommendation: StateFlow<AutoSettingsRecommendation> = _autoSettingsRecommendation.asStateFlow()

    private val _canDrawOverlays = MutableStateFlow(boosterManager.canDrawSystemOverlays())
    val canDrawOverlays: StateFlow<Boolean> = _canDrawOverlays.asStateFlow()

    private var l1InAppBurstJob: Job? = null
    private var r1InAppBurstJob: Job? = null
    private var l1KeyHeldDown = false
    private var r1KeyHeldDown = false
    private var lastAutoDetectedGamePkg = ""

    @Volatile
    private var isAppInForeground = true

    init {
        telemetryEngine.triggerLightweightFrameSample()
        observePreferences()
        observeOverlayRequests()
        observeForegroundGameTransitions()
        startTelemetryLoop()
        startAutoBackgroundCleanerLoop()
        refreshInstalledAppsAndGames()
    }

    fun setAppInForeground(inForeground: Boolean) {
        isAppInForeground = inForeground
        if (inForeground) {
            telemetryEngine.triggerLightweightFrameSample()
            _canDrawOverlays.value = boosterManager.canDrawSystemOverlays()
            refreshQuickReconnectState()
            val hasDndPerm = boosterManager.hasNotificationPolicyAccess()
            if (_notificationShieldState.value.hasDndPolicyPermission != hasDndPerm) {
                _notificationShieldState.value = _notificationShieldState.value.copy(
                    hasDndPolicyPermission = hasDndPerm
                )
            }
        } else {
            telemetryEngine.stopFrameMonitor()
        }
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
                voiceChangerEngine.setPreset(config.voiceModPreset)
                recomputeThermalAndAdvisorStates(_telemetry.value, config)
                if (config.notificationShieldEnabled != _notificationShieldState.value.enabled) {
                    val shieldState = boosterManager.applyNotificationShield(config.notificationShieldEnabled)
                    _notificationShieldState.value = shieldState
                    TriggerEventBus.updateNotificationShieldState(shieldState)
                }
            }
        }
        viewModelScope.launch {
            prefsRepo.clonedButtonsConfigFlow.collectLatest { config ->
                _clonedButtonsConfig.value = config
                TriggerEventBus.updateClonedButtonsConfig(config)
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
        viewModelScope.launch {
            prefsRepo.savedPerGameProfilesRawFlow.collectLatest { rawSaved ->
                val defaultCatalog = boosterManager.buildGameSpaceCatalog()
                val merged = prefsRepo.mergeSavedProfilesWithCatalog(
                    rawSaved = rawSaved,
                    defaultCatalog = defaultCatalog,
                    isPackageInstalledCheck = { pkg -> boosterManager.isPackageInstalled(pkg) }
                )
                _gameCatalog.value = merged

                val lastActiveId = prefsRepo.lastActiveProfileIdFlow.first()
                val currentActive = _activeGameProfile.value
                val matched = merged.find { it.id == (currentActive?.id ?: lastActiveId) } ?: merged.firstOrNull()
                if (matched != null && _activeGameProfile.value == null) {
                    _activeGameProfile.value = matched
                    updateActiveSessionForProfile(matched)
                    recomputeThermalAndAdvisorStates(_telemetry.value, _lowEndConfig.value)
                } else if (currentActive != null) {
                    merged.find { it.id == currentActive.id }?.let { updatedProfile ->
                        _activeGameProfile.value = updatedProfile
                        updateActiveSessionForProfile(updatedProfile)
                    }
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
        viewModelScope.launch {
            TriggerEventBus.coolDownRequests.collectLatest {
                runCpuCoolDownNow()
            }
        }
        viewModelScope.launch {
            TriggerEventBus.toggleNotificationShieldRequests.collectLatest {
                toggleNotificationShield()
            }
        }
        viewModelScope.launch {
            TriggerEventBus.quickReconnectRequests.collectLatest {
                quickReconnectToActiveGame()
            }
        }
        viewModelScope.launch {
            TriggerEventBus.clonedButtonsConfig.collectLatest { busConfig ->
                if (busConfig != _clonedButtonsConfig.value) {
                    _clonedButtonsConfig.value = busConfig
                    prefsRepo.saveClonedButtonsConfig(busConfig)
                }
            }
        }
    }

    /**
     * Automatically detects when the user opens a configured game or exits back to launcher
     * (when Accessibility Service is active) and applies/restores the per-game profile.
     */
    private fun observeForegroundGameTransitions() {
        viewModelScope.launch {
            TriggerEventBus.detectedForegroundPackage.collectLatest { fgPkg ->
                if (fgPkg.isBlank()) return@collectLatest
                val matchedGame = _gameCatalog.value.find {
                    it.packageName.equals(fgPkg, ignoreCase = true)
                }
                if (matchedGame != null && fgPkg != lastAutoDetectedGamePkg) {
                    lastAutoDetectedGamePkg = fgPkg
                    applyGameProfile(matchedGame, showFeedbackBanner = false)
                } else if (matchedGame == null && lastAutoDetectedGamePkg.isNotBlank()) {
                    // User exited the game to another app/launcher
                    lastAutoDetectedGamePkg = ""
                    refreshQuickReconnectState()
                    if (_lowEndConfig.value.autoRestoreSettingsOnExit && _notificationShieldState.value.enabled) {
                        val restored = boosterManager.applyNotificationShield(false)
                        _notificationShieldState.value = restored
                        TriggerEventBus.updateNotificationShieldState(restored)
                    }
                }
            }
        }
    }

    private fun recomputeThermalAndAdvisorStates(
        telemetry: HardwareTelemetry,
        config: LowEndOptimizerConfig
    ) {
        val thermalStatus = telemetryEngine.evaluateSmartThermalStatus(
            telemetry = telemetry,
            mode = config.smartThermalMode
        )
        _smartThermalStatus.value = thermalStatus
        TriggerEventBus.updateSmartThermalStatus(thermalStatus)

        if (thermalStatus.audioDspThrottled) {
            boosterManager.releaseThermalHeavyEffects()
        }

        val advisorRec = telemetryEngine.buildAutoSettingsRecommendation(
            telemetry = telemetry,
            mode = config.advisorPresetMode,
            gameTitle = _activeGameProfile.value?.title
        )
        _autoSettingsRecommendation.value = advisorRec
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            var tick = 0
            while (isActive) {
                val shouldSample = isAppInForeground || TriggerEventBus.isOverlayServiceRunning.value
                if (shouldSample && TriggerEventBus.isMasterEngineRunning.value) {
                    if (isAppInForeground && !_smartThermalStatus.value.reduceAnimationsActive) {
                        telemetryEngine.triggerLightweightFrameSample()
                    }
                    val shouldPing = isAppInForeground && (tick % 8 == 0)
                    val snapshot = telemetryEngine.sampleTelemetry(
                        activeMode = _performanceMode.value,
                        measureNetworkPing = shouldPing
                    )
                    _telemetry.value = snapshot
                    TriggerEventBus.updateTelemetry(snapshot)
                    recomputeThermalAndAdvisorStates(snapshot, _lowEndConfig.value)
                    if (isAppInForeground) {
                        _canDrawOverlays.value = boosterManager.canDrawSystemOverlays()
                    }
                    tick++
                }
                val adaptiveDelay = if (isAppInForeground) {
                    _smartThermalStatus.value.effectivePollIntervalMs.coerceIn(4500L, 15000L)
                } else {
                    12000L
                }
                delay(adaptiveDelay)
            }
        }
    }

    private fun startAutoBackgroundCleanerLoop() {
        viewModelScope.launch {
            while (isActive) {
                val cfg = _lowEndConfig.value
                val waitSec = cfg.autoCleanIntervalSec.coerceIn(60, 240)
                delay(waitSec * 1000L)
                // Only run gentle background cleanup if RAM is high or Smart Thermal requests background trim
                val shouldClean = (cfg.autoCleanInBackground || _smartThermalStatus.value.backgroundTrimActive) &&
                    !_isBoosting.value && TriggerEventBus.isMasterEngineRunning.value
                if (shouldClean) {
                    val currentRamPct = _telemetry.value.ramUsagePercent
                    if (currentRamPct >= cfg.ramThresholdPercent.coerceAtLeast(80) || _smartThermalStatus.value.backgroundTrimActive) {
                        val result = boosterManager.executeSuperBoost(
                            apps = _backgroundApps.value,
                            whitelistedPackages = _whitelistedPackages.value,
                            isAutoBoost = true
                        )
                        telemetryEngine.notifyCacheCleaned(result.cleanedCacheMb)
                        _lastBoostResult.value = result
                        _boostHistory.value = (listOf(result) + _boostHistory.value).take(10)
                    }
                }
            }
        }
    }

    fun runCpuCoolDownNow() {
        if (_isBoosting.value) return
        viewModelScope.launch {
            _isBoosting.value = true
            _boostProgress.value = 0.35f
            _boostStageText.value = "❄️ جاري تفعيل وضع الاستقرار الحراري وتخفيف حمل الخلفية..."
            boosterManager.releaseThermalHeavyEffects()
            if (_performanceMode.value == PerformanceMode.DIABLO) {
                _performanceMode.value = PerformanceMode.BALANCE
                TriggerEventBus.updatePerformanceMode(PerformanceMode.BALANCE)
                prefsRepo.savePerformanceMode(PerformanceMode.BALANCE)
            }
            if (_lowEndConfig.value.smartThermalMode == SmartThermalMode.OFF) {
                updateLowEndConfig { it.copy(smartThermalMode = SmartThermalMode.AUTO_ADAPTIVE) }
            }
            delay(250L)

            val result = boosterManager.executeSuperBoost(
                apps = _backgroundApps.value,
                whitelistedPackages = _whitelistedPackages.value,
                isAutoBoost = true
            )
            telemetryEngine.notifyCacheCleaned(result.cleanedCacheMb)

            _boostProgress.value = 1.0f
            _boostStageText.value = "❄️ تم ضبط الاستقرار الحراري وإيقاف استنزاف الخلفية!"
            val updatedSnap = telemetryEngine.sampleTelemetry(_performanceMode.value, measureNetworkPing = false)
            _telemetry.value = updatedSnap
            TriggerEventBus.updateTelemetry(updatedSnap)
            recomputeThermalAndAdvisorStates(updatedSnap, _lowEndConfig.value)

            delay(300L)
            _isBoosting.value = false
            showBanner("❄️ تم تفعيل الاستقرار الحراري الذكي (${updatedSnap.batteryTempCelsius}°C) وتقليل حمل الخلفية للحفاظ على ثبات الإطارات!")
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
            val rawSaved = prefsRepo.savedPerGameProfilesRawFlow.first()
            val defaultCatalog = boosterManager.buildGameSpaceCatalog()
            val merged = prefsRepo.mergeSavedProfilesWithCatalog(
                rawSaved = rawSaved,
                defaultCatalog = defaultCatalog,
                isPackageInstalledCheck = { pkg -> boosterManager.isPackageInstalled(pkg) }
            )
            _gameCatalog.value = merged
            refreshQuickReconnectState()
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
        if (nextState) {
            TriggerEventBus.setMasterEngineRunning(true)
        }
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

    fun updateClonedButtonsConfig(transform: (ClonedButtonsConfig) -> ClonedButtonsConfig) {
        viewModelScope.launch {
            val updated = transform(_clonedButtonsConfig.value)
            _clonedButtonsConfig.value = updated
            TriggerEventBus.updateClonedButtonsConfig(updated)
            prefsRepo.saveClonedButtonsConfig(updated)
        }
    }

    fun activateOrToggleClonedButtonsOverlay() {
        val hasPerm = boosterManager.canDrawSystemOverlays()
        _canDrawOverlays.value = hasPerm
        if (!hasPerm) {
            showBanner("من فضلك فعّل إذن 'الظهور فوق التطبيقات' لإظهار الأزرار المنسوخة القابلة للتحريك فوق الألعاب")
            boosterManager.openOverlayPermissionSettings()
            return
        }

        val nextOverlayState = !_clonedButtonsConfig.value.systemOverlayEnabled
        updateClonedButtonsConfig {
            it.copy(
                masterEnabled = true,
                systemOverlayEnabled = nextOverlayState
            )
        }
        if (nextOverlayState) {
            TriggerEventBus.setMasterEngineRunning(true)
            updateEdgeSidebarConfig { it.copy(systemFloatingEnabled = true) }
            boosterManager.startOrStopFloatingSidebarService(true)
            showBanner("🔘 تم إظهار الأزرار المنسوخة فوق الشاشة! ضع (🎯 الهدف) فوق الزر الأصلي وحرّك (C1/C2) للمكان المناسب لك ثم اضغط قفل 🔒")
        } else {
            showBanner("تم إخفاء الأزرار المنسوخة العائمة الخارجية")
        }
    }

    fun triggerClonedButtonInApp(button: ClonedTouchButton, isDown: Boolean = true) {
        if (isDown) {
            boosterManager.playTacticalFeedback(isMajorBoost = false)
        }
        TriggerEventBus.emitClonedButtonTap(button, isDown = isDown)
    }

    fun selectAudioRadarPreset(preset: AudioRadarPreset) {
        boosterManager.applyAudioRadarPreset(preset)
        updateLowEndConfig { it.copy(audioRadarPreset = preset) }
        showBanner("تم تفعيل الصوت التكتيكي: ${preset.arabicName}")
    }

    fun selectVoiceModPreset(preset: VoiceModPreset) {
        voiceChangerEngine.previewVoicePreset(preset)
        updateLowEndConfig { it.copy(voiceModPreset = preset) }
        showBanner("🔊 جاري إسماعك مغير الصوت (${preset.arabicName}) فوراً بدون مايك!")
    }

    fun playReadyVoiceSampleWithoutMic(
        preset: VoiceModPreset = _lowEndConfig.value.voiceModPreset,
        phraseIndex: Int = voiceChangerEngine.selectedPhraseIndex.value
    ) {
        updateLowEndConfig { it.copy(voiceModPreset = preset) }
        voiceChangerEngine.playReadyVoiceDemoWithoutSpeaking(preset, phraseIndex)
        showBanner("🔊 تشغيل صوت جاهز بدون ما تتكلم: ${preset.arabicName}")
    }

    fun hasMicrophonePermission(): Boolean {
        return voiceChangerEngine.hasRecordAudioPermission()
    }

    fun toggleLiveMicrophoneVoiceChanger() {
        val started = voiceChangerEngine.toggleLiveMicStream()
        if (started) {
            TriggerEventBus.setMasterEngineRunning(true)
            showBanner("🔴 المايك المباشر يعمل الآن بفلتر (${_lowEndConfig.value.voiceModPreset.arabicName}) بصوت نقي وبدون تشويش")
        } else {
            showBanner("تم إيقاف البث المباشر للمايكروفون")
        }
    }

    fun startOrStopVoiceTestRecording() {
        voiceChangerEngine.startOrStopVoiceTestRecording()
    }

    fun replayRecordedVoiceSample() {
        voiceChangerEngine.replayLastRecordedVoice()
    }

    fun setVoiceNoiseGate(enabled: Boolean) {
        voiceChangerEngine.setNoiseGateEnabled(enabled)
    }

    /**
     * Unified Start / Stop & Exit toggle:
     * - If stopped (false): Starts the engine & gaming tools (sets running = true).
     * - If running (true): Stops all background services, overlays, mic, and closes the app completely!
     */
    fun toggleMasterStartOrStopAndExit(onExitActivity: () -> Unit) {
        if (!TriggerEventBus.isMasterEngineRunning.value) {
            TriggerEventBus.setMasterEngineRunning(true)
            telemetryEngine.triggerLightweightFrameSample()
            boosterManager.playTacticalFeedback(isMajorBoost = true)
            showBanner("▶️ تم بدء تشغيل البرنامج وتفعيل محرك الألعاب بنجاح! (اضغط 'إنهاء وإغلاق' عند الانتهاء من اللعب)")
        } else {
            stopAllBackgroundWorkAndExit(onExitActivity)
        }
    }

    fun stopAllBackgroundWorkAndExit(onExitActivity: () -> Unit) {
        TriggerEventBus.setMasterEngineRunning(false)
        TriggerEventBus.setInAppSidebarOpen(false)
        voiceChangerEngine.stopAllVoiceActivity()
        boosterManager.releaseThermalHeavyEffects()
        val restoredShield = boosterManager.applyNotificationShield(false)
        _notificationShieldState.value = restoredShield
        TriggerEventBus.updateNotificationShieldState(restoredShield)
        boosterManager.startOrStopFloatingSidebarService(false)
        telemetryEngine.stopFrameMonitor()
        l1InAppBurstJob?.cancel()
        r1InAppBurstJob?.cancel()
        updateEdgeSidebarConfig { it.copy(systemFloatingEnabled = false) }
        updateClonedButtonsConfig { it.copy(systemOverlayEnabled = false) }
        updateCrosshairConfig { it.copy(systemOverlayEnabled = false) }
        updateLowEndConfig {
            it.copy(
                notificationShieldEnabled = false,
                fpsOverlayEnabled = false,
                tempOverlayEnabled = false,
                ramOverlayEnabled = false,
                magnifierEnabled = false
            )
        }
        onExitActivity()
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 1. ONE-TAP GAME PREPARATION (نظام التجهيز الشامل بضغطة واحدة)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun runOneTapGamePreparation(
        targetProfile: GameSpaceProfile? = _activeGameProfile.value,
        onReadyCallback: (() -> Unit)? = null
    ) {
        if (_oneTapPrepStatus.value.overallState == OneTapPrepOverallState.PREPARING) return
        viewModelScope.launch {
            TriggerEventBus.setMasterEngineRunning(true)
            val gameName = targetProfile?.title ?: "جميع الألعاب (الوضع العام)"
            val initialSteps = listOf(
                OneTapPrepStep(
                    id = "mem_trim",
                    titleAr = "1. تنظيف الكاش وتخفيف العمليات الخلفية غير الضرورية",
                    detailAr = "تحرير الذاكرة العشوائية بدون استهلاك معالج إضافي",
                    state = PrepStepState.RUNNING
                ),
                OneTapPrepStep(
                    id = "thermal_guard",
                    titleAr = "2. معايرة الوضع الحراري الذكي واستقرار المعالج",
                    detailAr = "تفعيل ${targetProfile?.smartThermalMode?.arabicTitle ?: _lowEndConfig.value.smartThermalMode.arabicTitle}",
                    state = PrepStepState.PENDING
                ),
                OneTapPrepStep(
                    id = "notif_shield",
                    titleAr = "3. تفعيل درع عزل الإشعارات والتنبيهات المشتتة",
                    detailAr = "منع المقاطعة الصوتية والبصرية أثناء جلسة اللعب",
                    state = PrepStepState.PENDING
                ),
                OneTapPrepStep(
                    id = "profile_tools",
                    titleAr = "4. تطبيق بروفايل اللعب وأدوات التحكم والشاشة",
                    detailAr = if (targetProfile != null) "تطبيق إعدادات (${targetProfile.title})" else "ضبط أزرار L1/R1 ومؤشر التصويب",
                    state = PrepStepState.PENDING
                )
            )

            _oneTapPrepStatus.value = OneTapGamePrepStatus(
                overallState = OneTapPrepOverallState.PREPARING,
                badgeText = "PREPARING...",
                targetGameTitle = gameName,
                steps = initialSteps
            )

            // Step 1: Fast, safe memory & cache trim
            val boostRes = boosterManager.executeSuperBoost(
                apps = _backgroundApps.value,
                whitelistedPackages = _whitelistedPackages.value,
                isAutoBoost = true
            )
            telemetryEngine.notifyCacheCleaned(boostRes.cleanedCacheMb)
            _lastBoostResult.value = boostRes
            _boostHistory.value = (listOf(boostRes) + _boostHistory.value).take(10)
            delay(180L)

            // Step 2: Smart Thermal calibration
            val step2List = initialSteps.map {
                when (it.id) {
                    "mem_trim" -> it.copy(
                        detailAr = "تم تحرير ${boostRes.freedRamMb} MB وتوقيف ${boostRes.stoppedAppsCount} عمليات خلفية",
                        state = PrepStepState.COMPLETED
                    )
                    "thermal_guard" -> it.copy(state = PrepStepState.RUNNING)
                    else -> it
                }
            }
            _oneTapPrepStatus.value = _oneTapPrepStatus.value.copy(steps = step2List)

            val targetThermal = targetProfile?.smartThermalMode
                ?: if (_lowEndConfig.value.smartThermalMode == SmartThermalMode.OFF) SmartThermalMode.AUTO_ADAPTIVE else _lowEndConfig.value.smartThermalMode
            delay(160L)

            // Step 3: Notification Shield
            val step3List = step2List.map {
                when (it.id) {
                    "thermal_guard" -> it.copy(
                        detailAr = "نشط: ${targetThermal.arabicTitle} (${_telemetry.value.batteryTempCelsius}°C)",
                        state = PrepStepState.COMPLETED
                    )
                    "notif_shield" -> it.copy(state = PrepStepState.RUNNING)
                    else -> it
                }
            }
            _oneTapPrepStatus.value = _oneTapPrepStatus.value.copy(steps = step3List)

            val shouldEnableShield = targetProfile?.notificationShieldEnabled ?: true
            val shieldState = boosterManager.applyNotificationShield(shouldEnableShield)
            _notificationShieldState.value = shieldState
            TriggerEventBus.updateNotificationShieldState(shieldState)
            delay(160L)

            // Step 4: Apply Per-Game Profile or general gaming configuration
            val step4List = step3List.map {
                when (it.id) {
                    "notif_shield" -> it.copy(
                        detailAr = shieldState.statusLabelAr,
                        state = PrepStepState.COMPLETED
                    )
                    "profile_tools" -> it.copy(state = PrepStepState.RUNNING)
                    else -> it
                }
            }
            _oneTapPrepStatus.value = _oneTapPrepStatus.value.copy(steps = step4List)

            if (targetProfile != null) {
                applyGameProfileInternal(targetProfile)
            } else {
                updateLowEndConfig {
                    it.copy(
                        smartThermalMode = targetThermal,
                        notificationShieldEnabled = shouldEnableShield
                    )
                }
            }
            boosterManager.applyGameMediaVolumePercent(_lowEndConfig.value.gameMediaVolumePercent)
            delay(160L)

            val updatedSnap = telemetryEngine.sampleTelemetry(_performanceMode.value, measureNetworkPing = false)
            _telemetry.value = updatedSnap
            TriggerEventBus.updateTelemetry(updatedSnap)
            recomputeThermalAndAdvisorStates(updatedSnap, _lowEndConfig.value)

            val completedSteps = step4List.map {
                if (it.id == "profile_tools") {
                    it.copy(
                        detailAr = if (targetProfile != null) {
                            "تم تفعيل بروفايل ${targetProfile.title} (${targetProfile.recommendedMode.englishBadge})"
                        } else {
                            "تم تفعيل ${_performanceMode.value.arabicTitle} ومعايرة الأدوات"
                        },
                        state = PrepStepState.COMPLETED
                    )
                } else {
                    it
                }
            }

            val finalState = if (targetProfile != null) OneTapPrepOverallState.GAME_READY else OneTapPrepOverallState.BOOST_READY
            val finalBadge = if (targetProfile != null) "GAME READY ⚡" else "BOOST READY ⚡"

            _oneTapPrepStatus.value = OneTapGamePrepStatus(
                overallState = finalState,
                badgeText = finalBadge,
                targetGameTitle = gameName,
                steps = completedSteps,
                freedRamMb = boostRes.freedRamMb,
                availableRamMb = updatedSnap.ramAvailableMb,
                thermalStateLabel = _smartThermalStatus.value.thermalLevel.arabicLabel,
                appliedProfileTitle = targetProfile?.title ?: _performanceMode.value.arabicTitle,
                timestampMs = System.currentTimeMillis()
            )

            boosterManager.playTacticalFeedback(isMajorBoost = true)
            showBanner("✅ $finalBadge — الجهاز جاهز تماماً للعب (${updatedSnap.ramAvailableMb}MB رام متاح • حرارة ${updatedSnap.batteryTempCelsius}°C)")
            onReadyCallback?.invoke()
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 2. SMART THERMAL MODE & HUD OVERLAYS (الوضع الحراري الذكي والعدادات)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun setSmartThermalMode(mode: SmartThermalMode) {
        updateLowEndConfig { it.copy(smartThermalMode = mode) }
        recomputeThermalAndAdvisorStates(_telemetry.value, _lowEndConfig.value.copy(smartThermalMode = mode))
        showBanner("🌡️ الوضع الحراري الذكي: ${mode.arabicTitle}")
    }

    fun toggleHudOverlayMetric(fps: Boolean? = null, temp: Boolean? = null, ram: Boolean? = null, magnifier: Boolean? = null) {
        val current = _lowEndConfig.value
        val nextFps = fps ?: current.fpsOverlayEnabled
        val nextTemp = temp ?: current.tempOverlayEnabled
        val nextRam = ram ?: current.ramOverlayEnabled
        val nextMag = magnifier ?: current.magnifierEnabled

        updateLowEndConfig {
            it.copy(
                fpsOverlayEnabled = nextFps,
                tempOverlayEnabled = nextTemp,
                ramOverlayEnabled = nextRam,
                magnifierEnabled = nextMag
            )
        }

        val anySystemOverlayWanted = nextFps || nextTemp || nextRam || nextMag
        if (anySystemOverlayWanted && boosterManager.canDrawSystemOverlays()) {
            TriggerEventBus.setMasterEngineRunning(true)
            boosterManager.startOrStopFloatingSidebarService(true)
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 3. NOTIFICATION SHIELD (درع عزل الإشعارات الاحترافي)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun toggleNotificationShield() {
        val next = !_notificationShieldState.value.enabled
        setNotificationShield(next)
    }

    fun setNotificationShield(enabled: Boolean) {
        val state = boosterManager.applyNotificationShield(enabled)
        _notificationShieldState.value = state
        TriggerEventBus.updateNotificationShieldState(state)
        updateLowEndConfig { it.copy(notificationShieldEnabled = enabled) }
        showBanner(state.statusLabelAr)
    }

    fun openNotificationPolicyAccessSettings() {
        boosterManager.openNotificationPolicyAccessSettings()
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 4. PER-GAME PROFILES & QUICK GAME RECONNECT (البروفايلات والعودة السريعة)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private fun updateActiveSessionForProfile(profile: GameSpaceProfile) {
        val reconnectState = boosterManager.inspectQuickReconnectState(profile.packageName)
        val session = ActiveGameSession(
            profileId = profile.id,
            gameTitle = profile.title,
            packageName = profile.packageName,
            startedAtMs = System.currentTimeMillis(),
            isInstalledOnDevice = profile.isInstalledOnDevice,
            reconnectState = reconnectState
        )
        _activeGameSession.value = session
        TriggerEventBus.updateActiveGameSession(session)
    }

    fun refreshQuickReconnectState() {
        val profile = _activeGameProfile.value ?: return
        val updatedInstalled = boosterManager.isPackageInstalled(profile.packageName)
        val state = boosterManager.inspectQuickReconnectState(profile.packageName)
        val session = _activeGameSession.value.copy(
            profileId = profile.id,
            gameTitle = profile.title,
            packageName = profile.packageName,
            isInstalledOnDevice = updatedInstalled,
            reconnectState = state
        )
        _activeGameSession.value = session
        TriggerEventBus.updateActiveGameSession(session)
    }

    private suspend fun applyGameProfileInternal(profile: GameSpaceProfile) {
        _activeGameProfile.value = profile
        prefsRepo.saveLastActiveProfileId(profile.id)

        _performanceMode.value = profile.recommendedMode
        TriggerEventBus.updatePerformanceMode(profile.recommendedMode)
        prefsRepo.savePerformanceMode(profile.recommendedMode)

        val updatedTrigger = _triggerConfig.value.copy(
            enabled = profile.triggersEnabled,
            l1XRatio = profile.l1X,
            l1YRatio = profile.l1Y,
            l1ActionName = profile.l1ActionAr,
            r1XRatio = profile.r1X,
            r1YRatio = profile.r1Y,
            r1ActionName = profile.r1ActionAr
        )
        _triggerConfig.value = updatedTrigger
        TriggerEventBus.updateTriggerConfig(updatedTrigger)
        prefsRepo.saveTriggerConfig(updatedTrigger)

        val updatedCrosshair = _crosshairConfig.value.copy(
            enabledInApp = profile.crosshairEnabled,
            style = profile.crosshairStyle
        )
        _crosshairConfig.value = updatedCrosshair
        TriggerEventBus.updateCrosshairConfig(updatedCrosshair)
        prefsRepo.saveCrosshairConfig(updatedCrosshair)

        val updatedSidebar = _edgeSidebarConfig.value.copy(
            enabledInApp = profile.gamingSidebarEnabled
        )
        _edgeSidebarConfig.value = updatedSidebar
        TriggerEventBus.updateEdgeSidebarConfig(updatedSidebar)
        prefsRepo.saveEdgeSidebarConfig(updatedSidebar)

        val updatedCloned = _clonedButtonsConfig.value.copy(
            masterEnabled = profile.clonedButtonsEnabled
        )
        _clonedButtonsConfig.value = updatedCloned
        TriggerEventBus.updateClonedButtonsConfig(updatedCloned)
        prefsRepo.saveClonedButtonsConfig(updatedCloned)

        val updatedLowEnd = _lowEndConfig.value.copy(
            smartThermalMode = profile.smartThermalMode,
            notificationShieldEnabled = profile.notificationShieldEnabled,
            magnifierEnabled = profile.magnifierEnabled,
            magnifierZoom = profile.magnifierZoom,
            mistouchPrevention = profile.touchProtectionEnabled,
            fpsOverlayEnabled = profile.fpsOverlayEnabled,
            tempOverlayEnabled = profile.tempOverlayEnabled,
            ramOverlayEnabled = profile.ramOverlayEnabled,
            advisorPresetMode = profile.advisorMode
        )
        _lowEndConfig.value = updatedLowEnd
        TriggerEventBus.updateLowEndConfig(updatedLowEnd)
        prefsRepo.saveLowEndOptimizerConfig(updatedLowEnd)

        val shieldState = boosterManager.applyNotificationShield(profile.notificationShieldEnabled)
        _notificationShieldState.value = shieldState
        TriggerEventBus.updateNotificationShieldState(shieldState)

        updateActiveSessionForProfile(profile)
        recomputeThermalAndAdvisorStates(_telemetry.value, updatedLowEnd)
    }

    fun applyGameProfile(profile: GameSpaceProfile, showFeedbackBanner: Boolean = true) {
        viewModelScope.launch {
            applyGameProfileInternal(profile)
            if (showFeedbackBanner) {
                boosterManager.playTacticalFeedback(isMajorBoost = false)
                showBanner("🎮 تم تطبيق بروفايل (${profile.title}) المخصص وجميع أدواته بنجاح!")
            }
        }
    }

    fun saveOrUpdateGameProfile(updatedProfile: GameSpaceProfile) {
        viewModelScope.launch {
            val currentList = _gameCatalog.value.toMutableList()
            val idx = currentList.indexOfFirst { it.id == updatedProfile.id }
            val withInstallCheck = updatedProfile.copy(
                isInstalledOnDevice = boosterManager.isPackageInstalled(updatedProfile.packageName)
            )
            if (idx >= 0) {
                currentList[idx] = withInstallCheck
            } else {
                currentList.add(withInstallCheck)
            }
            _gameCatalog.value = currentList
            prefsRepo.savePerGameProfiles(currentList)
            applyGameProfileInternal(withInstallCheck)
            showBanner("💾 تم حفظ وتطبيق إعدادات بروفايل (${withInstallCheck.title}) بشكل دائم!")
        }
    }

    fun addCustomGameProfile(
        title: String,
        packageName: String,
        genreAr: String,
        mode: PerformanceMode,
        targetFps: Int
    ) {
        val cleanTitle = title.trim().ifBlank { "لعبة مخصصة جديدة" }
        val cleanPkg = packageName.trim().ifBlank { "com.custom.game.${System.currentTimeMillis() % 10000}" }
        val newProfile = GameSpaceProfile(
            id = "custom_${System.currentTimeMillis()}",
            title = cleanTitle,
            packageName = cleanPkg,
            genreAr = genreAr.trim().ifBlank { "بروفايل لعب مخصص" },
            recommendedMode = mode,
            l1ActionAr = "زر تكتيكي يسار (L1)",
            r1ActionAr = "زر إطلاق سريع يمين (R1)",
            l1X = 0.22f,
            l1Y = 0.36f,
            r1X = 0.80f,
            r1Y = 0.58f,
            targetFps = targetFps.coerceIn(30, 144),
            isInstalledOnDevice = boosterManager.isPackageInstalled(cleanPkg),
            accentHex = 0xFF00F0FF,
            smartThermalMode = SmartThermalMode.AUTO_ADAPTIVE,
            notificationShieldEnabled = true,
            magnifierEnabled = false,
            crosshairEnabled = true,
            touchProtectionEnabled = true,
            gamingSidebarEnabled = true,
            fpsOverlayEnabled = true,
            triggersEnabled = true,
            advisorMode = AdvisorPresetMode.BALANCED,
            isCustomAdded = true
        )
        saveOrUpdateGameProfile(newProfile)
    }

    fun duplicateGameProfile(source: GameSpaceProfile) {
        val copyProfile = source.copy(
            id = "${source.id}_copy_${System.currentTimeMillis() % 10000}",
            title = "${source.title} (نسخة مخصصة)",
            isCustomAdded = true
        )
        saveOrUpdateGameProfile(copyProfile)
    }

    fun resetGameProfileToDefault(profileId: String) {
        viewModelScope.launch {
            val defaults = boosterManager.buildGameSpaceCatalog()
            val defaultMatch = defaults.find { it.id == profileId }
            val currentList = _gameCatalog.value.toMutableList()
            if (defaultMatch != null) {
                val idx = currentList.indexOfFirst { it.id == profileId }
                if (idx >= 0) {
                    currentList[idx] = defaultMatch
                }
                _gameCatalog.value = currentList
                prefsRepo.savePerGameProfiles(currentList)
                applyGameProfileInternal(defaultMatch)
                showBanner("🔄 تمت استعادة الإعدادات الافتراضية لبروفايل (${defaultMatch.title})")
            } else {
                // Custom added profile -> delete it
                val filtered = currentList.filterNot { it.id == profileId }
                _gameCatalog.value = filtered
                prefsRepo.savePerGameProfiles(filtered)
                filtered.firstOrNull()?.let { applyGameProfileInternal(it) }
                showBanner("🗑️ تم حذف البروفايل المخصص")
            }
        }
    }

    fun quickReconnectToActiveGame() {
        val profile = _activeGameProfile.value ?: _gameCatalog.value.firstOrNull()
        if (profile == null) {
            _selectedTab.value = RedCoreTab.GAME_SPACE
            showBanner("اختر لعبة من قائمة البروفايلات أولاً لتفعيل العودة السريعة")
            return
        }
        viewModelScope.launch {
            applyGameProfileInternal(profile)
            val launched = boosterManager.quickReconnectOrLaunchGame(profile.packageName)
            refreshQuickReconnectState()
            if (launched) {
                showBanner("⚡ جاري العودة الفورية إلى ${profile.title} مع تفعيل البروفايل...")
            } else {
                showBanner("✅ تم تفعيل كامل إعدادات بروفايل (${profile.title})! اللعبة غير مثبتة على هذا الجهاز، يمكنك تجربة الأدوات الآن")
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 5. AUTO SETTINGS ADVISOR (مستشار إعدادات الجرافيك الذكي)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun selectAdvisorPresetMode(mode: AdvisorPresetMode) {
        updateLowEndConfig { it.copy(advisorPresetMode = mode) }
        val rec = telemetryEngine.buildAutoSettingsRecommendation(
            telemetry = _telemetry.value,
            mode = mode,
            gameTitle = _activeGameProfile.value?.title
        )
        _autoSettingsRecommendation.value = rec
    }

    fun applyAdvisorRecommendedToolsNow() {
        val mode = _lowEndConfig.value.advisorPresetMode
        val rec = _autoSettingsRecommendation.value
        val targetPerf = when (mode) {
            AdvisorPresetMode.BEST_PERFORMANCE -> PerformanceMode.DIABLO
            AdvisorPresetMode.BALANCED -> PerformanceMode.RISE
            AdvisorPresetMode.BEST_VISUAL_QUALITY -> PerformanceMode.BALANCE
        }
        val targetThermal = when (mode) {
            AdvisorPresetMode.BEST_PERFORMANCE -> SmartThermalMode.AUTO_ADAPTIVE
            AdvisorPresetMode.BALANCED -> SmartThermalMode.AUTO_ADAPTIVE
            AdvisorPresetMode.BEST_VISUAL_QUALITY -> SmartThermalMode.ECO_STABILITY
        }
        setPerformanceMode(targetPerf)
        updateLowEndConfig {
            it.copy(
                smartThermalMode = targetThermal,
                gpuForce4xMsaaOff = mode != AdvisorPresetMode.BEST_VISUAL_QUALITY,
                shadowDownscale = mode != AdvisorPresetMode.BEST_VISUAL_QUALITY,
                resolutionScalePreset = rec.resolutionRecommendation
            )
        }
        showBanner("✅ تم تطبيق توصيات مستشار الإعدادات (${mode.arabicTitle}) وضبط محرك الأداء والحرارة!")
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

    fun cycleVoiceModPreset() {
        val entries = VoiceModPreset.entries
        val next = entries[(_lowEndConfig.value.voiceModPreset.ordinal + 1) % entries.size]
        selectVoiceModPreset(next)
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
        runOneTapGamePreparation(targetProfile = profile) {
            val launched = boosterManager.quickReconnectOrLaunchGame(profile.packageName)
            refreshQuickReconnectState()
            if (!launched) {
                _selectedTab.value = RedCoreTab.SHOULDER_TRIGGERS
                showBanner("⚡ GAME READY — تم تجهيز وتطبيق بروفايل ${profile.title}! جرب أزرار L1/R1 والأدوات الآن")
            }
        }
    }

    override fun onCleared() {
        telemetryEngine.stopFrameMonitor()
        voiceChangerEngine.release()
        boosterManager.releaseThermalHeavyEffects()
        l1InAppBurstJob?.cancel()
        r1InAppBurstJob?.cancel()
        super.onCleared()
    }
}
