package com.example.engine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.atan2
import kotlin.math.sqrt

class SensorTiltController(context: Context) : SensorEventListener {

    private val sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val rotationSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Current smooth tilt (-1.0f to +1.0f)
    var smoothTiltX: Float = 0f
        private set
    var smoothTiltY: Float = 0f
        private set

    // Raw targets
    private var targetTiltX: Float = 0f
    private var targetTiltY: Float = 0f

    // Touch interactive tilt offset
    private var touchOffsetX: Float = 0f
    private var touchOffsetY: Float = 0f
    private var isTouching: Boolean = false

    private var isRunning: Boolean = false

    // Low-pass filter smoothing coefficient (0.05f to 0.20f)
    var smoothingAlpha: Float = 0.14f

    private val rotationMatrix = FloatArray(9)
    private val orientationVals = FloatArray(3)

    fun start() {
        if (!isRunning && rotationSensor != null) {
            sensorManager.registerListener(
                this,
                rotationSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
            isRunning = true
        }
    }

    fun stop() {
        if (isRunning) {
            sensorManager.unregisterListener(this)
            isRunning = false
        }
    }

    fun updateTick() {
        // Calculate total target including touch
        val finalTargetX = (targetTiltX + touchOffsetX).coerceIn(-1.5f, 1.5f)
        val finalTargetY = (targetTiltY + touchOffsetY).coerceIn(-1.5f, 1.5f)

        // Exponential smoothing (lerp)
        smoothTiltX += (finalTargetX - smoothTiltX) * smoothingAlpha
        smoothTiltY += (finalTargetY - smoothTiltY) * smoothingAlpha

        // Decay touch offset gradually if not touching
        if (!isTouching) {
            touchOffsetX *= 0.92f
            touchOffsetY *= 0.92f
            if (kotlin.math.abs(touchOffsetX) < 0.001f) touchOffsetX = 0f
            if (kotlin.math.abs(touchOffsetY) < 0.001f) touchOffsetY = 0f
        }
    }

    fun onTouchDown() {
        isTouching = true
    }

    fun onTouchMove(deltaXRatio: Float, deltaYRatio: Float) {
        touchOffsetX = (touchOffsetX + deltaXRatio * 2.5f).coerceIn(-1.0f, 1.0f)
        touchOffsetY = (touchOffsetY + deltaYRatio * 2.5f).coerceIn(-1.0f, 1.0f)
    }

    fun onTouchUp() {
        isTouching = false
    }

    fun setManualTilt(x: Float, y: Float) {
        targetTiltX = x.coerceIn(-1.5f, 1.5f)
        targetTiltY = y.coerceIn(-1.5f, 1.5f)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationVals)

                // azimuth = orientationVals[0], pitch = orientationVals[1], roll = orientationVals[2]
                val pitch = orientationVals[1] // Tilt forwards/backwards
                val roll = orientationVals[2]  // Tilt left/right

                // Typical handheld phone position is tilted back ~45 degrees (approx 0.75 rad)
                val normalizedPitch = ((pitch - 0.75f) / 0.8f).coerceIn(-1.0f, 1.0f)
                val normalizedRoll = (roll / 0.8f).coerceIn(-1.0f, 1.0f)

                targetTiltX = -normalizedRoll
                targetTiltY = normalizedPitch
            }
            Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GRAVITY -> {
                val ax = event.values[0]
                val ay = event.values[1]
                val az = event.values[2]

                val norm = sqrt(ax * ax + ay * ay + az * az)
                if (norm > 0.001f) {
                    val nax = ax / norm
                    val nay = ay / norm
                    val naz = az / norm

                    val roll = atan2(nax, sqrt(nay * nay + naz * naz))
                    val pitch = atan2(nay, sqrt(nax * nax + naz * naz))

                    targetTiltX = (roll / 0.7f).coerceIn(-1.0f, 1.0f)
                    targetTiltY = ((pitch - 0.6f) / 0.7f).coerceIn(-1.0f, 1.0f)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
