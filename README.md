<div align="center">
  <img src="https://github.com/staFF6773/YukiClient/blob/main/src/main/resources/assets/yukiclient/yuki.png?raw=true" width="120" alt="YukiCLient" />
  <h1>YukiClient</h1>

A clean PvP client for **Minecraft 1.8.9**.

Built on top of Forge, YukiClient adds lightweight HUD modules and PvP behavior tweaks without cluttering the vanilla experience.

---

## Features

### HUD Modules
- **FPS Counter** — Shows your current frames per second.
- **CPS Counter** — Tracks your left/right clicks per second.
- **Keystrokes** — Displays pressed movement and mouse keys.
- **Armor Status** — Shows your equipped armor and its durability.
- **Coordinates** — Shows your current X / Y / Z position and the direction you are facing.
- **Ping** — Shows your latency (ms) to the current server.
- **Speed** — Shows your horizontal speed in blocks per second (BPS).
- **Potion Status** — Lists your active potion effects with level and remaining time.
- **Clock** — Shows the current real-world time (HH:mm:ss).
- **Session Timer** — Shows how long your current play session has lasted.
- **Biome** — Shows the biome you are currently standing in.

### Behavior Modules
- **Freelook** — Hold `Left Alt` for a 360° third-person camera. Your real aim and movement direction stay locked, so you can look around freely without changing where you walk or what the server sees.
- **Sprint** — Keeps your sprint active automatically.
- **Zoom** — Hold `C` to smoothly zoom in with reduced mouse sensitivity.
- **FullBright** — Removes darkness without requiring torches or potions.
- **Toggle Sprint** — Sprints automatically while you move forward, so you never have to hold the sprint key.

### Client Badges
- **Client Badges** — Shows the YukiClient snowflake logo on players using the client: before the name in the tab list and beside the name in the nametag. The nametag badge is always visible in third person.
  - Your own badge is shown on any server or single-player world.
  - To see badges of **other** YukiClient players, the server must also run the YukiClient mod (for example, a LAN world or a dedicated Forge server with the mod in its `mods/` folder).
  - Can be toggled on/off in the ClickGUI.

### ClickGUI & HUD Editor
Press `Right Shift` in-game to open the **ClickGUI**, where you can toggle every module on or off. From there, click **Edit HUD Positions** to open the draggable **HUD Editor**: move, resize (scroll or drag a corner), snap to a grid, and use right-click options. Your layout and toggles are saved automatically.

### Controls
| Key | Action |
| --- | --- |
| `Right Shift` | Open the ClickGUI (module menu) |
| `Left Alt` (hold) | Freelook camera |
| `Left Control` (hold) | Sprint |
| `C` (hold) | Zoom |

Inside the HUD Editor: `Drag` to move, `Scroll` / corner-drag to resize, `Shift` to snap, `Ctrl + Shift + Scroll` for global scale, `R` to reset, `Delete` to hide, `G` to toggle the grid, `Esc` to exit.

---

## Requirements

- Minecraft 1.8.9
- Minecraft Forge 1.8.9 (`11.15.1.2318` or compatible)
- Java 8

---

## Building

This project uses **ForgeGradle 2.1**, so it must be built with **Gradle 2.9**. If it's not already present, download and extract it in the project root:

```bash
wget https://services.gradle.org/distributions/gradle-2.9-bin.zip
unzip gradle-2.9-bin.zip
```

### First-time setup

Run the decompilation workspace setup once before the first build:

```bash
./gradle-2.9/bin/gradle setupDecompWorkspace
```

### Build the mod

```bash
./gradle-2.9/bin/gradle build
```

The compiled mod JAR will be in `build/libs/`.

### Run in development

```bash
./gradle-2.9/bin/gradle runClient
```

---

## Installation

1. Install Minecraft Forge 1.8.9.
2. Drop the built `YukiClient-1.4.0.jar` into your `.minecraft/mods/` folder.
3. Launch Minecraft with the Forge profile.

---

## Configuration

Module positions, enabled states and settings are stored in `.minecraft/config/yukiclient.json` and are loaded/saved automatically when the game starts and exits.

---

## Credits

- Author: **staFF6773**
