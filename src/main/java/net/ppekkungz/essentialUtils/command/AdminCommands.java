package net.ppekkungz.essentialUtils.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.ppekkungz.essentialUtils.EssentialUtils;
import net.ppekkungz.essentialUtils.config.PluginConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Admin commands for EssentialUtils using Brigadier.
 * Supports enable/disable modules, status check, and config reload.
 */
@SuppressWarnings("UnstableApiUsage")
public class AdminCommands {
    private final EssentialUtils plugin;
    
    private static final List<String> MODULES = Arrays.asList("treefeller", "veinminer", "autofarm");

    public AdminCommands(EssentialUtils plugin) {
        this.plugin = plugin;
    }

    /**
     * Register commands using Paper's Brigadier API.
     */
    public void register() {
        LifecycleEventManager<Plugin> manager = plugin.getLifecycleManager();
        
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            
            // Register /eutils command - need to build() the command first
            commands.register(
                buildCommand("eutils").build(),
                "EssentialUtils admin commands",
                List.of("eu", "essentialutils")
            );
        });
    }

    /**
     * Build the command tree.
     */
    private LiteralArgumentBuilder<CommandSourceStack> buildCommand(String name) {
        return Commands.literal(name)
            // /eutils (no args - show help)
            .executes(this::showHelp)
            
            // /eutils status
            .then(Commands.literal("status")
                .requires(source -> source.getSender().hasPermission("essentialutils.admin"))
                .executes(this::showStatus))
            
            // /eutils reload
            .then(Commands.literal("reload")
                .requires(source -> source.getSender().hasPermission("essentialutils.admin"))
                .executes(this::reloadConfig))
            
            // /eutils enable <module>
            .then(Commands.literal("enable")
                .requires(source -> source.getSender().hasPermission("essentialutils.admin"))
                .then(Commands.argument("module", StringArgumentType.word())
                    .suggests(this::suggestModules)
                    .executes(this::enableModule)))
            
            // /eutils disable <module>
            .then(Commands.literal("disable")
                .requires(source -> source.getSender().hasPermission("essentialutils.admin"))
                .then(Commands.argument("module", StringArgumentType.word())
                    .suggests(this::suggestModules)
                    .executes(this::disableModule)));
    }

    /**
     * Suggest module names for tab completion.
     */
    private CompletableFuture<Suggestions> suggestModules(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase();
        MODULES.stream()
            .filter(m -> m.startsWith(input))
            .forEach(builder::suggest);
        return builder.buildFuture();
    }

    private int showHelp(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        sender.sendMessage("§6§l[EssentialUtils] §fCommands:");
        sender.sendMessage("");
        sender.sendMessage("  §e/eutils enable <module> §7- Enable a module");
        sender.sendMessage("  §e/eutils disable <module> §7- Disable a module");
        sender.sendMessage("  §e/eutils status §7- View module status");
        sender.sendMessage("  §e/eutils reload §7- Reload configuration");
        sender.sendMessage("");
        sender.sendMessage("§7Modules: treefeller, veinminer, autofarm");
        return Command.SINGLE_SUCCESS;
    }

    private int showStatus(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        PluginConfig cfg = plugin.cfg();
        
        sender.sendMessage("§6§l[EssentialUtils] §fModule Status:");
        sender.sendMessage("");
        
        // Tree Feller
        String treeStatus = cfg.treeFellerEnabled() ? "§a✓ Enabled" : "§c✗ Disabled";
        sender.sendMessage("  §f⚒ Tree Feller: " + treeStatus);
        if (cfg.treeFellerEnabled()) {
            sender.sendMessage("    §7├ Max blocks: §f" + cfg.treeFellerMaxBlocks());
            sender.sendMessage("    §7├ Replant: " + (cfg.treeFellerReplant() ? "§aYes" : "§cNo"));
            sender.sendMessage("    §7└ Particles: " + (cfg.treeFellerParticles() ? "§aYes" : "§cNo"));
        }
        
        // Vein Miner
        String veinStatus = cfg.veinMinerEnabled() ? "§a✓ Enabled" : "§c✗ Disabled";
        sender.sendMessage("  §b⛏ Vein Miner: " + veinStatus);
        if (cfg.veinMinerEnabled()) {
            sender.sendMessage("    §7├ Max ores: §f" + cfg.veinMinerMaxOres());
            sender.sendMessage("    §7├ Fortune: " + (cfg.veinMinerFortuneEnabled() ? "§aYes" : "§cNo"));
            sender.sendMessage("    §7└ Silk Touch: " + (cfg.veinMinerSilkTouchDropsOre() ? "§aDrops ore" : "§cDrops resources"));
        }
        
        // Auto Farm
        String farmStatus = cfg.autoFarmEnabled() ? "§a✓ Enabled" : "§c✗ Disabled";
        sender.sendMessage("  §e🌾 Auto Farm: " + farmStatus);
        if (cfg.autoFarmEnabled()) {
            sender.sendMessage("    §7├ Radius: §f" + cfg.autoFarmRadius());
            sender.sendMessage("    §7└ Replant: " + (cfg.autoFarmReplant() ? "§aYes" : "§cNo"));
        }
        
        sender.sendMessage("");
        sender.sendMessage("§7Performance: §f" + cfg.blocksPerTick() + " blocks/tick");
        
        return Command.SINGLE_SUCCESS;
    }

    private int reloadConfig(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        plugin.reloadConfig();
        plugin.loadPluginConfig();
        sender.sendMessage("§a[EssentialUtils] §fConfiguration reloaded!");
        return Command.SINGLE_SUCCESS;
    }

    private int enableModule(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        String module = StringArgumentType.getString(context, "module").toLowerCase();
        PluginConfig cfg = plugin.cfg();
        
        switch (module) {
            case "treefeller", "tree" -> {
                cfg.setTreeFellerEnabled(true);
                sender.sendMessage("§a[EssentialUtils] §fTree Feller §aenabled!");
            }
            case "veinminer", "vein" -> {
                cfg.setVeinMinerEnabled(true);
                sender.sendMessage("§a[EssentialUtils] §fVein Miner §aenabled!");
            }
            case "autofarm", "farm" -> {
                cfg.setAutoFarmEnabled(true);
                sender.sendMessage("§a[EssentialUtils] §fAuto Farm §aenabled!");
            }
            default -> {
                sender.sendMessage("§c[EssentialUtils] Unknown module: §f" + module);
                sender.sendMessage("§7Valid modules: treefeller, veinminer, autofarm");
                return Command.SINGLE_SUCCESS;
            }
        }
        
        plugin.saveConfig();
        return Command.SINGLE_SUCCESS;
    }

    private int disableModule(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        String module = StringArgumentType.getString(context, "module").toLowerCase();
        PluginConfig cfg = plugin.cfg();
        
        switch (module) {
            case "treefeller", "tree" -> {
                cfg.setTreeFellerEnabled(false);
                sender.sendMessage("§c[EssentialUtils] §fTree Feller §cdisabled!");
            }
            case "veinminer", "vein" -> {
                cfg.setVeinMinerEnabled(false);
                sender.sendMessage("§c[EssentialUtils] §fVein Miner §cdisabled!");
            }
            case "autofarm", "farm" -> {
                cfg.setAutoFarmEnabled(false);
                sender.sendMessage("§c[EssentialUtils] §fAuto Farm §cdisabled!");
            }
            default -> {
                sender.sendMessage("§c[EssentialUtils] Unknown module: §f" + module);
                sender.sendMessage("§7Valid modules: treefeller, veinminer, autofarm");
                return Command.SINGLE_SUCCESS;
            }
        }
        
        plugin.saveConfig();
        return Command.SINGLE_SUCCESS;
    }
}
