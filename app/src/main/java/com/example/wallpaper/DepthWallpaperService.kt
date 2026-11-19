package com.example.wallpaper

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.example.data.WallpaperPreferences
import com.example.engine.LayeredSceneRenderer
import com.example.engine.SensorTiltController
import com.example.model.WallpaperSettings

class DepthWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return DepthEngine()
    }

    inner class DepthEngine : Engine() {

        private val handler = Handler(Looper.getMainLooper())
        private lateinit var prefs: WallpaperPreferences
        private lateinit var tiltController: SensorTiltController
        private val renderer = LayeredSceneRenderer()

        private var settings: WallpaperSettings = WallpaperSettings()
        private var isVisibleState: Boolean = false
        private var isAmbientModeState: Boolean = false

        private var surfaceWidth = 0
        private var surfaceHeight = 0

        private var lastTouchX = 0f
        private var lastTouchY = 0f

        // Frame rate control
        private val targetFps: Long = 60
        private val frameIntervalMs: Long = 1000L / targetFps

        private val drawRunnable = object : Runnable {
            override fun run() {
                drawFrame()
                if (isVisibleState && !isAmbientModeState) {
                    val delay = if (settings.batterySaverEnabled) 33L else frameIntervalMs
                    handler.postDelayed(this, delay)
                } else if (isVisibleState && isAmbientModeState) {
                    // In AOD mode, only refresh once every 10 seconds or 1 second for clock
                    handler.postDelayed(this, 1000L)
                }
            }
        }

        private val screenReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                when (intent?.action) {
                    android.content.Intent.ACTION_SCREEN_OFF -> setAmbientMode(true)
                    android.content.Intent.ACTION_SCREEN_ON -> setAmbientMode(false)
                }
            }
        }
        private var isReceiverRegistered = false

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            prefs = WallpaperPreferences.getInstance(applicationContext)
            settings = prefs.getSettings()
            tiltController = SensorTiltController(applicationContext)
            setTouchEventsEnabled(true)

            val filter = android.content.IntentFilter().apply {
                addAction(android.content.Intent.ACTION_SCREEN_OFF)
                addAction(android.content.Intent.ACTION_SCREEN_ON)
            }
            if (!isReceiverRegistered) {
                registerReceiver(screenReceiver, filter)
                isReceiverRegistered = true
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            if (isReceiverRegistered) {
                try {
                    unregisterReceiver(screenReceiver)
                } catch (_: Exception) {}
                isReceiverRegistered = false
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            refreshSettings()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            surfaceWidth = width
            surfaceHeight = height
            renderer.onSurfaceChanged(width, height, settings.themePreset, settings, applicationContext)
            drawFrame()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            isVisibleState = false
            tiltController.stop()
            handler.removeCallbacks(drawRunnable)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisibleState = visible
            if (visible) {
                refreshSettings()
                if (!isAmbientModeState) {
                    tiltController.start()
                }
                handler.removeCallbacks(drawRunnable)
                handler.post(drawRunnable)
            } else {
                tiltController.stop()
                handler.removeCallbacks(drawRunnable)
            }
        }

        fun setAmbientMode(inAmbientMode: Boolean) {
            if (isAmbientModeState == inAmbientMode) return
            isAmbientModeState = inAmbientMode
            if (inAmbientMode) {
                tiltController.stop()
            } else if (isVisibleState) {
                tiltController.start()
            }
            drawFrame()
        }

        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            if (!settings.touchTiltEnabled || isAmbientModeState) return

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.x
                    lastTouchY = event.y
                    tiltController.onTouchDown()
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.x - lastTouchX) / (if (surfaceWidth > 0) surfaceWidth else 1080)
                    val dy = (event.y - lastTouchY) / (if (surfaceHeight > 0) surfaceHeight else 2400)
                    lastTouchX = event.x
                    lastTouchY = event.y
                    tiltController.onTouchMove(dx, dy)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    tiltController.onTouchUp()
                }
            }
        }

        private var lastLoadedTheme: com.example.model.ThemePreset? = null
        private var lastLoadedTimestamp: Long = 0L

        private fun refreshSettings() {
            val updated = prefs.getSettings()
            if (updated.themePreset != lastLoadedTheme || updated.lastModified != lastLoadedTimestamp) {
                lastLoadedTheme = updated.themePreset
                lastLoadedTimestamp = updated.lastModified
                renderer.onThemeChanged(updated.themePreset, updated, applicationContext)
            }
            settings = updated
        }

        private fun drawFrame() {
            val holder = surfaceHolder ?: return
            if (!holder.surface.isValid || surfaceWidth <= 0 || surfaceHeight <= 0) return

            // Update sensor smoothing
            if (!isAmbientModeState) {
                tiltController.updateTick()
            }

            // Sync settings periodically
            refreshSettings()

            val canvas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    holder.lockHardwareCanvas()
                } catch (_: Exception) {
                    holder.lockCanvas()
                }
            } else {
                holder.lockCanvas()
            } ?: return

            try {
                renderer.render(
                    canvas = canvas,
                    rawTiltX = if (isAmbientModeState) 0f else tiltController.smoothTiltX,
                    rawTiltY = if (isAmbientModeState) 0f else tiltController.smoothTiltY,
                    settings = settings,
                    isAmbientMode = isAmbientModeState
                )
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}
