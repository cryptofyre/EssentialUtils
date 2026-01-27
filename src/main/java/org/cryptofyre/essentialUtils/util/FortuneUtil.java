package org.cryptofyre.essentialUtils.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Utility for calculating Fortune enchantment effects on ore drops.
 * Follows vanilla Minecraft drop mechanics.
 */
public final class FortuneUtil {
    private static final Random random = new Random();
    
    private FortuneUtil() {}
    
    /**
     * Get the Fortune level from a tool.
     */
    public static int getFortuneLevel(ItemStack tool) {
        if (tool == null || tool.getItemMeta() == null) return 0;
        return tool.getEnchantmentLevel(Enchantment.FORTUNE);
    }
    
    /**
     * Check if tool has Silk Touch.
     */
    public static boolean hasSilkTouch(ItemStack tool) {
        if (tool == null || tool.getItemMeta() == null) return false;
        return tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0;
    }
    
    /**
     * Calculate the drop count for an ore based on Fortune level.
     * Returns the multiplier/count based on vanilla mechanics.
     */
    public static int calculateDropCount(Material ore, int fortuneLevel) {
        // Coal, Diamond, Emerald, Lapis, Nether Quartz, Nether Gold use "uniform bonus" formula
        // Redstone and Copper use different mechanics
        
        String name = ore.name();
        
        // Ores that drop multiple items (like Lapis, Redstone)
        if (name.contains("LAPIS")) {
            // Base 4-9, fortune adds more
            int base = 4 + random.nextInt(6); // 4-9
            return applyUniformBonus(base, fortuneLevel);
        }
        
        if (name.contains("REDSTONE")) {
            // Base 4-5, fortune adds more
            int base = 4 + random.nextInt(2); // 4-5
            return applyUniformBonus(base, fortuneLevel);
        }
        
        if (name.contains("COPPER")) {
            // Raw copper drops 2-5
            int base = 2 + random.nextInt(4); // 2-5
            return applyUniformBonus(base, fortuneLevel);
        }
        
        if (name.equals("NETHER_GOLD_ORE")) {
            // 2-6 gold nuggets
            int base = 2 + random.nextInt(5); // 2-6
            return applyUniformBonus(base, fortuneLevel);
        }
        
        // Standard ores (Coal, Diamond, Emerald, Quartz, Iron, Gold) - 1 base drop
        // Fortune: 33% chance per level to multiply (1 to fortune+1)
        return applyStandardFortuneBonus(1, fortuneLevel);
    }
    
    /**
     * Apply standard fortune bonus (used by Coal, Diamond, Emerald, Quartz).
     * Formula: Each level of fortune gives 1/(fortune+2) chance for +1 drop, up to fortune+1 drops.
     */
    private static int applyStandardFortuneBonus(int baseCount, int fortuneLevel) {
        if (fortuneLevel <= 0) return baseCount;
        
        // Vanilla formula: drops = base * (1 + random(0 to fortune))
        // But capped at fortune+1 multiplier
        int multiplier = 1 + random.nextInt(fortuneLevel + 1);
        return baseCount * multiplier;
    }
    
    /**
     * Apply uniform bonus (used by Lapis, Redstone, Copper, Nether Gold).
     * Adds 0 to fortuneLevel extra drops.
     */
    private static int applyUniformBonus(int baseCount, int fortuneLevel) {
        if (fortuneLevel <= 0) return baseCount;
        return baseCount + random.nextInt(fortuneLevel + 1);
    }
    
    /**
     * Get the XP amount dropped by an ore.
     */
    public static int getOreXP(Material ore) {
        String name = ore.name();
        
        if (name.contains("COAL")) return random.nextInt(3); // 0-2
        if (name.contains("DIAMOND")) return random.nextInt(5) + 3; // 3-7
        if (name.contains("EMERALD")) return random.nextInt(5) + 3; // 3-7
        if (name.contains("LAPIS")) return random.nextInt(4) + 2; // 2-5
        if (name.contains("REDSTONE")) return random.nextInt(4) + 1; // 1-4 (when broken without silk)
        if (name.contains("NETHER_QUARTZ")) return random.nextInt(4) + 2; // 2-5
        if (name.equals("NETHER_GOLD_ORE")) return random.nextInt(2); // 0-1
        if (name.equals("ANCIENT_DEBRIS")) return 0; // No XP
        
        // Iron, Gold, Copper ores don't drop XP when mined (only when smelted)
        if (name.contains("IRON") || name.contains("GOLD") || name.contains("COPPER")) {
            return 0;
        }
        
        return 0;
    }
    
    /**
     * Get the drop material for an ore.
     */
    public static Material getOreDrop(Material ore) {
        String name = ore.name();
        
        if (name.contains("COAL")) return Material.COAL;
        if (name.contains("DIAMOND")) return Material.DIAMOND;
        if (name.contains("EMERALD")) return Material.EMERALD;
        if (name.contains("LAPIS")) return Material.LAPIS_LAZULI;
        if (name.contains("REDSTONE")) return Material.REDSTONE;
        if (name.contains("NETHER_QUARTZ") || name.equals("NETHER_QUARTZ_ORE")) return Material.QUARTZ;
        if (name.equals("NETHER_GOLD_ORE")) return Material.GOLD_NUGGET;
        if (name.contains("COPPER")) return Material.RAW_COPPER;
        if (name.contains("IRON")) return Material.RAW_IRON;
        if (name.contains("GOLD") && !name.equals("NETHER_GOLD_ORE")) return Material.RAW_GOLD;
        if (name.equals("ANCIENT_DEBRIS")) return Material.ANCIENT_DEBRIS;
        
        // Default: return the ore itself (for unknown ores)
        return ore;
    }
    
    /**
     * Check if an ore drops itself (needs smelting) or drops resources directly.
     */
    public static boolean dropsRawMaterial(Material ore) {
        String name = ore.name();
        // These ores drop raw materials that need smelting
        return name.contains("IRON") || 
               (name.contains("GOLD") && !name.equals("NETHER_GOLD_ORE")) || 
               name.contains("COPPER") ||
               name.equals("ANCIENT_DEBRIS");
    }
    
    /**
     * Get a friendly name for the ore (for display purposes).
     */
    public static String getOreFriendlyName(Material ore) {
        String name = ore.name();
        // Convert DEEPSLATE_COAL_ORE -> Coal Ore
        name = name.replace("DEEPSLATE_", "");
        name = name.replace("_", " ");
        // Title case
        StringBuilder result = new StringBuilder();
        for (String word : name.toLowerCase().split(" ")) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        return result.toString().trim();
    }
    
    /**
     * Get the drop material friendly name.
     */
    public static String getDropFriendlyName(Material drop) {
        String name = drop.name().replace("_", " ");
        StringBuilder result = new StringBuilder();
        for (String word : name.toLowerCase().split(" ")) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        return result.toString().trim();
    }
}

