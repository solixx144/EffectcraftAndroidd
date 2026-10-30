package com.example.effectcraft.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.effectcraft.model.AnimatableProperty
import com.example.effectcraft.model.AppliedEffect
import com.example.effectcraft.model.Composition
import com.example.effectcraft.model.Layer
import com.example.effectcraft.model.LayerBlendMode
import com.example.effectcraft.model.LayerTransform
import com.example.effectcraft.model.LayerType
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectorPanel(
  viewModel: EffectCraftViewModel,
  activeComp: Composition,
  modifier: Modifier = Modifier
) {
  val selectedLayer = activeComp.getSelectedLayer()
  var selectedTab by remember { mutableIntStateOf(0) }

  Surface(
    modifier = modifier
      .width(280.dp)
      .fillMaxHeight(),
    color = StudioSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder)
  ) {
    if (selectedLayer == null) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "No Layer Selected\nTap a layer in the timeline or canvas to inspect.",
          color = TextMuted,
          fontSize = 12.sp,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
      }
    } else {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Selected Layer Header
        LayerInspectorHeader(viewModel, selectedLayer)

        // Inspector Tabs
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = StudioSurfaceElevated,
          contentColor = TextPrimary,
          indicator = { tabPositions ->
            TabRowDefaults.PrimaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = AccentCyan
            )
          }
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Transform", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            icon = { Icon(Icons.Default.Transform, contentDescription = null, modifier = Modifier.size(14.dp)) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Content", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp)) }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            text = { Text("Effects (${selectedLayer.effects.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp)) }
          )
        }

        when (selectedTab) {
          0 -> TransformTabContent(viewModel, selectedLayer, activeComp.currentFrame, activeComp.frameRate)
          1 -> ContentTabContent(viewModel, selectedLayer)
          2 -> EffectControlsTabContent(viewModel, selectedLayer, activeComp.currentFrame, activeComp.frameRate)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayerInspectorHeader(viewModel: EffectCraftViewModel, layer: Layer) {
  var blendMenuExpanded by remember { mutableStateOf(false) }

  Surface(
    color = StudioSurfaceElevated,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(RoundedCornerShape(3.dp))
              .background(Color(layer.type.defaultColor))
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = layer.name,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontSize = 13.sp,
            maxLines = 1
          )
        }

        IconButton(
          onClick = { viewModel.deleteSelectedLayer() },
          modifier = Modifier.size(24.dp)
        ) {
          Icon(Icons.Default.Delete, contentDescription = "Delete Layer", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Blend Mode Selector
        ExposedDropdownMenuBox(
          expanded = blendMenuExpanded,
          onExpandedChange = { blendMenuExpanded = !blendMenuExpanded },
          modifier = Modifier.weight(1f)
        ) {
          OutlinedTextField(
            value = layer.blendMode.displayName,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = blendMenuExpanded) },
            modifier = Modifier
              .menuAnchor()
              .height(40.dp)
              .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AccentCyan,
              unfocusedBorderColor = StudioSurfaceBorder,
              focusedContainerColor = StudioBackground,
              unfocusedContainerColor = StudioBackground,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
          )

          ExposedDropdownMenu(
            expanded = blendMenuExpanded,
            onDismissRequest = { blendMenuExpanded = false },
            modifier = Modifier.background(StudioSurfaceElevated)
          ) {
            LayerBlendMode.values().take(12).forEach { mode ->
              DropdownMenuItem(
                text = { Text(mode.displayName, fontSize = 11.sp, color = TextPrimary) },
                onClick = {
                  viewModel.setLayerBlendMode(layer.id, mode)
                  blendMenuExpanded = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 3D Switch
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "3D", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (layer.is3D) AccentCyan else TextMuted)
          Spacer(modifier = Modifier.width(4.dp))
          Switch(
            checked = layer.is3D,
            onCheckedChange = { viewModel.toggleLayer3D(layer.id) },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.Black,
              checkedTrackColor = AccentCyan
            ),
            modifier = Modifier.size(32.dp)
          )
        }
      }
    }
  }
}

@Composable
fun TransformTabContent(
  viewModel: EffectCraftViewModel,
  layer: Layer,
  currentFrame: Int,
  fps: Int
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxWidth()
      .padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Position X & Y
    item {
      PropertySliderRow(
        name = "Position X",
        prop = layer.transform.positionX,
        currentValue = layer.transform.positionX.getValueAt(currentFrame, fps),
        onValueChange = { newVal ->
          viewModel.updateLayerTransformProperty(layer.id) { t ->
            t.copy(positionX = t.positionX.copy(baseValue = newVal))
          }
        },
        onToggleKeyframe = {
          viewModel.togglePropertyKeyframing(layer.id, "Position X", { it.positionX }, { t, p -> t.copy(positionX = p) })
        }
      )
    }

    item {
      PropertySliderRow(
        name = "Position Y",
        prop = layer.transform.positionY,
        currentValue = layer.transform.positionY.getValueAt(currentFrame, fps),
        onValueChange = { newVal ->
          viewModel.updateLayerTransformProperty(layer.id) { t ->
            t.copy(positionY = t.positionY.copy(baseValue = newVal))
          }
        },
        onToggleKeyframe = {
          viewModel.togglePropertyKeyframing(layer.id, "Position Y", { it.positionY }, { t, p -> t.copy(positionY = p) })
        }
      )
    }

    // Scale X & Y
    item {
      PropertySliderRow(
        name = "Scale",
        prop = layer.transform.scaleX,
        currentValue = layer.transform.scaleX.getValueAt(currentFrame, fps),
        onValueChange = { newVal ->
          viewModel.updateLayerTransformProperty(layer.id) { t ->
            t.copy(
              scaleX = t.scaleX.copy(baseValue = newVal),
              scaleY = t.scaleY.copy(baseValue = newVal)
            )
          }
        },
        onToggleKeyframe = {
          viewModel.togglePropertyKeyframing(layer.id, "Scale", { it.scaleX }, { t, p -> t.copy(scaleX = p, scaleY = p) })
        }
      )
    }

    // Rotation
    item {
      PropertySliderRow(
        name = "Rotation",
        prop = layer.transform.rotation,
        currentValue = layer.transform.rotation.getValueAt(currentFrame, fps),
        onValueChange = { newVal ->
          viewModel.updateLayerTransformProperty(layer.id) { t ->
            t.copy(rotation = t.rotation.copy(baseValue = newVal))
          }
        },
        onToggleKeyframe = {
          viewModel.togglePropertyKeyframing(layer.id, "Rotation", { it.rotation }, { t, p -> t.copy(rotation = p) })
        }
      )
    }

    // Opacity
    item {
      PropertySliderRow(
        name = "Opacity",
        prop = layer.transform.opacity,
        currentValue = layer.transform.opacity.getValueAt(currentFrame, fps),
        onValueChange = { newVal ->
          viewModel.updateLayerTransformProperty(layer.id) { t ->
            t.copy(opacity = t.opacity.copy(baseValue = newVal))
          }
        },
        onToggleKeyframe = {
          viewModel.togglePropertyKeyframing(layer.id, "Opacity", { it.opacity }, { t, p -> t.copy(opacity = p) })
        }
      )
    }
  }
}

@Composable
fun PropertySliderRow(
  name: String,
  prop: AnimatableProperty,
  currentValue: Float,
  onValueChange: (Float) -> Unit,
  onToggleKeyframe: () -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onToggleKeyframe,
          modifier = Modifier.size(20.dp)
        ) {
          Icon(
            Icons.Default.Timer,
            contentDescription = "Stopwatch Keyframe",
            tint = if (prop.isKeyframing) AccentCyan else TextMuted,
            modifier = Modifier.size(15.dp)
          )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = name, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
      }

      Text(
        text = "%.1f".format(currentValue),
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = if (prop.isKeyframing) AccentAmber else AccentCyan,
        fontWeight = FontWeight.Bold
      )
    }

    Slider(
      value = currentValue.coerceIn(prop.minValue, prop.maxValue),
      onValueChange = onValueChange,
      valueRange = prop.minValue..prop.maxValue,
      colors = SliderDefaults.colors(
        thumbColor = if (prop.isKeyframing) AccentAmber else AccentCyan,
        activeTrackColor = if (prop.isKeyframing) AccentAmber else AccentCyan,
        inactiveTrackColor = StudioSurfaceBorder
      ),
      modifier = Modifier.height(26.dp)
    )
  }
}

@Composable
fun ContentTabContent(viewModel: EffectCraftViewModel, layer: Layer) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(12.dp)
  ) {
    when (layer.type) {
      LayerType.TEXT -> {
        val td = layer.textData
        if (td != null) {
          Text("Text String", fontSize = 11.sp, color = TextMuted)
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = td.text, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(10.dp))
          Text("Font Size: ${td.fontSize.toInt()}sp", fontSize = 12.sp, color = TextSecondary)
          Text("Letter Tracking: ${td.tracking}", fontSize = 12.sp, color = TextSecondary)
        }
      }
      LayerType.SHAPE -> {
        val sd = layer.shapeData
        if (sd != null) {
          Text("Shape Geometry", fontSize = 11.sp, color = TextMuted)
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "Type: ${sd.shapeType.name}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
          Text(text = "Dimensions: ${sd.width.toInt()} × ${sd.height.toInt()} px", fontSize = 12.sp, color = TextSecondary)
          Text(text = "Corner Radius: ${sd.cornerRadius.toInt()} px", fontSize = 12.sp, color = TextSecondary)
        }
      }
      LayerType.SOLID -> {
        Text("Solid Properties", fontSize = 11.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Dimensions: ${layer.solidWidth} × ${layer.solidHeight}", fontSize = 12.sp, color = TextSecondary)
      }
      else -> {
        Text("Layer Content Inspector", fontSize = 12.sp, color = TextMuted)
      }
    }
  }
}

@Composable
fun EffectControlsTabContent(
  viewModel: EffectCraftViewModel,
  layer: Layer,
  currentFrame: Int,
  fps: Int
) {
  if (layer.effects.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "No effects applied.\nOpen the Effects tab on the left to add one.",
        fontSize = 11.sp,
        color = TextMuted,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )
    }
  } else {
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(layer.effects) { effect ->
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = StudioSurfaceElevated,
          border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                  checked = effect.isEnabled,
                  onCheckedChange = { viewModel.toggleEffectEnabled(layer.id, effect.id) },
                  colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = AccentCyan
                  ),
                  modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = effect.displayName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = if (effect.isEnabled) TextPrimary else TextMuted
                )
              }

              IconButton(
                onClick = { viewModel.removeEffect(layer.id, effect.id) },
                modifier = Modifier.size(20.dp)
              ) {
                Icon(Icons.Default.Delete, contentDescription = "Remove Effect", tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp))
              }
            }

            // Effect Parameters Sliders
            for ((paramKey, paramProp) in effect.parameters) {
              val curVal = paramProp.getValueAt(currentFrame, fps)
              Column(modifier = Modifier.padding(top = 6.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = paramProp.name, fontSize = 10.sp, color = TextSecondary)
                  Text(text = "%.1f".format(curVal), fontSize = 10.sp, color = AccentCyan, fontFamily = FontFamily.Monospace)
                }
                Slider(
                  value = curVal.coerceIn(paramProp.minValue, paramProp.maxValue),
                  onValueChange = { newVal ->
                    viewModel.updateEffectParam(layer.id, effect.id, paramKey, newVal)
                  },
                  valueRange = paramProp.minValue..paramProp.maxValue,
                  colors = SliderDefaults.colors(
                    thumbColor = AccentCyan,
                    activeTrackColor = AccentCyan,
                    inactiveTrackColor = StudioSurfaceBorder
                  ),
                  modifier = Modifier.height(24.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
