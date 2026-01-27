package org.cryptofyre.essentialUtils.features;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Set;

public interface Feature {
    String name();
    boolean canTrigger(Player p, Block origin);
    Set<Block> collectTargets(Player p, Block origin); // BFS/scan result
}
