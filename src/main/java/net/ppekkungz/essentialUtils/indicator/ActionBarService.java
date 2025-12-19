package net.ppekkungz.essentialUtils.indicator;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.ppekkungz.essentialUtils.EssentialUtils;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for displaying actionbar messages with automatic clearing.
 * Folia-compatible using per-player scheduling.
 * Uses Adventure API for modern text handling.
 */
public class ActionBarService {
    private final EssentialUtils plugin;
    private final Map<UUID, ScheduledTask> clearTasks = new ConcurrentHashMap<>();
    private final Map<UUID, String> persistentMessages = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> persistentTasks = new ConcurrentHashMap<>();
    
    // Use legacyAmpersand to parse & color codes, and legacySection to also handle § codes
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
        .character('&')
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build();
    
    public ActionBarService(EssentialUtils plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Show a message that auto-clears after specified ticks.
     */
    public void showTimed(Player player, String message, int durationTicks) {
        if (player == null || !player.isOnline()) return;
        
        // Cancel any existing clear task
        cancelClearTask(player);
        
        // Show the message
        sendActionBar(player, message);
        
        // Schedule clear task
        ScheduledTask task = player.getScheduler().runDelayed(plugin, t -> {
            // Only clear if no persistent message is set
            if (!persistentMessages.containsKey(player.getUniqueId())) {
                clearActionBar(player);
            } else {
                // Re-show persistent message
                sendActionBar(player, persistentMessages.get(player.getUniqueId()));
            }
            clearTasks.remove(player.getUniqueId());
        }, null, durationTicks);
        
        if (task != null) {
            clearTasks.put(player.getUniqueId(), task);
        }
    }
    
    /**
     * Show a message that persists until explicitly cleared.
     * Refreshes every 20 ticks to prevent natural fadeout.
     */
    public void showPersistent(Player player, String message) {
        if (player == null || !player.isOnline()) return;
        
        // Cancel existing persistent task if any
        cancelPersistentTask(player);
        
        // Store the message
        persistentMessages.put(player.getUniqueId(), message);
        
        // Show immediately
        sendActionBar(player, message);
        
        // Schedule refresh every 20 ticks (1 second) to keep it visible
        ScheduledTask task = player.getScheduler().runAtFixedRate(plugin, t -> {
            String msg = persistentMessages.get(player.getUniqueId());
            if (msg != null && player.isOnline()) {
                sendActionBar(player, msg);
            } else {
                t.cancel();
                persistentTasks.remove(player.getUniqueId());
            }
        }, null, 20L, 20L);
        
        if (task != null) {
            persistentTasks.put(player.getUniqueId(), task);
        }
    }
    
    /**
     * Update a persistent message without restarting the refresh loop.
     */
    public void updatePersistent(Player player, String message) {
        if (player == null || !player.isOnline()) return;
        
        if (persistentMessages.containsKey(player.getUniqueId())) {
            persistentMessages.put(player.getUniqueId(), message);
            sendActionBar(player, message);
        } else {
            showPersistent(player, message);
        }
    }
    
    /**
     * Clear any persistent message.
     */
    public void clearPersistent(Player player) {
        if (player == null) return;
        
        persistentMessages.remove(player.getUniqueId());
        cancelPersistentTask(player);
        
        // Clear the actionbar if online
        if (player.isOnline()) {
            clearActionBar(player);
        }
    }
    
    /**
     * Check if player has a persistent message.
     */
    public boolean hasPersistent(Player player) {
        return player != null && persistentMessages.containsKey(player.getUniqueId());
    }
    
    /**
     * Show a one-shot message (no auto-clear scheduling, just send).
     */
    public void showOnce(Player player, String message) {
        if (player != null && player.isOnline()) {
            sendActionBar(player, message);
        }
    }
    
    /**
     * Clean up when player disconnects.
     */
    public void cleanup(Player player) {
        if (player == null) return;
        
        UUID id = player.getUniqueId();
        persistentMessages.remove(id);
        cancelClearTask(player);
        cancelPersistentTask(player);
    }
    
    /**
     * Shutdown all tasks.
     */
    public void shutdown() {
        clearTasks.values().forEach(ScheduledTask::cancel);
        clearTasks.clear();
        persistentTasks.values().forEach(ScheduledTask::cancel);
        persistentTasks.clear();
        persistentMessages.clear();
    }
    
    private void cancelClearTask(Player player) {
        ScheduledTask existing = clearTasks.remove(player.getUniqueId());
        if (existing != null) {
            existing.cancel();
        }
    }
    
    private void cancelPersistentTask(Player player) {
        ScheduledTask existing = persistentTasks.remove(player.getUniqueId());
        if (existing != null) {
            existing.cancel();
        }
    }
    
    /**
     * Send actionbar using Adventure API.
     */
    private void sendActionBar(Player player, String message) {
        Component component = LEGACY.deserialize(message);
        player.sendActionBar(component);
    }
    
    /**
     * Clear the actionbar.
     */
    private void clearActionBar(Player player) {
        player.sendActionBar(Component.empty());
    }
}
