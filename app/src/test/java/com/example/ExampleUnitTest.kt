package com.example

import com.example.effectcraft.effects.EffectCatalog
import com.example.effectcraft.engine.UndoRedoManager
import com.example.effectcraft.model.AnimatableProperty
import com.example.effectcraft.model.InterpolationMode
import com.example.effectcraft.model.Keyframe
import com.example.effectcraft.storage.ProjectTemplates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testKeyframeLinearInterpolation() {
    val prop = AnimatableProperty(
      name = "Opacity",
      baseValue = 0f,
      isKeyframing = true,
      keyframes = listOf(
        Keyframe(0, 0f, InterpolationMode.LINEAR),
        Keyframe(10, 100f, InterpolationMode.LINEAR)
      )
    )
    assertEquals(0f, prop.getValueAt(0), 0.01f)
    assertEquals(50f, prop.getValueAt(5), 0.01f)
    assertEquals(100f, prop.getValueAt(10), 0.01f)
  }

  @Test
  fun testWiggleExpressionEvaluation() {
    val prop = AnimatableProperty(
      name = "Position Y",
      baseValue = 500f,
      expression = "wiggle(5, 20)"
    )
    val v1 = prop.getValueAt(10)
    val v2 = prop.getValueAt(11)
    assertTrue(v1 in 470f..530f)
    assertTrue(v2 in 470f..530f)
  }

  @Test
  fun testUndoRedoTransactionManager() {
    val undoRedo = UndoRedoManager()
    val proj1 = ProjectTemplates.createNeonKineticTitle()
    val proj2 = proj1.copy(name = "Modified Project")

    undoRedo.recordState(proj1)
    assertTrue(undoRedo.canUndo)

    val undone = undoRedo.undo(proj2)
    assertNotNull(undone)
    assertEquals(proj1.name, undone?.name)
    assertTrue(undoRedo.canRedo)
  }

  @Test
  fun testEffectCatalogParity() {
    assertTrue(EffectCatalog.allSpecs.size >= 25)
    assertNotNull(EffectCatalog.getSpec("glow"))
    assertNotNull(EffectCatalog.getSpec("gaussian_blur"))
    assertNotNull(EffectCatalog.getSpec("brightness_contrast"))
  }
}
