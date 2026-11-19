package com.example.engine

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.model.ClockFontStyle
import com.example.model.WatchDesignType
import com.example.model.WallpaperSettings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

class WatchRenderer {

    private val calendar = Calendar.getInstance()
    private val dateInstance = Date()

    private val hour12Format = SimpleDateFormat("h", Locale.getDefault())
    private val hour12TwoDigit = SimpleDateFormat("hh", Locale.getDefault())
    private val hour24Format = SimpleDateFormat("HH", Locale.getDefault())
    private val minFormat = SimpleDateFormat("mm", Locale.getDefault())
    private val secFormat = SimpleDateFormat("ss", Locale.getDefault())
    private val amPmFormat = SimpleDateFormat("a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    // Shared reusable paints
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val secondaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val dialRect = RectF()

    fun render(
        canvas: Canvas,
        settings: WallpaperSettings,
        isAmbient: Boolean,
        centerX: Float,
        centerY: Float,
        width: Int,
        height: Int
    ) {
        val config = settings.clock
        val now = System.currentTimeMillis()
        dateInstance.time = now
        calendar.timeInMillis = now

        val hours = calendar.get(Calendar.HOUR)
        val hours24 = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)
        val millis = calendar.get(Calendar.MILLISECOND)

        when (config.watchDesign) {
            WatchDesignType.BIG_STACKED -> {
                renderBigStacked(canvas, settings, isAmbient, centerX, centerY, width)
            }
            WatchDesignType.CHRONO_ANALOG -> {
                renderChronoAnalog(
                    canvas, settings, isAmbient, centerX, centerY, width,
                    hours, minutes, seconds, millis
                )
            }
            WatchDesignType.CYBER_HUD -> {
                renderCyberHud(
                    canvas, settings, isAmbient, centerX, centerY, width,
                    hours24, minutes, seconds, millis
                )
            }
            WatchDesignType.MINIMAL_HORIZONTAL -> {
                renderMinimalHorizontal(canvas, settings, isAmbient, centerX, centerY, width)
            }
            WatchDesignType.RETRO_FLIP -> {
                renderRetroFlip(
                    canvas, settings, isAmbient, centerX, centerY, width,
                    hours24, hours, minutes, seconds
                )
            }
        }
    }

    private fun getTypeface(fontStyle: ClockFontStyle): Typeface {
        return when (fontStyle) {
            ClockFontStyle.BOLD_MODERN -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            ClockFontStyle.SERIF_ELEGANT -> Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            ClockFontStyle.MONO_TECH -> Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            ClockFontStyle.CYBER_DIGITAL -> Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            ClockFontStyle.MINIMAL_SLIM -> Typeface.create("sans-serif-thin", Typeface.NORMAL)
        }
    }

    // =========================================================================
    // 1. BIG STACKED DEPTH (iOS 16 Lock Screen Two-Tier Clock)
    // =========================================================================
    private fun renderBigStacked(
        canvas: Canvas,
        settings: WallpaperSettings,
        isAmbient: Boolean,
        cx: Float,
        cy: Float,
        width: Int
    ) {
        val config = settings.clock
        val hourStr = if (config.is24Hour) hour24Format.format(dateInstance) else hour12Format.format(dateInstance)
        val minStr = minFormat.format(dateInstance)
        val dateStr = dateFormat.format(dateInstance).uppercase(Locale.getDefault())

        val typeface = getTypeface(config.fontStyle)
        val fontSize = width * 0.32f * config.sizeScale
        val lineSpacing = fontSize * 0.82f

        val clockColor = if (isAmbient) Color.parseColor("#94A3B8") else config.colorInt

        textPaint.typeface = typeface
        textPaint.textSize = fontSize
        textPaint.textAlign = Paint.Align.CENTER

        if (isAmbient && settings.aodOutlineClock) {
            textPaint.style = Paint.Style.STROKE
            textPaint.strokeWidth = 3.5f
            textPaint.color = Color.WHITE
            textPaint.clearShadowLayer()
        } else {
            textPaint.style = Paint.Style.FILL
            textPaint.color = clockColor
            if (!isAmbient) {
                textPaint.setShadowLayer(22f, 0f, 8f, 0xAA000000.toInt())
            } else {
                textPaint.clearShadowLayer()
            }
        }

        val hourY = cy - lineSpacing * 0.42f
        val minY = cy + lineSpacing * 0.58f

        // Optional date pill above hours
        if (config.showDate) {
            val dateY = hourY - fontSize * 0.80f
            secondaryTextPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            secondaryTextPaint.textSize = width * 0.038f * config.sizeScale
            secondaryTextPaint.color = if (isAmbient) Color.parseColor("#64748B") else 0xEEFFFFFF.toInt()
            secondaryTextPaint.textAlign = Paint.Align.CENTER
            if (!isAmbient) {
                secondaryTextPaint.setShadowLayer(10f, 0f, 4f, 0x88000000.toInt())
            } else {
                secondaryTextPaint.clearShadowLayer()
            }
            canvas.drawText(dateStr, cx, dateY, secondaryTextPaint)
        }

        // Draw stacked hours and minutes
        canvas.drawText(hourStr, cx, hourY, textPaint)
        canvas.drawText(minStr, cx, minY, textPaint)
    }

    // =========================================================================
    // 2. PRECISION CHRONOGRAPH (Luxury Analog Dial with Hands and Subdial)
    // =========================================================================
    private fun renderChronoAnalog(
        canvas: Canvas,
        settings: WallpaperSettings,
        isAmbient: Boolean,
        cx: Float,
        cy: Float,
        width: Int,
        hours: Int,
        minutes: Int,
        seconds: Int,
        millis: Int
    ) {
        val config = settings.clock
        val dialRadius = width * 0.27f * config.sizeScale
        val accentColor = if (isAmbient) Color.WHITE else config.colorInt

        // 1. Dial Face Disc
        if (!isAmbient) {
            shapePaint.style = Paint.Style.FILL
            shapePaint.shader = LinearGradient(
                cx - dialRadius, cy - dialRadius, cx + dialRadius, cy + dialRadius,
                intArrayOf(0xDD0F172A.toInt(), 0xEE090D16.toInt(), 0xCC1E293B.toInt()),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, dialRadius, shapePaint)
            shapePaint.shader = null

            // Outer Bezel Ring
            strokePaint.strokeWidth = 3f
            strokePaint.color = 0x8838BDF8.toInt()
            canvas.drawCircle(cx, cy, dialRadius, strokePaint)
        } else {
            // Minimal ambient outline
            strokePaint.strokeWidth = 2f
            strokePaint.color = 0x66FFFFFF.toInt()
            canvas.drawCircle(cx, cy, dialRadius, strokePaint)
        }

        // 2. Tick Marks (60 ticks)
        for (i in 0 until 60) {
            val angle = Math.toRadians((i * 6).toDouble())
            val cosVal = cos(angle).toFloat()
            val sinVal = sin(angle).toFloat()

            val isFiveMin = (i % 5 == 0)
            val outerR = dialRadius - 6f
            val innerR = if (isFiveMin) dialRadius - 20f else dialRadius - 12f

            strokePaint.strokeWidth = if (isFiveMin) 3f else 1.2f
            strokePaint.color = if (isFiveMin) accentColor else (if (isAmbient) 0x44FFFFFF.toInt() else 0x66CBD5E1.toInt())

            val startX = cx + sinVal * innerR
            val startY = cy - cosVal * innerR
            val endX = cx + sinVal * outerR
            val endY = cy - cosVal * outerR
            canvas.drawLine(startX, startY, endX, endY, strokePaint)
        }

        // 3. Cardinal Numbers (12, 3, 6, 9)
        if (!isAmbient) {
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textPaint.textSize = dialRadius * 0.18f
            textPaint.color = Color.WHITE
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.style = Paint.Style.FILL
            textPaint.clearShadowLayer()

            canvas.drawText("12", cx, cy - dialRadius + 44f, textPaint)
            canvas.drawText("3", cx + dialRadius - 32f, cy + 8f, textPaint)
            canvas.drawText("9", cx - dialRadius + 32f, cy + 8f, textPaint)

            // Date Window at 3 o'clock or brand text
            val dateNum = calendar.get(Calendar.DAY_OF_MONTH).toString()
            shapePaint.color = 0x66000000.toInt()
            shapePaint.style = Paint.Style.FILL
            canvas.drawRoundRect(cx + dialRadius * 0.45f, cy - 12f, cx + dialRadius * 0.72f, cy + 14f, 4f, 4f, shapePaint)
            strokePaint.strokeWidth = 1f
            strokePaint.color = 0x8894A3B8.toInt()
            canvas.drawRoundRect(cx + dialRadius * 0.45f, cy - 12f, cx + dialRadius * 0.72f, cy + 14f, 4f, 4f, strokePaint)

            secondaryTextPaint.textSize = dialRadius * 0.14f
            secondaryTextPaint.color = accentColor
            secondaryTextPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            secondaryTextPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(dateNum, cx + dialRadius * 0.585f, cy + 7f, secondaryTextPaint)

            // Small Seconds Sub-dial at 6 o'clock
            val subRadius = dialRadius * 0.28f
            val subCy = cy + dialRadius * 0.45f
            strokePaint.strokeWidth = 1.2f
            strokePaint.color = 0x44FFFFFF.toInt()
            canvas.drawCircle(cx, subCy, subRadius, strokePaint)

            val subSecAngle = Math.toRadians((seconds * 6).toDouble())
            val subX = cx + (sin(subSecAngle) * (subRadius - 4f)).toFloat()
            val subY = subCy - (cos(subSecAngle) * (subRadius - 4f)).toFloat()
            strokePaint.strokeWidth = 2f
            strokePaint.color = accentColor
            canvas.drawLine(cx, subCy, subX, subY, strokePaint)
        }

        // 4. Hour Hand
        val smoothHours = hours + (minutes / 60f) + (seconds / 3600f)
        val hourAngle = Math.toRadians((smoothHours * 30.0))
        val hourLen = dialRadius * 0.55f

        val hourX = cx + (sin(hourAngle) * hourLen).toFloat()
        val hourY = cy - (cos(hourAngle) * hourLen).toFloat()

        strokePaint.strokeWidth = 7f
        strokePaint.color = if (isAmbient) Color.WHITE else Color.parseColor("#E2E8F0")
        strokePaint.strokeCap = Paint.Cap.ROUND
        if (!isAmbient) strokePaint.setShadowLayer(8f, 0f, 4f, 0x88000000.toInt())
        canvas.drawLine(cx, cy, hourX, hourY, strokePaint)

        // 5. Minute Hand
        val smoothMinutes = minutes + (seconds / 60f)
        val minAngle = Math.toRadians((smoothMinutes * 6.0))
        val minLen = dialRadius * 0.80f

        val minX = cx + (sin(minAngle) * minLen).toFloat()
        val minY = cy - (cos(minAngle) * minLen).toFloat()

        strokePaint.strokeWidth = 4.5f
        strokePaint.color = if (isAmbient) Color.WHITE else Color.parseColor("#F8FAFC")
        canvas.drawLine(cx, cy, minX, minY, strokePaint)

        // 6. Second Hand (Sweeping needle with tail)
        if (!isAmbient) {
            val smoothSeconds = seconds + (millis / 1000f)
            val secAngle = Math.toRadians((smoothSeconds * 6.0))
            val secLen = dialRadius * 0.88f
            val tailLen = dialRadius * 0.22f

            val secX = cx + (sin(secAngle) * secLen).toFloat()
            val secY = cy - (cos(secAngle) * secLen).toFloat()
            val tailX = cx - (sin(secAngle) * tailLen).toFloat()
            val tailY = cy + (cos(secAngle) * tailLen).toFloat()

            strokePaint.strokeWidth = 2f
            strokePaint.color = accentColor
            strokePaint.clearShadowLayer()
            canvas.drawLine(tailX, tailY, secX, secY, strokePaint)

            // Second hand counter-weight ring
            shapePaint.color = accentColor
            shapePaint.style = Paint.Style.FILL
            canvas.drawCircle(tailX, tailY, 4f, shapePaint)
        }

        // Center Pin Cap
        shapePaint.color = if (isAmbient) Color.WHITE else Color.parseColor("#0F172A")
        shapePaint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, 6f, shapePaint)
        strokePaint.strokeWidth = 2f
        strokePaint.color = accentColor
        canvas.drawCircle(cx, cy, 6f, strokePaint)
    }

    // =========================================================================
    // 3. CYBER HUD GAUGE (Futuristic Radial Arc Gauge & Telemetry Readout)
    // =========================================================================
    private fun renderCyberHud(
        canvas: Canvas,
        settings: WallpaperSettings,
        isAmbient: Boolean,
        cx: Float,
        cy: Float,
        width: Int,
        hours24: Int,
        minutes: Int,
        seconds: Int,
        millis: Int
    ) {
        val config = settings.clock
        val hudRadius = width * 0.28f * config.sizeScale
        val accentColor = if (isAmbient) Color.WHITE else config.colorInt

        // Outer Arc Seconds Gauge (0 to 360 deg)
        val secProgress = (seconds + (millis / 1000f)) / 60f
        val sweepAngle = secProgress * 360f

        dialRect.set(cx - hudRadius, cy - hudRadius, cx + hudRadius, cy + hudRadius)

        // Dim track background
        strokePaint.strokeWidth = 3f
        strokePaint.color = if (isAmbient) 0x33FFFFFF.toInt() else 0x3300F5D4.toInt()
        strokePaint.strokeCap = Paint.Cap.BUTT
        canvas.drawArc(dialRect, 0f, 360f, false, strokePaint)

        // Active glowing seconds arc
        if (!isAmbient) {
            strokePaint.strokeWidth = 5f
            strokePaint.color = accentColor
            strokePaint.setShadowLayer(14f, 0f, 0f, accentColor)
            canvas.drawArc(dialRect, -90f, sweepAngle, false, strokePaint)
            strokePaint.clearShadowLayer()
        }

        // Inner dashed ring
        val innerRadius = hudRadius * 0.85f
        for (i in 0 until 36) {
            val a = Math.toRadians((i * 10.0))
            val sx = cx + (sin(a) * innerRadius).toFloat()
            val sy = cy - (cos(a) * innerRadius).toFloat()
            val ex = cx + (sin(a) * (innerRadius - 6f)).toFloat()
            val ey = cy - (cos(a) * (innerRadius - 6f)).toFloat()
            strokePaint.strokeWidth = 1.5f
            strokePaint.color = if (i % 3 == 0) accentColor else 0x4464748B.toInt()
            canvas.drawLine(sx, sy, ex, ey, strokePaint)
        }

        // Corner Tactical HUD Brackets
        val bSize = hudRadius * 0.35f
        strokePaint.strokeWidth = 2.5f
        strokePaint.color = accentColor

        // Top-left bracket
        canvas.drawLine(cx - hudRadius - 10f, cy - hudRadius, cx - hudRadius + bSize, cy - hudRadius, strokePaint)
        canvas.drawLine(cx - hudRadius - 10f, cy - hudRadius, cx - hudRadius - 10f, cy - hudRadius + bSize, strokePaint)

        // Top-right bracket
        canvas.drawLine(cx + hudRadius + 10f, cy - hudRadius, cx + hudRadius - bSize, cy - hudRadius, strokePaint)
        canvas.drawLine(cx + hudRadius + 10f, cy - hudRadius, cx + hudRadius + 10f, cy - hudRadius + bSize, strokePaint)

        // Bottom-left bracket
        canvas.drawLine(cx - hudRadius - 10f, cy + hudRadius, cx - hudRadius + bSize, cy + hudRadius, strokePaint)
        canvas.drawLine(cx - hudRadius - 10f, cy + hudRadius, cx - hudRadius - 10f, cy + hudRadius - bSize, strokePaint)

        // Bottom-right bracket
        canvas.drawLine(cx + hudRadius + 10f, cy + hudRadius, cx + hudRadius - bSize, cy + hudRadius, strokePaint)
        canvas.drawLine(cx + hudRadius + 10f, cy + hudRadius, cx + hudRadius + 10f, cy + hudRadius - bSize, strokePaint)

        // Center Digital HUD Readout
        val hStr = String.format("%02d", if (config.is24Hour) hours24 else (if (hours24 % 12 == 0) 12 else hours24 % 12))
        val mStr = String.format("%02d", minutes)
        val sStr = String.format("%02d", seconds)
        val digitalTime = "$hStr:$mStr"

        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textPaint.textSize = hudRadius * 0.46f
        textPaint.textAlign = Paint.Align.CENTER

        if (isAmbient && settings.aodOutlineClock) {
            textPaint.style = Paint.Style.STROKE
            textPaint.strokeWidth = 3f
            textPaint.color = Color.WHITE
            canvas.drawText(digitalTime, cx, cy + 12f, textPaint)
        } else {
            textPaint.style = Paint.Style.FILL
            textPaint.color = Color.WHITE
            textPaint.setShadowLayer(16f, 0f, 0f, accentColor)
            canvas.drawText(digitalTime, cx, cy + 12f, textPaint)
            textPaint.clearShadowLayer()
        }

        // Seconds and Telemetry Subtitle
        if (!isAmbient) {
            secondaryTextPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            secondaryTextPaint.textSize = hudRadius * 0.16f
            secondaryTextPaint.color = accentColor
            secondaryTextPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("SEC // $sStr", cx, cy + hudRadius * 0.42f, secondaryTextPaint)

            // Header Telemetry
            val dateCode = dateFormat.format(dateInstance).uppercase(Locale.getDefault())
            canvas.drawText("SYS.HUD // $dateCode", cx, cy - hudRadius * 0.42f, secondaryTextPaint)
        }
    }

    // =========================================================================
    // 4. MINIMAL HORIZON (Modern Linear Typography with Capsule)
    // =========================================================================
    private fun renderMinimalHorizontal(
        canvas: Canvas,
        settings: WallpaperSettings,
        isAmbient: Boolean,
        cx: Float,
        cy: Float,
        width: Int
    ) {
        val config = settings.clock
        val timeStr = if (config.is24Hour) {
            if (config.showSeconds) SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(dateInstance)
            else SimpleDateFormat("HH:mm", Locale.getDefault()).format(dateInstance)
        } else {
            if (config.showSeconds) SimpleDateFormat("h:mm:ss", Locale.getDefault()).format(dateInstance)
            else SimpleDateFormat("h:mm", Locale.getDefault()).format(dateInstance)
        }

        val dateStr = fullDateFormat.format(dateInstance).uppercase(Locale.getDefault())
        val accentColor = if (isAmbient) Color.parseColor("#94A3B8") else config.colorInt

        val fontSize = width * 0.22f * config.sizeScale
        textPaint.typeface = getTypeface(config.fontStyle)
        textPaint.textSize = fontSize
        textPaint.textAlign = Paint.Align.CENTER

        if (isAmbient && settings.aodOutlineClock) {
            textPaint.style = Paint.Style.STROKE
            textPaint.strokeWidth = 3f
            textPaint.color = Color.WHITE
            textPaint.clearShadowLayer()
        } else {
            textPaint.style = Paint.Style.FILL
            textPaint.color = accentColor
            if (!isAmbient) textPaint.setShadowLayer(16f, 0f, 6f, 0xAA000000.toInt())
        }

        // Draw Time
        canvas.drawText(timeStr, cx, cy, textPaint)

        // Draw Modern Date Line
        if (config.showDate) {
            secondaryTextPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            secondaryTextPaint.textSize = width * 0.036f * config.sizeScale
            secondaryTextPaint.color = if (isAmbient) Color.parseColor("#64748B") else Color.WHITE
            secondaryTextPaint.textAlign = Paint.Align.CENTER
            secondaryTextPaint.clearShadowLayer()
            if (!isAmbient) secondaryTextPaint.setShadowLayer(8f, 0f, 2f, 0x88000000.toInt())

            val dateY = cy + fontSize * 0.42f
            canvas.drawText(dateStr, cx, dateY, secondaryTextPaint)

            // Thin geometric accent line under date
            if (!isAmbient) {
                strokePaint.strokeWidth = 1.5f
                strokePaint.color = accentColor
                val lineWidth = width * 0.28f * config.sizeScale
                canvas.drawLine(cx - lineWidth, dateY + 16f, cx + lineWidth, dateY + 16f, strokePaint)
            }
        }
    }

    // =========================================================================
    // 5. VINTAGE FLIP CARD (Mechanical Split-Flap Cards)
    // =========================================================================
    private fun renderRetroFlip(
        canvas: Canvas,
        settings: WallpaperSettings,
        isAmbient: Boolean,
        cx: Float,
        cy: Float,
        width: Int,
        hours24: Int,
        hours: Int,
        minutes: Int,
        seconds: Int
    ) {
        val config = settings.clock
        val hStr = String.format("%02d", if (config.is24Hour) hours24 else (if (hours % 12 == 0) 12 else hours % 12))
        val mStr = String.format("%02d", minutes)

        val cardW = width * 0.24f * config.sizeScale
        val cardH = cardW * 1.30f
        val gap = width * 0.04f
        val totalW = (cardW * 2) + gap

        val hourLeft = cx - totalW / 2f
        val minLeft = hourLeft + cardW + gap

        // Draw Hour Card and Minute Card
        drawFlipCard(canvas, hStr, hourLeft, cy - cardH / 2f, cardW, cardH, config, isAmbient)
        drawFlipCard(canvas, mStr, minLeft, cy - cardH / 2f, cardW, cardH, config, isAmbient)

        // Draw Mechanical Colon Divider
        val colonCx = hourLeft + cardW + (gap / 2f)
        shapePaint.color = if (isAmbient) Color.WHITE else config.colorInt
        shapePaint.style = Paint.Style.FILL
        canvas.drawCircle(colonCx, cy - cardH * 0.18f, 5f, shapePaint)
        canvas.drawCircle(colonCx, cy + cardH * 0.18f, 5f, shapePaint)

        // Date Banner below flip cards
        if (config.showDate) {
            val dateStr = dateFormat.format(dateInstance).uppercase(Locale.getDefault())
            secondaryTextPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            secondaryTextPaint.textSize = width * 0.038f * config.sizeScale
            secondaryTextPaint.color = if (isAmbient) Color.parseColor("#64748B") else 0xEEFFFFFF.toInt()
            secondaryTextPaint.textAlign = Paint.Align.CENTER
            if (!isAmbient) secondaryTextPaint.setShadowLayer(8f, 0f, 3f, 0x88000000.toInt())
            canvas.drawText(dateStr, cx, cy + cardH * 0.72f, secondaryTextPaint)
        }
    }

    private fun drawFlipCard(
        canvas: Canvas,
        text: String,
        left: Float,
        top: Float,
        w: Float,
        h: Float,
        config: com.example.model.ClockConfig,
        isAmbient: Boolean
    ) {
        val rect = RectF(left, top, left + w, top + h)
        val corner = 16f

        // Card background
        if (!isAmbient) {
            shapePaint.style = Paint.Style.FILL
            shapePaint.color = 0xEE1E293B.toInt()
            canvas.drawRoundRect(rect, corner, corner, shapePaint)

            // Top half subtle highlight
            shapePaint.color = 0x22FFFFFF.toInt()
            canvas.drawRoundRect(RectF(left, top, left + w, top + h / 2f), corner, corner, shapePaint)

            // Card border
            strokePaint.strokeWidth = 2f
            strokePaint.color = 0x4464748B.toInt()
            canvas.drawRoundRect(rect, corner, corner, strokePaint)

            // Middle horizontal split seam
            strokePaint.strokeWidth = 3f
            strokePaint.color = 0xFF0B0F19.toInt()
            canvas.drawLine(left, top + h / 2f, left + w, top + h / 2f, strokePaint)
        } else {
            strokePaint.strokeWidth = 2f
            strokePaint.color = 0x55FFFFFF.toInt()
            canvas.drawRoundRect(rect, corner, corner, strokePaint)
            canvas.drawLine(left, top + h / 2f, left + w, top + h / 2f, strokePaint)
        }

        // Numeral text
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textPaint.textSize = h * 0.65f
        textPaint.textAlign = Paint.Align.CENTER

        if (isAmbient) {
            textPaint.style = Paint.Style.STROKE
            textPaint.strokeWidth = 3f
            textPaint.color = Color.WHITE
            textPaint.clearShadowLayer()
        } else {
            textPaint.style = Paint.Style.FILL
            textPaint.color = config.colorInt
            textPaint.setShadowLayer(12f, 0f, 4f, 0x88000000.toInt())
        }

        val textY = top + h * 0.72f
        canvas.drawText(text, left + w / 2f, textY, textPaint)
    }
}
