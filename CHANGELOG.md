# Changelog

All notable changes to SimpleFPS will be documented in this file.

## [1.3.1] - 2025-12-19

### Fixed
- **Time Clock synchronization** - Fixed time display not syncing correctly with game time
  - Now uses `world.getTimeOfDay()` directly for accurate real-time sync
  - Time updates correctly after sleeping, closing/reopening game, and using `/time set` commands

## [1.3.0] - 2025-01-13

### Added
- **Time Clock Display** - New HUD element showing Minecraft day number and time
  - Synced with Minecraft day/night cycle
  - 12-hour format (7:30 AM) by default with 24-hour toggle (07:30)
  - Verbose ("Day: 1342 Time: 7:30 AM") and minimalist ("1342 7:30 AM") display modes
  - Customizable text and background colors with color picker
  - Draggable positioning like other HUD elements
  - Full integration with Cloth Config and fallback config screens

### Changed
- Config tabs reorganized: FPS, Coordinates, Biome, Time Clock, Graph, Adaptive, Keybinds

### Fixed
- **HUD position scaling** - Fixed HUD elements overlapping when resizing the Minecraft window
  - Elements now use anchor-based positioning (top-left stays top-left, bottom-right stays bottom-right)
  - Maintains proper spacing regardless of window size
- **Day counter** - Fixed day number always showing as 1 (now correctly tracks total days played)

## [1.2.1] - 2024-12-17

### Added
- **Multi-version Support** - Now supports Minecraft 1.21.9, 1.21.10, and 1.21.11
- **Coordinates Display** - Show X/Y/Z position on screen with customizable colors and position
- **Biome Display** - Show current biome name with customizable colors and position
- **Cardinal Direction** - Optional compass direction indicator on FPS counter
- **Tooltip Improvements** - All config tooltips now show default values and valid ranges

### Changed
- **Fabric Loader requirement relaxed** - Now requires >=0.16.0 instead of >=0.18.2 for broader compatibility
- **Cloth Config sliders → text fields** - Replaced sliders with text input fields (workaround for Cloth Config slider bug)
- **Config tabs reorganized** - FPS, Coordinates, Biome, Graph, Adaptive, Keybinds

### Fixed
- Color picker values now save correctly in Cloth Config
- Tab order matches logical grouping of settings
- Build configuration fix for Stonecutter source management

### Technical
- Added Stonecutter 0.5.1 for multi-version build management
- Converted from Groovy to Kotlin DSL build scripts
- Backup of original slider implementation preserved for future restoration

## [1.1.0] - 2024-12-11

### Changed
- **Minecraft 1.21.11** - Updated to support Minecraft 1.21.11
- **Cloth Config & Mod Menu are now optional** - Only Fabric API is required
- Updated Fabric Loader to 0.18.2
- Updated Fabric API to 0.139.4+1.21.11
- Updated Loom to 1.14-SNAPSHOT
- Updated Gradle to 9.2.1

### Added
- **Built-in Config Screen** - Native Minecraft config screen with sliders, tooltips, and reset buttons (used when Cloth Config is not installed)
- **Interactive Scrollbar** - Click to jump or drag to scroll through config options
- **Config Keybind** - Opens Cloth Config screen if installed, otherwise opens built-in config
- Automatic detection of Cloth Config at runtime
- Scroll indicators show when more content is available

### Fixed
- **Scrollable Fallback Config** - Config screen now scrolls and adapts to any window size
- Fixed config widgets overlapping bottom buttons on small Minecraft windows

### Technical
- Cloth Config changed from `modImplementation` to `modCompileOnly`
- Mod Menu integration works with or without Cloth Config
- All keybinds remain unbound by default
- Improved widget positioning in fallback config (uses manual scroll offset)
- Bottom buttons (Save & Close, Key Binds, Cancel) are now fixed at the bottom

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
