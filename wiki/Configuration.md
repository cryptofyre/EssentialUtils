# Configuration Reference

This page documents all configuration options available in `plugins/EssentialUtils/config.yml`.

## Config Version

```yaml
config-version: 1
```

**Do not edit this value.** It's used for automatic config migration when updating the plugin.

---

## Modules

### Tree Feller

```yaml
modules:
  treeFeller:
    enabled: true           # Enable/disable the feature
    maxBlocks: 200          # Maximum logs + leaves per tree
    replantSaplings: true   # Auto-replant sapling at stump
    particleEffects: true   # Show green sparkle on replant
```

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable or disable Tree Feller |
| `maxBlocks` | integer | `200` | Maximum blocks (logs + leaves) to process per tree. Prevents lag on massive trees. |
| `replantSaplings` | boolean | `true` | Automatically plant a sapling at the tree stump after felling |
| `particleEffects` | boolean | `true` | Show green sparkle particles when a sapling is replanted |

### Vein Miner

```yaml
modules:
  veinMiner:
    enabled: true
    maxOres: 64             # Maximum ores per vein
    fortuneEnabled: true    # Apply fortune enchantment multipliers
    silkTouchDropsOre: true # Silk touch drops ore blocks instead of resources
```

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable or disable Vein Miner |
| `maxOres` | integer | `64` | Maximum ores to mine per vein. Prevents lag on huge ore deposits. |
| `fortuneEnabled` | boolean | `true` | Apply Fortune enchantment to drops (uses vanilla multipliers) |
| `silkTouchDropsOre` | boolean | `true` | Silk Touch drops ore blocks instead of processed resources |

### Auto Farm

```yaml
modules:
  autoFarm:
    enabled: true
    radius: 4               # Harvest radius around broken crop
    autoReplant: true       # Replant seeds after harvest
```

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable or disable Auto Farm |
| `radius` | integer | `4` | Radius around the broken crop to harvest (total area = (2*radius+1)^2) |
| `autoReplant` | boolean | `true` | Automatically replant seeds after harvesting |

**Supported crops:** Wheat, carrots, potatoes, beetroots, nether wart, cocoa beans, sweet berries, and more.

### Chunk Loader

```yaml
modules:
  chunkLoader:
    enabled: true
    maxChunksPerPlayer: 9   # Max chunks each player can claim
    validationInterval: 300 # Seconds between chunk validation checks
    claimOnFarm: true       # Auto-claim chunks when using AutoFarm while sneaking
```

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable or disable Chunk Loader |
| `maxChunksPerPlayer` | integer | `9` | Maximum chunks each player can keep loaded (9 = 3x3 area) |
| `validationInterval` | integer | `300` | Seconds between chunk validation to ensure they stay loaded |
| `claimOnFarm` | boolean | `true` | Automatically claim chunks when crouching + breaking a crop |

**Note:** Players with `essentialutils.chunkloader.bypass` permission are not limited.

---

## Action Bar Messages

### Tree Feller Action Bar

```yaml
actionbar:
  treeFeller:
    showActiveIndicator: true
    activeMessage: "&a⚒ Tree Feller Active"
    showSummary: true
    summaryFormat: "&a🌳 &f{logs} logs &7| &f{saplings} saplings &7| &f{apples} apples"
```

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `showActiveIndicator` | boolean | `true` | Show indicator while crouching with axe |
| `activeMessage` | string | `&a⚒ Tree Feller Active` | Message shown while Tree Feller is ready |
| `showSummary` | boolean | `true` | Show harvest summary after felling |
| `summaryFormat` | string | (see above) | Format for harvest summary |

**Summary placeholders:**
- `{logs}` - Number of logs harvested
- `{saplings}` - Number of saplings dropped
- `{apples}` - Number of apples dropped

### Vein Miner Action Bar

```yaml
actionbar:
  veinMiner:
    showSummary: true
    summaryDuration: 40     # Ticks (2 seconds)
    summaryFormat: "&b⛏ &ex{count} {ore} &7| &f{drops} &7({mult}) &7| &a{xp} XP"
```

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `showSummary` | boolean | `true` | Show summary after mining completes |
| `summaryDuration` | integer | `40` | How long to show summary (in ticks, 20 = 1 second) |
| `summaryFormat` | string | (see above) | Format for mining summary |

**Summary placeholders:**
- `{count}` - Number of ores mined
- `{ore}` - Ore type name
- `{drops}` - Total items dropped
- `{mult}` - Fortune multiplier (e.g., "x3 Fortune")
- `{xp}` - Total XP gained

### Chunk Loader Action Bar

```yaml
actionbar:
  chunkLoader:
    showClaimMessage: true
    claimMessage: "&a📦 Chunk claimed! &7({current}/{max})"
    unclaimMessage: "&e📦 Chunk unclaimed."
```

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `showClaimMessage` | boolean | `true` | Show message when claiming/unclaiming chunks |
| `claimMessage` | string | (see above) | Message shown when claiming a chunk |
| `unclaimMessage` | string | (see above) | Message shown when unclaiming a chunk |

**Claim message placeholders:**
- `{current}` - Current number of claimed chunks
- `{max}` - Maximum allowed chunks

---

## Tab Menu

```yaml
tabMenu:
  enabled: true
  updateInterval: 4           # Ticks between updates
```

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable or disable Tab Menu |
| `updateInterval` | integer | `4` | Ticks between animation updates (lower = smoother, higher CPU) |

### Header Settings

```yaml
tabMenu:
  header:
    logoText: "Server Name"   # Your server/network name (animated gradient)
    serverIp: "your.server.ip"
    tagline: ""               # Optional tagline below logo
    showDecorations: true     # Show decorative lines and icons
    decorationStyle: "═"      # Character for decorative lines
    decorationLength: 10      # Length of decorative lines
```

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `logoText` | string | `Server Name` | Server name displayed with animated rainbow gradient |
| `serverIp` | string | `your.server.ip` | Server IP displayed below logo |
| `tagline` | string | (empty) | Optional tagline below logo (leave empty to hide) |
| `showDecorations` | boolean | `true` | Show decorative lines and sparkle icons |
| `decorationStyle` | string | `═` | Character used for decorative lines |
| `decorationLength` | integer | `10` | Length of decorative lines |

### Footer Settings

```yaml
tabMenu:
  footer:
    showPlayers: true         # Show online/max players
    showPing: true            # Show player's ping
    showTps: true             # Show server TPS
    showMemory: false         # Show memory usage
    showChunkInfo: true       # Show player's claimed chunks
    tagline: ""               # Optional footer tagline
    compactMode: true         # Combine stats on fewer lines
```

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `showPlayers` | boolean | `true` | Display online/max player count |
| `showPing` | boolean | `true` | Display player's current ping |
| `showTps` | boolean | `true` | Display server TPS (color-coded) |
| `showMemory` | boolean | `false` | Display server memory usage |
| `showChunkInfo` | boolean | `true` | Display player's claimed chunks (if Chunk Loader enabled) |
| `tagline` | string | (empty) | Optional tagline at bottom (leave empty to hide) |
| `compactMode` | boolean | `true` | Display stats on a single line vs. multiple lines |

---

## Performance

```yaml
performance:
  blocksPerTick: 32         # Max blocks processed per tick per player
  requireChunkLoaded: true  # Only process blocks in loaded chunks
```

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `blocksPerTick` | integer | `32` | Maximum blocks to process per tick per player. Lower = less lag spikes, slower processing. |
| `requireChunkLoaded` | boolean | `true` | Skip processing blocks in unloaded chunks (prevents errors) |

---

## Auto-Updater

```yaml
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

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable or disable update checking |
| `checkOnStartup` | boolean | `true` | Check for updates when server starts |
| `notifyAdmins` | boolean | `true` | Notify players with `essentialutils.admin` permission on join |
| `autoDownload` | boolean | `false` | Automatically download new versions (manual move + restart required) |
| `downloadPath` | string | `update` | Folder inside `plugins/` where updates are downloaded |
| `github.owner` | string | `cryptofyre` | GitHub repository owner |
| `github.repo` | string | `EssentialUtils` | GitHub repository name |

---

## Color Codes

All messages support Minecraft color codes using `&`:

| Code | Color | Code | Color |
|------|-------|------|-------|
| `&0` | Black | `&8` | Dark Gray |
| `&1` | Dark Blue | `&9` | Blue |
| `&2` | Dark Green | `&a` | Green |
| `&3` | Dark Aqua | `&b` | Aqua |
| `&4` | Dark Red | `&c` | Red |
| `&5` | Dark Purple | `&d` | Light Purple |
| `&6` | Gold | `&e` | Yellow |
| `&7` | Gray | `&f` | White |

**Formatting codes:**
- `&l` - Bold
- `&o` - Italic
- `&n` - Underline
- `&m` - Strikethrough
- `&r` - Reset

---

## Example Full Config

See the default [config.yml](https://github.com/cryptofyre/EssentialUtils/blob/master/src/main/resources/config.yml) for a complete example.
