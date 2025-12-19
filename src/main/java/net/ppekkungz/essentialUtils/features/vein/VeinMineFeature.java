package net.ppekkungz.essentialUtils.features.vein;

import net.ppekkungz.essentialUtils.config.PluginConfig;
import net.ppekkungz.essentialUtils.features.Feature;
import net.ppekkungz.essentialUtils.util.BlockUtil;
import net.ppekkungz.essentialUtils.util.HarvestUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * VeinMiner feature - mines connected ore veins including diagonal blocks.
 * Always active when using a pickaxe on ores.
 * Supports Fortune and Silk Touch enchantments.
 */
public class VeinMineFeature implements Feature {
    private final PluginConfig cfg;
    
    // All mineable ores
    private static final Set<Material> ORES = new HashSet<>();
    
    static {
        // Standard ores
        for (Material m : Material.values()) {
            String name = m.name();
            if (name.endsWith("_ORE") && m.isBlock()) {
                ORES.add(m);
            }
        }
        // Ancient debris
        ORES.add(Material.ANCIENT_DEBRIS);
    }

    public VeinMineFeature(PluginConfig cfg) {
        this.cfg = cfg;
    }

    @Override 
    public String name() { 
        return "Vein Miner"; 
    }

    @Override 
    public boolean canTrigger(Player p, Block origin) {
        // VeinMiner is always active with pickaxe - no crouch required
        Material blockType = origin.getType();
        return isOre(blockType);
    }

    @Override 
    public Set<Block> collectTargets(Player p, Block origin) {
        int limit = cfg.veinMinerMaxOres();
        Material ore = origin.getType();
        
        // Check if player's pickaxe tier is sufficient
        ItemStack tool = p.getInventory().getItemInMainHand();
        int playerTier = HarvestUtil.pickaxeTier(tool);
        int requiredTier = HarvestUtil.requiredTierForOre(ore);
        
        if (playerTier < requiredTier) {
            return Collections.emptySet();
        }
        
        Set<Block> result = new LinkedHashSet<>();
        Deque<Block> queue = new ArrayDeque<>();
        
        queue.add(origin);
        result.add(origin);

        // BFS using 26-neighbor search (3x3x3 cube, includes diagonals)
        while (!queue.isEmpty() && result.size() < limit) {
            Block b = queue.poll();
            
            // Use neighbors27 for diagonal ore detection
            for (Block n : BlockUtil.neighbors27(b)) {
                if (result.size() >= limit) break;
                
                // Match same ore type (including deepslate variants)
                if (!result.contains(n) && isSameOreType(ore, n.getType())) {
                    // Verify player can mine this block
                    if (playerTier >= HarvestUtil.requiredTierForOre(n.getType())) {
                        result.add(n);
                        queue.add(n);
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * Check if a material is an ore.
     */
    public static boolean isOre(Material m) {
        return ORES.contains(m);
    }

    /**
     * Check if two ore types are the same (accounting for deepslate variants).
     * For example, COAL_ORE and DEEPSLATE_COAL_ORE are considered the same.
     */
    public static boolean isSameOreType(Material ore1, Material ore2) {
        if (ore1 == ore2) return true;
        
        String name1 = normalizeOreName(ore1.name());
        String name2 = normalizeOreName(ore2.name());
        
        return name1.equals(name2);
    }

    /**
     * Normalize ore name by removing DEEPSLATE_ prefix.
     */
    private static String normalizeOreName(String name) {
        if (name.startsWith("DEEPSLATE_")) {
            return name.substring("DEEPSLATE_".length());
        }
        return name;
    }

    /**
     * Get all ore materials.
     */
    public static Set<Material> getAllOres() {
        return Collections.unmodifiableSet(ORES);
    }
}
