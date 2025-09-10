package net.ppekkungz.essentialUtils.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PluginConfig {
    private final FileConfiguration c;
    public PluginConfig(FileConfiguration c) { this.c = c; }

    // UI
    public boolean showProgress() { return c.getBoolean("ui.showProgress", true); }
    public String indicatorMode() { return c.getString("ui.indicator", "actionbar"); }

    // Performance
    public int perPlayerBudget() { return c.getInt("performance.perPlayerWorkBudgetPerTick", 60); }
    public int idleTimeoutSeconds() { return c.getInt("performance.idleTimeoutSeconds", 12); }

    // Tree
    public boolean treeEnabled() { return c.getBoolean("features.treeAssist.enabled", true); }
    public int treeMaxPerTick() { return c.getInt("features.treeAssist.maxLogsPerTick", 24); }
    public int treeMaxLogs() { return c.getInt("features.treeAssist.maxLogsPerTree", 160); }
    public int treeRadius() { return c.getInt("features.treeAssist.searchRadius", 6); }
    public boolean treeRequireLeaves() { return c.getBoolean("features.treeAssist.requireLeavesNearby", true); }
    public boolean treeIncludeStripped() { return c.getBoolean("features.treeAssist.includeStrippedLogs", true); }
    public boolean treeReplantEnabled() { return c.getBoolean("features.treeAssist.replant.enabled", true); }
    public boolean treeReplantRequireInventory() { return c.getBoolean("features.treeAssist.replant.requireSaplingInInventory", false); }
    public int treeReplantRetryCooldownTicks() { return c.getInt("features.treeAssist.replant.retryCooldownTicks", 5); }
    public int treeReplantMaxRetries() { return c.getInt("features.treeAssist.replant.maxRetries", 10); }
    public Set<Material> treeAllowedSoils() {
        List<String> raw = c.getStringList("features.treeAssist.replant.allowedSoils");
        Set<Material> out = new HashSet<>();
        for (String s : raw) { try { out.add(Material.valueOf(s)); } catch (Exception ignored) {} }
        return out;
    }

    // Vein
    public boolean veinEnabled() { return c.getBoolean("features.veinMine.enabled", true); }
    public int veinMaxPerTick() { return c.getInt("features.veinMine.maxPerTick", 32); }
    public int veinMaxOres() { return c.getInt("features.veinMine.maxOresPerVein", 128); }
    public boolean veinIncludeVariants() { return c.getBoolean("features.veinMine.includeVariants", true); }
    public List<String> veinWhitelist() { return c.getStringList("features.veinMine.whitelist"); }
    public List<String> veinBlacklist() { return c.getStringList("features.veinMine.blacklist"); }

    // Farm
    public boolean farmEnabled() { return c.getBoolean("features.autoFarm.enabled", true); }
    public int farmRadius() { return c.getInt("features.autoFarm.harvestRadius", 4); }
    public int farmMaxPerTick() { return c.getInt("features.autoFarm.maxCropsPerTick", 40); }
    public List<String> farmWhitelist() { return c.getStringList("features.autoFarm.cropsWhitelist"); }
    public List<String> farmBlacklist() { return c.getStringList("features.autoFarm.cropsBlacklist"); }

    // Safety
    public boolean requireChunkLoaded() { return c.getBoolean("safety.requireChunkLoaded", true); }
}
