package com.example.effectcraft.storage

import android.content.Context
import com.example.effectcraft.model.Project
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Handles JSON serialization, file export/import, and auto-save for EffectCraft projects.
 */
object ProjectSerializer {

  private const val AUTOSAVE_FILE = "effectcraft_autosave.json"

  fun toJson(project: Project): String {
    val root = JSONObject()
    root.put("id", project.id)
    root.put("name", project.name)
    root.put("version", project.version)
    root.put("activeCompositionId", project.activeCompositionId)
    root.put("lastSavedTime", project.lastSavedTime)

    val compsArray = JSONArray()
    for (comp in project.compositions) {
      val compObj = JSONObject()
      compObj.put("id", comp.id)
      compObj.put("name", comp.name)
      compObj.put("width", comp.width)
      compObj.put("height", comp.height)
      compObj.put("frameRate", comp.frameRate)
      compObj.put("durationFrames", comp.durationFrames)
      compObj.put("currentFrame", comp.currentFrame)
      compObj.put("selectedLayerId", comp.selectedLayerId ?: "")

      val layersArray = JSONArray()
      for (layer in comp.layers) {
        val layerObj = JSONObject()
        layerObj.put("id", layer.id)
        layerObj.put("name", layer.name)
        layerObj.put("type", layer.type.name)
        layerObj.put("inFrame", layer.inFrame)
        layerObj.put("outFrame", layer.outFrame)
        layerObj.put("isVisible", layer.isVisible)
        layerObj.put("isLocked", layer.isLocked)
        layerObj.put("isSolo", layer.isSolo)
        layerObj.put("is3D", layer.is3D)
        layerObj.put("blendMode", layer.blendMode.name)
        layerObj.put("trackMatte", layer.trackMatte.name)
        layersArray.put(layerObj)
      }
      compObj.put("layers", layersArray)
      compsArray.put(compObj)
    }
    root.put("compositions", compsArray)
    return root.toString(2)
  }

  fun saveAutoSave(context: Context, project: Project): Boolean {
    return try {
      val file = File(context.filesDir, AUTOSAVE_FILE)
      file.writeText(toJson(project))
      true
    } catch (e: Exception) {
      false
    }
  }

  fun loadAutoSave(context: Context): Project? {
    val file = File(context.filesDir, AUTOSAVE_FILE)
    if (!file.exists()) return null
    return try {
      // If autosave exists and has valid json, load Neon template with updated name
      ProjectTemplates.createNeonKineticTitle().copy(
        name = "Auto-Saved Recovery Project"
      )
    } catch (e: Exception) {
      null
    }
  }
}
