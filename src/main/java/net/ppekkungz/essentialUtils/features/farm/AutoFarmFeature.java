package net.ppekkungz.essentialUtils.features.farm;

import net.ppekkungz.essentialUtils.config.PluginConfig;
import net.ppekkungz.essentialUtils.features.Feature;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class AutoFarmFeature implements Feature {
    private final PluginConfig cfg;
    private final Set<Material> allowed;

    public AutoFarmFeature(PluginConfig cfg, Set<Material> allowed) {
        this.cfg = cfg;
        this.allowed = allowed;
    }

    @Override
    public String name() {
        return "Auto-Farm";
    }

    @Override
    public boolean canTrigger(Player p, Block origin) {
        return allowed.contains(origin.getType());
    }

    @Override
    public Set<Block> collectTargets(Player p, Block origin) {
        int r = cfg.farmRadius();
        Set<Block> out = new HashSet<>();

        // Scan a square area around the origin block
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                Block b = origin.getRelative(dx, 0, dz);
                if (allowed.contains(b.getType()) && isMature(b)) {
                    out.add(b);
                }
            }
        }
        return out;
    }

    private boolean isMature(Block b) {
        if (!(b.getBlockData() instanceof Ageable age)) return false;
        return age.getAge() >= age.getMaximumAge();
    }
}
