package net.ppekkungz.essentialUtils.work;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.ppekkungz.essentialUtils.EssentialUtils;
import net.ppekkungz.essentialUtils.config.PluginConfig;
import net.ppekkungz.essentialUtils.features.farm.AutoFarmFeature;
import net.ppekkungz.essentialUtils.indicator.ActionBarService;
import net.ppekkungz.essentialUtils.state.PlayerState;
import net.ppekkungz.essentialUtils.state.StateManager;
import net.ppekkungz.essentialUtils.util.FortuneUtil;
import net.ppekkungz.essentialUtils.util.LeafDropUtil;
import net.ppekkungz.essentialUtils.util.Protection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Folia-safe work processing service.
 * Handles block breaking, item drops, XP spawning, and replanting.
 */
public class WorkService {
    private final EssentialUtils plugin;
    private final PluginConfig cfg;
    private final StateManager states;
    private final ActionBarService actionBar;

    private final Map<UUID, WorkQueue> queues = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> loops = new ConcurrentHashMap<>();

    public WorkService(EssentialUtils plugin, PluginConfig cfg, StateManager states, ActionBarService actionBar) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.states = states;
        this.actionBar = actionBar;
    }

    public WorkQueue queue(Player p) {
        return queues.computeIfAbsent(p.getUniqueId(), k -> new WorkQueue());
    }

    public void ensureLoop(Player p) {
        loops.computeIfAbsent(p.getUniqueId(), id ->
                p.getScheduler().runAtFixedRate(plugin, task -> tickPlayer(p), null, 1L, 1L)
        );
    }

    public void stopLoop(Player p) {
        ScheduledTask t = loops.remove(p.getUniqueId());
        if (t != null) t.cancel();
        queues.remove(p.getUniqueId());
    }

    public void shutdown() {
        loops.values().forEach(ScheduledTask::cancel);
        loops.clear();
        queues.clear();
    }

    private void tickPlayer(Player p) {
        if (!p.isOnline()) {
            stopLoop(p);
            return;
        }
        
        WorkQueue q = queue(p);
        if (q.isEmpty()) {
            if (states.get(p) == PlayerState.ACTIVE) {
                // Processing complete - show summaries and cleanup
                finishProcessing(p);
            }
            return;
        }

        final int budget = cfg.blocksPerTick();
        EnumMap<WorkItem.FeatureTag, Integer> used = new EnumMap<>(WorkItem.FeatureTag.class);
        for (WorkItem.FeatureTag t : WorkItem.FeatureTag.values()) {
            used.put(t, 0);
        }

        int processed = 0;
        int guard = Math.max(64, q.size() * 2);

        for (int i = 0; i < guard && processed < budget; i++) {
            WorkItem wi = q.poll();
            if (wi == null) break;

            // Handle cooldown
            if (wi.cooldownTicks > 0) {
                wi.cooldownTicks--;
                q.add(wi);
                continue;
            }

            Block b = wi.block;
            
            // Chunk check
            if (cfg.requireChunkLoaded() && !b.getChunk().isLoaded()) {
                wi.cooldownTicks = 5;
                q.add(wi);
                continue;
            }
            
            // Protection check
            if (!Protection.canModify(wi.player, b)) {
                continue;
            }

            // Process the work item
            switch (wi.action) {
                case BREAK -> handleBreak(wi);
                case PLANT -> handlePlant(wi, q);
                case REPLANT -> handleReplant(wi, q);
            }

            used.put(wi.tag, used.getOrDefault(wi.tag, 0) + 1);
            processed++;
        }
    }

    /**
     * Handle block breaking based on feature type.
     */
    private void handleBreak(WorkItem wi) {
        Block b = wi.block;
        Player p = wi.player;
        
        if (b.getType().isAir()) return;
        
        switch (wi.tag) {
            case TREE -> handleTreeBreak(wi);
            case VEIN -> handleVeinBreak(wi);
            case FARM -> handleFarmBreak(wi);
            default -> b.breakNaturally(p.getInventory().getItemInMainHand(), true);
        }
    }

    /**
     * Handle tree block breaking (logs and leaves).
     */
    private void handleTreeBreak(WorkItem wi) {
        Block b = wi.block;
        Player p = wi.player;
        
        LeafDropUtil.TreeFellerResult result = states.getTreeFellerResult(p);
        
        if (wi.isLeaf) {
            // Get tree type for drop calculations
            Material logType = states.getTreeFellerLogType(p);
            LeafDropUtil.TreeType treeType = logType != null 
                ? LeafDropUtil.getTreeTypeFromLog(logType) 
                : LeafDropUtil.TreeType.OAK;
            
            // Calculate drops instead of using breakNaturally
            if (result != null) {
                result.addLeafDrops(treeType);
            }
            
            // Break the leaf silently (drops calculated above)
            // Note: Leaves don't damage axes in vanilla Minecraft
            b.setType(Material.AIR);
        } else {
            // Log - break naturally and damage tool
            b.breakNaturally(p.getInventory().getItemInMainHand(), true);
            if (result != null) {
                result.addLog();
            }
            // breakNaturally doesn't damage the tool, we need to do it manually
            damageToolSlightly(p);
        }
    }

    /**
     * Handle ore breaking with Fortune/Silk Touch.
     */
    private void handleVeinBreak(WorkItem wi) {
        Block b = wi.block;
        Player p = wi.player;
        ItemStack tool = p.getInventory().getItemInMainHand();
        Material oreType = b.getType();
        
        VeinMineResult result = states.getVeinMineResult(p);
        
        boolean silkTouch = FortuneUtil.hasSilkTouch(tool);
        int fortuneLevel = FortuneUtil.getFortuneLevel(tool);
        
        if (silkTouch && cfg.veinMinerSilkTouchDropsOre()) {
            // Silk Touch: drop the ore block itself
            b.setType(Material.AIR);
            b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), new ItemStack(oreType, 1));
            
            if (result != null) {
                result.addMinedBlock();
                result.addDrops(oreType, 1);
                result.setSilkTouch(true);
            }
        } else {
            // Fortune or normal: calculate drops
            Material dropType = FortuneUtil.getOreDrop(oreType);
            int dropCount = cfg.veinMinerFortuneEnabled() 
                ? FortuneUtil.calculateDropCount(oreType, fortuneLevel)
                : 1;
            int xp = FortuneUtil.getOreXP(oreType);
            
            // Break block and drop items
            b.setType(Material.AIR);
            if (dropCount > 0) {
                b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), new ItemStack(dropType, dropCount));
            }
            
            if (result != null) {
                result.addMinedBlock();
                result.addDrops(dropType, dropCount);
                result.addXP(xp);
                result.setFortuneLevel(fortuneLevel);
            }
        }
        
        // Damage tool
        damageToolSlightly(p);
    }

    /**
     * Handle crop breaking with auto-replant.
     */
    private void handleFarmBreak(WorkItem wi) {
        Block b = wi.block;
        Player p = wi.player;
        Material cropType = b.getType();
        
        // Break naturally
        // Note: Hoes don't take damage when breaking crops in vanilla
        // (only when tilling soil), so we don't call damageToolSlightly here
        b.breakNaturally(p.getInventory().getItemInMainHand(), true);
        
        // Queue replant if enabled
        if (cfg.autoFarmReplant() && AutoFarmFeature.canReplant(cropType)) {
            WorkItem replant = WorkItem.replantCrop(p, b, cropType);
            queue(p).add(replant);
        }
    }

    /**
     * Handle sapling planting with particles.
     */
    private void handlePlant(WorkItem wi, WorkQueue q) {
        Block airPos = wi.block;
        
        if (airPos.getType().isAir()) {
            Block soil = airPos.getRelative(0, -1, 0);
            if (isValidTreeSoil(soil.getType())) {
                    airPos.setType(wi.plantType, true);
                
                // Spawn green sparkle particles
                if (cfg.treeFellerParticles()) {
                    Location loc = airPos.getLocation().add(0.5, 0.5, 0.5);
                    airPos.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 10, 0.3, 0.3, 0.3, 0);
                }
                return;
            }
        }
        
        // Retry if not planted
        if (wi.retries > 0) {
            wi.retries--;
            wi.cooldownTicks = 5;
            q.add(wi);
        }
    }

    /**
     * Handle crop replanting.
     */
    private void handleReplant(WorkItem wi, WorkQueue q) {
        Block pos = wi.block;
        
        if (pos.getType().isAir()) {
            Block soil = pos.getRelative(0, -1, 0);
            Material seedCrop = AutoFarmFeature.getCropBlock(AutoFarmFeature.getSeed(wi.plantType));
            
            if (seedCrop != null && isValidFarmSoil(soil.getType(), wi.plantType)) {
                pos.setType(seedCrop, true);
                return;
            }
        }
        
        // Retry if not planted
        if (wi.retries > 0) {
            wi.retries--;
            wi.cooldownTicks = 2;
            q.add(wi);
        }
    }

    /**
     * Called when all work items are processed.
     */
    private void finishProcessing(Player p) {
        // Handle VeinMiner completion
        VeinMineResult veinResult = states.endVeinMine(p);
        if (veinResult != null && veinResult.hasData()) {
            // Spawn XP at origin location
            if (veinResult.getTotalXP() > 0) {
                Location loc = veinResult.getOriginLocation();
                if (loc != null && loc.getWorld() != null) {
                    loc.getWorld().spawn(loc.add(0.5, 0.5, 0.5), ExperienceOrb.class, orb -> {
                        orb.setExperience(veinResult.getTotalXP());
                    });
                }
            }
            
            // Show actionbar summary
            if (cfg.veinMinerShowSummary()) {
                String msg = formatVeinMinerSummary(veinResult);
                actionBar.showTimed(p, msg, cfg.veinMinerSummaryDuration());
            }
        }
        
        // Handle TreeFeller completion
        LeafDropUtil.TreeFellerResult treeResult = states.endTreeFeller(p);
        if (treeResult != null && treeResult.logs > 0) {
            Material logType = states.getTreeFellerLogType(p);
            Location stumpLoc = states.getTreeFellerStumpLocation(p);
            LeafDropUtil.TreeType treeType = logType != null 
                ? LeafDropUtil.getTreeTypeFromLog(logType) 
                : LeafDropUtil.TreeType.OAK;
            
            // Drop calculated items at stump location
            if (stumpLoc != null) {
                for (ItemStack drop : treeResult.toItemStacks(treeType)) {
                    stumpLoc.getWorld().dropItemNaturally(stumpLoc.add(0.5, 1, 0.5), drop);
                }
            }
            
            // Show summary
            if (cfg.treeFellerShowSummary()) {
                String msg = formatTreeFellerSummary(treeResult);
                actionBar.showTimed(p, msg, cfg.veinMinerSummaryDuration());
            }
        }
        
        states.set(p, PlayerState.IDLE);
        stopLoop(p);
    }

    /**
     * Format VeinMiner actionbar summary with nice separators.
     */
    private String formatVeinMinerSummary(VeinMineResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("&b⛏ ");
        
        List<String> parts = new ArrayList<>();
        
        String oreName = FortuneUtil.getOreFriendlyName(result.getOreType());
        String dropName = FortuneUtil.getDropFriendlyName(result.getPrimaryDrop());
        
        // Ore count
        parts.add("&ex" + result.getBlocksMined() + " &f" + oreName);
        
        // Drops with multiplier
        if (result.getTotalDrops() > 0) {
            String mult = result.getMultiplierString();
            parts.add("&f" + result.getTotalDrops() + " " + dropName + " &7(" + mult + ")");
        }
        
        // XP
        if (result.getTotalXP() > 0) {
            parts.add("&a" + result.getTotalXP() + " XP");
        }
        
        sb.append(String.join(" &8• ", parts));
        
        return sb.toString();
    }

    /**
     * Format TreeFeller actionbar summary.
     * Only shows non-zero entries with nice separators.
     */
    private String formatTreeFellerSummary(LeafDropUtil.TreeFellerResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("&a🌳 ");
        
        List<String> parts = new ArrayList<>();
        
        if (result.logs > 0) {
            parts.add("&f" + result.logs + " &7logs");
        }
        if (result.saplings > 0) {
            parts.add("&f" + result.saplings + " &7saplings");
        }
        if (result.apples > 0) {
            parts.add("&f" + result.apples + " &7apples");
        }
        if (result.sticks > 0) {
            parts.add("&f" + result.sticks + " &7sticks");
        }
        
        sb.append(String.join(" &8• ", parts));
        
        return sb.toString();
    }

    /**
     * Check if material is valid soil for trees.
     */
    private boolean isValidTreeSoil(Material m) {
        return m == Material.DIRT || 
               m == Material.GRASS_BLOCK || 
               m == Material.PODZOL || 
               m == Material.ROOTED_DIRT ||
               m == Material.MOSS_BLOCK ||
               m == Material.MUD ||
               m == Material.MUDDY_MANGROVE_ROOTS;
    }

    /**
     * Check if material is valid soil for farming.
     */
    private boolean isValidFarmSoil(Material soil, Material crop) {
        if (crop == Material.NETHER_WART) {
            return soil == Material.SOUL_SAND || soil == Material.SOUL_SOIL;
        }
        return soil == Material.FARMLAND;
    }

    /**
     * Damage tool with proper Unbreaking enchantment handling.
     * 
     * Unbreaking mechanics (for tools, not armor):
     * - Chance to consume durability = 1 / (unbreaking_level + 1)
     * - Unbreaking I: 50% chance
     * - Unbreaking II: 33.3% chance  
     * - Unbreaking III: 25% chance
     */
    private void damageToolSlightly(Player p) {
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (tool == null || tool.getType().isAir()) return;
        
        // Check if tool can be damaged
        if (!(tool.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable dmg)) {
            return;
        }
        
        // Get Unbreaking level
        int unbreakingLevel = tool.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.UNBREAKING);
        
        // Calculate if durability should be consumed
        // Formula: 1 / (unbreaking_level + 1) chance to consume
        if (unbreakingLevel > 0) {
            double chance = 1.0 / (unbreakingLevel + 1);
            if (Math.random() >= chance) {
                // Unbreaking saved the durability!
                return;
            }
        }
        
        // Check if tool would break
        int maxDurability = tool.getType().getMaxDurability();
        if (dmg.getDamage() >= maxDurability - 1) {
            // Tool is about to break - stop processing to prevent loss
            return;
        }
        
        // Apply damage
        dmg.setDamage(dmg.getDamage() + 1);
        tool.setItemMeta(dmg);
    }
}

