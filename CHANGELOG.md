# Changelog

All notable changes to SimpleFPS will be documented in this file.

## [1.0.0] - 2024-12-06

### Added
- **FPS Counter** - Real-time FPS display
- **FPS Graph** - Visual FPS history with Min/Max/Avg statistics
- **Draggable UI** - Drag both counter and graph to any screen position
- **Color Picker** - Built-in visual color picker with HSV wheel, RGB sliders, and presets
- **Adaptive Colors** - Optional color-coding based on FPS thresholds (Red/Yellow/Green)
- **Live Preview** - See changes in real-time while in config screen
- **Customization Options:**
  - Text color (hex or color picker)
  - Text size (50% - 400%)
  - Text opacity (0% - 100%)
  - Background toggle, color, and opacity
  - Show/hide "FPS" label
- **Graph Options:**
  - Enable/disable graph
  - Show/hide background and border
  - Adjustable scale (50% - 200%)
  - Separate threshold settings for graph colors
- **Keybindings** (all unbound by default):
  - Toggle FPS counter
  - Open config screen
  - Drag FPS counter
  - Drag FPS graph
  - Reload config from file

### Technical
- Built for Minecraft 1.21.10 (Fabric)
- Requires Fabric API and Cloth Config
- Optional Mod Menu integration
