package com.example.effectcraft.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.effectcraft.model.WorkstationTool
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceBorder
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.effectcraft.viewmodel.EffectCraftViewModel

@Composable
fun TopToolbar(
  viewModel: EffectCraftViewModel,
  activeTool: WorkstationTool,
  isPlaying: Boolean,
  canUndo: Boolean,
  canRedo: Boolean,
  timecodeFormatted: String,
  currentFrame: Int,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(54.dp),
    color = StudioSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder),
    tonalElevation = 4.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // 1. Branding & Project Info
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AccentCyan),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "EC",
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black,
            fontSize = 15.sp
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "EffectCraft",
          color = TextPrimary,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
        Text(
          text = " Pro",
          color = AccentCyan,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp
        )

        Spacer(modifier = Modifier.width(12.dp))
        VerticalDivider(modifier = Modifier.height(24.dp), color = StudioSurfaceBorder)
        Spacer(modifier = Modifier.width(8.dp))

        // Tool Selector Buttons
        ToolIconButton(
          icon = Icons.Default.Navigation,
          label = "Select (V)",
          isSelected = activeTool == WorkstationTool.SELECTION,
          onClick = { viewModel.setTool(WorkstationTool.SELECTION) }
        )
        ToolIconButton(
          icon = Icons.Default.PanTool,
          label = "Hand (H)",
          isSelected = activeTool == WorkstationTool.HAND,
          onClick = { viewModel.setTool(WorkstationTool.HAND) }
        )
        ToolIconButton(
          icon = Icons.Default.RotateRight,
          label = "Rotate (W)",
          isSelected = activeTool == WorkstationTool.ROTATION,
          onClick = { viewModel.setTool(WorkstationTool.ROTATION) }
        )
        ToolIconButton(
          icon = Icons.Default.CropFree,
          label = "Shape (Q)",
          isSelected = activeTool == WorkstationTool.SHAPE,
          onClick = { viewModel.setTool(WorkstationTool.SHAPE) }
        )
        ToolIconButton(
          icon = Icons.Default.Title,
          label = "Text (Ctrl+T)",
          isSelected = activeTool == WorkstationTool.TEXT,
          onClick = { viewModel.setTool(WorkstationTool.TEXT) }
        )
      }

      // 2. Playback Transport & Timecode
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(StudioSurfaceElevated)
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        IconButton(
          onClick = { viewModel.goToStart() },
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            Icons.Default.SkipPrevious,
            contentDescription = "Go to Start",
            tint = TextPrimary,
            modifier = Modifier.size(18.dp)
          )
        }

        IconButton(
          onClick = { viewModel.togglePlayPause() },
          modifier = Modifier
            .size(36.dp)
            .testTag("play_pause_button")
        ) {
          Icon(
            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = if (isPlaying) AccentAmber else AccentCyan,
            modifier = Modifier.size(24.dp)
          )
        }

        IconButton(
          onClick = { viewModel.goToEnd() },
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            Icons.Default.SkipNext,
            contentDescription = "Go to End",
            tint = TextPrimary,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Timecode Readout
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "$timecodeFormatted (f:$currentFrame)",
            color = AccentCyan,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }
      }

      // 3. Right Quick Actions: Templates, Undo, Redo, Diagnostics, Checklist, Render Export
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Undo / Redo
        IconButton(
          onClick = { viewModel.undo() },
          enabled = canUndo,
          modifier = Modifier.size(34.dp)
        ) {
          Icon(
            Icons.Default.Undo,
            contentDescription = "Undo",
            tint = if (canUndo) TextPrimary else TextMuted,
            modifier = Modifier.size(18.dp)
          )
        }
        IconButton(
          onClick = { viewModel.redo() },
          enabled = canRedo,
          modifier = Modifier.size(34.dp)
        ) {
          Icon(
            Icons.Default.Redo,
            contentDescription = "Redo",
            tint = if (canRedo) TextPrimary else TextMuted,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Templates
        IconButton(
          onClick = { viewModel.openTemplatesDialog() },
          modifier = Modifier.size(34.dp)
        ) {
          Icon(
            Icons.Default.Folder,
            contentDescription = "Templates",
            tint = AccentAmber,
            modifier = Modifier.size(18.dp)
          )
        }

        // Hardware Diagnostics
        IconButton(
          onClick = { viewModel.openDiagnosticsDialog() },
          modifier = Modifier.size(34.dp)
        ) {
          Icon(
            Icons.Default.Memory,
            contentDescription = "Hardware Diagnostics",
            tint = AccentCyan,
            modifier = Modifier.size(18.dp)
          )
        }

        // Desktop Parity Checklist
        IconButton(
          onClick = { viewModel.openChecklistDialog() },
          modifier = Modifier.size(34.dp)
        ) {
          Icon(
            Icons.Default.CheckCircle,
            contentDescription = "Parity Checklist",
            tint = Color(0xFF00E676),
            modifier = Modifier.size(18.dp)
          )
        }

        // About & License
        IconButton(
          onClick = { viewModel.openAboutDialog() },
          modifier = Modifier.size(34.dp)
        ) {
          Icon(
            Icons.Default.Info,
            contentDescription = "About & License",
            tint = TextPrimary,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Export Render Queue Button
        Button(
          onClick = { viewModel.openExportDialog() },
          colors = ButtonDefaults.buttonColors(
            containerColor = AccentCyan,
            contentColor = Color.Black
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .height(36.dp)
            .testTag("export_button")
        ) {
          Icon(
            Icons.Default.VideoFile,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Export",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }
      }
    }
  }
}

@Composable
fun ToolIconButton(
  icon: ImageVector,
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .padding(horizontal = 2.dp)
      .size(34.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(if (isSelected) AccentCyan else Color.Transparent)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = if (isSelected) Color.Black else TextPrimary,
      modifier = Modifier.size(18.dp)
    )
  }
}
