package org.cryptofyre.essentialUtils.indicator;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

/**
 * Simple actionbar indicator using Adventure API.
 */
public class ActionBarIndicator implements IndicatorService {
    // Use ampersand for color codes (&a, &b, etc.)
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
        .character('&')
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build();
    
    @Override 
    public void show(Player p, String text) {
        p.sendActionBar(LEGACY.deserialize(text));
    }
}
