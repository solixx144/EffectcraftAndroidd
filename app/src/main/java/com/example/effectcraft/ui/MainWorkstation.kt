package com.example.effectcraft.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.StudioBackground
import com.example.effectcraft.viewmodel.EffectCraftViewModel

@Composable
fun MainWorkstation(viewModel: EffectCraftViewModel) {
  val project by viewModel.project.collectAsState()
  val activeTool by viewModel.activeTool.collectAsState()
  val isPlaying by viewModel.isPlaying.collectAsState()
  val viewportZoom by viewModel.viewportZoom.collectAsState()
  val viewportPan by viewModel.viewportPan.collectAsState()
  val showSafeZones by viewModel.showSafeZones.collectAsState()
  val showTransparencyGrid by viewModel.showTransparencyGrid.collectAsState()
  val isGraphEditorMode by viewModel.isGraphEditorMode.collectAsState()

  // Dialog States
  val showExportDialog by viewModel.showExportDialog.collectAsState()
  val exportProgress by viewModel.exportProgress.collectAsState()
  val showDiagnosticsDialog by viewModel.showDiagnosticsDialog.collectAsState()
  val showChecklistDialog by viewModel.showChecklistDialog.collectAsState()
  val showTemplatesDialog by viewModel.showTemplatesDialog.collectAsState()
  val showAboutDialog by viewModel.showAboutDialog.collectAsState()
  val showNewSolidDialog by viewModel.showNewSolidDialog.collectAsState()
  val showNewTextDialog by viewModel.showNewTextDialog.collectAsState()

  val activeComp = project.getActiveComposition()

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    containerColor = StudioBackground,
    modifier = Modifier
      .fillMaxSize()
      .testTag("main_workstation")
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(StudioBackground)
    ) {
      // 1. Top Bar: Tools, Transport, Timecode, Actions
      TopToolbar(
        viewModel = viewModel,
        activeTool = activeTool,
        isPlaying = isPlaying,
        canUndo = viewModel.canUndo,
        canRedo = viewModel.canRedo,
        timecodeFormatted = activeComp.currentTimecode.formatted(),
        currentFrame = activeComp.currentFrame
      )

      // 2. Middle Row: Left Dock (Bin/Effects), Center Canvas (Viewport), Right Dock (Inspector)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) {
        // Left Dock: Project Bin & Effects Catalog
        ProjectBinPanel(
          viewModel = viewModel,
          activeComp = activeComp
        )

        // Center: Interactive Viewport Canvas
        ViewportCanvas(
          viewModel = viewModel,
          activeComp = activeComp,
          zoom = viewportZoom,
          panOffset = viewportPan,
          showSafeZones = showSafeZones,
          showTransparencyGrid = showTransparencyGrid,
          modifier = Modifier.weight(1f)
        )

        // Right Dock: Inspector & Effect Controls
        InspectorPanel(
          viewModel = viewModel,
          activeComp = activeComp
        )
      }

      // 3. Bottom Row: Multi-track Timeline & Keyframe Graph Editor
      TimelinePanel(
        viewModel = viewModel,
        activeComp = activeComp,
        isGraphEditorMode = isGraphEditorMode
      )
    }

    // Dialogs & Modals
    if (showExportDialog) {
      ExportDialog(
        viewModel = viewModel,
        progress = exportProgress,
        onDismiss = { viewModel.dismissExportDialog() }
      )
    }

    if (showDiagnosticsDialog) {
      HardwareDiagnosticsDialog(
        onDismiss = { viewModel.closeDiagnosticsDialog() }
      )
    }

    if (showChecklistDialog) {
      ParityChecklistDialog(
        onDismiss = { viewModel.closeChecklistDialog() }
      )
    }

    if (showTemplatesDialog) {
      TemplatesDialog(
        viewModel = viewModel,
        onDismiss = { viewModel.closeTemplatesDialog() }
      )
    }

    if (showAboutDialog) {
      AboutDialog(
        onDismiss = { viewModel.closeAboutDialog() }
      )
    }

    if (showNewSolidDialog) {
      NewSolidDialog(
        viewModel = viewModel,
        onDismiss = { viewModel.closeNewSolidDialog() }
      )
    }

    if (showNewTextDialog) {
      NewTextDialog(
        viewModel = viewModel,
        onDismiss = { viewModel.closeNewTextDialog() }
      )
    }
  }
}
