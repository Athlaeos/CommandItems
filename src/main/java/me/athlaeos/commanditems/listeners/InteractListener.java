package me.athlaeos.commanditems.listeners;

import me.athlaeos.commanditems.Utils;
import me.athlaeos.commanditems.managers.CooldownManager;
import me.athlaeos.commanditems.managers.ItemBoundCommandsManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class InteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        if (e.getHand() == EquipmentSlot.HAND && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)){
            ItemStack mainHandItem = e.getPlayer().getInventory().getItemInMainHand();
            if (Utils.isItemEmptyOrNull(mainHandItem)) return;
            if (!ItemBoundCommandsManager.getInstance().getCommands(mainHandItem).isEmpty()){
                long remainingCooldown = CooldownManager.getInstance().getCooldown(e.getPlayer().getUniqueId(), ItemBoundCommandsManager.getInstance().getCooldownID(mainHandItem));
                if (remainingCooldown <= 0){
                    e.getPlayer().getInventory().setItemInMainHand(ItemBoundCommandsManager.getInstance().executeItem(e.getPlayer(), mainHandItem));
                } else {
                    e.getPlayer().spigot().sendMessage(ChatMessageType.SYSTEM, new TextComponent(Utils.chat("&cCooldown: " + Utils.toTimeStamp((int) remainingCooldown, 1000))));
                }
                e.setCancelled(true);
            }
        }
    }

}
