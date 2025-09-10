package net.ppekkungz.essentialUtils.features.tree;

import net.ppekkungz.essentialUtils.config.PluginConfig;
import net.ppekkungz.essentialUtils.features.Feature;
import net.ppekkungz.essentialUtils.util.BlockUtil;
import net.ppekkungz.essentialUtils.util.Materials;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class TreeAssistFeature implements Feature {
    private final PluginConfig cfg;
    private static final int LEAF_CHECK_RADIUS = 3;

    public TreeAssistFeature(PluginConfig cfg) { this.cfg = cfg; }

    @Override public String name() { return "Tree-Assist"; }

    @Override public boolean canTrigger(Player p, Block origin) {
        return Materials.isLog(origin.getType(), cfg.treeIncludeStripped());
    }

    @Override public Set<Block> collectTargets(Player p, Block origin) {
        // Root isolation: find stump (lowest connected log), then BFS upwards
        Block stump = findStump(origin);
        if (cfg.treeRequireLeaves() && !hasLeavesNearby(origin, LEAF_CHECK_RADIUS)) {
            return Collections.emptySet(); // avoid adjacent bare pillars/structures
        }

        int limit = cfg.treeMaxLogs();
        Set<Block> result = new LinkedHashSet<>();
        Deque<Block> dq = new ArrayDeque<>();
        dq.add(stump);
        result.add(stump);

        while (!dq.isEmpty() && result.size() < limit) {
            Block b = dq.poll();
            for (Block n : BlockUtil.neighbors27(b)) {
                if (result.size() >= limit) break;
                if (!result.contains(n) && Materials.isLog(n.getType(), cfg.treeIncludeStripped())) {
                    result.add(n); dq.add(n);
                }
            }
        }
        // We’ll replant later at stump (handled in ActivationListener using PLANT work items)
        return result;
    }

    /** Find the lowest contiguous log block (root) to avoid cutting adjacent trees. */
    private Block findStump(Block start) {
        Block cur = start;
        while (Materials.isLog(cur.getType(), cfg.treeIncludeStripped())) {
            Block below = cur.getRelative(0, -1, 0);
            if (Materials.isLog(below.getType(), cfg.treeIncludeStripped())) {
                cur = below;
            } else {
                break;
            }
        }
        return cur;
    }

    private boolean hasLeavesNearby(Block origin, int r) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    Block b = origin.getRelative(dx, dy, dz);
                    if (b.getType().name().endsWith("_LEAVES")) return true;
                }
            }
        }
        return false;
    }

    /** Map log type -> sapling type; default to OAK_SAPLING if unknown. */
    public static Material saplingForLog(Material log) {
        String n = log.name().replace("STRIPPED_", "");
        if (n.endsWith("_LOG")) n = n.substring(0, n.length() - 4);
        return switch (n) {
            case "OAK", "DARK_OAK" -> Material.OAK_SAPLING;
            case "BIRCH" -> Material.BIRCH_SAPLING;
            case "SPRUCE" -> Material.SPRUCE_SAPLING;
            case "JUNGLE" -> Material.JUNGLE_SAPLING;
            case "ACACIA" -> Material.ACACIA_SAPLING;
            case "MANGROVE" -> Material.MANGROVE_PROPAGULE;
            case "CHERRY" -> Material.CHERRY_SAPLING;
            case "PALE_OAK" -> Material.PALE_OAK_SAPLING;
            default -> Material.OAK_SAPLING;
        };
    }
}
