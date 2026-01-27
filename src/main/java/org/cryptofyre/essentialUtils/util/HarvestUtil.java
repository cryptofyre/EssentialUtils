package org.cryptofyre.essentialUtils.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class HarvestUtil {
    private HarvestUtil(){}

    public static int pickaxeTier(ItemStack tool) {
        if (tool == null) return -1;
        String n = tool.getType().name();
        if (!n.endsWith("_PICKAXE")) return -1;
        if (n.startsWith("WOODEN_") || n.startsWith("GOLDEN_")) return 0;
        if (n.startsWith("STONE_")) return 1;
        if (n.startsWith("IRON_")) return 2;
        if (n.startsWith("DIAMOND_")) return 3;
        if (n.startsWith("NETHERITE_")) return 4;
        return -1;
    }

    public static int requiredTierForOre(Material ore) {
        String n = ore.name();
        if (n.equals("ANCIENT_DEBRIS")) return 3;           // diamond+
        if (n.endsWith("REDSTONE_ORE")) return 2;           // iron+
        if (n.endsWith("GOLD_ORE")) return 2;               // iron+
        if (n.endsWith("DIAMOND_ORE")) return 2;            // iron+
        if (n.endsWith("EMERALD_ORE")) return 2;            // iron+
        if (n.endsWith("COPPER_ORE")) return 1;             // stone+
        if (n.endsWith("LAPIS_ORE")) return 1;              // stone+
        if (n.endsWith("IRON_ORE")) return 1;               // stone+
        if (n.endsWith("COAL_ORE")) return 0;               // any
        if (n.endsWith("NETHER_QUARTZ_ORE")) return 0;      // any
        if (n.endsWith("NETHER_GOLD_ORE")) return 0;        // any
        return 2; // safe default
    }

    public static String tierName(int tier) {
        return switch (tier) {
            case 0 -> "Wood/Gold";
            case 1 -> "Stone";
            case 2 -> "Iron";
            case 3 -> "Diamond";
            case 4 -> "Netherite";
            default -> "Unknown";
        };
    }
}
