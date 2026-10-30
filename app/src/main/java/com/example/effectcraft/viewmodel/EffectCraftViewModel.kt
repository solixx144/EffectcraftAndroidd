package com.example.effectcraft.viewmodel

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.effectcraft.effects.EffectCatalog
import com.example.effectcraft.engine.Command
import com.example.effectcraft.engine.UndoRedoManager
import com.example.effectcraft.model.AnimatableProperty
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
import com.example.effectcraft.model.WorkstationTool
import com.example.effectcraft.render.ExportEngine
import com.example.effectcraft.render.ExportFormat
import com.example.effectcraft.render.RenderProgress
import com.example.effectcraft.storage.ProjectSerializer
import com.example.effectcraft.storage.ProjectTemplates
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class EffectCraftViewModel(application: Application) : AndroidViewModel(application) {

  private val undoRedoManager = UndoRedoManager()

  private val _project = MutableStateFlow<Project>(ProjectTemplates.createNeonKineticTitle())
  val project: StateFlow<Project> = _project.asStateFlow()

  private val _activeTool = MutableStateFlow(WorkstationTool.SELECTION)
  val activeTool: StateFlow<WorkstationTool> = _activeTool.asStateFlow()

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  // Viewport Settings
  private val _viewportZoom = MutableStateFlow(0.75f)
  val viewportZoom: StateFlow<Float> = _viewportZoom.asStateFlow()

  private val _viewportPan = MutableStateFlow(Offset.Zero)
  val viewportPan: StateFlow<Offset> = _viewportPan.asStateFlow()

  private val _showSafeZones = MutableStateFlow(false)
  val showSafeZones: StateFlow<Boolean> = _showSafeZones.asStateFlow()

  private val _showTransparencyGrid = MutableStateFlow(false)
  val showTransparencyGrid: StateFlow<Boolean> = _showTransparencyGrid.asStateFlow()

  private val _isGraphEditorMode = MutableStateFlow(false)
  val isGraphEditorMode: StateFlow<Boolean> = _isGraphEditorMode.asStateFlow()

  // Modals & Panels
  private val _showExportDialog = MutableStateFlow(false)
  val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

  private val _exportProgress = MutableStateFlow<RenderProgress?>(null)
  val exportProgress: StateFlow<RenderProgress?> = _exportProgress.asStateFlow()

  private val _showDiagnosticsDialog = MutableStateFlow(false)
  val showDiagnosticsDialog: StateFlow<Boolean> = _showDiagnosticsDialog.asStateFlow()

  private val _showChecklistDialog = MutableStateFlow(false)
  val showChecklistDialog: StateFlow<Boolean> = _showChecklistDialog.asStateFlow()

  private val _showTemplatesDialog = MutableStateFlow(false)
  val showTemplatesDialog: StateFlow<Boolean> = _showTemplatesDialog.asStateFlow()

  private val _showAboutDialog = MutableStateFlow(false)
  val showAboutDialog: StateFlow<Boolean> = _showAboutDialog.asStateFlow()

  private val _showNewSolidDialog = MutableStateFlow(false)
  val showNewSolidDialog: StateFlow<Boolean> = _showNewSolidDialog.asStateFlow()

  private val _showNewTextDialog = MutableStateFlow(false)
  val showNewTextDialog: StateFlow<Boolean> = _showNewTextDialog.asStateFlow()

  private var playbackJob: Job? = null
  private var autoSaveJob: Job? = null

  init {
    startAutoSaveTimer()
  }

  // --- Auto-Save Mechanism ---
  private fun startAutoSaveTimer() {
    autoSaveJob?.cancel()
    autoSaveJob = viewModelScope.launch {
      while (true) {
        delay(60_000) // Every 60 seconds
        ProjectSerializer.saveAutoSave(getApplication(), _project.value)
      }
    }
  }

  // --- Tool & Viewport Actions ---
  fun setTool(tool: WorkstationTool) {
    _activeTool.value = tool
  }

  fun updateViewportPan(panDelta: Offset) {
    _viewportPan.update { it + panDelta }
  }

  fun updateViewportZoom(zoomFactor: Float) {
    _viewportZoom.update { (it * zoomFactor).coerceIn(0.2f, 4.0f) }
  }

  fun resetViewport() {
    _viewportZoom.value = 0.75f
    _viewportPan.value = Offset.Zero
  }

  fun toggleSafeZones() {
    _showSafeZones.update { !it }
  }

  fun toggleTransparencyGrid() {
    _showTransparencyGrid.update { !it }
  }

  fun toggleGraphEditor() {
    _isGraphEditorMode.update { !it }
  }

  // --- Playback Controls ---
  fun togglePlayPause() {
    if (_isPlaying.value) {
      pause()
    } else {
      play()
    }
  }

  fun play() {
    _isPlaying.value = true
    playbackJob?.cancel()
    playbackJob = viewModelScope.launch {
      val comp = _project.value.getActiveComposition()
      val frameDurationMs = (1000L / comp.frameRate).coerceAtLeast(16L)

      while (_isPlaying.value) {
        delay(frameDurationMs)
        stepFrame(1)
      }
    }
  }

  fun pause() {
    _isPlaying.value = false
    playbackJob?.cancel()
  }

  fun seekToFrame(targetFrame: Int) {
    val comp = _project.value.getActiveComposition()
    val clamped = targetFrame.coerceIn(0, comp.durationFrames)
    updateActiveComp { it.copy(currentFrame = clamped) }
  }

  fun stepFrame(delta: Int) {
    val comp = _project.value.getActiveComposition()
    var next = comp.currentFrame + delta
    if (next > comp.workAreaEnd) {
      next = comp.workAreaStart // Loop back
    } else if (next < 0) {
      next = 0
    }
    updateActiveComp { it.copy(currentFrame = next) }
  }

  fun goToStart() {
    val comp = _project.value.getActiveComposition()
    seekToFrame(comp.workAreaStart)
  }

  fun goToEnd() {
    val comp = _project.value.getActiveComposition()
    seekToFrame(comp.workAreaEnd)
  }

  // --- Layer Management ---
  fun selectLayer(layerId: String?) {
    updateActiveComp { it.copy(selectedLayerId = layerId) }
  }

  fun addSolidLayer(name: String, color: Color, width: Int = 1920, height: Int = 1080) {
    recordHistory()
    val comp = _project.value.getActiveComposition()
    val newLayer = Layer(
      id = "layer_" + UUID.randomUUID().toString().take(8),
      name = name,
      type = LayerType.SOLID,
      solidColor = color,
      solidWidth = width,
      solidHeight = height,
      inFrame = 0,
      outFrame = comp.durationFrames,
      transform = LayerTransform(
        positionX = AnimatableProperty("Position X", width / 2f),
        positionY = AnimatableProperty("Position Y", height / 2f)
      )
    )
    updateActiveComp { c ->
      c.copy(layers = listOf(newLayer) + c.layers, selectedLayerId = newLayer.id)
    }
  }

  fun addTextLayer(text: String, fontSize: Float = 72f, color: Color = Color.White) {
    recordHistory()
    val comp = _project.value.getActiveComposition()
    val newLayer = Layer(
      id = "layer_" + UUID.randomUUID().toString().take(8),
      name = text.take(16),
      type = LayerType.TEXT,
      textData = TextLayerData(text = text, fontSize = fontSize, color = color),
      inFrame = 0,
      outFrame = comp.durationFrames,
      transform = LayerTransform(
        positionX = AnimatableProperty("Position X", comp.width / 2f),
        positionY = AnimatableProperty("Position Y", comp.height / 2f)
      )
    )
    updateActiveComp { c ->
      c.copy(layers = listOf(newLayer) + c.layers, selectedLayerId = newLayer.id)
    }
  }

  fun addShapeLayer(shapeType: ShapeType, color: Color = Color(0xFF00E5FF)) {
    recordHistory()
    val comp = _project.value.getActiveComposition()
    val newLayer = Layer(
      id = "layer_" + UUID.randomUUID().toString().take(8),
      name = "Shape - ${shapeType.name.lowercase().replaceFirstChar { it.uppercase() }}",
      type = LayerType.SHAPE,
      shapeData = ShapeLayerData(shapeType = shapeType, fillColor = color),
      inFrame = 0,
      outFrame = comp.durationFrames,
      transform = LayerTransform(
        positionX = AnimatableProperty("Position X", comp.width / 2f),
        positionY = AnimatableProperty("Position Y", comp.height / 2f)
      )
    )
    updateActiveComp { c ->
      c.copy(layers = listOf(newLayer) + c.layers, selectedLayerId = newLayer.id)
    }
  }

  fun addNullObjectLayer() {
    recordHistory()
    val comp = _project.value.getActiveComposition()
    val newLayer = Layer(
      id = "null_" + UUID.randomUUID().toString().take(8),
      name = "Null Controller",
      type = LayerType.NULL_OBJECT,
      inFrame = 0,
      outFrame = comp.durationFrames
    )
    updateActiveComp { c ->
      c.copy(layers = listOf(newLayer) + c.layers, selectedLayerId = newLayer.id)
    }
  }

  fun addAdjustmentLayer() {
    recordHistory()
    val comp = _project.value.getActiveComposition()
    val newLayer = Layer(
      id = "adj_" + UUID.randomUUID().toString().take(8),
      name = "Adjustment Layer",
      type = LayerType.ADJUSTMENT_LAYER,
      inFrame = 0,
      outFrame = comp.durationFrames
    )
    updateActiveComp { c ->
      c.copy(layers = listOf(newLayer) + c.layers, selectedLayerId = newLayer.id)
    }
  }

  fun deleteSelectedLayer() {
    val comp = _project.value.getActiveComposition()
    val targetId = comp.selectedLayerId ?: return
    recordHistory()
    updateActiveComp { c ->
      val remaining = c.layers.filter { it.id != targetId }
      c.copy(layers = remaining, selectedLayerId = remaining.firstOrNull()?.id)
    }
  }

  fun duplicateSelectedLayer() {
    val comp = _project.value.getActiveComposition()
    val target = comp.getSelectedLayer() ?: return
    recordHistory()
    val dup = target.copy(
      id = "layer_" + UUID.randomUUID().toString().take(8),
      name = "${target.name} Copy"
    )
    updateActiveComp { c ->
      val idx = c.layers.indexOf(target)
      val list = c.layers.toMutableList()
      list.add(idx, dup)
      c.copy(layers = list, selectedLayerId = dup.id)
    }
  }

  fun moveLayerUp(layerId: String) {
    recordHistory()
    updateActiveComp { c ->
      val list = c.layers.toMutableList()
      val idx = list.indexOfFirst { it.id == layerId }
      if (idx > 0) {
        val item = list.removeAt(idx)
        list.add(idx - 1, item)
      }
      c.copy(layers = list)
    }
  }

  fun moveLayerDown(layerId: String) {
    recordHistory()
    updateActiveComp { c ->
      val list = c.layers.toMutableList()
      val idx = list.indexOfFirst { it.id == layerId }
      if (idx >= 0 && idx < list.size - 1) {
        val item = list.removeAt(idx)
        list.add(idx + 1, item)
      }
      c.copy(layers = list)
    }
  }

  fun toggleLayerVisibility(layerId: String) {
    recordHistory()
    updateActiveComp { c ->
      c.copy(layers = c.layers.map { if (it.id == layerId) it.copy(isVisible = !it.isVisible) else it })
    }
  }

  fun toggleLayerLock(layerId: String) {
    recordHistory()
    updateActiveComp { c ->
      c.copy(layers = c.layers.map { if (it.id == layerId) it.copy(isLocked = !it.isLocked) else it })
    }
  }

  fun toggleLayerSolo(layerId: String) {
    recordHistory()
    updateActiveComp { c ->
      c.copy(layers = c.layers.map { if (it.id == layerId) it.copy(isSolo = !it.isSolo) else it })
    }
  }

  fun toggleLayer3D(layerId: String) {
    recordHistory()
    updateActiveComp { c ->
      c.copy(layers = c.layers.map { if (it.id == layerId) it.copy(is3D = !it.is3D) else it })
    }
  }

  fun setLayerBlendMode(layerId: String, blendMode: LayerBlendMode) {
    recordHistory()
    updateActiveComp { c ->
      c.copy(layers = c.layers.map { if (it.id == layerId) it.copy(blendMode = blendMode) else it })
    }
  }

  // --- Effects Application ---
  fun applyEffectToSelectedLayer(effectId: String) {
    val comp = _project.value.getActiveComposition()
    val target = comp.getSelectedLayer() ?: return
    val spec = EffectCatalog.getSpec(effectId) ?: return

    recordHistory()
    val newEffectInstance = spec.instantiate()
    val updatedEffects = target.effects + newEffectInstance

    updateActiveComp { c ->
      c.copy(layers = c.layers.map { if (it.id == target.id) it.copy(effects = updatedEffects) else it })
    }
  }

  fun toggleEffectEnabled(layerId: String, effectInstanceId: String) {
    recordHistory()
    updateActiveComp { c ->
      c.copy(layers = c.layers.map { layer ->
        if (layer.id == layerId) {
          layer.copy(effects = layer.effects.map { eff ->
            if (eff.id == effectInstanceId) eff.copy(isEnabled = !eff.isEnabled) else eff
          })
        } else layer
      })
    }
  }

  fun removeEffect(layerId: String, effectInstanceId: String) {
    recordHistory()
    updateActiveComp { c ->
      c.copy(layers = c.layers.map { layer ->
        if (layer.id == layerId) {
          layer.copy(effects = layer.effects.filter { it.id != effectInstanceId })
        } else layer
      })
    }
  }

  fun updateEffectParam(layerId: String, effectInstanceId: String, paramKey: String, newValue: Float) {
    updateActiveComp { c ->
      c.copy(layers = c.layers.map { layer ->
        if (layer.id == layerId) {
          layer.copy(effects = layer.effects.map { eff ->
            if (eff.id == effectInstanceId) {
              val currentParam = eff.parameters[paramKey]
              if (currentParam != null) {
                val updatedParam = currentParam.copy(baseValue = newValue)
                eff.copy(parameters = eff.parameters + (paramKey to updatedParam))
              } else eff
            } else eff
          })
        } else layer
      })
    }
  }

  // --- Keyframe Toggling & Updating ---
  fun updateLayerTransformProperty(
    layerId: String,
    transformUpdater: (LayerTransform) -> LayerTransform
  ) {
    updateActiveComp { c ->
      c.copy(layers = c.layers.map { layer ->
        if (layer.id == layerId) {
          layer.copy(transform = transformUpdater(layer.transform))
        } else layer
      })
    }
  }

  fun togglePropertyKeyframing(
    layerId: String,
    propName: String,
    transformPropertySelector: (LayerTransform) -> AnimatableProperty,
    transformPropertyUpdater: (LayerTransform, AnimatableProperty) -> LayerTransform
  ) {
    recordHistory()
    val comp = _project.value.getActiveComposition()
    val currentFrame = comp.currentFrame

    updateActiveComp { c ->
      c.copy(layers = c.layers.map { layer ->
        if (layer.id == layerId) {
          val prop = transformPropertySelector(layer.transform)
          val newIsKeyframing = !prop.isKeyframing
          val updatedKeyframes = if (newIsKeyframing) {
            listOf(Keyframe(frame = currentFrame, value = prop.baseValue, interpolation = InterpolationMode.EASE_IN_OUT))
          } else {
            emptyList()
          }
          val updatedProp = prop.copy(isKeyframing = newIsKeyframing, keyframes = updatedKeyframes)
          layer.copy(transform = transformPropertyUpdater(layer.transform, updatedProp))
        } else layer
      })
    }
  }

  fun addOrUpdateKeyframeAtCurrentTime(
    layerId: String,
    transformPropertySelector: (LayerTransform) -> AnimatableProperty,
    transformPropertyUpdater: (LayerTransform, AnimatableProperty) -> LayerTransform
  ) {
    recordHistory()
    val comp = _project.value.getActiveComposition()
    val currentFrame = comp.currentFrame

    updateActiveComp { c ->
      c.copy(layers = c.layers.map { layer ->
        if (layer.id == layerId) {
          val prop = transformPropertySelector(layer.transform)
          val curVal = prop.getValueAt(currentFrame, comp.frameRate)
          val filtered = prop.keyframes.filter { it.frame != currentFrame }
          val newKf = Keyframe(frame = currentFrame, value = curVal, interpolation = InterpolationMode.BEZIER)
          val updatedProp = prop.copy(
            isKeyframing = true,
            keyframes = (filtered + newKf).sortedBy { it.frame }
          )
          layer.copy(transform = transformPropertyUpdater(layer.transform, updatedProp))
        } else layer
      })
    }
  }

  // --- Undo / Redo ---
  private fun recordHistory() {
    undoRedoManager.recordState(_project.value)
  }

  fun undo() {
    val previous = undoRedoManager.undo(_project.value)
    if (previous != null) {
      _project.value = previous
    }
  }

  fun redo() {
    val next = undoRedoManager.redo(_project.value)
    if (next != null) {
      _project.value = next
    }
  }

  val canUndo: Boolean get() = undoRedoManager.canUndo
  val canRedo: Boolean get() = undoRedoManager.canRedo

  // --- Project Loading & Templates ---
  fun loadTemplate(templateType: Int) {
    recordHistory()
    val newProj = when (templateType) {
      0 -> ProjectTemplates.createNeonKineticTitle()
      1 -> ProjectTemplates.createBroadcastLowerThird()
      else -> ProjectTemplates.createCyberpunkGlow()
    }
    _project.value = newProj
    resetViewport()
  }

  // --- Export Execution ---
  fun startExport(format: ExportFormat) {
    _exportProgress.value = RenderProgress(
      currentFrame = 0,
      totalFrames = _project.value.getActiveComposition().durationFrames,
      percentage = 0f,
      statusMessage = "Initializing render pipeline..."
    )
    viewModelScope.launch {
      ExportEngine.exportComposition(
        context = getApplication(),
        comp = _project.value.getActiveComposition(),
        format = format,
        onProgress = { progress ->
          _exportProgress.value = progress
        }
      )
    }
  }

  fun dismissExportDialog() {
    _showExportDialog.value = false
    _exportProgress.value = null
  }

  // --- Dialog Visibility Toggles ---
  fun openExportDialog() { _showExportDialog.value = true }
  fun openDiagnosticsDialog() { _showDiagnosticsDialog.value = true }
  fun closeDiagnosticsDialog() { _showDiagnosticsDialog.value = false }
  fun openChecklistDialog() { _showChecklistDialog.value = true }
  fun closeChecklistDialog() { _showChecklistDialog.value = false }
  fun openTemplatesDialog() { _showTemplatesDialog.value = true }
  fun closeTemplatesDialog() { _showTemplatesDialog.value = false }
  fun openAboutDialog() { _showAboutDialog.value = true }
  fun closeAboutDialog() { _showAboutDialog.value = false }
  fun openNewSolidDialog() { _showNewSolidDialog.value = true }
  fun closeNewSolidDialog() { _showNewSolidDialog.value = false }
  fun openNewTextDialog() { _showNewTextDialog.value = true }
  fun closeNewTextDialog() { _showNewTextDialog.value = false }

  // --- Helper to update current composition cleanly ---
  private fun updateActiveComp(block: (Composition) -> Composition) {
    _project.update { proj ->
      val activeId = proj.activeCompositionId
      val updatedComps = proj.compositions.map { c ->
        if (c.id == activeId) block(c) else c
      }
      proj.copy(compositions = updatedComps, isDirty = true)
    }
  }
}
