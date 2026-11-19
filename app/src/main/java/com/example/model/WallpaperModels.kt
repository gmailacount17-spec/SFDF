package com.example.model

import android.graphics.Color as AndroidColor

enum class ClockFontStyle(val displayName: String) {
    BOLD_MODERN("Modern Bold"),
    SERIF_ELEGANT("Serif Elegant"),
    MONO_TECH("Cyber Monospace"),
    CYBER_DIGITAL("Digital HUD"),
    MINIMAL_SLIM("Minimal Slim")
}

enum class ClockColorPreset(val displayName: String, val colorInt: Int) {
    PURE_WHITE("Pure White", AndroidColor.parseColor("#FFFFFF")),
    ELECTRIC_CYAN("Electric Cyan", AndroidColor.parseColor("#00F5D4")),
    NEON_VIOLET("Neon Violet", AndroidColor.parseColor("#D946EF")),
    SUNSET_GOLD("Sunset Amber", AndroidColor.parseColor("#FBBF24")),
    ROSE_QUARTZ("Rose Quartz", AndroidColor.parseColor("#FB7185")),
    EMERALD_MINT("Mint Neon", AndroidColor.parseColor("#34D399"))
}

enum class ParticleType(val displayName: String) {
    NONE("None"),
    RAIN("Cyber Rain"),
    SNOW("Winter Snow"),
    STARDUST("Cosmic Stardust"),
    SAKURA("Sakura Petals"),
    NEON_ORBS("Neon Bokeh")
}

enum class WatchDesignType(
    val id: String,
    val title: String,
    val description: String
) {
    BIG_STACKED(
        id = "big_stacked",
        title = "Stacked Depth",
        description = "Large two-tier typography behind subject with dramatic depth overlap"
    ),
    CHRONO_ANALOG(
        id = "chrono_analog",
        title = "Precision Chronograph",
        description = "Luxury analog dial with minute markers, sub-dial & metallic hands"
    ),
    CYBER_HUD(
        id = "cyber_hud",
        title = "Cyber HUD Gauge",
        description = "Futuristic circular arc gauge, rotating seconds ring & glowing core"
    ),
    MINIMAL_HORIZONTAL(
        id = "minimal_horizontal",
        title = "Minimal Horizon",
        description = "Clean modern linear typography with inline date header"
    ),
    RETRO_FLIP(
        id = "retro_flip",
        title = "Vintage Flip Card",
        description = "Split-flap mechanical flip board cards with subtle 3D lighting"
    )
}

data class ClockConfig(
    val enabled: Boolean = true,
    val depthBehindSubject: Boolean = true, // The signature 3D depth effect!
    val watchDesign: WatchDesignType = WatchDesignType.BIG_STACKED,
    val fontStyle: ClockFontStyle = ClockFontStyle.BOLD_MODERN,
    val colorInt: Int = AndroidColor.WHITE,
    val is24Hour: Boolean = false,
    val showSeconds: Boolean = false,
    val showDate: Boolean = true,
    val sizeScale: Float = 1.0f, // 0.7f to 1.3f
    val verticalOffsetRatio: Float = 0.22f // 0.0f = top, 0.5f = center
)

enum class ThemePreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val defaultParticles: ParticleType
) {
    CYBERPUNK(
        id = "cyberpunk",
        title = "Neon 2099",
        subtitle = "Cyberpunk warrior atop megacity spire",
        defaultParticles = ParticleType.RAIN
    ),
    COSMIC(
        id = "cosmic",
        title = "Cosmic Odyssey",
        subtitle = "Astronaut drifting through deep nebula",
        defaultParticles = ParticleType.STARDUST
    ),
    ALPINE(
        id = "alpine",
        title = "Alpine Twilight",
        subtitle = "Solitary stag on misty mountain ridge",
        defaultParticles = ParticleType.SNOW
    ),
    SYNTHWAVE(
        id = "synthwave",
        title = "Outrun Horizon",
        subtitle = "Retro supercar chasing wireframe sunset",
        defaultParticles = ParticleType.NEON_ORBS
    ),
    FANTASY(
        id = "fantasy",
        title = "Floating Citadel",
        subtitle = "Ancient floating island castle with waterfalls",
        defaultParticles = ParticleType.SAKURA
    ),
    CUSTOM(
        id = "custom",
        title = "Custom Gallery Photo",
        subtitle = "Your uploaded photo with 3D parallax, watch & effects",
        defaultParticles = ParticleType.STARDUST
    )
}

data class WallpaperSettings(
    val themePreset: ThemePreset = ThemePreset.CYBERPUNK,
    val tiltSensitivity: Float = 1.2f, // 0.2f to 2.5f
    val touchTiltEnabled: Boolean = true,
    val invertTilt: Boolean = false,
    val clock: ClockConfig = ClockConfig(),
    val particleType: ParticleType = ParticleType.RAIN,
    val particleDensity: Float = 1.0f, // 0.3f to 2.0f
    val particleSpeed: Float = 1.0f,
    val aodOutlineClock: Boolean = true,
    val batterySaverEnabled: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)
