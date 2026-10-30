package com.example.effectcraft.storage

import androidx.compose.ui.graphics.Color
import com.example.effectcraft.effects.EffectCatalog
import com.example.effectcraft.model.AnimatableProperty
import com.example.effectcraft.model.AppliedEffect
import com.example.effectcraft.model.Composition
import com.example.effectcraft.model.InterpolationMode
import com.example.effectcraft.model.Keyframe
import com.example.effectcraft.model.Layer
import com.example.effectcraft.model.LayerBlendMode
import com.example.effectcraft.model.LayerTransform
import com.example.effectcraft.model.LayerType
import com.example.effectcraft.model.Project
import com.example.effectcraft.model.ShapeLayerData
import com.example.effectcraft.model.ShapeType
import com.example.effectcraft.model.TextLayerData
import java.util.UUID

object ProjectTemplates {

  /**
   * Template 1: Kinetic Neon Title with Wiggle Expression and Glow
   */
  fun createNeonKineticTitle(): Project {
    val compId = "comp_neon"
    val backgroundLayer = Layer(
      id = "layer_bg",
      name = "Dark Void Solid",
      type = LayerType.SOLID,
      solidColor = Color(0xFF09090D),
      inFrame = 0,
      outFrame = 300,
      transform = LayerTransform(
        positionX = AnimatableProperty("Position X", 960f),
        positionY = AnimatableProperty("Position Y", 540f)
      )
    )

    val gridLayer = Layer(
      id = "layer_grid",
      name = "Cyber Grid",
      type = LayerType.SOLID,
      solidColor = Color(0xFF141420),
      blendMode = LayerBlendMode.SCREEN,
      inFrame = 0,
      outFrame = 300,
      effects = listOf(
        EffectCatalog.getSpec("grid")!!.instantiate().copy(
          parameters = mapOf(
            "border" to AnimatableProperty("Line Width", 1.5f),
            "size" to AnimatableProperty("Cell Size", 60f)
          )
        )
      )
    )

    val textLayer = Layer(
      id = "layer_text",
      name = "EFFECTCRAFT VFX",
      type = LayerType.TEXT,
      textData = TextLayerData(
        text = "EFFECTCRAFT",
        fontSize = 92f,
        color = Color(0xFF00E5FF),
        tracking = 6f
      ),
      inFrame = 0,
      outFrame = 300,
      transform = LayerTransform(
        positionX = AnimatableProperty(
          name = "Position X",
          baseValue = 960f,
          isKeyframing = true,
          keyframes = listOf(
            Keyframe(frame = 0, value = 960f, interpolation = InterpolationMode.EASE_OUT),
            Keyframe(frame = 40, value = 960f, interpolation = InterpolationMode.BEZIER)
          )
        ),
        positionY = AnimatableProperty(
          name = "Position Y",
          baseValue = 520f,
          isKeyframing = true,
          keyframes = listOf(
            Keyframe(frame = 0, value = 620f, interpolation = InterpolationMode.EASE_OUT),
            Keyframe(frame = 35, value = 520f, interpolation = InterpolationMode.BEZIER)
          ),
          expression = "wiggle(4, 8)"
        ),
        scaleX = AnimatableProperty(
          name = "Scale X",
          baseValue = 100f,
          isKeyframing = true,
          keyframes = listOf(
            Keyframe(frame = 0, value = 40f, interpolation = InterpolationMode.EASE_OUT),
            Keyframe(frame = 35, value = 100f, interpolation = InterpolationMode.EASE_IN_OUT)
          )
        ),
        scaleY = AnimatableProperty(
          name = "Scale Y",
          baseValue = 100f,
          isKeyframing = true,
          keyframes = listOf(
            Keyframe(frame = 0, value = 40f, interpolation = InterpolationMode.EASE_OUT),
            Keyframe(frame = 35, value = 100f, interpolation = InterpolationMode.EASE_IN_OUT)
          )
        ),
        opacity = AnimatableProperty(
          name = "Opacity",
          baseValue = 100f,
          isKeyframing = true,
          keyframes = listOf(
            Keyframe(frame = 0, value = 0f, interpolation = InterpolationMode.EASE_OUT),
            Keyframe(frame = 20, value = 100f, interpolation = InterpolationMode.LINEAR)
          )
        )
      ),
      effects = listOf(
        EffectCatalog.getSpec("glow")!!.instantiate().copy(
          parameters = mapOf(
            "glow_threshold" to AnimatableProperty("Glow Threshold", 40f),
            "glow_radius" to AnimatableProperty("Glow Radius", 50f),
            "glow_intensity" to AnimatableProperty("Glow Intensity", 2.2f)
          )
        ),
        EffectCatalog.getSpec("drop_shadow")!!.instantiate().copy(
          parameters = mapOf(
            "shadow_opacity" to AnimatableProperty("Opacity", 80f),
            "direction" to AnimatableProperty("Direction", 135f),
            "distance" to AnimatableProperty("Distance", 18f),
            "softness" to AnimatableProperty("Softness", 25f)
          )
        )
      )
    )

    val subtitleLayer = Layer(
      id = "layer_sub",
      name = "Motion Graphics Studio",
      type = LayerType.TEXT,
      textData = TextLayerData(
        text = "PRO MOTION GRAPHICS & VFX • ANDROID 16",
        fontSize = 28f,
        color = Color(0xFFFFB800),
        tracking = 8f
      ),
      inFrame = 15,
      outFrame = 300,
      transform = LayerTransform(
        positionX = AnimatableProperty("Position X", 960f),
        positionY = AnimatableProperty(
          name = "Position Y",
          baseValue = 620f,
          isKeyframing = true,
          keyframes = listOf(
            Keyframe(frame = 15, value = 660f, interpolation = InterpolationMode.EASE_OUT),
            Keyframe(frame = 45, value = 620f, interpolation = InterpolationMode.BEZIER)
          )
        ),
        opacity = AnimatableProperty(
          name = "Opacity",
          baseValue = 100f,
          isKeyframing = true,
          keyframes = listOf(
            Keyframe(frame = 15, value = 0f),
            Keyframe(frame = 40, value = 100f)
          )
        )
      )
    )

    val comp = Composition(
      id = compId,
      name = "Neon Kinetic Title",
      width = 1920,
      height = 1080,
      frameRate = 30,
      durationFrames = 300,
      layers = listOf(textLayer, subtitleLayer, gridLayer, backgroundLayer),
      selectedLayerId = "layer_text"
    )

    return Project(
      name = "Neon Kinetic Title",
      compositions = listOf(comp),
      activeCompositionId = compId
    )
  }

  /**
   * Template 2: Broadcast Lower Third with Linear Wipe and Shapes
   */
  fun createBroadcastLowerThird(): Project {
    val compId = "comp_lower_third"

    val shapeBar = Layer(
      id = "layer_bar",
      name = "Accent Lower Bar",
      type = LayerType.SHAPE,
      shapeData = ShapeLayerData(
        shapeType = ShapeType.ROUNDED_RECT,
        fillColor = Color(0xFF00E5FF),
        hasFill = true,
        strokeWidth = 0f,
        hasStroke = false,
        cornerRadius = 8f,
        width = 680f,
        height = 80f
      ),
      inFrame = 10,
      outFrame = 300,
      transform = LayerTransform(
        positionX = AnimatableProperty(
          name = "Position X",
          baseValue = 540f,
          isKeyframing = true,
          keyframes = listOf(
            Keyframe(10, -300f, InterpolationMode.EASE_OUT),
            Keyframe(40, 540f, InterpolationMode.EASE_IN_OUT)
          )
        ),
        positionY = AnimatableProperty("Position Y", 860f)
      )
    )

    val textName = Layer(
      id = "layer_name",
      name = "Presenter Name",
      type = LayerType.TEXT,
      textData = TextLayerData(
        text = "SARAH CONNOR",
        fontSize = 44f,
        color = Color(0xFF121217),
        isBold = true
      ),
      inFrame = 20,
      outFrame = 300,
      transform = LayerTransform(
        positionX = AnimatableProperty("Position X", 520f),
        positionY = AnimatableProperty("Position Y", 860f),
        opacity = AnimatableProperty(
          name = "Opacity",
          baseValue = 100f,
          isKeyframing = true,
          keyframes = listOf(
            Keyframe(20, 0f),
            Keyframe(35, 100f)
          )
        )
      )
    )

    val comp = Composition(
      id = compId,
      name = "Broadcast Lower Third",
      width = 1920,
      height = 1080,
      frameRate = 30,
      durationFrames = 240,
      layers = listOf(textName, shapeBar),
      selectedLayerId = "layer_name"
    )

    return Project(
      name = "Broadcast Lower Third",
      compositions = listOf(comp),
      activeCompositionId = compId
    )
  }

  /**
   * Template 3: Cyberpunk Particle Glow
   */
  fun createCyberpunkGlow(): Project {
    val compId = "comp_cyberpunk"

    val shapeCenter = Layer(
      id = "layer_ring",
      name = "Neon Ring Center",
      type = LayerType.SHAPE,
      shapeData = ShapeLayerData(
        shapeType = ShapeType.ELLIPSE,
        fillColor = Color(0x33F50057),
        hasFill = true,
        strokeColor = Color(0xFFF50057),
        strokeWidth = 6f,
        hasStroke = true,
        width = 450f,
        height = 450f
      ),
      inFrame = 0,
      outFrame = 300,
      transform = LayerTransform(
        positionX = AnimatableProperty("Position X", 960f),
        positionY = AnimatableProperty("Position Y", 540f),
        rotation = AnimatableProperty(
          name = "Rotation",
          baseValue = 0f,
          isKeyframing = true,
          keyframes = listOf(
            Keyframe(0, 0f),
            Keyframe(300, 360f)
          )
        )
      ),
      effects = listOf(
        EffectCatalog.getSpec("glow")!!.instantiate()
      )
    )

    val comp = Composition(
      id = compId,
      name = "Cyberpunk Portal",
      width = 1920,
      height = 1080,
      frameRate = 30,
      durationFrames = 300,
      layers = listOf(shapeCenter),
      selectedLayerId = "layer_ring"
    )

    return Project(
      name = "Cyberpunk Portal",
      compositions = listOf(comp),
      activeCompositionId = compId
    )
  }
}
