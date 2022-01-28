package me.athlaeos.commanditems.listeners;

import me.athlaeos.commanditems.Utils;
import me.athlaeos.commanditems.managers.ItemBoundCommandsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class InteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        if (e.getHand() == EquipmentSlot.HAND && (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)){
            ItemStack mainHandItem = e.getPlayer().getInventory().getItemInMainHand();
            if (Utils.isItemEmptyOrNull(mainHandItem)) return;
            if (!ItemBoundCommandsManager.getInstance().getCommands(mainHandItem).isEmpty()){
                ItemBoundCommandsManager.getInstance().executeItem(e.getPlayer(), mainHandItem);
                e.setCancelled(true);
            }
        }
    }

}
