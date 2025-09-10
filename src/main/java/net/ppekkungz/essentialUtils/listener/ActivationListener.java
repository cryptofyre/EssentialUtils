package net.ppekkungz.essentialUtils.listener;

import net.ppekkungz.essentialUtils.EssentialUtils;
import net.ppekkungz.essentialUtils.config.PluginConfig;
import net.ppekkungz.essentialUtils.features.Feature;
import net.ppekkungz.essentialUtils.features.farm.AutoFarmFeature;
import net.ppekkungz.essentialUtils.features.tree.TreeAssistFeature;
import net.ppekkungz.essentialUtils.features.vein.VeinMineFeature;
import net.ppekkungz.essentialUtils.indicator.BossBarIndicator;
import net.ppekkungz.essentialUtils.state.PlayerState;
import net.ppekkungz.essentialUtils.state.StateManager;
import net.ppekkungz.essentialUtils.util.HarvestUtil;
import net.ppekkungz.essentialUtils.util.Materials;
import net.ppekkungz.essentialUtils.util.Ores;
import net.ppekkungz.essentialUtils.work.WorkItem;
import net.ppekkungz.essentialUtils.work.WorkService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Set;

public class ActivationListener implements Listener {
    private final EssentialUtils plugin;
    private final PluginConfig cfg;
    private final StateManager states;
    private final WorkService work;

    private final Feature tree;
    private final Feature vein;
    private final Feature farm;

    private final Set<Material> oresWhitelist;
    private final Set<Material> oresBlacklist;
    private final Set<Material> farmAllow;

    private final BossBarIndicator boss = new BossBarIndicator();

    public ActivationListener(EssentialUtils plugin, PluginConfig cfg, StateManager states, WorkService work) {
        this.plugin = plugin; this.cfg = cfg; this.states = states; this.work = work;

        this.oresWhitelist = Ores.expandWhitelist(cfg.veinWhitelist(), cfg.veinIncludeVariants());
        this.oresBlacklist = Materials.fromListPatterns(cfg.veinBlacklist());
        this.farmAllow    = Materials.fromListPatterns(cfg.farmWhitelist());

        this.tree = new TreeAssistFeature(cfg);
        this.vein = new VeinMineFeature(cfg, oresWhitelist, oresBlacklist);
        this.farm = new AutoFarmFeature(cfg, farmAllow);
    }

    private boolean isAxe(ItemStack it) { return it != null && it.getType().name().endsWith("_AXE"); }
    private boolean isPick(ItemStack it) { return it != null && it.getType().name().endsWith("_PICKAXE"); }
    private boolean isHoe(ItemStack it) { return it != null && it.getType().name().endsWith("_HOE"); }
    private boolean isSupportedTool(ItemStack it) { return isAxe(it) || isPick(it) || isHoe(it); }

    private boolean requireToolWhileHold() {
        return plugin.getConfig().getBoolean("activation.requireToolWhileHold", true);
    }
    private int holdSneakMillis() {
        int sec = plugin.getConfig().getInt("activation.holdSneakSeconds", 5);
        return Math.max(1, sec) * 1000;
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        if (e.isSneaking()) {
            if (requireToolWhileHold() && !isSupportedTool(p.getInventory().getItemInMainHand())) return;

            states.startHold(p);
            states.set(p, PlayerState.ARMED);

            final int totalMs = holdSneakMillis();
            // Folia-safe per player loop
            p.getScheduler().runAtFixedRate(plugin, task -> {
                if (!p.isSneaking() ||
                        (requireToolWhileHold() && !isSupportedTool(p.getInventory().getItemInMainHand()))) {
                    boss.hide(p);
                    task.cancel();
                    return;
                }
                long held = states.heldMillis(p);
                double prog = Math.min(1.0, held / (double) totalMs);
                boss.showProgress(p, "§a[EssentialUtils] Hold Shift to arm…", prog);

                if (held >= totalMs) {
                    boss.hide(p);
                    p.sendActionBar("§a[EssentialUtils] Armed. Use your tool on a block.");
                    task.cancel();
                }
            }, null, 1L, 1L);
        } else {
            boss.hide(p);
            states.stopHold(p);
            states.set(p, PlayerState.IDLE);
            work.stopLoop(p);
            p.sendActionBar("§7[EssentialUtils] §cDeactivated (stopped sneaking)");
        }
    }

    // Reset on slot change
    @EventHandler
    public void onSlot(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        boss.hide(p);
        states.reset(p);
        work.stopLoop(p);
        p.sendActionBar("§7[EssentialUtils] §cDeactivated (item changed)");
    }

    // Trigger after armed: click block WHILE sneaking with a valid tool
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block b = e.getClickedBlock(); if (b == null) return;
        Player p = e.getPlayer();
        if (!p.isSneaking()) return;

        // must be armed already (finished hold duration)
        if (states.heldMillis(p) < holdSneakMillis()) return;

        ItemStack hand = p.getInventory().getItemInMainHand();
        if (!isSupportedTool(hand)) return;

        Feature f = null;
        if (cfg.treeEnabled() && isAxe(hand) && tree.canTrigger(p, b)) f = tree;
        else if (cfg.veinEnabled() && isPick(hand) && vein.canTrigger(p, b)) f = vein;
        else if (cfg.farmEnabled() && isHoe(hand) && farm.canTrigger(p, b)) f = farm;

        if (f == null) return;

        var targets = f.collectTargets(p, b);
        if (targets.isEmpty()) { p.sendActionBar("§7[EssentialUtils] No targets."); return; }

        // Per-block delay “staircase”
        int idx = 0;
        for (Block tb : targets) {
            int cd = 0;
            WorkItem.FeatureTag tag = WorkItem.FeatureTag.OTHER;

            if (f == tree) {
                tag = WorkItem.FeatureTag.TREE;
                int per = plugin.getConfig().getInt("features.treeAssist.breakDelayTicks", 2);
                cd = Math.max(0, per) * idx;
            } else if (f == vein) {
                tag = WorkItem.FeatureTag.VEIN;
                int tier = HarvestUtil.pickaxeTier(p.getInventory().getItemInMainHand());
                targets.removeIf(block -> tier < HarvestUtil.requiredTierForOre(block.getType()));
                if (targets.isEmpty()) {
                    p.sendActionBar("§c[EssentialUtils] Your pickaxe tier is too low for this vein.");
                    return;
                }
                int base = plugin.getConfig().getInt("features.veinMine.breakDelayBaseTicks", 1);
                int extra = plugin.getConfig().getInt("features.veinMine.breakDelayExtraPer16", 1) * (targets.size() / 16);
                cd = Math.max(0, base + extra) * idx;
            } else if (f == farm) {
                tag = WorkItem.FeatureTag.FARM;
                cd = 0;
            }

            work.queue(p).add(new WorkItem(p, tb, WorkItem.Action.BREAK, null, 0, cd, tag));
            idx++;
        }

        // Tree: optional replant (respect config); delay first so drops can be picked
        if (f == tree && cfg.treeReplantEnabled()) {
            Material sapling = TreeAssistFeature.saplingForLog(b.getType());
            Block stump = findStumpFromSet(targets);
            if (stump != null) {
                Block plantPos = stump.getRelative(0, 1, 0);
                p.getScheduler().runDelayed(plugin, task -> {
                    var item = new WorkItem(
                            p, plantPos,
                            WorkItem.Action.PLANT, sapling,
                            Math.max(0, cfg.treeReplantMaxRetries()),
                            0, WorkItem.FeatureTag.TREE
                    );
                    work.queue(p).add(item);
                }, null, 20L);
            }
        }

        states.set(p, PlayerState.ACTIVE);
        work.ensureLoop(p);
        p.sendActionBar("§a[EssentialUtils] §f" + f.name() + " queued §e" + targets.size() + " §7blocks.");
        e.setCancelled(true);
    }

    private Block findStumpFromSet(java.util.Set<Block> logs) {
        Block best = null;
        int bestY = Integer.MAX_VALUE;
        for (Block b : logs) {
            if (b.getY() < bestY) { bestY = b.getY(); best = b; }
        }
        return best;
    }

    @EventHandler public void onQuit(PlayerQuitEvent e) {
        boss.hide(e.getPlayer());
        work.stopLoop(e.getPlayer());
        states.reset(e.getPlayer());
    }
}
