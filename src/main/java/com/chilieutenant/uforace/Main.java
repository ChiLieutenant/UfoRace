package com.chilieutenant.uforace;

import com.chilieutenant.uforace.arena.*;
import com.chilieutenant.uforace.items.ItemListener;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public final class Main extends JavaPlugin {

    @Getter public static Main instance;
    private ProtocolManager pm;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mobkill endercrystal happyplaceminigames");

        pm = ProtocolLibrary.getProtocolManager();
        pm.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.STEER_VEHICLE) {
            //
            // Steer Vehicle packet gets called when the player is riding a vehicle and
            // presses WASD or some other keys like spacebar.
            //
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {

                    // Grab the necessary objects
                    PacketContainer pc = event.getPacket();
                    Player player = event.getPlayer();


                    if(ArenaMethods.getArena(player) == null) return;
                    Arena arena = ArenaMethods.getArena(player);

                    // First float in packet is the Left/Right value (A/D)
                    float side = pc.getFloat().read(0);

                    // Second float in packet is the Forward/Backward value (W/S keys)
                    float forw = pc.getFloat().read(1);

                    arena.setVec(player, ArenaMethods.getVelocityVector(player, side, forw, arena.getSpeed(player)));


                    // Now, calculate the new velocity using the function below, and apply to the
                    // vehicle entity
                    if(forw > 0 && arena.canSpeed(player)){
                        if(arena.getSpeed(player) < 5) arena.setSpeed(player, arena.getSpeed(player) + 0.0075);
                    }else{
                        arena.setSpeed(player, arena.getSpeed(player) - 0.02);
                    }
                }
            }
        });
        instance = this;
        ArenaMethods.loadArenas();
        Bukkit.getPluginManager().registerEvents(new ArenaListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemListener(), this);
        this.getCommand("ur").setExecutor(new UfoRaceCommand());
        this.getCommand("ur").setTabCompleter(new UfoRaceTC());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
