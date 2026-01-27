package org.cryptofyre.essentialUtils.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility for calculating leaf drops (saplings and apples) based on vanilla Minecraft mechanics.
 */
public final class LeafDropUtil {
    private static final Random random = new Random();
    
    private LeafDropUtil() {}
    
    /**
     * Tree type enumeration for drop rate lookup.
     */
    public enum TreeType {
        OAK(0.05, 0.005),           // 5% sapling, 0.5% apple
        DARK_OAK(0.05, 0.005),      // 5% sapling, 0.5% apple
        BIRCH(0.05, 0.0),           // 5% sapling, no apples
        SPRUCE(0.05, 0.0),          // 5% sapling, no apples
        JUNGLE(0.025, 0.0),         // 2.5% sapling, no apples
        ACACIA(0.05, 0.0),          // 5% sapling, no apples
        MANGROVE(0.05, 0.0),        // 5% propagule, no apples
        CHERRY(0.05, 0.0),          // 5% sapling, no apples
        PALE_OAK(0.05, 0.0),        // 5% sapling, no apples (assumed)
        AZALEA(0.05, 0.0),          // 5% sapling, no apples
        UNKNOWN(0.05, 0.0);         // Default fallback
        
        public final double saplingChance;
        public final double appleChance;
        
        TreeType(double saplingChance, double appleChance) {
            this.saplingChance = saplingChance;
            this.appleChance = appleChance;
        }
    }
    
    /**
     * Determine tree type from a leaf block material.
     */
    public static TreeType getTreeType(Material leaf) {
        String name = leaf.name();
        
        if (name.contains("DARK_OAK")) return TreeType.DARK_OAK;
        if (name.contains("OAK") && !name.contains("PALE")) return TreeType.OAK;
        if (name.contains("BIRCH")) return TreeType.BIRCH;
        if (name.contains("SPRUCE")) return TreeType.SPRUCE;
        if (name.contains("JUNGLE")) return TreeType.JUNGLE;
        if (name.contains("ACACIA")) return TreeType.ACACIA;
        if (name.contains("MANGROVE")) return TreeType.MANGROVE;
        if (name.contains("CHERRY")) return TreeType.CHERRY;
        if (name.contains("PALE_OAK")) return TreeType.PALE_OAK;
        if (name.contains("AZALEA")) return TreeType.AZALEA;
        
        return TreeType.UNKNOWN;
    }
    
    /**
     * Determine tree type from a log block material.
     */
    public static TreeType getTreeTypeFromLog(Material log) {
        String name = log.name().replace("STRIPPED_", "");
        
        if (name.contains("DARK_OAK")) return TreeType.DARK_OAK;
        if (name.contains("OAK") && !name.contains("PALE")) return TreeType.OAK;
        if (name.contains("BIRCH")) return TreeType.BIRCH;
        if (name.contains("SPRUCE")) return TreeType.SPRUCE;
        if (name.contains("JUNGLE")) return TreeType.JUNGLE;
        if (name.contains("ACACIA")) return TreeType.ACACIA;
        if (name.contains("MANGROVE")) return TreeType.MANGROVE;
        if (name.contains("CHERRY")) return TreeType.CHERRY;
        if (name.contains("PALE_OAK")) return TreeType.PALE_OAK;
        
        return TreeType.UNKNOWN;
    }
    
    /**
     * Get the sapling material for a tree type.
     */
    public static Material getSapling(TreeType type) {
        return switch (type) {
            case OAK -> Material.OAK_SAPLING;
            case DARK_OAK -> Material.DARK_OAK_SAPLING;
            case BIRCH -> Material.BIRCH_SAPLING;
            case SPRUCE -> Material.SPRUCE_SAPLING;
            case JUNGLE -> Material.JUNGLE_SAPLING;
            case ACACIA -> Material.ACACIA_SAPLING;
            case MANGROVE -> Material.MANGROVE_PROPAGULE;
            case CHERRY -> Material.CHERRY_SAPLING;
            case PALE_OAK -> Material.PALE_OAK_SAPLING;
            case AZALEA -> Material.AZALEA;
            case UNKNOWN -> Material.OAK_SAPLING;
        };
    }
    
    /**
     * Get the sapling material for a log type.
     */
    public static Material getSaplingForLog(Material log) {
        return getSapling(getTreeTypeFromLog(log));
    }
    
    /**
     * Calculate drops for a set of leaves.
     * Returns a list of ItemStacks to drop.
     */
    public static List<ItemStack> calculateDropsForLeaves(Material leafType, int leafCount) {
        List<ItemStack> drops = new ArrayList<>();
        TreeType type = getTreeType(leafType);
        
        int saplings = 0;
        int apples = 0;
        int sticks = 0;
        
        for (int i = 0; i < leafCount; i++) {
            // Sapling chance
            if (random.nextDouble() < type.saplingChance) {
                saplings++;
            }
            
            // Apple chance (only Oak and Dark Oak)
            if (random.nextDouble() < type.appleChance) {
                apples++;
            }
            
            // Stick chance (2% for all leaves, drops 1-2 sticks)
            if (random.nextDouble() < 0.02) {
                sticks += 1 + random.nextInt(2);
            }
        }
        
        if (saplings > 0) {
            drops.add(new ItemStack(getSapling(type), saplings));
        }
        if (apples > 0) {
            drops.add(new ItemStack(Material.APPLE, apples));
        }
        if (sticks > 0) {
            drops.add(new ItemStack(Material.STICK, sticks));
        }
        
        return drops;
    }
    
    /**
     * Result class for tracking tree feller drops.
     */
    public static class TreeFellerResult {
        public int logs = 0;
        public int leaves = 0;
        public int saplings = 0;
        public int apples = 0;
        public int sticks = 0;
        
        public void addLeafDrops(TreeType type) {
            leaves++;
            
            if (random.nextDouble() < type.saplingChance) {
                saplings++;
            }
            if (random.nextDouble() < type.appleChance) {
                apples++;
            }
            if (random.nextDouble() < 0.02) {
                sticks += 1 + random.nextInt(2);
            }
        }
        
        public void addLog() {
            logs++;
        }
        
        public List<ItemStack> toItemStacks(TreeType type) {
            List<ItemStack> drops = new ArrayList<>();
            if (saplings > 0) {
                drops.add(new ItemStack(getSapling(type), saplings));
            }
            if (apples > 0) {
                drops.add(new ItemStack(Material.APPLE, apples));
            }
            if (sticks > 0) {
                drops.add(new ItemStack(Material.STICK, sticks));
            }
            return drops;
        }
    }
    
    /**
     * Check if a material is a leaf block.
     */
    public static boolean isLeaf(Material m) {
        return m.name().endsWith("_LEAVES");
    }
}

