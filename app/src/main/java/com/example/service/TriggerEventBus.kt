package com.example.service

import com.example.model.CrosshairConfig
import com.example.model.EdgeSidebarConfig
import com.example.model.HardwareTelemetry
import com.example.model.LowEndOptimizerConfig
import com.example.model.PerformanceMode
import com.example.model.ShoulderTriggerConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TriggerButtonType {
    L1_VOLUME_UP,
    R1_VOLUME_DOWN
}

data class TriggerFireEvent(
    val button: TriggerButtonType,
    val isPressed: Boolean,
    val xRatio: Float,
    val yRatio: Float,
    val timestampMs: Long = System.currentTimeMillis()
)

/**
 * Real-time singleton bridge connecting hardware key events, Floating Edge-Swipe Game Bar,
 * AccessibilityService, and ViewModel.
 */
object TriggerEventBus {
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

    private val _performanceMode = MutableStateFlow(PerformanceMode.DIABLO)
    val performanceMode: StateFlow<PerformanceMode> = _performanceMode.asStateFlow()

    private val _isAccessibilityServiceRunning = MutableStateFlow(false)
    val isAccessibilityServiceRunning: StateFlow<Boolean> = _isAccessibilityServiceRunning.asStateFlow()

    private val _isOverlayServiceRunning = MutableStateFlow(false)
    val isOverlayServiceRunning: StateFlow<Boolean> = _isOverlayServiceRunning.asStateFlow()

    private val _isInAppSidebarOpen = MutableStateFlow(false)
    val isInAppSidebarOpen: StateFlow<Boolean> = _isInAppSidebarOpen.asStateFlow()

    private val _l1Pressed = MutableStateFlow(false)
    val l1Pressed: StateFlow<Boolean> = _l1Pressed.asStateFlow()

    private val _r1Pressed = MutableStateFlow(false)
    val r1Pressed: StateFlow<Boolean> = _r1Pressed.asStateFlow()

    private val _totalL1Shots = MutableStateFlow(0)
    val totalL1Shots: StateFlow<Int> = _totalL1Shots.asStateFlow()

    private val _totalR1Shots = MutableStateFlow(0)
    val totalR1Shots: StateFlow<Int> = _totalR1Shots.asStateFlow()

    private val _fireEvents = MutableSharedFlow<TriggerFireEvent>(extraBufferCapacity = 64)
    val fireEvents: SharedFlow<TriggerFireEvent> = _fireEvents.asSharedFlow()

    private val _quickBoostRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    val quickBoostRequests: SharedFlow<Unit> = _quickBoostRequests.asSharedFlow()

    fun updateTriggerConfig(config: ShoulderTriggerConfig) {
        _triggerConfig.value = config
    }

    fun updateCrosshairConfig(config: CrosshairConfig) {
        _crosshairConfig.value = config
    }

    fun updateEdgeSidebarConfig(config: EdgeSidebarConfig) {
        _edgeSidebarConfig.value = config
    }

    fun updateLowEndConfig(config: LowEndOptimizerConfig) {
        _lowEndConfig.value = config
    }

    fun updateTelemetry(snapshot: HardwareTelemetry) {
        _telemetry.value = snapshot
    }

    fun updatePerformanceMode(mode: PerformanceMode) {
        _performanceMode.value = mode
    }

    fun setAccessibilityServiceRunning(running: Boolean) {
        _isAccessibilityServiceRunning.value = running
    }

    fun setOverlayServiceRunning(running: Boolean) {
        _isOverlayServiceRunning.value = running
    }

    fun setInAppSidebarOpen(open: Boolean) {
        _isInAppSidebarOpen.value = open
    }

    fun requestQuickBoostFromOverlay() {
        _quickBoostRequests.tryEmit(Unit)
    }

    fun onTriggerKeyStateChanged(button: TriggerButtonType, isDown: Boolean) {
        val cfg = _triggerConfig.value
        when (button) {
            TriggerButtonType.L1_VOLUME_UP -> {
                _l1Pressed.value = isDown
                if (isDown) {
                    _totalL1Shots.value += 1
                    _fireEvents.tryEmit(
                        TriggerFireEvent(
                            button = TriggerButtonType.L1_VOLUME_UP,
                            isPressed = true,
                            xRatio = cfg.l1XRatio,
                            yRatio = cfg.l1YRatio
                        )
                    )
                } else {
                    _fireEvents.tryEmit(
                        TriggerFireEvent(
                            button = TriggerButtonType.L1_VOLUME_UP,
                            isPressed = false,
                            xRatio = cfg.l1XRatio,
                            yRatio = cfg.l1YRatio
                        )
                    )
                }
            }
            TriggerButtonType.R1_VOLUME_DOWN -> {
                _r1Pressed.value = isDown
                if (isDown) {
                    _totalR1Shots.value += 1
                    if (cfg.comboLinkLR) {
                        _totalL1Shots.value += 1
                        _fireEvents.tryEmit(
                            TriggerFireEvent(
                                button = TriggerButtonType.L1_VOLUME_UP,
                                isPressed = true,
                                xRatio = cfg.l1XRatio,
                                yRatio = cfg.l1YRatio
                            )
                        )
                    }
                    _fireEvents.tryEmit(
                        TriggerFireEvent(
                            button = TriggerButtonType.R1_VOLUME_DOWN,
                            isPressed = true,
                            xRatio = cfg.r1XRatio,
                            yRatio = cfg.r1YRatio
                        )
                    )
                } else {
                    _fireEvents.tryEmit(
                        TriggerFireEvent(
                            button = TriggerButtonType.R1_VOLUME_DOWN,
                            isPressed = false,
                            xRatio = cfg.r1XRatio,
                            yRatio = cfg.r1YRatio
                        )
                    )
                }
            }
        }
    }

    fun emitBurstPulse(button: TriggerButtonType) {
        val cfg = _triggerConfig.value
        when (button) {
            TriggerButtonType.L1_VOLUME_UP -> {
                _totalL1Shots.value += 1
                _fireEvents.tryEmit(
                    TriggerFireEvent(
                        button = TriggerButtonType.L1_VOLUME_UP,
                        isPressed = true,
                        xRatio = cfg.l1XRatio,
                        yRatio = cfg.l1YRatio
                    )
                )
            }
            TriggerButtonType.R1_VOLUME_DOWN -> {
                _totalR1Shots.value += 1
                _fireEvents.tryEmit(
                    TriggerFireEvent(
                        button = TriggerButtonType.R1_VOLUME_DOWN,
                        isPressed = true,
                        xRatio = cfg.r1XRatio,
                        yRatio = cfg.r1YRatio
                    )
                )
            }
        }
    }

    fun resetShotCounters() {
        _totalL1Shots.value = 0
        _totalR1Shots.value = 0
    }
}
