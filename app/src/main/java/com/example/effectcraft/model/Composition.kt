package com.example.effectcraft.model

import androidx.compose.ui.graphics.Color

/**
 * Composition settings and layer timeline data.
 */
data class Composition(
  val id: String = "comp_main",
  val name: String = "Comp 1",
  val width: Int = 1920,
  val height: Int = 1080,
  val frameRate: Int = 30,
  val durationFrames: Int = 300, // 10 seconds at 30fps
  val currentFrame: Int = 0,
  val workAreaStart: Int = 0,
  val workAreaEnd: Int = 300,
  val backgroundColor: Color = Color(0xFF0D0D12),
  val layers: List<Layer> = emptyList(),
  val selectedLayerId: String? = null
) {
  val currentTimecode: Timecode get() = Timecode(currentFrame, frameRate)
  val durationTimecode: Timecode get() = Timecode(durationFrames, frameRate)

  fun getSelectedLayer(): Layer? = layers.find { it.id == selectedLayerId }
}

/**
 * An imported media asset (Image, Video, Audio, or Vector) in the Project Bin.
 */
data class ProjectAsset(
  val id: String,
  val name: String,
  val type: String, // "Image", "Video", "Audio"
  val uri: String,
  val width: Int = 1920,
  val height: Int = 1080,
  val durationFrames: Int = 0,
  val frameRate: Int = 30
)

/**
 * Entire Project State.
 */
data class Project(
  val id: String = "proj_default",
  val name: String = "EffectCraft Motion Project",
  val version: String = "1.0.0",
  val compositions: List<Composition> = listOf(Composition()),
  val activeCompositionId: String = "comp_main",
  val assets: List<ProjectAsset> = emptyList(),
  val isDirty: Boolean = false,
  val lastSavedTime: Long = System.currentTimeMillis()
) {
  fun getActiveComposition(): Composition {
    return compositions.find { it.id == activeCompositionId } ?: compositions.first()
  }
}
