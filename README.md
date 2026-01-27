# Essential Utils ![Java](https://img.shields.io/badge/Java-21-orange) ![Gradle](https://img.shields.io/badge/Gradle-Build-brightgreen) ![PaperMC](https://img.shields.io/badge/PaperMC-1.21.8-blue) ![Folia](https://img.shields.io/badge/Folia-Supported-success)

> A **Folia-optimized survival utilities plugin** for Paper/Folia 1.21.8+.  
> Provides intuitive tree felling, vein mining, and auto-farming with actionbar feedback.

---

## Features

### Tree Feller

Break entire trees with a single swing! Activated by **crouching while using an axe**.

- Breaks all connected logs AND natural leaves
- Calculates proper **sapling and apple drops** based on vanilla mechanics
- **Auto-replants saplings** at the stump with a green sparkle effect
- Shows active indicator in actionbar while crouching with axe
- Displays summary of harvested items when complete

### Vein Miner

Mine connected ore veins effortlessly. **Always active** when using a pickaxe.

- Mines all connected ores including **diagonal blocks** (3x3x3 search)
- Full **Fortune enchantment** support with vanilla drop rates
- **Silk Touch** support - drops ore blocks directly
- Respects tool tier requirements (won't mine diamond with stone pick)
- **XP drops** at the original ore location
- Beautiful actionbar summary: `⛏ x14 Coal Ore | 43 Coal (x3 Fortune) | 14 XP`

### Auto Farm

Harvest crops in an area with your hoe. **Always active** when using a hoe on mature crops.

- Harvests all mature crops in a configurable radius
- **Auto-replants** seeds automatically
- Supports wheat, carrots, potatoes, beetroots, nether wart, and more
- No spam notifications - works silently in the background

---

## Activation

| Feature | Activation | Indicator |
|---------|------------|-----------|
| **Tree Feller** | Crouch + break log with axe | Shows `⚒ Tree Feller Active` while crouching |
| **Vein Miner** | Break ore with pickaxe | Summary shown after mining completes |
| **Auto Farm** | Break mature crop with hoe | No indicator (silent) |

---

## Commands

```bash
/eutils status              # View all module states
/eutils enable <module>     # Enable a module
/eutils disable <module>    # Disable a module  
/eutils reload              # Reload configuration
```

**Modules:** `treefeller`, `veinminer`, `autofarm`

**Aliases:** `/eu`, `/essentialutils`

---

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `essentialutils.admin` | Access to admin commands | OP |
| `essentialutils.use` | Use all features | true |
| `essentialutils.treefeller` | Use Tree Feller | true |
| `essentialutils.veinminer` | Use Vein Miner | true |
| `essentialutils.autofarm` | Use Auto Farm | true |

---

## Configuration

```yaml
modules:
  treeFeller:
    enabled: true
    maxBlocks: 200          # Max logs + leaves per tree
    replantSaplings: true   # Auto-replant at stump
    particleEffects: true   # Green sparkle on replant
  
  veinMiner:
    enabled: true
    maxOres: 64             # Max ores per vein
    fortuneEnabled: true    # Apply fortune multipliers
    silkTouchDropsOre: true # Silk touch drops ore blocks
  
  autoFarm:
    enabled: true
    radius: 4               # Harvest radius
    autoReplant: true       # Replant seeds

actionbar:
  treeFeller:
    showActiveIndicator: true
    activeMessage: "&a⚒ Tree Feller Active"
    showSummary: true
  
  veinMiner:
    showSummary: true
    summaryDuration: 40     # Ticks (2 seconds)
    summaryFormat: "&b⛏ &ex{count} {ore} &7| &f{drops} &7({mult}) &7| &a{xp} XP"

performance:
  blocksPerTick: 32         # Max blocks per tick per player
  requireChunkLoaded: true
```

---

## Project Structure

```
src/main/java/net/ppekkungz/essentialUtils/
├── EssentialUtils.java           # Main plugin class
├── config/
│   └── PluginConfig.java         # Configuration wrapper
├── command/
│   └── AdminCommands.java        # Admin commands handler
├── features/
│   ├── Feature.java              # Feature interface
│   ├── tree/TreeAssistFeature.java
│   ├── vein/VeinMineFeature.java
│   └── farm/AutoFarmFeature.java
├── indicator/
│   ├── ActionBarService.java     # Timed actionbar messages
│   └── IndicatorService.java
├── listener/
│   └── ActivationListener.java   # Event handling
├── state/
│   ├── PlayerState.java          # Player state enum
│   └── StateManager.java         # State tracking
├── util/
│   ├── BlockUtil.java            # Block neighbor utilities
│   ├── FortuneUtil.java          # Fortune/Silk Touch calculations
│   ├── HarvestUtil.java          # Tool tier utilities
│   ├── LeafDropUtil.java         # Sapling/apple drop rates
│   ├── Materials.java            # Material utilities
│   └── Protection.java           # Protection checks
└── work/
    ├── WorkService.java          # Folia-safe work processing
    ├── WorkQueue.java            # Per-player work queue
    ├── WorkItem.java             # Work unit definition
    └── VeinMineResult.java       # Mining session tracking
```

---

## Installation

1. Download `EssentialUtils-x.x.x.jar` from releases
2. Drop into your `plugins/` folder
3. Run with **Paper 1.21.8+** or **Folia**
4. Config is generated on first run at `plugins/EssentialUtils/config.yml`

---

## Build

Requires:
- JDK **21**
- Gradle (wrapper included)

```bash
./gradlew build
```

Output: `build/libs/essential-utils-<version>.jar`

---

## Folia Compatibility

This plugin is fully compatible with Folia's regionized multithreading:

- Uses `player.getScheduler()` for per-player task scheduling
- All block operations happen on the correct region thread
- Thread-safe state management with ConcurrentHashMap
- No global schedulers or async block modifications

---

## License

MIT © 2025 cryptofyre
