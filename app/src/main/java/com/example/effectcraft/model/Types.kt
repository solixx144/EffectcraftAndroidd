package com.example.effectcraft.model

import androidx.compose.ui.graphics.BlendMode

/**
 * Core enumeration types matching EffectCraft (storytold/effectcraft) & Adobe After Effects specifications.
 */

enum class LayerType(val displayName: String, val defaultColor: Long) {
  SOLID("Solid", 0xFFE53935),
  TEXT("Text", 0xFFE040FB),
  SHAPE("Shape", 0xFF00E5FF),
  FOOTAGE("Footage", 0xFF2979FF),
  CAMERA("Camera 3D", 0xFFFFB800),
  LIGHT("Light 3D", 0xFFFFD600),
  NULL_OBJECT("Null Object", 0xFF78909C),
  ADJUSTMENT_LAYER("Adjustment Layer", 0xFF26A69A),
  AUDIO("Audio", 0xFF00E676)
}

/**
 * 38 Blend Modes implemented in EffectCraft
 */
enum class LayerBlendMode(val displayName: String, val category: String) {
  NORMAL("Normal", "Standard"),
  DISSOLVE("Dissolve", "Standard"),
  // Darken
  DARKEN("Darken", "Darken"),
  MULTIPLY("Multiply", "Darken"),
  COLOR_BURN("Color Burn", "Darken"),
  CLASSIC_COLOR_BURN("Classic Color Burn", "Darken"),
  LINEAR_BURN("Linear Burn", "Darken"),
  DARKER_COLOR("Darker Color", "Darken"),
  // Lighten
  ADD("Add", "Lighten"),
  LIGHTEN("Lighten", "Lighten"),
  SCREEN("Screen", "Lighten"),
  COLOR_DODGE("Color Dodge", "Lighten"),
  CLASSIC_COLOR_DODGE("Classic Color Dodge", "Lighten"),
  LINEAR_DODGE("Linear Dodge", "Lighten"),
  LIGHTER_COLOR("Lighter Color", "Lighten"),
  // Contrast
  OVERLAY("Overlay", "Contrast"),
  SOFT_LIGHT("Soft Light", "Contrast"),
  HARD_LIGHT("Hard Light", "Contrast"),
  VIVID_LIGHT("Vivid Light", "Contrast"),
  LINEAR_LIGHT("Linear Light", "Contrast"),
  PIN_LIGHT("Pin Light", "Contrast"),
  HARD_MIX("Hard Mix", "Contrast"),
  // Inversion / Cancellation
  DIFFERENCE("Difference", "Comparative"),
  EXCLUSION("Exclusion", "Comparative"),
  SUBTRACT("Subtract", "Comparative"),
  DIVIDE("Divide", "Comparative"),
  // Component
  HUE("Hue", "Component"),
  SATURATION("Saturation", "Component"),
  COLOR("Color", "Component"),
  LUMINOSITY("Luminosity", "Component"),
  // Matte / Stencil
  STENCIL_ALPHA("Stencil Alpha", "Matte"),
  STENCIL_LUMA("Stencil Luma", "Matte"),
  SILHOUETTE_ALPHA("Silhouette Alpha", "Matte"),
  SILHOUETTE_LUMA("Silhouette Luma", "Matte"),
  ALPHA_ADD("Alpha Add", "Utility"),
  LUMINESCENT_PREMUL("Luminescent Premul", "Utility");

  fun toComposeBlendMode(): BlendMode = when (this) {
    NORMAL -> BlendMode.SrcOver
    MULTIPLY -> BlendMode.Multiply
    SCREEN -> BlendMode.Screen
    OVERLAY -> BlendMode.Overlay
    DARKEN -> BlendMode.Darken
    LIGHTEN -> BlendMode.Lighten
    COLOR_DODGE -> BlendMode.ColorDodge
    COLOR_BURN -> BlendMode.ColorBurn
    HARD_LIGHT -> BlendMode.Hardlight
    SOFT_LIGHT -> BlendMode.Softlight
    DIFFERENCE -> BlendMode.Difference
    EXCLUSION -> BlendMode.Exclusion
    HUE -> BlendMode.Hue
    SATURATION -> BlendMode.Saturation
    COLOR -> BlendMode.Color
    LUMINOSITY -> BlendMode.Luminosity
    ADD, LINEAR_DODGE -> BlendMode.Plus
    else -> BlendMode.SrcOver
  }
}

enum class TrackMatteType(val displayName: String) {
  NONE("None"),
  ALPHA("Alpha Matte"),
  ALPHA_INVERTED("Alpha Inverted Matte"),
  LUMA("Luma Matte"),
  LUMA_INVERTED("Luma Inverted Matte")
}

enum class InterpolationMode(val displayName: String) {
  LINEAR("Linear"),
  BEZIER("Bezier (Smooth)"),
  HOLD("Hold"),
  EASE_IN("Ease In"),
  EASE_OUT("Ease Out"),
  EASE_IN_OUT("Easy Ease")
}

enum class WorkstationTool(val displayName: String) {
  SELECTION("Selection Tool (V)"),
  HAND("Hand / Pan Tool (H)"),
  ZOOM("Zoom Tool (Z)"),
  ROTATION("Rotation Tool (W)"),
  PAN_ANCHOR("Pan Behind / Anchor (Y)"),
  SHAPE("Shape Tool (Q)"),
  TEXT("Text Tool (Ctrl+T)"),
  PEN("Pen / Mask Tool (G)")
}

data class Timecode(val frames: Int, val frameRate: Int) {
  val totalSeconds: Double get() = frames.toDouble() / frameRate
  val hours: Int get() = (totalSeconds / 3600).toInt()
  val minutes: Int get() = ((totalSeconds % 3600) / 60).toInt()
  val seconds: Int get() = (totalSeconds % 60).toInt()
  val framePart: Int get() = frames % frameRate

  fun formatted(): String {
    return "%02d:%02d:%02d:%02d".format(hours, minutes, seconds, framePart)
  }
}
