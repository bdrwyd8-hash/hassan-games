package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AudioRadarPreset
import com.example.model.AdvisorPresetMode
import com.example.model.ClonedButtonsConfig
import com.example.model.ClonedTouchButton
import com.example.model.CrosshairColorOption
import com.example.model.CrosshairConfig
import com.example.model.CrosshairStyle
import com.example.model.EdgeSidebarConfig
import com.example.model.GameSpaceProfile
import com.example.model.LowEndOptimizerConfig
import com.example.model.PerformanceMode
import com.example.model.ScreenVisionFilter
import com.example.model.ShoulderTriggerConfig
import com.example.model.SmartThermalMode
import com.example.model.TriggerFireMode
import com.example.model.VoiceModPreset
import com.example.model.defaultClonedTouchButtons
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "redcore_game_space_prefs")

class RedCorePreferencesRepository(private val context: Context) {

    private object Keys {
        val PERF_MODE = stringPreferencesKey("perf_mode")

        // L1 / R1 Shoulder Triggers
        val TRIGGERS_ENABLED = booleanPreferencesKey("triggers_enabled")
        val L1_X = floatPreferencesKey("l1_x")
        val L1_Y = floatPreferencesKey("l1_y")
        val L1_MODE = stringPreferencesKey("l1_mode")
        val L1_RPS = intPreferencesKey("l1_rps")
        val R1_X = floatPreferencesKey("r1_x")
        val R1_Y = floatPreferencesKey("r1_y")
        val R1_MODE = stringPreferencesKey("r1_mode")
        val R1_RPS = intPreferencesKey("r1_rps")
        val TRIGGER_HAPTIC = booleanPreferencesKey("trigger_haptic")
        val TRIGGER_SOUND = booleanPreferencesKey("trigger_sound")
        val TRIGGER_COMBO = booleanPreferencesKey("trigger_combo")

        // Crosshair
        val CROSSHAIR_IN_APP = booleanPreferencesKey("crosshair_in_app")
        val CROSSHAIR_SYSTEM = booleanPreferencesKey("crosshair_system")
        val CROSSHAIR_STYLE = stringPreferencesKey("crosshair_style")
        val CROSSHAIR_COLOR = stringPreferencesKey("crosshair_color")
        val CROSSHAIR_SIZE = floatPreferencesKey("crosshair_size")
        val CROSSHAIR_STROKE = floatPreferencesKey("crosshair_stroke")
        val CROSSHAIR_OPACITY = floatPreferencesKey("crosshair_opacity")
        val CROSSHAIR_OFFSET_X = floatPreferencesKey("crosshair_offset_x")
        val CROSSHAIR_OFFSET_Y = floatPreferencesKey("crosshair_offset_y")

        // Edge-Swipe Floating Sidebar
        val SIDEBAR_IN_APP = booleanPreferencesKey("sidebar_in_app")
        val SIDEBAR_SYSTEM = booleanPreferencesKey("sidebar_system")
        val SIDEBAR_RIGHT_EDGE = booleanPreferencesKey("sidebar_right_edge")
        val SIDEBAR_SHOW_FPS = booleanPreferencesKey("sidebar_show_fps")
        val SIDEBAR_OPACITY = floatPreferencesKey("sidebar_opacity")

        // Low-End Optimizer & Multi-Brand Features
        val AUTO_CLEAN_BG = booleanPreferencesKey("auto_clean_bg")
        val AUTO_CLEAN_INTERVAL = intPreferencesKey("auto_clean_interval")
        val RAM_THRESHOLD = intPreferencesKey("ram_threshold")
        val RES_PRESET = stringPreferencesKey("res_preset")
        val TOUCH_HZ = intPreferencesKey("touch_hz")
        val GPU_MSAA_OFF = booleanPreferencesKey("gpu_msaa_off")
        val SHADOW_DOWNSCALE = booleanPreferencesKey("shadow_downscale")
        val ZERO_LAG_NET = booleanPreferencesKey("zero_lag_net")
        val BLOCK_NOTIFS = booleanPreferencesKey("block_notifs")
        val LOCK_BRIGHTNESS = booleanPreferencesKey("lock_brightness")
        val FAN_SPEED = intPreferencesKey("fan_speed")
        val VISION_FILTER = stringPreferencesKey("vision_filter")
        val AUDIO_RADAR = stringPreferencesKey("audio_radar")
        val VOICE_MOD = stringPreferencesKey("voice_mod")
        val MISTOUCH_PREV = booleanPreferencesKey("mistouch_prev")
        val BYPASS_CHARGE = booleanPreferencesKey("bypass_charge")
        val CUSTOM_BRIGHTNESS = intPreferencesKey("custom_brightness")
        val GAME_MEDIA_VOL = intPreferencesKey("game_media_vol")
        val DNS_PRESET = stringPreferencesKey("dns_preset")
        val AUTO_RESTORE_EXIT = booleanPreferencesKey("auto_restore_exit")
        val SMART_THERMAL_MODE = stringPreferencesKey("smart_thermal_mode")
        val NOTIF_SHIELD_ENABLED = booleanPreferencesKey("notif_shield_enabled")
        val FPS_OVERLAY_ENABLED = booleanPreferencesKey("fps_overlay_enabled")
        val TEMP_OVERLAY_ENABLED = booleanPreferencesKey("temp_overlay_enabled")
        val RAM_OVERLAY_ENABLED = booleanPreferencesKey("ram_overlay_enabled")
        val MAGNIFIER_ENABLED = booleanPreferencesKey("magnifier_enabled")
        val MAGNIFIER_ZOOM = floatPreferencesKey("magnifier_zoom")
        val ADVISOR_MODE = stringPreferencesKey("advisor_mode")

        // Cloned Touch Buttons
        val CLONED_MASTER_ENABLED = booleanPreferencesKey("cloned_master_enabled")
        val CLONED_SYSTEM_OVERLAY = booleanPreferencesKey("cloned_system_overlay")
        val CLONED_LOCKED_PLAY = booleanPreferencesKey("cloned_locked_play")
        val CLONED_BTN_SIZE = floatPreferencesKey("cloned_btn_size")
        val CLONED_BTN_OPACITY = floatPreferencesKey("cloned_btn_opacity")
        val CLONED_SERIALIZED = stringPreferencesKey("cloned_serialized")

        // Per-Game Profiles & Active Session
        val PER_GAME_PROFILES_SERIALIZED = stringPreferencesKey("per_game_profiles_serialized")
        val LAST_ACTIVE_PROFILE_ID = stringPreferencesKey("last_active_profile_id")

        // Whitelist
        val WHITELISTED_PKGS = stringSetPreferencesKey("whitelisted_pkgs")
    }

    val performanceModeFlow: Flow<PerformanceMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.PERF_MODE] ?: PerformanceMode.BALANCE.id
        PerformanceMode.entries.find { it.id == raw } ?: PerformanceMode.BALANCE
    }

    val triggerConfigFlow: Flow<ShoulderTriggerConfig> = context.dataStore.data.map { prefs ->
        val l1ModeId = prefs[Keys.L1_MODE] ?: TriggerFireMode.SINGLE_TAP.id
        val r1ModeId = prefs[Keys.R1_MODE] ?: TriggerFireMode.RAPID_BURST.id
        ShoulderTriggerConfig(
            enabled = prefs[Keys.TRIGGERS_ENABLED] ?: true,
            l1XRatio = prefs[Keys.L1_X] ?: 0.24f,
            l1YRatio = prefs[Keys.L1_Y] ?: 0.36f,
            l1Mode = TriggerFireMode.entries.find { it.id == l1ModeId } ?: TriggerFireMode.SINGLE_TAP,
            l1BurstRps = prefs[Keys.L1_RPS] ?: 10,
            r1XRatio = prefs[Keys.R1_X] ?: 0.78f,
            r1YRatio = prefs[Keys.R1_Y] ?: 0.56f,
            r1Mode = TriggerFireMode.entries.find { it.id == r1ModeId } ?: TriggerFireMode.RAPID_BURST,
            r1BurstRps = prefs[Keys.R1_RPS] ?: 14,
            hapticFeedback = prefs[Keys.TRIGGER_HAPTIC] ?: true,
            soundEffect = prefs[Keys.TRIGGER_SOUND] ?: false,
            comboLinkLR = prefs[Keys.TRIGGER_COMBO] ?: false
        )
    }

    val crosshairConfigFlow: Flow<CrosshairConfig> = context.dataStore.data.map { prefs ->
        val styleId = prefs[Keys.CROSSHAIR_STYLE] ?: CrosshairStyle.CIRCLE_DOT.id
        val colorId = prefs[Keys.CROSSHAIR_COLOR] ?: CrosshairColorOption.CRIMSON.id
        CrosshairConfig(
            enabledInApp = prefs[Keys.CROSSHAIR_IN_APP] ?: true,
            systemOverlayEnabled = prefs[Keys.CROSSHAIR_SYSTEM] ?: false,
            style = CrosshairStyle.entries.find { it.id == styleId } ?: CrosshairStyle.CIRCLE_DOT,
            colorOption = CrosshairColorOption.entries.find { it.id == colorId } ?: CrosshairColorOption.CRIMSON,
            sizeDp = prefs[Keys.CROSSHAIR_SIZE] ?: 34f,
            strokeWidthDp = prefs[Keys.CROSSHAIR_STROKE] ?: 2.2f,
            opacity = prefs[Keys.CROSSHAIR_OPACITY] ?: 0.92f,
            offsetX = prefs[Keys.CROSSHAIR_OFFSET_X] ?: 0f,
            offsetY = prefs[Keys.CROSSHAIR_OFFSET_Y] ?: 0f
        )
    }

    val edgeSidebarConfigFlow: Flow<EdgeSidebarConfig> = context.dataStore.data.map { prefs ->
        EdgeSidebarConfig(
            enabledInApp = prefs[Keys.SIDEBAR_IN_APP] ?: true,
            systemFloatingEnabled = prefs[Keys.SIDEBAR_SYSTEM] ?: false,
            isRightEdge = prefs[Keys.SIDEBAR_RIGHT_EDGE] ?: true,
            showFpsBadgeOnHandle = prefs[Keys.SIDEBAR_SHOW_FPS] ?: true,
            handleOpacity = prefs[Keys.SIDEBAR_OPACITY] ?: 0.90f
        )
    }

    val lowEndOptimizerFlow: Flow<LowEndOptimizerConfig> = context.dataStore.data.map { prefs ->
        val visionId = prefs[Keys.VISION_FILTER] ?: ScreenVisionFilter.NONE.id
        val audioId = prefs[Keys.AUDIO_RADAR] ?: AudioRadarPreset.NORMAL.id
        val voiceId = prefs[Keys.VOICE_MOD] ?: VoiceModPreset.ORIGINAL.id
        val thermalId = prefs[Keys.SMART_THERMAL_MODE] ?: SmartThermalMode.AUTO_ADAPTIVE.id
        val advisorId = prefs[Keys.ADVISOR_MODE] ?: AdvisorPresetMode.BALANCED.id
        LowEndOptimizerConfig(
            autoCleanInBackground = prefs[Keys.AUTO_CLEAN_BG] ?: true,
            autoCleanIntervalSec = prefs[Keys.AUTO_CLEAN_INTERVAL] ?: 120,
            ramThresholdPercent = prefs[Keys.RAM_THRESHOLD] ?: 82,
            resolutionScalePreset = prefs[Keys.RES_PRESET] ?: "720p Esports (موصى به للأجهزة الضعيفة)",
            touchSamplingRateHz = prefs[Keys.TOUCH_HZ] ?: 480,
            gpuForce4xMsaaOff = prefs[Keys.GPU_MSAA_OFF] ?: true,
            shadowDownscale = prefs[Keys.SHADOW_DOWNSCALE] ?: true,
            zeroLagNetworkMode = prefs[Keys.ZERO_LAG_NET] ?: true,
            blockHeadsUpNotifications = prefs[Keys.BLOCK_NOTIFS] ?: true,
            lockScreenBrightness = prefs[Keys.LOCK_BRIGHTNESS] ?: true,
            coolingFanSpeedLevel = prefs[Keys.FAN_SPEED] ?: 3,
            visionFilter = ScreenVisionFilter.entries.find { it.id == visionId } ?: ScreenVisionFilter.NONE,
            audioRadarPreset = AudioRadarPreset.entries.find { it.id == audioId } ?: AudioRadarPreset.NORMAL,
            voiceModPreset = VoiceModPreset.entries.find { it.id == voiceId } ?: VoiceModPreset.ORIGINAL,
            mistouchPrevention = prefs[Keys.MISTOUCH_PREV] ?: true,
            bypassChargingGuard = prefs[Keys.BYPASS_CHARGE] ?: true,
            afkBlackScreenSaver = false,
            customBrightnessLevel = prefs[Keys.CUSTOM_BRIGHTNESS] ?: 85,
            gameMediaVolumePercent = prefs[Keys.GAME_MEDIA_VOL] ?: 90,
            dnsServerPreset = prefs[Keys.DNS_PRESET] ?: "Cloudflare 1.1.1.1 (أسرع استجابة بنج)",
            autoRestoreSettingsOnExit = prefs[Keys.AUTO_RESTORE_EXIT] ?: true,
            smartThermalMode = SmartThermalMode.entries.find { it.id == thermalId } ?: SmartThermalMode.AUTO_ADAPTIVE,
            notificationShieldEnabled = prefs[Keys.NOTIF_SHIELD_ENABLED] ?: false,
            fpsOverlayEnabled = prefs[Keys.FPS_OVERLAY_ENABLED] ?: false,
            tempOverlayEnabled = prefs[Keys.TEMP_OVERLAY_ENABLED] ?: false,
            ramOverlayEnabled = prefs[Keys.RAM_OVERLAY_ENABLED] ?: false,
            magnifierEnabled = prefs[Keys.MAGNIFIER_ENABLED] ?: false,
            magnifierZoom = prefs[Keys.MAGNIFIER_ZOOM] ?: 2.0f,
            advisorPresetMode = AdvisorPresetMode.entries.find { it.id == advisorId } ?: AdvisorPresetMode.BALANCED
        )
    }

    val savedPerGameProfilesRawFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.PER_GAME_PROFILES_SERIALIZED] ?: ""
    }

    val lastActiveProfileIdFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_ACTIVE_PROFILE_ID] ?: ""
    }

    val clonedButtonsConfigFlow: Flow<ClonedButtonsConfig> = context.dataStore.data.map { prefs ->
        val rawSerialized = prefs[Keys.CLONED_SERIALIZED]
        val parsedButtons = if (!rawSerialized.isNullOrBlank()) {
            deserializeClonedButtons(rawSerialized)
        } else {
            defaultClonedTouchButtons()
        }
        ClonedButtonsConfig(
            masterEnabled = prefs[Keys.CLONED_MASTER_ENABLED] ?: true,
            systemOverlayEnabled = prefs[Keys.CLONED_SYSTEM_OVERLAY] ?: false,
            isLockedForPlay = prefs[Keys.CLONED_LOCKED_PLAY] ?: false,
            buttonSizeDp = prefs[Keys.CLONED_BTN_SIZE] ?: 52f,
            buttonOpacity = prefs[Keys.CLONED_BTN_OPACITY] ?: 0.86f,
            buttons = parsedButtons
        )
    }

    val whitelistedPackagesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.WHITELISTED_PKGS] ?: emptySet()
    }

    suspend fun savePerformanceMode(mode: PerformanceMode) {
        context.dataStore.edit { it[Keys.PERF_MODE] = mode.id }
    }

    suspend fun saveTriggerConfig(config: ShoulderTriggerConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TRIGGERS_ENABLED] = config.enabled
            prefs[Keys.L1_X] = config.l1XRatio
            prefs[Keys.L1_Y] = config.l1YRatio
            prefs[Keys.L1_MODE] = config.l1Mode.id
            prefs[Keys.L1_RPS] = config.l1BurstRps
            prefs[Keys.R1_X] = config.r1XRatio
            prefs[Keys.R1_Y] = config.r1YRatio
            prefs[Keys.R1_MODE] = config.r1Mode.id
            prefs[Keys.R1_RPS] = config.r1BurstRps
            prefs[Keys.TRIGGER_HAPTIC] = config.hapticFeedback
            prefs[Keys.TRIGGER_SOUND] = config.soundEffect
            prefs[Keys.TRIGGER_COMBO] = config.comboLinkLR
        }
    }

    suspend fun saveCrosshairConfig(config: CrosshairConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CROSSHAIR_IN_APP] = config.enabledInApp
            prefs[Keys.CROSSHAIR_SYSTEM] = config.systemOverlayEnabled
            prefs[Keys.CROSSHAIR_STYLE] = config.style.id
            prefs[Keys.CROSSHAIR_COLOR] = config.colorOption.id
            prefs[Keys.CROSSHAIR_SIZE] = config.sizeDp
            prefs[Keys.CROSSHAIR_STROKE] = config.strokeWidthDp
            prefs[Keys.CROSSHAIR_OPACITY] = config.opacity
            prefs[Keys.CROSSHAIR_OFFSET_X] = config.offsetX
            prefs[Keys.CROSSHAIR_OFFSET_Y] = config.offsetY
        }
    }

    suspend fun saveEdgeSidebarConfig(config: EdgeSidebarConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SIDEBAR_IN_APP] = config.enabledInApp
            prefs[Keys.SIDEBAR_SYSTEM] = config.systemFloatingEnabled
            prefs[Keys.SIDEBAR_RIGHT_EDGE] = config.isRightEdge
            prefs[Keys.SIDEBAR_SHOW_FPS] = config.showFpsBadgeOnHandle
            prefs[Keys.SIDEBAR_OPACITY] = config.handleOpacity
        }
    }

    suspend fun saveLowEndOptimizerConfig(config: LowEndOptimizerConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_CLEAN_BG] = config.autoCleanInBackground
            prefs[Keys.AUTO_CLEAN_INTERVAL] = config.autoCleanIntervalSec
            prefs[Keys.RAM_THRESHOLD] = config.ramThresholdPercent
            prefs[Keys.RES_PRESET] = config.resolutionScalePreset
            prefs[Keys.TOUCH_HZ] = config.touchSamplingRateHz
            prefs[Keys.GPU_MSAA_OFF] = config.gpuForce4xMsaaOff
            prefs[Keys.SHADOW_DOWNSCALE] = config.shadowDownscale
            prefs[Keys.ZERO_LAG_NET] = config.zeroLagNetworkMode
            prefs[Keys.BLOCK_NOTIFS] = config.blockHeadsUpNotifications
            prefs[Keys.LOCK_BRIGHTNESS] = config.lockScreenBrightness
            prefs[Keys.FAN_SPEED] = config.coolingFanSpeedLevel
            prefs[Keys.VISION_FILTER] = config.visionFilter.id
            prefs[Keys.AUDIO_RADAR] = config.audioRadarPreset.id
            prefs[Keys.VOICE_MOD] = config.voiceModPreset.id
            prefs[Keys.MISTOUCH_PREV] = config.mistouchPrevention
            prefs[Keys.BYPASS_CHARGE] = config.bypassChargingGuard
            prefs[Keys.CUSTOM_BRIGHTNESS] = config.customBrightnessLevel
            prefs[Keys.GAME_MEDIA_VOL] = config.gameMediaVolumePercent
            prefs[Keys.DNS_PRESET] = config.dnsServerPreset
            prefs[Keys.AUTO_RESTORE_EXIT] = config.autoRestoreSettingsOnExit
            prefs[Keys.SMART_THERMAL_MODE] = config.smartThermalMode.id
            prefs[Keys.NOTIF_SHIELD_ENABLED] = config.notificationShieldEnabled
            prefs[Keys.FPS_OVERLAY_ENABLED] = config.fpsOverlayEnabled
            prefs[Keys.TEMP_OVERLAY_ENABLED] = config.tempOverlayEnabled
            prefs[Keys.RAM_OVERLAY_ENABLED] = config.ramOverlayEnabled
            prefs[Keys.MAGNIFIER_ENABLED] = config.magnifierEnabled
            prefs[Keys.MAGNIFIER_ZOOM] = config.magnifierZoom
            prefs[Keys.ADVISOR_MODE] = config.advisorPresetMode.id
        }
    }

    suspend fun saveLastActiveProfileId(profileId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_ACTIVE_PROFILE_ID] = profileId
        }
    }

    suspend fun savePerGameProfiles(profiles: List<GameSpaceProfile>) {
        val raw = serializePerGameProfiles(profiles)
        context.dataStore.edit { prefs ->
            prefs[Keys.PER_GAME_PROFILES_SERIALIZED] = raw
        }
    }

    fun mergeSavedProfilesWithCatalog(
        rawSaved: String,
        defaultCatalog: List<GameSpaceProfile>,
        isPackageInstalledCheck: (String) -> Boolean
    ): List<GameSpaceProfile> {
        if (rawSaved.isBlank()) return defaultCatalog
        val parsed = deserializePerGameProfiles(rawSaved)
        if (parsed.isEmpty()) return defaultCatalog

        val savedById = parsed.associateBy { it.id }
        val mergedDefaults = defaultCatalog.map { def ->
            val saved = savedById[def.id]
            if (saved != null) {
                saved.copy(isInstalledOnDevice = isPackageInstalledCheck(saved.packageName))
            } else {
                def
            }
        }
        val customAdded = parsed.filter { p -> defaultCatalog.none { it.id == p.id } }.map {
            it.copy(
                isInstalledOnDevice = isPackageInstalledCheck(it.packageName),
                isCustomAdded = true
            )
        }
        return mergedDefaults + customAdded
    }

    private fun serializePerGameProfiles(profiles: List<GameSpaceProfile>): String {
        return profiles.joinToString(";;") { p ->
            listOf(
                p.id.replace("|", "").replace(";", ""),
                p.title.replace("|", "").replace(";", ""),
                p.packageName.replace("|", "").replace(";", ""),
                p.genreAr.replace("|", "").replace(";", ""),
                p.recommendedMode.id,
                p.l1ActionAr.replace("|", "").replace(";", ""),
                p.r1ActionAr.replace("|", "").replace(";", ""),
                p.l1X,
                p.l1Y,
                p.r1X,
                p.r1Y,
                p.targetFps,
                p.accentHex,
                p.smartThermalMode.id,
                p.notificationShieldEnabled,
                p.magnifierEnabled,
                p.magnifierZoom,
                p.crosshairEnabled,
                p.crosshairStyle.id,
                p.touchProtectionEnabled,
                p.gamingSidebarEnabled,
                p.fpsOverlayEnabled,
                p.tempOverlayEnabled,
                p.ramOverlayEnabled,
                p.triggersEnabled,
                p.clonedButtonsEnabled,
                p.advisorMode.id,
                p.isCustomAdded
            ).joinToString("|")
        }
    }

    private fun deserializePerGameProfiles(raw: String): List<GameSpaceProfile> {
        return raw.split(";;").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size < 28) return@mapNotNull null
            try {
                GameSpaceProfile(
                    id = parts[0],
                    title = parts[1],
                    packageName = parts[2],
                    genreAr = parts[3],
                    recommendedMode = PerformanceMode.entries.find { it.id == parts[4] } ?: PerformanceMode.BALANCE,
                    l1ActionAr = parts[5],
                    r1ActionAr = parts[6],
                    l1X = parts[7].toFloat().coerceIn(0.05f, 0.95f),
                    l1Y = parts[8].toFloat().coerceIn(0.05f, 0.95f),
                    r1X = parts[9].toFloat().coerceIn(0.05f, 0.95f),
                    r1Y = parts[10].toFloat().coerceIn(0.05f, 0.95f),
                    targetFps = parts[11].toInt().coerceIn(30, 144),
                    accentHex = parts[12].toLong(),
                    smartThermalMode = SmartThermalMode.entries.find { it.id == parts[13] } ?: SmartThermalMode.AUTO_ADAPTIVE,
                    notificationShieldEnabled = parts[14].toBoolean(),
                    magnifierEnabled = parts[15].toBoolean(),
                    magnifierZoom = parts[16].toFloat().coerceIn(1.2f, 4.0f),
                    crosshairEnabled = parts[17].toBoolean(),
                    crosshairStyle = CrosshairStyle.entries.find { it.id == parts[18] } ?: CrosshairStyle.CIRCLE_DOT,
                    touchProtectionEnabled = parts[19].toBoolean(),
                    gamingSidebarEnabled = parts[20].toBoolean(),
                    fpsOverlayEnabled = parts[21].toBoolean(),
                    tempOverlayEnabled = parts[22].toBoolean(),
                    ramOverlayEnabled = parts[23].toBoolean(),
                    triggersEnabled = parts[24].toBoolean(),
                    clonedButtonsEnabled = parts[25].toBoolean(),
                    advisorMode = AdvisorPresetMode.entries.find { it.id == parts[26] } ?: AdvisorPresetMode.BALANCED,
                    isCustomAdded = parts[27].toBoolean()
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun saveClonedButtonsConfig(config: ClonedButtonsConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CLONED_MASTER_ENABLED] = config.masterEnabled
            prefs[Keys.CLONED_SYSTEM_OVERLAY] = config.systemOverlayEnabled
            prefs[Keys.CLONED_LOCKED_PLAY] = config.isLockedForPlay
            prefs[Keys.CLONED_BTN_SIZE] = config.buttonSizeDp
            prefs[Keys.CLONED_BTN_OPACITY] = config.buttonOpacity
            prefs[Keys.CLONED_SERIALIZED] = serializeClonedButtons(config.buttons)
        }
    }

    private fun serializeClonedButtons(buttons: List<ClonedTouchButton>): String {
        return buttons.joinToString(";") { b ->
            listOf(
                b.id,
                b.badge,
                b.labelAr.replace(";", "").replace("|", ""),
                b.enabled,
                b.buttonXRatio,
                b.buttonYRatio,
                b.targetXRatio,
                b.targetYRatio,
                b.fireMode.id,
                b.burstRps,
                b.colorHex
            ).joinToString("|")
        }
    }

    private fun deserializeClonedButtons(raw: String): List<ClonedTouchButton> {
        val defaults = defaultClonedTouchButtons()
        val parsed = raw.split(";").mapNotNull { item ->
            val parts = item.split("|")
            if (parts.size < 11) return@mapNotNull null
            try {
                val modeId = parts[8]
                ClonedTouchButton(
                    id = parts[0].toInt(),
                    badge = parts[1],
                    labelAr = parts[2],
                    enabled = parts[3].toBoolean(),
                    buttonXRatio = parts[4].toFloat().coerceIn(0.05f, 0.95f),
                    buttonYRatio = parts[5].toFloat().coerceIn(0.08f, 0.92f),
                    targetXRatio = parts[6].toFloat().coerceIn(0.05f, 0.95f),
                    targetYRatio = parts[7].toFloat().coerceIn(0.08f, 0.92f),
                    fireMode = TriggerFireMode.entries.find { it.id == modeId } ?: TriggerFireMode.SINGLE_TAP,
                    burstRps = parts[9].toInt().coerceIn(4, 20),
                    colorHex = parts[10].toLong()
                )
            } catch (_: Exception) {
                null
            }
        }
        return if (parsed.size == defaults.size) parsed else defaults
    }

    suspend fun toggleWhitelistPackage(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.WHITELISTED_PKGS] ?: emptySet()
            prefs[Keys.WHITELISTED_PKGS] = if (current.contains(packageName)) {
                current - packageName
            } else {
                current + packageName
            }
        }
    }
}
