package net.ppekkungz.essentialUtils.util;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Materials {
    private Materials(){}

    public static boolean isLog(Material m, boolean includeStripped) {
        String n = m.name();
        if (n.endsWith("_LOG")) return true;
        if (includeStripped && n.startsWith("STRIPPED_") && n.endsWith("_LOG")) return true;
        return false;
    }

    public static Set<Material> fromListPatterns(List<String> patterns) {
        Set<Material> out = new HashSet<>();
        for (String s : patterns) {
            if (s.endsWith("*")) {
                String prefix = s.substring(0, s.length()-1);
                for (Material m : Material.values())
                    if (m.name().startsWith(prefix)) out.add(m);
            } else if (s.startsWith("*_")) {
                String suffix = s.substring(1);
                for (Material m : Material.values())
                    if (m.name().endsWith(suffix)) out.add(m);
            } else {
                try { out.add(Material.valueOf(s)); } catch (Exception ignored) {}
            }
        }
        return out;
    }
}
