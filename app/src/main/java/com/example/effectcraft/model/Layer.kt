package com.example.effectcraft.model

import androidx.compose.ui.graphics.Color

/**
 * 2D / 3D Transform properties of an EffectCraft layer.
 */
data class LayerTransform(
  val positionX: AnimatableProperty = AnimatableProperty("Position X", 960f, minValue = -2000f, maxValue = 4000f),
  val positionY: AnimatableProperty = AnimatableProperty("Position Y", 540f, minValue = -2000f, maxValue = 4000f),
  val positionZ: AnimatableProperty = AnimatableProperty("Position Z", 0f, minValue = -5000f, maxValue = 5000f),
  val scaleX: AnimatableProperty = AnimatableProperty("Scale X", 100f, minValue = 0f, maxValue = 500f),
  val scaleY: AnimatableProperty = AnimatableProperty("Scale Y", 100f, minValue = 0f, maxValue = 500f),
  val rotation: AnimatableProperty = AnimatableProperty("Rotation", 0f, minValue = -3600f, maxValue = 3600f),
  val rotationX: AnimatableProperty = AnimatableProperty("Rotation X", 0f, minValue = -3600f, maxValue = 3600f),
  val rotationY: AnimatableProperty = AnimatableProperty("Rotation Y", 0f, minValue = -3600f, maxValue = 3600f),
  val opacity: AnimatableProperty = AnimatableProperty("Opacity", 100f, minValue = 0f, maxValue = 100f),
  val anchorX: AnimatableProperty = AnimatableProperty("Anchor X", 0f, minValue = -1000f, maxValue = 2000f),
  val anchorY: AnimatableProperty = AnimatableProperty("Anchor Y", 0f, minValue = -1000f, maxValue = 2000f)
)

enum class MaskMode(val displayName: String) {
  ADD("Add"),
  SUBTRACT("Subtract"),
  INTERSECT("Intersect"),
  DIFFERENCE("Difference"),
  NONE("None")
}

data class LayerMask(
  val id: String,
  val name: String,
  val mode: MaskMode = MaskMode.ADD,
  val inverted: Boolean = false,
  val opacity: AnimatableProperty = AnimatableProperty("Mask Opacity", 100f, minValue = 0f, maxValue = 100f),
  val feather: AnimatableProperty = AnimatableProperty("Mask Feather", 0f, minValue = 0f, maxValue = 200f),
  val expansion: AnimatableProperty = AnimatableProperty("Mask Expansion", 0f, minValue = -200f, maxValue = 200f),
  // Normalized 0..1 coordinates for polygon / path or rectangle
  val points: List<Pair<Float, Float>> = listOf(0.1f to 0.1f, 0.9f to 0.1f, 0.9f to 0.9f, 0.1f to 0.9f)
)

enum class ShapeType {
  RECTANGLE,
  ELLIPSE,
  STAR,
  POLYGON,
  ROUNDED_RECT
}

data class ShapeLayerData(
  val shapeType: ShapeType = ShapeType.ROUNDED_RECT,
  val fillColor: Color = Color(0xFF00E5FF),
  val hasFill: Boolean = true,
  val strokeColor: Color = Color(0xFFFFFFFF),
  val strokeWidth: Float = 4f,
  val hasStroke: Boolean = true,
  val cornerRadius: Float = 24f,
  val starPoints: Int = 5,
  val width: Float = 400f,
  val height: Float = 260f
)

data class TextLayerData(
  val text: String = "EffectCraft Motion",
  val fontSize: Float = 64f,
  val color: Color = Color(0xFFFFFFFF),
  val tracking: Float = 2f,
  val isBold: Boolean = true,
  val isItalic: Boolean = false
)

data class CameraLayerData(
  val zoom: Float = 1000f,
  val depthOfField: Boolean = true,
  val focusDistance: Float = 1000f,
  val aperture: Float = 50f,
  val blurLevel: Float = 100f
)

data class LightLayerData(
  val lightType: String = "Point", // Point, Ambient, Spot, Parallel
  val intensity: Float = 100f,
  val color: Color = Color(0xFFFFFFFF),
  val coneAngle: Float = 45f,
  val coneFeather: Float = 50f
)

/**
 * An applied visual effect instance on a layer.
 */
data class AppliedEffect(
  val id: String,
  val effectId: String,
  val displayName: String,
  val isEnabled: Boolean = true,
  val parameters: Map<String, AnimatableProperty> = emptyMap()
)

/**
 * Core Layer entity matching EffectCraft engine structure.
 */
data class Layer(
  val id: String,
  val name: String,
  val type: LayerType,
  val inFrame: Int = 0,
  val outFrame: Int = 300,
  val startFrame: Int = 0,
  val isVisible: Boolean = true,
  val isLocked: Boolean = false,
  val isSolo: Boolean = false,
  val is3D: Boolean = false,
  val isShy: Boolean = false,
  val blendMode: LayerBlendMode = LayerBlendMode.NORMAL,
  val trackMatte: TrackMatteType = TrackMatteType.NONE,
  val parentLayerId: String? = null,
  val transform: LayerTransform = LayerTransform(),
  val solidColor: Color = Color(0xFF1E88E5),
  val solidWidth: Int = 1920,
  val solidHeight: Int = 1080,
  val textData: TextLayerData? = null,
  val shapeData: ShapeLayerData? = null,
  val cameraData: CameraLayerData? = null,
  val lightData: LightLayerData? = null,
  val footageUri: String? = null,
  val masks: List<LayerMask> = emptyList(),
  val effects: List<AppliedEffect> = emptyList()
) {
  fun isVisibleAt(frame: Int): Boolean {
    return isVisible && frame in inFrame..outFrame
  }
}
