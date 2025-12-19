package net.ppekkungz.essentialUtils.work;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks the results of a VeinMiner session for a player.
 * Used to generate actionbar summaries after mining completes.
 */
public class VeinMineResult {
    private final Location originLocation;
    private final Material oreType;
    private int blocksMined = 0;
    private int totalDrops = 0;
    private int totalXP = 0;
    private int fortuneLevel = 0;
    private boolean usedSilkTouch = false;
    private final Map<Material, Integer> dropCounts = new HashMap<>();
    
    public VeinMineResult(Location origin, Material oreType) {
        this.originLocation = origin;
        this.oreType = oreType;
    }
    
    /**
     * Record a mined block.
     */
    public void addMinedBlock() {
        blocksMined++;
    }
    
    /**
     * Record drops from an ore.
     */
    public void addDrops(Material dropType, int count) {
        totalDrops += count;
        dropCounts.merge(dropType, count, (a, b) -> a + b);
    }
    
    /**
     * Record XP from an ore.
     */
    public void addXP(int xp) {
        totalXP += xp;
    }
    
    /**
     * Set the fortune level used.
     */
    public void setFortuneLevel(int level) {
        this.fortuneLevel = level;
    }
    
    /**
     * Mark that silk touch was used.
     */
    public void setSilkTouch(boolean silkTouch) {
        this.usedSilkTouch = silkTouch;
    }
    
    // Getters
    
    public Location getOriginLocation() {
        return originLocation;
    }
    
    public Material getOreType() {
        return oreType;
    }
    
    public int getBlocksMined() {
        return blocksMined;
    }
    
    public int getTotalDrops() {
        return totalDrops;
    }
    
    public int getTotalXP() {
        return totalXP;
    }
    
    public int getFortuneLevel() {
        return fortuneLevel;
    }
    
    public boolean usedSilkTouch() {
        return usedSilkTouch;
    }
    
    public Map<Material, Integer> getDropCounts() {
        return new HashMap<>(dropCounts);
    }
    
    /**
     * Get the primary drop material (the one with the most drops).
     */
    public Material getPrimaryDrop() {
        return dropCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(oreType);
    }
    
    /**
     * Get multiplier string for display (e.g., "x2 Fortune" or "Silk Touch").
     */
    public String getMultiplierString() {
        if (usedSilkTouch) {
            return "Silk Touch";
        }
        if (fortuneLevel > 0) {
            return "x" + (fortuneLevel + 1) + " Fortune";
        }
        return "x1";
    }
    
    /**
     * Check if this result has any meaningful data.
     */
    public boolean hasData() {
        return blocksMined > 0;
    }
    
    /**
     * Reset for reuse.
     */
    public void reset() {
        blocksMined = 0;
        totalDrops = 0;
        totalXP = 0;
        fortuneLevel = 0;
        usedSilkTouch = false;
        dropCounts.clear();
    }
}

