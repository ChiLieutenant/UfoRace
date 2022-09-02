package com.chilieutenant.uforace.items;

import com.chilieutenant.uforace.Main;
import com.chilieutenant.uforace.arena.Arena;
import com.chilieutenant.uforace.arena.ArenaMethods;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class ItemListener implements Listener {

    @EventHandler
    public void onDrop(PlayerDropItemEvent event){
        Item item = event.getItemDrop();
        if(ArenaMethods.getQueuedArena(event.getPlayer()) != null) event.setCancelled(true);
        Arena arena = ArenaMethods.getArena(event.getPlayer());

        if(arena == null) return;

        if(arena.getRandomRunnable(event.getPlayer()) != null && !arena.getRandomRunnable(event.getPlayer()).isCancelled()) {
            event.setCancelled(true);
            return;
        }

        if(item.getItemStack().isSimilar(Items.BANANA_PEEL.getItem())){
            item.setCustomName(event.getPlayer().getName() + " Banana Peel");
            item.setGlowing(true);
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                if(!item.isDead()) item.remove();
            }, 200);
        }

        if(item.getItemStack().isSimilar(Items.BOMB.getItem())){
            item.remove();
            ItemEffects.throwBomb(event.getPlayer());
        }

        if(item.getItemStack().isSimilar(Items.SADBOT_HEAD.getItem())){
            item.remove();
            ItemEffects.throwHead(event.getPlayer());
        }

        if(item.getItemStack().isSimilar(Items.SUPER_HORN.getItem())){
            item.remove();
            ItemEffects.superHorn(event.getPlayer());
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event){
        ItemStack item = event.getItem().getItemStack();
        if(item.getType().equals(Material.HORN_CORAL) && Objects.requireNonNull(item.getItemMeta()).getDisplayName().contains("Banana Peel")){
            event.setCancelled(true);
        }
    }


}
