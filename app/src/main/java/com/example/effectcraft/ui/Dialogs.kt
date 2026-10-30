package com.example.effectcraft.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.effectcraft.checklist.ParityChecklist
import com.example.effectcraft.render.ExportFormat
import com.example.effectcraft.render.HardwareDiagnostics
import com.example.effectcraft.render.RenderProgress
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
fun ExportDialog(
  viewModel: EffectCraftViewModel,
  progress: RenderProgress?,
  onDismiss: () -> Unit
) {
  var selectedFormat by remember { mutableStateOf(ExportFormat.MP4) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Videocam, contentDescription = null, tint = AccentCyan)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Render Queue & Export", color = TextPrimary, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text("Select Output Format:", fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          ExportFormat.values().forEach { fmt ->
            val isSelected = selectedFormat == fmt
            Surface(
              modifier = Modifier
                .weight(1f)
                .clickable { if (progress == null) selectedFormat = fmt },
              shape = RoundedCornerShape(6.dp),
              color = if (isSelected) AccentCyan else StudioSurfaceElevated,
              border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder)
            ) {
              Text(
                text = fmt.name.replace("_", " "),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.Black else TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (progress != null) {
          Text(text = progress.statusMessage, fontSize = 12.sp, color = AccentCyan)
          Spacer(modifier = Modifier.height(6.dp))
          LinearProgressIndicator(
            progress = { progress.percentage },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = AccentCyan,
            trackColor = StudioSurfaceBorder
          )
          Spacer(modifier = Modifier.height(6.dp))
          if (progress.isComplete) {
            Text(
              text = "Saved to: ${progress.outputPath}",
              fontSize = 11.sp,
              color = Color(0xFF00E676),
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    },
    confirmButton = {
      if (progress?.isComplete == true) {
        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
        ) {
          Text("Done")
        }
      } else {
        Button(
          onClick = { viewModel.startExport(selectedFormat) },
          enabled = progress == null,
          colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black),
          modifier = Modifier.testTag("start_render_button")
        ) {
          Text(if (progress == null) "Start Render" else "Rendering...")
        }
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    },
    containerColor = StudioSurface,
    shape = RoundedCornerShape(12.dp)
  )
}

@Composable
fun HardwareDiagnosticsDialog(onDismiss: () -> Unit) {
  val context = LocalContext.current
  val report = remember { HardwareDiagnostics.runDiagnostics(context) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Memory, contentDescription = null, tint = AccentCyan)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Hardware & WebGPU Diagnostics", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Device: ${report.deviceModel} | ${report.androidVersion}",
          fontSize = 11.sp,
          color = AccentAmber,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
          modifier = Modifier.height(280.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(report.items) { item ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = StudioSurfaceElevated,
              border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(text = item.feature, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (item.isOperational) Color(0xFF003822) else Color(0xFF4A1212)
                  ) {
                    Text(
                      text = item.status,
                      color = if (item.isOperational) Color(0xFF00E676) else Color(0xFFFF5252),
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.details, color = TextMuted, fontSize = 10.sp)
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)) {
        Text("Close")
      }
    },
    containerColor = StudioSurface,
    shape = RoundedCornerShape(12.dp)
  )
}

@Composable
fun ParityChecklistDialog(onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Desktop Parity Checklist", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(
          text = "${ParityChecklist.completionRatePercent}% Complete",
          color = Color(0xFF00E676),
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier.height(340.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(ParityChecklist.categories) { cat ->
          Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = cat.title, fontWeight = FontWeight.Bold, color = AccentCyan, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            for (f in cat.features) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = StudioSurfaceElevated,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 2.dp)
              ) {
                Row(
                  modifier = Modifier.padding(6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Text(text = f.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(text = "Android: ${f.androidStatus} (${f.note})", fontSize = 9.sp, color = TextMuted)
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)) {
        Text("Close")
      }
    },
    containerColor = StudioSurface,
    shape = RoundedCornerShape(12.dp)
  )
}

@Composable
fun TemplatesDialog(
  viewModel: EffectCraftViewModel,
  onDismiss: () -> Unit
) {
  val templates = listOf(
    "Neon Kinetic Title" to "Glowing kinetic title with dynamic wiggle expressions, cyber grid, and glow",
    "Broadcast Lower Third" to "Sleek broadcast lower third bar with smooth linear wipe and typography",
    "Cyberpunk Portal Glow" to "Rotating neon ring with glow stylization and particle bursts"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Folder, contentDescription = null, tint = AccentAmber)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Motion Templates", color = TextPrimary, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        templates.forEachIndexed { index, (title, desc) ->
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = StudioSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder),
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                viewModel.loadTemplate(index)
                onDismiss()
              }
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(text = title, fontWeight = FontWeight.Bold, color = AccentCyan, fontSize = 13.sp)
              Spacer(modifier = Modifier.height(2.dp))
              Text(text = desc, color = TextMuted, fontSize = 11.sp)
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
    },
    containerColor = StudioSurface,
    shape = RoundedCornerShape(12.dp)
  )
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Info, contentDescription = null, tint = AccentCyan)
        Spacer(modifier = Modifier.width(8.dp))
        Text("About EffectCraft & Licenses", color = TextPrimary, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .height(260.dp)
      ) {
        Text(
          text = "EffectCraft for Android",
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = AccentCyan
        )
        Text(
          text = "Based on the open-source EffectCraft project (storytold/effectcraft).\n" +
            "Architected for Samsung Galaxy Tab S10+ (Android 16, ARM64-v8a/v9a).",
          fontSize = 11.sp,
          color = TextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Dual Licensed under MIT & Apache 2.0:\n\n" +
            "Permission is hereby granted, free of charge, to any person obtaining a copy " +
            "of this software and associated documentation files, to deal in the Software without restriction.\n\n" +
            "Licensed under the Apache License, Version 2.0.",
          fontSize = 10.sp,
          color = TextMuted,
          fontFamily = FontFamily.Monospace
        )
      }
    },
    confirmButton = {
      Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)) {
        Text("OK")
      }
    },
    containerColor = StudioSurface,
    shape = RoundedCornerShape(12.dp)
  )
}

@Composable
fun NewSolidDialog(
  viewModel: EffectCraftViewModel,
  onDismiss: () -> Unit
) {
  var solidName by remember { mutableStateOf("Deep Red Solid") }
  val solidColors = listOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047),
    Color(0xFFFB8C00), Color(0xFF8E24AA), Color(0xFF00ACC1),
    Color(0xFF121217), Color(0xFFFFFFFF)
  )
  var selectedColor by remember { mutableStateOf(solidColors.first()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("New Solid Layer", color = TextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = solidName,
          onValueChange = { solidName = it },
          label = { Text("Solid Name", fontSize = 11.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentCyan,
            unfocusedBorderColor = StudioSurfaceBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("Choose Color:", fontSize = 11.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          solidColors.forEach { col ->
            Box(
              modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(col)
                .border(
                  width = if (selectedColor == col) 2.dp else 1.dp,
                  color = if (selectedColor == col) AccentCyan else StudioSurfaceBorder,
                  shape = RoundedCornerShape(6.dp)
                )
                .clickable { selectedColor = col }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          viewModel.addSolidLayer(solidName, selectedColor)
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
      ) {
        Text("Create Solid")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
    },
    containerColor = StudioSurface,
    shape = RoundedCornerShape(12.dp)
  )
}

@Composable
fun NewTextDialog(
  viewModel: EffectCraftViewModel,
  onDismiss: () -> Unit
) {
  var textValue by remember { mutableStateOf("Motion Graphic Title") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("New Text Layer", color = TextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = textValue,
          onValueChange = { textValue = it },
          label = { Text("Text Content", fontSize = 11.sp) },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentCyan,
            unfocusedBorderColor = StudioSurfaceBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          viewModel.addTextLayer(textValue)
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
      ) {
        Text("Create Text")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
    },
    containerColor = StudioSurface,
    shape = RoundedCornerShape(12.dp)
  )
}
