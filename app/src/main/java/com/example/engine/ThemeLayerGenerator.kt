package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.model.ThemePreset
import kotlin.math.cos
import kotlin.math.sin

import android.content.Context
import com.example.data.CustomWallpaperManager

data class LayerBundle(
    val background: Bitmap,
    val subject: Bitmap,
    val foreground: Bitmap
) {
    fun recycle() {
        if (!background.isRecycled) background.recycle()
        if (!subject.isRecycled) subject.recycle()
        if (!foreground.isRecycled) foreground.recycle()
    }
}

object ThemeLayerGenerator {

    fun generateLayers(theme: ThemePreset, width: Int, height: Int, context: Context? = null): LayerBundle {
        val w = width.coerceAtLeast(400)
        val h = height.coerceAtLeast(600)

        val bgBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val subBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val fgBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val bgCanvas = Canvas(bgBitmap)
        val subCanvas = Canvas(subBitmap)
        val fgCanvas = Canvas(fgBitmap)

        when (theme) {
            ThemePreset.CYBERPUNK -> {
                drawCyberpunkBackground(bgCanvas, w, h)
                drawCyberpunkSubject(subCanvas, w, h)
                drawCyberpunkForeground(fgCanvas, w, h)
            }
            ThemePreset.COSMIC -> {
                drawCosmicBackground(bgCanvas, w, h)
                drawCosmicSubject(subCanvas, w, h)
                drawCosmicForeground(fgCanvas, w, h)
            }
            ThemePreset.ALPINE -> {
                drawAlpineBackground(bgCanvas, w, h)
                drawAlpineSubject(subCanvas, w, h)
                drawAlpineForeground(fgCanvas, w, h)
            }
            ThemePreset.SYNTHWAVE -> {
                drawSynthwaveBackground(bgCanvas, w, h)
                drawSynthwaveSubject(subCanvas, w, h)
                drawSynthwaveForeground(fgCanvas, w, h)
            }
            ThemePreset.FANTASY -> {
                drawFantasyBackground(bgCanvas, w, h)
                drawFantasySubject(subCanvas, w, h)
                drawFantasyForeground(fgCanvas, w, h)
            }
            ThemePreset.CUSTOM -> {
                drawCustomBackground(bgCanvas, w, h, context)
                drawCustomSubject(subCanvas, w, h, context)
                drawCustomForeground(fgCanvas, w, h)
            }
        }

        return LayerBundle(bgBitmap, subBitmap, fgBitmap)
    }

    // ==========================================
    // 1. CYBERPUNK 2099
    // ==========================================
    private fun drawCyberpunkBackground(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Night sky gradient (dark violet to midnight navy)
        paint.shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.parseColor("#090A1A"), Color.parseColor("#170B3B"), Color.parseColor("#2B0938")),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Distant mega-city skyscraper silhouettes
        paint.color = Color.parseColor("#150E28")
        paint.style = Paint.Style.FILL

        val buildingCount = 18
        for (i in 0 until buildingCount) {
            val bx = (i * (w / 14f)) - 30f
            val bw = (w / 16f) + (i % 3) * 15f
            val bh = h * (0.45f + (i % 5) * 0.08f)
            canvas.drawRect(bx, h - bh, bx + bw, h.toFloat(), paint)

            // Building illuminated windows
            val winPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            winPaint.color = if (i % 2 == 0) Color.parseColor("#4400F5D4") else Color.parseColor("#44D946EF")
            for (wy in (h - bh + 20).toInt() until (h - 80) step 35) {
                if ((wy + i * 7) % 3 == 0) {
                    canvas.drawRect(bx + 6f, wy.toFloat(), bx + bw - 6f, wy + 4f, winPaint)
                }
            }
        }

        // Distant glowing holo-moon / neon billboard
        val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        moonPaint.shader = RadialGradient(
            w * 0.72f, h * 0.28f, w * 0.35f,
            intArrayOf(Color.parseColor("#8800E5FF"), Color.parseColor("#33D946EF"), Color.TRANSPARENT),
            floatArrayOf(0f, 0.4f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(w * 0.72f, h * 0.28f, w * 0.35f, moonPaint)
    }

    private fun drawCyberpunkSubject(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Vantage ledge / rooftop platform
        paint.color = Color.parseColor("#0C0E17")
        paint.style = Paint.Style.FILL
        val ledgePath = Path().apply {
            moveTo(0f, h * 0.78f)
            lineTo(w * 0.85f, h * 0.74f)
            lineTo(w.toFloat(), h * 0.76f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        canvas.drawPath(ledgePath, paint)

        // Neon ledge accent line
        val neonLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00F5D4")
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(0f, h * 0.78f, w * 0.85f, h * 0.74f, neonLine)

        // Central Cyber Samurai silhouette standing on the vantage point
        val cx = w * 0.50f
        val footY = h * 0.75f
        val bodyH = h * 0.42f
        val headY = footY - bodyH

        val warriorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#080911")
            style = Paint.Style.FILL
        }

        // Flowing cyber trenchcoat / cape
        val coatPath = Path().apply {
            moveTo(cx, headY + bodyH * 0.25f)
            // Left cape blowing in wind
            cubicTo(cx - w * 0.15f, headY + bodyH * 0.45f, cx - w * 0.22f, footY - 20f, cx - w * 0.28f, footY + 10f)
            lineTo(cx - w * 0.08f, footY - 10f)
            // Legs / boots
            lineTo(cx - w * 0.05f, footY)
            lineTo(cx + w * 0.06f, footY)
            // Right cape
            lineTo(cx + w * 0.12f, footY - 15f)
            cubicTo(cx + w * 0.16f, headY + bodyH * 0.6f, cx + w * 0.12f, headY + bodyH * 0.35f, cx, headY + bodyH * 0.25f)
            close()
        }
        canvas.drawPath(coatPath, warriorPaint)

        // Torso & Shoulders
        val torso = Path().apply {
            moveTo(cx - w * 0.09f, headY + bodyH * 0.25f)
            lineTo(cx + w * 0.09f, headY + bodyH * 0.25f)
            lineTo(cx + w * 0.06f, headY + bodyH * 0.55f)
            lineTo(cx - w * 0.06f, headY + bodyH * 0.55f)
            close()
        }
        canvas.drawPath(torso, warriorPaint)

        // Cybernetic Helmet / Head
        val headRadius = w * 0.065f
        canvas.drawCircle(cx, headY + headRadius, headRadius, warriorPaint)

        // Glowing visor slit (Cyan / Hot Pink)
        val visorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00F5D4")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(cx - headRadius * 0.8f, headY + headRadius * 0.85f, cx + headRadius * 0.8f, headY + headRadius * 1.15f),
            4f, 4f, visorPaint
        )

        // High-tech Katana blade across back with glowing red/pink edge
        val swordPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D946EF")
            strokeWidth = 4.5f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(cx + w * 0.08f, headY - 30f, cx - w * 0.15f, headY + bodyH * 0.7f, swordPaint)
    }

    private fun drawCyberpunkForeground(canvas: Canvas, w: Int, h: Int) {
        // Foreground overhanging industrial cables and neon signs
        val cablePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#05060A")
            strokeWidth = 6f
            style = Paint.Style.STROKE
        }

        val cablePath = Path().apply {
            moveTo(-20f, h * 0.12f)
            cubicTo(w * 0.3f, h * 0.22f, w * 0.7f, h * 0.18f, w + 20f, h * 0.08f)
        }
        canvas.drawPath(cablePath, cablePaint)

        val cablePath2 = Path().apply {
            moveTo(-20f, h * 0.16f)
            cubicTo(w * 0.25f, h * 0.28f, w * 0.75f, h * 0.24f, w + 20f, h * 0.14f)
        }
        canvas.drawPath(cablePath2, cablePaint)

        // Hanging holographic lantern / sign on left
        val signBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#080A14")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(w * 0.06f, h * 0.18f, w * 0.24f, h * 0.36f), 12f, 12f, signBg)

        val signGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00F5D4")
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(RectF(w * 0.06f, h * 0.18f, w * 0.24f, h * 0.36f), 12f, 12f, signGlow)
    }

    // ==========================================
    // 2. COSMIC ODYSSEY
    // ==========================================
    private fun drawCosmicBackground(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Deep cosmos gradient
        paint.shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.parseColor("#030712"), Color.parseColor("#0F172A"), Color.parseColor("#1E1B4B")),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Glowing spiral nebula cloud
        val nebulaPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        nebulaPaint.shader = RadialGradient(
            w * 0.35f, h * 0.32f, w * 0.55f,
            intArrayOf(Color.parseColor("#88818CF8"), Color.parseColor("#44C084FC"), Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(w * 0.35f, h * 0.32f, w * 0.55f, nebulaPaint)

        // Distant stars
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        for (i in 0 until 80) {
            val sx = ((i * 137.5f) % w)
            val sy = ((i * 389.3f) % h)
            val sRadius = 1.0f + (i % 3) * 0.8f
            starPaint.alpha = 100 + (i * 17 % 155)
            canvas.drawCircle(sx, sy, sRadius, starPaint)
        }

        // Distant crescent planet in upper right
        val planetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w * 0.82f, h * 0.18f, w * 0.22f,
                intArrayOf(Color.parseColor("#38BDF8"), Color.parseColor("#0369A1"), Color.parseColor("#082F49")),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w * 0.82f, h * 0.18f, w * 0.16f, planetPaint)
    }

    private fun drawCosmicSubject(canvas: Canvas, w: Int, h: Int) {
        val cx = w * 0.50f
        val cy = h * 0.62f
        val bodyW = w * 0.32f
        val bodyH = h * 0.36f

        val suitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.FILL
        }

        // Astronaut Life Support Backpack
        val packPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(cx - bodyW * 0.55f, cy - bodyH * 0.35f, cx + bodyW * 0.55f, cy + bodyH * 0.25f), 24f, 24f, packPaint)

        // Space Suit Torso
        canvas.drawRoundRect(RectF(cx - bodyW * 0.45f, cy - bodyH * 0.25f, cx + bodyW * 0.45f, cy + bodyH * 0.30f), 30f, 30f, suitPaint)

        // Helmet Sphere
        val headRadius = bodyW * 0.42f
        val headY = cy - bodyH * 0.35f
        canvas.drawCircle(cx, headY, headRadius, suitPaint)

        // Gold reflective visor with cosmic reflection!
        val visorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                cx - headRadius, headY - headRadius, cx + headRadius, headY + headRadius,
                intArrayOf(Color.parseColor("#F59E0B"), Color.parseColor("#FBBF24"), Color.parseColor("#92400E")),
                null,
                Shader.TileMode.CLAMP
            )
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(cx - headRadius * 0.75f, headY - headRadius * 0.6f, cx + headRadius * 0.75f, headY + headRadius * 0.6f), 22f, 22f, visorPaint)

        // Visor glare shine
        val glarePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 180
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        canvas.drawArc(RectF(cx - headRadius * 0.65f, headY - headRadius * 0.5f, cx + headRadius * 0.65f, headY + headRadius * 0.5f), 200f, 60f, false, glarePaint)

        // Floating legs & arms (spacewalk pose)
        val limbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.FILL
        }
        // Left arm reaching
        canvas.drawRoundRect(RectF(cx - bodyW * 0.75f, cy - bodyH * 0.15f, cx - bodyW * 0.38f, cy + bodyH * 0.1f), 18f, 18f, limbPaint)
        // Right arm
        canvas.drawRoundRect(RectF(cx + bodyW * 0.38f, cy - bodyH * 0.15f, cx + bodyW * 0.75f, cy + bodyH * 0.1f), 18f, 18f, limbPaint)
        // Left leg
        canvas.drawRoundRect(RectF(cx - bodyW * 0.40f, cy + bodyH * 0.25f, cx - bodyW * 0.08f, cy + bodyH * 0.55f), 18f, 18f, limbPaint)
        // Right leg
        canvas.drawRoundRect(RectF(cx + bodyW * 0.08f, cy + bodyH * 0.25f, cx + bodyW * 0.40f, cy + bodyH * 0.55f), 18f, 18f, limbPaint)
    }

    private fun drawCosmicForeground(canvas: Canvas, w: Int, h: Int) {
        // High-speed asteroid / rock passing close in foreground
        val rockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }

        val rock = Path().apply {
            moveTo(w * 0.78f, h * 0.82f)
            lineTo(w * 0.98f, h * 0.78f)
            lineTo(w.toFloat(), h * 0.92f)
            lineTo(w * 0.88f, h.toFloat())
            lineTo(w * 0.74f, h * 0.94f)
            close()
        }
        canvas.drawPath(rock, rockPaint)

        // Close space dust / solar glare corner
        val glareCorner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w.toFloat(), 0f, w * 0.4f,
                intArrayOf(Color.parseColor("#5538BDF8"), Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w.toFloat(), 0f, w * 0.4f, glareCorner)
    }

    // ==========================================
    // 3. ALPINE TWILIGHT
    // ==========================================
    private fun drawAlpineBackground(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Sunset mountain sky
        paint.shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.parseColor("#0C1824"), Color.parseColor("#1E2D42"), Color.parseColor("#4A3B52"), Color.parseColor("#D97706")),
            floatArrayOf(0f, 0.35f, 0.65f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Distant majestic jagged snowy peaks
        val peakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1B2A3D")
            style = Paint.Style.FILL
        }
        val peaks = Path().apply {
            moveTo(0f, h * 0.58f)
            lineTo(w * 0.22f, h * 0.36f)
            lineTo(w * 0.38f, h * 0.46f)
            lineTo(w * 0.58f, h * 0.30f)
            lineTo(w * 0.78f, h * 0.48f)
            lineTo(w.toFloat(), h * 0.38f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        canvas.drawPath(peaks, peakPaint)

        // Snow caps on peaks
        val snowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D0E2EC")
            style = Paint.Style.FILL
        }
        val snowCap = Path().apply {
            moveTo(w * 0.58f, h * 0.30f)
            lineTo(w * 0.52f, h * 0.36f)
            lineTo(w * 0.58f, h * 0.38f)
            lineTo(w * 0.64f, h * 0.36f)
            close()
        }
        canvas.drawPath(snowCap, snowPaint)
    }

    private fun drawAlpineSubject(canvas: Canvas, w: Int, h: Int) {
        val ridgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0E1622")
            style = Paint.Style.FILL
        }

        // Forest ridge slope
        val ridge = Path().apply {
            moveTo(0f, h * 0.70f)
            cubicTo(w * 0.35f, h * 0.68f, w * 0.65f, h * 0.74f, w.toFloat(), h * 0.66f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        canvas.drawPath(ridge, ridgePaint)

        // Majestic Stag / Elk silhouette atop the ridge
        val cx = w * 0.50f
        val groundY = h * 0.71f
        val stagH = h * 0.24f

        val stagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#060A10")
            style = Paint.Style.FILL
        }

        // Stag Body
        canvas.drawOval(cx - w * 0.08f, groundY - stagH * 0.55f, cx + w * 0.08f, groundY - stagH * 0.25f, stagPaint)
        // Neck & Head
        val neck = Path().apply {
            moveTo(cx + w * 0.04f, groundY - stagH * 0.45f)
            lineTo(cx + w * 0.12f, groundY - stagH * 0.85f)
            lineTo(cx + w * 0.07f, groundY - stagH * 0.85f)
            lineTo(cx - w * 0.02f, groundY - stagH * 0.45f)
            close()
        }
        canvas.drawPath(neck, stagPaint)

        // Head
        canvas.drawOval(cx + w * 0.06f, groundY - stagH * 0.95f, cx + w * 0.15f, groundY - stagH * 0.78f, stagPaint)

        // Antlers
        val antlerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#060A10")
            strokeWidth = 3.5f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        // Main beam
        canvas.drawLine(cx + w * 0.09f, groundY - stagH * 0.92f, cx + w * 0.06f, groundY - stagH * 1.15f, antlerPaint)
        canvas.drawLine(cx + w * 0.06f, groundY - stagH * 1.15f, cx + w * 0.02f, groundY - stagH * 1.25f, antlerPaint)
        // Tines
        canvas.drawLine(cx + w * 0.08f, groundY - stagH * 1.0f, cx + w * 0.14f, groundY - stagH * 1.08f, antlerPaint)
        canvas.drawLine(cx + w * 0.06f, groundY - stagH * 1.12f, cx + w * 0.12f, groundY - stagH * 1.20f, antlerPaint)

        // Stag Legs
        canvas.drawLine(cx - w * 0.05f, groundY - stagH * 0.35f, cx - w * 0.05f, groundY, antlerPaint)
        canvas.drawLine(cx - w * 0.02f, groundY - stagH * 0.35f, cx - w * 0.02f, groundY, antlerPaint)
        canvas.drawLine(cx + w * 0.04f, groundY - stagH * 0.35f, cx + w * 0.04f, groundY, antlerPaint)
        canvas.drawLine(cx + w * 0.06f, groundY - stagH * 0.35f, cx + w * 0.06f, groundY, antlerPaint)
    }

    private fun drawAlpineForeground(canvas: Canvas, w: Int, h: Int) {
        // Close pine branch silhouettes and misty fog
        val pinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#04060A")
            style = Paint.Style.FILL
        }

        // Left pine trees
        for (i in 0 until 4) {
            val px = (i * 50f) - 30f
            val py = h * (0.78f + i * 0.05f)
            val pw = w * 0.22f
            val ph = h * 0.26f
            val tree = Path().apply {
                moveTo(px + pw * 0.5f, py - ph)
                lineTo(px + pw, py)
                lineTo(px, py)
                close()
            }
            canvas.drawPath(tree, pinePaint)
        }

        // Fog layer across bottom
        val fogPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, h * 0.85f, 0f, h.toFloat(),
                intArrayOf(Color.TRANSPARENT, Color.parseColor("#441E2D42"), Color.parseColor("#AA0C1824")),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, h * 0.82f, w.toFloat(), h.toFloat(), fogPaint)
    }

    // ==========================================
    // 4. SYNTHWAVE / OUTRUN
    // ==========================================
    private fun drawSynthwaveBackground(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 80s gradient
        paint.shader = LinearGradient(
            0f, 0f, 0f, h * 0.65f,
            intArrayOf(Color.parseColor("#18002E"), Color.parseColor("#4A0E4E"), Color.parseColor("#E02476")),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h * 0.65f, paint)
        paint.shader = null

        // Big segmented wireframe sun
        val sunCx = w * 0.5f
        val sunCy = h * 0.48f
        val sunRadius = w * 0.28f

        val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                sunCx, sunCy - sunRadius, sunCx, sunCy + sunRadius,
                intArrayOf(Color.parseColor("#FDE047"), Color.parseColor("#F97316"), Color.parseColor("#EC4899")),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(sunCx, sunCy, sunRadius, sunPaint)

        // Cut horizontal sunset scanlines through the lower half of sun
        val scanline = Paint().apply {
            color = Color.parseColor("#4A0E4E")
        }
        for (step in 1..7) {
            val ly = sunCy + (step * (sunRadius / 8f))
            val lineH = step * 2.2f
            canvas.drawRect(sunCx - sunRadius, ly, sunCx + sunRadius, ly + lineH, scanline)
        }

        // Horizon line & 3D grid floor
        val floorPaint = Paint().apply {
            color = Color.parseColor("#090214")
        }
        canvas.drawRect(0f, h * 0.65f, w.toFloat(), h.toFloat(), floorPaint)

        // Perspective grid lines
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D946EF")
            strokeWidth = 2.0f
        }
        // Horizontal lines with perspective compression
        var gy = h * 0.66f
        var gap = 8f
        while (gy < h) {
            canvas.drawLine(0f, gy, w.toFloat(), gy, gridPaint)
            gy += gap
            gap *= 1.35f
        }
        // Radial perspective lines
        for (i in -8..8) {
            val targetX = sunCx + i * (w / 7f)
            canvas.drawLine(sunCx, h * 0.65f, targetX, h.toFloat(), gridPaint)
        }
    }

    private fun drawSynthwaveSubject(canvas: Canvas, w: Int, h: Int) {
        // Retro Supercar speeding into horizon
        val carCx = w * 0.50f
        val carCy = h * 0.78f
        val carW = w * 0.48f
        val carH = h * 0.14f

        val carPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F051D")
            style = Paint.Style.FILL
        }

        // Car Body wedge shape
        val carBody = Path().apply {
            moveTo(carCx - carW * 0.45f, carCy + carH * 0.35f)
            lineTo(carCx + carW * 0.45f, carCy + carH * 0.35f)
            lineTo(carCx + carW * 0.40f, carCy - carH * 0.05f)
            // Rear window roof
            lineTo(carCx + carW * 0.28f, carCy - carH * 0.45f)
            lineTo(carCx - carW * 0.28f, carCy - carH * 0.45f)
            lineTo(carCx - carW * 0.40f, carCy - carH * 0.05f)
            close()
        }
        canvas.drawPath(carBody, carPaint)

        // Rear glowing LED lightbar (electric red/magenta)
        val lightbar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EF4444")
            strokeWidth = 6f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(carCx - carW * 0.38f, carCy + carH * 0.08f, carCx + carW * 0.38f, carCy + carH * 0.08f, lightbar)

        // Dual exhaust glowing cyan tips
        val exhaust = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00F5D4")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(carCx - carW * 0.22f, carCy + carH * 0.30f, 6f, exhaust)
        canvas.drawCircle(carCx + carW * 0.22f, carCy + carH * 0.30f, 6f, exhaust)
    }

    private fun drawSynthwaveForeground(canvas: Canvas, w: Int, h: Int) {
        // Silhouetted retro palm trees on left and right borders
        val palmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#05010A")
            style = Paint.Style.FILL
        }

        // Left palm trunk
        val trunkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#05010A")
            strokeWidth = 14f
            style = Paint.Style.STROKE
        }
        val trunk = Path().apply {
            moveTo(-10f, h.toFloat())
            cubicTo(w * 0.12f, h * 0.7f, w * 0.05f, h * 0.45f, w * 0.18f, h * 0.32f)
        }
        canvas.drawPath(trunk, trunkPaint)

        // Palm fronds
        val frondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#05010A")
            strokeWidth = 6f
            style = Paint.Style.STROKE
        }
        val topX = w * 0.18f
        val topY = h * 0.32f
        for (angle in listOf(-60f, -20f, 20f, 60f, 100f)) {
            val rad = Math.toRadians(angle.toDouble())
            val ex = topX + (cos(rad) * w * 0.28f).toFloat()
            val ey = topY + (sin(rad) * w * 0.20f).toFloat()
            canvas.drawLine(topX, topY, ex, ey, frondPaint)
        }
    }

    // ==========================================
    // 5. FLOATING CITADEL
    // ==========================================
    private fun drawFantasyBackground(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Pastel fantasy dawn
        paint.shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(Color.parseColor("#2E1065"), Color.parseColor("#7C3AED"), Color.parseColor("#F472B6"), Color.parseColor("#FEF08A")),
            floatArrayOf(0f, 0.4f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Distant floating rocks and cloud banks
        val cloudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#44FBCFE8")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(w * 0.25f, h * 0.52f, w * 0.30f, cloudPaint)
        canvas.drawCircle(w * 0.75f, h * 0.48f, w * 0.35f, cloudPaint)

        // Distant small floating island
        val distIsland = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4C1D95")
            style = Paint.Style.FILL
        }
        canvas.drawOval(w * 0.65f, h * 0.35f, w * 0.88f, h * 0.42f, distIsland)
    }

    private fun drawFantasySubject(canvas: Canvas, w: Int, h: Int) {
        val cx = w * 0.50f
        val cy = h * 0.64f
        val islandW = w * 0.70f
        val islandH = h * 0.28f

        val rockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E1065")
            style = Paint.Style.FILL
        }

        // Giant floating island inverted cone
        val islandPath = Path().apply {
            moveTo(cx - islandW * 0.5f, cy)
            lineTo(cx + islandW * 0.5f, cy)
            lineTo(cx + islandW * 0.2f, cy + islandH)
            lineTo(cx, cy + islandH * 1.25f)
            lineTo(cx - islandW * 0.2f, cy + islandH)
            close()
        }
        canvas.drawPath(islandPath, rockPaint)

        // Green lush top surface
        val grassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#059669")
            style = Paint.Style.FILL
        }
        canvas.drawOval(cx - islandW * 0.52f, cy - 18f, cx + islandW * 0.52f, cy + 18f, grassPaint)

        // Falling waterfall off island edge
        val waterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A7F3D0")
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(cx - islandW * 0.2f, cy + 10f, cx - islandW * 0.2f, cy + islandH * 1.4f, waterPaint)

        // Castle Citadel on top of the island
        val castlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E1B4B")
            style = Paint.Style.FILL
        }
        // Central tower
        canvas.drawRect(cx - w * 0.08f, cy - h * 0.22f, cx + w * 0.08f, cy, castlePaint)
        // Spire roof
        val roof = Path().apply {
            moveTo(cx, cy - h * 0.29f)
            lineTo(cx + w * 0.09f, cy - h * 0.22f)
            lineTo(cx - w * 0.09f, cy - h * 0.22f)
            close()
        }
        canvas.drawPath(roof, castlePaint)

        // Left & right guard towers
        canvas.drawRect(cx - w * 0.20f, cy - h * 0.14f, cx - w * 0.10f, cy, castlePaint)
        canvas.drawRect(cx + w * 0.10f, cy - h * 0.14f, cx + w * 0.20f, cy, castlePaint)
    }

    private fun drawFantasyForeground(canvas: Canvas, w: Int, h: Int) {
        // Circling flying dragon/bird silhouettes in close foreground
        val dragonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#180D35")
            style = Paint.Style.FILL
        }

        // Dragon in top right
        val dx = w * 0.82f
        val dy = h * 0.18f
        val dragon = Path().apply {
            moveTo(dx, dy)
            // Left wing
            cubicTo(dx - 30f, dy - 20f, dx - 45f, dy - 15f, dx - 50f, dy - 5f)
            lineTo(dx - 20f, dy)
            // Tail
            lineTo(dx, dy + 15f)
            // Right wing
            lineTo(dx + 20f, dy)
            cubicTo(dx + 45f, dy - 15f, dx + 30f, dy - 20f, dx, dy)
            close()
        }
        canvas.drawPath(dragon, dragonPaint)

        // Foreground dreamy cloud veil across top left
        val cloudVeil = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                0f, 0f, w * 0.45f,
                intArrayOf(Color.parseColor("#55F472B6"), Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(0f, 0f, w * 0.45f, cloudVeil)
    }

    // ==========================================
    // 6. CUSTOM GALLERY UPLOAD
    // ==========================================
    private fun drawCustomBackground(canvas: Canvas, w: Int, h: Int, context: Context?) {
        val customBg = context?.let { CustomWallpaperManager.loadCustomBackground(it, w, h) }
        if (customBg != null) {
            drawBitmapCenterCrop(canvas, customBg, w, h)
        } else {
            drawCustomPlaceholder(canvas, w, h)
        }
    }

    private fun drawCustomSubject(canvas: Canvas, w: Int, h: Int, context: Context?) {
        val customSub = context?.let { CustomWallpaperManager.loadCustomSubject(it, w, h) }
        if (customSub != null) {
            drawBitmapCenterCrop(canvas, customSub, w, h)
        } else {
            val customBg = context?.let { CustomWallpaperManager.loadCustomBackground(it, w, h) }
            if (customBg != null) {
                // Smart depth layer: takes lower region with soft feathering
                drawFeatheredSubjectLayer(canvas, customBg, w, h)
            }
        }
    }

    private fun drawCustomForeground(canvas: Canvas, w: Int, h: Int) {
        // Atmospheric cinematic vignette
        val vignette = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w / 2f, h / 2f, (w * 0.72f).coerceAtLeast(h * 0.52f),
                intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, 0x33000000.toInt(), 0x77000000.toInt()),
                floatArrayOf(0f, 0.65f, 0.88f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), vignette)
    }

    private fun drawBitmapCenterCrop(canvas: Canvas, bitmap: Bitmap, viewW: Int, viewH: Int) {
        val bw = bitmap.width.toFloat()
        val bh = bitmap.height.toFloat()
        val scale = kotlin.math.max(viewW / bw, viewH / bh)
        val scaledW = bw * scale
        val scaledH = bh * scale
        val left = (viewW - scaledW) / 2f
        val top = (viewH - scaledH) / 2f

        val src = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
        val dst = RectF(left, top, left + scaledW, top + scaledH)
        val p = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(bitmap, src, dst, p)
    }

    private fun drawFeatheredSubjectLayer(canvas: Canvas, bitmap: Bitmap, viewW: Int, viewH: Int) {
        // Draw the bottom ~50% of the image onto the subject canvas so that when clock is placed behind,
        // the clock tucks behind the foreground subject/landscape
        val bw = bitmap.width.toFloat()
        val bh = bitmap.height.toFloat()
        val scale = kotlin.math.max(viewW / bw, viewH / bh)
        val scaledW = bw * scale
        val scaledH = bh * scale
        val left = (viewW - scaledW) / 2f
        val top = (viewH - scaledH) / 2f

        val splitY = viewH * 0.44f

        canvas.save()
        // Clip to the lower portion
        canvas.clipRect(0f, splitY, viewW.toFloat(), viewH.toFloat())

        val src = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
        val dst = RectF(left, top, left + scaledW, top + scaledH)
        val p = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(bitmap, src, dst, p)
        canvas.restore()

        // Soft gradient fade line to seamlessly blend with background
        val blendPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, splitY - 15f, 0f, splitY + 45f,
                intArrayOf(Color.TRANSPARENT, 0x55000000.toInt(), Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, splitY - 15f, viewW.toFloat(), splitY + 45f, blendPaint)
    }

    private fun drawCustomPlaceholder(canvas: Canvas, w: Int, h: Int) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                intArrayOf(Color.parseColor("#090D16"), Color.parseColor("#1E293B"), Color.parseColor("#0F172A")),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Subtle tech grid lines
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x2238BDF8.toInt()
            strokeWidth = 1f
        }
        for (x in 0..w step 80) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), h.toFloat(), gridPaint)
        }
        for (y in 0..h step 80) {
            canvas.drawLine(0f, y.toFloat(), w.toFloat(), y.toFloat(), gridPaint)
        }

        // Center card hint
        val cx = w / 2f
        val cy = h / 2f
        val cardW = w * 0.70f
        val cardH = h * 0.22f
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xAA0B0F19.toInt()
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF00F5D4.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        val rect = RectF(cx - cardW / 2f, cy - cardH / 2f, cx + cardW / 2f, cy + cardH / 2f)
        canvas.drawRoundRect(rect, 24f, 24f, cardPaint)
        canvas.drawRoundRect(rect, 24f, 24f, borderPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = w * 0.048f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("No Photo Uploaded", cx, cy - 12f, textPaint)

        val subText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xCC94A3B8.toInt()
            textSize = w * 0.034f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Tap 'Upload from Gallery' below", cx, cy + 32f, subText)
    }
}
