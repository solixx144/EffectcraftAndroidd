package com.example.effectcraft.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.effectcraft.effects.EffectCatalog
import com.example.effectcraft.effects.EffectCategory
import com.example.effectcraft.model.Composition
import com.example.effectcraft.model.ShapeType
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
fun ProjectBinPanel(
  viewModel: EffectCraftViewModel,
  activeComp: Composition,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) }

  Surface(
    modifier = modifier
      .width(260.dp)
      .fillMaxHeight(),
    color = StudioSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Panel Tabs: Project Bin vs Effects
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
          text = { Text("Project Bin", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
          icon = { Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Effects", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
          icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
      }

      if (selectedTab == 0) {
        ProjectBinTabContent(viewModel, activeComp)
      } else {
        EffectsLibraryTabContent(viewModel, activeComp)
      }
    }
  }
}

@Composable
fun ProjectBinTabContent(viewModel: EffectCraftViewModel, activeComp: Composition) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(8.dp)
  ) {
    // Quick Add Layer Section
    Text(
      text = "ADD NEW LAYER",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = TextMuted,
      letterSpacing = 1.sp,
      modifier = Modifier.padding(vertical = 4.dp)
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      AddLayerButton(
        label = "Solid",
        icon = Icons.Default.CropSquare,
        color = Color(0xFFE53935),
        onClick = { viewModel.openNewSolidDialog() },
        modifier = Modifier.weight(1f)
      )
      AddLayerButton(
        label = "Text",
        icon = Icons.Default.TextFields,
        color = Color(0xFFE040FB),
        onClick = { viewModel.openNewTextDialog() },
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      AddLayerButton(
        label = "Shape",
        icon = Icons.Default.Category,
        color = Color(0xFF00E5FF),
        onClick = { viewModel.addShapeLayer(ShapeType.ROUNDED_RECT) },
        modifier = Modifier.weight(1f)
      )
      AddLayerButton(
        label = "Null",
        icon = Icons.Default.Layers,
        color = Color(0xFF78909C),
        onClick = { viewModel.addNullObjectLayer() },
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      AddLayerButton(
        label = "Adjustment",
        icon = Icons.Default.Filter,
        color = Color(0xFF26A69A),
        onClick = { viewModel.addAdjustmentLayer() },
        modifier = Modifier.weight(1f)
      )
      AddLayerButton(
        label = "3D Camera",
        icon = Icons.Default.CameraAlt,
        color = Color(0xFFFFB800),
        onClick = { viewModel.addNullObjectLayer() },
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Active Composition Info Card
    Text(
      text = "COMPOSITION INFO",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = TextMuted,
      letterSpacing = 1.sp,
      modifier = Modifier.padding(bottom = 6.dp)
    )

    Surface(
      color = StudioSurfaceElevated,
      shape = RoundedCornerShape(8.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        Text(text = activeComp.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "${activeComp.width} × ${activeComp.height} @ ${activeComp.frameRate}fps",
          fontSize = 12.sp,
          color = AccentCyan
        )
        Text(
          text = "Duration: ${activeComp.durationFrames} frames (${activeComp.durationTimecode.formatted()})",
          fontSize = 12.sp,
          color = TextSecondary
        )
        Text(
          text = "Layers: ${activeComp.layers.size} total",
          fontSize = 12.sp,
          color = TextMuted
        )
      }
    }
  }
}

@Composable
fun AddLayerButton(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .height(38.dp)
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(6.dp),
    color = StudioSurfaceElevated,
    border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(color)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary
      )
    }
  }
}

@Composable
fun EffectsLibraryTabContent(viewModel: EffectCraftViewModel, activeComp: Composition) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf<EffectCategory?>(null) }

  val filteredEffects = remember(searchQuery, selectedCategory) {
    EffectCatalog.allSpecs.filter { spec ->
      (selectedCategory == null || spec.category == selectedCategory) &&
        (searchQuery.isBlank() || spec.name.contains(searchQuery, ignoreCase = true) ||
          spec.description.contains(searchQuery, ignoreCase = true))
    }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(8.dp)
  ) {
    // Search Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
      placeholder = { Text("Search 40+ effects...", fontSize = 12.sp, color = TextMuted) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
      singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AccentCyan,
        unfocusedBorderColor = StudioSurfaceBorder,
        focusedContainerColor = StudioBackground,
        unfocusedContainerColor = StudioBackground,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
      ),
      shape = RoundedCornerShape(8.dp)
    )

    Spacer(modifier = Modifier.height(6.dp))

    // Category Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      item {
        FilterChip(
          selected = selectedCategory == null,
          onClick = { selectedCategory = null },
          label = { Text("All", fontSize = 10.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AccentCyan,
            selectedLabelColor = Color.Black
          )
        )
      }
      items(EffectCategory.values()) { cat ->
        FilterChip(
          selected = selectedCategory == cat,
          onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
          label = { Text(cat.displayName, fontSize = 10.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AccentCyan,
            selectedLabelColor = Color.Black
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // List of Effects
    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      items(filteredEffects) { spec ->
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = StudioSurfaceElevated,
          border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(text = spec.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
              Text(text = spec.category.displayName, color = AccentCyan, fontSize = 10.sp)
              Text(
                text = spec.description,
                color = TextMuted,
                fontSize = 10.sp,
                maxLines = 1
              )
            }

            Button(
              onClick = { viewModel.applyEffectToSelectedLayer(spec.id) },
              colors = ButtonDefaults.buttonColors(
                containerColor = AccentCyan,
                contentColor = Color.Black
              ),
              shape = RoundedCornerShape(4.dp),
              modifier = Modifier
                .height(26.dp)
                .padding(start = 6.dp)
            ) {
              Text("+ Apply", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
