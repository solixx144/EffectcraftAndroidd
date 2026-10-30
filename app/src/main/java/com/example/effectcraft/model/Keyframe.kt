package com.example.effectcraft.model

import kotlin.math.PI
import kotlin.math.sin

/**
 * Keyframe for animating numeric or vector values across time.
 */
data class Keyframe(
  val frame: Int,
  val value: Float,
  val interpolation: InterpolationMode = InterpolationMode.LINEAR,
  val inTangentX: Float = 0.33f,
  val inTangentY: Float = 0f,
  val outTangentX: Float = 0.33f,
  val outTangentY: Float = 0f
)

/**
 * Property that supports static value, animated keyframes, and After Effects style expressions.
 */
data class AnimatableProperty(
  val name: String,
  val baseValue: Float,
  val keyframes: List<Keyframe> = emptyList(),
  val expression: String = "",
  val isKeyframing: Boolean = false,
  val minValue: Float = 0f,
  val maxValue: Float = 100f
) {
  fun getValueAt(frame: Int, fps: Int = 30): Float {
    // 1. If expression is defined, evaluate it first
    if (expression.isNotBlank()) {
      val exprVal = evaluateSimpleExpression(expression, frame, fps, baseValue)
      if (exprVal != null) return exprVal
    }

    // 2. If no keyframes or not keyframing, return static baseValue
    if (!isKeyframing || keyframes.isEmpty()) {
      return baseValue
    }

    // 3. Find keyframe boundaries
    val sorted = keyframes.sortedBy { it.frame }
    if (frame <= sorted.first().frame) return sorted.first().value
    if (frame >= sorted.last().frame) return sorted.last().value

    for (i in 0 until sorted.size - 1) {
      val k1 = sorted[i]
      val k2 = sorted[i + 1]
      if (frame in k1.frame..k2.frame) {
        val span = (k2.frame - k1.frame).toFloat()
        if (span == 0f) return k1.value
        val progress = (frame - k1.frame) / span

        return when (k1.interpolation) {
          InterpolationMode.HOLD -> k1.value
          InterpolationMode.LINEAR -> k1.value + (k2.value - k1.value) * progress
          InterpolationMode.EASE_IN -> {
            // Cubic ease-in: t^2
            k1.value + (k2.value - k1.value) * (progress * progress)
          }
          InterpolationMode.EASE_OUT -> {
            // Quadratic ease-out: 1 - (1-t)^2
            val inv = 1f - progress
            k1.value + (k2.value - k1.value) * (1f - inv * inv)
          }
          InterpolationMode.BEZIER, InterpolationMode.EASE_IN_OUT -> {
            // Smoothstep / S-curve: 3t^2 - 2t^3
            val smooth = progress * progress * (3f - 2f * progress)
            k1.value + (k2.value - k1.value) * smooth
          }
        }
      }
    }
    return baseValue
  }

  companion object {
    /**
     * Evaluates standard expressions like:
     * - wiggle(freq, amp)
     * - time * rate
     * - sin(time * freq) * amp
     * - loopOut()
     */
    fun evaluateSimpleExpression(expr: String, frame: Int, fps: Int, base: Float): Float? {
      val clean = expr.trim().lowercase()
      val time = frame.toFloat() / fps.toFloat()

      return try {
        if (clean.startsWith("wiggle(") && clean.endsWith(")")) {
          val inner = clean.removeSurrounding("wiggle(", ")")
          val parts = inner.split(",").map { it.trim().toFloat() }
          val freq = parts.getOrNull(0) ?: 5f
          val amp = parts.getOrNull(1) ?: 20f
          // Deterministic pseudo-random harmonic noise for reproducible rendering
          val noise = sin(time * freq * 2.0 * PI) + 0.5 * sin(time * freq * 5.3) + 0.25 * sin(time * freq * 9.7)
          (base + (noise * amp * 0.5f)).toFloat()
        } else if (clean.startsWith("time *")) {
          val factor = clean.substringAfter("time *").trim().toFloatOrNull() ?: 10f
          base + (time * factor)
        } else if (clean.startsWith("sin(time")) {
          base + (sin(time * 3f) * 20f)
        } else {
          null
        }
      } catch (e: Exception) {
        null
      }
    }
  }
}
