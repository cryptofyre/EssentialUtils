package org.cryptofyre.essentialUtils.features.tree;

import org.cryptofyre.essentialUtils.config.PluginConfig;
import org.cryptofyre.essentialUtils.features.Feature;
import org.cryptofyre.essentialUtils.util.BlockUtil;
import org.cryptofyre.essentialUtils.util.LeafDropUtil;
import org.cryptofyre.essentialUtils.util.Materials;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Tree Feller feature - breaks all logs and natural leaves of a tree.
 * Activated by crouching while breaking with an axe.
 * 
 * Leaf collection is limited to a horizontal radius from the trunk center
 * to prevent spreading to neighboring trees, while allowing unlimited Y
 * for tall trees. The radius is adjusted based on tree type.
 */
public class TreeAssistFeature implements Feature {
    private final PluginConfig cfg;
    
    // For initial leaf detection
    private static final int LEAF_CHECK_RADIUS = 4;

    public TreeAssistFeature(PluginConfig cfg) { 
        this.cfg = cfg; 
    }

    @Override 
    public String name() { 
        return "Tree Feller"; 
    }

    @Override 
    public boolean canTrigger(Player p, Block origin) {
        // Must be crouching and hitting a log
        if (!p.isSneaking()) return false;
        return Materials.isLog(origin.getType(), true);
    }

    @Override 
    public Set<Block> collectTargets(Player p, Block origin) {
        int limit = cfg.treeFellerMaxBlocks();
        
        // First, find the stump (lowest log)
        Block stump = findStump(origin);
        
        // Verify this is a natural tree (has leaves nearby)
        if (!hasNaturalLeavesNearby(origin, LEAF_CHECK_RADIUS)) {
            return Collections.emptySet();
        }
        
        // Determine tree type for leaf radius
        LeafDropUtil.TreeType treeType = LeafDropUtil.getTreeTypeFromLog(origin.getType());
        int maxLeafRadius = getLeafRadiusForTreeType(treeType);

        Set<Block> result = new LinkedHashSet<>();
        Set<Block> logs = new LinkedHashSet<>();
        Set<Block> leaves = new LinkedHashSet<>();
        Deque<Block> logQueue = new ArrayDeque<>();
        
        // Start BFS from stump for logs
        logQueue.add(stump);
        logs.add(stump);

        // Collect all connected logs
        while (!logQueue.isEmpty() && logs.size() < limit) {
            Block b = logQueue.poll();
            for (Block n : BlockUtil.neighbors27(b)) {
                if (logs.size() >= limit) break;
                if (!logs.contains(n) && Materials.isLog(n.getType(), true)) {
                    logs.add(n);
                    logQueue.add(n);
                }
            }
        }
        
        // Calculate trunk center (average X/Z of all logs)
        double centerX = 0, centerZ = 0;
        for (Block log : logs) {
            centerX += log.getX();
            centerZ += log.getZ();
        }
        centerX /= logs.size();
        centerZ /= logs.size();
        
        final double trunkCenterX = centerX;
        final double trunkCenterZ = centerZ;
        final int leafRadius = maxLeafRadius;

        // Now collect natural leaves connected to the logs
        // BUT limited to within leafRadius horizontally from trunk center
        Set<Block> visited = new HashSet<>(logs);
        Deque<Block> leafQueue = new ArrayDeque<>();
        
        // Start from blocks adjacent to logs
        for (Block log : logs) {
            for (Block n : BlockUtil.neighbors27(log)) {
                if (isNaturalLeaf(n) && !visited.contains(n)) {
                    // Check horizontal distance from trunk center
                    if (isWithinLeafRadius(n, trunkCenterX, trunkCenterZ, leafRadius)) {
                        leafQueue.add(n);
                        visited.add(n);
                        leaves.add(n);
                    }
                }
            }
        }
        
        // BFS through connected natural leaves (with horizontal radius limit)
        while (!leafQueue.isEmpty() && (logs.size() + leaves.size()) < limit) {
            Block b = leafQueue.poll();
            for (Block n : BlockUtil.neighbors27(b)) {
                if ((logs.size() + leaves.size()) >= limit) break;
                if (!visited.contains(n) && isNaturalLeaf(n)) {
                    // Check horizontal distance from trunk center
                    if (isWithinLeafRadius(n, trunkCenterX, trunkCenterZ, leafRadius)) {
                        visited.add(n);
                        leaves.add(n);
                        leafQueue.add(n);
                    }
                }
            }
        }

        // Add logs first, then leaves (logs break first)
        result.addAll(logs);
        result.addAll(leaves);
        
        return result;
    }
    
    /**
     * Get the appropriate leaf radius for a tree type.
     * Smaller trees get smaller radius, larger trees (jungle, dark oak) get more.
     */
    private int getLeafRadiusForTreeType(LeafDropUtil.TreeType type) {
        return switch (type) {
            // Small/normal trees: 2-3 block leaf spread
            case OAK, BIRCH, SPRUCE, CHERRY, PALE_OAK, AZALEA -> 4;
            
            // Medium trees: slightly larger canopy
            case ACACIA -> 5;
            
            // Large trees: 2x2 trunks, bigger canopies
            case DARK_OAK -> 5;
            
            // Jungle trees can be massive (2x2 trunk, huge canopy)
            case JUNGLE -> 7;
            
            // Mangrove has wide spreading roots and canopy
            case MANGROVE -> 6;
            
            // Unknown - use conservative value
            case UNKNOWN -> 4;
        };
    }
    
    /**
     * Check if a block is within the allowed horizontal radius from trunk center.
     * Y is unlimited to handle tall trees.
     */
    private boolean isWithinLeafRadius(Block block, double centerX, double centerZ, int maxRadius) {
        double dx = block.getX() + 0.5 - centerX;
        double dz = block.getZ() + 0.5 - centerZ;
        double distanceSquared = dx * dx + dz * dz;
        return distanceSquared <= maxRadius * maxRadius;
    }

    /**
     * Find the lowest contiguous log block (stump).
     */
    private Block findStump(Block start) {
        Block cur = start;
        while (Materials.isLog(cur.getType(), true)) {
            Block below = cur.getRelative(0, -1, 0);
            if (Materials.isLog(below.getType(), true)) {
                cur = below;
            } else {
                break;
            }
        }
        return cur;
    }

    /**
     * Check if there are natural (non-persistent) leaves nearby.
     */
    private boolean hasNaturalLeavesNearby(Block origin, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block b = origin.getRelative(dx, dy, dz);
                    if (isNaturalLeaf(b)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check if a block is a natural (not player-placed) leaf.
     */
    private boolean isNaturalLeaf(Block block) {
        if (!block.getType().name().endsWith("_LEAVES")) {
            return false;
        }
        
        if (block.getBlockData() instanceof Leaves leaves) {
            // persistent=false means it was naturally generated
            // persistent=true means player-placed
            return !leaves.isPersistent();
        }
        
        return false;
    }

    /**
     * Get the sapling type for a log.
     */
    public static Material saplingForLog(Material log) {
        return LeafDropUtil.getSaplingForLog(log);
    }

    /**
     * Determine tree type from log for drop calculations.
     */
    public static LeafDropUtil.TreeType getTreeType(Material log) {
        return LeafDropUtil.getTreeTypeFromLog(log);
    }
}
