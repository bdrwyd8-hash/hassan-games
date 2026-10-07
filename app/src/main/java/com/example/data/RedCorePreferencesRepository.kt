package com.example.data

import android.content.Context
import androidx.core.content.edit
import com.example.model.AdvisorPresetMode
import com.example.model.ClonedButtonMode
import com.example.model.ClonedButtonsConfig
import com.example.model.ClonedTouchButton
import com.example.model.CrosshairConfig
import com.example.model.CrosshairStyle
import com.example.model.GameSpaceProfile
import com.example.model.GfxResolutionPreset
import com.example.model.LowEndDeviceConfig
import com.example.model.PerformanceMode
import com.example.model.SmartThermalMode
import com.example.model.TouchSamplingRate

class RedCorePreferencesRepository(context: Context) {

    private val prefs = context.getSharedPreferences("hassan_games_pro_prefs", Context.MODE_PRIVATE)

    fun savePerformanceMode(mode: PerformanceMode) {
        prefs.edit { putString(KEY_PERF_MODE, mode.name) }
    }

    fun loadPerformanceMode(): PerformanceMode {
        val raw = prefs.getString(KEY_PERF_MODE, PerformanceMode.PERFORMANCE.name)
        return PerformanceMode.entries.firstOrNull { it.name == raw } ?: PerformanceMode.PERFORMANCE
    }

    fun saveAdvisorPresetMode(mode: AdvisorPresetMode) {
        prefs.edit { putString(KEY_ADVISOR_PRESET_MODE, mode.name) }
    }

    fun loadAdvisorPresetMode(): AdvisorPresetMode {
        val raw = prefs.getString(KEY_ADVISOR_PRESET_MODE, AdvisorPresetMode.BEST_PERFORMANCE.name)
        return AdvisorPresetMode.entries.firstOrNull { it.name == raw } ?: AdvisorPresetMode.BEST_PERFORMANCE
    }

    fun saveClonedButtonsConfig(config: ClonedButtonsConfig) {
        val serializedButtons = config.buttons.joinToString("|") { btn ->
            listOf(
                btn.id,
                btn.label,
                btn.actionTitleAr.replace("|", " ").replace(";", " "),
                btn.enabled,
                btn.mode.name,
                btn.sourceX,
                btn.sourceY,
                btn.targetX,
                btn.targetY,
                btn.buttonSizeDp,
                btn.opacity,
                btn.colorHex
            ).joinToString(";")
        }
        prefs.edit {
            putBoolean(KEY_CLONED_ENABLED, config.enabled)
            putBoolean(KEY_CLONED_LINES, config.showConnectionLines)
            putBoolean(KEY_CLONED_LOCKED, config.editPositionsLocked)
            putBoolean(KEY_CLONED_HAPTIC, config.hapticFeedback)
            putString(KEY_CLONED_BUTTONS_DATA, serializedButtons)
        }
    }

    fun loadClonedButtonsConfig(): ClonedButtonsConfig {
        val defaults = ClonedButtonsConfig.defaultClonedButtons()
        val rawData = prefs.getString(KEY_CLONED_BUTTONS_DATA, null)
        val parsedButtons = if (!rawData.isNullOrBlank()) {
            rawData.split("|").mapNotNull { entry ->
                val parts = entry.split(";")
                if (parts.size >= 12) {
                    val id = parts[0].toIntOrNull() ?: return@mapNotNull null
                    val label = parts[1]
                    val title = parts[2]
                    val enabled = parts[3].toBooleanStrictOrNull() ?: true
                    val mode = ClonedButtonMode.entries.firstOrNull { it.name == parts[4] }
                        ?: ClonedButtonMode.SINGLE_TAP
                    val sx = parts[5].toFloatOrNull()?.coerceIn(0.05f, 0.95f) ?: 0.2f
                    val sy = parts[6].toFloatOrNull()?.coerceIn(0.08f, 0.92f) ?: 0.4f
                    val tx = parts[7].toFloatOrNull()?.coerceIn(0.05f, 0.95f) ?: 0.8f
                    val ty = parts[8].toFloatOrNull()?.coerceIn(0.08f, 0.92f) ?: 0.7f
                    val size = parts[9].toIntOrNull()?.coerceIn(36, 84) ?: 52
                    val opacity = parts[10].toFloatOrNull()?.coerceIn(0.35f, 1.0f) ?: 0.88f
                    val colorHex = parts[11].toLongOrNull() ?: 0xFFFF1744L
                    ClonedTouchButton(
                        id = id,
                        label = label,
                        actionTitleAr = title,
                        enabled = enabled,
                        mode = mode,
                        sourceX = sx,
                        sourceY = sy,
                        targetX = tx,
                        targetY = ty,
                        buttonSizeDp = size,
                        opacity = opacity,
                        colorHex = colorHex
                    )
                } else {
                    null
                }
            }
        } else {
            emptyList()
        }

        val finalButtons = if (parsedButtons.size == defaults.size) parsedButtons else defaults
        return ClonedButtonsConfig(
            enabled = prefs.getBoolean(KEY_CLONED_ENABLED, false),
            showConnectionLines = prefs.getBoolean(KEY_CLONED_LINES, true),
            editPositionsLocked = prefs.getBoolean(KEY_CLONED_LOCKED, false),
            hapticFeedback = prefs.getBoolean(KEY_CLONED_HAPTIC, true),
            buttons = finalButtons
        )
    }

    fun saveCrosshairConfig(config: CrosshairConfig) {
        prefs.edit {
            putBoolean(KEY_CH_ENABLED, config.enabled)
            putString(KEY_CH_STYLE, config.style.name)
            putFloat(KEY_CH_SIZE, config.sizeDp)
            putFloat(KEY_CH_STROKE, config.strokeWidthDp)
            putFloat(KEY_CH_OPACITY, config.opacity)
            putLong(KEY_CH_COLOR, config.colorHex)
            putFloat(KEY_CH_OFFSET_X, config.offsetXDp)
            putFloat(KEY_CH_OFFSET_Y, config.offsetYDp)
        }
    }

    fun loadCrosshairConfig(): CrosshairConfig {
        val styleName = prefs.getString(KEY_CH_STYLE, CrosshairStyle.TACTICAL_CROSS.name)
        val style = CrosshairStyle.entries.firstOrNull { it.name == styleName }
            ?: CrosshairStyle.TACTICAL_CROSS

        return CrosshairConfig(
            enabled = prefs.getBoolean(KEY_CH_ENABLED, false),
            style = style,
            sizeDp = prefs.getFloat(KEY_CH_SIZE, 28f),
            strokeWidthDp = prefs.getFloat(KEY_CH_STROKE, 2.2f),
            opacity = prefs.getFloat(KEY_CH_OPACITY, 0.9f),
            colorHex = prefs.getLong(KEY_CH_COLOR, 0xFFFF1744),
            offsetXDp = prefs.getFloat(KEY_CH_OFFSET_X, 0f),
            offsetYDp = prefs.getFloat(KEY_CH_OFFSET_Y, 0f)
        )
    }

    fun saveLowEndConfig(config: LowEndDeviceConfig) {
        prefs.edit {
            putBoolean(KEY_LOW_END_BOOST, config.lowEndBoostEnabled)
            putString(KEY_SMART_THERMAL_MODE, config.smartThermalMode.name)
            putBoolean(KEY_LOW_END_THERMAL, config.thermalGuardEnabled)
            putBoolean(KEY_LOW_END_SHADERS, config.disableHeavyShaders)
            putBoolean(KEY_LOW_END_TOUCH_GUARD, config.touchEdgeRejectEnabled)
            putBoolean(KEY_LOW_END_BRIGHTNESS_LOCK, config.brightnessLockEnabled)
            putInt(KEY_LOW_END_BRIGHTNESS_VAL, config.lockedBrightnessPercent)
            putBoolean(KEY_LOW_END_WIFI_PRIORITY, config.wiFiPriorityEnabled)
            putBoolean(KEY_LOW_END_BLOCK_NOTIFS, config.blockHeadsUpNotifications)
            putInt(KEY_LOW_END_RAM_THRESHOLD, config.ramAutoPurgeThresholdPercent)
        }
    }

    fun loadLowEndConfig(): LowEndDeviceConfig {
        val smartThermalName = prefs.getString(KEY_SMART_THERMAL_MODE, SmartThermalMode.AUTO_ADAPTIVE.name)
        val smartThermalMode = SmartThermalMode.entries.firstOrNull { it.name == smartThermalName }
            ?: SmartThermalMode.AUTO_ADAPTIVE

        return LowEndDeviceConfig(
            lowEndBoostEnabled = prefs.getBoolean(KEY_LOW_END_BOOST, false),
            smartThermalMode = smartThermalMode,
            thermalGuardEnabled = prefs.getBoolean(KEY_LOW_END_THERMAL, true),
            disableHeavyShaders = prefs.getBoolean(KEY_LOW_END_SHADERS, false),
            touchEdgeRejectEnabled = prefs.getBoolean(KEY_LOW_END_TOUCH_GUARD, false),
            brightnessLockEnabled = prefs.getBoolean(KEY_LOW_END_BRIGHTNESS_LOCK, false),
            lockedBrightnessPercent = prefs.getInt(KEY_LOW_END_BRIGHTNESS_VAL, 82).coerceIn(25, 100),
            wiFiPriorityEnabled = prefs.getBoolean(KEY_LOW_END_WIFI_PRIORITY, true),
            blockHeadsUpNotifications = prefs.getBoolean(KEY_LOW_END_BLOCK_NOTIFS, false),
            ramAutoPurgeThresholdPercent = prefs.getInt(KEY_LOW_END_RAM_THRESHOLD, 80).coerceIn(60, 95)
        )
    }

    fun saveGfxResolution(preset: GfxResolutionPreset) {
        prefs.edit { putString(KEY_GFX_RES, preset.name) }
    }

    fun loadGfxResolution(): GfxResolutionPreset {
        val raw = prefs.getString(KEY_GFX_RES, GfxResolutionPreset.RES_900P.name)
        return GfxResolutionPreset.entries.firstOrNull { it.name == raw } ?: GfxResolutionPreset.RES_900P
    }

    fun saveTouchSamplingRate(rate: TouchSamplingRate) {
        prefs.edit { putString(KEY_TOUCH_RATE, rate.name) }
    }

    fun loadTouchSamplingRate(): TouchSamplingRate {
        val raw = prefs.getString(KEY_TOUCH_RATE, TouchSamplingRate.HZ_960.name)
        return TouchSamplingRate.entries.firstOrNull { it.name == raw } ?: TouchSamplingRate.HZ_960
    }

    fun saveWhitelistedPackages(packages: Set<String>) {
        prefs.edit { putStringSet(KEY_WHITELIST_PKGS, packages) }
    }

    fun loadWhitelistedPackages(): Set<String> {
        return prefs.getStringSet(KEY_WHITELIST_PKGS, emptySet()) ?: emptySet()
    }

    fun saveGameProfiles(profiles: List<GameSpaceProfile>) {
        val serialized = profiles.joinToString("|") { p ->
            listOf(
                p.id,
                p.title.replace("|", " ").replace(";", " "),
                p.packageName,
                p.genreAr.replace("|", " ").replace(";", " "),
                p.recommendedMode.name,
                p.recommendedFps,
                p.accentHex,
                p.autoCleanRamBeforeLaunch,
                p.notificationShieldEnabled,
                p.crosshairEnabled,
                p.fpsOverlayEnabled,
                p.brightnessLockEnabled,
                p.touchGuardEnabled,
                p.wiFiPriorityEnabled,
                p.preferredAdvisorMode.name,
                p.sidebarOverlayEnabled,
                p.isUserAdded
            ).joinToString(";")
        }
        prefs.edit {
            putString(KEY_MY_GAMES_DATA, serialized)
            putBoolean(KEY_MY_GAMES_INITIALIZED, true)
        }
    }

    fun hasSavedMyGamesList(): Boolean {
        return prefs.getBoolean(KEY_MY_GAMES_INITIALIZED, false)
    }

    fun loadRawGameProfiles(): String {
        return prefs.getString(KEY_MY_GAMES_DATA, "") ?: ""
    }

    fun parseSavedGameProfiles(
        rawSaved: String,
        isPackageInstalled: (String) -> Boolean
    ): List<GameSpaceProfile> {
        if (rawSaved.isBlank()) return emptyList()
        return rawSaved.split("|").mapNotNull { entry ->
            val parts = entry.split(";")
            if (parts.size >= 15) {
                val id = parts[0]
                val title = parts[1]
                val pkg = parts[2]
                val genre = parts[3]
                val mode = PerformanceMode.entries.firstOrNull { it.name == parts[4] }
                    ?: PerformanceMode.PERFORMANCE
                val fps = parts[5].toIntOrNull() ?: 90
                val accent = parts[6].toLongOrNull() ?: 0xFFFF1744L
                val autoClean = parts[7].toBooleanStrictOrNull() ?: true
                val notifShield = parts[8].toBooleanStrictOrNull() ?: true
                val crosshair = parts[9].toBooleanStrictOrNull() ?: false
                val fpsHud = parts[10].toBooleanStrictOrNull() ?: true
                val brightLock = parts[11].toBooleanStrictOrNull() ?: false
                val touchGuard = parts[12].toBooleanStrictOrNull() ?: false
                val wifiPrio = parts[13].toBooleanStrictOrNull() ?: true
                val advisor = AdvisorPresetMode.entries.firstOrNull { it.name == parts[14] }
                    ?: AdvisorPresetMode.BEST_PERFORMANCE
                val sidebar = if (parts.size >= 16) parts[15].toBooleanStrictOrNull() ?: true else true
                val userAdded = if (parts.size >= 17) parts[16].toBooleanStrictOrNull() ?: true else true

                GameSpaceProfile(
                    id = id,
                    title = title,
                    packageName = pkg,
                    genreAr = genre,
                    recommendedMode = mode,
                    recommendedFps = fps,
                    accentHex = accent,
                    isInstalled = isPackageInstalled(pkg),
                    isUserAdded = userAdded,
                    autoCleanRamBeforeLaunch = autoClean,
                    notificationShieldEnabled = notifShield,
                    crosshairEnabled = crosshair,
                    fpsOverlayEnabled = fpsHud,
                    sidebarOverlayEnabled = sidebar,
                    brightnessLockEnabled = brightLock,
                    touchGuardEnabled = touchGuard,
                    wiFiPriorityEnabled = wifiPrio,
                    preferredAdvisorMode = advisor
                )
            } else {
                null
            }
        }
    }

    fun mergeSavedProfilesWithCatalog(
        rawSaved: String,
        defaultCatalog: List<GameSpaceProfile>,
        isPackageInstalled: (String) -> Boolean
    ): List<GameSpaceProfile> {
        val savedList = parseSavedGameProfiles(rawSaved, isPackageInstalled)
        if (savedList.isNotEmpty()) {
            return savedList
        }
        return defaultCatalog
    }

    fun saveLastSessionGame(
        gameId: String,
        title: String,
        packageName: String,
        timestampMs: Long,
        mode: PerformanceMode
    ) {
        prefs.edit {
            putString(KEY_LAST_GAME_ID, gameId)
            putString(KEY_LAST_GAME_TITLE, title)
            putString(KEY_LAST_GAME_PKG, packageName)
            putLong(KEY_LAST_GAME_TIME, timestampMs)
            putString(KEY_LAST_GAME_MODE, mode.name)
        }
    }

    fun loadLastSessionGameRaw(): Triple<String, String, String>? {
        val id = prefs.getString(KEY_LAST_GAME_ID, null) ?: return null
        val title = prefs.getString(KEY_LAST_GAME_TITLE, null) ?: return null
        val pkg = prefs.getString(KEY_LAST_GAME_PKG, null) ?: return null
        return Triple(id, title, pkg)
    }

    fun loadLastSessionMeta(): Pair<Long, PerformanceMode> {
        val ts = prefs.getLong(KEY_LAST_GAME_TIME, 0L)
        val modeName = prefs.getString(KEY_LAST_GAME_MODE, PerformanceMode.PERFORMANCE.name)
        val mode = PerformanceMode.entries.firstOrNull { it.name == modeName } ?: PerformanceMode.PERFORMANCE
        return ts to mode
    }

    companion object {
        private const val KEY_PERF_MODE = "key_perf_mode"
        private const val KEY_ADVISOR_PRESET_MODE = "key_advisor_preset_mode"

        private const val KEY_CLONED_ENABLED = "key_cloned_enabled"
        private const val KEY_CLONED_LINES = "key_cloned_lines"
        private const val KEY_CLONED_LOCKED = "key_cloned_locked"
        private const val KEY_CLONED_HAPTIC = "key_cloned_haptic"
        private const val KEY_CLONED_BUTTONS_DATA = "key_cloned_buttons_data"

        private const val KEY_CH_ENABLED = "key_ch_enabled"
        private const val KEY_CH_STYLE = "key_ch_style"
        private const val KEY_CH_SIZE = "key_ch_size"
        private const val KEY_CH_STROKE = "key_ch_stroke"
        private const val KEY_CH_OPACITY = "key_ch_opacity"
        private const val KEY_CH_COLOR = "key_ch_color"
        private const val KEY_CH_OFFSET_X = "key_ch_offset_x"
        private const val KEY_CH_OFFSET_Y = "key_ch_offset_y"

        private const val KEY_LOW_END_BOOST = "key_low_end_boost"
        private const val KEY_SMART_THERMAL_MODE = "key_smart_thermal_mode"
        private const val KEY_LOW_END_THERMAL = "key_low_end_thermal"
        private const val KEY_LOW_END_SHADERS = "key_low_end_shaders"
        private const val KEY_LOW_END_TOUCH_GUARD = "key_low_end_touch_guard"
        private const val KEY_LOW_END_BRIGHTNESS_LOCK = "key_low_end_brightness_lock"
        private const val KEY_LOW_END_BRIGHTNESS_VAL = "key_low_end_brightness_val"
        private const val KEY_LOW_END_WIFI_PRIORITY = "key_low_end_wifi_priority"
        private const val KEY_LOW_END_BLOCK_NOTIFS = "key_low_end_block_notifs"
        private const val KEY_LOW_END_RAM_THRESHOLD = "key_low_end_ram_threshold"

        private const val KEY_GFX_RES = "key_gfx_res"
        private const val KEY_TOUCH_RATE = "key_touch_rate"
        private const val KEY_WHITELIST_PKGS = "key_whitelist_pkgs"

        private const val KEY_MY_GAMES_DATA = "key_my_games_data_v2"
        private const val KEY_MY_GAMES_INITIALIZED = "key_my_games_initialized_v2"
        private const val KEY_LAST_GAME_ID = "key_last_game_id"
        private const val KEY_LAST_GAME_TITLE = "key_last_game_title"
        private const val KEY_LAST_GAME_PKG = "key_last_game_pkg"
        private const val KEY_LAST_GAME_TIME = "key_last_game_time"
        private const val KEY_LAST_GAME_MODE = "key_last_game_mode"
    }
}
