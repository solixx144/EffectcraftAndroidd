package com.example.effectcraft.checklist

data class ParityFeature(
  val name: String,
  val desktopStatus: String,
  val androidStatus: String,
  val isComplete: Boolean,
  val note: String
)

data class ParityCategory(
  val title: String,
  val features: List<ParityFeature>
)

object ParityChecklist {

  val categories: List<ParityCategory> = listOf(
    ParityCategory(
      title = "1. Core Engine & Architecture",
      features = listOf(
        ParityFeature("Command Pattern ('Everything is a command')", "Active (crates/engine)", "Implemented (CommandRegistry)", true, "Unified command dispatching"),
        ParityFeature("Undo / Redo Transaction History", "Active", "Implemented (UndoRedoManager)", true, "Deep state snapshot undo stack"),
        ParityFeature("No-Crash Architecture (Safe Code)", "Active", "Implemented (Null-safe Kotlin)", true, "Robust boundary handling"),
        ParityFeature("Project Auto-Save Recovery", "Active", "Implemented (60s timer + JSON)", true, "Automatic background snapshot recovery"),
        ParityFeature("Dual License Preservation (MIT & Apache-2.0)", "Dual License", "Preserved in Root & App UI", true, "Full legal open-source compliance")
      )
    ),
    ParityCategory(
      title = "2. Composition & Timeline",
      features = listOf(
        ParityFeature("Custom Resolution (1080p, 4K, 720p)", "Active", "Supported", true, "Presets & custom dimensions"),
        ParityFeature("Configurable Frame Rates (24, 30, 60 fps)", "Active", "Supported", true, "Accurate frame-by-frame stepping"),
        ParityFeature("SMPTE Timecode Display & Scrubber", "Active", "Supported", true, "HH:MM:SS:FF format"),
        ParityFeature("Work Area In & Out Brackets", "Active", "Supported", true, "Work area duration clamping"),
        ParityFeature("Multi-track Timeline Dope Sheet", "Active (egui)", "Implemented (Compose M3)", true, "Touch-optimized dragging & scrubbing"),
        ParityFeature("Keyframe Graph Editor", "Active", "Implemented (Bezier Curves)", true, "Interactive curve manipulation")
      )
    ),
    ParityCategory(
      title = "3. Layer System & Compositing",
      features = listOf(
        ParityFeature("Solid Color Layers", "Active", "Supported", true, "Custom RGBA solid creation"),
        ParityFeature("Text Layers (Typography, Tracking, Styling)", "Active", "Supported", true, "Font sizes, tracking, alignment"),
        ParityFeature("Shape Layers (Rect, Ellipse, Star, Polygon)", "Active", "Supported", true, "Vector fills, strokes, corner radii"),
        ParityFeature("Footage Media Layers (Images & Videos)", "Active", "Supported (Photo Picker)", true, "Zero-permission Android picker"),
        ParityFeature("Camera 3D Layer", "Active", "Supported", true, "Focal length, zoom, depth of field"),
        ParityFeature("Light 3D Layer", "Active", "Supported", true, "Point, ambient, spot, intensity"),
        ParityFeature("Null Object Layer", "Active", "Supported", true, "Parent transform driver"),
        ParityFeature("Adjustment Layers", "Active", "Supported", true, "Global effect cascading"),
        ParityFeature("Audio Waveform Layer", "Active", "Supported", true, "Timeline audio track display"),
        ParityFeature("38 Blend Modes", "Active (crates/effects)", "Implemented (Compose & GPU)", true, "Multiply, Screen, Overlay, Add, etc."),
        ParityFeature("Track Mattes (Alpha, Luma, Inverted)", "Active", "Supported", true, "Layer masking channels")
      )
    ),
    ParityCategory(
      title = "4. Keyframing & Expressions",
      features = listOf(
        ParityFeature("Multi-Parameter Keyframing", "Active", "Supported", true, "Position, Scale, Rotation, Opacity, Effects"),
        ParityFeature("Interpolation (Linear, Bezier, Hold, Ease)", "Active", "Supported", true, "Easy Ease, Ease In, Ease Out"),
        ParityFeature("Expressions Engine: wiggle(freq, amp)", "Active (JavaScript)", "Implemented (Harmonic Math)", true, "Dynamic procedural motion"),
        ParityFeature("Expressions Engine: time * rate", "Active", "Implemented", true, "Continuous linear motion"),
        ParityFeature("Stopwatch Keyframe Toggles", "Active", "Supported", true, "One-tap keyframe enabling")
      )
    ),
    ParityCategory(
      title = "5. Visual Effects Catalog (40+ Effects)",
      features = listOf(
        ParityFeature("Blur & Sharpen (Gaussian, Fast, Directional, Radial)", "Active", "Supported (GPU Shaders)", true, "High performance blur"),
        ParityFeature("Color Correction (Brightness/Contrast, Hue/Sat, Levels)", "Active", "Supported (ColorMatrix)", true, "Precise tonal grading"),
        ParityFeature("Tint & Tritone Color Grading", "Active", "Supported", true, "Two-tone & three-tone mapping"),
        ParityFeature("Distort (Transform, Twirl, Wave Warp, Spherize)", "Active", "Supported", true, "Geometric warping"),
        ParityFeature("Generate (4-Color Gradient, Ramp, Checkerboard, Grid)", "Active", "Supported (Canvas GPU)", true, "Procedural texture generators"),
        ParityFeature("Stylize (Glow, Drop Shadow, Mosaic, Posterize, Vignette)", "Active", "Supported", true, "Luminescence & stylization"),
        ParityFeature("Transitions (Linear Wipe, Radial Wipe, Blinds)", "Active", "Supported", true, "Wipe and completion controls"),
        ParityFeature("Keying (Linear Color Key, Luma Key)", "Active", "Supported", true, "Chroma removal"),
        ParityFeature("Noise & Grain (Add Noise, Fractal Noise)", "Active", "Supported", true, "Film grain simulation"),
        ParityFeature("Time (Posterize Time, Echo)", "Active", "Supported", true, "Temporal effects")
      )
    ),
    ParityCategory(
      title = "6. Android Tablet UI & Hardware",
      features = listOf(
        ParityFeature("Samsung Galaxy Tab S10+ Layout", "N/A (Desktop)", "Custom Tablet Workstation", true, "Landscape multi-pane split layout"),
        ParityFeature("Samsung S-Pen & High Precision Touch", "N/A (Mouse/Key)", "Supported", true, "Pixel-accurate transform handle dragging"),
        ParityFeature("Pinch-to-Zoom Viewport Canvas", "N/A", "Supported (Touch Gesture)", true, "Smooth 2-finger pan & zoom"),
        ParityFeature("Action Safe & Title Safe Guides", "Active", "Supported", true, "Broadcast 90% & 80% guide overlays"),
        ParityFeature("Export Formats (MP4, GIF, PNG, Lottie)", "Active", "Supported (MediaCodec / JSON)", true, "Multi-format rendering queue"),
        ParityFeature("Android 16 & ARM64 v8a/v9a Optimization", "N/A", "Targeted (Dimensity 9300+)", true, "Native ARM64 architecture")
      )
    )
  )

  val totalFeatures: Int get() = categories.sumOf { it.features.size }
  val completedFeatures: Int get() = categories.sumOf { it.features.count { f -> f.isComplete } }
  val completionRatePercent: Int get() = ((completedFeatures.toDouble() / totalFeatures) * 100).toInt()
}
