package org.cryptofyre.essentialUtils.features.farm;

import org.cryptofyre.essentialUtils.config.PluginConfig;
import org.cryptofyre.essentialUtils.features.Feature;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * AutoFarm feature - harvests mature crops in a radius when using a hoe.
 * Always active when using a hoe on mature crops.
 * Supports auto-replanting.
 */
public class AutoFarmFeature implements Feature {
    private final PluginConfig cfg;
    
    // Crops that can be harvested and replanted
    private static final Set<Material> REPLANTABLE_CROPS = Set.of(
        Material.WHEAT,
        Material.CARROTS,
        Material.POTATOES,
        Material.BEETROOTS,
        Material.NETHER_WART
    );
    
    // All harvestable crops
    private static final Set<Material> ALL_CROPS = Set.of(
        Material.WHEAT,
        Material.CARROTS,
        Material.POTATOES,
        Material.BEETROOTS,
        Material.NETHER_WART,
        Material.SWEET_BERRY_BUSH,
        Material.COCOA,
        Material.MELON,
        Material.PUMPKIN
    );

    public AutoFarmFeature(PluginConfig cfg) {
        this.cfg = cfg;
    }

    @Override
    public String name() {
        return "Auto Farm";
    }

    @Override
    public boolean canTrigger(Player p, Block origin) {
        // AutoFarm is always active with hoe - no crouch required
        return isCrop(origin.getType()) && isMature(origin);
    }

    @Override
    public Set<Block> collectTargets(Player p, Block origin) {
        int radius = cfg.autoFarmRadius();
        Set<Block> out = new HashSet<>();

        // Scan a square area around the origin block
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Block b = origin.getRelative(dx, 0, dz);
                if (isCrop(b.getType()) && isMature(b)) {
                    out.add(b);
                }
            }
        }
        return out;
    }

    /**
     * Check if a material is a crop.
     */
    public static boolean isCrop(Material m) {
        return ALL_CROPS.contains(m);
    }

    /**
     * Check if a block is mature (fully grown).
     */
    public static boolean isMature(Block b) {
        Material type = b.getType();
        
        // Sweet berry bush: mature at age 2-3 (can harvest at 2, max at 3)
        if (type == Material.SWEET_BERRY_BUSH) {
            if (b.getBlockData() instanceof Ageable age) {
                return age.getAge() >= 2;
            }
            return false;
        }
        
        // Standard ageable crops
        if (b.getBlockData() instanceof Ageable age) {
            return age.getAge() >= age.getMaximumAge();
        }
        
        // Melon and pumpkin are always "mature" (they're the fruit, not the stem)
        if (type == Material.MELON || type == Material.PUMPKIN) {
            return true;
        }
        
        return false;
    }

    /**
     * Check if a crop can be replanted.
     */
    public static boolean canReplant(Material crop) {
        return REPLANTABLE_CROPS.contains(crop);
    }

    /**
     * Get the seed material for a crop.
     */
    public static Material getSeed(Material crop) {
        return switch (crop) {
            case WHEAT -> Material.WHEAT_SEEDS;
            case CARROTS -> Material.CARROT;
            case POTATOES -> Material.POTATO;
            case BEETROOTS -> Material.BEETROOT_SEEDS;
            case NETHER_WART -> Material.NETHER_WART;
            default -> null;
        };
    }

    /**
     * Get the crop block type for a seed.
     */
    public static Material getCropBlock(Material seed) {
        return switch (seed) {
            case WHEAT_SEEDS -> Material.WHEAT;
            case CARROT -> Material.CARROTS;
            case POTATO -> Material.POTATOES;
            case BEETROOT_SEEDS -> Material.BEETROOTS;
            case NETHER_WART -> Material.NETHER_WART;
            default -> null;
        };
    }
}
