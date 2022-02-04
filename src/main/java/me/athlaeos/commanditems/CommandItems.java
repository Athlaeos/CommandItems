package me.athlaeos.commanditems;

import me.athlaeos.commanditems.commands.BindCommand;
import me.athlaeos.commanditems.listeners.InteractListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class CommandItems extends JavaPlugin {
    private static CommandItems plugin = null;

    @Override
    public void onEnable() {
        plugin = this;
        // Plugin startup logic
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new InteractListener(), this);
        new BindCommand(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static CommandItems getPlugin() {
        return plugin;
    }
}
