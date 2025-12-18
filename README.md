# SimpleFPS

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.9--1.21.11-green)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Mod%20Loader-Fabric-blue)](https://fabricmc.net)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.2.0-orange)](https://modrinth.com/project/simplefps)

A lightweight, customizable FPS counter mod for Minecraft (Fabric).

**Author:** Partacus-SPQR  
**Source:** [GitHub](https://github.com/Partacus-SPQR/SimpleFPS)  
**Download:** [Modrinth](https://modrinth.com/project/simplefps)

## Features

- **FPS Counter** - Real-time FPS display with customizable appearance
- **Coordinates Display** - Show X/Y/Z position on screen
- **Biome Display** - Show current biome name
- **FPS Graph** - Visual FPS history with Min/Max/Avg statistics
- **Draggable UI** - Position all HUD elements anywhere on screen
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
| Minecraft | 1.21.9 - 1.21.11 | Yes |
| Fabric Loader | ≥0.18.2 | Yes |
| Fabric API | Any | Yes |
| Cloth Config | Any | Optional |
| Mod Menu | Any | Optional |

## Version Compatibility

| Minecraft | Mod Version | Fabric Loader |
|-----------|-------------|---------------|
| 1.21.11 | 1.2.0 | ≥0.18.2 |
| 1.21.10 | 1.2.0 | ≥0.18.2 |
| 1.21.9 | 1.2.0 | ≥0.18.2 |

## License

[MIT License](LICENSE)

## Author

**Partacus-SPQR**
