package net.ppekkungz.essentialUtils.indicator;

import org.bukkit.entity.Player;

public class ActionBarIndicator implements IndicatorService {
    @Override public void show(Player p, String text) {
        p.sendActionBar(text);
    }
}
