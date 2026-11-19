package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CustomWallpaperManager
import com.example.data.WallpaperPreferences
import com.example.model.ParticleType
import com.example.model.ThemePreset
import com.example.model.WatchDesignType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DepthX", appName)
    }

    @Test
    fun `wallpaper preferences persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = WallpaperPreferences.getInstance(context)

        assertNotNull(prefs.getSettings())

        val current = prefs.getSettings()
        val updated = current.copy(
            themePreset = ThemePreset.COSMIC,
            particleType = ParticleType.STARDUST,
            tiltSensitivity = 1.8f,
            clock = current.clock.copy(
                watchDesign = WatchDesignType.CHRONO_ANALOG
            )
        )
        prefs.updateSettings(updated)

        val reloaded = prefs.getSettings()
        assertEquals(ThemePreset.COSMIC, reloaded.themePreset)
        assertEquals(ParticleType.STARDUST, reloaded.particleType)
        assertEquals(1.8f, reloaded.tiltSensitivity, 0.001f)
        assertEquals(WatchDesignType.CHRONO_ANALOG, reloaded.clock.watchDesign)
        assertTrue(reloaded.clock.depthBehindSubject)
    }

    @Test
    fun `custom wallpaper theme preset persistence and manager check`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = WallpaperPreferences.getInstance(context)

        prefs.setThemePreset(ThemePreset.CUSTOM)
        assertEquals(ThemePreset.CUSTOM, prefs.getSettings().themePreset)

        CustomWallpaperManager.clearCustomLayers(context)
        assertFalse(CustomWallpaperManager.hasCustomBackground(context))
        assertFalse(CustomWallpaperManager.hasCustomSubject(context))
    }
}
