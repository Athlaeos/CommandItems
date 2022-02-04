package me.athlaeos.commanditems.commands;

import me.athlaeos.commanditems.CommandItems;
import me.athlaeos.commanditems.Utils;
import me.athlaeos.commanditems.managers.ItemBoundCommandsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BindCommand implements TabExecutor {
    private final String password;
    private final int max_invalid_attempts;
    private final List<String> banned_commands;
    private final Map<UUID, Integer> passwordAttempts = new HashMap<>();

    public BindCommand(CommandItems plugin){
        this.password = plugin.getConfig().getString("password");
        this.max_invalid_attempts = plugin.getConfig().getInt("max_invalid_attempts");
        this.banned_commands = plugin.getConfig().getStringList("command_blacklist");
        PluginCommand c = plugin.getCommand("commanditem");
        if (c != null){
            c.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            if (sender.hasPermission("commanditems.create") || sender.hasPermission("commanditems.create.op")) {
                ItemStack mainHandItem = ((Player) sender).getInventory().getItemInMainHand();
                if (Utils.isItemEmptyOrNull(mainHandItem)) {
                    sender.sendMessage(Utils.chat("&cYou must hold an item in your main hand"));
                    return true;
                }
                if (args.length > 0){
                    if (args[0].equalsIgnoreCase("add")){
                        if (args.length > 1){
                            String cmd = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                            for (String bannedCommand : banned_commands){
                                if (cmd.toLowerCase().contains(bannedCommand.toLowerCase())) {
                                    sender.sendMessage(Utils.chat("&cIllegal command!"));
                                    return true;
                                }
                            }
                            ItemBoundCommandsManager.getInstance().addCommand(mainHandItem, cmd);
                            sender.sendMessage(Utils.chat("&aCommand '/" + cmd + "' was bound to the item"));
                            if (ItemBoundCommandsManager.getInstance().getCommands(mainHandItem).size() == 1){
                                sender.sendMessage(Utils.chat("&aThis item also respects player permissions"));
                            }
                        } else {
                            sender.sendMessage(Utils.chat("&cToo few arguments, add a command the item should execute"));
                        }
                        return true;
                    } else if (args[0].equalsIgnoreCase("clear")){
                        ItemBoundCommandsManager.getInstance().removeCommands(mainHandItem);
                        sender.sendMessage(Utils.chat("&aItem was purged of its commands and properties"));
                        return true;
                    } else if (args[0].equalsIgnoreCase("stats")){
                        Collection<String> commands = ItemBoundCommandsManager.getInstance().getCommands(mainHandItem);
                        boolean isConsumed = ItemBoundCommandsManager.getInstance().isConsumed(mainHandItem);
                        boolean requiresPermission = ItemBoundCommandsManager.getInstance().requirePermission(mainHandItem);
                        int cooldown = ItemBoundCommandsManager.getInstance().getCooldown(mainHandItem);
                        sender.sendMessage(Utils.chat("&8&m                                  "));
                        sender.sendMessage(Utils.chat("&9Consumable: " + isConsumed));
                        sender.sendMessage(Utils.chat("&9Permission: " + requiresPermission));
                        sender.sendMessage(Utils.chat("&9Cooldown: " + Utils.toTimeStamp(cooldown, 1000)));
                        sender.sendMessage(Utils.chat("&9Commands: "));
                        for (String s : commands){
                            sender.sendMessage(Utils.chat("&7- " + s));
                        }
                        return true;
                    } else if (args[0].equalsIgnoreCase("setconsumable")){
                        if (ItemBoundCommandsManager.getInstance().getCommands(mainHandItem).isEmpty()){
                            sender.sendMessage(Utils.chat("&cItem cannot have this property assigned while having no commands"));
                            return true;
                        }
                        if (args.length > 1){
                            boolean consumable;
                            if (args[1].equalsIgnoreCase("true")) consumable = true;
                            else if (args[1].equalsIgnoreCase("false")) consumable = false;
                            else {
                                sender.sendMessage(Utils.chat("&cThe only acceptable options are 'true' and 'false'"));
                                return true;
                            }
                            ItemBoundCommandsManager.getInstance().setConsumed(mainHandItem, consumable);
                            if (consumable){
                                sender.sendMessage(Utils.chat("&aThe item will now be consumed upon usage"));
                            } else {
                                sender.sendMessage(Utils.chat("&aThe item will now not be consumed upon usage"));
                            }
                        } else {
                            sender.sendMessage(Utils.chat("&cToo few arguments, add a 'true' or 'false' depending on if you want the item consumed or not"));
                        }
                        return true;
                    } else if (args[0].equalsIgnoreCase("setrequirepermission")){
                        if (ItemBoundCommandsManager.getInstance().getCommands(mainHandItem).isEmpty()){
                            sender.sendMessage(Utils.chat("&cItem cannot have this property assigned while having no commands"));
                            return true;
                        }
                        if (args.length > 2){
                            boolean permissionsRequired;
                            if (passwordAttempts.getOrDefault(((Player) sender).getUniqueId(), 0) >= max_invalid_attempts){
                                sender.sendMessage(Utils.chat("&cToo many invalid attempts, try again next restart."));
                                return true;
                            }
                            if (!args[2].equals(password)){
                                int currentAttempts = passwordAttempts.getOrDefault(((Player) sender).getUniqueId(), 0);
                                passwordAttempts.put(((Player) sender).getUniqueId(), currentAttempts + 1);
                                sender.sendMessage(Utils.chat("&cInvalid password, try again. (" + (max_invalid_attempts - currentAttempts) + " attempts remaining)"));
                                return true;
                            }
                            if (args[1].equalsIgnoreCase("true")) permissionsRequired = true;
                            else if (args[1].equalsIgnoreCase("false")) permissionsRequired = false;
                            else {
                                sender.sendMessage(Utils.chat("&cThe only acceptable options are 'true' and 'false'"));
                                return true;
                            }
                            ItemBoundCommandsManager.getInstance().setRequirePermission(mainHandItem, permissionsRequired);
                            if (permissionsRequired){
                                sender.sendMessage(Utils.chat("&aThe item will now respect permissions"));
                            } else {
                                sender.sendMessage(Utils.chat("&aThe item will no longer respect permissions, I trust you know what you're doing"));
                            }
                        } else {
                            sender.sendMessage(Utils.chat("&cToo few arguments, add a 'true' or 'false' depending on if you want permissions respected or not as well as the password"));
                        }
                        return true;
                    } else if (args[0].equalsIgnoreCase("setcooldown")){
                        if (ItemBoundCommandsManager.getInstance().getCommands(mainHandItem).isEmpty()){
                            sender.sendMessage(Utils.chat("&cItem cannot have this property assigned while having no commands"));
                            return true;
                        }
                        if (args.length > 1){
                            int cooldown;
                            try {
                                cooldown = Integer.parseInt(args[1]);
                                if (cooldown < 100){
                                    sender.sendMessage(Utils.chat("&7Please keep in mind the cooldown you're entering is in &emilliseconds (1000 = 1 second)"));
                                }
                            } catch (IllegalArgumentException ignored){
                                sender.sendMessage(Utils.chat("&cNot a valid cooldown, enter a whole number to represent the cooldown in &emilliseconds"));
                                return true;
                            }
                            ItemBoundCommandsManager.getInstance().setCooldown(mainHandItem, cooldown);
                            if (cooldown <= 0){
                                sender.sendMessage(Utils.chat("&aThe item will no longer have a cooldown"));
                            } else {
                                sender.sendMessage(Utils.chat("&aThe item will now have a cooldown of " + Utils.toTimeStamp(cooldown, 1000)));
                            }
                        } else {
                            sender.sendMessage(Utils.chat("&cToo few arguments, add a whole number to represent the cooldown in &emilliseconds"));
                        }
                        return true;
                    }
                }
            } else {
                sender.sendMessage(Utils.chat("&cYou can't do that"));
            }
        } else {
            sender.sendMessage(Utils.chat("&cNot allowed"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1){
            return Arrays.asList("add", "clear", "setconsumable", "setrequirepermission", "setcooldown", "stats");
        }
        if (args.length == 2){
            if (args[0].equalsIgnoreCase("setconsumable") || args[0].equalsIgnoreCase("setrequirepermission")){
                return Arrays.asList("true", "false");
            } else if (args[0].equalsIgnoreCase("setcooldown")){
                return Collections.singletonList("cooldownmillis");
            }
        }
        if (args.length == 3){
            if (args[0].equalsIgnoreCase("setrequirepermission")){
                return Collections.singletonList("password");
            }
        }
        return null;
    }
}
