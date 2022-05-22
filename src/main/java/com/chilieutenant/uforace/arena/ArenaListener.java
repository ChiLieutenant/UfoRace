package com.chilieutenant.uforace.arena;

import com.chilieutenant.uforace.Main;
import com.chilieutenant.uforace.events.GameJoinEvent;
import com.ticxo.modelengine.api.ModelEngineAPI;
import io.lumine.mythic.api.MythicPlugin;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.Objects;

public class ArenaListener implements Listener{

    @EventHandler
    public void onMobSpawn(MythicMobSpawnEvent event){
        ActiveMob mob = event.getMob();
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                if(!mob.getOwner().isPresent()){
                    return;
                }
                if(Bukkit.getPlayer(Objects.requireNonNull(mob.getOwner().get())) == null){
                    return;
                }
                Player owner = Bukkit.getPlayer(mob.getOwner().get());
                Arena arena = ArenaMethods.getArena(owner);
                if(arena == null) return;

                arena.setVehicle(owner, mob);
            }
        }, 10);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        p.teleport(p.getWorld().getSpawnLocation());
        p.setGameMode(GameMode.SURVIVAL);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player p = event.getPlayer();
        for(Arena arena : ArenaMethods.getArenas()){
            if(arena.queuePlayers().contains(p)) arena.removePlayerFromQueue(p);
            if(arena.arenaPlayers().contains(p)) arena.removePlayerFromArena(p);
        }
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event){
        if(!event.getEntityType().equals(EntityType.PLAYER)) return;
        Player player = (Player) event.getEntity();
        if(ArenaMethods.getArena(player) != null) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if(event.getWhoClicked() instanceof Player player){
            if(ArenaMethods.getQueuedArena(player) != null) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event){
        if(!event.getAction().equals(Action.RIGHT_CLICK_AIR)) return;

        Player player = event.getPlayer();
        if(ArenaMethods.getQueuedArena(player) == null) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if(item.getType() == Material.AIR) return;
        if(item.getType() == Material.RED_BED) Bukkit.dispatchCommand(player, "ur leave");
    }

}
