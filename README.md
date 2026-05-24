<div align="center">

# Pathmind

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21--1.21.11-00AA00?style=for-the-badge&logo=minecraft)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Fabric-0.17.2%2B-DBD0B4?style=for-the-badge)](https://fabricmc.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-extra%20target-FF6B00?style=for-the-badge)](https://neoforged.net)
[![Java](https://img.shields.io/badge/Java-21+-FF6B6B?style=for-the-badge&logo=openjdk)](https://openjdk.java.net)
[![License](https://img.shields.io/badge/License-See%20LICENSE-lightgrey?style=for-the-badge)](LICENSE.txt)

A visual node editor for building Minecraft automation workflows.

Created by `soymods`.

English | Deutsch | Espanol | Francais | Polski | Portugues (BR) | Russian

### Download On Modrinth

<a href="https://modrinth.com/mod/pathmind">
  <img src="https://img.shields.io/badge/Modrinth-DOWNLOAD-00D5AA?style=for-the-badge&logo=modrinth&logoColor=white">
</a>

</div>

## What Pathmind Is

Pathmind lets you build automation with a visual graph instead of writing commands or scripts. You place nodes, connect them, configure parameters, then run the graph in-game.

The current mod includes:

- A visual editor for building automation graphs in-game.
- Workspace and preset management for saving and organizing your graphs.
- Runtime automation features for executing workflows and reacting to game state.
- Pathfinding and movement automation, with optional integrations for expanded behavior.
- An in-game marketplace for sharing and discovering community presets.
- HUD and editor feedback to help monitor execution and troubleshoot graphs.

## Feature Overview

### Visual Editor

- Full-screen graph editing built around nodes and connections.
- Tools for organizing, editing, and validating workflows.
- Customizable editor presentation and general usability settings.

### Nodes And Logic

- Node categories for flow control, world interaction, player actions, data handling, sensing, parameters, and reusable logic.
- Support for combining simple actions into larger automation workflows.
- Reusable graph structures for building modular systems.

### Execution And Runtime

- Run graphs directly in-game and monitor what they are doing.
- Build workflows that respond to events, conditions, and changing state.
- Use runtime state and feedback overlays while automation is active.

### Navigation And Pathfinding

Pathmind ships with its own local movement backend and also supports Baritone-aware nodes.

- Built-in movement and pathfinding support for navigation-focused automation.
- Optional Baritone integration for players who want expanded navigation behavior.
- Visual feedback for navigation state while workflows are running.

### Marketplace

The in-game marketplace is more than a static browser:

- Browse shared presets from inside the mod.
- Import community presets into your own workspace.
- Publish and manage your own presets through the in-game UI.

## Controls

Default keybinds:

- Open editor: `Right Alt`
- Play graphs: `K`
- Stop graphs: `J`

Pathmind also adds main-menu integration so the editor is reachable before joining a world.

## Installation

### Required

- Minecraft `1.21` - `1.21.11`
- Fabric Loader `0.17.2` or newer, or NeoForge for your selected Minecraft version
- Architectury API matching your Minecraft version and loader
- Matching Fabric API for Fabric installs
- Java `21+`

### Optional

- Baritone API mod for Baritone-backed nodes and extended navigation/building integration
- UI Utils for UI automation nodes and related integrations

### Steps

1. Install Fabric or NeoForge for your selected Minecraft version.
2. Install Architectury API for your loader. Fabric installs also need Fabric API.
3. Download the matching Pathmind jar from Modrinth.
4. Place the jar in your `mods` folder.
5. Launch the game and open Pathmind with the configured keybind.

## Workspace Files

Pathmind stores data inside your Minecraft directory under `pathmind/`.

- `pathmind/presets/`: saved workspace graphs
- `pathmind/active_preset.txt`: current preset selection
- `pathmind/settings.json`: user settings
- `pathmind/marketplace_auth.json`: marketplace session data

Imported marketplace presets and exported graphs also flow through this preset system.

## Compatibility

- Fabric is the primary loader target. NeoForge is supported as an additional loader target.
- Release jars are versioned as `pathmind-fabric-<modVersion>+mc<version>-fabric.jar` and `pathmind-<modVersion>+mc<version>-neoforge.jar`.
- This port compiles for Minecraft `1.21` through `1.21.11` on Fabric and NeoForge.
- Multiple language files are included.
- Marketplace listings include version compatibility metadata.

## Development

### Build From Source

```bash
git clone https://github.com/soymods/pathmind.git
cd pathmind
./gradlew build
```

The default build produces both loader jars:

- Fabric: `fabric/build/libs/`
- NeoForge: `build/libs/`

To build only the primary Fabric jar:

```bash
./gradlew buildFabric
```

To build only the NeoForge jar:

```bash
./gradlew buildNeoForge
```

To compile for a specific supported Minecraft `1.21.x` target, pass `mc_version`:

```bash
./gradlew clean build -Pmc_version=1.21.11
```

On Windows PowerShell, quote the property:

```powershell
.\gradlew.bat clean build "-Pmc_version=1.21.11"
```

This also works with single-loader builds:

```bash
./gradlew :fabric:build -Pmc_version=1.21.11
```

### Run In Dev

Fabric is the default development loader:

```bash
./gradlew runClient
```

The explicit Fabric client task is also available:

```bash
./gradlew :fabric:runClient
```

NeoForge dev runs are available as extra targets:

```bash
./gradlew runNeoForgeClient
```

### Supported Build Targets

`1.21` - `1.21.11`

To build every supported Minecraft version for both loaders:

```bash
./gradlew buildAllTargets
```

## Version Information

| Component | Version |
|-----------|---------|
| Mod Version | `1.1.4` |
| Supported Minecraft Versions | `1.21` - `1.21.11` |
| Fabric Loader | `0.17.2+` |
| NeoForge | Matching Minecraft version |
| Fabric API | Matching Minecraft version |
| Architectury API | Matching Minecraft version and loader |
| Java | `21+` |

## Release Readiness

Use [`RELEASE_GATE.md`](RELEASE_GATE.md) before promoting a build.

## License

This project is distributed under the custom **Pathmind License (All Rights Reserved)** in [`LICENSE.txt`](LICENSE.txt).

In short:

- Redistribution, modification, or re-uploading is not allowed without explicit written permission.
- Videos featuring the mod are allowed, including monetized videos.
- Modpack inclusion is allowed under the limits described in the license.
- The mod is provided as-is without warranty.

## Support And Feedback

- Issues: [GitHub Issues](https://github.com/soymods/pathmind/issues)
- Downloads: [Modrinth](https://modrinth.com/mod/pathmind)
- Community: [Discord](https://discord.gg/7nGRX2d8a6)

## Acknowledgments

- NeoForge, Fabric, and Architectury for the loader/tooling stack
- Blender and Scratch for helping inspire the node-based workflow direction
