# Commands Reference

This page documents all commands available in EssentialUtils.

## Main Command

**Command:** `/eutils`

**Aliases:** `/eu`, `/essentialutils`

**Permission:** `essentialutils.admin`

## Subcommands

### Status

View the current state of all modules.

```
/eutils status
```

**Output:**
```
EssentialUtils Status:
  Tree Feller: Enabled
  Vein Miner: Enabled
  Auto Farm: Enabled
  Chunk Loader: Enabled
  Tab Menu: Enabled
```

### Enable

Enable a disabled module.

```
/eutils enable <module>
```

**Arguments:**
- `<module>` - The module to enable

**Available modules:**
- `treefeller` - Tree Feller feature
- `veinminer` - Vein Miner feature
- `autofarm` - Auto Farm feature
- `chunkloader` - Chunk Loader feature
- `tabmenu` - Tab Menu feature

**Example:**
```
/eutils enable treefeller
```

**Output:**
```
Tree Feller has been enabled.
```

### Disable

Disable an enabled module.

```
/eutils disable <module>
```

**Arguments:**
- `<module>` - The module to disable

**Example:**
```
/eutils disable veinminer
```

**Output:**
```
Vein Miner has been disabled.
```

### Reload

Reload the configuration from disk.

```
/eutils reload
```

**What it does:**
1. Reloads `config.yml` from disk
2. Applies new settings immediately
3. Does NOT restart services (Tab Menu continues with new settings)

**Output:**
```
EssentialUtils configuration reloaded.
```

**Note:** Some changes may require a full server restart to take effect.

## Tab Completion

All commands support tab completion:

- `/eutils <tab>` - Shows: `status`, `enable`, `disable`, `reload`
- `/eutils enable <tab>` - Shows available modules
- `/eutils disable <tab>` - Shows available modules

## Usage Examples

### Check what's running

```
/eutils status
```

### Temporarily disable Vein Miner for an event

```
/eutils disable veinminer
# ... run your event ...
/eutils enable veinminer
```

### Apply config changes

1. Edit `plugins/EssentialUtils/config.yml`
2. Run `/eutils reload`

### Disable Tab Menu if causing issues

```
/eutils disable tabmenu
```

## Error Messages

| Message | Cause | Solution |
|---------|-------|----------|
| `Unknown module: xyz` | Invalid module name | Use one of: treefeller, veinminer, autofarm, chunkloader, tabmenu |
| `You don't have permission` | Missing `essentialutils.admin` | Grant the permission or use OP |
| `Module is already enabled/disabled` | Module already in desired state | No action needed |

## Console Usage

All commands can be run from the server console:

```
eutils status
eutils enable autofarm
eutils reload
```

## Planned Commands

Future updates may include:

- `/eutils chunkloader list` - List your claimed chunks
- `/eutils chunkloader unclaim` - Unclaim current chunk
- `/eutils chunkloader tp <number>` - Teleport to a claimed chunk
