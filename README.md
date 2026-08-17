# Atmo | Ambient Sounds & Music [1.20.6+]

**Give every corner of your world its own soundscape, ambient sounds and background music.**

https://www.spigotmc.org/resources/atmo-ambient-sounds-music-1-20-6.138062/

---

**Supported versions:**
- 1.20.6-26.2 (PAPER Only)

**Contributors:**
omijica

**Soft Dependency:**
ItemsAdder (optional)

---

Atmo is a Paper plugin that plays custom ambient sounds and background music depending on where a player is standing. Zones are cuboid regions created directly in-game (menu + click selection), each linked to an ambient profile and a music track defined in simple YAML files.
Atmo can also trigger sounds based on what is around the player (such as a decorative Armor Stand or a piece of ItemsAdder furniture) or from fixed coordinates.

### Video preview

### Features

- **✓** Create cuboid **zones** in-game by selecting two corners, without any dependencies (no WorldGuard needed).
- **✓** Each zone has a **priority**, a linked **ambient profile** and/or **background music**, editable through `area.yml`.
- **✓** Ambient profiles (`ambient.yml`) combine a **looping ambient sound** with a set of **random one-off sounds**, each with its own probability, delay range, volume and pitch variation.
- **✓** **Background music per zone**.
- **✓** A **default/fallback ambience and music** (`config.yml → general_area`) applies to configured worlds when a player is outside any zone.
- **✓** **Block-bound sounds**: detect a nearby entity (e.g. an Armor Stand identified by CustomModelData) and play a sound as if it came from it.
- **✓** **Optional ItemsAdder integration**: bind sounds to ItemsAdder custom furniture the same way (`type: ITEMSADDER`).
- **✓** **Position-bound sounds**: play a sound when a player enters the radius around one or several fixed coordinates, addable in-game with `/atmo addloc`.
- **✓** **Per-player controls**: players can toggle background music and ambient sounds on/off, and set their own music/ambient volume (0–100%).
- **✓** Built-in **menu displaying the list of zones**, showing the world, positions, priority, music and ambience for each zone, as well as options for teleportation, redefinition and deletion.

### Future features

* Option to disable/re-enable a sound zone/ambient/music per player.

### Requirements

1. [Paper](https://papermc.io/downloads/all) 1.20.6+
2. Java 17+
3. [ItemsAdder](https://www.spigotmc.org/resources/itemsadder.73355/) — **optional**, only needed if you want to bind sounds to ItemsAdder furniture.

### Installation

1. **Download** the Atmo plugin JAR file.
2. Place the JAR in your server's **plugins** folder.
3. **Restart** your server; Atmo will generate `area.yml`, `ambient.yml`, `blockSound.yml` and `config.yml` in `plugins/atmo/`.
4. Edit `ambient.yml` (and `config.yml` for the default ambience) to design your soundscapes, then create zones in-game with `/atmo menu`.

### Configuration

Atmo generates four configuration files, each with a specific function:

- **`config.yml`** — Global player permissions (`AllowPlayerDisableMusic`, `AllowPlayerDisableAmbient`) and the **default ambience/music** applied per-world when a player isn't inside any zone.
- **`area.yml`** — The **zones file**: world, the two corner positions, priority, and the linked ambient/music settings.
- **`ambient.yml`** — The **ambient profiles**: a background sound and a list of random sounds, referenced by name from `area.yml` and `config.yml`.
- **`blockSound.yml`** — **Entity-bound** and **position-bound** sound triggers.

**`config.yml` – default ambience example**
```yaml
config:
  default:
    AllowPlayerDisableMusic: true
    AllowPlayerDisableAmbient: true

  general_area:
    worlds:
      - world
    ambient:
      enabled: true
      name: field
    music:
      enabled: true
      name: "minecraft:custom.music"
      volume: 50
      duration: 300
```

**`area.yml` – zone example**
```yaml
areas:
  spawn:
    world: world
    pos1: {x: 100, y: 60, z: 100}
    pos2: {x: 200, y: 80, z: 200}
    ambient:
      enabled: true
      name: field
    music:
      enabled: true
      name: "minecraft:custom.music"
      volume: 50
      duration: 300
```

**`ambient.yml` – ambient profile example**
```yaml
field:
  background_sound: "minecraft:music.menu"
  background_volume: 0.5
  background_interval: 10
  random_sounds:
    - sound: "minecraft:ambient.cave"
      chance: 0.4
      min_delay: 20
      max_delay: 50
      volume: 0.5
      pitch_base: 1.0
      pitch_variation: 0.2
```

**`blockSound.yml` – entity & position example**
```yaml
blockSound:
  entities:
    radio:
      type: "ENTITY"          # or "ITEMSADDER"
      entity: "ARMOR_STAND"
      customModelData: 5
      radius: 5.0
      sound: "minecraft:block.note_block.chime"
      chance: 0.3
      min_delay: 30
      max_delay: 70
      volume: 0.4
      pitchBase: 1.0
      pitchVariation: 0.2
      enabled: true

  positions:
    voices:
      sound: "minecraft:ambient.cave"
      radius: 5.0
      chance: 0.3
      min_delay: 30
      max_delay: 70
      volume: 0.4
      pitchBase: 1.0
      pitchVariation: 0.2
      enabled: true
      locations:
        - world, 100, 60, 100
```

### Commands 

![Commands](https://i.ibb.co/6cVMzMmd/image.png)

### Permissions

- **atmo.use** — Base permission required to use `/atmo` (default command permission).
- **atmo.admin** — Grants access to every subcommand.
- **atmo.menu** — Use `/atmo menu`.
- **atmo.editor** — Use `/atmo editor`.
- **atmo.info** — Use `/atmo info`.
- **atmo.reload** — Use `/atmo reload`.
- **atmo.addloc** — Use `/atmo addloc <name>`.
- **atmo.music** — Use `/atmo music` (not required if `AllowPlayerDisableMusic` is true in `config.yml`).
- **atmo.ambient** — Use `/atmo ambient` (not required if `AllowPlayerDisableAmbient` is true in `config.yml`).
- **atmo.volume** — Use `/atmo volume`.

### Usage

**Creating a Zone**

1. Run **/atmo menu** and click **"Create an area."**
2. Type the zone's name in the anvil GUI and confirm.
3. **Left-click** a block to set **Position 1**, then **right-click** another block to set **Position 2**.
4. The zone is instantly saved to `area.yml` with default settings (priority 1, music/ambient disabled). Edit the file to link it to an ambient profile and/or music track, then run `/atmo reload`.

**Browsing Zones**

Run **/atmo menu** and click **"Edit an area."** to open the paginated zone list, showing the world, positions, priority, music and ambient status for each zone, with options to teleport, redefine boundaries or delete it.

**Adding a Position-Bound Sound Location**

Once a `positions` entry exists in `blockSound.yml` (e.g. `voices`), stand where you want the sound to trigger and run **/atmo addloc voices** to append your current coordinates to that entry.

### Support

If you run into any issue or have a suggestion for **Atmo**, feel free to open the Discussion tab below.

**Credits**
Atmo Plugin developed by @omijica.
