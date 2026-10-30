package com.example.effectcraft.engine

import com.example.effectcraft.model.Project

/**
 * Command pattern implementation matching EffectCraft desktop command registry architecture.
 * "Everything is a command" — UI, shortcuts, CLI, and automation all route through here.
 */
data class Command(
  val id: String,
  val label: String,
  val category: String,
  val shortcut: String? = null,
  val isDestructive: Boolean = false
)

/**
 * Manages reversible transactions and undo/redo history stack for the project.
 */
class UndoRedoManager(val maxHistory: Int = 50) {
  private val undoStack = ArrayDeque<Project>()
  private val redoStack = ArrayDeque<Project>()

  val canUndo: Boolean get() = undoStack.isNotEmpty()
  val canRedo: Boolean get() = redoStack.isNotEmpty()

  fun recordState(currentProject: Project) {
    undoStack.addLast(currentProject)
    if (undoStack.size > maxHistory) {
      undoStack.removeFirst()
    }
    redoStack.clear()
  }

  fun undo(currentProject: Project): Project? {
    if (undoStack.isEmpty()) return null
    val previous = undoStack.removeLast()
    redoStack.addLast(currentProject)
    return previous
  }

  fun redo(currentProject: Project): Project? {
    if (redoStack.isEmpty()) return null
    val next = redoStack.removeLast()
    undoStack.addLast(currentProject)
    return next
  }

  fun clear() {
    undoStack.clear()
    redoStack.clear()
  }
}
