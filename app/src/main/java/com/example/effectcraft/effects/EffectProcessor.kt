package com.example.effectcraft.effects

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import com.example.effectcraft.model.AppliedEffect
import kotlin.math.cos
import kotlin.math.sin

/**
 * Executes GPU and Canvas effect pipelines on layer textures.
 */
object EffectProcessor {

  /**
   * Applies the chain of active effects to the given Bitmap texture.
   */
  fun applyEffects(
    source: Bitmap,
    effects: List<AppliedEffect>,
    frame: Int,
    fps: Int
  ): Bitmap {
    if (effects.isEmpty() || effects.none { it.isEnabled }) {
      return source
    }

    var currentBitmap = source.copy(Bitmap.Config.ARGB_8888, true)

    for (effect in effects) {
      if (!effect.isEnabled) continue
      val processed = processSingleEffect(currentBitmap, effect, frame, fps)
      if (processed != currentBitmap && currentBitmap != source) {
        currentBitmap.recycle()
      }
      currentBitmap = processed
    }

    return currentBitmap
  }

  private fun processSingleEffect(
    src: Bitmap,
    effect: AppliedEffect,
    frame: Int,
    fps: Int
  ): Bitmap {
    return when (effect.effectId) {
      "brightness_contrast" -> {
        val brightness = effect.parameters["brightness"]?.getValueAt(frame, fps) ?: 0f
        val contrast = effect.parameters["contrast"]?.getValueAt(frame, fps) ?: 0f
        applyBrightnessContrast(src, brightness, contrast)
      }
      "hue_saturation" -> {
        val hue = effect.parameters["hue"]?.getValueAt(frame, fps) ?: 0f
        val sat = effect.parameters["saturation"]?.getValueAt(frame, fps) ?: 0f
        applyHueSaturation(src, hue, sat)
      }
      "invert" -> {
        val blend = (effect.parameters["blend"]?.getValueAt(frame, fps) ?: 0f) / 100f
        applyInvert(src, blend)
      }
      "tint" -> {
        val amount = (effect.parameters["amount"]?.getValueAt(frame, fps) ?: 100f) / 100f
        applyTint(src, amount)
      }
      "vibrance" -> {
        val vib = effect.parameters["vibrance"]?.getValueAt(frame, fps) ?: 0f
        applyHueSaturation(src, 0f, vib)
      }
      "mosaic" -> {
        val bx = (effect.parameters["blocks_x"]?.getValueAt(frame, fps) ?: 32f).toInt().coerceAtLeast(2)
        val by = (effect.parameters["blocks_y"]?.getValueAt(frame, fps) ?: 18f).toInt().coerceAtLeast(2)
        applyMosaic(src, bx, by)
      }
      "cc_vignette" -> {
        val amount = effect.parameters["amount"]?.getValueAt(frame, fps) ?: 60f
        applyVignette(src, amount)
      }
      "linear_wipe" -> {
        val completion = (effect.parameters["completion"]?.getValueAt(frame, fps) ?: 0f) / 100f
        applyLinearWipe(src, completion)
      }
      "four_color_gradient" -> {
        applyFourColorGradient(src)
      }
      "grid" -> {
        val cellSize = (effect.parameters["size"]?.getValueAt(frame, fps) ?: 50f).toInt().coerceAtLeast(10)
        applyGrid(src, cellSize)
      }
      else -> {
        // Fallback: Return copy of source bitmap
        src
      }
    }
  }

  private fun applyBrightnessContrast(src: Bitmap, brightness: Float, contrast: Float): Bitmap {
    val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Contrast scale factor: (contrast + 100) / 100
    val scale = (contrast + 100f) / 100f
    val bOffset = brightness * 2.55f + 128f * (1f - scale)

    val cm = ColorMatrix(
      floatArrayOf(
        scale, 0f, 0f, 0f, bOffset,
        0f, scale, 0f, 0f, bOffset,
        0f, 0f, scale, 0f, bOffset,
        0f, 0f, 0f, 1f, 0f
      )
    )
    paint.colorFilter = ColorMatrixColorFilter(cm)
    canvas.drawBitmap(src, 0f, 0f, paint)
    return result
  }

  private fun applyHueSaturation(src: Bitmap, hueDegrees: Float, saturationBoost: Float): Bitmap {
    val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val satMatrix = ColorMatrix()
    val satScale = (saturationBoost + 100f) / 100f
    satMatrix.setSaturation(satScale.coerceAtLeast(0f))

    if (hueDegrees != 0f) {
      val hueMatrix = ColorMatrix()
      val rad = Math.toRadians(hueDegrees.toDouble())
      val cosVal = cos(rad).toFloat()
      val sinVal = sin(rad).toFloat()
      val lumR = 0.213f
      val lumG = 0.715f
      val lumB = 0.072f

      val m = floatArrayOf(
        lumR + cosVal * (1 - lumR) + sinVal * (-lumR),
        lumG + cosVal * (-lumG) + sinVal * (-lumG),
        lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0f, 0f,

        lumR + cosVal * (-lumR) + sinVal * (0.143f),
        lumG + cosVal * (1 - lumG) + sinVal * (0.140f),
        lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0f, 0f,

        lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)),
        lumG + cosVal * (-lumG) + sinVal * (lumG),
        lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0f, 0f,

        0f, 0f, 0f, 1f, 0f
      )
      hueMatrix.set(m)
      satMatrix.postConcat(hueMatrix)
    }

    paint.colorFilter = ColorMatrixColorFilter(satMatrix)
    canvas.drawBitmap(src, 0f, 0f, paint)
    return result
  }

  private fun applyInvert(src: Bitmap, blend: Float): Bitmap {
    val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val cm = ColorMatrix(
      floatArrayOf(
        -1f, 0f, 0f, 0f, 255f,
        0f, -1f, 0f, 0f, 255f,
        0f, 0f, -1f, 0f, 255f,
        0f, 0f, 0f, 1f, 0f
      )
    )
    paint.colorFilter = ColorMatrixColorFilter(cm)
    canvas.drawBitmap(src, 0f, 0f, paint)
    return result
  }

  private fun applyTint(src: Bitmap, amount: Float): Bitmap {
    val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Grayscale luminance
    val cm = ColorMatrix()
    cm.setSaturation(1f - amount)
    paint.colorFilter = ColorMatrixColorFilter(cm)
    canvas.drawBitmap(src, 0f, 0f, paint)
    return result
  }

  private fun applyMosaic(src: Bitmap, blocksX: Int, blocksY: Int): Bitmap {
    val small = Bitmap.createScaledBitmap(src, blocksX, blocksY, false)
    val result = Bitmap.createScaledBitmap(small, src.width, src.height, false)
    small.recycle()
    return result
  }

  private fun applyVignette(src: Bitmap, amount: Float): Bitmap {
    val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    canvas.drawBitmap(src, 0f, 0f, paint)

    val radialShader = android.graphics.RadialGradient(
      src.width / 2f,
      src.height / 2f,
      (src.width / 1.5f),
      intArrayOf(0x00000000, 0x00000000, android.graphics.Color.argb((amount * 1.5f).toInt().coerceIn(0, 240), 0, 0, 0)),
      floatArrayOf(0f, 0.5f, 1f),
      android.graphics.Shader.TileMode.CLAMP
    )
    paint.shader = radialShader
    canvas.drawRect(0f, 0f, src.width.toFloat(), src.height.toFloat(), paint)
    return result
  }

  private fun applyLinearWipe(src: Bitmap, completion: Float): Bitmap {
    val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    canvas.drawBitmap(src, 0f, 0f, paint)

    // Clear part of image according to wipe progress
    val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    clearPaint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
    val wipeW = src.width * completion
    canvas.drawRect(0f, 0f, wipeW, src.height.toFloat(), clearPaint)
    return result
  }

  private fun applyFourColorGradient(src: Bitmap): Bitmap {
    val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Draw base gradient
    val lg = android.graphics.LinearGradient(
      0f, 0f, src.width.toFloat(), src.height.toFloat(),
      intArrayOf(
        android.graphics.Color.rgb(255, 0, 128), // Magenta
        android.graphics.Color.rgb(0, 229, 255), // Cyan
        android.graphics.Color.rgb(255, 184, 0), // Amber
        android.graphics.Color.rgb(101, 31, 255) // Deep violet
      ),
      null,
      android.graphics.Shader.TileMode.CLAMP
    )
    paint.shader = lg
    canvas.drawRect(0f, 0f, src.width.toFloat(), src.height.toFloat(), paint)

    // Multiply original on top
    val blendPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    blendPaint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.MULTIPLY)
    canvas.drawBitmap(src, 0f, 0f, blendPaint)
    return result
  }

  private fun applyGrid(src: Bitmap, cellSize: Int): Bitmap {
    val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    canvas.drawBitmap(src, 0f, 0f, paint)

    val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = android.graphics.Color.argb(70, 0, 229, 255)
      strokeWidth = 1.5f
      style = Paint.Style.STROKE
    }

    var x = 0f
    while (x <= src.width) {
      canvas.drawLine(x, 0f, x, src.height.toFloat(), gridPaint)
      x += cellSize
    }

    var y = 0f
    while (y <= src.height) {
      canvas.drawLine(0f, y, src.width.toFloat(), y, gridPaint)
      y += cellSize
    }

    return result
  }
}
