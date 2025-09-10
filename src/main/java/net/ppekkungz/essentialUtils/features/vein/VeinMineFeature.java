package net.ppekkungz.essentialUtils.features.vein;

import net.ppekkungz.essentialUtils.config.PluginConfig;
import net.ppekkungz.essentialUtils.features.Feature;
import net.ppekkungz.essentialUtils.util.BlockUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class VeinMineFeature implements Feature {
    private final PluginConfig cfg;
    private final Set<Material> whitelist;
    private final Set<Material> blacklist;

    public VeinMineFeature(PluginConfig cfg, Set<Material> white, Set<Material> black) {
        this.cfg = cfg; this.whitelist = white; this.blacklist = black;
    }

    @Override public String name() { return "Vein-Mine"; }

    @Override public boolean canTrigger(Player p, Block origin) {
        Material m = origin.getType();
        return whitelist.contains(m) && !blacklist.contains(m);
    }

    @Override public Set<Block> collectTargets(Player p, Block origin) {
        int limit = cfg.veinMaxOres();
        Material ore = origin.getType();
        Set<Block> result = new LinkedHashSet<>();
        Deque<Block> dq = new ArrayDeque<>();
        dq.add(origin); result.add(origin);

        while (!dq.isEmpty() && result.size() < limit) {
            Block b = dq.poll();
            for (Block n : BlockUtil.neighbors6(b)) {
                if (result.size() >= limit) break;
                if (!result.contains(n) && n.getType() == ore) {
                    result.add(n); dq.add(n);
                }
            }
        }
        return result;
    }
}
