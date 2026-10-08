package com.example.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.HardwareTelemetryEngine
import com.example.data.SystemBoosterManager
import com.example.model.ClonedButtonMode
import com.example.model.ClonedTouchButton
import com.example.model.CrosshairConfig
import com.example.model.CrosshairStyle
import com.example.model.HardwareTelemetry
import com.example.model.PerformanceMode
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Lightweight In-Game Overlay Service for Hassan Games Gaming Center.
 * Provides:
 * 1. Draggable Edge Sidebar with instant Gaming Mode controls & telemetry.
 * 2. Floating Crosshair Overlay (attached only when enabled).
 * 3. Floating Mini FPS/Temp HUD (attached only when enabled).
 * 4. Floating On-Screen Cloned Touch Buttons C1-C4 (attached only when enabled).
 */
class FloatingGameSidebarService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var telemetryEngine: HardwareTelemetryEngine
    private lateinit var boosterManager: SystemBoosterManager
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var sidebarComposeView: ComposeView? = null
    private var sidebarParams: WindowManager.LayoutParams? = null
    private var sidebarOwner: OverlayComposeLifecycleOwner? = null

    private var crosshairComposeView: ComposeView? = null
    private var crosshairParams: WindowManager.LayoutParams? = null
    private var crosshairOwner: OverlayComposeLifecycleOwner? = null

    private var fpsHudComposeView: ComposeView? = null
    private var fpsHudParams: WindowManager.LayoutParams? = null
    private var fpsHudOwner: OverlayComposeLifecycleOwner? = null

    private data class ClonedOverlayHolder(
        val buttonId: Int,
        val sourceView: ComposeView,
        val sourceParams: WindowManager.LayoutParams,
        val sourceOwner: OverlayComposeLifecycleOwner,
        var targetView: ComposeView? = null,
        var targetParams: WindowManager.LayoutParams? = null,
        var targetOwner: OverlayComposeLifecycleOwner? = null
    )

    private val clonedOverlayHolders = mutableMapOf<Int, ClonedOverlayHolder>()

    private var crosshairObserverJob: Job? = null
    private var clonedObserverJob: Job? = null
    private var shutdownObserverJob: Job? = null

    private val fpsHudEnabledState = mutableStateOf(false)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        telemetryEngine = HardwareTelemetryEngine(applicationContext)
        boosterManager = SystemBoosterManager(applicationContext)

        if (!Settings.canDrawOverlays(this)) {
            TriggerEventBus.setOverlayRunning(false)
            stopSelf()
            return
        }

        attachSidebarOverlay()
        observeCrosshairSync()
        observeClonedButtonsSync()
        observeCompleteShutdown()
        TriggerEventBus.setOverlayRunning(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_OVERLAY -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_SET_FPS_HUD -> {
                val showHud = intent.getBooleanExtra(EXTRA_SHOW_FPS_HUD, false)
                setFpsHudVisible(showHud)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        crosshairObserverJob?.cancel()
        clonedObserverJob?.cancel()
        shutdownObserverJob?.cancel()
        telemetryEngine.stopFrameMonitor()
        removeAllOverlays()
        TriggerEventBus.setOverlayRunning(false)
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun overlayLayoutType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private fun observeCrosshairSync() {
        crosshairObserverJob?.cancel()
        crosshairObserverJob = serviceScope.launch {
            TriggerEventBus.crosshairConfig.collect { config ->
                if (!Settings.canDrawOverlays(this@FloatingGameSidebarService)) return@collect
                if (config.enabled) {
                    attachCrosshairOverlayIfNeeded()
                    updateCrosshairPosition(config)
                } else {
                    removeCrosshairOverlay()
                }
            }
        }
    }

    private fun observeClonedButtonsSync() {
        clonedObserverJob?.cancel()
        clonedObserverJob = serviceScope.launch {
            TriggerEventBus.clonedButtonsConfig.collect { config ->
                if (!Settings.canDrawOverlays(this@FloatingGameSidebarService)) return@collect
                if (config.enabled) {
                    syncClonedButtonsWindows(config.buttons, !config.editPositionsLocked)
                } else {
                    removeClonedButtonsOverlays()
                }
            }
        }
    }

    private fun observeCompleteShutdown() {
        shutdownObserverJob?.cancel()
        shutdownObserverJob = serviceScope.launch {
            TriggerEventBus.shutdownRequests.collect {
                stopSelf()
            }
        }
    }

    private fun setFpsHudVisible(visible: Boolean) {
        fpsHudEnabledState.value = visible
        if (visible) {
            attachFpsHudOverlayIfNeeded()
        } else {
            removeFpsHudOverlay()
        }
    }

    private fun attachSidebarOverlay() {
        if (sidebarComposeView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayLayoutType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 240
        }
        sidebarParams = params

        val owner = OverlayComposeLifecycleOwner().apply { onCreate() }
        sidebarOwner = owner

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                FloatingSidebarOverlayRoot(
                    telemetryEngine = telemetryEngine,
                    boosterManager = boosterManager,
                    fpsHudVisible = fpsHudEnabledState.value,
                    onToggleFpsHud = { setFpsHudVisible(!fpsHudEnabledState.value) },
                    onDragDelta = { dx, dy ->
                        sidebarParams?.let { lp ->
                            lp.x = (lp.x + dx.roundToInt()).coerceAtLeast(0)
                            lp.y = (lp.y + dy.roundToInt()).coerceAtLeast(48)
                            safeUpdateViewLayout(this, lp)
                        }
                    },
                    onOpenMainApp = {
                        val launchIntent = Intent(this@FloatingGameSidebarService, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        startActivity(launchIntent)
                    },
                    onStopGamingMode = {
                        boosterManager.restoreAllSessionControls()
                        TriggerEventBus.requestCompleteShutdown()
                        stopSelf()
                    }
                )
            }
        }

        try {
            windowManager.addView(composeView, params)
            sidebarComposeView = composeView
        } catch (_: Exception) {
            owner.onDestroy()
            sidebarOwner = null
        }
    }

    private fun attachFpsHudOverlayIfNeeded() {
        if (fpsHudComposeView != null) return

        val metrics = resources.displayMetrics
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayLayoutType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (metrics.widthPixels * 0.32f).roundToInt()
            y = 32
        }
        fpsHudParams = params

        val owner = OverlayComposeLifecycleOwner().apply { onCreate() }
        fpsHudOwner = owner

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                FloatingMiniFpsHudPill(
                    telemetryEngine = telemetryEngine,
                    onDragDelta = { dx, dy ->
                        fpsHudParams?.let { lp ->
                            val maxX = (metrics.widthPixels - 140).coerceAtLeast(0)
                            val maxY = (metrics.heightPixels - 80).coerceAtLeast(0)
                            lp.x = (lp.x + dx.roundToInt()).coerceIn(0, maxX)
                            lp.y = (lp.y + dy.roundToInt()).coerceIn(0, maxY)
                            safeUpdateViewLayout(this, lp)
                        }
                    },
                    onClose = { setFpsHudVisible(false) }
                )
            }
        }

        try {
            windowManager.addView(view, params)
            fpsHudComposeView = view
        } catch (_: Exception) {
            owner.onDestroy()
            fpsHudOwner = null
        }
    }

    private fun removeFpsHudOverlay() {
        fpsHudComposeView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }
        fpsHudComposeView = null
        fpsHudParams = null
        fpsHudOwner?.onDestroy()
        fpsHudOwner = null
    }

    private fun attachCrosshairOverlayIfNeeded() {
        if (crosshairComposeView != null) return

        val config = TriggerEventBus.crosshairConfig.value
        val density = resources.displayMetrics.density
        val boxSizePx = (90 * density).roundToInt()

        val params = WindowManager.LayoutParams(
            boxSizePx,
            boxSizePx,
            overlayLayoutType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            x = (config.offsetXDp * density).roundToInt()
            y = (config.offsetYDp * density).roundToInt()
        }
        crosshairParams = params

        val owner = OverlayComposeLifecycleOwner().apply { onCreate() }
        crosshairOwner = owner

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                val liveConfig by TriggerEventBus.crosshairConfig.collectAsState()
                if (liveConfig.enabled) {
                    FloatingCrosshairOverlayCanvas(config = liveConfig)
                }
            }
        }

        try {
            windowManager.addView(view, params)
            crosshairComposeView = view
        } catch (_: Exception) {
            owner.onDestroy()
            crosshairOwner = null
        }
    }

    private fun updateCrosshairPosition(config: CrosshairConfig) {
        val view = crosshairComposeView ?: return
        val params = crosshairParams ?: return
        val density = resources.displayMetrics.density
        params.x = (config.offsetXDp * density).roundToInt()
        params.y = (config.offsetYDp * density).roundToInt()
        safeUpdateViewLayout(view, params)
    }

    private fun removeCrosshairOverlay() {
        crosshairComposeView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }
        crosshairComposeView = null
        crosshairParams = null
        crosshairOwner?.onDestroy()
        crosshairOwner = null
    }

    private fun syncClonedButtonsWindows(
        buttons: List<ClonedTouchButton>,
        showTargetPins: Boolean
    ) {
        val metrics = resources.displayMetrics
        val screenW = metrics.widthPixels.coerceAtLeast(320)
        val screenH = metrics.heightPixels.coerceAtLeast(480)
        val density = metrics.density

        val enabledIds = buttons.filter { it.enabled }.map { it.id }.toSet()

        val toRemove = clonedOverlayHolders.keys.filter { it !in enabledIds }
        for (id in toRemove) {
            clonedOverlayHolders.remove(id)?.let { holder ->
                try {
                    windowManager.removeView(holder.sourceView)
                } catch (_: Exception) {
                }
                holder.sourceOwner.onDestroy()
                holder.targetView?.let { tv ->
                    try {
                        windowManager.removeView(tv)
                    } catch (_: Exception) {
                    }
                }
                holder.targetOwner?.onDestroy()
            }
        }

        for (btn in buttons.filter { it.enabled }) {
            val sizePx = (btn.buttonSizeDp * density).roundToInt().coerceIn(96, 260)
            val maxSrcX = (screenW - sizePx).coerceAtLeast(0)
            val maxSrcY = (screenH - sizePx).coerceAtLeast(0)
            val existing = clonedOverlayHolders[btn.id]

            if (existing == null) {
                val srcParams = WindowManager.LayoutParams(
                    sizePx,
                    sizePx,
                    overlayLayoutType(),
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = (btn.sourceX * screenW - sizePx / 2f).roundToInt().coerceIn(0, maxSrcX)
                    y = (btn.sourceY * screenH - sizePx / 2f).roundToInt().coerceIn(0, maxSrcY)
                }

                val srcOwner = OverlayComposeLifecycleOwner().apply { onCreate() }
                val srcView = ComposeView(this).apply {
                    setViewTreeLifecycleOwner(srcOwner)
                    setViewTreeViewModelStoreOwner(srcOwner)
                    setViewTreeSavedStateRegistryOwner(srcOwner)
                    setContent {
                        val liveConfig by TriggerEventBus.clonedButtonsConfig.collectAsState()
                        val liveBtn = liveConfig.buttons.firstOrNull { it.id == btn.id } ?: btn
                        FloatingClonedSourceNode(
                            button = liveBtn,
                            editLocked = liveConfig.editPositionsLocked,
                            onDragDelta = { dx, dy ->
                                val newX = (srcParams.x + dx.roundToInt()).coerceIn(0, maxSrcX)
                                val newY = (srcParams.y + dy.roundToInt()).coerceIn(0, maxSrcY)
                                srcParams.x = newX
                                srcParams.y = newY
                                safeUpdateViewLayout(this, srcParams)

                                val normX = ((newX + sizePx / 2f) / screenW).coerceIn(0.04f, 0.96f)
                                val normY = ((newY + sizePx / 2f) / screenH).coerceIn(0.06f, 0.94f)
                                val updatedList = TriggerEventBus.clonedButtonsConfig.value.buttons.map {
                                    if (it.id == btn.id) it.copy(sourceX = normX, sourceY = normY) else it
                                }
                                TriggerEventBus.updateClonedButtonsConfig(
                                    TriggerEventBus.clonedButtonsConfig.value.copy(buttons = updatedList)
                                )
                            }
                        )
                    }
                }

                try {
                    windowManager.addView(srcView, srcParams)
                    val holder = ClonedOverlayHolder(
                        buttonId = btn.id,
                        sourceView = srcView,
                        sourceParams = srcParams,
                        sourceOwner = srcOwner
                    )
                    clonedOverlayHolders[btn.id] = holder
                } catch (_: Exception) {
                    srcOwner.onDestroy()
                }
            } else {
                existing.sourceParams.width = sizePx
                existing.sourceParams.height = sizePx
                existing.sourceParams.x =
                    (btn.sourceX * screenW - sizePx / 2f).roundToInt().coerceIn(0, maxSrcX)
                existing.sourceParams.y =
                    (btn.sourceY * screenH - sizePx / 2f).roundToInt().coerceIn(0, maxSrcY)
                safeUpdateViewLayout(existing.sourceView, existing.sourceParams)
            }

            val holder = clonedOverlayHolders[btn.id] ?: continue
            val targetPinPx = (44 * density).roundToInt()
            val maxTgtX = (screenW - targetPinPx).coerceAtLeast(0)
            val maxTgtY = (screenH - targetPinPx).coerceAtLeast(0)
            if (showTargetPins) {
                if (holder.targetView == null) {
                    val tgtParams = WindowManager.LayoutParams(
                        targetPinPx,
                        targetPinPx,
                        overlayLayoutType(),
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT
                    ).apply {
                        gravity = Gravity.TOP or Gravity.START
                        x = (btn.targetX * screenW - targetPinPx / 2f).roundToInt()
                            .coerceIn(0, maxTgtX)
                        y = (btn.targetY * screenH - targetPinPx / 2f).roundToInt()
                            .coerceIn(0, maxTgtY)
                    }
                    val tgtOwner = OverlayComposeLifecycleOwner().apply { onCreate() }
                    val tgtView = ComposeView(this).apply {
                        setViewTreeLifecycleOwner(tgtOwner)
                        setViewTreeViewModelStoreOwner(tgtOwner)
                        setViewTreeSavedStateRegistryOwner(tgtOwner)
                        setContent {
                            val liveConfig by TriggerEventBus.clonedButtonsConfig.collectAsState()
                            val liveBtn = liveConfig.buttons.firstOrNull { it.id == btn.id } ?: btn
                            FloatingClonedTargetPin(
                                button = liveBtn,
                                onDragDelta = { dx, dy ->
                                    val newX = (tgtParams.x + dx.roundToInt()).coerceIn(0, maxTgtX)
                                    val newY = (tgtParams.y + dy.roundToInt()).coerceIn(0, maxTgtY)
                                    tgtParams.x = newX
                                    tgtParams.y = newY
                                    safeUpdateViewLayout(this, tgtParams)

                                    val normX = ((newX + targetPinPx / 2f) / screenW).coerceIn(0.04f, 0.96f)
                                    val normY = ((newY + targetPinPx / 2f) / screenH).coerceIn(0.06f, 0.94f)
                                    val updatedList = TriggerEventBus.clonedButtonsConfig.value.buttons.map {
                                        if (it.id == btn.id) it.copy(targetX = normX, targetY = normY) else it
                                    }
                                    TriggerEventBus.updateClonedButtonsConfig(
                                        TriggerEventBus.clonedButtonsConfig.value.copy(buttons = updatedList)
                                    )
                                }
                            )
                        }
                    }
                    try {
                        windowManager.addView(tgtView, tgtParams)
                        holder.targetView = tgtView
                        holder.targetParams = tgtParams
                        holder.targetOwner = tgtOwner
                    } catch (_: Exception) {
                        tgtOwner.onDestroy()
                    }
                } else {
                    holder.targetParams?.let { tp ->
                        tp.x = (btn.targetX * screenW - targetPinPx / 2f).roundToInt()
                            .coerceIn(0, maxTgtX)
                        tp.y = (btn.targetY * screenH - targetPinPx / 2f).roundToInt()
                            .coerceIn(0, maxTgtY)
                        holder.targetView?.let { tv -> safeUpdateViewLayout(tv, tp) }
                    }
                }
            } else {
                holder.targetView?.let { tv ->
                    try {
                        windowManager.removeView(tv)
                    } catch (_: Exception) {
                    }
                }
                holder.targetOwner?.onDestroy()
                holder.targetView = null
                holder.targetParams = null
                holder.targetOwner = null
            }
        }
    }

    private fun removeClonedButtonsOverlays() {
        for (holder in clonedOverlayHolders.values) {
            try {
                windowManager.removeView(holder.sourceView)
            } catch (_: Exception) {
            }
            holder.sourceOwner.onDestroy()
            holder.targetView?.let { tv ->
                try {
                    windowManager.removeView(tv)
                } catch (_: Exception) {
                }
            }
            holder.targetOwner?.onDestroy()
        }
        clonedOverlayHolders.clear()
    }

    private fun safeUpdateViewLayout(view: ComposeView, params: WindowManager.LayoutParams) {
        try {
            windowManager.updateViewLayout(view, params)
        } catch (_: Exception) {
        }
    }

    private fun removeAllOverlays() {
        sidebarComposeView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }
        sidebarComposeView = null
        sidebarParams = null
        sidebarOwner?.onDestroy()
        sidebarOwner = null

        removeCrosshairOverlay()
        removeFpsHudOverlay()
        removeClonedButtonsOverlays()
    }

    companion object {
        const val ACTION_STOP_OVERLAY = "com.example.action.STOP_OVERLAY"
        const val ACTION_SET_FPS_HUD = "com.example.action.SET_FPS_HUD"
        const val EXTRA_SHOW_FPS_HUD = "extra_show_fps_hud"
    }
}

@Composable
private fun FloatingSidebarOverlayRoot(
    telemetryEngine: HardwareTelemetryEngine,
    boosterManager: SystemBoosterManager,
    fpsHudVisible: Boolean,
    onToggleFpsHud: () -> Unit,
    onDragDelta: (Float, Float) -> Unit,
    onOpenMainApp: () -> Unit,
    onStopGamingMode: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var telemetry by remember { mutableStateOf(HardwareTelemetry()) }
    var quickBoostBanner by remember { mutableStateOf<String?>(null) }
    var boostTick by remember { mutableIntStateOf(0) }

    val activeMode by TriggerEventBus.activePerformanceMode.collectAsState()
    val crosshairConfig by TriggerEventBus.crosshairConfig.collectAsState()
    val clonedConfig by TriggerEventBus.clonedButtonsConfig.collectAsState()
    val activeGameTitle by TriggerEventBus.activeGameTitle.collectAsState()

    // Lightweight telemetry loop — only runs when panel is expanded
    LaunchedEffect(expanded, activeMode) {
        while (isActive && expanded) {
            telemetryEngine.triggerLightweightFrameSample()
            telemetry = telemetryEngine.sampleTelemetry(activeMode, measureNetworkPing = false)
            delay(4000L)
        }
    }

    LaunchedEffect(boostTick) {
        if (boostTick > 0) {
            val cleanedMb = boosterManager.purgeOwnCacheDirectory()
            telemetryEngine.notifyCacheCleaned(cleanedMb)
            quickBoostBanner = "تم تسريع الرام وتنظيف ${cleanedMb}MB كاش!"
            delay(2500L)
            quickBoostBanner = null
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Sleek edge handle
        Box(
            modifier = Modifier
                .width(if (expanded) 12.dp else 18.dp)
                .height(92.dp)
                .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            activeMode.primaryColor,
                            Color(0xFF99001B)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                )
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDragDelta(dragAmount.x, dragAmount.y)
                    }
                }
                .clickable { expanded = !expanded },
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Surface(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .width(268.dp),
                shape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
                color = Color(0xF20D0D14),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, activeMode.primaryColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = activeMode.primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "GAMING MODE HUD",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = activeGameTitle ?: "Hassan Games Center",
                                    color = activeMode.primaryColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onOpenMainApp,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "فتح التطبيق",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            IconButton(
                                onClick = { expanded = false },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "طي الشريط",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live telemetry strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF151722))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MiniOverlayStat("FPS", "${telemetry.liveFps}", Color(0xFF00E676))
                        MiniOverlayStat("RAM", "${telemetry.ramUsagePercent}%", Color(0xFF00E5FF))
                        MiniOverlayStat("TEMP", "${telemetry.batteryTempCelsius}°C", Color(0xFFFF9100))
                        MiniOverlayStat("PING", "${telemetry.pingMs}ms", Color(0xFFFFEA00))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Performance Mode Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        PerformanceMode.entries.forEach { mode ->
                            val selected = activeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (selected) mode.primaryColor.copy(alpha = 0.25f)
                                        else Color(0xFF1A1C26)
                                    )
                                    .border(
                                        1.dp,
                                        if (selected) mode.primaryColor else Color.White.copy(alpha = 0.12f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { TriggerEventBus.updatePerformanceMode(mode) }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (mode) {
                                        PerformanceMode.BALANCED -> "ECO"
                                        PerformanceMode.PERFORMANCE -> "BOOST"
                                        PerformanceMode.DIABLO -> "DIABLO"
                                    },
                                    color = if (selected) mode.primaryColor else Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Tool Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SidebarQuickToggleChip(
                            title = "التصويب",
                            active = crosshairConfig.enabled,
                            accent = Color(0xFFFF1744),
                            icon = Icons.Default.GpsFixed,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                TriggerEventBus.updateCrosshairConfig(
                                    crosshairConfig.copy(enabled = !crosshairConfig.enabled)
                                )
                            }
                        )
                        SidebarQuickToggleChip(
                            title = "عداد FPS",
                            active = fpsHudVisible,
                            accent = Color(0xFF00E676),
                            icon = Icons.Default.Speed,
                            modifier = Modifier.weight(1f),
                            onClick = onToggleFpsHud
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SidebarQuickToggleChip(
                            title = "أزرار لمس C1-C4",
                            active = clonedConfig.enabled,
                            accent = Color(0xFF00E5FF),
                            icon = Icons.Default.ControlCamera,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                TriggerEventBus.updateClonedButtonsConfig(
                                    clonedConfig.copy(enabled = !clonedConfig.enabled)
                                )
                            }
                        )
                        SidebarQuickToggleChip(
                            title = "تنظيف الرام",
                            active = quickBoostBanner != null,
                            accent = Color(0xFFFF9100),
                            icon = Icons.Default.Memory,
                            modifier = Modifier.weight(1f),
                            onClick = { boostTick += 1 }
                        )
                    }

                    quickBoostBanner?.let { msg ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = msg,
                            color = Color(0xFF00E676),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Stop Gaming Mode button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2B1118))
                            .border(1.dp, Color(0xFFFF1744).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .clickable { onStopGamingMode() }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = Color(0xFFFF1744),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إيقاف Gaming Mode وإغلاق الأدوات",
                                color = Color(0xFFFF5252),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniOverlayStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun SidebarQuickToggleChip(
    title: String,
    active: Boolean,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) accent.copy(alpha = 0.22f) else Color(0xFF171923))
            .border(
                1.dp,
                if (active) accent else Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) accent else Color.White.copy(alpha = 0.65f),
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            color = if (active) Color.White else Color.White.copy(alpha = 0.75f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FloatingMiniFpsHudPill(
    telemetryEngine: HardwareTelemetryEngine,
    onDragDelta: (Float, Float) -> Unit,
    onClose: () -> Unit
) {
    var snap by remember { mutableStateOf(HardwareTelemetry()) }
    val activeMode by TriggerEventBus.activePerformanceMode.collectAsState()

    LaunchedEffect(activeMode) {
        while (isActive) {
            telemetryEngine.triggerLightweightFrameSample()
            snap = telemetryEngine.sampleTelemetry(activeMode, measureNetworkPing = false)
            delay(3000L)
        }
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xE60B0C12),
        border = androidx.compose.foundation.BorderStroke(1.dp, activeMode.primaryColor.copy(alpha = 0.8f)),
        modifier = Modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                onDragDelta(dragAmount.x, dragAmount.y)
            }
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "${snap.liveFps} FPS",
                    color = Color(0xFF00E676),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = null,
                    tint = Color(0xFFFF9100),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "${snap.batteryTempCelsius}°C",
                    color = Color(0xFFFF9100),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "${snap.pingMs}ms",
                color = Color(0xFF00E5FF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "إغلاق",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(13.dp)
                    .clickable { onClose() }
            )
        }
    }
}

@Composable
private fun FloatingCrosshairOverlayCanvas(config: CrosshairConfig) {
    val accent = Color(config.colorHex).copy(alpha = config.opacity.coerceIn(0.2f, 1f))
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (config.sizeDp / 2f).dp.toPx()
        val stroke = config.strokeWidthDp.dp.toPx().coerceAtLeast(1.5f)

        when (config.style) {
            CrosshairStyle.RED_DOT -> {
                drawCircle(color = Color.Black.copy(alpha = 0.55f), radius = stroke * 2.2f, center = center)
                drawCircle(color = accent, radius = stroke * 1.6f, center = center)
            }
            CrosshairStyle.TACTICAL_CROSS -> {
                val gap = radius * 0.28f
                drawLine(accent, Offset(center.x - radius, center.y), Offset(center.x - gap, center.y), stroke)
                drawLine(accent, Offset(center.x + gap, center.y), Offset(center.x + radius, center.y), stroke)
                drawLine(accent, Offset(center.x, center.y - radius), Offset(center.x, center.y - gap), stroke)
                drawLine(accent, Offset(center.x, center.y + gap), Offset(center.x, center.y + radius), stroke)
                drawCircle(accent, radius = stroke * 0.8f, center = center)
            }
            CrosshairStyle.SNIPER_CIRCLE -> {
                drawCircle(accent, radius = radius, center = center, style = Stroke(width = stroke))
                drawCircle(accent, radius = stroke, center = center)
            }
            CrosshairStyle.CHEVRON_PRO -> {
                drawLine(accent, center, Offset(center.x - radius * 0.7f, center.y + radius * 0.7f), stroke)
                drawLine(accent, center, Offset(center.x + radius * 0.7f, center.y + radius * 0.7f), stroke)
                drawCircle(accent, radius = stroke * 0.85f, center = Offset(center.x, center.y - stroke * 2f))
            }
        }
    }
}

@Composable
private fun FloatingClonedSourceNode(
    button: ClonedTouchButton,
    editLocked: Boolean,
    onDragDelta: (Float, Float) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var autoLocked by remember { mutableStateOf(false) }
    val color = Color(button.colorHex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(
                if (isPressed || autoLocked) color.copy(alpha = 0.55f)
                else Color(0xCC10131C)
            )
            .border(
                width = if (isPressed || autoLocked) 2.5.dp else 1.5.dp,
                color = color.copy(alpha = button.opacity),
                shape = CircleShape
            )
            .pointerInput(button.id, button.mode, editLocked) {
                if (!editLocked) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDragDelta(dragAmount.x, dragAmount.y)
                    }
                } else {
                    detectTapGestures(
                        onPress = {
                            if (button.mode == ClonedButtonMode.AUTO_LOCK) {
                                autoLocked = !autoLocked
                                TriggerEventBus.emitClonedButtonTap(button, isDown = autoLocked)
                            } else {
                                isPressed = true
                                TriggerEventBus.emitClonedButtonTap(button, isDown = true)
                                tryAwaitRelease()
                                isPressed = false
                                TriggerEventBus.emitClonedButtonTap(button, isDown = false)
                            }
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = button.label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = if (editLocked) "TAP" else "DRAG",
                color = color,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun FloatingClonedTargetPin(
    button: ClonedTouchButton,
    onDragDelta: (Float, Float) -> Unit
) {
    val color = Color(button.colorHex)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(color.copy(alpha = 0.25f))
            .border(1.5.dp, color, CircleShape)
            .pointerInput(button.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragDelta(dragAmount.x, dragAmount.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🎯${button.label}",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
        )
    }
}

private class OverlayComposeLifecycleOwner :
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        try {
            if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            }
            if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }
        } catch (_: Exception) {
        }
        store.clear()
    }
}
