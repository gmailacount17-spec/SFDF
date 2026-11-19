package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ClockColorPreset
import com.example.model.ClockConfig
import com.example.model.ClockFontStyle
import com.example.model.ParticleType
import com.example.model.ThemePreset
import com.example.model.WallpaperSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WallpaperPreferences(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<WallpaperSettings> = _settingsFlow.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        _settingsFlow.value = loadSettings()
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun getSettings(): WallpaperSettings = loadSettings()

    fun updateSettings(settings: WallpaperSettings) {
        prefs.edit().apply {
            putString(KEY_THEME, settings.themePreset.id)
            putFloat(KEY_TILT_SENSITIVITY, settings.tiltSensitivity)
            putBoolean(KEY_TOUCH_TILT, settings.touchTiltEnabled)
            putBoolean(KEY_INVERT_TILT, settings.invertTilt)

            // Clock
            putBoolean(KEY_CLOCK_ENABLED, settings.clock.enabled)
            putBoolean(KEY_CLOCK_DEPTH_BEHIND, settings.clock.depthBehindSubject)
            putString(KEY_WATCH_DESIGN, settings.clock.watchDesign.name)
            putString(KEY_CLOCK_FONT, settings.clock.fontStyle.name)
            putInt(KEY_CLOCK_COLOR, settings.clock.colorInt)
            putBoolean(KEY_CLOCK_24H, settings.clock.is24Hour)
            putBoolean(KEY_CLOCK_SECONDS, settings.clock.showSeconds)
            putBoolean(KEY_CLOCK_DATE, settings.clock.showDate)
            putFloat(KEY_CLOCK_SIZE, settings.clock.sizeScale)
            putFloat(KEY_CLOCK_V_OFFSET, settings.clock.verticalOffsetRatio)

            // Particles
            putString(KEY_PARTICLE_TYPE, settings.particleType.name)
            putFloat(KEY_PARTICLE_DENSITY, settings.particleDensity)
            putFloat(KEY_PARTICLE_SPEED, settings.particleSpeed)

            // Battery / AOD
            putBoolean(KEY_AOD_OUTLINE, settings.aodOutlineClock)
            putBoolean(KEY_BATTERY_SAVER, settings.batterySaverEnabled)
            putLong(KEY_LAST_MODIFIED, settings.lastModified)
            apply()
        }
        _settingsFlow.value = settings
    }

    fun notifyCustomContentChanged() {
        val current = _settingsFlow.value
        updateSettings(current.copy(
            themePreset = ThemePreset.CUSTOM,
            lastModified = System.currentTimeMillis()
        ))
    }

    fun setThemePreset(theme: ThemePreset) {
        val current = _settingsFlow.value
        updateSettings(current.copy(
            themePreset = theme,
            particleType = theme.defaultParticles
        ))
    }

    private fun loadSettings(): WallpaperSettings {
        val themeId = prefs.getString(KEY_THEME, ThemePreset.CYBERPUNK.id)
        val theme = ThemePreset.values().firstOrNull { it.id == themeId } ?: ThemePreset.CYBERPUNK

        val clockFontName = prefs.getString(KEY_CLOCK_FONT, ClockFontStyle.BOLD_MODERN.name)
        val clockFont = try {
            ClockFontStyle.valueOf(clockFontName ?: ClockFontStyle.BOLD_MODERN.name)
        } catch (_: Exception) {
            ClockFontStyle.BOLD_MODERN
        }

        val particleName = prefs.getString(KEY_PARTICLE_TYPE, theme.defaultParticles.name)
        val particleType = try {
            ParticleType.valueOf(particleName ?: theme.defaultParticles.name)
        } catch (_: Exception) {
            theme.defaultParticles
        }

        val watchDesignName = prefs.getString(KEY_WATCH_DESIGN, com.example.model.WatchDesignType.BIG_STACKED.name)
        val watchDesign = try {
            com.example.model.WatchDesignType.valueOf(watchDesignName ?: com.example.model.WatchDesignType.BIG_STACKED.name)
        } catch (_: Exception) {
            com.example.model.WatchDesignType.BIG_STACKED
        }

        val clock = ClockConfig(
            enabled = prefs.getBoolean(KEY_CLOCK_ENABLED, true),
            depthBehindSubject = prefs.getBoolean(KEY_CLOCK_DEPTH_BEHIND, true),
            watchDesign = watchDesign,
            fontStyle = clockFont,
            colorInt = prefs.getInt(KEY_CLOCK_COLOR, ClockColorPreset.PURE_WHITE.colorInt),
            is24Hour = prefs.getBoolean(KEY_CLOCK_24H, false),
            showSeconds = prefs.getBoolean(KEY_CLOCK_SECONDS, false),
            showDate = prefs.getBoolean(KEY_CLOCK_DATE, true),
            sizeScale = prefs.getFloat(KEY_CLOCK_SIZE, 1.0f),
            verticalOffsetRatio = prefs.getFloat(KEY_CLOCK_V_OFFSET, 0.22f)
        )

        return WallpaperSettings(
            themePreset = theme,
            tiltSensitivity = prefs.getFloat(KEY_TILT_SENSITIVITY, 1.2f),
            touchTiltEnabled = prefs.getBoolean(KEY_TOUCH_TILT, true),
            invertTilt = prefs.getBoolean(KEY_INVERT_TILT, false),
            clock = clock,
            particleType = particleType,
            particleDensity = prefs.getFloat(KEY_PARTICLE_DENSITY, 1.0f),
            particleSpeed = prefs.getFloat(KEY_PARTICLE_SPEED, 1.0f),
            aodOutlineClock = prefs.getBoolean(KEY_AOD_OUTLINE, true),
            batterySaverEnabled = prefs.getBoolean(KEY_BATTERY_SAVER, false),
            lastModified = prefs.getLong(KEY_LAST_MODIFIED, 0L)
        )
    }

    companion object {
        private const val PREFS_NAME = "depthx_wallpaper_prefs"

        private const val KEY_THEME = "pref_theme"
        private const val KEY_TILT_SENSITIVITY = "pref_tilt_sensitivity"
        private const val KEY_TOUCH_TILT = "pref_touch_tilt"
        private const val KEY_INVERT_TILT = "pref_invert_tilt"

        private const val KEY_CLOCK_ENABLED = "pref_clock_enabled"
        private const val KEY_CLOCK_DEPTH_BEHIND = "pref_clock_depth_behind"
        private const val KEY_WATCH_DESIGN = "pref_watch_design"
        private const val KEY_CLOCK_FONT = "pref_clock_font"
        private const val KEY_CLOCK_COLOR = "pref_clock_color"
        private const val KEY_CLOCK_24H = "pref_clock_24h"
        private const val KEY_CLOCK_SECONDS = "pref_clock_seconds"
        private const val KEY_CLOCK_DATE = "pref_clock_date"
        private const val KEY_CLOCK_SIZE = "pref_clock_size"
        private const val KEY_CLOCK_V_OFFSET = "pref_clock_v_offset"

        private const val KEY_PARTICLE_TYPE = "pref_particle_type"
        private const val KEY_PARTICLE_DENSITY = "pref_particle_density"
        private const val KEY_PARTICLE_SPEED = "pref_particle_speed"

        private const val KEY_AOD_OUTLINE = "pref_aod_outline"
        private const val KEY_BATTERY_SAVER = "pref_battery_saver"
        private const val KEY_LAST_MODIFIED = "pref_last_modified"

        @Volatile
        private var INSTANCE: WallpaperPreferences? = null

        fun getInstance(context: Context): WallpaperPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WallpaperPreferences(context).also { INSTANCE = it }
            }
        }
    }
}
