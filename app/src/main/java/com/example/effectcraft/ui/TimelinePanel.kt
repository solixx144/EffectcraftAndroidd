package com.example.effectcraft.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.effectcraft.model.Composition
import com.example.effectcraft.model.Layer
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceBorder
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.effectcraft.viewmodel.EffectCraftViewModel

@Composable
fun TimelinePanel(
  viewModel: EffectCraftViewModel,
  activeComp: Composition,
  isGraphEditorMode: Boolean,
  modifier: Modifier = Modifier
) {
  val expandedLayers = remember { mutableStateMapOf<String, Boolean>() }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(240.dp),
    color = StudioSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder)
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Timeline Top Bar: Mode Switcher & Time Ruler
      TimelineHeader(
        viewModel = viewModel,
        activeComp = activeComp,
        isGraphEditorMode = isGraphEditorMode
      )

      if (isGraphEditorMode) {
        // Graph Editor View
        GraphEditorView(activeComp = activeComp)
      } else {
        // Multi-track Dope Sheet
        Row(modifier = Modifier.fillMaxSize()) {
          // Left: Layer Control Headers (Width 220dp)
          LazyColumn(
            modifier = Modifier
              .width(220.dp)
              .fillMaxHeight()
              .background(StudioSurfaceElevated)
              .border(
                width = 1.dp,
                color = StudioSurfaceBorder,
                shape = RoundedCornerShape(0.dp)
              )
          ) {
            items(activeComp.layers) { layer ->
              val isExpanded = expandedLayers[layer.id] ?: false
              val isSelected = layer.id == activeComp.selectedLayerId

              LayerTimelineHeaderRow(
                layer = layer,
                isSelected = isSelected,
                isExpanded = isExpanded,
                onToggleExpand = { expandedLayers[layer.id] = !isExpanded },
                onSelect = { viewModel.selectLayer(layer.id) },
                onToggleVisibility = { viewModel.toggleLayerVisibility(layer.id) },
                onToggleLock = { viewModel.toggleLayerLock(layer.id) },
                onToggleSolo = { viewModel.toggleLayerSolo(layer.id) }
              )

              if (isExpanded) {
                // Sub-property labels
                SubPropertyLabelRow("Position")
                SubPropertyLabelRow("Scale")
                SubPropertyLabelRow("Rotation")
                SubPropertyLabelRow("Opacity")
              }
            }
          }

          // Right: Interactive Timeline Canvas (Tracks, Keyframes, Playhead)
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .background(StudioBackground)
          ) {
            TimelineTracksCanvas(
              activeComp = activeComp,
              expandedLayers = expandedLayers,
              onSeek = { frame -> viewModel.seekToFrame(frame) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun TimelineHeader(
  viewModel: EffectCraftViewModel,
  activeComp: Composition,
  isGraphEditorMode: Boolean
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(34.dp)
      .background(StudioSurfaceElevated)
      .border(androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder)),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Mode switcher buttons
    Row(
      modifier = Modifier
        .width(220.dp)
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = "TIMELINE",
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = TextMuted,
        letterSpacing = 1.sp
      )

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = { if (isGraphEditorMode) viewModel.toggleGraphEditor() },
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            Icons.Default.Timeline,
            contentDescription = "Dope Sheet",
            tint = if (!isGraphEditorMode) AccentCyan else TextMuted,
            modifier = Modifier.size(16.dp)
          )
        }
        IconButton(
          onClick = { if (!isGraphEditorMode) viewModel.toggleGraphEditor() },
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            Icons.Default.ShowChart,
            contentDescription = "Graph Editor",
            tint = if (isGraphEditorMode) AccentAmber else TextMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    // Interactive Time Ruler
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxHeight()
    ) {
      TimeRulerCanvas(
        currentFrame = activeComp.currentFrame,
        durationFrames = activeComp.durationFrames,
        frameRate = activeComp.frameRate,
        onSeek = { frame -> viewModel.seekToFrame(frame) }
      )
    }
  }
}

@Composable
fun TimeRulerCanvas(
  currentFrame: Int,
  durationFrames: Int,
  frameRate: Int,
  onSeek: (Int) -> Unit
) {
  Canvas(
    modifier = Modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectTapGestures { offset ->
          val fraction = (offset.x / size.width).coerceIn(0f, 1f)
          val targetFrame = (fraction * durationFrames).toInt()
          onSeek(targetFrame)
        }
      }
      .pointerInput(Unit) {
        detectDragGestures { change, _ ->
          val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
          val targetFrame = (fraction * durationFrames).toInt()
          onSeek(targetFrame)
        }
      }
  ) {
    // Draw frame ticks
    val tickInterval = 30 // Every 1 second
    var f = 0
    while (f <= durationFrames) {
      val x = (f.toFloat() / durationFrames.toFloat()) * size.width
      val isMajor = f % 30 == 0
      drawLine(
        color = if (isMajor) AccentCyan.copy(alpha = 0.5f) else StudioSurfaceBorder,
        start = Offset(x, if (isMajor) 0f else size.height * 0.5f),
        end = Offset(x, size.height),
        strokeWidth = if (isMajor) 1.5f else 1f
      )
      f += 10
    }

    // Draw Playhead scrubber marker in ruler
    val playheadX = (currentFrame.toFloat() / durationFrames.toFloat()) * size.width
    val trianglePath = Path().apply {
      moveTo(playheadX - 6f, 0f)
      lineTo(playheadX + 6f, 0f)
      lineTo(playheadX, size.height * 0.7f)
      close()
    }
    drawPath(trianglePath, color = AccentCyan)
  }
}

@Composable
fun LayerTimelineHeaderRow(
  layer: Layer,
  isSelected: Boolean,
  isExpanded: Boolean,
  onToggleExpand: () -> Unit,
  onSelect: () -> Unit,
  onToggleVisibility: () -> Unit,
  onToggleLock: () -> Unit,
  onToggleSolo: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(32.dp)
      .background(if (isSelected) StudioSurfaceBorder else Color.Transparent)
      .clickable(onClick = onSelect)
      .padding(horizontal = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Expand toggle icon
    IconButton(onClick = onToggleExpand, modifier = Modifier.size(20.dp)) {
      Icon(
        if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
        contentDescription = "Expand",
        tint = TextMuted,
        modifier = Modifier.size(16.dp)
      )
    }

    // Visibility eyeball
    IconButton(onClick = onToggleVisibility, modifier = Modifier.size(20.dp)) {
      Icon(
        if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
        contentDescription = "Visibility",
        tint = if (layer.isVisible) TextPrimary else TextMuted,
        modifier = Modifier.size(14.dp)
      )
    }

    // Lock icon
    IconButton(onClick = onToggleLock, modifier = Modifier.size(20.dp)) {
      Icon(
        if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
        contentDescription = "Lock",
        tint = if (layer.isLocked) AccentAmber else TextMuted,
        modifier = Modifier.size(14.dp)
      )
    }

    // Layer Color Pip
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(RoundedCornerShape(2.dp))
        .background(Color(layer.type.defaultColor))
    )

    Spacer(modifier = Modifier.width(6.dp))

    // Layer Name
    Text(
      text = layer.name,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      color = if (isSelected) AccentCyan else TextPrimary,
      maxLines = 1,
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
fun SubPropertyLabelRow(label: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(24.dp)
      .background(StudioBackground)
      .padding(start = 28.dp, end = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      fontSize = 10.sp,
      color = TextSecondary,
      fontWeight = FontWeight.Medium
    )
  }
}

@Composable
fun TimelineTracksCanvas(
  activeComp: Composition,
  expandedLayers: Map<String, Boolean>,
  onSeek: (Int) -> Unit
) {
  Canvas(
    modifier = Modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectTapGestures { offset ->
          val fraction = (offset.x / size.width).coerceIn(0f, 1f)
          val targetFrame = (fraction * activeComp.durationFrames).toInt()
          onSeek(targetFrame)
        }
      }
      .pointerInput(Unit) {
        detectDragGestures { change, _ ->
          val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
          val targetFrame = (fraction * activeComp.durationFrames).toInt()
          onSeek(targetFrame)
        }
      }
  ) {
    val duration = activeComp.durationFrames.toFloat()
    var currentY = 0f

    // 1. Draw each layer track bar and keyframe diamonds
    for (layer in activeComp.layers) {
      val isExpanded = expandedLayers[layer.id] ?: false
      val trackH = 32f

      val startX = (layer.inFrame.toFloat() / duration) * size.width
      val endX = (layer.outFrame.toFloat() / duration) * size.width
      val barWidth = (endX - startX).coerceAtLeast(10f)

      // Layer duration bar
      drawRoundRect(
        color = Color(layer.type.defaultColor).copy(alpha = 0.45f),
        topLeft = Offset(startX, currentY + 6f),
        size = Size(barWidth, trackH - 12f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
      )

      // Draw all layer keyframes as golden diamonds
      val allKfs = mutableListOf<Int>()
      allKfs.addAll(layer.transform.positionX.keyframes.map { it.frame })
      allKfs.addAll(layer.transform.scaleX.keyframes.map { it.frame })
      allKfs.addAll(layer.transform.rotation.keyframes.map { it.frame })
      allKfs.addAll(layer.transform.opacity.keyframes.map { it.frame })

      for (kfFrame in allKfs.distinct()) {
        val kfX = (kfFrame.toFloat() / duration) * size.width
        val kfY = currentY + trackH / 2f
        drawDiamond(kfX, kfY, 5f, AccentAmber)
      }

      currentY += trackH

      if (isExpanded) {
        // Draw 4 sub-property rows
        for (i in 0 until 4) {
          val subH = 24f
          drawRect(
            color = StudioSurfaceElevated.copy(alpha = 0.3f),
            topLeft = Offset(0f, currentY),
            size = Size(size.width, subH)
          )
          // Property specific keyframe
          val propKfs = when (i) {
            0 -> layer.transform.positionX.keyframes
            1 -> layer.transform.scaleX.keyframes
            2 -> layer.transform.rotation.keyframes
            else -> layer.transform.opacity.keyframes
          }
          for (kf in propKfs) {
            val kfX = (kf.frame.toFloat() / duration) * size.width
            drawDiamond(kfX, currentY + subH / 2f, 4.5f, AccentAmber)
          }
          currentY += subH
        }
      }
    }

    // 2. Draw vertical Playhead scrubber line extending through all tracks
    val playheadX = (activeComp.currentFrame.toFloat() / duration) * size.width
    drawLine(
      color = AccentCyan,
      start = Offset(playheadX, 0f),
      end = Offset(playheadX, size.height),
      strokeWidth = 2f
    )
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDiamond(
  cx: Float,
  cy: Float,
  radius: Float,
  color: Color
) {
  val path = Path().apply {
    moveTo(cx, cy - radius)
    lineTo(cx + radius, cy)
    lineTo(cx, cy + radius)
    lineTo(cx - radius, cy)
    close()
  }
  drawPath(path, color = color)
}

@Composable
fun GraphEditorView(activeComp: Composition) {
  val selectedLayer = activeComp.getSelectedLayer()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(StudioBackground)
      .padding(12.dp)
  ) {
    if (selectedLayer == null) {
      Text(
        text = "Select an animated layer to view curves in Graph Editor.",
        color = TextMuted,
        fontSize = 12.sp,
        modifier = Modifier.align(Alignment.Center)
      )
    } else {
      Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw grid
        val step = 30f
        var x = 0f
        while (x <= size.width) {
          drawLine(color = StudioSurfaceBorder, start = Offset(x, 0f), end = Offset(x, size.height))
          x += step
        }
        var y = 0f
        while (y <= size.height) {
          drawLine(color = StudioSurfaceBorder, start = Offset(0f, y), end = Offset(size.width, y))
          y += step
        }

        // Draw animated curve for Position / Scale
        val duration = activeComp.durationFrames.toFloat()
        val path = Path()

        for (f in 0..activeComp.durationFrames) {
          val px = (f.toFloat() / duration) * size.width
          val valNorm = (selectedLayer.transform.positionY.getValueAt(f, activeComp.frameRate) / 1080f).coerceIn(0f, 1f)
          val py = size.height * (1f - valNorm)

          if (f == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }

        drawPath(path, color = AccentAmber, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f))

        // Draw playhead vertical line
        val curX = (activeComp.currentFrame.toFloat() / duration) * size.width
        drawLine(color = AccentCyan, start = Offset(curX, 0f), end = Offset(curX, size.height), strokeWidth = 1.5f)
      }
    }
  }
}
