package net.ppekkungz.essentialUtils.work;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Represents a unit of work to be processed by the WorkService.
 * Supports various actions: breaking blocks, planting saplings, replanting crops.
 */
public class WorkItem {
    
    public enum Action { 
        /** Break a block and drop items */
        BREAK, 
        /** Plant a sapling/propagule */
        PLANT,
        /** Replant a crop (seeds) */
        REPLANT
    }
    
    public enum FeatureTag { 
        TREE, 
        VEIN, 
        FARM, 
        OTHER 
    }

    public final Player player;
    public final Block block;
    public final Action action;
    public final FeatureTag tag;

    // For PLANT/REPLANT actions
    public final Material plantType;
    public int retries;
    public int cooldownTicks;
    
    // For tracking if this is a leaf (for drop calculations)
    public final boolean isLeaf;

    public WorkItem(Player player, Block block) {
        this(player, block, Action.BREAK, null, 0, 0, FeatureTag.OTHER, false);
    }

    public WorkItem(Player player, Block block, Action action, Material plantType, int retries) {
        this(player, block, action, plantType, retries, 0, FeatureTag.OTHER, false);
    }

    public WorkItem(Player player, Block block, Action action, Material plantType, int retries, int cooldownTicks, FeatureTag tag) {
        this(player, block, action, plantType, retries, cooldownTicks, tag, false);
    }
    
    public WorkItem(Player player, Block block, Action action, Material plantType, int retries, int cooldownTicks, FeatureTag tag, boolean isLeaf) {
        this.player = player;
        this.block = block;
        this.action = action;
        this.plantType = plantType;
        this.retries = retries;
        this.cooldownTicks = cooldownTicks;
        this.tag = tag;
        this.isLeaf = isLeaf;
    }
    
    /**
     * Create a break work item for a log.
     */
    public static WorkItem breakLog(Player player, Block block, int cooldown) {
        return new WorkItem(player, block, Action.BREAK, null, 0, cooldown, FeatureTag.TREE, false);
    }
    
    /**
     * Create a break work item for a leaf.
     */
    public static WorkItem breakLeaf(Player player, Block block, int cooldown) {
        return new WorkItem(player, block, Action.BREAK, null, 0, cooldown, FeatureTag.TREE, true);
    }
    
    /**
     * Create a break work item for an ore.
     */
    public static WorkItem breakOre(Player player, Block block, int cooldown) {
        return new WorkItem(player, block, Action.BREAK, null, 0, cooldown, FeatureTag.VEIN, false);
    }
    
    /**
     * Create a break work item for a crop.
     */
    public static WorkItem breakCrop(Player player, Block block) {
        return new WorkItem(player, block, Action.BREAK, null, 0, 0, FeatureTag.FARM, false);
    }
    
    /**
     * Create a plant work item for a sapling.
     */
    public static WorkItem plantSapling(Player player, Block block, Material sapling, int retries) {
        return new WorkItem(player, block, Action.PLANT, sapling, retries, 5, FeatureTag.TREE, false);
    }
    
    /**
     * Create a replant work item for a crop.
     */
    public static WorkItem replantCrop(Player player, Block block, Material cropType) {
        return new WorkItem(player, block, Action.REPLANT, cropType, 3, 0, FeatureTag.FARM, false);
    }
}
