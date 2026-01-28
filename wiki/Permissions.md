# Permissions Reference

This page documents all permission nodes available in EssentialUtils.

## Permission Nodes

| Permission | Description | Default |
|------------|-------------|---------|
| `essentialutils.admin` | Access to admin commands (`/eutils`) | OP |
| `essentialutils.use` | Use all EssentialUtils features | true |
| `essentialutils.treefeller` | Use Tree Feller feature | true |
| `essentialutils.veinminer` | Use Vein Miner feature | true |
| `essentialutils.autofarm` | Use Auto Farm feature | true |
| `essentialutils.chunkloader` | Use Chunk Loader to claim chunks | true |
| `essentialutils.chunkloader.bypass` | Bypass chunk loader limits | OP |

## Default Values

- **true** = All players have this permission by default
- **OP** = Only server operators have this permission by default

## Detailed Descriptions

### essentialutils.admin

Grants access to administrative commands:

- `/eutils status` - View module states
- `/eutils enable <module>` - Enable a module
- `/eutils disable <module>` - Disable a module
- `/eutils reload` - Reload configuration

Also receives update notifications when joining the server (if `updater.notifyAdmins` is enabled).

### essentialutils.use

Parent permission for all feature permissions. Granting this permission allows access to all features. Denying this permission blocks all features.

**Note:** This is a convenience permission. Individual feature permissions still work independently.

### essentialutils.treefeller

Allows the player to use Tree Feller:
- Crouch + break log to fell entire trees
- Receive sapling/apple drops
- Auto-replant functionality

### essentialutils.veinminer

Allows the player to use Vein Miner:
- Mine connected ore veins
- Fortune/Silk Touch application
- XP collection from veins

### essentialutils.autofarm

Allows the player to use Auto Farm:
- Harvest crops in radius
- Auto-replant seeds

### essentialutils.chunkloader

Allows the player to use Chunk Loader:
- Claim chunks to keep loaded
- Subject to `maxChunksPerPlayer` limit

### essentialutils.chunkloader.bypass

Allows the player to bypass Chunk Loader restrictions:
- No limit on claimed chunks
- Useful for admins or premium ranks

## LuckPerms Examples

### Basic Setup

```bash
# Give a group access to all features (default behavior)
/lp group default permission set essentialutils.use true

# Remove Tree Feller from guests
/lp group guest permission set essentialutils.treefeller false

# Give VIPs more chunk slots (via bypass)
/lp group vip permission set essentialutils.chunkloader.bypass true

# Give admins full control
/lp group admin permission set essentialutils.admin true
```

### Rank-Based Permissions

```bash
# Guest: Only Auto Farm
/lp group guest permission set essentialutils.treefeller false
/lp group guest permission set essentialutils.veinminer false
/lp group guest permission set essentialutils.chunkloader false

# Member: All features except Chunk Loader
/lp group member permission set essentialutils.chunkloader false

# VIP: All features including unlimited chunks
/lp group vip permission set essentialutils.chunkloader.bypass true

# Admin: Everything
/lp group admin permission set essentialutils.admin true
```

## Permission Plugins

EssentialUtils works with any permission plugin that supports Bukkit's permission system:

- [LuckPerms](https://luckperms.net/) (recommended)
- [PermissionsEx](https://github.com/PEXPlugins/PermissionsEx)
- [GroupManager](https://github.com/ElgarL/GroupManager)
- [bPermissions](https://github.com/rymate1234/bPermissions)
- UltraPermissions
- And many others

## Troubleshooting

### Feature not working for a player

1. Check if the module is enabled: `/eutils status`
2. Check player's permission: `/lp user <player> permission check essentialutils.<feature>`
3. Verify no parent permission is denying access
4. Check if player is using the correct tool

### Admin commands not working

1. Verify player has `essentialutils.admin` permission
2. Check if player is OP (default has this permission)
3. Try `/lp user <player> permission set essentialutils.admin true`

### Chunk Loader limit not applying

- The `maxChunksPerPlayer` config applies to players WITHOUT `essentialutils.chunkloader.bypass`
- Players with bypass permission have unlimited chunks
- OPs have bypass by default
