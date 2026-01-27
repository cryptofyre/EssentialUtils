package org.cryptofyre.essentialUtils.util;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Ores {
    private Ores(){}

    public static Set<Material> expandWhitelist(List<String> patterns, boolean includeVariants) {
        Set<Material> base = Materials.fromListPatterns(patterns);
        if (!includeVariants) return base;

        Set<Material> out = new HashSet<>(base);
        for (Material m : base) {
            String name = m.name();
            // handle *_ORE → DEEPSLATE_*_ORE
            if (name.endsWith("_ORE")) {
                String core = name.substring(0, name.length() - "_ORE".length());
                tryAdd(out, "DEEPSLATE_" + core + "_ORE");
                // nether variants mostly for GOLD and QUARTZ only (Minecraft has NETHER_GOLD_ORE)
                if (core.equals("GOLD")) tryAdd(out, "NETHER_GOLD_ORE");
                if (core.equals("QUARTZ")) tryAdd(out, "NETHER_QUARTZ_ORE");
            }
            // ancient debris is standalone—already handled if in base
        }
        // Always keep ANCIENT_DEBRIS if user whitelisted variants by wildcard
        tryAdd(out, "ANCIENT_DEBRIS");
        return out;
    }

    private static void tryAdd(Set<Material> set, String matName) {
        try { set.add(Material.valueOf(matName)); } catch (IllegalArgumentException ignored) {}
    }
}
