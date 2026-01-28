# Features Guide

This page provides detailed documentation for each EssentialUtils feature.

## Table of Contents

- [Tree Feller](#tree-feller)
- [Vein Miner](#vein-miner)
- [Auto Farm](#auto-farm)
- [Chunk Loader](#chunk-loader)
- [Tab Menu](#tab-menu)
- [Auto-Updater](#auto-updater)

---

## Tree Feller

Break entire trees with a single swing by crouching.

### How It Works

1. **Crouch** (hold shift) while holding an **axe**
2. The action bar shows `⚒ Tree Feller Active`
3. **Break any log** of the tree
4. The entire tree (logs + leaves) will be harvested
5. A **sapling is auto-replanted** at the stump
6. **Saplings and apples** drop based on vanilla mechanics

### Technical Details

- **Block detection**: Finds all connected logs using flood-fill algorithm
- **Leaf detection**: Finds natural leaves within 6 blocks of logs
- **Drop calculation**: Uses vanilla sapling/apple drop rates per leaf type
- **Tool durability**: Consumes 1 durability per log broken
- **Performance**: Processes blocks over multiple ticks to prevent lag

### Supported Trees

All vanilla tree types are supported:
- Oak, Spruce, Birch, Jungle, Acacia, Dark Oak
- Cherry, Mangrove
- Azalea (flowering)
- Crimson/Warped stems (Nether)

### Configuration Options

```yaml
modules:
  treeFeller:
    enabled: true
    maxBlocks: 200          # Limit for huge trees
    replantSaplings: true   # Auto-plant at stump
    particleEffects: true   # Visual feedback
```

### Tips

- Works with stripped logs too (if `includeStripped` is enabled in code)
- The `maxBlocks` limit includes both logs AND leaves
- Large jungle/dark oak trees may hit the block limit
- Replanting requires the correct sapling to be in drops

---

## Vein Miner

Mine connected ore veins effortlessly.

### How It Works

1. Hold a **pickaxe** (any tier)
2. **Break any ore** block
3. All connected ores of the same type are automatically mined
4. **Fortune/Silk Touch** enchantments are applied
5. **XP drops** at your location
6. Action bar shows a summary: `⛏ x14 Coal Ore | 43 Coal (x3 Fortune) | 14 XP`

### Technical Details

- **Search pattern**: 3x3x3 area around each ore (includes diagonals)
- **Same-type matching**: Only mines the same ore type (Coal with Coal, etc.)
- **Variant support**: Automatically includes deepslate variants
- **Tool validation**: Respects tool tier requirements
- **Fortune calculation**: Uses vanilla fortune probabilities
- **Silk Touch**: Drops the ore block itself

### Supported Ores

All vanilla ores are supported:
- Coal, Iron, Copper, Gold, Redstone, Lapis, Diamond, Emerald
- Deepslate variants (auto-detected)
- Nether Gold Ore, Nether Quartz Ore
- Ancient Debris

### Configuration Options

```yaml
modules:
  veinMiner:
    enabled: true
    maxOres: 64             # Prevent huge veins from lagging
    fortuneEnabled: true    # Apply fortune enchantment
    silkTouchDropsOre: true # Silk touch behavior
```

### Tips

- The `maxOres` limit prevents massive cave systems from causing lag
- Fortune III averages ~2.2x drops for coal/lapis/redstone/diamond
- XP is collected at the original ore location (not spread out)
- Works with all pickaxe tiers (stone, iron, diamond, netherite)

---

## Auto Farm

Harvest and replant crops in an area automatically.

### How It Works

1. Hold a **hoe** (any tier)
2. **Break a mature crop**
3. All mature crops within the radius are harvested
4. **Seeds are auto-replanted**
5. Works silently (no action bar spam)

### Technical Details

- **Radius**: Configurable (default 4 = 9x9 area)
- **Mature only**: Only harvests fully grown crops
- **Replanting**: Seeds/tubers are automatically replanted
- **Silent**: No notifications to avoid spam during large harvests

### Supported Crops

| Crop | Mature State | Replant Item |
|------|--------------|--------------|
| Wheat | Age 7 | Wheat Seeds |
| Carrots | Age 7 | Carrot |
| Potatoes | Age 7 | Potato |
| Beetroots | Age 3 | Beetroot Seeds |
| Nether Wart | Age 3 | Nether Wart |
| Cocoa | Age 2 | Cocoa Beans |
| Sweet Berries | Age 3 | Sweet Berries |

### Configuration Options

```yaml
modules:
  autoFarm:
    enabled: true
    radius: 4               # 4 = 9x9 area
    autoReplant: true       # Auto-plant after harvest
```

### Tips

- Increase `radius` for larger farms (but watch for lag)
- Works well with Chunk Loader to keep farms active
- Hoe durability is consumed per crop harvested
- Fortune on hoes affects crop drops (1.17+ vanilla feature)

---

## Chunk Loader

Keep your farm chunks loaded even when you're offline.

### How It Works

1. **Crouch** (hold shift) while holding a **hoe**
2. **Break a crop** (any state)
3. The chunk is claimed and will stay loaded
4. Action bar shows: `📦 Chunk claimed! (1/9)`
5. Chunks remain loaded across server restarts

### Technical Details

- **Plugin tickets**: Uses Bukkit's plugin chunk ticket system
- **Validation loop**: Periodically verifies chunks are still loaded
- **Persistence**: Claims saved to config and restored on startup
- **Per-player limits**: Configurable max chunks per player

### Use Cases

- **AFK farms**: Keep crop farms growing while you're away
- **Mob farms**: Ensure spawning chunks stay loaded
- **Redstone**: Keep complex redstone machines running

### Claiming and Unclaiming

**To claim a chunk:**
1. Crouch + break a crop with a hoe
2. Must have `essentialutils.chunkloader` permission
3. Must not exceed your chunk limit

**To unclaim a chunk:**
- Currently requires admin commands or config editing
- Future update will add unclaim functionality

### Configuration Options

```yaml
modules:
  chunkLoader:
    enabled: true
    maxChunksPerPlayer: 9   # 9 = 3x3 chunk area
    validationInterval: 300 # Check every 5 minutes
    claimOnFarm: true       # Enable crouch+break claiming
```

### Tips

- 9 chunks = approximately 144x144 blocks (3x3 chunk grid)
- Claimed chunks persist even after server restarts
- Players with `essentialutils.chunkloader.bypass` have no limit
- Too many loaded chunks can impact server performance

---

## Tab Menu

A beautiful animated player list with server statistics.

### Features

- **Animated logo**: Rainbow gradient text animation
- **Server IP**: Displayed prominently
- **Real-time stats**: TPS, ping, memory, player count
- **Chunk info**: Shows player's claimed chunks
- **Customizable**: Every element can be enabled/disabled

### Display Elements

**Header:**
- Animated server logo (rainbow gradient)
- Server IP
- Optional tagline
- Decorative lines

**Footer:**
- Player count (online/max)
- Your ping (color-coded: green < 50ms, yellow < 150ms, red > 150ms)
- Server TPS (color-coded: green >= 19, yellow >= 15, red < 15)
- Memory usage (optional)
- Your claimed chunks
- Optional tagline

### Configuration Options

```yaml
tabMenu:
  enabled: true
  updateInterval: 4           # Animation smoothness
  header:
    logoText: "My Server"     # Your server name
    serverIp: "play.myserver.com"
    tagline: "Welcome!"       # Optional
    showDecorations: true
  footer:
    showPlayers: true
    showPing: true
    showTps: true
    showMemory: false
    showChunkInfo: true
    compactMode: true         # Single line vs multiple
```

### Tips

- Lower `updateInterval` = smoother animation but more CPU usage
- Set `logoText` to your server/network name
- `compactMode: true` looks cleaner on smaller screens
- Disable elements you don't need to reduce clutter

---

## Auto-Updater

Automatically checks for plugin updates via GitHub Releases.

### How It Works

1. On server startup, checks GitHub for the latest release
2. Compares version numbers (supports semver and build numbers)
3. If update available:
   - Logs notification to console
   - Notifies admins on join (clickable link)
   - Optionally auto-downloads the JAR

### Update Notification

Admins see a chat message when joining:

```
[EssentialUtils] A new version is available! v1.2.0 - [Download]
```

The `[Download]` link opens the GitHub releases page.

### Auto-Download

If enabled, the new JAR is downloaded to `plugins/update/`. You must:
1. Stop the server
2. Move the JAR from `plugins/update/` to `plugins/`
3. Start the server

### Configuration Options

```yaml
updater:
  enabled: true               # Enable update checking
  checkOnStartup: true        # Check on server start
  notifyAdmins: true          # Notify ops on join
  autoDownload: false         # Download automatically
  downloadPath: "update"      # Download folder
  github:
    owner: "cryptofyre"
    repo: "EssentialUtils"
```

### Tips

- `autoDownload` is disabled by default for safety
- Only players with `essentialutils.admin` see notifications
- Rate limiting: GitHub API has limits, but startup checks are rare
- Dev builds (ending in `-dev`) always show as "outdated"
