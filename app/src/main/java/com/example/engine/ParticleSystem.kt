package com.example.engine

import android.graphics.Canvas
import android.graphics.Paint
import com.example.model.ParticleType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class ParticleSystem(private val maxParticles: Int = 120) {

    private class Particle {
        var x: Float = 0f
        var y: Float = 0f
        var speedX: Float = 0f
        var speedY: Float = 0f
        var size: Float = 4f
        var alpha: Int = 200
        var phase: Float = 0f
        var color: Int = 0xFFFFFFFF.toInt()
        var rotation: Float = 0f
        var rotationSpeed: Float = 0f
    }

    private val particles = Array(maxParticles) { Particle() }
    private var activeCount = 50
    private var viewportWidth = 1080
    private var viewportHeight = 2400

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun resize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        viewportWidth = width
        viewportHeight = height
    }

    fun initParticles(type: ParticleType, densityRatio: Float) {
        activeCount = (maxParticles * densityRatio.coerceIn(0.2f, 1.5f)).toInt().coerceIn(10, maxParticles)
        for (i in 0 until activeCount) {
            resetParticle(particles[i], type, randomizeY = true)
        }
    }

    private fun resetParticle(p: Particle, type: ParticleType, randomizeY: Boolean) {
        val random = Random
        p.x = random.nextFloat() * viewportWidth
        p.y = if (randomizeY) random.nextFloat() * viewportHeight else -20f
        p.phase = random.nextFloat() * 6.28f

        when (type) {
            ParticleType.RAIN -> {
                p.speedX = -1.5f - random.nextFloat() * 1.5f
                p.speedY = 22f + random.nextFloat() * 14f
                p.size = 12f + random.nextFloat() * 16f // streak length
                p.alpha = 110 + random.nextInt(120)
                p.color = 0xAA00E5FF.toInt()
            }
            ParticleType.SNOW -> {
                p.speedX = (random.nextFloat() - 0.5f) * 1.2f
                p.speedY = 2.5f + random.nextFloat() * 3.5f
                p.size = 2.5f + random.nextFloat() * 4.5f
                p.alpha = 140 + random.nextInt(100)
                p.color = 0xEEFFFFFF.toInt()
            }
            ParticleType.STARDUST -> {
                p.speedX = (random.nextFloat() - 0.5f) * 0.8f
                p.speedY = -1.2f - random.nextFloat() * 2.0f
                if (!randomizeY) p.y = viewportHeight + 20f
                p.size = 2.0f + random.nextFloat() * 3.5f
                p.alpha = 100 + random.nextInt(155)
                p.color = if (random.nextBoolean()) 0xFF818CF8.toInt() else 0xFF38BDF8.toInt()
            }
            ParticleType.SAKURA -> {
                p.speedX = 1.2f + random.nextFloat() * 2.2f
                p.speedY = 3.0f + random.nextFloat() * 3.0f
                p.size = 5.0f + random.nextFloat() * 6.0f
                p.alpha = 180 + random.nextInt(60)
                p.color = if (random.nextBoolean()) 0xFFF472B6.toInt() else 0xFFFB7185.toInt()
                p.rotation = random.nextFloat() * 360f
                p.rotationSpeed = (random.nextFloat() - 0.5f) * 4f
            }
            ParticleType.NEON_ORBS -> {
                p.speedX = (random.nextFloat() - 0.5f) * 0.6f
                p.speedY = -0.8f - random.nextFloat() * 1.4f
                if (!randomizeY) p.y = viewportHeight + 20f
                p.size = 8.0f + random.nextFloat() * 16.0f
                p.alpha = 60 + random.nextInt(100)
                val colors = intArrayOf(0x9900F5D4.toInt(), 0x99D946EF.toInt(), 0x99F59E0B.toInt(), 0x9938BDF8.toInt())
                p.color = colors[random.nextInt(colors.size)]
            }
            ParticleType.NONE -> {
                p.alpha = 0
            }
        }
    }

    private var currentType: ParticleType = ParticleType.NONE
    private var currentDensity: Float = 1.0f

    fun update(
        type: ParticleType,
        speedMultiplier: Float,
        tiltShiftX: Float,
        densityRatio: Float = 1.0f
    ) {
        if (type != currentType || kotlin.math.abs(densityRatio - currentDensity) > 0.08f) {
            currentType = type
            currentDensity = densityRatio
            initParticles(type, densityRatio)
        }

        if (type == ParticleType.NONE) return

        val speed = speedMultiplier.coerceIn(0.2f, 3.0f)

        for (i in 0 until activeCount) {
            val p = particles[i]
            p.phase += 0.05f

            when (type) {
                ParticleType.RAIN -> {
                    p.x += (p.speedX + tiltShiftX * 12f) * speed
                    p.y += p.speedY * speed
                    if (p.y > viewportHeight + 30f || p.x < -30f || p.x > viewportWidth + 30f) {
                        resetParticle(p, type, randomizeY = false)
                    }
                }
                ParticleType.SNOW -> {
                    val sway = sin(p.phase) * 1.4f
                    p.x += (p.speedX + sway + tiltShiftX * 8f) * speed
                    p.y += p.speedY * speed
                    if (p.y > viewportHeight + 20f) {
                        resetParticle(p, type, randomizeY = false)
                    }
                }
                ParticleType.STARDUST -> {
                    val floatWave = cos(p.phase) * 0.8f
                    p.x += (p.speedX + floatWave + tiltShiftX * 5f) * speed
                    p.y += p.speedY * speed
                    if (p.y < -20f) {
                        resetParticle(p, type, randomizeY = false)
                    }
                }
                ParticleType.SAKURA -> {
                    val flutter = sin(p.phase) * 2.5f
                    p.x += (p.speedX + flutter + tiltShiftX * 10f) * speed
                    p.y += p.speedY * speed
                    p.rotation += p.rotationSpeed
                    if (p.y > viewportHeight + 20f || p.x > viewportWidth + 40f) {
                        resetParticle(p, type, randomizeY = false)
                    }
                }
                ParticleType.NEON_ORBS -> {
                    val drift = sin(p.phase * 0.7f) * 0.9f
                    p.x += (p.speedX + drift + tiltShiftX * 6f) * speed
                    p.y += p.speedY * speed
                    if (p.y < -30f) {
                        resetParticle(p, type, randomizeY = false)
                    }
                }
                ParticleType.NONE -> {}
            }
        }
    }

    fun draw(canvas: Canvas, type: ParticleType) {
        if (type == ParticleType.NONE) return

        for (i in 0 until activeCount) {
            val p = particles[i]
            paint.color = p.color
            paint.alpha = p.alpha

            when (type) {
                ParticleType.RAIN -> {
                    paint.strokeWidth = 2.2f
                    canvas.drawLine(p.x, p.y, p.x + p.speedX * 1.4f, p.y + p.size, paint)
                }
                ParticleType.SNOW -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(p.x, p.y, p.size, paint)
                }
                ParticleType.STARDUST -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(p.x, p.y, p.size, paint)
                }
                ParticleType.SAKURA -> {
                    canvas.save()
                    canvas.translate(p.x, p.y)
                    canvas.rotate(p.rotation)
                    paint.style = Paint.Style.FILL
                    canvas.drawOval(-p.size, -p.size * 0.6f, p.size, p.size * 0.6f, paint)
                    canvas.restore()
                }
                ParticleType.NEON_ORBS -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(p.x, p.y, p.size, paint)
                }
                ParticleType.NONE -> {}
            }
        }
    }
}
