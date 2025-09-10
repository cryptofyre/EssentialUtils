package net.ppekkungz.essentialUtils;

import net.ppekkungz.essentialUtils.config.PluginConfig;
import net.ppekkungz.essentialUtils.listener.ActivationListener;
import net.ppekkungz.essentialUtils.state.StateManager;
import net.ppekkungz.essentialUtils.work.WorkService;
import org.bukkit.plugin.java.JavaPlugin;

public final class EssentialUtils extends JavaPlugin {
    private static EssentialUtils instance;
    private PluginConfig cfg;
    private StateManager states;
    private WorkService work;

    @Override public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadPluginConfig();

        states = new StateManager();
        work = new WorkService(this, cfg, states); // Folia-safe per player loops

        getServer().getPluginManager().registerEvents(new ActivationListener(this, cfg, states, work), this);

        getLogger().info("Essential Utils enabled (Folia-safe).");
    }

    @Override public void onDisable() {
        if (work != null) work.shutdown();
        getLogger().info("Essential Utils disabled.");
    }

    public void loadPluginConfig() {
        this.cfg = new PluginConfig(getConfig());
    }

    public static EssentialUtils get() { return instance; }
    public PluginConfig cfg() { return cfg; }
    public StateManager states() { return states; }
    public WorkService work() { return work; }
}
