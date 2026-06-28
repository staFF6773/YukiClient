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

### Behavior Modules
- **Freelook** — Hold `Left Alt` for a 360° third-person camera. Your real aim and movement direction stay locked, so you can look around freely without changing where you walk or what the server sees.
- **Sprint** — Keeps your sprint active automatically.
- **Zoom** — Hold `C` to smoothly zoom in with reduced mouse sensitivity.
- **FullBright** — Removes darkness without requiring torches or potions.

### In-Game HUD Editor
Press `Right Shift` in-game to open the draggable HUD editor. Move and resize modules however you like — your layout is saved automatically.

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
2. Drop the built `YukiClient-1.0.0.jar` into your `.minecraft/mods/` folder.
3. Launch Minecraft with the Forge profile.

---

## Configuration

Module positions, enabled states and settings are stored in `.minecraft/config/yukiclient/` and are loaded/saved automatically when the game starts and exits.

---

## Credits

- Author: **staFF6773**
