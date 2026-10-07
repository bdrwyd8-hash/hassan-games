package com.example.service

import com.example.model.ClonedButtonsConfig
import com.example.model.ClonedTouchButton
import com.example.model.CrosshairConfig
import com.example.model.LowEndDeviceConfig
import com.example.model.PerformanceMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

data class ClonedTapDispatchEvent(
    val buttonId: Int,
    val label: String,
    val targetX: Float,
    val targetY: Float,
    val isDown: Boolean,
    val timestampMs: Long = System.currentTimeMillis()
)

object TriggerEventBus {

    private val _serviceConnected = MutableStateFlow(false)
    val serviceConnected: StateFlow<Boolean> = _serviceConnected.asStateFlow()

    private val _overlayRunning = MutableStateFlow(false)
    val overlayRunning: StateFlow<Boolean> = _overlayRunning.asStateFlow()

    private val _clonedButtonsConfig = MutableStateFlow(ClonedButtonsConfig())
    val clonedButtonsConfig: StateFlow<ClonedButtonsConfig> = _clonedButtonsConfig.asStateFlow()

    private val _clonedTapEvents = MutableSharedFlow<ClonedTapDispatchEvent>(extraBufferCapacity = 32)
    val clonedTapEvents: SharedFlow<ClonedTapDispatchEvent> = _clonedTapEvents.asSharedFlow()

    private val _totalClonedTaps = MutableStateFlow(0)
    val totalClonedTaps: StateFlow<Int> = _totalClonedTaps.asStateFlow()

    private val _lastClonedActiveId = MutableStateFlow<Int?>(null)
    val lastClonedActiveId: StateFlow<Int?> = _lastClonedActiveId.asStateFlow()

    private val _crosshairConfig = MutableStateFlow(CrosshairConfig())
    val crosshairConfig: StateFlow<CrosshairConfig> = _crosshairConfig.asStateFlow()

    private val _activePerformanceMode = MutableStateFlow(PerformanceMode.PERFORMANCE)
    val activePerformanceMode: StateFlow<PerformanceMode> = _activePerformanceMode.asStateFlow()

    private val _lowEndConfig = MutableStateFlow(LowEndDeviceConfig())
    val lowEndConfig: StateFlow<LowEndDeviceConfig> = _lowEndConfig.asStateFlow()

    private val _isMasterEngineRunning = MutableStateFlow(true)
    val isMasterEngineRunning: StateFlow<Boolean> = _isMasterEngineRunning.asStateFlow()

    private val _activeGamePackage = MutableStateFlow<String?>(null)
    val activeGamePackage: StateFlow<String?> = _activeGamePackage.asStateFlow()

    private val _activeGameTitle = MutableStateFlow<String?>(null)
    val activeGameTitle: StateFlow<String?> = _activeGameTitle.asStateFlow()

    private val _shutdownRequests = MutableSharedFlow<Long>(extraBufferCapacity = 4)
    val shutdownRequests: SharedFlow<Long> = _shutdownRequests.asSharedFlow()

    fun setServiceConnected(connected: Boolean) {
        _serviceConnected.value = connected
        if (!connected) {
            _lastClonedActiveId.value = null
        }
    }

    fun setOverlayRunning(running: Boolean) {
        _overlayRunning.value = running
    }

    fun updateClonedButtonsConfig(config: ClonedButtonsConfig) {
        _clonedButtonsConfig.value = config
    }

    fun emitClonedButtonTap(button: ClonedTouchButton, isDown: Boolean) {
        if (isDown) {
            _totalClonedTaps.value += 1
            _lastClonedActiveId.value = button.id
        } else if (_lastClonedActiveId.value == button.id) {
            _lastClonedActiveId.value = null
        }
        _clonedTapEvents.tryEmit(
            ClonedTapDispatchEvent(
                buttonId = button.id,
                label = button.label,
                targetX = button.targetX,
                targetY = button.targetY,
                isDown = isDown
            )
        )
    }

    fun updateCrosshairConfig(config: CrosshairConfig) {
        _crosshairConfig.value = config
    }

    fun updatePerformanceMode(mode: PerformanceMode) {
        _activePerformanceMode.value = mode
    }

    fun updateLowEndConfig(config: LowEndDeviceConfig) {
        _lowEndConfig.value = config
    }

    fun setMasterEngineRunning(running: Boolean) {
        _isMasterEngineRunning.value = running
    }

    fun setActiveGameSession(packageName: String?, title: String?) {
        _activeGamePackage.value = packageName
        _activeGameTitle.value = title
    }

    fun requestCompleteShutdown() {
        _isMasterEngineRunning.value = false
        _lastClonedActiveId.value = null
        _clonedButtonsConfig.value = _clonedButtonsConfig.value.copy(enabled = false)
        _crosshairConfig.value = _crosshairConfig.value.copy(enabled = false)
        _shutdownRequests.tryEmit(System.currentTimeMillis())
    }

    fun resetShotCounters() {
        _totalClonedTaps.value = 0
    }
}
