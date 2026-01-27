package org.cryptofyre.essentialUtils.util;

import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class BlockUtil {
    private BlockUtil(){}

    public static List<Block> neighbors6(Block b) {
        List<Block> n = new ArrayList<>(6);
        n.add(b.getRelative( 1, 0, 0));
        n.add(b.getRelative(-1, 0, 0));
        n.add(b.getRelative(0,  1, 0));
        n.add(b.getRelative(0, -1, 0));
        n.add(b.getRelative(0, 0,  1));
        n.add(b.getRelative(0, 0, -1));
        return n;
    }

    /** All neighbors in a 3×3×3 cube around the block (including diagonals, excluding self). */
    public static List<Block> neighbors27(Block b) {
        List<Block> res = new ArrayList<>(26);
        for (int dx=-1; dx<=1; dx++)
            for (int dy=-1; dy<=1; dy++)
                for (int dz=-1; dz<=1; dz++) {
                    if (dx==0 && dy==0 && dz==0) continue;
                    res.add(b.getRelative(dx, dy, dz));
                }
        return res;
    }
}
