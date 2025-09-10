package net.ppekkungz.essentialUtils.command;

import net.ppekkungz.essentialUtils.EssentialUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private final EssentialUtils plugin;

    public ReloadCommand(EssentialUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.loadPluginConfig();
            sender.sendMessage("§a[EssentialUtils] Config reloaded!");
            return true;
        }
        sender.sendMessage("§cUsage: /" + label + " reload");
        return true;
    }
}
