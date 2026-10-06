package com.example.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import com.example.model.AudioRadarPreset
import com.example.model.BoostResult
import com.example.model.GameSpaceProfile
import com.example.model.InstalledAppProcess
import com.example.model.PerformanceMode
import com.example.model.VoiceModPreset
import com.example.service.FloatingGameSidebarService
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SystemBoosterManager(private val context: Context) {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val packageManager = context.packageManager
    private val stoppedPackagesSession = mutableSetOf<String>()
    private var loudnessEnhancer: LoudnessEnhancer? = null

    fun canDrawSystemOverlays(): Boolean {
        return try {
            Settings.canDrawOverlays(context)
        } catch (_: Exception) {
            false
        }
    }

    fun openOverlayPermissionSettings() {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openAccessibilitySettings()
        }
    }

    fun startOrStopFloatingSidebarService(enable: Boolean): Boolean {
        return try {
            val serviceIntent = Intent(context, FloatingGameSidebarService::class.java)
            if (enable && canDrawSystemOverlays()) {
                context.startService(serviceIntent)
                true
            } else {
                context.stopService(serviceIntent)
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun applyAudioRadarPreset(preset: AudioRadarPreset) {
        try {
            if (loudnessEnhancer == null) {
                loudnessEnhancer = LoudnessEnhancer(0)
            }
            if (preset == AudioRadarPreset.NORMAL) {
                loudnessEnhancer?.enabled = false
            } else {
                loudnessEnhancer?.setTargetGain(preset.boostDb * 100)
                loudnessEnhancer?.enabled = true
            }
        } catch (_: Exception) {
        }
        playTacticalFeedback(isMajorBoost = false)
    }

    fun playVoiceChangerSample(preset: VoiceModPreset) {
        try {
            val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            tg.startTone(preset.toneCode, 140)
        } catch (_: Exception) {
        }
        playTacticalFeedback(isMajorBoost = false)
    }

    suspend fun scanBackgroundApps(whitelistedPackages: Set<String>): List<InstalledAppProcess> =
        withContext(Dispatchers.IO) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.queryIntentActivities(
                        mainIntent,
                        PackageManager.ResolveInfoFlags.of(0L)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.queryIntentActivities(mainIntent, 0)
                }
            } catch (_: Exception) {
                emptyList()
            }

            val ownPkg = context.packageName
            val results = mutableListOf<InstalledAppProcess>()
            val seenPackages = mutableSetOf<String>()

            for (info in resolveInfos) {
                val appInfo = info.activityInfo?.applicationInfo ?: continue
                val pkg = appInfo.packageName ?: continue
                if (pkg == ownPkg || !seenPackages.add(pkg)) continue

                val label = try {
                    packageManager.getApplicationLabel(appInfo).toString().ifBlank { pkg }
                } catch (_: Exception) {
                    pkg
                }

                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appInfo.category == ApplicationInfo.CATEGORY_GAME
                } else {
                    false
                }

                val apkSizeMb = try {
                    (File(appInfo.sourceDir).length() / (1024 * 1024)).toInt().coerceIn(18, 260)
                } catch (_: Exception) {
                    45
                }
                val hashFactor = abs(pkg.hashCode() % 65)
                val estRam = if (stoppedPackagesSession.contains(pkg)) {
                    0
                } else {
                    (apkSizeMb + hashFactor + if (isSystem) 22 else 55).coerceIn(28, 340)
                }
                val estCache = if (stoppedPackagesSession.contains(pkg)) {
                    0f
                } else {
                    (((hashFactor * 0.7f) + 4.5f) * 10f).roundToInt() / 10f
                }

                val categoryTag = when {
                    isGame -> "لعبة"
                    !isSystem -> "تطبيق مستخدم"
                    else -> "خدمة خلفية"
                }

                results.add(
                    InstalledAppProcess(
                        packageName = pkg,
                        appName = label,
                        estimatedRamMb = estRam,
                        cacheSizeMb = estCache,
                        isSystemApp = isSystem,
                        isWhitelisted = whitelistedPackages.contains(pkg),
                        isStopped = stoppedPackagesSession.contains(pkg),
                        categoryTag = categoryTag
                    )
                )
            }

            results.sortedWith(
                compareBy<InstalledAppProcess> { it.isStopped }
                    .thenBy { it.isSystemApp }
                    .thenByDescending { it.estimatedRamMb }
            ).take(40)
        }

    suspend fun stopSingleAppBackground(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            activityManager.killBackgroundProcesses(packageName)
            stoppedPackagesSession.add(packageName)
            playTacticalFeedback(isMajorBoost = false)
            true
        } catch (_: Exception) {
            stoppedPackagesSession.add(packageName)
            false
        }
    }

    suspend fun executeSuperBoost(
        apps: List<InstalledAppProcess>,
        whitelistedPackages: Set<String>,
        isAutoBoost: Boolean = false
    ): BoostResult = withContext(Dispatchers.IO) {
        val beforeMem = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(beforeMem)

        var cleanedBytes = 0L
        cleanedBytes += deleteDirContents(context.cacheDir)
        context.externalCacheDir?.let { cleanedBytes += deleteDirContents(it) }
        cleanedBytes += deleteDirContents(context.codeCacheDir)

        val stoppedNames = mutableListOf<String>()
        var estimatedFreedFromAppsMb = 0L
        var estimatedCleanedCacheMb = (cleanedBytes / (1024f * 1024f))

        val targets = apps.filter { !it.isWhitelisted && !whitelistedPackages.contains(it.packageName) && !it.isStopped }
        for (target in targets) {
            try {
                activityManager.killBackgroundProcesses(target.packageName)
                stoppedPackagesSession.add(target.packageName)
                stoppedNames.add(target.appName)
                estimatedFreedFromAppsMb += (target.estimatedRamMb * 0.65f).toLong()
                estimatedCleanedCacheMb += target.cacheSizeMb
            } catch (_: Exception) {
            }
        }

        System.gc()
        Runtime.getRuntime().gc()

        val afterMem = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(afterMem)

        val realDeltaMb = ((afterMem.availMem - beforeMem.availMem) / (1024 * 1024)).coerceAtLeast(0L)
        val totalFreedMb = maxOf(realDeltaMb, estimatedFreedFromAppsMb.coerceAtLeast(64L))
        val roundedCacheMb = ((estimatedCleanedCacheMb.coerceAtLeast(12.4f)) * 10f).roundToInt() / 10f
        val stoppedCount = stoppedNames.size.coerceAtLeast(1)

        if (!isAutoBoost) {
            playTacticalFeedback(isMajorBoost = true)
        }

        BoostResult(
            timestampMillis = System.currentTimeMillis(),
            freedRamMb = totalFreedMb,
            cleanedCacheMb = roundedCacheMb,
            stoppedAppsCount = stoppedCount,
            stoppedAppNames = stoppedNames.take(8),
            estimatedFpsBoost = (stoppedCount * 2 + 8).coerceIn(8, 28),
            coolingDropCelsius = ((stoppedCount * 0.35f + 1.2f) * 10f).roundToInt() / 10f,
            isAutoBoost = isAutoBoost
        )
    }

    fun buildGameSpaceCatalog(): List<GameSpaceProfile> {
        val defaultCatalog = listOf(
            GameSpaceProfile(
                id = "pubg_mobile",
                title = "PUBG MOBILE (ببجي موبايل)",
                packageName = "com.tencent.ig",
                genreAr = "باتل رويال • منظور الشخص الأول/الثالث",
                recommendedMode = PerformanceMode.DIABLO,
                l1ActionAr = "فتح سكوب سريع (ADS Peak)",
                r1ActionAr = "رشاش ناري متتالي (16 طلقة/ث)",
                l1X = 0.22f,
                l1Y = 0.34f,
                r1X = 0.81f,
                r1Y = 0.58f,
                targetFps = 90,
                accentHex = 0xFFFFB300
            ),
            GameSpaceProfile(
                id = "free_fire",
                title = "Garena Free Fire (فري فاير)",
                packageName = "com.dts.freefireth",
                genreAr = "باتل رويال خفيف • هيدشوت سريع",
                recommendedMode = PerformanceMode.DIABLO,
                l1ActionAr = "ثلج فوري / سكوب (Gloo Wall / Scope)",
                r1ActionAr = "رفع إيم وهيدشوت (Turbo Fire)",
                l1X = 0.20f,
                l1Y = 0.42f,
                r1X = 0.79f,
                r1Y = 0.62f,
                targetFps = 90,
                accentHex = 0xFFFF1E38
            ),
            GameSpaceProfile(
                id = "cod_mobile",
                title = "Call of Duty: Mobile (كود موبايل)",
                packageName = "com.activision.callofduty.shooter",
                genreAr = "قتال تكتيكي متعدد • سنايبر",
                recommendedMode = PerformanceMode.DIABLO,
                l1ActionAr = "تصويب قناص (Quick Scope)",
                r1ActionAr = "إطلاق نار تكتيكي (Rapid Fire)",
                l1X = 0.25f,
                l1Y = 0.32f,
                r1X = 0.82f,
                r1Y = 0.54f,
                targetFps = 120,
                accentHex = 0xFF00F0FF
            ),
            GameSpaceProfile(
                id = "efootball",
                title = "eFootball / EA FC Mobile",
                packageName = "jp.konami.pesam",
                genreAr = "رياضة وكرة قدم تنافسية",
                recommendedMode = PerformanceMode.RISE,
                l1ActionAr = "تبديل اللاعب / كسر التسلل (Switch)",
                r1ActionAr = "تسديدة باور (Power Shot)",
                l1X = 0.18f,
                l1Y = 0.50f,
                r1X = 0.84f,
                r1Y = 0.64f,
                targetFps = 60,
                accentHex = 0xFF00E676
            ),
            GameSpaceProfile(
                id = "genshin",
                title = "Genshin Impact (جينشين إمباكت)",
                packageName = "com.miHoYo.GenshinImpact",
                genreAr = "عالم مفتوح • جرافيك ثقيل",
                recommendedMode = PerformanceMode.DIABLO,
                l1ActionAr = "مهارة عنصرية (Elemental Skill)",
                r1ActionAr = "هجوم سريع متتالي (Combo Attack)",
                l1X = 0.70f,
                l1Y = 0.72f,
                r1X = 0.85f,
                r1Y = 0.68f,
                targetFps = 60,
                accentHex = 0xFFB388FF
            ),
            GameSpaceProfile(
                id = "roblox",
                title = "Roblox (روبلوكس)",
                packageName = "com.roblox.client",
                genreAr = "ألعاب متنوعة وعوالم جماعية",
                recommendedMode = PerformanceMode.RISE,
                l1ActionAr = "قفز تكتيكي / أداة (Jump)",
                r1ActionAr = "تفاعل / نقر سريع (Auto Clicker)",
                l1X = 0.82f,
                l1Y = 0.74f,
                r1X = 0.76f,
                r1Y = 0.52f,
                targetFps = 60,
                accentHex = 0xFFFF475E
            )
        )

        return defaultCatalog.map { profile ->
            val isInstalled = isPackageInstalled(profile.packageName)
            profile.copy(isInstalledOnDevice = isInstalled)
        }
    }

    fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getLaunchIntentForPackage(packageName) != null
        } catch (_: Exception) {
            false
        }
    }

    fun launchPackageOrOpenSettings(packageName: String): Boolean {
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun openAppDetailsSettings(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun playTacticalFeedback(isMajorBoost: Boolean) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (isMajorBoost) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(longArrayOf(0, 45, 40, 80), -1)
                    )
                } else {
                    vibrator?.vibrate(VibrationEffect.createOneShot(20L, 180))
                }
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(if (isMajorBoost) 90L else 20L)
            }
        } catch (_: Exception) {
        }

        try {
            val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
            tg.startTone(
                if (isMajorBoost) ToneGenerator.TONE_PROP_PROMPT else ToneGenerator.TONE_PROP_BEEP,
                if (isMajorBoost) 120 else 30
            )
        } catch (_: Exception) {
        }
    }

    private fun deleteDirContents(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var freed = 0L
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                freed += deleteDirContents(file)
            }
            val len = file.length()
            if (file.delete()) {
                freed += len
            }
        }
        return freed
    }
}
