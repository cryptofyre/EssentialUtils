# Contributing to EssentialUtils

Thank you for your interest in contributing to EssentialUtils! This document provides guidelines and instructions for contributing.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How to Contribute](#how-to-contribute)
- [Reporting Issues](#reporting-issues)
- [Pull Request Process](#pull-request-process)
- [Development Setup](#development-setup)
- [Code Style Guidelines](#code-style-guidelines)
- [Testing](#testing)

---

## Code of Conduct

Please be respectful and constructive in all interactions. We welcome contributors of all experience levels.

---

## How to Contribute

### Types of Contributions

- **Bug fixes** - Fix issues with existing features
- **New features** - Add new functionality (please discuss first)
- **Documentation** - Improve README, wiki, or code comments
- **Performance** - Optimize existing code
- **Tests** - Add or improve test coverage

### Before You Start

1. Check [existing issues](https://github.com/cryptofyre/EssentialUtils/issues) to see if your idea/bug is already discussed
2. For new features, open an issue first to discuss the implementation
3. For bug fixes, reference the issue number in your PR

---

## Reporting Issues

### Bug Reports

When reporting a bug, please include:

- **Server version**: Paper/Folia version (e.g., Paper 1.21.8-123)
- **Plugin version**: EssentialUtils version from `/version EssentialUtils`
- **Description**: Clear description of the issue
- **Steps to reproduce**: How to trigger the bug
- **Expected behavior**: What should happen
- **Actual behavior**: What actually happens
- **Logs**: Relevant console errors (use pastebin for long logs)

### Feature Requests

For feature requests, please include:

- **Use case**: Why is this feature needed?
- **Description**: What should the feature do?
- **Alternatives**: Have you considered other solutions?

---

## Pull Request Process

### 1. Fork and Clone

```bash
git clone https://github.com/YOUR_USERNAME/EssentialUtils.git
cd EssentialUtils
```

### 2. Create a Branch

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/your-bug-fix
```

### 3. Make Your Changes

- Follow the [code style guidelines](#code-style-guidelines)
- Keep commits focused and atomic
- Write clear commit messages

### 4. Test Your Changes

```bash
./gradlew build
```

Test on both Paper and Folia if possible.

### 5. Submit a Pull Request

- Push your branch to your fork
- Open a PR against `master`
- Fill out the PR template
- Link any related issues

### PR Requirements

- [ ] Code compiles without errors
- [ ] Follows existing code style
- [ ] Folia-compatible (no unsafe thread access)
- [ ] Config changes include defaults
- [ ] New features are documented

---

## Development Setup

### Requirements

- **JDK 21** or later
- **Gradle** (wrapper included)
- **IDE**: IntelliJ IDEA recommended

### Building

```bash
# Build the plugin
./gradlew build

# Output JAR location
build/libs/essential-utils-<version>.jar
```

### Project Structure

```
src/main/java/org/cryptofyre/essentialUtils/
├── EssentialUtils.java     # Main plugin entry point
├── config/                 # Configuration handling
├── command/                # Command handlers
├── features/               # Feature implementations
├── indicator/              # UI indicators (actionbar, tab menu)
├── listener/               # Event listeners
├── state/                  # Player state management
├── updater/                # Auto-update system
├── util/                   # Utility classes
└── work/                   # Async work queue system
```

---

## Code Style Guidelines

### General

- **Java 21**: Use modern Java features where appropriate
- **Indentation**: 4 spaces (no tabs)
- **Line length**: 120 characters max
- **Braces**: K&R style (opening brace on same line)

### Naming Conventions

```java
// Classes: PascalCase
public class TreeAssistFeature { }

// Methods/Variables: camelCase
public void processBlock() { }
private int maxBlocks;

// Constants: UPPER_SNAKE_CASE
private static final int MAX_BLOCKS = 200;
```

### Folia Compatibility

**Critical**: All code must be Folia-compatible.

```java
// GOOD: Per-player scheduler
player.getScheduler().runDelayed(plugin, task -> {
    // Code runs on player's region thread
}, null, 1L);

// GOOD: Global region scheduler for non-player tasks
plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
    // Code runs on global region
});

// BAD: Bukkit scheduler (not Folia-compatible)
// Bukkit.getScheduler().runTask(plugin, () -> { });
```

### Thread Safety

```java
// Use ConcurrentHashMap for shared state
private final Map<UUID, PlayerData> playerData = new ConcurrentHashMap<>();

// Avoid synchronization on hot paths
```

### Configuration

When adding new config options:

1. Add default to `config.yml`
2. Add getter to `PluginConfig.java`
3. Update `ConfigMigrator.java` if needed
4. Document in README

```java
// PluginConfig.java
public boolean myNewOption() { 
    return c.getBoolean("modules.myFeature.newOption", true); 
}
```

---

## Testing

### Manual Testing Checklist

Before submitting a PR, test on:

- [ ] **Paper**: Standard Paper server
- [ ] **Folia**: If you have access, test regionized behavior
- [ ] **Config reload**: Test `/eutils reload`
- [ ] **Permissions**: Test with and without permissions

### Test Scenarios

For feature changes, test:

1. Normal operation
2. Edge cases (max blocks, empty config, etc.)
3. Permission denied
4. Module disabled
5. Config reload

---

## Questions?

- Open a [Discussion](https://github.com/cryptofyre/EssentialUtils/discussions) for questions
- Join our community for real-time help

Thank you for contributing!
