package com.example.effectcraft.effects

import com.example.effectcraft.model.AnimatableProperty
import com.example.effectcraft.model.AppliedEffect
import java.util.UUID

enum class EffectCategory(val displayName: String) {
  BLUR_SHARPEN("Blur & Sharpen"),
  COLOR_CORRECTION("Color Correction"),
  DISTORT("Distort"),
  GENERATE("Generate"),
  STYLIZE("Stylize"),
  TRANSITION("Transition"),
  KEYING("Keying"),
  NOISE_GRAIN("Noise & Grain"),
  TIME("Time")
}

data class EffectParamDef(
  val key: String,
  val name: String,
  val defaultValue: Float,
  val min: Float,
  val max: Float,
  val unit: String = ""
)

data class EffectSpec(
  val id: String,
  val name: String,
  val category: EffectCategory,
  val description: String,
  val parameters: List<EffectParamDef>,
  val isGpuAccelerated: Boolean = true
) {
  fun instantiate(): AppliedEffect {
    val paramsMap = parameters.associate { p ->
      p.key to AnimatableProperty(
        name = p.name,
        baseValue = p.defaultValue,
        minValue = p.min,
        maxValue = p.max
      )
    }
    return AppliedEffect(
      id = UUID.randomUUID().toString(),
      effectId = id,
      displayName = name,
      isEnabled = true,
      parameters = paramsMap
    )
  }
}

/**
 * Full EffectCraft effects registry matching desktop crates/effects.
 */
object EffectCatalog {
  val allSpecs: List<EffectSpec> = listOf(
    // 1. BLUR & SHARPEN
    EffectSpec(
      id = "gaussian_blur",
      name = "Gaussian Blur",
      category = EffectCategory.BLUR_SHARPEN,
      description = "Smooths the image using a 2D Gaussian distribution filter.",
      parameters = listOf(
        EffectParamDef("blurriness", "Blurriness", 25f, 0f, 250f, "px"),
        EffectParamDef("repeat_edges", "Repeat Edge Pixels", 1f, 0f, 1f)
      )
    ),
    EffectSpec(
      id = "fast_blur",
      name = "Fast Blur",
      category = EffectCategory.BLUR_SHARPEN,
      description = "High-performance multi-pass box approximation of Gaussian blur.",
      parameters = listOf(
        EffectParamDef("blurriness", "Blurriness", 20f, 0f, 200f, "px"),
        EffectParamDef("iterations", "Iterations", 3f, 1f, 10f)
      )
    ),
    EffectSpec(
      id = "directional_blur",
      name = "Directional Blur",
      category = EffectCategory.BLUR_SHARPEN,
      description = "Blurs in a specified angle direction to simulate camera motion.",
      parameters = listOf(
        EffectParamDef("direction", "Direction", 45f, 0f, 360f, "°"),
        EffectParamDef("blur_length", "Blur Length", 30f, 0f, 200f, "px")
      )
    ),
    EffectSpec(
      id = "radial_blur",
      name = "Radial / Zoom Blur",
      category = EffectCategory.BLUR_SHARPEN,
      description = "Simulates circular spin or dramatic zoom blur outward from center.",
      parameters = listOf(
        EffectParamDef("amount", "Amount", 20f, 0f, 100f),
        EffectParamDef("center_x", "Center X", 50f, 0f, 100f, "%"),
        EffectParamDef("center_y", "Center Y", 50f, 0f, 100f, "%")
      )
    ),
    EffectSpec(
      id = "unsharp_mask",
      name = "Unsharp Mask",
      category = EffectCategory.BLUR_SHARPEN,
      description = "Sharpens edges and fine textures by subtracting a blurred version.",
      parameters = listOf(
        EffectParamDef("amount", "Amount", 80f, 0f, 500f, "%"),
        EffectParamDef("radius", "Radius", 2f, 0.1f, 50f, "px"),
        EffectParamDef("threshold", "Threshold", 0f, 0f, 255f)
      )
    ),

    // 2. COLOR CORRECTION
    EffectSpec(
      id = "brightness_contrast",
      name = "Brightness & Contrast",
      category = EffectCategory.COLOR_CORRECTION,
      description = "Adjusts overall luminance and dynamic tonal range.",
      parameters = listOf(
        EffectParamDef("brightness", "Brightness", 0f, -100f, 100f),
        EffectParamDef("contrast", "Contrast", 15f, -100f, 100f)
      )
    ),
    EffectSpec(
      id = "hue_saturation",
      name = "Hue / Saturation",
      category = EffectCategory.COLOR_CORRECTION,
      description = "Rotates color hue around wheel and scales saturation and lightness.",
      parameters = listOf(
        EffectParamDef("hue", "Master Hue", 0f, -180f, 180f, "°"),
        EffectParamDef("saturation", "Master Saturation", 20f, -100f, 100f),
        EffectParamDef("lightness", "Master Lightness", 0f, -100f, 100f)
      )
    ),
    EffectSpec(
      id = "levels",
      name = "Levels",
      category = EffectCategory.COLOR_CORRECTION,
      description = "Re-maps histogram black, gamma, and white points.",
      parameters = listOf(
        EffectParamDef("input_black", "Input Black", 10f, 0f, 255f),
        EffectParamDef("input_white", "Input White", 240f, 0f, 255f),
        EffectParamDef("gamma", "Gamma", 1.1f, 0.1f, 5f)
      )
    ),
    EffectSpec(
      id = "tint",
      name = "Tint",
      category = EffectCategory.COLOR_CORRECTION,
      description = "Maps black and white luminance values to custom two-tone colors.",
      parameters = listOf(
        EffectParamDef("amount", "Amount to Tint", 100f, 0f, 100f, "%")
      )
    ),
    EffectSpec(
      id = "tritone",
      name = "Tritone",
      category = EffectCategory.COLOR_CORRECTION,
      description = "Maps highlights, midtones, and shadows to 3 distinct colors.",
      parameters = listOf(
        EffectParamDef("blend_with_original", "Blend with Original", 0f, 0f, 100f, "%")
      )
    ),
    EffectSpec(
      id = "exposure",
      name = "Exposure",
      category = EffectCategory.COLOR_CORRECTION,
      description = "Photographic f-stop exposure control with offset and gamma correction.",
      parameters = listOf(
        EffectParamDef("exposure", "Exposure (Stops)", 0.5f, -5f, 5f, "ev"),
        EffectParamDef("offset", "Offset", 0f, -0.5f, 0.5f),
        EffectParamDef("gamma_correction", "Gamma Correction", 1f, 0.2f, 5f)
      )
    ),
    EffectSpec(
      id = "vibrance",
      name = "Vibrance",
      category = EffectCategory.COLOR_CORRECTION,
      description = "Boosts under-saturated colors while protecting skin tones.",
      parameters = listOf(
        EffectParamDef("vibrance", "Vibrance", 35f, -100f, 100f),
        EffectParamDef("saturation", "Saturation", 10f, -100f, 100f)
      )
    ),
    EffectSpec(
      id = "invert",
      name = "Invert",
      category = EffectCategory.COLOR_CORRECTION,
      description = "Inverts color channels (RGB, Red, Green, Blue, or Alpha).",
      parameters = listOf(
        EffectParamDef("blend", "Blend with Original", 0f, 0f, 100f, "%")
      )
    ),
    EffectSpec(
      id = "cc_vignette",
      name = "CC Vignette",
      category = EffectCategory.COLOR_CORRECTION,
      description = "Darkens or tints the outer perimeter to focus attention toward center.",
      parameters = listOf(
        EffectParamDef("amount", "Amount", 60f, 0f, 200f),
        EffectParamDef("radius", "Radius", 45f, 0f, 100f, "%"),
        EffectParamDef("feather", "Feather", 80f, 0f, 100f, "%")
      )
    ),

    // 3. DISTORT
    EffectSpec(
      id = "transform_effect",
      name = "Transform",
      category = EffectCategory.DISTORT,
      description = "Applies 2D affine transforms (anchor, position, scale, skew, opacity).",
      parameters = listOf(
        EffectParamDef("scale", "Scale", 110f, 0f, 400f, "%"),
        EffectParamDef("rotation", "Rotation", 15f, -360f, 360f, "°"),
        EffectParamDef("skew", "Skew", 0f, -85f, 85f, "°"),
        EffectParamDef("opacity", "Opacity", 100f, 0f, 100f, "%")
      )
    ),
    EffectSpec(
      id = "twirl",
      name = "Twirl",
      category = EffectCategory.DISTORT,
      description = "Rotates pixels in a spiral vortex around a center point.",
      parameters = listOf(
        EffectParamDef("angle", "Angle", 90f, -720f, 720f, "°"),
        EffectParamDef("twirl_radius", "Twirl Radius", 35f, 0f, 100f, "%")
      )
    ),
    EffectSpec(
      id = "wave_warp",
      name = "Wave Warp",
      category = EffectCategory.DISTORT,
      description = "Creates animated sine, square, or triangle geometric waves.",
      parameters = listOf(
        EffectParamDef("wave_height", "Wave Height", 20f, 0f, 100f),
        EffectParamDef("wave_width", "Wave Width", 80f, 5f, 400f),
        EffectParamDef("wave_speed", "Wave Speed", 1f, -10f, 10f)
      )
    ),
    EffectSpec(
      id = "spherize",
      name = "Spherize",
      category = EffectCategory.DISTORT,
      description = "Bulges or pinches pixels inside a spherical lens distortion.",
      parameters = listOf(
        EffectParamDef("radius", "Radius", 50f, 0f, 100f, "%"),
        EffectParamDef("strength", "Strength", 40f, -100f, 100f)
      )
    ),

    // 4. GENERATE
    EffectSpec(
      id = "four_color_gradient",
      name = "4-Color Gradient",
      category = EffectCategory.GENERATE,
      description = "Generates a smooth bilinear blend between four corner color points.",
      parameters = listOf(
        EffectParamDef("blend", "Blend Distance", 100f, 0f, 200f),
        EffectParamDef("opacity", "Opacity", 100f, 0f, 100f, "%")
      )
    ),
    EffectSpec(
      id = "gradient_ramp",
      name = "Gradient Ramp",
      category = EffectCategory.GENERATE,
      description = "Produces linear or radial gradient ramps across the layer.",
      parameters = listOf(
        EffectParamDef("ramp_shape", "Ramp Shape (0:Linear, 1:Radial)", 0f, 0f, 1f),
        EffectParamDef("ramp_blend", "Blend with Original", 0f, 0f, 100f, "%")
      )
    ),
    EffectSpec(
      id = "checkerboard",
      name = "Checkerboard",
      category = EffectCategory.GENERATE,
      description = "Generates an alternating two-color grid pattern.",
      parameters = listOf(
        EffectParamDef("size", "Square Size", 40f, 2f, 200f, "px"),
        EffectParamDef("opacity", "Opacity", 80f, 0f, 100f, "%")
      )
    ),
    EffectSpec(
      id = "grid",
      name = "Grid",
      category = EffectCategory.GENERATE,
      description = "Generates customizable matrix lines and wireframe guidelines.",
      parameters = listOf(
        EffectParamDef("border", "Line Width", 2f, 1f, 20f, "px"),
        EffectParamDef("size", "Cell Size", 50f, 10f, 300f, "px")
      )
    ),

    // 5. STYLIZE
    EffectSpec(
      id = "glow",
      name = "Glow",
      category = EffectCategory.STYLIZE,
      description = "Diffuse luminescence emitting from bright pixels with falloff.",
      parameters = listOf(
        EffectParamDef("glow_threshold", "Glow Threshold", 60f, 0f, 100f, "%"),
        EffectParamDef("glow_radius", "Glow Radius", 40f, 0f, 200f, "px"),
        EffectParamDef("glow_intensity", "Glow Intensity", 1.8f, 0.1f, 10f)
      )
    ),
    EffectSpec(
      id = "drop_shadow",
      name = "Drop Shadow",
      category = EffectCategory.STYLIZE,
      description = "Casts a realistic directional shadow behind alpha cutouts.",
      parameters = listOf(
        EffectParamDef("shadow_opacity", "Opacity", 75f, 0f, 100f, "%"),
        EffectParamDef("direction", "Direction", 135f, 0f, 360f, "°"),
        EffectParamDef("distance", "Distance", 15f, 0f, 150f, "px"),
        EffectParamDef("softness", "Softness", 20f, 0f, 100f, "px")
      )
    ),
    EffectSpec(
      id = "mosaic",
      name = "Mosaic",
      category = EffectCategory.STYLIZE,
      description = "Divides the layer into discrete pixel blocks for retro/pixel art.",
      parameters = listOf(
        EffectParamDef("blocks_x", "Horizontal Blocks", 32f, 2f, 200f),
        EffectParamDef("blocks_y", "Vertical Blocks", 18f, 2f, 200f)
      )
    ),
    EffectSpec(
      id = "find_edges",
      name = "Find Edges",
      category = EffectCategory.STYLIZE,
      description = "Highlights high-contrast boundary transitions (Sobel filter).",
      parameters = listOf(
        EffectParamDef("invert", "Invert Edges", 0f, 0f, 1f),
        EffectParamDef("blend", "Blend with Original", 0f, 0f, 100f, "%")
      )
    ),
    EffectSpec(
      id = "posterize",
      name = "Posterize",
      category = EffectCategory.STYLIZE,
      description = "Quantizes color depth to a fixed number of tonal steps.",
      parameters = listOf(
        EffectParamDef("levels", "Tonal Levels", 6f, 2f, 32f)
      )
    ),

    // 6. TRANSITION
    EffectSpec(
      id = "linear_wipe",
      name = "Linear Wipe",
      category = EffectCategory.TRANSITION,
      description = "Reveals or hides layer along a directional straight edge line.",
      parameters = listOf(
        EffectParamDef("completion", "Transition Completion", 35f, 0f, 100f, "%"),
        EffectParamDef("wipe_angle", "Wipe Angle", 90f, 0f, 360f, "°"),
        EffectParamDef("feather", "Feather", 15f, 0f, 100f, "px")
      )
    ),
    EffectSpec(
      id = "radial_wipe",
      name = "Radial Wipe",
      category = EffectCategory.TRANSITION,
      description = "Circular clock-sweep reveal around a central anchor.",
      parameters = listOf(
        EffectParamDef("completion", "Transition Completion", 40f, 0f, 100f, "%"),
        EffectParamDef("start_angle", "Start Angle", 0f, 0f, 360f, "°"),
        EffectParamDef("feather", "Feather", 10f, 0f, 100f, "px")
      )
    ),
    EffectSpec(
      id = "venetian_blinds",
      name = "Venetian Blinds",
      category = EffectCategory.TRANSITION,
      description = "Simulates opening slatted blinds across the layer.",
      parameters = listOf(
        EffectParamDef("completion", "Transition Completion", 25f, 0f, 100f, "%"),
        EffectParamDef("direction", "Direction", 0f, 0f, 360f, "°"),
        EffectParamDef("width", "Width", 20f, 2f, 100f, "px"),
        EffectParamDef("feather", "Feather", 2f, 0f, 20f, "px")
      )
    ),

    // 7. KEYING
    EffectSpec(
      id = "linear_color_key",
      name = "Linear Color Key",
      category = EffectCategory.KEYING,
      description = "Transparentizes greenscreen/bluescreen chroma backgrounds.",
      parameters = listOf(
        EffectParamDef("threshold", "Key Threshold", 40f, 0f, 100f, "%"),
        EffectParamDef("softness", "Key Softness", 10f, 0f, 100f, "%")
      )
    ),
    EffectSpec(
      id = "luma_key",
      name = "Luma Key",
      category = EffectCategory.KEYING,
      description = "Keys out dark or light pixels based on brightness values.",
      parameters = listOf(
        EffectParamDef("threshold", "Threshold", 20f, 0f, 255f),
        EffectParamDef("tolerance", "Tolerance", 15f, 0f, 100f)
      )
    ),

    // 8. NOISE & GRAIN
    EffectSpec(
      id = "add_noise",
      name = "Add Noise",
      category = EffectCategory.NOISE_GRAIN,
      description = "Adds organic film grain or digital sensor noise.",
      parameters = listOf(
        EffectParamDef("amount", "Noise Amount", 15f, 0f, 100f, "%"),
        EffectParamDef("monochromatic", "Monochromatic", 1f, 0f, 1f)
      )
    ),
    EffectSpec(
      id = "fractal_noise",
      name = "Fractal Noise",
      category = EffectCategory.NOISE_GRAIN,
      description = "Generates Perlin fractal turbulence for smoke, clouds, and fire.",
      parameters = listOf(
        EffectParamDef("contrast", "Contrast", 120f, 0f, 300f),
        EffectParamDef("brightness", "Brightness", 0f, -100f, 100f),
        EffectParamDef("complexity", "Complexity", 4f, 1f, 8f),
        EffectParamDef("evolution", "Evolution (Cycle)", 0f, 0f, 360f, "°")
      )
    ),

    // 9. TIME
    EffectSpec(
      id = "posterize_time",
      name = "Posterize Time",
      category = EffectCategory.TIME,
      description = "Locks frame updates to a lower frame rate (e.g. 12fps for anime look).",
      parameters = listOf(
        EffectParamDef("frame_rate", "Target Frame Rate", 12f, 1f, 60f, "fps")
      )
    ),
    EffectSpec(
      id = "echo",
      name = "Echo",
      category = EffectCategory.TIME,
      description = "Combines previous frames with current frame for motion streaks.",
      parameters = listOf(
        EffectParamDef("echo_time", "Echo Time", -0.05f, -1f, 1f, "s"),
        EffectParamDef("number_of_echoes", "Number of Echoes", 3f, 1f, 10f),
        EffectParamDef("decay", "Decay", 0.7f, 0f, 1f)
      )
    )
  )

  fun getSpec(id: String): EffectSpec? = allSpecs.find { it.id == id }
}
