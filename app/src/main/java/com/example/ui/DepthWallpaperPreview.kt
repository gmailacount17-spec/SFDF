package com.example.ui

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.LayeredSceneRenderer
import com.example.engine.SensorTiltController
import com.example.model.WallpaperSettings

class InteractivePreviewView(context: Context) : View(context) {

    private val renderer = LayeredSceneRenderer()
    val tiltController = SensorTiltController(context)

    var settings: WallpaperSettings = WallpaperSettings()
        set(value) {
            val themeChanged = field.themePreset != value.themePreset
            val timestampChanged = field.lastModified != value.lastModified
            field = value
            if (width > 0 && height > 0) {
                if (themeChanged || timestampChanged) {
                    renderer.onThemeChanged(value.themePreset, value, context)
                }
            }
            invalidate()
        }

    fun reloadCustomWallpaper() {
        if (width > 0 && height > 0) {
            renderer.reloadLayers(settings, context)
            invalidate()
        }
    }

    var isAmbientPreview: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private var lastTouchX = 0f
    private var lastTouchY = 0f

    init {
        // Will be started/stopped by Compose lifecycle
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            renderer.onSurfaceChanged(w, h, settings.themePreset, settings, context)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                tiltController.onTouchDown()
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = (event.x - lastTouchX) / (if (width > 0) width else 1000)
                val dy = (event.y - lastTouchY) / (if (height > 0) height else 1000)
                lastTouchX = event.x
                lastTouchY = event.y
                tiltController.onTouchMove(dx, dy)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                tiltController.onTouchUp()
                parent?.requestDisallowInterceptTouchEvent(false)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        if (!isAmbientPreview) {
            tiltController.updateTick()
        }

        renderer.render(
            canvas = canvas,
            rawTiltX = if (isAmbientPreview) 0f else tiltController.smoothTiltX,
            rawTiltY = if (isAmbientPreview) 0f else tiltController.smoothTiltY,
            settings = settings,
            isAmbientMode = isAmbientPreview
        )

        // Request next frame for continuous 60fps sensor/particle updates
        if (!isAmbientPreview) {
            postInvalidateOnAnimation()
        }
    }
}

@Composable
fun DepthWallpaperPreview(
    settings: WallpaperSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isAodMode by remember { mutableStateOf(false) }

    val previewView = remember {
        InteractivePreviewView(context)
    }

    DisposableEffect(previewView) {
        previewView.tiltController.start()
        onDispose {
            previewView.tiltController.stop()
        }
    }

    // Update settings when Compose state changes
    previewView.settings = settings
    previewView.isAmbientPreview = isAodMode

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .clip(RoundedCornerShape(28.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.6f),
                        Color(0xFFD946EF).copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Color(0xFF00F5D4))
            .testTag("wallpaper_preview_container")
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .testTag("interactive_canvas_preview")
        )

        // Top badges overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3D Parallax indicator badge
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (isAodMode) Color(0xFF94A3B8) else Color(0xFF00F5D4), CircleShape)
                    )
                    Text(
                        text = if (isAodMode) "AOD AMBIENT" else "3D DEPTH ACTIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

            // AOD Toggle button
            AssistChip(
                onClick = { isAodMode = !isAodMode },
                label = {
                    Text(
                        text = if (isAodMode) "Normal Mode" else "AOD Preview",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (isAodMode) Icons.Default.WbSunny else Icons.Default.Bedtime,
                        contentDescription = "Toggle AOD preview",
                        tint = if (isAodMode) Color(0xFFFBBF24) else Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color.Black.copy(alpha = 0.65f)
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = Color.White.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("aod_preview_toggle_chip")
            )
        }

        // Bottom helper hint
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Drag or tilt phone to test 3D parallax",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
            )
        }
    }
}
