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
import com.example.model.CrosshairColorOption
import com.example.model.CrosshairConfig
import com.example.model.CrosshairStyle
import com.example.model.EdgeSidebarConfig
import com.example.model.LowEndOptimizerConfig
import com.example.model.PerformanceMode
import com.example.model.ScreenVisionFilter
import com.example.model.ShoulderTriggerConfig
import com.example.model.TriggerFireMode
import com.example.model.VoiceModPreset
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

        // Whitelist
        val WHITELISTED_PKGS = stringSetPreferencesKey("whitelisted_pkgs")
    }

    val performanceModeFlow: Flow<PerformanceMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.PERF_MODE] ?: PerformanceMode.DIABLO.id
        PerformanceMode.entries.find { it.id == raw } ?: PerformanceMode.DIABLO
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
            soundEffect = prefs[Keys.TRIGGER_SOUND] ?: true,
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
        val audioId = prefs[Keys.AUDIO_RADAR] ?: AudioRadarPreset.FOOTSTEPS_PRO.id
        val voiceId = prefs[Keys.VOICE_MOD] ?: VoiceModPreset.ORIGINAL.id
        LowEndOptimizerConfig(
            autoCleanInBackground = prefs[Keys.AUTO_CLEAN_BG] ?: true,
            autoCleanIntervalSec = prefs[Keys.AUTO_CLEAN_INTERVAL] ?: 45,
            ramThresholdPercent = prefs[Keys.RAM_THRESHOLD] ?: 75,
            resolutionScalePreset = prefs[Keys.RES_PRESET] ?: "720p Esports (موصى به للأجهزة الضعيفة)",
            touchSamplingRateHz = prefs[Keys.TOUCH_HZ] ?: 480,
            gpuForce4xMsaaOff = prefs[Keys.GPU_MSAA_OFF] ?: true,
            shadowDownscale = prefs[Keys.SHADOW_DOWNSCALE] ?: true,
            zeroLagNetworkMode = prefs[Keys.ZERO_LAG_NET] ?: true,
            blockHeadsUpNotifications = prefs[Keys.BLOCK_NOTIFS] ?: true,
            lockScreenBrightness = prefs[Keys.LOCK_BRIGHTNESS] ?: true,
            coolingFanSpeedLevel = prefs[Keys.FAN_SPEED] ?: 3,
            visionFilter = ScreenVisionFilter.entries.find { it.id == visionId } ?: ScreenVisionFilter.NONE,
            audioRadarPreset = AudioRadarPreset.entries.find { it.id == audioId } ?: AudioRadarPreset.FOOTSTEPS_PRO,
            voiceModPreset = VoiceModPreset.entries.find { it.id == voiceId } ?: VoiceModPreset.ORIGINAL,
            mistouchPrevention = prefs[Keys.MISTOUCH_PREV] ?: true,
            bypassChargingGuard = prefs[Keys.BYPASS_CHARGE] ?: true,
            afkBlackScreenSaver = false
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
        }
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
