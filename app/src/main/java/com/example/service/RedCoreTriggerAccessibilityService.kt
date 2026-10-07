package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.accessibility.AccessibilityEvent
import com.example.model.ClonedButtonMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Accessibility Service for On-Screen Touch Button Cloner (C1-C4) and Foreground Game Detection.
 * Dispatches real screen tap gestures (`dispatchGesture`) at target coordinates when the user
 * touches a cloned on-screen button.
 */
class RedCoreTriggerAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var clonedButtonsJob: Job? = null
    private val activeClonedLoops = mutableMapOf<Int, Job>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        TriggerEventBus.setServiceConnected(true)
        observeClonedButtonEvents()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            if (pkg != packageName && !pkg.startsWith("com.android.systemui")) {
                val activeGamePkg = TriggerEventBus.activeGamePackage.value
                if (activeGamePkg != null && pkg == activeGamePkg) {
                    TriggerEventBus.setMasterEngineRunning(true)
                }
            }
        }
    }

    override fun onInterrupt() {
        stopAllLoops()
    }

    override fun onDestroy() {
        stopAllLoops()
        TriggerEventBus.setServiceConnected(false)
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun observeClonedButtonEvents() {
        clonedButtonsJob?.cancel()
        clonedButtonsJob = serviceScope.launch {
            TriggerEventBus.clonedTapEvents.collect { tapEvent ->
                val clonedConfig = TriggerEventBus.clonedButtonsConfig.value
                if (!clonedConfig.enabled) return@collect
                val button = clonedConfig.buttons.firstOrNull { it.id == tapEvent.buttonId } ?: return@collect

                if (tapEvent.isDown) {
                    if (clonedConfig.hapticFeedback) {
                        triggerMicroHaptic()
                    }
                    when (button.mode) {
                        ClonedButtonMode.SINGLE_TAP -> {
                            dispatchClonedTapAtCoord(button.targetX, button.targetY)
                        }
                        ClonedButtonMode.TURBO_HOLD, ClonedButtonMode.AUTO_LOCK -> {
                            activeClonedLoops[button.id]?.cancel()
                            val interval = button.mode.intervalMs.coerceAtLeast(60L)
                            activeClonedLoops[button.id] = serviceScope.launch {
                                while (isActive) {
                                    val latestBtn = TriggerEventBus.clonedButtonsConfig.value
                                        .buttons.firstOrNull { it.id == button.id } ?: break
                                    dispatchClonedTapAtCoord(latestBtn.targetX, latestBtn.targetY)
                                    delay(interval)
                                }
                            }
                        }
                    }
                } else {
                    activeClonedLoops.remove(button.id)?.cancel()
                }
            }
        }
    }

    private fun dispatchClonedTapAtCoord(normX: Float, normY: Float) {
        val metrics = resources.displayMetrics
        val screenW = metrics.widthPixels.coerceAtLeast(100)
        val screenH = metrics.heightPixels.coerceAtLeast(100)

        val px = (normX.coerceIn(0.03f, 0.97f) * screenW)
        val py = (normY.coerceIn(0.03f, 0.97f) * screenH)

        val path = Path().apply {
            moveTo(px, py)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, 28L)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        dispatchGesture(gesture, null, null)
    }

    private fun triggerMicroHaptic() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(12L, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {
        }
    }

    private fun stopAllLoops() {
        activeClonedLoops.values.forEach { it.cancel() }
        activeClonedLoops.clear()
    }
}
