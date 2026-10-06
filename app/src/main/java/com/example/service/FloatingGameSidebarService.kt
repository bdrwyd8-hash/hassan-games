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
            TriggerEventBus.telemetry.collectLatest {
                edgeHandleView?.invalidate()
                expandedPanelView?.invalidate()
            }
        }
        serviceScope.launch {
            TriggerEventBus.edgeSidebarConfig.collectLatest { cfg ->
                if (!cfg.systemFloatingEnabled) {
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
                expandedPanelView?.invalidate()
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

        val panelW = (300 * density).toInt()
        val panelH = (390 * density).toInt()

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

    override fun onDestroy() {
        TriggerEventBus.setOverlayServiceRunning(false)
        val wm = windowManager
        listOfNotNull(edgeHandleView, expandedPanelView, crosshairView, visionFilterView).forEach { v ->
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
    private val modeRect = RectF()
    private val triggersRect = RectF()
    private val crosshairRect = RectF()
    private val visionRect = RectF()
    private val closeRect = RectF()
    private val openAppRect = RectF()

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
        val cross = TriggerEventBus.crosshairConfig.value
        val lowEnd = TriggerEventBus.lowEndConfig.value

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

        val rowH = 42f * density
        val gap = 8f * density
        var topY = 62f * density

        // 1. Instant Super Boost Button
        boostRect.set(pad, topY, width - pad, topY + rowH)
        drawActionButton(canvas, boostRect, 0xFFFF1E38.toInt(), "🚀 مسح الكاش وتسريع الرام فوراً", density)
        topY += rowH + gap

        // 2. Performance Mode Cycle
        modeRect.set(pad, topY, width - pad, topY + rowH)
        drawActionButton(canvas, modeRect, 0xFF1C2234.toInt(), "🔥 وضع القوة: ${mode.arabicTitle}", density)
        topY += rowH + gap

        // 3. Volume L1/R1 Toggle
        triggersRect.set(pad, topY, width - pad, topY + rowH)
        val trigColor = if (trig.enabled) 0xFF007B8A.toInt() else 0xFF1C2234.toInt()
        val trigState = if (trig.enabled) "مفعّل ON" else "متوقف OFF"
        drawActionButton(canvas, triggersRect, trigColor, "🎮 أزرار الصوت L1/R1: $trigState", density)
        topY += rowH + gap

        // 4. Floating Crosshair Toggle
        crosshairRect.set(pad, topY, width - pad, topY + rowH)
        val crossColor = if (cross.systemOverlayEnabled) 0xFF990A1C.toInt() else 0xFF1C2234.toInt()
        val crossState = if (cross.systemOverlayEnabled) "مفعّل ON" else "متوقف OFF"
        drawActionButton(canvas, crosshairRect, crossColor, "🎯 مؤشر التصويب العائم: $crossState", density)
        topY += rowH + gap

        // 5. Night Hunter Vision Filter Cycle
        visionRect.set(pad, topY, width - pad, topY + rowH)
        val visColor = if (lowEnd.visionFilter != ScreenVisionFilter.NONE) 0xFF00693C.toInt() else 0xFF1C2234.toInt()
        drawActionButton(canvas, visionRect, visColor, "👁️ الرؤية: ${lowEnd.visionFilter.arabicName}", density)
        topY += rowH + gap + (4f * density)

        // Bottom Row: Open Full App & Close Panel
        val halfW = (width - pad * 3) / 2f
        openAppRect.set(pad, topY, pad + halfW, topY + rowH * 0.9f)
        closeRect.set(pad * 2 + halfW, topY, width - pad, topY + rowH * 0.9f)

        drawActionButton(canvas, openAppRect, 0xFF283048.toInt(), "فتح التطبيق", density)
        drawActionButton(canvas, closeRect, 0xFF381824.toInt(), "إخفاء ✕", density)
    }

    private fun drawActionButton(canvas: Canvas, rect: RectF, bgColor: Int, text: String, density: Float) {
        btnPaint.color = bgColor
        canvas.drawRoundRect(rect, 10f * density, 10f * density, btnPaint)
        titlePaint.textSize = 12f * density
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
            }
            return true
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
