package net.ppekkungz.essentialUtils.work;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.ppekkungz.essentialUtils.EssentialUtils;
import net.ppekkungz.essentialUtils.config.PluginConfig;
import net.ppekkungz.essentialUtils.indicator.ActionBarIndicator;
import net.ppekkungz.essentialUtils.indicator.IndicatorService;
import net.ppekkungz.essentialUtils.state.PlayerState;
import net.ppekkungz.essentialUtils.state.StateManager;
import net.ppekkungz.essentialUtils.util.HarvestUtil;
import net.ppekkungz.essentialUtils.util.Protection;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorkService {
    private final EssentialUtils plugin;
    private final PluginConfig cfg;
    private final StateManager states;
    private final IndicatorService indicator = new ActionBarIndicator();

    private final Map<UUID, WorkQueue> queues = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> loops = new ConcurrentHashMap<>();

    // durability throttle counters
    private final Map<UUID, Integer> treeBroken = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> veinBroken = new ConcurrentHashMap<>();

    // rate-limit for tier alerts
    private final Map<UUID, Long> lastTierWarn = new ConcurrentHashMap<>();

    public WorkService(EssentialUtils plugin, PluginConfig cfg, StateManager states) {
        this.plugin = plugin; this.cfg = cfg; this.states = states;
    }

    public WorkQueue queue(Player p) {
        return queues.computeIfAbsent(p.getUniqueId(), k -> new WorkQueue());
    }

    public void ensureLoop(Player p) {
        loops.computeIfAbsent(p.getUniqueId(), id ->
                p.getScheduler().runAtFixedRate(plugin, task -> tickPlayer(p), null, 1L, 1L)
        );
    }

    public void stopLoop(Player p) {
        ScheduledTask t = loops.remove(p.getUniqueId());
        if (t != null) t.cancel();
        queues.remove(p.getUniqueId());
        treeBroken.remove(p.getUniqueId());
        veinBroken.remove(p.getUniqueId());
        lastTierWarn.remove(p.getUniqueId());
    }

    public void shutdown() {
        loops.values().forEach(ScheduledTask::cancel);
        loops.clear();
        queues.clear();
        treeBroken.clear();
        veinBroken.clear();
        lastTierWarn.clear();
    }

    private int capFor(WorkItem.FeatureTag tag) {
        return switch (tag) {
            case TREE -> cfg.treeMaxPerTick();
            case VEIN -> cfg.veinMaxPerTick();
            case FARM -> cfg.farmMaxPerTick();
            default -> Integer.MAX_VALUE;
        };
    }

    private void tickPlayer(Player p) {
        if (!p.isOnline()) { stopLoop(p); return; }
        WorkQueue q = queue(p);
        if (q.isEmpty()) {
            if (states.get(p) == PlayerState.ACTIVE) {
                indicator.show(p, "§7[EssentialUtils] §fQueue empty. §cIdle");
                states.set(p, PlayerState.IDLE);
            }
            return;
        }

        final int perPlayerBudget = cfg.perPlayerBudget();
        EnumMap<WorkItem.FeatureTag, Integer> used = new EnumMap<>(WorkItem.FeatureTag.class);
        for (WorkItem.FeatureTag t : WorkItem.FeatureTag.values()) used.put(t, 0);

        int processed = 0;
        int guard = Math.max(64, q.size() * 2);

        for (int i = 0; i < guard && processed < perPlayerBudget; i++) {
            WorkItem wi = q.poll();
            if (wi == null) break;

            // cooldown
            if (wi.cooldownTicks > 0) {
                wi.cooldownTicks--;
                q.add(wi);
                continue;
            }

            // per-feature cap
            int usedSoFar = used.getOrDefault(wi.tag, 0);
            if (usedSoFar >= capFor(wi.tag)) {
                q.add(wi);
                continue;
            }

            Block b = wi.block;
            if (cfg.requireChunkLoaded() && !b.getChunk().isLoaded()) {
                wi.cooldownTicks = Math.max(1, cfg.treeReplantRetryCooldownTicks()); // reuse knob
                q.add(wi);
                continue;
            }
            if (!Protection.canModify(wi.player, b)) {
                continue; // drop silently if protected
            }

            switch (wi.action) {
                case BREAK -> {
                    if (!b.getType().isAir()) breakControlled(wi);
                }
                case PLANT -> {
                    if (cfg.treeReplantEnabled()) handlePlant(wi, q);
                }
            }

            used.put(wi.tag, usedSoFar + 1);
            processed++;
        }

        if (cfg.showProgress()) {
            indicator.show(p, "§a[EU] §fTick: §e" + processed + "§7/§b" + perPlayerBudget + " §7| Q: §b" + q.size());
        }
    }

    private void breakControlled(WorkItem wi) {
        Player p = wi.player;
        Block b = wi.block;

        // TREE/FARM: break with drops; custom durability per N
        if (wi.tag == WorkItem.FeatureTag.TREE || wi.tag == WorkItem.FeatureTag.FARM) {
            b.breakNaturally(p.getInventory().getItemInMainHand(), true);
            if (wi.tag == WorkItem.FeatureTag.TREE) damageToolEveryN(p, wi.tag);
            return;
        }

        // VEIN: enforce pickaxe tier; if under-tier -> alert & don't break
        if (wi.tag == WorkItem.FeatureTag.VEIN) {
            int tier = HarvestUtil.pickaxeTier(p.getInventory().getItemInMainHand());
            int need = HarvestUtil.requiredTierForOre(b.getType());

            if (tier >= need) {
                b.breakNaturally(p.getInventory().getItemInMainHand(), true);
                damageToolEveryN(p, wi.tag);
                return;
            }

            // under-tier: alert (rate-limited), do NOT break or damage
            if (plugin.getConfig().getBoolean("features.veinMine.alertOnInsufficientTier", true)) {
                long now = System.currentTimeMillis();
                long last = lastTierWarn.getOrDefault(p.getUniqueId(), 0L);
                if (now - last >= 1000L) {
                    p.sendActionBar("§c[EssentialUtils] Can't mine " + b.getType() +
                            " with your pickaxe. Requires §e" + HarvestUtil.tierName(need) + "§c.");
                    lastTierWarn.put(p.getUniqueId(), now);
                }
            }
        }
    }

    private void damageToolEveryN(Player p, WorkItem.FeatureTag tag) {
        int step = switch (tag) {
            case TREE -> plugin.getConfig().getInt("features.treeAssist.durabilityPerNBlocks", 8);
            case VEIN -> plugin.getConfig().getInt("features.veinMine.durabilityPerNBlocks", 10);
            default -> 0;
        };
        if (step <= 0) return;

        Map<UUID, Integer> map = (tag == WorkItem.FeatureTag.TREE) ? treeBroken : veinBroken;
        int count = map.getOrDefault(p.getUniqueId(), 0) + 1;
        if (count >= step) {
            map.put(p.getUniqueId(), 0);
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand != null && hand.getItemMeta() instanceof Damageable dmg) {
                dmg.setDamage(dmg.getDamage() + 1);
                hand.setItemMeta(dmg);
            }
        } else {
            map.put(p.getUniqueId(), count);
        }
    }

    private void handlePlant(WorkItem wi, WorkQueue q) {
        Block airPos = wi.block;
        if (airPos.getType().isAir()) {
            Block soil = airPos.getRelative(0, -1, 0);
            if (cfg.treeAllowedSoils().contains(soil.getType())) {
                if (consumeIfRequired(wi.player, wi.plantType)) {
                    airPos.setType(wi.plantType, true);
                    return;
                }
            }
        }
        if (wi.retries > 0) {
            wi.retries--;
            wi.cooldownTicks = Math.max(1, cfg.treeReplantRetryCooldownTicks());
            q.add(wi);
        }
    }

    private boolean consumeIfRequired(Player p, Material sapling) {
        if (!cfg.treeReplantEnabled()) return false;
        if (!cfg.treeReplantRequireInventory()) return true; // allow planting without strict consume

        for (ItemStack stack : p.getInventory().getContents()) {
            if (stack == null) continue;
            if (stack.getType() == sapling && stack.getAmount() > 0) {
                stack.setAmount(stack.getAmount() - 1);
                return true;
            }
        }
        return false;
    }
}
