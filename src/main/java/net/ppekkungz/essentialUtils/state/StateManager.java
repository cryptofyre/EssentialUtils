package net.ppekkungz.essentialUtils.state;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-player activation state and the "hold-to-arm" timer.
 * Thread-safe for Folia (maps are concurrent; all mutations happen on the player's region thread).
 */
public class StateManager {

    private final Map<UUID, PlayerState> states = new ConcurrentHashMap<>();
    // When the player started holding sneak (ms since epoch). Missing entry => not holding.
    private final Map<UUID, Long> sneakStart = new ConcurrentHashMap<>();

    /** Get current state (defaults to IDLE). */
    public PlayerState get(Player p) {
        return states.getOrDefault(p.getUniqueId(), PlayerState.IDLE);
    }

    /** Set current state. */
    public void set(Player p, PlayerState s) {
        states.put(p.getUniqueId(), s);
    }

    /** Mark the moment the player began holding Shift (sneak). */
    public void startHold(Player p) {
        sneakStart.put(p.getUniqueId(), System.currentTimeMillis());
    }

    /** Stop the hold timer (called when player releases Shift or cancels). */
    public void stopHold(Player p) {
        sneakStart.remove(p.getUniqueId());
    }

    /**
     * Milliseconds the player has continuously held Shift.
     * Returns 0 if not currently holding or never started.
     */
    public long heldMillis(Player p) {
        Long t = sneakStart.get(p.getUniqueId());
        return (t == null) ? 0L : (System.currentTimeMillis() - t);
    }

    /** Fully clear a player's state and timers (on quit, item change, etc.). */
    public void reset(Player p) {
        UUID id = p.getUniqueId();
        states.remove(id);
        sneakStart.remove(id);
    }
}
