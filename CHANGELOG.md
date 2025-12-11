# Changelog

All notable changes to SimpleFPS will be documented in this file.

## [1.1.1] - 2024-12-11

### Fixed
- **Scrollable Fallback Config** - Config screen now scrolls and adapts to any window size
- Fixed config widgets overlapping bottom buttons on small Minecraft windows

### Added
- **Interactive Scrollbar** - Click to jump or drag to scroll through config options
- Scroll indicators show when more content is available

### Changed
- Improved widget positioning in fallback config (uses manual scroll offset)
- Bottom buttons (Save & Close, Key Binds, Cancel) are now fixed at the bottom

## [1.1.0] - 2024-12-10

### Changed
- **Minecraft 1.21.11** - Updated to support Minecraft 1.21.11
- **Cloth Config & Mod Menu are now optional** - Only Fabric API is required
- Updated Fabric Loader to 0.18.2
- Updated Fabric API to 0.139.4+1.21.11
- Updated Loom to 1.14-SNAPSHOT
- Updated Gradle to 9.2.1

### Added
- **Built-in Config Screen** - Native Minecraft config screen with sliders, tooltips, and reset buttons (used when Cloth Config is not installed)
- **Config Keybind** - Opens Cloth Config screen if installed, otherwise opens built-in config
- Automatic detection of Cloth Config at runtime

### Technical
- Cloth Config changed from `modImplementation` to `modCompileOnly`
- Mod Menu integration works with or without Cloth Config
- All keybinds remain unbound by default

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
