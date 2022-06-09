package com.chilieutenant.uforace.arena;

import com.chilieutenant.uforace.Main;
import com.chilieutenant.uforace.events.GameJoinEvent;
import com.chilieutenant.uforace.events.GameLeaveEvent;
import com.chilieutenant.uforace.utils.NFTMethods;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UfoRaceCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if(args.length < 1) return false;

        if(player.hasPermission("admin.ur")) {
            if (args[0].equalsIgnoreCase("arena")) {
                if(args.length == 3){
                    if(args[1].equalsIgnoreCase("create")) {
                        String name = args[2];
                        if (ArenaMethods.isArenaCreated(name)) {
                            player.sendMessage(ChatColor.RED + "A arena named " + name + " has already created.");
                            return false;
                        }
                        ArenaMethods.createArena(name);
                        player.sendMessage(ChatColor.GREEN + "You have successfully created an arena named " + name);
                    }
                    if(args[1].equalsIgnoreCase("remove")) {
                        String name = args[2];
                        if (!ArenaMethods.isArenaCreated(name)) {
                            player.sendMessage(ChatColor.RED + "A arena named " + name + " has not created.");
                            return false;
                        }
                        ArenaMethods.removeArena(name);
                        player.sendMessage(ChatColor.GREEN + "You have successfully removed an arena named " + name);
                    }
                    if(args[1].equalsIgnoreCase("endloc")) {
                        String name = args[2];
                        if (!ArenaMethods.isArenaCreated(name) || ArenaMethods.getArena(name) == null) {
                            player.sendMessage(ChatColor.RED + "A arena named " + name + " has not created.");
                            return false;
                        }
                        Arena arena = ArenaMethods.getArena(name);
                        arena.setEndLocation(player.getLocation());
                        player.sendMessage(ChatColor.GREEN + "You have successfully set the end location of " + name);
                    }
                    if(args[1].equalsIgnoreCase("lobby")) {
                        String name = args[2];
                        if (!ArenaMethods.isArenaCreated(name) || ArenaMethods.getArena(name) == null) {
                            player.sendMessage(ChatColor.RED + "A arena named " + name + " has not created.");
                            return false;
                        }
                        Arena arena = ArenaMethods.getArena(name);
                        arena.setLobbyLocation(player.getLocation());
                        player.sendMessage(ChatColor.GREEN + "You have successfully set the lobby location of " + name);
                    }
                    if(args[1].equalsIgnoreCase("addbuffer")) {
                        String name = args[2];
                        if (!ArenaMethods.isArenaCreated(name) || ArenaMethods.getArena(name) == null) {
                            player.sendMessage(ChatColor.RED + "A arena named " + name + " has not created.");
                            return false;
                        }
                        Arena arena = ArenaMethods.getArena(name);
                        arena.addBuffer(player.getLocation());
                        player.sendMessage(ChatColor.GREEN + "You have successfully added a buffer to " + name);
                    }
                    if(args[1].equalsIgnoreCase("addstart")) {
                        String name = args[2];
                        if (!ArenaMethods.isArenaCreated(name) || ArenaMethods.getArena(name) == null) {
                            player.sendMessage(ChatColor.RED + "A arena named " + name + " has not created.");
                            return false;
                        }
                        Arena arena = ArenaMethods.getArena(name);
                        arena.addStartLoc(player.getLocation());
                        player.sendMessage(ChatColor.GREEN + "You have successfully added a start location to " + name);
                    }
                }
            }
        }

        if(args[0].equalsIgnoreCase("join")){
            if(ArenaMethods.getAvailableArenas().isEmpty()){
                player.sendMessage(ChatColor.RED + "There is no available arena at the moment, please try again later.");
                return false;
            }
            for(Arena arena : ArenaMethods.getArenas()){
                if(arena.queuePlayers().contains(player)) {
                    player.sendMessage(ChatColor.RED + "You are already in a queue.");
                    return false;
                }
                if(arena.arenaPlayers().contains(player)){
                    player.sendMessage(ChatColor.RED + "You are already in a game.");
                    return false;
                }
            }
            Arena arena = ArenaMethods.getAvailableArenas().get(0);

            GameJoinEvent event = new GameJoinEvent(player, arena);
            Bukkit.getPluginManager().callEvent(event);
            if(event.isCancelled()) return false;

            arena.addPlayerToQueue(player);
            player.sendMessage(ChatColor.GREEN + "You have joined the lobby.");
        }

        if(args[0].equalsIgnoreCase("leave")){
            Arena queuedArena = ArenaMethods.getQueuedArena(player);
            Arena arena = ArenaMethods.getArena(player);
            if (queuedArena == null && arena == null) {
                player.sendMessage(ChatColor.RED + "You are not in a lobby or a game.");
                return false;
            }
            if(queuedArena != null) {
                queuedArena.removePlayerFromQueue(player);
                GameLeaveEvent event = new GameLeaveEvent(player, arena);
                Bukkit.getPluginManager().callEvent(event);
            }
            if(arena != null){
                arena.removePlayerFromArena(player);
                player.teleport(player.getWorld().getSpawnLocation());
            }
        }
        return false;
    }
}
