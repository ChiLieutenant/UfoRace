package com.chilieutenant.uforace.NFTSelection;

import com.chilieutenant.uforace.Main;
import com.chilieutenant.uforace.utils.NFTMethods;
import com.chilieutenant.uforace.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class NFTEvents implements Listener {

    @EventHandler
    public void inventoryInteractEvent(InventoryClickEvent event){
        Player p = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());
        if(p == null) return;
        if(event.getView().getTitle().equalsIgnoreCase(Main.getInvname())){
            event.setCancelled(true);
            ItemStack item = event.getInventory().getItem(event.getSlot());
            if(item == null) return;
            NFTItem nftitem = NFTMethods.getNFTItemBySlot(event.getSlot());
            if(nftitem == null) return;
            if(Utils.hasPermission(p,"vehicle." + nftitem.getName())) {
                NFTMethods.editVehicle(p, nftitem.getName());
                p.closeInventory();
                p.sendMessage(Utils.replaceColorCodes("&aYou selected " + nftitem.getName() + "!"));
            }
            else p.sendMessage(Utils.replaceColorCodes("&cYou don't have that NFT."));
        }
    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event){
        NFTMethods.saveDefault(event.getPlayer());
        NFTMethods.loadNFT(event.getPlayer());
    }
}
