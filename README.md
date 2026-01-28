# Essential Utils

![Java](https://img.shields.io/badge/Java-21-orange)
![Gradle](https://img.shields.io/badge/Gradle-Build-brightgreen)
![PaperMC](https://img.shields.io/badge/PaperMC-1.21.8-blue)
![Folia](https://img.shields.io/badge/Folia-Supported-success)

> A **Folia-optimized survival utilities plugin** for Paper/Folia 1.21.8+.  
> Provides intuitive tree felling, vein mining, auto-farming, chunk loading, and a customizable tab menu with actionbar feedback.

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

### Chunk Loader

Keep your farm chunks loaded even when offline. Activated by **crouching + breaking a crop**.

- Claims chunks to keep them loaded 24/7
- Configurable per-player chunk limit (default: 9 chunks / 3x3 area)
- Integrates with Auto Farm for seamless claiming
- Persists across server restarts
- Periodic validation ensures chunks stay loaded

### Tab Menu

Beautiful animated player list with server stats.

- Rainbow gradient animated server logo
- Real-time stats: TPS, ping, memory, players
- Chunk claim counter for each player
- Fully configurable header/footer
- Compact and expanded display modes

---

## Activation

| Feature | Activation | Indicator |
|---------|------------|-----------|
| **Tree Feller** | Crouch + break log with axe | Shows `⚒ Tree Feller Active` while crouching |
| **Vein Miner** | Break ore with pickaxe | Summary shown after mining completes |
| **Auto Farm** | Break mature crop with hoe | No indicator (silent) |
| **Chunk Loader** | Crouch + break crop with hoe | Shows `📦 Chunk claimed!` on claim |

---

## Commands

```bash
/eutils status              # View all module states
/eutils enable <module>     # Enable a module
/eutils disable <module>    # Disable a module  
/eutils reload              # Reload configuration
```

**Modules:** `treefeller`, `veinminer`, `autofarm`, `chunkloader`, `tabmenu`

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
| `essentialutils.chunkloader` | Use Chunk Loader | true |
| `essentialutils.chunkloader.bypass` | Bypass chunk limits | OP |

---

## Configuration

```yaml
# Config version - DO NOT EDIT (used for automatic migration)
config-version: 1

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
  
  chunkLoader:
    enabled: true
    maxChunksPerPlayer: 9   # Max chunks each player can claim (3x3 area)
    validationInterval: 300 # Seconds between chunk validation checks
    claimOnFarm: true       # Auto-claim chunks when using AutoFarm while sneaking

actionbar:
  treeFeller:
    showActiveIndicator: true
    activeMessage: "&a⚒ Tree Feller Active"
    showSummary: true
    summaryFormat: "&a🌳 &f{logs} logs &7| &f{saplings} saplings &7| &f{apples} apples"
  
  veinMiner:
    showSummary: true
    summaryDuration: 40     # Ticks (2 seconds)
    summaryFormat: "&b⛏ &ex{count} {ore} &7| &f{drops} &7({mult}) &7| &a{xp} XP"
  
  chunkLoader:
    showClaimMessage: true
    claimMessage: "&a📦 Chunk claimed! &7({current}/{max})"
    unclaimMessage: "&e📦 Chunk unclaimed."

tabMenu:
  enabled: true
  updateInterval: 4           # Ticks between updates
  header:
    logoText: "Server Name"   # Your server/network name (animated gradient)
    serverIp: "your.server.ip"
    tagline: ""               # Optional tagline below logo
    showDecorations: true     # Show decorative lines and icons
    decorationStyle: "═"      # Character for decorative lines
    decorationLength: 10      # Length of decorative lines
  footer:
    showPlayers: true         # Show online/max players
    showPing: true            # Show player's ping
    showTps: true             # Show server TPS
    showMemory: false         # Show memory usage
    showChunkInfo: true       # Show player's claimed chunks
    tagline: ""               # Optional footer tagline
    compactMode: true         # Combine stats on fewer lines

performance:
  blocksPerTick: 32         # Max blocks per tick per player
  requireChunkLoaded: true

updater:
  enabled: true               # Enable update checking
  checkOnStartup: true        # Check for updates when server starts
  notifyAdmins: true          # Notify ops when they join if update available
  autoDownload: false         # Auto-download updates (requires restart)
  downloadPath: "update"      # Folder inside plugins/ to download updates to
  github:
    owner: "cryptofyre"
    repo: "EssentialUtils"
```

---

## Auto-Updater

The plugin automatically checks for updates via GitHub Releases:

- Checks for updates on server startup
- Notifies admins when they join if an update is available
- Clickable download link in chat
- Optional auto-download to `plugins/update/` folder
- Configure in `config.yml` under `updater:`

---

## Project Structure

```
src/main/java/org/cryptofyre/essentialUtils/
├── EssentialUtils.java           # Main plugin class
├── config/
│   ├── PluginConfig.java         # Configuration wrapper
│   └── ConfigMigrator.java       # Config version migration
├── command/
│   └── AdminCommands.java        # Admin commands handler
├── features/
│   ├── Feature.java              # Feature interface
│   ├── tree/TreeAssistFeature.java
│   ├── vein/VeinMineFeature.java
│   ├── farm/AutoFarmFeature.java
│   └── chunkloader/ChunkLoaderFeature.java
├── indicator/
│   ├── ActionBarService.java     # Timed actionbar messages
│   ├── ActionBarIndicator.java   # Indicator interface
│   ├── BossBarIndicator.java     # Boss bar display
│   ├── IndicatorService.java     # Indicator management
│   └── TabMenuService.java       # Animated tab menu
├── listener/
│   └── ActivationListener.java   # Event handling
├── state/
│   ├── PlayerState.java          # Player state enum
│   └── StateManager.java         # State tracking
├── updater/
│   └── UpdateChecker.java        # GitHub release checker
├── util/
│   ├── BlockUtil.java            # Block neighbor utilities
│   ├── FortuneUtil.java          # Fortune/Silk Touch calculations
│   ├── HarvestUtil.java          # Tool tier utilities
│   ├── LeafDropUtil.java         # Sapling/apple drop rates
│   ├── Materials.java            # Material pattern matching
│   ├── Ores.java                 # Ore variant expansion
│   └── Protection.java           # Protection checks
└── work/
    ├── WorkService.java          # Folia-safe work processing
    ├── WorkQueue.java            # Per-player work queue
    ├── WorkItem.java             # Work unit definition
    └── VeinMineResult.java       # Mining session tracking
```

---

## Installation

1. Download `EssentialUtils-x.x.x.jar` from [releases](https://github.com/cryptofyre/EssentialUtils/releases)
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
- Chunk loader uses plugin chunk tickets (Folia-safe)

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to contribute to this project.

---

## License

MIT © 2025 cryptofyre
