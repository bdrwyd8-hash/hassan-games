package com.example.viewmodel

import android.app.Application
import android.content.Intent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HardwareTelemetryEngine
import com.example.data.RedCorePreferencesRepository
import com.example.data.SystemBoosterManager
import com.example.model.AdvisorPresetMode
import com.example.model.AutoSettingsRecommendation
import com.example.model.BoostProcessItem
import com.example.model.BoostResultReport
import com.example.model.ClonedButtonMode
import com.example.model.ClonedButtonsConfig
import com.example.model.CrosshairConfig
import com.example.model.CrosshairStyle
import com.example.model.GamePrepReport
import com.example.model.GameSpaceProfile
import com.example.model.GfxResolutionPreset
import com.example.model.HardwareTelemetry
import com.example.model.InstalledAppCandidate
import com.example.model.LastSessionGameInfo
import com.example.model.LowEndDeviceConfig
import com.example.model.NotificationShieldState
import com.example.model.PerformanceMode
import com.example.model.QuickReconnectState
import com.example.model.SmartThermalMode
import com.example.model.SmartThermalStatus
import com.example.model.TouchSamplingRate
import com.example.service.FloatingGameSidebarService
import com.example.service.TriggerEventBus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RedCoreTab(val titleAr: String, val badgeCode: String) {
    GAME_SPACE("ألعابي My Games", "MY GAMES"),
    COMMAND_CENTER("مركز القيادة", "CORE"),
    RAM_BOOSTER("مُقوّي الرام", "BOOST"),
    ARSENAL_GFX("ترسانة اللعب", "ARSENAL")
}

class RedCoreViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = RedCorePreferencesRepository(application)
    private val telemetryEngine = HardwareTelemetryEngine(application)
    private val boosterManager = SystemBoosterManager(application)

    private val _selectedTab = MutableStateFlow(RedCoreTab.GAME_SPACE)
    val selectedTab: StateFlow<RedCoreTab> = _selectedTab.asStateFlow()

    private val _performanceMode = MutableStateFlow(prefsRepo.loadPerformanceMode())
    val performanceMode: StateFlow<PerformanceMode> = _performanceMode.asStateFlow()

    private val _telemetry = MutableStateFlow(HardwareTelemetry())
    val telemetry: StateFlow<HardwareTelemetry> = _telemetry.asStateFlow()

    private val _smartThermalStatus = MutableStateFlow(SmartThermalStatus())
    val smartThermalStatus: StateFlow<SmartThermalStatus> = _smartThermalStatus.asStateFlow()

    private val _advisorPresetMode = MutableStateFlow(prefsRepo.loadAdvisorPresetMode())
    val advisorPresetMode: StateFlow<AdvisorPresetMode> = _advisorPresetMode.asStateFlow()

    private val _autoSettingsRecommendation = MutableStateFlow(AutoSettingsRecommendation())
    val autoSettingsRecommendation: StateFlow<AutoSettingsRecommendation> =
        _autoSettingsRecommendation.asStateFlow()

    private val _clonedButtonsConfig = MutableStateFlow(prefsRepo.loadClonedButtonsConfig())
    val clonedButtonsConfig: StateFlow<ClonedButtonsConfig> = _clonedButtonsConfig.asStateFlow()

    private val _selectedClonedButtonId = MutableStateFlow(1)
    val selectedClonedButtonId: StateFlow<Int> = _selectedClonedButtonId.asStateFlow()

    private val _crosshairConfig = MutableStateFlow(prefsRepo.loadCrosshairConfig())
    val crosshairConfig: StateFlow<CrosshairConfig> = _crosshairConfig.asStateFlow()

    private val _lowEndConfig = MutableStateFlow(prefsRepo.loadLowEndConfig())
    val lowEndConfig: StateFlow<LowEndDeviceConfig> = _lowEndConfig.asStateFlow()

    private val _gfxResolution = MutableStateFlow(prefsRepo.loadGfxResolution())
    val gfxResolution: StateFlow<GfxResolutionPreset> = _gfxResolution.asStateFlow()

    private val _touchSamplingRate = MutableStateFlow(prefsRepo.loadTouchSamplingRate())
    val touchSamplingRate: StateFlow<TouchSamplingRate> = _touchSamplingRate.asStateFlow()

    private val _whitelistedPackages = MutableStateFlow(prefsRepo.loadWhitelistedPackages())
    val whitelistedPackages: StateFlow<Set<String>> = _whitelistedPackages.asStateFlow()

    private val _optimizableProcesses = MutableStateFlow<List<BoostProcessItem>>(emptyList())
    val optimizableProcesses: StateFlow<List<BoostProcessItem>> = _optimizableProcesses.asStateFlow()

    private val _isBoosting = MutableStateFlow(false)
    val isBoosting: StateFlow<Boolean> = _isBoosting.asStateFlow()

    private val _lastBoostReport = MutableStateFlow<BoostResultReport?>(null)
    val lastBoostReport: StateFlow<BoostResultReport?> = _lastBoostReport.asStateFlow()

    private val _isPreparingGame = MutableStateFlow(false)
    val isPreparingGame: StateFlow<Boolean> = _isPreparingGame.asStateFlow()

    private val _lastGamePrepReport = MutableStateFlow<GamePrepReport?>(null)
    val lastGamePrepReport: StateFlow<GamePrepReport?> = _lastGamePrepReport.asStateFlow()

    private val _gameSpaceCatalog = MutableStateFlow<List<GameSpaceProfile>>(emptyList())
    val gameSpaceCatalog: StateFlow<List<GameSpaceProfile>> = _gameSpaceCatalog.asStateFlow()

    private val _installedAppCandidates = MutableStateFlow<List<InstalledAppCandidate>>(emptyList())
    val installedAppCandidates: StateFlow<List<InstalledAppCandidate>> =
        _installedAppCandidates.asStateFlow()

    private val _showAddGameSheet = MutableStateFlow(false)
    val showAddGameSheet: StateFlow<Boolean> = _showAddGameSheet.asStateFlow()

    private val _addGameSearchQuery = MutableStateFlow("")
    val addGameSearchQuery: StateFlow<String> = _addGameSearchQuery.asStateFlow()

    private val _addGameFilterOnlyGames = MutableStateFlow(false)
    val addGameFilterOnlyGames: StateFlow<Boolean> = _addGameFilterOnlyGames.asStateFlow()

    private val _selectedProfileForSheet = MutableStateFlow<GameSpaceProfile?>(null)
    val selectedProfileForSheet: StateFlow<GameSpaceProfile?> = _selectedProfileForSheet.asStateFlow()

    private val _lastSessionGameInfo = MutableStateFlow<LastSessionGameInfo?>(null)
    val lastSessionGameInfo: StateFlow<LastSessionGameInfo?> = _lastSessionGameInfo.asStateFlow()

    private val _notificationShieldState = MutableStateFlow(NotificationShieldState())
    val notificationShieldState: StateFlow<NotificationShieldState> =
        _notificationShieldState.asStateFlow()

    private val _fpsHudOverlayEnabled = MutableStateFlow(false)
    val fpsHudOverlayEnabled: StateFlow<Boolean> = _fpsHudOverlayEnabled.asStateFlow()

    private val _edgeGenieExpanded = MutableStateFlow(false)
    val edgeGenieExpanded: StateFlow<Boolean> = _edgeGenieExpanded.asStateFlow()

    private val _overlayPermissionGranted = MutableStateFlow(false)
    val overlayPermissionGranted: StateFlow<Boolean> = _overlayPermissionGranted.asStateFlow()

    private val _accessibilityServiceEnabled = MutableStateFlow(false)
    val accessibilityServiceEnabled: StateFlow<Boolean> = _accessibilityServiceEnabled.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    val accessibilityConnectedLive: StateFlow<Boolean> = TriggerEventBus.serviceConnected
    val floatingOverlayRunning: StateFlow<Boolean> = TriggerEventBus.overlayRunning
    val totalClonedTaps: StateFlow<Int> = TriggerEventBus.totalClonedTaps
    val lastClonedActiveId: StateFlow<Int?> = TriggerEventBus.lastClonedActiveId
    val isMasterEngineRunning: StateFlow<Boolean> = TriggerEventBus.isMasterEngineRunning
    val activeGameTitle: StateFlow<String?> = TriggerEventBus.activeGameTitle

    private var telemetryLoopJob: Job? = null
    private var isAppInForeground: Boolean = true
    private var pingPollCycleCounter = 0

    init {
        TriggerEventBus.updatePerformanceMode(_performanceMode.value)
        TriggerEventBus.updateClonedButtonsConfig(_clonedButtonsConfig.value)
        TriggerEventBus.updateCrosshairConfig(_crosshairConfig.value)
        TriggerEventBus.updateLowEndConfig(_lowEndConfig.value)

        refreshSystemPermissionsAndLists()
        startAdaptiveTelemetryLoop()
        observeBusStateChanges()
    }

    fun getAppIconBitmap(packageName: String): ImageBitmap? {
        return boosterManager.getAppIconBitmap(packageName)
    }

    fun setAppInForeground(inForeground: Boolean) {
        isAppInForeground = inForeground
        if (inForeground) {
            refreshSystemPermissionsAndLists()
            if (telemetryLoopJob?.isActive != true && isMasterEngineRunning.value) {
                startAdaptiveTelemetryLoop()
            }
        } else {
            telemetryEngine.stopFrameMonitor()
        }
    }

    fun refreshSystemPermissionsAndLists() {
        _overlayPermissionGranted.value = boosterManager.canDrawOverlays()
        _accessibilityServiceEnabled.value =
            boosterManager.isAccessibilityServiceEnabled() || TriggerEventBus.serviceConnected.value

        _optimizableProcesses.value =
            boosterManager.scanOptimizableProcesses(_whitelistedPackages.value)

        val defaultCatalog = boosterManager.buildGameSpaceCatalog()
        val rawSaved = prefsRepo.loadRawGameProfiles()
        val currentMyGames = if (prefsRepo.hasSavedMyGamesList()) {
            prefsRepo.parseSavedGameProfiles(rawSaved) { pkg ->
                boosterManager.isPackageInstalled(pkg)
            }
        } else {
            prefsRepo.mergeSavedProfilesWithCatalog(
                rawSaved = rawSaved,
                defaultCatalog = defaultCatalog,
                isPackageInstalled = { pkg -> boosterManager.isPackageInstalled(pkg) }
            )
        }
        _gameSpaceCatalog.value = currentMyGames

        // Scan installed launchable apps in background for "+ ADD GAME"
        viewModelScope.launch {
            val addedPkgs = _gameSpaceCatalog.value.map { it.packageName }.toSet()
            _installedAppCandidates.value = boosterManager.scanInstalledLaunchableApps(addedPkgs)
        }

        _notificationShieldState.value = boosterManager.inspectNotificationShieldState(
            enabledInConfig = _lowEndConfig.value.blockHeadsUpNotifications
        )

        refreshLastSessionGameInfo()
    }

    private fun startAdaptiveTelemetryLoop() {
        telemetryLoopJob?.cancel()
        telemetryLoopJob = viewModelScope.launch {
            while (isActive) {
                if (!isMasterEngineRunning.value) {
                    delay(6000L)
                    continue
                }

                if (!isAppInForeground && !TriggerEventBus.overlayRunning.value) {
                    delay(12000L)
                    continue
                }

                val thermalStatus = _smartThermalStatus.value
                if (isAppInForeground && !thermalStatus.reduceAnimationsActive) {
                    telemetryEngine.triggerLightweightFrameSample()
                }

                val shouldMeasurePing = (pingPollCycleCounter % 4 == 0)
                pingPollCycleCounter += 1

                val snap = telemetryEngine.sampleTelemetry(
                    activeMode = _performanceMode.value,
                    measureNetworkPing = shouldMeasurePing
                )
                _telemetry.value = snap

                val evaluatedThermal = telemetryEngine.evaluateSmartThermalStatus(
                    telemetry = snap,
                    mode = _lowEndConfig.value.smartThermalMode
                )
                _smartThermalStatus.value = evaluatedThermal

                val activeGameName = TriggerEventBus.activeGameTitle.value
                    ?: _lastSessionGameInfo.value?.title
                _autoSettingsRecommendation.value = telemetryEngine.buildAutoSettingsRecommendation(
                    telemetry = snap,
                    mode = _advisorPresetMode.value,
                    gameTitle = activeGameName
                )

                val delayMs = if (isAppInForeground) {
                    evaluatedThermal.effectivePollIntervalMs
                } else {
                    (evaluatedThermal.effectivePollIntervalMs * 2L).coerceAtLeast(10000L)
                }
                delay(delayMs)
            }
        }
    }

    private fun observeBusStateChanges() {
        viewModelScope.launch {
            TriggerEventBus.clonedButtonsConfig.collect { busConfig ->
                if (busConfig != _clonedButtonsConfig.value) {
                    _clonedButtonsConfig.value = busConfig
                    prefsRepo.saveClonedButtonsConfig(busConfig)
                }
            }
        }
        viewModelScope.launch {
            TriggerEventBus.activePerformanceMode.collect { mode ->
                if (mode != _performanceMode.value) {
                    _performanceMode.value = mode
                    prefsRepo.savePerformanceMode(mode)
                }
            }
        }
        viewModelScope.launch {
            TriggerEventBus.crosshairConfig.collect { ch ->
                if (ch != _crosshairConfig.value) {
                    _crosshairConfig.value = ch
                    prefsRepo.saveCrosshairConfig(ch)
                }
            }
        }
    }

    fun selectTab(tab: RedCoreTab) {
        _selectedTab.value = tab
    }

    fun setEdgeGenieExpanded(expanded: Boolean) {
        _edgeGenieExpanded.value = expanded
    }

    fun dismissStatusBanner() {
        _statusBannerMessage.value = null
    }

    private fun postBanner(message: String) {
        _statusBannerMessage.value = message
    }

    // -------------------------------------------------------------------------
    // ADD GAME & MY GAMES MANAGEMENT (UNIVERSAL GAMING CENTER)
    // -------------------------------------------------------------------------

    fun openAddGameSheet() {
        _addGameSearchQuery.value = ""
        _showAddGameSheet.value = true
        viewModelScope.launch {
            val addedPkgs = _gameSpaceCatalog.value.map { it.packageName }.toSet()
            _installedAppCandidates.value = boosterManager.scanInstalledLaunchableApps(addedPkgs)
        }
    }

    fun closeAddGameSheet() {
        _showAddGameSheet.value = false
    }

    fun setAddGameSearchQuery(query: String) {
        _addGameSearchQuery.value = query
    }

    fun setAddGameFilterOnlyGames(onlyGames: Boolean) {
        _addGameFilterOnlyGames.value = onlyGames
    }

    fun addInstalledCandidateToMyGames(candidate: InstalledAppCandidate) {
        val current = _gameSpaceCatalog.value
        if (current.any { it.packageName == candidate.packageName }) {
            postBanner("اللعبة (${candidate.title}) مضافة بالفعل في قائمة ألعابي MY GAMES")
            return
        }
        val newProfile = boosterManager.createProfileFromCandidate(candidate)
        val updated = listOf(newProfile) + current
        _gameSpaceCatalog.value = updated
        prefsRepo.saveGameProfiles(updated)

        val addedPkgs = updated.map { it.packageName }.toSet()
        _installedAppCandidates.value = _installedAppCandidates.value.map {
            it.copy(isAddedToMyGames = addedPkgs.contains(it.packageName))
        }
        postBanner("تمت إضافة (${candidate.title}) إلى MY GAMES بنجاح مع ملف إعدادات مخصص!")
    }

    fun addCustomGameToMyGames(
        title: String,
        packageName: String,
        genreAr: String = "لعبة مخصصة • Custom Profile",
        recommendedMode: PerformanceMode = PerformanceMode.DIABLO,
        recommendedFps: Int = 120
    ) {
        val cleanTitle = title.trim()
        val cleanPkg = packageName.trim().ifBlank {
            "com.custom.game." + cleanTitle.lowercase().replace(Regex("[^a-z0-9]"), "")
                .ifBlank { System.currentTimeMillis().toString() }
        }
        if (cleanTitle.isBlank()) return

        val current = _gameSpaceCatalog.value
        if (current.any { it.packageName.equals(cleanPkg, ignoreCase = true) }) {
            postBanner("اللعبة ($cleanTitle) موجودة بالفعل في MY GAMES")
            return
        }

        val id = "custom_" + cleanPkg.replace('.', '_')
        val newProfile = GameSpaceProfile(
            id = id,
            title = cleanTitle,
            packageName = cleanPkg,
            genreAr = genreAr.ifBlank { "لعبة مضافة يدوياً" },
            recommendedMode = recommendedMode,
            recommendedFps = recommendedFps,
            accentHex = 0xFFFF1744L,
            isInstalled = boosterManager.isPackageInstalled(cleanPkg),
            isUserAdded = true,
            autoCleanRamBeforeLaunch = true,
            notificationShieldEnabled = true,
            crosshairEnabled = false,
            fpsOverlayEnabled = true,
            sidebarOverlayEnabled = true,
            brightnessLockEnabled = true,
            touchGuardEnabled = true,
            wiFiPriorityEnabled = true,
            preferredAdvisorMode = AdvisorPresetMode.BEST_PERFORMANCE
        )

        val updated = listOf(newProfile) + current
        _gameSpaceCatalog.value = updated
        prefsRepo.saveGameProfiles(updated)
        _showAddGameSheet.value = false
        postBanner("تمت إضافة ($cleanTitle) إلى قائمة MY GAMES بنجاح!")
    }

    fun removeGameFromMyGames(gameId: String) {
        val removed = _gameSpaceCatalog.value.firstOrNull { it.id == gameId }
        val updated = _gameSpaceCatalog.value.filterNot { it.id == gameId }
        _gameSpaceCatalog.value = updated
        prefsRepo.saveGameProfiles(updated)

        if (_selectedProfileForSheet.value?.id == gameId) {
            _selectedProfileForSheet.value = null
        }

        val addedPkgs = updated.map { it.packageName }.toSet()
        _installedAppCandidates.value = _installedAppCandidates.value.map {
            it.copy(isAddedToMyGames = addedPkgs.contains(it.packageName))
        }
        if (removed != null) {
            postBanner("تم حذف (${removed.title}) من قائمة MY GAMES.")
        }
    }

    // -------------------------------------------------------------------------
    // ONE-TAP GAME PREPARATION & PERFORMANCE MODES
    // -------------------------------------------------------------------------

    fun selectPerformanceMode(mode: PerformanceMode) {
        _performanceMode.value = mode
        prefsRepo.savePerformanceMode(mode)
        TriggerEventBus.updatePerformanceMode(mode)
        postBanner("تم تفعيل ${mode.titleAr} (${mode.badgeText})")
    }

    fun executeOneTapGamePreparation(targetProfile: GameSpaceProfile? = null) {
        if (_isPreparingGame.value) return
        viewModelScope.launch {
            _isPreparingGame.value = true
            TriggerEventBus.setMasterEngineRunning(true)

            val snapBefore = telemetryEngine.sampleTelemetry(
                activeMode = _performanceMode.value,
                measureNetworkPing = false
            )
            val ramBefore = snapBefore.ramAvailableMb

            val targetMode = targetProfile?.recommendedMode ?: PerformanceMode.DIABLO
            _performanceMode.value = targetMode
            prefsRepo.savePerformanceMode(targetMode)
            TriggerEventBus.updatePerformanceMode(targetMode)

            val currentThermalMode = if (_lowEndConfig.value.smartThermalMode == SmartThermalMode.OFF) {
                SmartThermalMode.AUTO_ADAPTIVE
            } else {
                _lowEndConfig.value.smartThermalMode
            }

            val enableShield = targetProfile?.notificationShieldEnabled ?: true
            val shieldState = boosterManager.applyNotificationShield(enableShield)
            _notificationShieldState.value = shieldState

            val updatedLowEnd = _lowEndConfig.value.copy(
                smartThermalMode = currentThermalMode,
                blockHeadsUpNotifications = enableShield,
                wiFiPriorityEnabled = targetProfile?.wiFiPriorityEnabled ?: true,
                brightnessLockEnabled = targetProfile?.brightnessLockEnabled
                    ?: _lowEndConfig.value.brightnessLockEnabled,
                touchEdgeRejectEnabled = targetProfile?.touchGuardEnabled
                    ?: _lowEndConfig.value.touchEdgeRejectEnabled
            )
            _lowEndConfig.value = updatedLowEnd
            prefsRepo.saveLowEndConfig(updatedLowEnd)
            TriggerEventBus.updateLowEndConfig(updatedLowEnd)

            val (cleanedProcs, boostReport) = boosterManager.performDeepBoost(
                processes = _optimizableProcesses.value,
                whitelistedPackages = _whitelistedPackages.value,
                currentPingMs = snapBefore.pingMs
            )
            _optimizableProcesses.value = cleanedProcs
            _lastBoostReport.value = boostReport
            telemetryEngine.notifyCacheCleaned(boostReport.cleanedCacheMb)

            val (livePing, _) = telemetryEngine.forceMeasureNetworkPing()
            val snapAfter = telemetryEngine.sampleTelemetry(
                activeMode = targetMode,
                measureNetworkPing = false
            )
            _telemetry.value = snapAfter
            _smartThermalStatus.value = telemetryEngine.evaluateSmartThermalStatus(
                telemetry = snapAfter,
                mode = currentThermalMode
            )

            val targetAdvisor = targetProfile?.preferredAdvisorMode ?: AdvisorPresetMode.BEST_PERFORMANCE
            _advisorPresetMode.value = targetAdvisor
            prefsRepo.saveAdvisorPresetMode(targetAdvisor)
            _autoSettingsRecommendation.value = telemetryEngine.buildAutoSettingsRecommendation(
                telemetry = snapAfter,
                mode = targetAdvisor,
                gameTitle = targetProfile?.title
            )

            val ramAfter = (ramBefore + boostReport.freedRamMb).coerceAtMost(snapAfter.ramTotalMb - 450L)
            val steps = buildList {
                add("تحرير ${boostReport.freedRamMb}MB من الرام وتنظيف ${boostReport.cleanedCacheMb}MB كاش مؤقت")
                add("إيقاف ${boostReport.stoppedProcessesCount} مهام خلفية غير ضرورية لتخفيف حمل المعالج")
                add("ضبط الأداء على ${targetMode.titleAr} والحماية الحرارية على ${currentThermalMode.titleAr}")
                if (enableShield) {
                    add(shieldState.statusLabelAr)
                }
                add("فحص استقرار الشبكة: البنق الحالي ${livePing}ms جاهز للعب التنافسي")
            }

            val prepReport = GamePrepReport(
                timestampMs = System.currentTimeMillis(),
                targetGameTitle = targetProfile?.title ?: "جميع الألعاب (تجهيز شامل)",
                ramBeforeMb = ramBefore,
                ramAfterMb = ramAfter,
                freedRamMb = boostReport.freedRamMb,
                cleanedCacheMb = boostReport.cleanedCacheMb,
                trimmedAppsCount = boostReport.stoppedProcessesCount,
                appliedPerformanceMode = targetMode,
                appliedThermalMode = currentThermalMode,
                notificationShieldActive = enableShield,
                pingMs = livePing,
                batteryTempCelsius = snapAfter.batteryTempCelsius,
                stepsCompletedAr = steps
            )
            _lastGamePrepReport.value = prepReport
            _isPreparingGame.value = false

            postBanner("اكتمل تجهيز الجهاز للعب! تم تحرير ${boostReport.freedRamMb}MB رام وتفعيل ${targetMode.badgeText}")
        }
    }

    // -------------------------------------------------------------------------
    // SMART THERMAL MODE & AUTO SETTINGS ADVISOR
    // -------------------------------------------------------------------------

    fun setSmartThermalMode(mode: SmartThermalMode) {
        val updated = _lowEndConfig.value.copy(
            smartThermalMode = mode,
            thermalGuardEnabled = mode != SmartThermalMode.OFF
        )
        updateAndPersistLowEndConfig(updated)
        _smartThermalStatus.value = telemetryEngine.evaluateSmartThermalStatus(
            telemetry = _telemetry.value,
            mode = mode
        )
        postBanner("تم ضبط النظام الحراري على: ${mode.titleAr}")
    }

    fun selectAdvisorPresetMode(mode: AdvisorPresetMode) {
        _advisorPresetMode.value = mode
        prefsRepo.saveAdvisorPresetMode(mode)
        val activeGameName = TriggerEventBus.activeGameTitle.value
            ?: _lastSessionGameInfo.value?.title
        _autoSettingsRecommendation.value = telemetryEngine.buildAutoSettingsRecommendation(
            telemetry = _telemetry.value,
            mode = mode,
            gameTitle = activeGameName
        )
    }

    fun applyAdvisorRecommendationToEngine() {
        val recMode = _advisorPresetMode.value
        val targetPerf = when (recMode) {
            AdvisorPresetMode.BEST_PERFORMANCE -> PerformanceMode.DIABLO
            AdvisorPresetMode.BALANCED -> PerformanceMode.PERFORMANCE
            AdvisorPresetMode.BEST_VISUAL_QUALITY -> PerformanceMode.BALANCED
        }
        val targetRes = when (recMode) {
            AdvisorPresetMode.BEST_PERFORMANCE -> GfxResolutionPreset.RES_720P
            AdvisorPresetMode.BALANCED -> GfxResolutionPreset.RES_900P
            AdvisorPresetMode.BEST_VISUAL_QUALITY -> GfxResolutionPreset.RES_1080P
        }
        selectPerformanceMode(targetPerf)
        selectGfxResolution(targetRes)
        postBanner("تم تطبيق توصيات مستشار الإعدادات (${recMode.titleAr}) بنجاح!")
    }

    // -------------------------------------------------------------------------
    // NOTIFICATION SHIELD
    // -------------------------------------------------------------------------

    fun toggleNotificationShield() {
        val next = !_lowEndConfig.value.blockHeadsUpNotifications
        val updated = _lowEndConfig.value.copy(blockHeadsUpNotifications = next)
        updateAndPersistLowEndConfig(updated)
        val state = boosterManager.applyNotificationShield(next)
        _notificationShieldState.value = state
        postBanner(state.statusLabelAr)
    }

    // -------------------------------------------------------------------------
    // PER-GAME PROFILES & START GAME WORKFLOW
    // -------------------------------------------------------------------------

    fun openGameProfileSheet(profile: GameSpaceProfile) {
        _selectedProfileForSheet.value = profile
    }

    fun closeGameProfileSheet() {
        _selectedProfileForSheet.value = null
    }

    fun updateGameProfile(updatedProfile: GameSpaceProfile) {
        val updatedList = _gameSpaceCatalog.value.map {
            if (it.id == updatedProfile.id) updatedProfile else it
        }
        _gameSpaceCatalog.value = updatedList
        _selectedProfileForSheet.value = updatedProfile
        prefsRepo.saveGameProfiles(updatedList)
        postBanner("تم حفظ إعدادات ملف اللعبة (${updatedProfile.title}) بنجاح")
    }

    fun applyProfileWithoutLaunching(profile: GameSpaceProfile) {
        applyGameProfileSettingsInternal(profile)
        postBanner("تم تفعيل إعدادات (${profile.title}) داخل Gaming Mode!")
    }

    private fun applyGameProfileSettingsInternal(profile: GameSpaceProfile) {
        TriggerEventBus.setMasterEngineRunning(true)
        TriggerEventBus.setActiveGameSession(profile.packageName, profile.title)

        _performanceMode.value = profile.recommendedMode
        prefsRepo.savePerformanceMode(profile.recommendedMode)
        TriggerEventBus.updatePerformanceMode(profile.recommendedMode)

        val shieldState = boosterManager.applyNotificationShield(profile.notificationShieldEnabled)
        _notificationShieldState.value = shieldState

        val updatedLowEnd = _lowEndConfig.value.copy(
            blockHeadsUpNotifications = profile.notificationShieldEnabled,
            brightnessLockEnabled = profile.brightnessLockEnabled,
            touchEdgeRejectEnabled = profile.touchGuardEnabled,
            wiFiPriorityEnabled = profile.wiFiPriorityEnabled
        )
        updateAndPersistLowEndConfig(updatedLowEnd)

        val updatedCrosshair = _crosshairConfig.value.copy(enabled = profile.crosshairEnabled)
        _crosshairConfig.value = updatedCrosshair
        prefsRepo.saveCrosshairConfig(updatedCrosshair)
        TriggerEventBus.updateCrosshairConfig(updatedCrosshair)

        _fpsHudOverlayEnabled.value = profile.fpsOverlayEnabled
        _advisorPresetMode.value = profile.preferredAdvisorMode
        prefsRepo.saveAdvisorPresetMode(profile.preferredAdvisorMode)
        _autoSettingsRecommendation.value = telemetryEngine.buildAutoSettingsRecommendation(
            telemetry = _telemetry.value,
            mode = profile.preferredAdvisorMode,
            gameTitle = profile.title
        )

        val needsOverlayService = profile.sidebarOverlayEnabled ||
            profile.crosshairEnabled ||
            profile.fpsOverlayEnabled ||
            _clonedButtonsConfig.value.enabled

        if (boosterManager.canDrawOverlays()) {
            if (needsOverlayService) {
                startFloatingSidebarOverlay(showFpsHud = profile.fpsOverlayEnabled)
            } else {
                stopFloatingSidebarOverlay()
            }
        }
    }

    /**
     * Triggered when the user taps START on any game in MY GAMES:
     * 1. Prepares Gaming Mode.
     * 2. Activates the game's custom profile settings.
     * 3. Starts ONLY required services (e.g. Sidebar/Crosshair/FPS overlay if enabled in profile).
     * 4. Opens the selected game via PackageManager.
     * 5. Keeps Gaming Mode tools ready during gameplay.
     */
    fun launchOrSimulateGame(profile: GameSpaceProfile) {
        viewModelScope.launch {
            applyGameProfileSettingsInternal(profile)

            if (profile.autoCleanRamBeforeLaunch) {
                val (cleanedProcs, report) = boosterManager.performDeepBoost(
                    processes = _optimizableProcesses.value,
                    whitelistedPackages = _whitelistedPackages.value,
                    currentPingMs = _telemetry.value.pingMs
                )
                _optimizableProcesses.value = cleanedProcs
                _lastBoostReport.value = report
                telemetryEngine.notifyCacheCleaned(report.cleanedCacheMb)
            }

            val now = System.currentTimeMillis()
            prefsRepo.saveLastSessionGame(
                gameId = profile.id,
                title = profile.title,
                packageName = profile.packageName,
                timestampMs = now,
                mode = profile.recommendedMode
            )
            refreshLastSessionGameInfo()

            val launched = boosterManager.launchGame(profile.packageName)
            if (launched) {
                postBanner("تم تجهيز Gaming Mode وتشغيل ${profile.title} بأقصى سرعة!")
            } else {
                postBanner(
                    "تم تفعيل Gaming Mode وإعدادات (${profile.title})! لتشغيل لعبة مثبتة فعلياً على جهازك اضغط (+ ADD GAME) واخترها من القائمة."
                )
            }
        }
    }

    fun quickReconnectLastGame() {
        val lastInfo = _lastSessionGameInfo.value ?: return
        val matchingProfile = _gameSpaceCatalog.value.firstOrNull {
            it.id == lastInfo.gameId || it.packageName == lastInfo.packageName
        }
        if (matchingProfile != null) {
            launchOrSimulateGame(matchingProfile)
        } else {
            val launched = boosterManager.launchGame(lastInfo.packageName)
            if (launched) {
                postBanner("تمت العودة الفورية إلى ${lastInfo.title}!")
            } else {
                postBanner("تمت استعادة جلسة ${lastInfo.title} وتنشيط الشبكة والرام.")
            }
        }
    }

    private fun refreshLastSessionGameInfo() {
        val raw = prefsRepo.loadLastSessionGameRaw()
        if (raw == null) {
            val firstInstalled = _gameSpaceCatalog.value.firstOrNull { it.isInstalled }
                ?: _gameSpaceCatalog.value.firstOrNull()
            if (firstInstalled != null) {
                val state = boosterManager.inspectQuickReconnectState(firstInstalled.packageName)
                _lastSessionGameInfo.value = LastSessionGameInfo(
                    gameId = firstInstalled.id,
                    title = firstInstalled.title,
                    packageName = firstInstalled.packageName,
                    lastLaunchedTimestampMs = System.currentTimeMillis(),
                    appliedMode = firstInstalled.recommendedMode,
                    reconnectState = state,
                    statusDetailAr = when (state) {
                        QuickReconnectState.ACTIVE_IN_MEMORY ->
                            "اللعبة نشطة في الرام — يمكنك العودة إليها فوراً دون إعادة تحميل"
                        QuickReconnectState.READY_TO_LAUNCH ->
                            "اللعبة مثبتة على الجهاز وجاهزة للتشغيل السريع مع استعادة الإعدادات"
                        QuickReconnectState.PROFILE_READY_SIMULATION ->
                            "ملف الإعدادات جاهز — اضغط لتفعيل ملف اللعبة أو اختر لعبة مثبتة من + ADD GAME"
                    }
                )
            }
            return
        }

        val (id, title, pkg) = raw
        val (timestamp, mode) = prefsRepo.loadLastSessionMeta()
        val state = boosterManager.inspectQuickReconnectState(pkg)
        _lastSessionGameInfo.value = LastSessionGameInfo(
            gameId = id,
            title = title,
            packageName = pkg,
            lastLaunchedTimestampMs = timestamp,
            appliedMode = mode,
            reconnectState = state,
            statusDetailAr = when (state) {
                QuickReconnectState.ACTIVE_IN_MEMORY ->
                    "اللعبة ما زالت نشطة في الذاكرة — اضغط للعودة الفورية واستئناف المباراة"
                QuickReconnectState.READY_TO_LAUNCH ->
                    "اللعبة مثبتة وجاهزة — اضغط لإعادة تشغيلها مع تفعيل ${mode.badgeText} وتنشيط الشبكة"
                QuickReconnectState.PROFILE_READY_SIMULATION ->
                    "ملف $title محفوظ — اضغط لاستعادة إعدادات الجلسة الأخيرة فوراً"
            }
        )
    }

    // -------------------------------------------------------------------------
    // RAM BOOSTER & PROCESS TRIMMER
    // -------------------------------------------------------------------------

    fun executeSuperBoost() {
        if (_isBoosting.value) return
        viewModelScope.launch {
            _isBoosting.value = true
            val (updatedProcs, report) = boosterManager.performDeepBoost(
                processes = _optimizableProcesses.value,
                whitelistedPackages = _whitelistedPackages.value,
                currentPingMs = _telemetry.value.pingMs
            )
            _optimizableProcesses.value = updatedProcs
            _lastBoostReport.value = report
            telemetryEngine.notifyCacheCleaned(report.cleanedCacheMb)

            val snap = telemetryEngine.sampleTelemetry(_performanceMode.value, measureNetworkPing = true)
            _telemetry.value = snap
            _isBoosting.value = false
            postBanner(report.summaryMessageAr)
        }
    }

    fun toggleProcessWhitelist(packageName: String) {
        val current = _whitelistedPackages.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        _whitelistedPackages.value = current
        prefsRepo.saveWhitelistedPackages(current)
        _optimizableProcesses.value = _optimizableProcesses.value.map {
            if (it.packageName == packageName) it.copy(isWhitelisted = current.contains(packageName))
            else it
        }
    }

    // -------------------------------------------------------------------------
    // ON-SCREEN TOUCH BUTTON CLONER (C1-C4)
    // -------------------------------------------------------------------------

    fun toggleClonedButtonsMaster(enabled: Boolean) {
        val updated = _clonedButtonsConfig.value.copy(enabled = enabled)
        updateAndPersistClonedConfig(updated)
        if (enabled && boosterManager.canDrawOverlays()) {
            startFloatingSidebarOverlay(showFpsHud = _fpsHudOverlayEnabled.value)
        }
        postBanner(
            if (enabled) "تم تفعيل استنساخ الأزرار اللمسية على الشاشة (C1–C4)"
            else "تم إيقاف الأزرار المستنسخة (C1–C4)"
        )
    }

    fun selectClonedButtonForEdit(buttonId: Int) {
        _selectedClonedButtonId.value = buttonId
    }

    fun toggleSingleClonedButton(buttonId: Int, enabled: Boolean) {
        val updatedBtns = _clonedButtonsConfig.value.buttons.map {
            if (it.id == buttonId) it.copy(enabled = enabled) else it
        }
        updateAndPersistClonedConfig(_clonedButtonsConfig.value.copy(buttons = updatedBtns))
    }

    fun setClonedButtonMode(buttonId: Int, mode: ClonedButtonMode) {
        val updatedBtns = _clonedButtonsConfig.value.buttons.map {
            if (it.id == buttonId) it.copy(mode = mode) else it
        }
        updateAndPersistClonedConfig(_clonedButtonsConfig.value.copy(buttons = updatedBtns))
    }

    fun updateClonedButtonSourceCoord(buttonId: Int, x: Float, y: Float) {
        val updatedBtns = _clonedButtonsConfig.value.buttons.map {
            if (it.id == buttonId) {
                it.copy(
                    sourceX = x.coerceIn(0.05f, 0.95f),
                    sourceY = y.coerceIn(0.08f, 0.92f)
                )
            } else {
                it
            }
        }
        updateAndPersistClonedConfig(_clonedButtonsConfig.value.copy(buttons = updatedBtns))
    }

    fun updateClonedButtonTargetCoord(buttonId: Int, x: Float, y: Float) {
        val updatedBtns = _clonedButtonsConfig.value.buttons.map {
            if (it.id == buttonId) {
                it.copy(
                    targetX = x.coerceIn(0.05f, 0.95f),
                    targetY = y.coerceIn(0.08f, 0.92f)
                )
            } else {
                it
            }
        }
        updateAndPersistClonedConfig(_clonedButtonsConfig.value.copy(buttons = updatedBtns))
    }

    fun updateClonedButtonSize(buttonId: Int, sizeDp: Int) {
        val updatedBtns = _clonedButtonsConfig.value.buttons.map {
            if (it.id == buttonId) it.copy(buttonSizeDp = sizeDp.coerceIn(38, 80)) else it
        }
        updateAndPersistClonedConfig(_clonedButtonsConfig.value.copy(buttons = updatedBtns))
    }

    fun toggleClonedPositionsLocked() {
        val next = !_clonedButtonsConfig.value.editPositionsLocked
        updateAndPersistClonedConfig(_clonedButtonsConfig.value.copy(editPositionsLocked = next))
    }

    fun toggleClonedConnectionLines() {
        val next = !_clonedButtonsConfig.value.showConnectionLines
        updateAndPersistClonedConfig(_clonedButtonsConfig.value.copy(showConnectionLines = next))
    }

    fun simulateClonedButtonTap(buttonId: Int) {
        val btn = _clonedButtonsConfig.value.buttons.firstOrNull { it.id == buttonId } ?: return
        viewModelScope.launch {
            TriggerEventBus.emitClonedButtonTap(btn, isDown = true)
            delay(110L)
            TriggerEventBus.emitClonedButtonTap(btn, isDown = false)
        }
    }

    fun resetClonedButtonsToDefault() {
        val reset = ClonedButtonsConfig(
            enabled = _clonedButtonsConfig.value.enabled,
            buttons = ClonedButtonsConfig.defaultClonedButtons()
        )
        updateAndPersistClonedConfig(reset)
        postBanner("تمت استعادة المواقع الافتراضية للأزرار المستنسخة C1–C4")
    }

    private fun updateAndPersistClonedConfig(config: ClonedButtonsConfig) {
        _clonedButtonsConfig.value = config
        prefsRepo.saveClonedButtonsConfig(config)
        TriggerEventBus.updateClonedButtonsConfig(config)
    }

    // -------------------------------------------------------------------------
    // CROSSHAIR, GFX & ARSENAL TOOLS
    // -------------------------------------------------------------------------

    fun toggleCrosshair(enabled: Boolean) {
        val updated = _crosshairConfig.value.copy(enabled = enabled)
        updateAndPersistCrosshair(updated)
        if (enabled && boosterManager.canDrawOverlays()) {
            startFloatingSidebarOverlay(showFpsHud = _fpsHudOverlayEnabled.value)
        }
    }

    fun selectCrosshairStyle(style: CrosshairStyle) {
        updateAndPersistCrosshair(_crosshairConfig.value.copy(style = style))
    }

    fun updateCrosshairSize(sizeDp: Float) {
        updateAndPersistCrosshair(_crosshairConfig.value.copy(sizeDp = sizeDp.coerceIn(14f, 56f)))
    }

    fun updateCrosshairStroke(strokeDp: Float) {
        updateAndPersistCrosshair(_crosshairConfig.value.copy(strokeWidthDp = strokeDp.coerceIn(1f, 5f)))
    }

    fun updateCrosshairColor(colorHex: Long) {
        updateAndPersistCrosshair(_crosshairConfig.value.copy(colorHex = colorHex))
    }

    fun updateCrosshairOffset(offsetX: Float, offsetY: Float) {
        updateAndPersistCrosshair(
            _crosshairConfig.value.copy(
                offsetXDp = offsetX.coerceIn(-80f, 80f),
                offsetYDp = offsetY.coerceIn(-80f, 80f)
            )
        )
    }

    private fun updateAndPersistCrosshair(config: CrosshairConfig) {
        _crosshairConfig.value = config
        prefsRepo.saveCrosshairConfig(config)
        TriggerEventBus.updateCrosshairConfig(config)
    }

    fun selectGfxResolution(preset: GfxResolutionPreset) {
        _gfxResolution.value = preset
        prefsRepo.saveGfxResolution(preset)
    }

    fun selectTouchSamplingRate(rate: TouchSamplingRate) {
        _touchSamplingRate.value = rate
        prefsRepo.saveTouchSamplingRate(rate)
    }

    fun toggleLowEndBoost(enabled: Boolean) {
        val updated = _lowEndConfig.value.copy(
            lowEndBoostEnabled = enabled,
            smartThermalMode = if (enabled) SmartThermalMode.ECO_STABILITY else SmartThermalMode.AUTO_ADAPTIVE,
            disableHeavyShaders = enabled
        )
        updateAndPersistLowEndConfig(updated)
        _smartThermalStatus.value = telemetryEngine.evaluateSmartThermalStatus(
            telemetry = _telemetry.value,
            mode = updated.smartThermalMode
        )
    }

    fun toggleTouchEdgeReject(enabled: Boolean) {
        updateAndPersistLowEndConfig(_lowEndConfig.value.copy(touchEdgeRejectEnabled = enabled))
    }

    fun toggleBrightnessLock(enabled: Boolean) {
        updateAndPersistLowEndConfig(_lowEndConfig.value.copy(brightnessLockEnabled = enabled))
    }

    fun setLockedBrightnessPercent(percent: Int) {
        updateAndPersistLowEndConfig(
            _lowEndConfig.value.copy(lockedBrightnessPercent = percent.coerceIn(25, 100))
        )
    }

    fun toggleWiFiPriority(enabled: Boolean) {
        updateAndPersistLowEndConfig(_lowEndConfig.value.copy(wiFiPriorityEnabled = enabled))
    }

    private fun updateAndPersistLowEndConfig(config: LowEndDeviceConfig) {
        _lowEndConfig.value = config
        prefsRepo.saveLowEndConfig(config)
        TriggerEventBus.updateLowEndConfig(config)
    }

    // -------------------------------------------------------------------------
    // FLOATING OVERLAY & UNIFIED START/STOP ENGINE CONTROL
    // -------------------------------------------------------------------------

    fun toggleFpsHudOverlay(enabled: Boolean) {
        _fpsHudOverlayEnabled.value = enabled
        if (boosterManager.canDrawOverlays()) {
            startFloatingSidebarOverlay(showFpsHud = enabled)
        } else if (enabled) {
            postBanner("يرجى منح صلاحية الظهور فوق التطبيقات لعرض شريط FPS العائم")
        }
    }

    fun startFloatingSidebarOverlay(showFpsHud: Boolean = _fpsHudOverlayEnabled.value) {
        val app = getApplication<Application>()
        if (!boosterManager.canDrawOverlays()) {
            postBanner("امنح صلاحية الظهور فوق التطبيقات لتشغيل الشريط الجانبي داخل الألعاب")
            return
        }
        TriggerEventBus.setMasterEngineRunning(true)
        val intent = Intent(app, FloatingGameSidebarService::class.java).apply {
            action = FloatingGameSidebarService.ACTION_SET_FPS_HUD
            putExtra(FloatingGameSidebarService.EXTRA_SHOW_FPS_HUD, showFpsHud)
        }
        try {
            app.startService(intent)
        } catch (_: Exception) {
        }
    }

    fun stopFloatingSidebarOverlay() {
        val app = getApplication<Application>()
        try {
            app.stopService(Intent(app, FloatingGameSidebarService::class.java))
        } catch (_: Exception) {
        }
        TriggerEventBus.setOverlayRunning(false)
    }

    fun toggleMasterEngineOrExit(onExitRequested: () -> Unit) {
        if (isMasterEngineRunning.value) {
            stopAllBackgroundWorkAndExit(onExitRequested)
        } else {
            startMasterEngine()
        }
    }

    fun clearLastSessionRecord() {
        _lastSessionGameInfo.value = null
    }

    fun testNetworkPingNow() {
        viewModelScope.launch {
            val (ping, _) = telemetryEngine.forceMeasureNetworkPing()
            val snap = telemetryEngine.sampleTelemetry(_performanceMode.value, measureNetworkPing = false)
            _telemetry.value = snap
            postBanner("تم فحص سرعة استجابة الشبكة: Ping ${ping}ms")
        }
    }

    fun updateClonedButtonsTransform(transform: (ClonedButtonsConfig) -> ClonedButtonsConfig) {
        updateAndPersistClonedConfig(transform(_clonedButtonsConfig.value))
    }

    fun updateCrosshairTransform(transform: (CrosshairConfig) -> CrosshairConfig) {
        val next = transform(_crosshairConfig.value)
        updateAndPersistCrosshair(next)
        if (next.enabled && boosterManager.canDrawOverlays()) {
            startFloatingSidebarOverlay(showFpsHud = _fpsHudOverlayEnabled.value)
        }
    }

    fun updateLowEndTransform(transform: (LowEndDeviceConfig) -> LowEndDeviceConfig) {
        updateAndPersistLowEndConfig(transform(_lowEndConfig.value))
    }

    fun emitClonedTap(button: com.example.model.ClonedTouchButton, isDown: Boolean) {
        TriggerEventBus.emitClonedButtonTap(button, isDown)
    }

    fun openOverlayPermissionSettings() {
        try {
            val intent = Intent(
                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:${getApplication<Application>().packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun openAccessibilitySettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun openDndPermissionSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun startMasterEngine() {
        TriggerEventBus.setMasterEngineRunning(true)
        if (telemetryLoopJob?.isActive != true) {
            startAdaptiveTelemetryLoop()
        }
        postBanner("تم تشغيل محرك Hassan Games بنجاح!")
    }

    fun stopAllBackgroundWorkAndExit(onExitRequested: () -> Unit) {
        telemetryLoopJob?.cancel()
        telemetryEngine.stopFrameMonitor()
        boosterManager.restoreAllSessionControls()
        _notificationShieldState.value = NotificationShieldState(enabled = false)

        val disabledCrosshair = _crosshairConfig.value.copy(enabled = false)
        _crosshairConfig.value = disabledCrosshair
        prefsRepo.saveCrosshairConfig(disabledCrosshair)

        val disabledCloned = _clonedButtonsConfig.value.copy(enabled = false)
        _clonedButtonsConfig.value = disabledCloned
        prefsRepo.saveClonedButtonsConfig(disabledCloned)

        TriggerEventBus.requestCompleteShutdown()
        stopFloatingSidebarOverlay()

        onExitRequested()
    }

    override fun onCleared() {
        telemetryLoopJob?.cancel()
        telemetryEngine.stopFrameMonitor()
        super.onCleared()
    }
}
