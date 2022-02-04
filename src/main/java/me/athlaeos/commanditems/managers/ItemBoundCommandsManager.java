package me.athlaeos.commanditems.managers;

import me.athlaeos.commanditems.CommandItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ItemBoundCommandsManager {
    private static ItemBoundCommandsManager manager = null;
    private final NamespacedKey commandsKey = new NamespacedKey(CommandItems.getPlugin(), "commands");
    private final NamespacedKey consumedKey = new NamespacedKey(CommandItems.getPlugin(), "consumed");
    private final NamespacedKey requirePermissionKey = new NamespacedKey(CommandItems.getPlugin(), "permission_required");
    private final NamespacedKey cooldownKey = new NamespacedKey(CommandItems.getPlugin(), "cooldown");
    private final NamespacedKey cooldownIDKey = new NamespacedKey(CommandItems.getPlugin(), "cooldown_id");

    public static ItemBoundCommandsManager getInstance() {
        if (manager == null) manager = new ItemBoundCommandsManager();
        return manager;
    }

    public ItemStack executeItem(Player player, ItemStack i){
        if (i == null) return null;
        ItemStack copy = i.clone();
        Collection<String> commands = getCommands(i);
        boolean requirePermission = requirePermission(i);
        boolean isConsumed = isConsumed(i);
        for (String c : commands){
            if (requirePermission){
                player.performCommand(c.replace("%player%", player.getName()));
            } else {
                CommandItems.getPlugin().getServer().dispatchCommand(
                        CommandItems.getPlugin().getServer().getConsoleSender(),
                        c.replace("%player%", player.getName())
                );
            }
        }
        applyCooldown(player, copy);
        if (isConsumed){
            if (copy.getAmount() <= 1){
                copy = null;
            } else {
                copy.setAmount(copy.getAmount() - 1);
            }
        }
        return copy;
    }

    public boolean isConsumed(ItemStack i){
        if (i == null) return false;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(consumedKey, PersistentDataType.INTEGER);
    }

    public void setConsumed(ItemStack i, boolean consumed){
        if (i == null) return;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return;
        if (consumed){
            meta.getPersistentDataContainer().set(consumedKey, PersistentDataType.INTEGER, 1);
        } else {
            meta.getPersistentDataContainer().remove(consumedKey);
        }
        i.setItemMeta(meta);
    }

    public void setCooldown(ItemStack i, int cooldownMillis){
        if (i == null) return;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return;
        if (cooldownMillis <= 0){
            meta.getPersistentDataContainer().remove(cooldownKey);
            meta.getPersistentDataContainer().remove(cooldownIDKey);
        } else {
            meta.getPersistentDataContainer().set(cooldownKey, PersistentDataType.INTEGER, cooldownMillis);
            meta.getPersistentDataContainer().set(cooldownIDKey, PersistentDataType.STRING, UUID.randomUUID().toString());
        }
        i.setItemMeta(meta);
    }

    public int getCooldown(ItemStack i){
        if (i == null) return 0;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return 0;
        Integer value = meta.getPersistentDataContainer().get(cooldownKey, PersistentDataType.INTEGER);
        if (value == null) return 0;
        return value;
    }

    public String getCooldownID(ItemStack i){
        if (i == null) return null;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(cooldownIDKey, PersistentDataType.STRING);
    }

    public void applyCooldown(Player p, ItemStack i){
        if (i == null) return;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return;
        int cooldown = getCooldown(i);
        String key = getCooldownID(i);
        if (cooldown <= 0 || key == null) return;
        CooldownManager.getInstance().setCooldown(p.getUniqueId(), cooldown, key);
    }

    public boolean requirePermission(ItemStack i){
        if (i == null) return false;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(requirePermissionKey, PersistentDataType.INTEGER);
    }

    public void setRequirePermission(ItemStack i, boolean required){
        if (i == null) return;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return;
        if (required){
            meta.getPersistentDataContainer().set(requirePermissionKey, PersistentDataType.INTEGER, 1);
        } else {
            meta.getPersistentDataContainer().remove(requirePermissionKey);
        }
        i.setItemMeta(meta);
    }

    public Collection<String> getCommands(ItemStack i){
        Collection<String> commands = new HashSet<>();
        if (i == null) return commands;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return commands;
        if (meta.getPersistentDataContainer().has(commandsKey, PersistentDataType.STRING)){
            String value = meta.getPersistentDataContainer().get(commandsKey, PersistentDataType.STRING);
            if (value == null) return commands;
            commands.addAll(Arrays.asList(value.split("<!>")));
        }
        return commands;
    }

    public void setCommands(ItemStack i, Collection<String> commands){
        if (i == null) return;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(commandsKey, PersistentDataType.STRING, String.join("<!>", commands));
        i.setItemMeta(meta);
    }

    public void addCommand(ItemStack i, String command){
        Collection<String> commands = getCommands(i);
        if (commands.isEmpty()) setRequirePermission(i, true);
        commands.add(command);
        setCommands(i, commands);
    }

    public void removeCommands(ItemStack i){
        if (i == null) return;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().remove(commandsKey);
        meta.getPersistentDataContainer().remove(requirePermissionKey);
        meta.getPersistentDataContainer().remove(consumedKey);
        i.setItemMeta(meta);
    }
}
