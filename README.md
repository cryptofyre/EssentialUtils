# Essential Utils ![Java](https://img.shields.io/badge/Java-21-orange) ![Gradle](https://img.shields.io/badge/Gradle-Build-brightgreen) ![PaperMC](https://img.shields.io/badge/PaperMC-1.21.8-blue) ![Folia](https://img.shields.io/badge/Folia-Supported-success)

> A **Folia-optimized survival convenience plugin** for Paper/Folia 1.21.8.  
> Adds smooth, lag-free mechanics like tree chopping, vein mining, and auto-farming.

---

## ✨ Features

- **🌲 Tree Assist (axe)**
  - Incrementally chops connected logs with **per-block delay** (smooth, no spikes).
  - **Leaf checks** to avoid cutting adjacent trees.
  - **Optional auto-replant** (saplings/propagules), retries with cooldown until successful.
  - Durability decreases **once per N blocks**, not per block.

- **⛏ Vein Mine (pickaxe)**
  - Incrementally mines connected ore veins.
  - Enforces **Minecraft tool tier rules**:
    - If tier too low → **no break**, **alert shown** (rate-limited).
  - Delay per block scales with vein size.
  - Durability decreases once per N blocks.

- **🌾 Auto Farm (hoe)**
  - Harvests **only mature crops** in a radius.
  - No auto-replant (intentional).
  - Optimized for local chunk scans.

- **⚙ Activation**
  - **Hold Shift for 5 seconds** → arms tool (progress bar shown).
  - Once armed, stays armed even after releasing Shift.
  - **Deactivates only when item is changed** or player quits.

- **🛡 Safety**
  - Respects protections (future: WorldGuard/GriefDefender hooks).
  - Requires loaded chunks.
  - Folia region-safe scheduling only (no async hacks).

- **📊 Performance**
  - Centralized per-player tick budget.
  - Strict per-feature caps.
  - Smooth staggered breaking.

---

## 📂 Project Structure

```
src/main/java/net/ppekkungz/essentialUtils/
├── EssentialUtils.java
├── config/PluginConfig.java
├── state/StateManager.java
├── listener/ActivationListener.java
├── work/
│   ├── WorkService.java
│   ├── WorkItem.java
│   └── WorkQueue.java
├── features/
│   ├── Feature.java
│   ├── tree/TreeAssistFeature.java
│   ├── vein/VeinMineFeature.java
│   └── farm/AutoFarmFeature.java
├── util/
│   ├── HarvestUtil.java
│   ├── Materials.java
│   └── Ores.java
└── indicator/
    ├── BossBarIndicator.java
    └── ActionBarIndicator.java
```

---

## ⚡ Usage

1. Drop `EssentialUtils-x.x.x.jar` into your `plugins/` folder.
2. Run with **Paper 1.21.8+** and **Folia**.
3. Config (`config.yml`) is generated on first run.

### Commands
```bash
/eutils reload   # reloads config.yml
```

### Permission
```
essentialutils.admin   # required for /eutils reload (default: op)
```

---

## ⚙ Config Highlights

```yaml
activation:
  holdSneakSeconds: 5
  requireToolWhileHold: true

features:
  treeAssist:
    breakDelayTicks: 2
    durabilityPerNBlocks: 8
    replant:
      enabled: true
      retryCooldownTicks: 5
      maxRetries: 10

  veinMine:
    breakDelayBaseTicks: 1
    breakDelayExtraPer16: 1
    durabilityPerNBlocks: 10
    alertOnInsufficientTier: true

  autoFarm:
    replant: false
```

---

## 🛠 Build

Requires:
- JDK **21**
- Gradle (wrapper included)

Build with:
```bash
./gradlew build
```

Jar output:  
```
build/libs/essential-utils-<version>.jar
```

---

## ✅ TODO / Roadmap

- [x] Tree Assist (incremental, replant, durability-per-N)
- [x] Vein Mine (incremental, tier alerts, delay scaling, durability-per-N)
- [x] Auto Farm (mature crops only, no replant)
- [x] Activation (Shift-hold progress, persists armed, deactivates on item change)
- [x] Config reload (`/eutils reload`)
- [x] Folia-safe scheduling
- [x] Strict per-feature caps
- [ ] Protection hooks (WorldGuard/GriefDefender)
- [ ] BossBar indicators during ACTIVE
- [ ] Configurable durability rules per tool type
- [ ] Additional auto-farm crops (bamboo, sugarcane, kelp)
- [ ] Multi-language messages
- [ ] Metrics toggle (bStats)
- [ ] Per-player toggle commands (enable/disable features individually)

---

## 📜 License

MIT © 2025 ppekkungz
