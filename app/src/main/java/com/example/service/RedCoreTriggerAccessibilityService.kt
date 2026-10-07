package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.example.model.CrosshairConfig
import com.example.model.CrosshairStyle
import com.example.model.TriggerFireMode
import kotlinx.coroutines.CoroutineScope
import																											kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RedCoreTriggerAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var l1BurstJob: Job? = null
    private var r1BurstJob: Job? = null
    private val clonedBurstJobs = mutableMapOf<Int, Job>()
    private var l1KeyHeld = false
    private var r1KeyHeld = false

    private var windowManager: WindowManager? = null
    private var crosshairOverlayView: SystemCrosshairOverlayView? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        TriggerEventBus.setAccessibilityServiceRunning(true)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager

        serviceScope.launch {
            TriggerEventBus.crosshairConfig.collectLatest { config ->
                updateSystemCrosshairOverlay(config)
            }
        }

        serviceScope.launch {
            TriggerEventBus.clonedTapEvents.collectLatest { event ->
                handleClonedButtonTapEvent(event)
            }
        }

        serviceScope.launch {
            TriggerEventBus.isMasterEngineRunning.collectLatest { running ->
                if (!running) {
                    onInterrupt()
                    updateSystemCrosshairOverlay(
                        TriggerEventBus.crosshairConfig.value.copy(systemOverlayEnabled = false)
                    )
                } else {
                    updateSystemCrosshairOverlay(TriggerEventBus.crosshairConfig.value)
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            // Ignore system UI overlays so we track the actual foreground app/game
            if (pkg != packageName && !pkg.startsWith("com.android.systemui")) {
                TriggerEventBus.onForegroundPackageChanged(pkg)
            }
        }
    }

    override fun onInterrupt() {
        l1BurstJob?.cancel()
        r1BurstJob?.cancel()
        clonedBurstJobs.values.forEach { it.cancel() }
        clonedBurstJobs.clear()
    }

    private fun handleClonedButtonTapEvent(event: ClonedTapEvent) {
        val btn = event.button
        if (!btn.enabled) return

        if (!event.isPressed) {
            clonedBurstJobs.remove(btn.id)?.cancel()
            return
        }

        val (screenW, screenH) = getScreenDimensions()
        val targetX = (btn.targetXRatio * screenW).coerceIn(10f, screenW - 10f)
        val targetY = (btn.targetYRatio * screenH).coerceIn(10f, screenH - 10f)

        triggerFeedback(haptic = true, sound = false, isL1 = true)

        when (btn.fireMode) {
            TriggerFireMode.SINGLE_TAP -> {
                dispatchScreenTap(targetX, targetY, 38L)
            }
            TriggerFireMode.DOUBLE_TAP -> {
                serviceScope.launch {
                    dispatchScreenTap(targetX, targetY, 30L)
                    delay(60L)
                    dispatchScreenTap(targetX, targetY, 30L)
                }
            }
            TriggerFireMode.HOLD_PRESS -> {
                clonedBurstJobs.remove(btn.id)?.cancel()
                clonedBurstJobs[btn.id] = serviceScope.launch {
                    while (isActive) {
                        dispatchScreenTap(targetX, targetY, 360L)
                        delay(340L)
                    }
                }
            }
            TriggerFireMode.RAPID_BURST -> {
                val safeRps = btn.burstRps.coerceIn(4, 20)
                val intervalMs = (1000L / safeRps).coerceAtLeast(45L)
                clonedBurstJobs.remove(btn.id)?.cancel()
                clonedBurstJobs[btn.id] = serviceScope.launch {
                    while (isActive) {
                        dispatchScreenTap(targetX, targetY, 26L)
                        delay(intervalMs)
                    }
                }
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val config = TriggerEventBus.triggerConfig.value
        if (!TriggerEventBus.isMasterEngineRunning.value || !config.enabled) {
            return super.onKeyEvent(event)
        }

        val keyCode = event.keyCode
        val action = event.action

        if (keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            return super.onKeyEvent(event)
        }

        val (screenW, screenH) = getScreenDimensions()

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            val targetX = (config.l1XRatio * screenW).coerceIn(10f, screenW - 10f)
            val targetY = (config.l1YRatio * screenH).coerceIn(10f, screenH - 10f)

            if (action == KeyEvent.ACTION_DOWN) {
                if (!l1KeyHeld) {
                    l1KeyHeld = true
                    TriggerEventBus.onTriggerKeyStateChanged(TriggerButtonType.L1_VOLUME_UP, true)
                    triggerFeedback(config.hapticFeedback, config.soundEffect, isL1 = true)
                    executeTriggerAction(
                        button = TriggerButtonType.L1_VOLUME_UP,
                        mode = config.l1Mode,
                        burstRps = config.l1BurstRps,
                        x = targetX,
                        y = targetY
                    )
                }
                return true
            } else if (action == KeyEvent.ACTION_UP) {
                l1KeyHeld = false
                l1BurstJob?.cancel()
                TriggerEventBus.onTriggerKeyStateChanged(TriggerButtonType.L1_VOLUME_UP, false)
                return true
            }
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            val targetX = (config.r1XRatio * screenW).coerceIn(10f, screenW - 10f)
            val targetY = (config.r1YRatio * screenH).coerceIn(10f, screenH - 10f)

            if (action == KeyEvent.ACTION_DOWN) {
                if (!r1KeyHeld) {
                    r1KeyHeld = true
                    TriggerEventBus.onTriggerKeyStateChanged(TriggerButtonType.R1_VOLUME_DOWN, true)
                    triggerFeedback(config.hapticFeedback, config.soundEffect, isL1 = false)

                    if (config.comboLinkLR) {
                        val l1X = (config.l1XRatio * screenW).coerceIn(10f, screenW - 10f)
                        val l1Y = (config.l1YRatio * screenH).coerceIn(10f, screenH - 10f)
                        dispatchScreenTap(l1X, l1Y, 35L)
                    }

                    executeTriggerAction(
                        button = TriggerButtonType.R1_VOLUME_DOWN,
                        mode = config.r1Mode,
                        burstRps = config.r1BurstRps,
                        x = targetX,
                        y = targetY
                    )
                }
                return true
            } else if (action == KeyEvent.ACTION_UP) {
                r1KeyHeld = false
                r1BurstJob?.cancel()
                TriggerEventBus.onTriggerKeyStateChanged(TriggerButtonType.R1_VOLUME_DOWN, false)
                return true
            }
        }

        return super.onKeyEvent(event)
    }

    private fun executeTriggerAction(
        button: TriggerButtonType,
        mode: TriggerFireMode,
        burstRps: Int,
        x: Float,
        y: Float
    ) {
        when (mode) {
            TriggerFireMode.SINGLE_TAP -> {
                dispatchScreenTap(x, y, 40L)
            }
            TriggerFireMode.DOUBLE_TAP -> {
                serviceScope.launch {
                    dispatchScreenTap(x, y, 32L)
                    delay(65L)
                    TriggerEventBus.emitBurstPulse(button)
                    dispatchScreenTap(x, y, 32L)
                }
            }
            TriggerFireMode.HOLD_PRESS -> {
                val job = serviceScope.launch {
                    while (isActive) {
                        dispatchScreenTap(x, y, 380L)
                        delay(360L)
                    }
                }
                if (button == TriggerButtonType.L1_VOLUME_UP) {
                    l1BurstJob?.cancel()
                    l1BurstJob = job
                } else {
                    r1BurstJob?.cancel()
                    r1BurstJob = job
                }
            }
            TriggerFireMode.RAPID_BURST -> {
                val safeRps = burstRps.coerceIn(4, 20)
                val intervalMs = (1000L / safeRps).coerceAtLeast(45L)
                val job = serviceScope.launch {
                    while (isActive) {
                        dispatchScreenTap(x, y, 26L)
                        delay(intervalMs)
                        if (isActive) {
                            TriggerEventBus.emitBurstPulse(button)
                        }
                    }
                }
                if (button == TriggerButtonType.L1_VOLUME_UP) {
                    l1BurstJob?.cancel()
                    l1BurstJob = job
                } else {
                    r1BurstJob?.cancel()
                    r1BurstJob = job
                }
            }
        }
    }

    private fun dispatchScreenTap(x: Float, y: Float, durationMs: Long) {
        try {
            val clickPath = Path().apply {
                moveTo(x, y)
            }
            val stroke = GestureDescription.StrokeDescription(clickPath, 0L, durationMs)
            val gesture = GestureDescription.Builder()
                .addStroke(stroke)
                .build()
            dispatchGesture(gesture, null, null)
        } catch (_: Exception) {
        }
    }

    private fun triggerFeedback(haptic: Boolean, sound: Boolean, isL1: Boolean) {
        if (haptic) {
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vm?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(18L, 200))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(18L)
                }
            } catch (_: Exception) {
            }
        }
        if (sound) {
            serviceScope.launch(Dispatchers.Default) {
                var tg: ToneGenerator? = null
                try {
                    tg = ToneGenerator(AudioManager.STREAM_MUSIC, 55)
                    val tone = if (isL1) ToneGenerator.TONE_PROP_BEEP else ToneGenerator.TONE_PROP_ACK
                    tg.startTone(tone, 22)
                    delay(45L)
                } catch (_: Exception) {
                } finally {
                    try {
                        tg?.release()
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    private fun getScreenDimensions(): Pair<Float, Float> {
        val wm = windowManager ?: return 1080f to 2400f
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            bounds.width().toFloat() to bounds.height().toFloat()
        } else {
            val dm = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(dm)
            dm.widthPixels.toFloat() to dm.heightPixels.toFloat()
        }
    }

    private fun updateSystemCrosshairOverlay(config: CrosshairConfig) {
        val wm = windowManager ?: return
        if (!config.systemOverlayEnabled) {
            crosshairOverlayView?.let {
                try {
                    wm.removeView(it)
                } catch (_: Exception) {
                }
            }
            crosshairOverlayView = null
            return
        }

        val density = resources.displayMetrics.density
        val boxPx = (120 * density).toInt()
        val params = WindowManager.LayoutParams(
            boxPx,
            boxPx,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            x = (config.offsetX * density).toInt()
            y = (config.offsetY * density).toInt()
        }

        val existing = crosshairOverlayView
        if (existing == null) {
            val view = SystemCrosshairOverlayView(this, config)
            try {
                wm.addView(view, params)
                crosshairOverlayView = view
            } catch (_: Exception) {
            }
        } else {
            existing.updateConfig(config)
            try {
                wm.updateViewLayout(existing, params)
            } catch (_: Exception) {
            }
        }
    }

    override fun onDestroy() {
        TriggerEventBus.setAccessibilityServiceRunning(false)
        l1BurstJob?.cancel()
        r1BurstJob?.cancel()
        crosshairOverlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {
            }
        }
        serviceScope.cancel()
        super.onDestroy()
    }
}

private class SystemCrosshairOverlayView(
    context: Context,
    private var config: CrosshairConfig
) : View(context) {

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun updateConfig(newConfig: CrosshairConfig) {
        config = newConfig
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val cx = width / 2f
        val cy = height / 2f
        val radius = (config.sizeDp * density) / 2f

        val baseColor = config.colorOption.androidColorInt
        val alphaInt = (config.opacity.coerceIn(0.2f, 1f) * 255).toInt()

        strokePaint.color = baseColor
        strokePaint.alpha = alphaInt
        strokePaint.strokeWidth = config.strokeWidthDp * density

        fillPaint.color = baseColor
        fillPaint.alpha = alphaInt

        when (config.style) {
            CrosshairStyle.RED_DOT -> {
                canvas.drawCircle(cx, cy, (radius * 0.28f).coerceAtLeast(4f), fillPaint)
            }
            CrosshairStyle.TACTICAL_CROSS -> {
                val gap = radius * 0.25f
                canvas.drawLine(cx - radius, cy, cx - gap, cy, strokePaint)
                canvas.drawLine(cx + gap, cy, cx + radius, cy, strokePaint)
                canvas.drawLine(cx, cy - radius, cx, cy - gap, strokePaint)
                canvas.drawLine(cx, cy + gap, cx, cy + radius, strokePaint)
                canvas.drawCircle(cx, cy, 2.5f * density, fillPaint)
            }
            CrosshairStyle.CIRCLE_DOT -> {
                canvas.drawCircle(cx, cy, radius * 0.72f, strokePaint)
                canvas.drawCircle(cx, cy, 3f * density, fillPaint)
                canvas.drawLine(cx - radius, cy, cx - radius * 0.5f, cy, strokePaint)
                canvas.drawLine(cx + radius * 0.5f, cy, cx + radius, cy, strokePaint)
                canvas.drawLine(cx, cy - radius, cx, cy - radius * 0.5f, strokePaint)
                canvas.drawLine(cx, cy + radius * 0.5f, cx, cy + radius, strokePaint)
            }
            CrosshairStyle.SNIPER_CHEVRON -> {
                val path = Path().apply {
                    moveTo(cx - radius * 0.65f, cy + radius * 0.45f)
                    lineTo(cx, cy)
                    lineTo(cx + radius * 0.65f, cy + radius * 0.45f)
                }
                canvas.drawPath(path, strokePaint)
                canvas.drawCircle(cx, cy - radius * 0.2f, 2.5f * density, fillPaint)
            }
            CrosshairStyle.CYBER_DIAMOND -> {
                val path = Path().apply {
                    moveTo(cx, cy - radius * 0.8f)
                    lineTo(cx + radius * 0.8f, cy)
                    lineTo(cx, cy + radius * 0.8f)
                    lineTo(cx - radius * 0.8f, cy)
                    close()
                }
                canvas.drawPath(path, strokePaint)
                canvas.drawCircle(cx, cy, 3f * density, fillPaint)
            }
            CrosshairStyle.PREDATOR_TRI -> {
                canvas.drawCircle(cx, cy - radius * 0.55f, 3.2f * density, fillPaint)
                canvas.drawCircle(cx - radius * 0.5f, cy + radius * 0.4f, 3.2f * density, fillPaint)
                canvas.drawCircle(cx + radius * 0.5f, cy + radius * 0.4f, 3.2f * density, fillPaint)
                canvas.drawCircle(cx, cy, 1.8f * density, fillPaint)
            }
            CrosshairStyle.HOLLOW_RING -> {
                canvas.drawCircle(cx, cy, radius * 0.65f, strokePaint)
            }
            CrosshairStyle.PULSE_CORE -> {
                canvas.drawCircle(cx, cy, radius * 0.85f, strokePaint)
                canvas.drawCircle(cx, cy, radius * 0.4f, strokePaint)
                canvas.drawCircle(cx, cy, 3f * density, fillPaint)
            }
        }
    }
}
