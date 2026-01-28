# Installation Guide

This guide covers how to install and set up EssentialUtils on your Paper or Folia server.

## Requirements

| Requirement | Version |
|-------------|---------|
| Server | Paper 1.21.8+ or Folia |
| Java | 21 or later |

## Installation Steps

### 1. Download the Plugin

Download the latest `EssentialUtils-x.x.x.jar` from the [GitHub Releases](https://github.com/cryptofyre/EssentialUtils/releases) page.

### 2. Install the Plugin

1. Stop your server if it's running
2. Place the JAR file in your server's `plugins/` folder
3. Start your server

### 3. Verify Installation

After starting the server, you should see in the console:

```
[EssentialUtils] EssentialUtils enabled (Folia-compatible)
[EssentialUtils]   Tree Feller: Enabled
[EssentialUtils]   Vein Miner: Enabled
[EssentialUtils]   Auto Farm: Enabled
[EssentialUtils]   Chunk Loader: Enabled
[EssentialUtils]   Tab Menu: Enabled
```

### 4. Configure (Optional)

The default configuration works great out of the box, but you can customize it:

1. Navigate to `plugins/EssentialUtils/config.yml`
2. Edit the settings as desired (see [Configuration](Configuration))
3. Run `/eutils reload` in-game or restart the server

## Folia Installation

EssentialUtils is fully compatible with Folia. The installation process is identical to Paper.

**Folia-specific features:**
- Uses per-player schedulers for thread-safe operations
- Chunk loader uses plugin chunk tickets (region-safe)
- All block operations run on the correct region thread

## Building from Source

If you want to build the plugin yourself:

### Requirements

- JDK 21
- Git

### Build Steps

```bash
# Clone the repository
git clone https://github.com/cryptofyre/EssentialUtils.git
cd EssentialUtils

# Build with Gradle
./gradlew build

# Output JAR is in build/libs/
```

## Troubleshooting

### Plugin doesn't load

- Ensure you're running Paper 1.21.8+ or Folia
- Verify Java 21 is being used: Check console for Java version on startup
- Check for errors in the console during startup

### Features not working

- Verify the module is enabled: `/eutils status`
- Check player permissions: `essentialutils.<feature>`
- Ensure correct tool is being used

### Config not updating

- Run `/eutils reload` after editing config.yml
- Check console for any config parsing errors
- Ensure YAML syntax is valid (proper indentation, no tabs)

## Updating

### Manual Update

1. Download the new version from GitHub Releases
2. Stop your server
3. Replace the old JAR with the new one
4. Start your server
5. Config will be automatically migrated if needed

### Auto-Update

EssentialUtils can notify you when updates are available:

1. Ensure `updater.enabled: true` in config
2. Admins will see a notification on join when updates are available
3. Optionally enable `updater.autoDownload: true` to auto-download updates

The auto-downloaded JAR is placed in `plugins/update/` - you'll need to manually move it and restart.

## Uninstalling

1. Stop your server
2. Delete `plugins/EssentialUtils-x.x.x.jar`
3. Delete `plugins/EssentialUtils/` folder (optional - keeps config for reinstall)
4. Start your server
