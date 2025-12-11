# SimpleFPS

A lightweight, customizable FPS counter mod for Minecraft (Fabric 1.21.11).

## Features

- **FPS Counter** - Real-time FPS display with customizable appearance
- **FPS Graph** - Visual FPS history with Min/Max/Avg statistics
- **Draggable UI** - Position both counter and graph anywhere on screen
- **Color Picker** - Built-in visual color picker for easy customization
- **Adaptive Colors** - Optional color-coding based on FPS thresholds
- **Live Preview** - See changes in real-time while configuring
- **Built-in Config Screen** - Scrollable fallback config with draggable scrollbar

## Installation

1. Install [Fabric Loader](https://fabricmc.net/) for Minecraft 1.21.11
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) (required)
3. Download SimpleFPS and place in your `mods` folder

### Recommended (Optional)

- [Cloth Config](https://modrinth.com/mod/cloth-config) - Enhanced config screen with live preview
- [Mod Menu](https://modrinth.com/mod/modmenu) - Access config from the mod list

*Without these, set a keybind in Options > Controls > SimpleFPS to open the built-in config screen.*

## Configuration

Access settings via:
- **Mod Menu** (if installed) - Click the config button
- **Keybind** - Set "Open Config" in Options > Controls > SimpleFPS
- **Config file** - Edit `.minecraft/config/simplefps.json` directly

### Keybindings

All keybinds are unbound by default. Set them in **Options > Controls > SimpleFPS**:

| Action | Description |
|--------|-------------|
| Toggle FPS | Show/hide FPS counter |
| Open Config | Open settings screen |
| Drag FPS Counter | Reposition the counter |
| Drag FPS Graph | Reposition the graph |
| Reload Config | Reload settings from file |

## Requirements

| Dependency | Version | Required |
|------------|---------|----------|
| Minecraft | 1.21.11 | ✅ |
| Fabric Loader | ≥0.18.0 | ✅ |
| Fabric API | Any | ✅ |
| Cloth Config | ≥20.0.0 | ❌ Optional |
| Mod Menu | ≥17.0.0 | ❌ Optional |

## License

[MIT License](LICENSE)

## Author

**Partacus-SPQR**
