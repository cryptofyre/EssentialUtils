package net.ppekkungz.essentialUtils.util;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class Protection {
    private Protection(){}

    // TODO: Wire real checks (WorldGuard/GriefDefender/Region permission, etc.)
    public static boolean canModify(Player p, Block b) {
        // Always true for now; replace with plugin hooks if safety.respectProtections=true
        return true;
    }
}
