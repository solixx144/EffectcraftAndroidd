package com.example.effectcraft.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.effectcraft.model.Composition
import com.example.effectcraft.model.Layer
import com.example.effectcraft.model.LayerType
import com.example.effectcraft.model.ShapeType
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioSurfaceBorder
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.effectcraft.viewmodel.EffectCraftViewModel

@Composable
fun ViewportCanvas(
  viewModel: EffectCraftViewModel,
  activeComp: Composition,
  zoom: Float,
  panOffset: Offset,
  showSafeZones: Boolean,
  showTransparencyGrid: Boolean,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(StudioBackground)
      .clipToBounds()
      .testTag("viewport_canvas")
      .pointerInput(Unit) {
        detectTransformGestures { _, pan, gestureZoom, _ ->
          viewModel.updateViewportPan(pan)
          viewModel.updateViewportZoom(gestureZoom)
        }
      }
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val centerCanvasX = size.width / 2f + panOffset.x
      val centerCanvasY = size.height / 2f + panOffset.y

      // Composition dimensions scaled
      val compW = activeComp.width * zoom * 0.45f
      val compH = activeComp.height * zoom * 0.45f
      val compLeft = centerCanvasX - compW / 2f
      val compTop = centerCanvasY - compH / 2f

      // 1. Draw Checkerboard Transparency Grid if enabled
      if (showTransparencyGrid) {
        val checkSize = 16f
        var cx = compLeft
        while (cx < compLeft + compW) {
          var cy = compTop
          while (cy < compTop + compH) {
            val isEven = (((cx - compLeft) / checkSize).toInt() + ((cy - compTop) / checkSize).toInt()) % 2 == 0
            drawRect(
              color = if (isEven) Color(0xFF202028) else Color(0xFF161620),
              topLeft = Offset(cx, cy),
              size = Size(checkSize, checkSize)
            )
            cy += checkSize
          }
          cx += checkSize
        }
      } else {
        // Draw solid composition background
        drawRect(
          color = activeComp.backgroundColor,
          topLeft = Offset(compLeft, compTop),
          size = Size(compW, compH)
        )
      }

      // Draw composition boundary border
      drawRect(
        color = StudioSurfaceBorder,
        topLeft = Offset(compLeft, compTop),
        size = Size(compW, compH),
        style = Stroke(width = 1.5f)
      )

      val currentFrame = activeComp.currentFrame
      val fps = activeComp.frameRate
      val scaleRatio = compW / activeComp.width.toFloat()

      // 2. Render all visible layers in back-to-front order
      for (layer in activeComp.layers.reversed()) {
        if (!layer.isVisibleAt(currentFrame)) continue

        val posX = compLeft + (layer.transform.positionX.getValueAt(currentFrame, fps) * scaleRatio)
        val posY = compTop + (layer.transform.positionY.getValueAt(currentFrame, fps) * scaleRatio)
        val sclX = (layer.transform.scaleX.getValueAt(currentFrame, fps) / 100f) * scaleRatio
        val sclY = (layer.transform.scaleY.getValueAt(currentFrame, fps) / 100f) * scaleRatio
        val rot = layer.transform.rotation.getValueAt(currentFrame, fps)
        val opacity = (layer.transform.opacity.getValueAt(currentFrame, fps) / 100f).coerceIn(0f, 1f)

        translate(left = posX, top = posY) {
          rotate(degrees = rot, pivot = Offset.Zero) {
            scale(scaleX = sclX, scaleY = sclY, pivot = Offset.Zero) {
              drawLayerContent(layer, opacity)
            }
          }
        }
      }

      // 3. Draw Selected Layer Bounding Box & Handles
      val selectedLayer = activeComp.getSelectedLayer()
      if (selectedLayer != null && selectedLayer.isVisibleAt(currentFrame)) {
        val selX = compLeft + (selectedLayer.transform.positionX.getValueAt(currentFrame, fps) * scaleRatio)
        val selY = compTop + (selectedLayer.transform.positionY.getValueAt(currentFrame, fps) * scaleRatio)
        val selSclX = (selectedLayer.transform.scaleX.getValueAt(currentFrame, fps) / 100f) * scaleRatio
        val selSclY = (selectedLayer.transform.scaleY.getValueAt(currentFrame, fps) / 100f) * scaleRatio
        val selRot = selectedLayer.transform.rotation.getValueAt(currentFrame, fps)

        translate(left = selX, top = selY) {
          rotate(degrees = selRot, pivot = Offset.Zero) {
            val boxW = 400f * selSclX
            val boxH = 240f * selSclY

            // Bounding box wireframe
            drawRect(
              color = AccentCyan,
              topLeft = Offset(-boxW / 2f, -boxH / 2f),
              size = Size(boxW, boxH),
              style = Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
              )
            )

            // 4 Corner scaling handles
            val handleSize = 10f
            val corners = listOf(
              Offset(-boxW / 2f, -boxH / 2f),
              Offset(boxW / 2f, -boxH / 2f),
              Offset(boxW / 2f, boxH / 2f),
              Offset(-boxW / 2f, boxH / 2f)
            )
            for (c in corners) {
              drawRect(
                color = AccentCyan,
                topLeft = Offset(c.x - handleSize / 2f, c.y - handleSize / 2f),
                size = Size(handleSize, handleSize)
              )
            }

            // Anchor point crosshair at center
            drawLine(
              color = AccentAmber,
              start = Offset(-8f, 0f),
              end = Offset(8f, 0f),
              strokeWidth = 2f
            )
            drawLine(
              color = AccentAmber,
              start = Offset(0f, -8f),
              end = Offset(0f, 8f),
              strokeWidth = 2f
            )
          }
        }

        // Draw Motion Path trajectory if position is keyframed
        val kfs = selectedLayer.transform.positionX.keyframes
        if (kfs.size >= 2) {
          val path = Path()
          for (i in kfs.indices) {
            val kf = kfs[i]
            val px = compLeft + (kf.value * scaleRatio)
            val py = compTop + (selectedLayer.transform.positionY.getValueAt(kf.frame, fps) * scaleRatio)
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)

            // Draw keyframe dot
            drawCircle(color = AccentAmber, radius = 4f, center = Offset(px, py))
          }
          drawPath(path, color = AccentAmber.copy(alpha = 0.6f), style = Stroke(width = 1.5f))
        }
      }

      // 4. Safe Zones Overlay (Action Safe 90%, Title Safe 80%)
      if (showSafeZones) {
        // Action safe (90%)
        val asW = compW * 0.9f
        val asH = compH * 0.9f
        drawRect(
          color = Color(0x6600E5FF),
          topLeft = Offset(centerCanvasX - asW / 2f, centerCanvasY - asH / 2f),
          size = Size(asW, asH),
          style = Stroke(width = 1f)
        )
        // Title safe (80%)
        val tsW = compW * 0.8f
        val tsH = compH * 0.8f
        drawRect(
          color = Color(0x66FFB800),
          topLeft = Offset(centerCanvasX - tsW / 2f, centerCanvasY - tsH / 2f),
          size = Size(tsW, tsH),
          style = Stroke(width = 1f)
        )
      }
    }

    // Viewport HUD Toolbar (Bottom Left Overlay)
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = StudioSurfaceElevated.copy(alpha = 0.9f),
      border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder),
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${(zoom * 100).toInt()}%",
          fontSize = 11.sp,
          color = AccentCyan,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
          onClick = { viewModel.resetViewport() },
          modifier = Modifier.size(28.dp)
        ) {
          Icon(Icons.Default.AspectRatio, contentDescription = "Fit Viewport", tint = TextPrimary, modifier = Modifier.size(16.dp))
        }

        IconButton(
          onClick = { viewModel.toggleSafeZones() },
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            Icons.Default.GridOn,
            contentDescription = "Safe Zones",
            tint = if (showSafeZones) AccentCyan else TextMuted,
            modifier = Modifier.size(16.dp)
          )
        }

        IconButton(
          onClick = { viewModel.toggleTransparencyGrid() },
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            Icons.Default.ZoomIn,
            contentDescription = "Transparency Grid",
            tint = if (showTransparencyGrid) AccentAmber else TextMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

private fun DrawScope.drawLayerContent(layer: Layer, opacity: Float) {
  when (layer.type) {
    LayerType.SOLID -> {
      val w = layer.solidWidth.toFloat()
      val h = layer.solidHeight.toFloat()
      drawRect(
        color = layer.solidColor.copy(alpha = layer.solidColor.alpha * opacity),
        topLeft = Offset(-w / 2f, -h / 2f),
        size = Size(w, h)
      )
    }
    LayerType.SHAPE -> {
      val sd = layer.shapeData
      if (sd != null) {
        if (sd.hasFill) {
          if (sd.shapeType == ShapeType.ELLIPSE) {
            drawOval(
              color = sd.fillColor.copy(alpha = sd.fillColor.alpha * opacity),
              topLeft = Offset(-sd.width / 2f, -sd.height / 2f),
              size = Size(sd.width, sd.height)
            )
          } else {
            drawRoundRect(
              color = sd.fillColor.copy(alpha = sd.fillColor.alpha * opacity),
              topLeft = Offset(-sd.width / 2f, -sd.height / 2f),
              size = Size(sd.width, sd.height),
              cornerRadius = androidx.compose.ui.geometry.CornerRadius(sd.cornerRadius, sd.cornerRadius)
            )
          }
        }
        if (sd.hasStroke) {
          if (sd.shapeType == ShapeType.ELLIPSE) {
            drawOval(
              color = sd.strokeColor.copy(alpha = sd.strokeColor.alpha * opacity),
              topLeft = Offset(-sd.width / 2f, -sd.height / 2f),
              size = Size(sd.width, sd.height),
              style = Stroke(width = sd.strokeWidth)
            )
          } else {
            drawRoundRect(
              color = sd.strokeColor.copy(alpha = sd.strokeColor.alpha * opacity),
              topLeft = Offset(-sd.width / 2f, -sd.height / 2f),
              size = Size(sd.width, sd.height),
              cornerRadius = androidx.compose.ui.geometry.CornerRadius(sd.cornerRadius, sd.cornerRadius),
              style = Stroke(width = sd.strokeWidth)
            )
          }
        }
      }
    }
    LayerType.TEXT -> {
      val td = layer.textData
      if (td != null) {
        val paint = android.graphics.Paint().apply {
          color = android.graphics.Color.argb(
            (td.color.alpha * opacity * 255).toInt(),
            (td.color.red * 255).toInt(),
            (td.color.green * 255).toInt(),
            (td.color.blue * 255).toInt()
          )
          textSize = td.fontSize
          textAlign = android.graphics.Paint.Align.CENTER
          letterSpacing = td.tracking / 20f
          isFakeBoldText = td.isBold
          isAntiAlias = true
        }
        drawContext.canvas.nativeCanvas.drawText(td.text, 0f, td.fontSize / 3f, paint)
      }
    }
    else -> {
      // Null, camera, or other indicators
    }
  }
}
