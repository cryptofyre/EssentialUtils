package org.cryptofyre.essentialUtils.state;

import org.cryptofyre.essentialUtils.util.LeafDropUtil;
import org.cryptofyre.essentialUtils.work.VeinMineResult;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-player state for EssentialUtils features.
 * Thread-safe for Folia (uses concurrent maps).
 */
public class StateManager {

    private final Map<UUID, PlayerState> states = new ConcurrentHashMap<>();
    
    // VeinMiner result tracking per player
    private final Map<UUID, VeinMineResult> veinMineResults = new ConcurrentHashMap<>();
    
    // TreeFeller result tracking per player
    private final Map<UUID, LeafDropUtil.TreeFellerResult> treeFellerResults = new ConcurrentHashMap<>();
    private final Map<UUID, Material> treeFellerLogTypes = new ConcurrentHashMap<>();
    private final Map<UUID, Location> treeFellerStumpLocations = new ConcurrentHashMap<>();

    // ==================== STATE MANAGEMENT ====================

    /**
     * Get current state (defaults to IDLE).
     */
    public PlayerState get(Player p) {
        return states.getOrDefault(p.getUniqueId(), PlayerState.IDLE);
    }

    /**
     * Set current state.
     */
    public void set(Player p, PlayerState s) {
        states.put(p.getUniqueId(), s);
    }

    /**
     * Check if player is actively processing.
     */
    public boolean isActive(Player p) {
        return get(p) == PlayerState.ACTIVE;
    }

    // ==================== VEINMINER RESULTS ====================

    /**
     * Start tracking a new VeinMiner session.
     */
    public VeinMineResult startVeinMine(Player p, Location origin, Material oreType) {
        VeinMineResult result = new VeinMineResult(origin, oreType);
        veinMineResults.put(p.getUniqueId(), result);
        return result;
    }

    /**
     * Get current VeinMiner result (or null if none).
     */
    public VeinMineResult getVeinMineResult(Player p) {
        return veinMineResults.get(p.getUniqueId());
    }

    /**
     * End VeinMiner session and return results.
     */
    public VeinMineResult endVeinMine(Player p) {
        return veinMineResults.remove(p.getUniqueId());
    }

    // ==================== TREEFELLER RESULTS ====================

    /**
     * Start tracking a new TreeFeller session.
     */
    public LeafDropUtil.TreeFellerResult startTreeFeller(Player p, Material logType, Location stumpLocation) {
        LeafDropUtil.TreeFellerResult result = new LeafDropUtil.TreeFellerResult();
        treeFellerResults.put(p.getUniqueId(), result);
        treeFellerLogTypes.put(p.getUniqueId(), logType);
        treeFellerStumpLocations.put(p.getUniqueId(), stumpLocation);
        return result;
    }

    /**
     * Get current TreeFeller result (or null if none).
     */
    public LeafDropUtil.TreeFellerResult getTreeFellerResult(Player p) {
        return treeFellerResults.get(p.getUniqueId());
    }

    /**
     * Get the log type for current TreeFeller session.
     */
    public Material getTreeFellerLogType(Player p) {
        return treeFellerLogTypes.get(p.getUniqueId());
    }

    /**
     * Get the stump location for current TreeFeller session.
     */
    public Location getTreeFellerStumpLocation(Player p) {
        return treeFellerStumpLocations.get(p.getUniqueId());
    }

    /**
     * End TreeFeller session and return results.
     */
    public LeafDropUtil.TreeFellerResult endTreeFeller(Player p) {
        treeFellerLogTypes.remove(p.getUniqueId());
        treeFellerStumpLocations.remove(p.getUniqueId());
        return treeFellerResults.remove(p.getUniqueId());
    }

    // ==================== CLEANUP ====================

    /**
     * Fully clear a player's state and all tracking data.
     */
    public void reset(Player p) {
        UUID id = p.getUniqueId();
        states.remove(id);
        veinMineResults.remove(id);
        treeFellerResults.remove(id);
        treeFellerLogTypes.remove(id);
        treeFellerStumpLocations.remove(id);
    }

    /**
     * Clear all data (used on plugin shutdown).
     */
    public void clear() {
        states.clear();
        veinMineResults.clear();
        treeFellerResults.clear();
        treeFellerLogTypes.clear();
        treeFellerStumpLocations.clear();
    }
}
