package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import com.example.model.ClockFontStyle
import com.example.model.ThemePreset
import com.example.model.WallpaperSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LayeredSceneRenderer {

    private var currentTheme: ThemePreset? = null
    private var layerBundle: LayerBundle? = null

    private var width: Int = 1080
    private var height: Int = 2400

    private val particleSystem = ParticleSystem(maxParticles = 110)

    // Caches and paints
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val clockPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val aodPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val srcRect = Rect()
    private val dstRect = RectF()

    // Date formatting
    private val dateInstance = Date()
    private val time12Format = SimpleDateFormat("h:mm", Locale.getDefault())
    private val time12SecFormat = SimpleDateFormat("h:mm:ss", Locale.getDefault())
    private val time24Format = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val time24SecFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val amPmFormat = SimpleDateFormat("a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    // Parallax overscan margin (pixels)
    private val maxShiftX = 60f
    private val maxShiftY = 60f

    fun onSurfaceChanged(w: Int, h: Int, theme: ThemePreset, settings: WallpaperSettings, context: android.content.Context? = null) {
        if (w <= 0 || h <= 0) return
        this.width = w
        this.height = h

        particleSystem.resize(w, h)
        particleSystem.initParticles(settings.particleType, settings.particleDensity)

        if (currentTheme != theme || layerBundle == null) {
            currentTheme = theme
            val old = layerBundle
            layerBundle = ThemeLayerGenerator.generateLayers(theme, w, h, context)
            old?.recycle()
        }
    }

    fun onThemeChanged(theme: ThemePreset, settings: WallpaperSettings, context: android.content.Context? = null) {
        if (width > 0 && height > 0) {
            currentTheme = theme
            val old = layerBundle
            layerBundle = ThemeLayerGenerator.generateLayers(theme, width, height, context)
            old?.recycle()
            particleSystem.initParticles(settings.particleType, settings.particleDensity)
        }
    }

    fun reloadLayers(settings: WallpaperSettings, context: android.content.Context? = null) {
        if (width > 0 && height > 0) {
            currentTheme = settings.themePreset
            val old = layerBundle
            layerBundle = ThemeLayerGenerator.generateLayers(settings.themePreset, width, height, context)
            old?.recycle()
            particleSystem.initParticles(settings.particleType, settings.particleDensity)
        }
    }

    fun render(
        canvas: Canvas,
        rawTiltX: Float,
        rawTiltY: Float,
        settings: WallpaperSettings,
        isAmbientMode: Boolean
    ) {
        val bundle = layerBundle ?: return

        // 1. Ambient / AOD Mode Optimization (Battery Saving)
        if (isAmbientMode) {
            canvas.drawColor(Color.BLACK)
            if (settings.clock.enabled) {
                renderClock(canvas, settings, isAmbient = true, tiltX = 0f, tiltY = 0f)
            }
            return
        }

        // Apply sensitivity and invert
        val invert = if (settings.invertTilt) -1f else 1f
        val tiltX = (rawTiltX * settings.tiltSensitivity * invert).coerceIn(-1.5f, 1.5f)
        val tiltY = (rawTiltY * settings.tiltSensitivity * invert).coerceIn(-1.5f, 1.5f)

        // Parallax multipliers
        // Background moves opposite or slightly for depth (-0.25x)
        val bgShiftX = -tiltX * (maxShiftX * 0.35f)
        val bgShiftY = -tiltY * (maxShiftY * 0.35f)

        // Subject moves moderately forward (+0.40x)
        val subShiftX = tiltX * (maxShiftX * 0.45f)
        val subShiftY = tiltY * (maxShiftY * 0.45f)

        // Clock moves between background and subject (+0.15x)
        val clockShiftX = tiltX * (maxShiftX * 0.18f)
        val clockShiftY = tiltY * (maxShiftY * 0.18f)

        // Foreground moves fastest (+1.25x)
        val fgShiftX = tiltX * (maxShiftX * 1.30f)
        val fgShiftY = tiltY * (maxShiftY * 1.30f)

        // Clear canvas
        canvas.drawColor(Color.BLACK)

        // PASS 1: Background Layer
        drawLayer(canvas, bundle.background, bgShiftX, bgShiftY)

        // PASS 2: Clock Layer (If Behind Subject - Apple iOS 16 style 3D Depth)
        if (settings.clock.enabled && settings.clock.depthBehindSubject) {
            renderClock(canvas, settings, isAmbient = false, tiltX = clockShiftX, tiltY = clockShiftY)
        }

        // PASS 3: Middle Subject Layer
        drawLayer(canvas, bundle.subject, subShiftX, subShiftY)

        // PASS 4: Clock Layer (If in front of subject)
        if (settings.clock.enabled && !settings.clock.depthBehindSubject) {
            renderClock(canvas, settings, isAmbient = false, tiltX = clockShiftX, tiltY = clockShiftY)
        }

        // PASS 5: Foreground Layer
        drawLayer(canvas, bundle.foreground, fgShiftX, fgShiftY)

        // PASS 6: Particle Weather System
        particleSystem.update(
            type = settings.particleType,
            speedMultiplier = settings.particleSpeed,
            tiltShiftX = tiltX,
            densityRatio = settings.particleDensity
        )
        particleSystem.draw(canvas, settings.particleType)
    }

    private fun drawLayer(canvas: Canvas, bitmap: Bitmap, shiftX: Float, shiftY: Float) {
        if (bitmap.isRecycled) return

        // Draw centered with slight overscan scale so shifts don't reveal borders
        val scale = 1.08f
        val scaledW = width * scale
        val scaledH = height * scale

        val left = (width - scaledW) / 2f + shiftX
        val top = (height - scaledH) / 2f + shiftY

        srcRect.set(0, 0, bitmap.width, bitmap.height)
        dstRect.set(left, top, left + scaledW, top + scaledH)

        canvas.drawBitmap(bitmap, srcRect, dstRect, bitmapPaint)
    }

    private val watchRenderer = WatchRenderer()

    private fun renderClock(
        canvas: Canvas,
        settings: WallpaperSettings,
        isAmbient: Boolean,
        tiltX: Float,
        tiltY: Float
    ) {
        val config = settings.clock
        val centerX = (width / 2f) + tiltX
        val centerY = (height * config.verticalOffsetRatio) + tiltY

        watchRenderer.render(
            canvas = canvas,
            settings = settings,
            isAmbient = isAmbient,
            centerX = centerX,
            centerY = centerY,
            width = width,
            height = height
        )
    }
}
