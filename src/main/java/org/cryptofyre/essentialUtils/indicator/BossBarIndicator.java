package org.cryptofyre.essentialUtils.indicator;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BossBarIndicator {
    private final Map<UUID, BossBar> bars = new ConcurrentHashMap<>();

    public void showProgress(Player p, String title, double progress) {
        BossBar bar = bars.computeIfAbsent(p.getUniqueId(), id ->
                Bukkit.createBossBar(title, BarColor.GREEN, BarStyle.SEGMENTED_10)
        );
        bar.setVisible(true);
        bar.setTitle(title);
        bar.setProgress(Math.max(0, Math.min(1, progress)));
        if (!bar.getPlayers().contains(p)) bar.addPlayer(p);
    }

    public void hide(Player p) {
        BossBar bar = bars.remove(p.getUniqueId());
        if (bar != null) {
            bar.removeAll();
            bar.setVisible(false);
        }
    }
}
