package com.chilieutenant.uforace;

import com.chilieutenant.uforace.NFTSelection.NFTEvents;
import com.chilieutenant.uforace.NFTSelection.NFTItem;
import com.chilieutenant.uforace.arena.*;
import com.chilieutenant.uforace.items.ItemListener;
import com.chilieutenant.uforace.utils.Utils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.mongodb.client.MongoDatabase;
import com.nftworlds.wallet.api.WalletAPI;
import de.leonhard.storage.Yaml;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.io.File;
import java.io.FileInputStream;

public final class Main extends JavaPlugin {

    @Getter public static Main instance;
    @Getter private static WalletAPI wallet;
    @Getter private static LuckPerms luckperms;
    @Getter private static MongoDatabase dndDB = MongoUtils.loadDatabase("happyplace");
    @Getter private static Yaml inventorycfg;
    @Getter private static Inventory inv;
    @Getter private static List<NFTItem> items = new ArrayList<>();
    @Getter private static String invname;
    private ProtocolManager pm;

    @Getter private static HashMap<String, String> vehmobs = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mobkill endercrystal happyplaceminigames");

        pm = ProtocolLibrary.getProtocolManager();
        pm.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.STEER_VEHICLE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {

                    PacketContainer pc = event.getPacket();
                    Player player = event.getPlayer();


                    if(ArenaMethods.getArena(player) == null) return;
                    Arena arena = ArenaMethods.getArena(player);

                    // First float in packet is the Left/Right value (A/D)
                    float side = pc.getFloat().read(0);

                    // Second float in packet is the Forward/Backward value (W/S keys)
                    float forw = pc.getFloat().read(1);

                    arena.setVec(player, ArenaMethods.getVelocityVector(player, side, forw, arena.getSpeed(player)));

                    if(forw > 0 && arena.canSpeed(player)){
                        if(arena.getSpeed(player) < arena.getMaxSpeed(player)) {
                            arena.setSpeed(player, arena.getSpeed(player) + 0.0045);
                        }
                    }else{
                        arena.setSpeed(player, arena.getSpeed(player) - 0.005);
                    }
                }
            }
        });
        instance = this;
        ArenaMethods.loadArenas();
        Bukkit.getPluginManager().registerEvents(new NFTEvents(), this);
        Bukkit.getPluginManager().registerEvents(new ArenaListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemListener(), this);

        this.getCommand("ur").setExecutor(new UfoRaceCommand());
        this.getCommand("ur").setTabCompleter(new UfoRaceTC());

        wallet = new WalletAPI();
        luckperms = LuckPermsProvider.get();
        inventorycfg = new Yaml("inventory", "plugins/UfoRace");

        saveDefault();
        inv = Bukkit.createInventory(null, inventorycfg.getInt("size"), invname);
        loadDefault();
        loadMobs();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        controlQueue();
    }

    @Getter private static String[] vehs = {"SAD FOMO Explorer", "SAD To The Moon Spaceship", "Sad Builder", "SAD ATV4ATH",
                                            "SAD Speedster", "SAD WAGMI Bike", "SAD Elon", "Sad Tragic Truck",
                                            "SAD LFG Bike", "SAD Speedster X", "SAD F1-GM", "SAD Degen Bike" };

    public void saveDefault(){
        ArrayList<String> list = new ArrayList();
        list.add("&aTest lore");

        int i = 0;
        for(String veh : vehs){
            inventorycfg.setDefault("vehs." + veh + ".name", "&a"+veh);
            inventorycfg.setDefault("vehs." + veh + ".nftname", veh);
            inventorycfg.setDefault("vehs." + veh + ".slot", i);
            inventorycfg.setDefault("vehs." + veh + ".lore", list);
            i++;
        }

        inventorycfg.setDefault("size", 2*9);
        inventorycfg.setDefault("name", "&aInventory");

        invname = Utils.replaceColorCodes(inventorycfg.getString("name"));
    }

    public void loadMobs(){
        vehmobs.put("SAD FOMO Explorer", "spacecraft_01");
        vehmobs.put("SAD To The Moon Spaceship", "spacecraft_02");
        vehmobs.put("Sad Builder", "dozer");
        vehmobs.put("SAD ATV4ATH", "atv");
        vehmobs.put("SAD Speedster", "racing_car_01");
        vehmobs.put("SAD Speedster X", "racing_car_02");
        vehmobs.put("SAD F1-GM", "racing_car_03");
        vehmobs.put("SAD LFG Bike", "motorbike_01");
        vehmobs.put("SAD Degen Bike", "motorbike_02");
        vehmobs.put("SAD WAGMI Bike", "motorbike_03");
        vehmobs.put("SAD Elon", "cybertruck");
        vehmobs.put("Sad Tragic Truck", "truck");
    }

    public void loadDefault(){
        for(String set : inventorycfg.singleLayerKeySet("vehs")){
            String name = inventorycfg.getString("vehs." + set + ".nftname");
            int slot = inventorycfg.getInt("vehs." + set + ".slot");
            ItemStack item = Utils.itemBuilder(set, inventorycfg);
            inv.setItem(slot, item);

            new NFTItem(name, slot, item);
        }
    }

    public void controlQueue(){
        for(Player p : Bukkit.getOnlinePlayers()){
            Arena arena = ArenaMethods.getQueuedArena(p);
            if(arena != null) arena.removePlayerFromQueue(p);
        }
    }
}
