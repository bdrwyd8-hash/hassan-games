package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.example.MainActivity
import com.example.model.ClonedButtonsConfig
import com.example.model.ClonedTouchButton
import com.example.model.CrosshairConfig
import com.example.model.CrosshairStyle
import com.example.model.PerformanceMode
import com.example.model.ScreenVisionFilter
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FloatingGameSidebarService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var windowManager: WindowManager? = null

    private var edgeHandleView: FloatingEdgeHandleView? = null
    private var handleLayoutParams: WindowManager.LayoutParams? = null

    private var expandedPanelView: FloatingTurboPanelView? = null
    private var isPanelExpanded = false

    private var crosshairView: OverlayCrosshairView? = null
    private var visionFilterView: View? = null
    private var hudPillView: FloatingHudPillOverlayView? = null
    private var hudPillParams: WindowManager.LayoutParams? = null
    private var magnifierView: FloatingMagnifierOverlayView? = null
    private var magnifierParams: WindowManager.LayoutParams? = null
    private val clonedButtonViews = mutableMapOf<Int, View>()
    private val clonedTargetViews = mutableMapOf<Int, View>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        TriggerEventBus.setOverlayServiceRunning(true)

        setupEdgeHandleOverlay()

        serviceScope.launch {
            TriggerEventBus.edgeSidebarConfig.collectLatest { cfg ->
                val lowEnd = TriggerEventBus.lowEndConfig.value
                val anyExtraOverlay = TriggerEventBus.clonedButtonsConfig.value.systemOverlayEnabled ||
                    lowEnd.fpsOverlayEnabled || lowEnd.tempOverlayEnabled ||
                    lowEnd.ramOverlayEnabled || lowEnd.magnifierEnabled
                if (!cfg.systemFloatingEnabled && !anyExtraOverlay) {
                    stopSelf()
                } else {
                    updateEdgeHandleSide(cfg.isRightEdge)
                }
            }
        }
        serviceScope.launch {
            TriggerEventBus.crosshairConfig.collectLatest { cfg ->
                updateFloatingCrosshair(cfg)
            }
        }
        serviceScope.launch {
            TriggerEventBus.lowEndConfig.collectLatest { cfg ->
                updateVisionFilterOverlay(cfg.visionFilter)
                updateHudPillOverlay(
                    showFps = cfg.fpsOverlayEnabled,
                    showTemp = cfg.tempOverlayEnabled,
                    showRam = cfg.ramOverlayEnabled
                )
                updateMagnifierOverlay(
                    enabled = cfg.magnifierEnabled,
                    zoom = cfg.magnifierZoom
                )
                expandedPanelView?.invalidate()
            }
        }
        serviceScope.launch {
            TriggerEventBus.telemetry.collectLatest {
                edgeHandleView?.invalidate()
                hudPillView?.invalidate()
                expandedPanelView?.invalidate()
            }
        }
        serviceScope.launch {
            TriggerEventBus.notificationShieldState.collectLatest {
                expandedPanelView?.invalidate()
            }
        }
        serviceScope.launch {
            TriggerEventBus.clonedButtonsConfig.collectLatest { cfg ->
                updateFloatingClonedButtonsOverlay(cfg)
                expandedPanelView?.invalidate()
            }
        }
        serviceScope.launch {
            TriggerEventBus.shutdownAndExitRequests.collectLatest {
                stopSelf()
            }
        }
    }

    private fun overlayWindowType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private fun setupEdgeHandleOverlay() {
        val wm = windowManager ?: return
        val density = resources.displayMetrics.density
        val cfg = TriggerEventBus.edgeSidebarConfig.value

        val handleW = (30 * density).toInt()
        val handleH = (128 * density).toInt()

        val params = WindowManager.LayoutParams(
            handleW,
            handleH,
            overlayWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or if (cfg.isRightEdge) Gravity.END else Gravity.START
            x = 0
            y = (resources.displayMetrics.heightPixels * 0.32f).toInt()
        }
        handleLayoutParams = params

        val view = FloatingEdgeHandleView(
            context = this,
            onSwipeOrTapExpand = { showExpandedPanel() },
            onDragVertical = { deltaY ->
                val p = handleLayoutParams ?: return@FloatingEdgeHandleView
                p.y = (p.y + deltaY.toInt()).coerceIn(80, resources.displayMetrics.heightPixels - 260)
                try {
                    wm.updateViewLayout(edgeHandleView, p)
                } catch (_: Exception) {
                }
            }
        )

        try {
            wm.addView(view, params)
            edgeHandleView = view
        } catch (_: Exception) {
        }
    }

    private fun updateEdgeHandleSide(isRightEdge: Boolean) {
        val wm = windowManager ?: return
        val p = handleLayoutParams ?: return
        val v = edgeHandleView ?: return
        p.gravity = Gravity.TOP or if (isRightEdge) Gravity.END else Gravity.START
        try {
            wm.updateViewLayout(v, p)
            v.invalidate()
        } catch (_: Exception) {
        }
    }

    private fun showExpandedPanel() {
        if (isPanelExpanded) return
        val wm = windowManager ?: return
        val density = resources.displayMetrics.density
        val cfg = TriggerEventBus.edgeSidebarConfig.value

        val panelW = (308 * density).toInt()
        val panelH = (486 * density).toInt()

        val params = WindowManager.LayoutParams(
            panelW,
            panelH,
            overlayWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER_VERTICAL or if (cfg.isRightEdge) Gravity.END else Gravity.START
            x = (10 * density).toInt()
            y = 0
        }

        val panel = FloatingTurboPanelView(
            context = this,
            onClosePanel = { hideExpandedPanel() },
            onOpenMainApp = {
                hideExpandedPanel()
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(intent)
            }
        )

        try {
            wm.addView(panel, params)
            expandedPanelView = panel
            isPanelExpanded = true
        } catch (_: Exception) {
        }
    }

    private fun hideExpandedPanel() {
        val wm = windowManager ?: return
        expandedPanelView?.let {
            try {
                wm.removeView(it)
            } catch (_: Exception) {
            }
        }
        expandedPanelView = null
        isPanelExpanded = false
    }

    private fun updateFloatingCrosshair(config: CrosshairConfig) {
        val wm = windowManager ?: return
        if (!config.systemOverlayEnabled) {
            crosshairView?.let {
                try {
                    wm.removeView(it)
                } catch (_: Exception) {
                }
            }
            crosshairView = null
            return
        }

        val density = resources.displayMetrics.density
        val boxPx = (110 * density).toInt()
        val params = WindowManager.LayoutParams(
            boxPx,
            boxPx,
            overlayWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            x = (config.offsetX * density).toInt()
            y = (config.offsetY * density).toInt()
        }

        val existing = crosshairView
        if (existing == null) {
            val v = OverlayCrosshairView(this, config)
            try {
                wm.addView(v, params)
                crosshairView = v
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

    private fun updateVisionFilterOverlay(filter: ScreenVisionFilter) {
        val wm = windowManager ?: return
        if (filter == ScreenVisionFilter.NONE) {
            visionFilterView?.let {
                try {
                    wm.removeView(it)
                } catch (_: Exception) {
                }
            }
            visionFilterView = null
            return
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        val existing = visionFilterView
        if (existing == null) {
            val v = View(this).apply {
                setBackgroundColor(filter.androidTintHex)
            }
            try {
                wm.addView(v, params)
                visionFilterView = v
            } catch (_: Exception) {
            }
        } else {
            existing.setBackgroundColor(filter.androidTintHex)
        }
    }

    private fun updateHudPillOverlay(showFps: Boolean, showTemp: Boolean, showRam: Boolean) {
        val wm = windowManager ?: return
        if (!showFps && !showTemp && !showRam) {
            hudPillView?.let {
                try {
                    wm.removeView(it)
                } catch (_: Exception) {
                }
            }
            hudPillView = null
            hudPillParams = null
            return
        }

        val density = resources.displayMetrics.density
        val activeCount = listOf(showFps, showTemp, showRam).count { it }.coerceAtLeast(1)
        val pillW = ((82 * activeCount + 24) * density).toInt()
        val pillH = (32 * density).toInt()

        val existing = hudPillView
        if (existing == null) {
            val params = WindowManager.LayoutParams(
                pillW,
                pillH,
                overlayWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = (24 * density).toInt()
                y = (44 * density).toInt()
            }
            hudPillParams = params
            val v = FloatingHudPillOverlayView(
                context = this,
                showFps = showFps,
                showTemp = showTemp,
                showRam = showRam,
                onDragDelta = { dx, dy ->
                    val p = hudPillParams ?: return@FloatingHudPillOverlayView
                    p.x = (p.x + dx.toInt()).coerceIn(0, (resources.displayMetrics.widthPixels - pillW).coerceAtLeast(0))
                    p.y = (p.y + dy.toInt()).coerceIn(20, (resources.displayMetrics.heightPixels - pillH - 20).coerceAtLeast(20))
                    try {
                        wm.updateViewLayout(hudPillView, p)
                    } catch (_: Exception) {
                    }
                }
            )
            try {
                wm.addView(v, params)
                hudPillView = v
            } catch (_: Exception) {
            }
        } else {
            existing.updateFlags(showFps, showTemp, showRam)
            hudPillParams?.let { p ->
                p.width = pillW
                try {
                    wm.updateViewLayout(existing, p)
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun updateMagnifierOverlay(enabled: Boolean, zoom: Float) {
        val wm = windowManager ?: return
        if (!enabled) {
            magnifierView?.let {
                try {
                    wm.removeView(it)
                } catch (_: Exception) {
                }
            }
            magnifierView = null
            magnifierParams = null
            return
        }

        val density = resources.displayMetrics.density
        val sizePx = ((112f + (zoom - 1.5f) * 24f).coerceIn(104f, 168f) * density).toInt()
        val existing = magnifierView
        if (existing == null) {
            val params = WindowManager.LayoutParams(
                sizePx,
                sizePx,
                overlayWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                x = 0
                y = (-42 * density).toInt()
            }
            magnifierParams = params
            val v = FloatingMagnifierOverlayView(
                context = this,
                zoom = zoom,
                onDragDelta = { dx, dy ->
                    val p = magnifierParams ?: return@FloatingMagnifierOverlayView
                    p.x += dx.toInt()
                    p.y += dy.toInt()
                    try {
                        wm.updateViewLayout(magnifierView, p)
                    } catch (_: Exception) {
                    }
                }
            )
            try {
                wm.addView(v, params)
                magnifierView = v
            } catch (_: Exception) {
            }
        } else {
            existing.updateZoom(zoom)
            magnifierParams?.let { p ->
                p.width = sizePx
                p.height = sizePx
                try {
                    wm.updateViewLayout(existing, p)
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun updateFloatingClonedButtonsOverlay(config: ClonedButtonsConfig) {
        val wm = windowManager ?: return
        removeAllClonedViews(wm)

        if (!config.masterEnabled || !config.systemOverlayEnabled) {
            return
        }

        val density = resources.displayMetrics.density
        val screenW = resources.displayMetrics.widthPixels.toFloat().coerceAtLeast(720f)
        val screenH = resources.displayMetrics.heightPixels.toFloat().coerceAtLeast(1280f)
        val btnPx = (config.buttonSizeDp.coerceIn(38f, 84f) * density).toInt()
        val targetPx = (38f * density).toInt()

        for (btn in config.buttons.filter { it.enabled }) {
            // 1. Floating Cloned Action Button (where the user presses)
            val btnParams = WindowManager.LayoutParams(
                btnPx,
                btnPx,
                overlayWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = ((btn.buttonXRatio * screenW) - (btnPx / 2f)).toInt().coerceIn(0, (screenW - btnPx).toInt())
                y = ((btn.buttonYRatio * screenH) - (btnPx / 2f)).toInt().coerceIn(0, (screenH - btnPx).toInt())
            }

            val actionView = FloatingClonedButtonOverlayView(
                context = this,
                button = btn,
                isLockedForPlay = config.isLockedForPlay,
                opacity = config.buttonOpacity,
                onDragDelta = { dx, dy ->
                    val newX = ((btnParams.x + dx + btnPx / 2f) / screenW).coerceIn(0.05f, 0.95f)
                    val newY = ((btnParams.y + dy + btnPx / 2f) / screenH).coerceIn(0.08f, 0.92f)
                    btnParams.x = ((newX * screenW) - btnPx / 2f).toInt()
                    btnParams.y = ((newY * screenH) - btnPx / 2f).toInt()
                    try {
                        wm.updateViewLayout(clonedButtonViews[btn.id], btnParams)
                    } catch (_: Exception) {
                    }
                    val curCfg = TriggerEventBus.clonedButtonsConfig.value
                    TriggerEventBus.updateClonedButtonsConfig(
                        curCfg.copy(
                            buttons = curCfg.buttons.map {
                                if (it.id == btn.id) it.copy(buttonXRatio = newX, buttonYRatio = newY) else it
                            }
                        )
                    )
                },
                onTapDownUp = { isDown ->
                    TriggerEventBus.emitClonedButtonTap(btn, isDown = isDown)
                }
            )
            try {
                wm.addView(actionView, btnParams)
                clonedButtonViews[btn.id] = actionView
            } catch (_: Exception) {
            }

            // 2. Original Target Marker (only shown when unlocked/editing positions)
            if (!config.isLockedForPlay) {
                val targetParams = WindowManager.LayoutParams(
                    targetPx,
                    targetPx,
                    overlayWindowType(),
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = ((btn.targetXRatio * screenW) - (targetPx / 2f)).toInt().coerceIn(0, (screenW - targetPx).toInt())
                    y = ((btn.targetYRatio * screenH) - (targetPx / 2f)).toInt().coerceIn(0, (screenH - targetPx).toInt())
                }

                val targetView = FloatingClonedTargetOverlayView(
                    context = this,
                    button = btn,
                    onDragDelta = { dx, dy ->
                        val newTX = ((targetParams.x + dx + targetPx / 2f) / screenW).coerceIn(0.05f, 0.95f)
                        val newTY = ((targetParams.y + dy + targetPx / 2f) / screenH).coerceIn(0.08f, 0.92f)
                        targetParams.x = ((newTX * screenW) - targetPx / 2f).toInt()
                        targetParams.y = ((newTY * screenH) - targetPx / 2f).toInt()
                        try {
                            wm.updateViewLayout(clonedTargetViews[btn.id], targetParams)
                        } catch (_: Exception) {
                        }
                        val curCfg = TriggerEventBus.clonedButtonsConfig.value
                        TriggerEventBus.updateClonedButtonsConfig(
                            curCfg.copy(
                                buttons = curCfg.buttons.map {
                                    if (it.id == btn.id) it.copy(targetXRatio = newTX, targetYRatio = newTY) else it
                                }
                            )
                        )
                    }
                )
                try {
                    wm.addView(targetView, targetParams)
                    clonedTargetViews[btn.id] = targetView
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun removeAllClonedViews(wm: WindowManager?) {
        clonedButtonViews.values.forEach { v ->
            try {
                wm?.removeView(v)
            } catch (_: Exception) {
            }
        }
        clonedButtonViews.clear()
        clonedTargetViews.values.forEach { v ->
            try {
                wm?.removeView(v)
            } catch (_: Exception) {
            }
        }
        clonedTargetViews.clear()
    }

    override fun onDestroy() {
        TriggerEventBus.setOverlayServiceRunning(false)
        val wm = windowManager
        removeAllClonedViews(wm)
        listOfNotNull(
            edgeHandleView,
            expandedPanelView,
            crosshairView,
            visionFilterView,
            hudPillView,
            magnifierView
        ).forEach { v ->
            try {
                wm?.removeView(v)
            } catch (_: Exception) {
            }
        }
        serviceScope.cancel()
        super.onDestroy()
    }
}

/**
 * Sleek glowing neon edge handle that detects swipe-inward or tap to open the Game Turbo panel.
 */
private class FloatingEdgeHandleView(
    context: Context,
    private val onSwipeOrTapExpand: () -> Unit,
    private val onDragVertical: (Float) -> Unit
) : View(context) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xE60E1017.toInt()
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF1E38.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val cyanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF00F0FF.toInt()
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 24f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private var downX = 0f
    private var downY = 0f
    private var lastY = 0f
    private var movedVertically = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rect = RectF(4f, 4f, width - 4f, height - 4f)
        canvas.drawRoundRect(rect, 22f, 22f, bgPaint)
        canvas.drawRoundRect(rect, 22f, 22f, strokePaint)

        // Glowing bar indicator
        val barRect = RectF(width * 0.38f, height * 0.2f, width * 0.62f, height * 0.52f)
        canvas.drawRoundRect(barRect, 6f, 6f, cyanPaint)

        // Live FPS readout on handle
        val fps = TriggerEventBus.telemetry.value.liveFps
        textPaint.textSize = width * 0.38f
        canvas.drawText("$fps", width / 2f, height * 0.74f, textPaint)
        textPaint.textSize = width * 0.26f
        textPaint.color = 0xFF00E676.toInt()
        canvas.drawText("FPS", width / 2f, height * 0.88f, textPaint)
        textPaint.color = Color.WHITE
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                downY = event.rawY
                lastY = event.rawY
                movedVertically = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = abs(event.rawX - downX)
                val dy = event.rawY - lastY
                if (dx > 28f && !movedVertically) {
                    onSwipeOrTapExpand()
                    return true
                }
                if (abs(event.rawY - downY) > 18f) {
                    movedVertically = true
                    onDragVertical(dy)
                    lastY = event.rawY
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!movedVertically) {
                    onSwipeOrTapExpand()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}

/**
 * Interactive Floating Game Turbo Panel drawn over external games.
 */
private class FloatingTurboPanelView(
    context: Context,
    private val onClosePanel: () -> Unit,
    private val onOpenMainApp: () -> Unit
) : View(context) {

    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xF210131C.toInt()
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF1E38.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFB0B8C8.toInt()
        textAlign = Paint.Align.CENTER
    }

    private val boostRect = RectF()
    private val coolRect = RectF()
    private val shieldRect = RectF()
    private val reconnectRect = RectF()
    private val modeRect = RectF()
    private val triggersRect = RectF()
    private val clonedBtnRect = RectF()
    private val clonedLockRect = RectF()
    private val crosshairRect = RectF()
    private val visionRect = RectF()
    private val closeRect = RectF()
    private val openAppRect = RectF()
    private val shutdownRect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val pad = 12f * density
        val outer = RectF(4f, 4f, width - 4f, height - 4f)
        canvas.drawRoundRect(outer, 18f * density, 18f * density, cardPaint)
        canvas.drawRoundRect(outer, 18f * density, 18f * density, borderPaint)

        val tel = TriggerEventBus.telemetry.value
        val mode = TriggerEventBus.performanceMode.value
        val trig = TriggerEventBus.triggerConfig.value
        val cloned = TriggerEventBus.clonedButtonsConfig.value
        val cross = TriggerEventBus.crosshairConfig.value
        val lowEnd = TriggerEventBus.lowEndConfig.value
        val shield = TriggerEventBus.notificationShieldState.value

        titlePaint.textSize = 15f * density
        canvas.drawText("⚡ HASSAN GAMES TURBO", width / 2f, 28f * density, titlePaint)

        subPaint.textSize = 11f * density
        subPaint.color = 0xFF00F0FF.toInt()
        canvas.drawText(
            "FPS: ${tel.liveFps}  |  RAM: ${tel.ramUsagePercent}%  |  ${tel.batteryTempCelsius}°C  |  ${tel.pingMs}ms",
            width / 2f,
            48f * density,
            subPaint
        )

        val rowH = 37f * density
        val gap = 6.5f * density
        var topY = 58f * density

        // 1. Instant Super Boost & ICE Cooler Row
        val halfRowW = (width - pad * 3) / 2f
        boostRect.set(pad, topY, pad + halfRowW, topY + rowH)
        coolRect.set(pad * 2 + halfRowW, topY, width - pad, topY + rowH)
        drawActionButton(canvas, boostRect, 0xFFFF1E38.toInt(), "🚀 تسريع الرام", density)
        drawActionButton(canvas, coolRect, 0xFF006D7A.toInt(), "❄️ استقرار حراري", density)
        topY += rowH + gap

        // 1b. Notification Shield & Quick Reconnect Row
        shieldRect.set(pad, topY, pad + halfRowW, topY + rowH)
        reconnectRect.set(pad * 2 + halfRowW, topY, width - pad, topY + rowH)
        val shieldBg = if (shield.enabled || lowEnd.notificationShieldEnabled) 0xFF00693C.toInt() else 0xFF1C2234.toInt()
        val shieldTxt = if (shield.enabled || lowEnd.notificationShieldEnabled) "🛡️ درع الإشعارات: ON" else "🛡️ درع الإشعارات: OFF"
        val hudAny = lowEnd.fpsOverlayEnabled || lowEnd.tempOverlayEnabled || lowEnd.ramOverlayEnabled
        val hudBg = if (hudAny) 0xFF007B8A.toInt() else 0xFF1C2234.toInt()
        val hudTxt = if (hudAny) "📊 عداد الشاشة: ON" else "📊 عداد الشاشة: OFF"
        drawActionButton(canvas, shieldRect, shieldBg, shieldTxt, density)
        drawActionButton(canvas, reconnectRect, hudBg, hudTxt, density)
        topY += rowH + gap

        // 2. Cloned Touch Buttons Row (Show/Hide + Lock/Unlock positions)
        clonedBtnRect.set(pad, topY, pad + halfRowW, topY + rowH)
        clonedLockRect.set(pad * 2 + halfRowW, topY, width - pad, topY + rowH)
        val cloneBg = if (cloned.systemOverlayEnabled) 0xFF007B8A.toInt() else 0xFF1C2234.toInt()
        val cloneText = if (cloned.systemOverlayEnabled) "🔘 أزرار منسوخة: ON" else "🔘 أزرار منسوخة: OFF"
        val lockBg = if (cloned.isLockedForPlay) 0xFF00693C.toInt() else 0xFF8A5A00.toInt()
        val lockText = if (cloned.isLockedForPlay) "🔒 مقفول للعب" else "🔓 تحريك المواقع"
        drawActionButton(canvas, clonedBtnRect, cloneBg, cloneText, density)
        drawActionButton(canvas, clonedLockRect, lockBg, lockText, density)
        topY += rowH + gap

        // 3. Performance Mode Cycle
        modeRect.set(pad, topY, width - pad, topY + rowH)
        drawActionButton(canvas, modeRect, 0xFF1C2234.toInt(), "🔥 وضع القوة: ${mode.arabicTitle}", density)
        topY += rowH + gap

        // 4. Volume L1/R1 Toggle
        triggersRect.set(pad, topY, width - pad, topY + rowH)
        val trigColor = if (trig.enabled) 0xFF007B8A.toInt() else 0xFF1C2234.toInt()
        val trigState = if (trig.enabled) "مفعّل ON" else "متوقف OFF"
        drawActionButton(canvas, triggersRect, trigColor, "🎮 أزرار الصوت L1/R1: $trigState", density)
        topY += rowH + gap

        // 5. Floating Crosshair Toggle
        crosshairRect.set(pad, topY, width - pad, topY + rowH)
        val crossColor = if (cross.systemOverlayEnabled) 0xFF990A1C.toInt() else 0xFF1C2234.toInt()
        val crossState = if (cross.systemOverlayEnabled) "مفعّل ON" else "متوقف OFF"
        drawActionButton(canvas, crosshairRect, crossColor, "🎯 مؤشر التصويب العائم: $crossState", density)
        topY += rowH + gap

        // 6. Night Hunter Vision Filter Cycle
        visionRect.set(pad, topY, width - pad, topY + rowH)
        val visColor = if (lowEnd.visionFilter != ScreenVisionFilter.NONE) 0xFF00693C.toInt() else 0xFF1C2234.toInt()
        drawActionButton(canvas, visionRect, visColor, "👁️ الرؤية: ${lowEnd.visionFilter.arabicName}", density)
        topY += rowH + gap + (4f * density)

        // Bottom Row: Open Full App, Hide Panel, & Complete Shutdown
        val thirdW = (width - pad * 4) / 3f
        openAppRect.set(pad, topY, pad + thirdW, topY + rowH * 0.9f)
        closeRect.set(pad * 2 + thirdW, topY, pad * 2 + thirdW * 2, topY + rowH * 0.9f)
        shutdownRect.set(pad * 3 + thirdW * 2, topY, width - pad, topY + rowH * 0.9f)

        drawActionButton(canvas, openAppRect, 0xFF283048.toInt(), "فتح التطبيق", density)
        drawActionButton(canvas, closeRect, 0xFF381824.toInt(), "إخفاء ✕", density)
        drawActionButton(canvas, shutdownRect, 0xFFB71C1C.toInt(), "⏹️ إنهاء الكل", density)
    }

    private fun drawActionButton(canvas: Canvas, rect: RectF, bgColor: Int, text: String, density: Float) {
        btnPaint.color = bgColor
        canvas.drawRoundRect(rect, 10f * density, 10f * density, btnPaint)
        titlePaint.textSize = 11.2f * density
        val textY = rect.centerY() - ((titlePaint.descent() + titlePaint.ascent()) / 2f)
        canvas.drawText(text, rect.centerX(), textY, titlePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            val x = event.x
            val y = event.y
            when {
                boostRect.contains(x, y) -> {
                    TriggerEventBus.requestQuickBoostFromOverlay()
                    invalidate()
                }
                coolRect.contains(x, y) -> {
                    TriggerEventBus.requestCoolDownFromOverlay()
                    invalidate()
                }
                shieldRect.contains(x, y) -> {
                    TriggerEventBus.requestToggleNotificationShieldFromOverlay()
                    invalidate()
                }
                reconnectRect.contains(x, y) -> {
                    val cur = TriggerEventBus.lowEndConfig.value
                    val anyActive = cur.fpsOverlayEnabled || cur.tempOverlayEnabled || cur.ramOverlayEnabled
                    val nextState = !anyActive
                    TriggerEventBus.updateLowEndConfig(
                        cur.copy(
                            fpsOverlayEnabled = nextState,
                            tempOverlayEnabled = nextState,
                            ramOverlayEnabled = nextState
                        )
                    )
                    invalidate()
                }
                clonedBtnRect.contains(x, y) -> {
                    val cur = TriggerEventBus.clonedButtonsConfig.value
                    TriggerEventBus.updateClonedButtonsConfig(
                        cur.copy(
                            masterEnabled = true,
                            systemOverlayEnabled = !cur.systemOverlayEnabled
                        )
                    )
                    invalidate()
                }
                clonedLockRect.contains(x, y) -> {
                    val cur = TriggerEventBus.clonedButtonsConfig.value
                    TriggerEventBus.updateClonedButtonsConfig(
                        cur.copy(
                            masterEnabled = true,
                            systemOverlayEnabled = true,
                            isLockedForPlay = !cur.isLockedForPlay
                        )
                    )
                    invalidate()
                }
                modeRect.contains(x, y) -> {
                    val entries = PerformanceMode.entries
                    val next = entries[(TriggerEventBus.performanceMode.value.ordinal + 1) % entries.size]
                    TriggerEventBus.updatePerformanceMode(next)
                    invalidate()
                }
                triggersRect.contains(x, y) -> {
                    val cur = TriggerEventBus.triggerConfig.value
                    TriggerEventBus.updateTriggerConfig(cur.copy(enabled = !cur.enabled))
                    invalidate()
                }
                crosshairRect.contains(x, y) -> {
                    val cur = TriggerEventBus.crosshairConfig.value
                    TriggerEventBus.updateCrosshairConfig(
                        cur.copy(
                            systemOverlayEnabled = !cur.systemOverlayEnabled,
                            enabledInApp = true
                        )
                    )
                    invalidate()
                }
                visionRect.contains(x, y) -> {
                    val entries = ScreenVisionFilter.entries
                    val cur = TriggerEventBus.lowEndConfig.value
                    val next = entries[(cur.visionFilter.ordinal + 1) % entries.size]
                    TriggerEventBus.updateLowEndConfig(cur.copy(visionFilter = next))
                    invalidate()
                }
                openAppRect.contains(x, y) -> {
                    onOpenMainApp()
                }
                closeRect.contains(x, y) -> {
                    onClosePanel()
                }
                shutdownRect.contains(x, y) -> {
                    onClosePanel()
                    TriggerEventBus.requestCompleteShutdown()
                }
            }
            return true
        }
        return true
    }
}

/**
 * Ultra-lightweight floating HUD pill showing live FPS, Battery Temp, and RAM % on top of games.
 * Redraws ONLY when telemetry snapshots arrive (every 5-15s), consuming 0% continuous CPU.
 */
private class FloatingHudPillOverlayView(
    context: Context,
    private var showFps: Boolean,
    private var showTemp: Boolean,
    private var showRam: Boolean,
    private val onDragDelta: (Float, Float) -> Unit
) : View(context) {

    private var lastRawX = 0f
    private var lastRawY = 0f

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xD90B0E17.toInt()
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF00F0FF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    fun updateFlags(fps: Boolean, temp: Boolean, ram: Boolean) {
        showFps = fps
        showTemp = temp
        showRam = ram
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val rect = RectF(3f, 3f, width - 3f, height - 3f)
        canvas.drawRoundRect(rect, 16f * density, 16f * density, bgPaint)
        canvas.drawRoundRect(rect, 16f * density, 16f * density, borderPaint)

        val tel = TriggerEventBus.telemetry.value
        val parts = mutableListOf<String>()
        if (showFps) parts.add("${tel.liveFps} FPS")
        if (showTemp) parts.add("${tel.batteryTempCelsius}°C")
        if (showRam) parts.add("RAM ${tel.ramUsagePercent}%")

        textPaint.textSize = 11f * density
        val text = parts.joinToString("  •  ").ifBlank { "${tel.liveFps} FPS" }
        val textY = rect.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(text, rect.centerX(), textY, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastRawX = event.rawX
                lastRawY = event.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - lastRawX
                val dy = event.rawY - lastRawY
                lastRawX = event.rawX
                lastRawY = event.rawY
                onDragDelta(dx, dy)
                return true
            }
        }
        return true
    }
}

/**
 * Floating Sniper Scope Magnifier Reticle Overlay.
 */
private class FloatingMagnifierOverlayView(
    context: Context,
    private var zoom: Float,
    private val onDragDelta: (Float, Float) -> Unit
) : View(context) {

    private var lastRawX = 0f
    private var lastRawY = 0f

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF00F0FF.toInt()
        style = Paint.Style.STROKE
    }
    private val lensPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x2200F0FF
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF00F0FF.toInt()
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    fun updateZoom(newZoom: Float) {
        zoom = newZoom
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val cx = width / 2f
        val cy = height / 2f
        val r = (width.coerceAtMost(height) / 2f) - (6f * density)

        canvas.drawCircle(cx, cy, r, lensPaint)
        ringPaint.strokeWidth = 2.5f * density
        ringPaint.color = 0xFF00F0FF.toInt()
        canvas.drawCircle(cx, cy, r, ringPaint)

        // Inner sniper rangefinder rings
        ringPaint.strokeWidth = 1.2f * density
        ringPaint.color = 0x8800F0FF.toInt()
        canvas.drawCircle(cx, cy, r * 0.58f, ringPaint)
        canvas.drawLine(cx - r * 0.85f, cy, cx + r * 0.85f, cy, ringPaint)
        canvas.drawLine(cx, cy - r * 0.85f, cx, cy + r * 0.85f, ringPaint)

        textPaint.textSize = 9.5f * density
        canvas.drawText("${(zoom * 10).toInt() / 10f}x SCOPE", cx, cy - r + (14f * density), textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastRawX = event.rawX
                lastRawY = event.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - lastRawX
                val dy = event.rawY - lastRawY
                lastRawX = event.rawX
                lastRawY = event.rawY
                onDragDelta(dx, dy)
                return true
            }
        }
        return true
    }
}

private class OverlayCrosshairView(
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

private class FloatingClonedButtonOverlayView(
    context: Context,
    private val button: ClonedTouchButton,
    private val isLockedForPlay: Boolean,
    private val opacity: Float,
    private val onDragDelta: (Float, Float) -> Unit,
    private val onTapDownUp: (Boolean) -> Unit
) : View(context) {

    private var isPressedNow = false
    private var lastRawX = 0f
    private var lastRawY = 0f

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val cx = width / 2f
        val cy = height / 2f
        val r = (width.coerceAtMost(height) / 2f) - (3f * density)

        val alphaInt = (opacity.coerceIn(0.3f, 1f) * 255).toInt()
        fillPaint.color = if (isPressedNow) button.colorHex.toInt() else 0xCC101420.toInt()
        fillPaint.alpha = alphaInt
        canvas.drawCircle(cx, cy, r, fillPaint)

        strokePaint.color = button.colorHex.toInt()
        strokePaint.strokeWidth = if (isLockedForPlay) 2.5f * density else 3.5f * density
        canvas.drawCircle(cx, cy, r, strokePaint)

        textPaint.textSize = 13f * density
        textPaint.color = if (isPressedNow) Color.BLACK else Color.WHITE
        canvas.drawText(button.badge, cx, cy + (2f * density), textPaint)

        textPaint.textSize = 8.5f * density
        textPaint.color = if (isLockedForPlay) 0xFF00E676.toInt() else 0xFFFFB300.toInt()
        val modeLabel = if (isLockedForPlay) "اضغط" else "اسحبني"
        canvas.drawText(modeLabel, cx, cy + (13f * density), textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isLockedForPlay) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastRawX = event.rawX
                    lastRawY = event.rawY
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - lastRawX
                    val dy = event.rawY - lastRawY
                    lastRawX = event.rawX
                    lastRawY = event.rawY
                    onDragDelta(dx, dy)
                    return true
                }
            }
            return true
        } else {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isPressedNow = true
                    invalidate()
                    onTapDownUp(true)
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isPressedNow = false
                    invalidate()
                    onTapDownUp(false)
                    return true
                }
            }
            return true
        }
    }
}

private class FloatingClonedTargetOverlayView(
    context: Context,
    private val button: ClonedTouchButton,
    private val onDragDelta: (Float, Float) -> Unit
) : View(context) {

    private var lastRawX = 0f
    private var lastRawY = 0f

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = button.colorHex.toInt()
        style = Paint.Style.STROKE
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xAA07080C.toInt()
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = button.colorHex.toInt()
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val cx = width / 2f
        val cy = height / 2f
        val r = (width.coerceAtMost(height) / 2f) - (3f * density)

        canvas.drawCircle(cx, cy, r, fillPaint)
        strokePaint.strokeWidth = 2f * density
        canvas.drawCircle(cx, cy, r, strokePaint)
        canvas.drawLine(cx - r, cy, cx + r, cy, strokePaint)
        canvas.drawLine(cx, cy - r, cx, cy + r, strokePaint)

        textPaint.textSize = 9f * density
        canvas.drawText("🎯${button.badge}", cx, cy + (3f * density), textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastRawX = event.rawX
                lastRawY = event.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - lastRawX
                val dy = event.rawY - lastRawY
                lastRawX = event.rawX
                lastRawY = event.rawY
                onDragDelta(dx, dy)
                return true
            }
        }
        return true
    }
}
