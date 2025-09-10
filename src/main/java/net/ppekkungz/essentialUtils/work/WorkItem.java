package net.ppekkungz.essentialUtils.work;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class WorkItem {
    public enum Action { BREAK, PLANT }
    public enum FeatureTag { TREE, VEIN, FARM, OTHER }

    public final Player player;
    public final Block block;
    public final Action action;
    public final FeatureTag tag;

    // For PLANT
    public final Material plantType; // e.g. OAK_SAPLING
    public int retries;              // for replant retry
    public int cooldownTicks;        // delay before trying again

    public WorkItem(Player player, Block block) {
        this(player, block, Action.BREAK, null, 0, 0, FeatureTag.OTHER);
    }

    public WorkItem(Player player, Block block, Action action, Material plantType, int retries) {
        this(player, block, action, plantType, retries, 0, FeatureTag.OTHER);
    }

    public WorkItem(Player player, Block block, Action action, Material plantType, int retries, int cooldownTicks, FeatureTag tag) {
        this.player = player;
        this.block = block;
        this.action = action;
        this.plantType = plantType;
        this.retries = retries;
        this.cooldownTicks = cooldownTicks;
        this.tag = tag;
    }
}
