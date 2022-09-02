package com.chilieutenant.uforace.arena;

import com.chilieutenant.uforace.Main;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.leonhard.storage.Json;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class ArenaMethods {

    @Getter public static List<Arena> arenas = new ArrayList<>();
    public static Material[] whitelist = new Material[]{
            Material.COAL_BLOCK, Material.RED_STAINED_GLASS, Material.ORANGE_STAINED_GLASS, Material.YELLOW_STAINED_GLASS, Material.LIME_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS, Material.BLUE_STAINED_GLASS, Material.PURPLE_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS, Material.MAGENTA_GLAZED_TERRACOTTA, Material.WHITE_WOOL, Material.BLACK_WOOL, Material.ORANGE_CONCRETE, Material.RED_CONCRETE, Material.PURPLE_CONCRETE, Material.BLUE_CONCRETE, Material.LIGHT_BLUE_CONCRETE, Material.LIME_CONCRETE, Material.YELLOW_CONCRETE, Material.YELLOW_CONCRETE_POWDER
    };

    public static Json getArenaData(String arena){
        return new Json(arena, "plugins/UfoRace/arena");
    }

    public static boolean isArenaCreated(String arena){
        return new File("plugins/UfoRace/arena/" + arena + ".json").isFile();
    }

    public static void createArena(String arena){
        Json data = getArenaData(arena);
        data.setDefault("buffers", Collections.singletonList(""));
        data.setDefault("players", Collections.singletonList(""));
        data.setDefault("queue", Collections.singletonList(""));
        data.setDefault("minplayers", 2);
        data.setDefault("maxplayers", 5);
        Arena arena1 = new Arena(arena);
        arenas.add(arena1);
    }

    public static void removeArena(String arena){
        arenas.removeIf(arena1 -> arena1.getData().getName().equalsIgnoreCase(arena));
        File file = new File("plugins/UfoRace/arena/" + arena);
        file.delete();
    }

    public static List<Json> getArenaList(){
        List<Json> arenas = new ArrayList<>();
        for(File file : new File("plugins/UfoRace/arena").listFiles()){
            arenas.add(new Json(file));
        }
        return arenas;
    }

    public static void loadArenas(){
        for(Json arenaData : getArenaList()){
            String arenaName = arenaData.getName();
            arenaName = arenaName.replace(".json", "");
            Bukkit.broadcastMessage(arenaName);
            Arena arena = new Arena(arenaName);
            arenas.add(arena);
        }
    }

    public static Arena getArena(String name){
        for(Arena arena : arenas){
            if(arena.getData().getName().equalsIgnoreCase(name + ".json")) return arena;
        }
        return null;
    }

    public static List<Arena> getUsableArenas(){
        List<Arena> arenas = new ArrayList<>();
        for(Arena arena : getArenas()){
            if(arena.getMinplayers() > 0 && arena.getMaxplayers() > arena.getMinplayers() && arena.getStartLocs().size() >= arena.getMinplayers() && arena.getLobbyLocation() != null && arena.getEndLocation() != null){
                arenas.add(arena);
            }
        }
        return arenas;
    }

    public static Arena getQueuedArena(Player player){
        for(Arena arena : arenas){
            if(arena.queuePlayers().contains(player)) return arena;
        }
        return null;
    }

    public static Arena getArena(Player player){
        for(Arena arena : arenas){
            if(arena.arenaPlayers().contains(player)) return arena;
        }
        return null;
    }

    public static List<Arena> getAvailableArenas(){
        List<Arena> arenas = new ArrayList<>();
        for(Arena arena : getUsableArenas()){
            if(arena.isAvailable()){
                arenas.add(arena);
            }
        }
        return arenas;
    }

    public static Vector getVelocityVector(Player player, float side, float forw, double speed) {
        // First, kill horizontal velocity that the entity might already have

        //
        // Many tests were run to get the math seen below.
        //

        // Create a new vector representing the direction of WASD
        Vector mot = new Vector(forw * -1.0, 0, side);

        if (mot.length() > 0.0) {
            // Turn to face the direction the player is facing
            mot.rotateAroundY(Math.toRadians(player.getLocation().getYaw() * -1.0F + 90.0F));
            // Now bring it back to a reasonable speed (0.2, reasonable default speed, can
            // be configured)
            mot.normalize().multiply(speed);
        }

        // Now, take this new horizontal direction velocity, and add it to what we
        // already have (which will only be vertical velocity at this point.)
        // We need to preserve vertical velocity so we handle gravity properly.
        return mot;
    }

}
