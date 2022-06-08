package com.chilieutenant.uforace.arena;

import com.chilieutenant.uforace.Main;
import com.chilieutenant.uforace.events.GameFinishEvent;
import com.chilieutenant.uforace.items.ItemEffects;
import com.chilieutenant.uforace.items.Items;
import com.chilieutenant.uforace.utils.NFTMethods;
import com.chilieutenant.uforace.utils.Utils;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import de.leonhard.storage.Json;
import fr.mrmicky.fastboard.FastBoard;
import io.lumine.mythic.api.MythicPlugin;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.utils.MythicUtil;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftMinecart;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

import static com.chilieutenant.uforace.items.ItemEffects.slide;
import static com.chilieutenant.uforace.utils.Utils.methods;

public class Arena {

    @Getter private final Json data;
    @Getter @Setter private boolean isStarted;
    private List<EnderCrystal> crystals = new ArrayList<>();
    private HashMap<Player, Long> times = new HashMap<>();
    private HashMap<Player, Location> checkPoints = new HashMap<>();
    private HashMap<Player, ActiveMob> vehicles = new HashMap<>();
    private HashMap<Player, Vector> vecs = new HashMap<>();
    private HashMap<Player, Boolean> canSpeed = new HashMap<>();
    private HashMap<Player, Double> speed = new HashMap<>();
    private List<FastBoard> boards = new ArrayList<>();
    private HashMap<Player, String> cars = new HashMap<>();
    private HashMap<Player, BukkitTask> speedRunnable = new HashMap<>();
    private HashMap<Player, BukkitTask> randomRunnable = new HashMap<>();
    private long startTime;
    private BukkitTask runnable;
    public static HashMap<Player, ItemStack[]> storage = new HashMap<>();
    public static HashMap<Player, ItemStack[]> armors = new HashMap<>();

    public Arena(String name){
        data = ArenaMethods.getArenaData(name);
        data.set("available", true);
        data.set("players", Collections.singletonList(""));
        data.set("queue", Collections.singletonList(""));
    }

    public void setRandomRunnable(Player player, BukkitTask br){
        randomRunnable.put(player, br);
    }

    public BukkitTask getRandomRunnable(Player player){
        if(!randomRunnable.containsKey(player)) return null;
        return randomRunnable.get(player);
    }

    public void setSpeedRunnable(Player player, BukkitTask br){
        speedRunnable.put(player, br);
    }

    public BukkitTask getSpeedRunnable(Player player){
        if(!speedRunnable.containsKey(player)) return null;
        return speedRunnable.get(player);
    }

    public void setCar(Player player, String car){
        cars.put(player, car);
    }

    public String getCar(Player player){
        return cars.get(player);
    }

    public ActiveMob getVehicle(Player player){
        return vehicles.get(player);
    }

    public void setVehicle(Player player, ActiveMob vehicle){
        vehicles.put(player, vehicle);
    }

    public void setVec(Player player, Vector vec){
        vecs.put(player, vec);
    }

    public int getMinplayers() {
        return data.getInt("minplayers");
    }

    public void setMinplayers(int minplayers){
        data.set("minplayers", minplayers);
    }

    public int getMaxplayers() {
        return data.getInt("maxplayers");
    }

    public void setMaxplayers(int maxplayers){
        data.set("maxplayers", maxplayers);
    }

    public List<Location> getStartLocs(){
        List<Location> buffers = new ArrayList<>();
        for(String locString : data.getStringList("startlocs")){
            if(!locString.equalsIgnoreCase("") && Utils.getLocationString(locString) != null) buffers.add(Utils.getLocationString(locString));
        }
        return buffers;
    }

    public void addStartLoc(Location location){
        data.set("startlocs", Utils.addStringToList(data.getStringList("startlocs"), Utils.getStringLocation(location)));
    }

    public Location getEndLocation(){
        return Utils.getLocationString(data.getString("endlocation"));
    }

    public void setEndLocation(Location location){
        data.set("endlocation", Utils.getStringLocation(location));
    }

    public Location getLobbyLocation(){
        return Utils.getLocationString(data.getString("lobby"));
    }

    public void setLobbyLocation(Location location){
        data.set("lobby", Utils.getStringLocation(location));
    }

    public List<Location> getBuffers(){
        List<Location> buffers = new ArrayList<>();
        for(String locString : data.getStringList("buffers")){
            if(!locString.equalsIgnoreCase("") && Utils.getLocationString(locString) != null) buffers.add(Utils.getLocationString(locString));
        }
        return buffers;
    }

    public void addBuffer(Location location){
        data.set("buffers", Utils.addStringToList(data.getStringList("buffers"), Utils.getStringLocation(location)));
    }

    public boolean isAvailable(){
        return data.getBoolean("available");
    }

    public void setAvailable(boolean available){
        data.set("available", available);
    }

    public void addPlayerToQueue(Player player){
        data.set("queue", Utils.addStringToList(new LinkedList<>(data.getStringList("queue")), player.getUniqueId().toString()));
        if(data.getStringList("queue").size() >= getMinplayers()){
            startForQueue();
        }
        storage.put(player, player.getInventory().getContents());
        armors.put(player, player.getInventory().getArmorContents());
        player.getInventory().setItem(0, new ItemStack(Material.COMPASS));
        player.getInventory().setItem(2, new ItemStack(Material.DIAMOND));
        player.getInventory().setItem(6, Utils.itemBuilder(Material.COMPASS, "&aVehicle selection"));
        player.getInventory().setItem(8, Utils.getItem(Material.RED_BED, ChatColor.RED + "Leave"));
        player.teleport(getLobbyLocation());
        setCar(player, NFTMethods.getPlayerVehicle(player));
    }

    public void removePlayerFromQueue(Player player){
        data.set("queue", Utils.removeStringFromList(data.getStringList("queue"), player.getUniqueId().toString()));
        player.getInventory().setContents(storage.get(player));
        player.getInventory().setArmorContents(armors.get(player));
        player.teleport(player.getWorld().getSpawnLocation());
    }

    public List<Player> queuePlayers(){
        List<Player> players = new ArrayList<>();
        for(String uuid : data.getStringList("queue")){
            if(uuid.equalsIgnoreCase("")) continue;
            Player player = Bukkit.getPlayer(UUID.fromString(uuid));
            if(player != null) players.add(player);
        }
        return players;
    }

    public void addPlayerToArena(Player player){
        data.set("players", Utils.addStringToList(data.getStringList("players"), player.getUniqueId().toString()));
    }

    public void removePlayerFromArena(Player player){
        data.set("players", Utils.removeStringFromList(data.getStringList("players"), player.getUniqueId().toString()));
        for(FastBoard board : boards) {
            if (board.getPlayer().equals(player) && !board.isDeleted()) board.delete();
        }
        player.getInventory().setContents(storage.get(player));
        player.getInventory().setArmorContents(armors.get(player));
        if(vehicles.containsKey(player)){
            vehicles.get(player).remove();
            vehicles.remove(player);
        }
        if(winners().contains(player)) {
            removeWinner(player);
        }else if(winners().size() == arenaPlayers().size() || arenaPlayers().isEmpty()){
            finish();
        }
        if(arenaPlayers().size() == 1){
            addWinner(arenaPlayers().get(0));
        }
    }

    public List<Player> arenaPlayers(){
        List<Player> players = new ArrayList<>();
        for(String uuid : data.getStringList("players")){
            if(uuid.equalsIgnoreCase("")) continue;
            Player player = Bukkit.getPlayer(UUID.fromString(uuid));
            if(player != null) players.add(player);
        }
        return players;
    }

    public void emptyPlayers(){
        data.set("players", Collections.singletonList(""));
    }

    public void startForQueue(){
        if(isStarted()) return;
        setStarted(true);
        new BukkitRunnable(){
            int i = 600;
            @Override
            public void run() {
                i--;
                if(queuePlayers().size() < getMinplayers()) {
                    setStarted(false);
                    this.cancel();
                    return;
                }
                if(i % 20 == 0) {
                    for (Player p : queuePlayers()) {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Game is starting in "+ (i/20) +" seconds..."));
                    }
                }
                if(i <= 0){
                    start();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }

    public void count(Player player){
        Location loc = player.getLocation();
        new BukkitRunnable(){
            int i = 100;
            @Override
            public void run() {
                i--;
                if(i <= 0){
                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            MythicBukkit.inst().getAPIHelper().castSkill(player, getCar(player));
                            times.put(player, System.currentTimeMillis());
                        }
                    }, 5);
                    this.cancel();
                    return;
                }
                player.teleport(loc);
                if(i % 20 == 0)
                    player.sendTitle(ChatColor.AQUA + "Game is starting in", ChatColor.DARK_AQUA + "" + (i/20) + " seconds...", 10, 20, 10);
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }

    public List<String> getLine(){
        List<String> line = new ArrayList<>();
        line.add(ChatColor.translateAlternateColorCodes('&', "&e&lTime:"));
        line.add(ChatColor.translateAlternateColorCodes('&', "&r &r &e" + ((300000 - (System.currentTimeMillis() - startTime)) / 1000) + " seconds"));
        line.add(ChatColor.translateAlternateColorCodes('&', "&r &r &r"));
        line.add(ChatColor.translateAlternateColorCodes('&', "&e&lDistances:"));
        for(Player p : arenaPlayers()){
            if(winners().contains(p)) {
                line.add(ChatColor.translateAlternateColorCodes('&', "&r &r &e" + p.getName() + ": &70"));
            }else{
                line.add(ChatColor.translateAlternateColorCodes('&', "&r &r &e" + p.getName() + ": &7" + (int) p.getLocation().distance(getEndLocation())));
            }
        }
        return line;
    }

    public double getSpeed(Player player){
        return speed.get(player);
    }

    public void setSpeed(Player player, double spd){
        if(spd < 0) return;
        if(getSpeedRunnable(player) != null && !getSpeedRunnable(player).isCancelled()) spd += 2;
        float exp = (float) (spd * (7/5));
        if(exp >= 1) exp = 0.99f;
        player.setExp(exp);
        speed.put(player, spd);
    }

    public boolean canSpeed(Player player){
        return canSpeed.get(player);
    }

    public void setCanSpeed(Player player, boolean b){
        canSpeed.put(player, b);
    }

    public void start(){
        setAvailable(false);
        startTime = System.currentTimeMillis();
        //create buffers
        for(Location loc : getBuffers()){
            for(Entity e : loc.getWorld().getNearbyEntities(loc, 0.1, 0.1, 0.1)){
                if(e instanceof EnderCrystal) e.remove();
            }
            EnderCrystal crystal = loc.getWorld().spawn(loc, EnderCrystal.class);
            crystal.setShowingBottom(false);
            crystal.setGravity(false);
            crystal.setInvulnerable(true);
            crystals.add(crystal);
        }

        int i = 0;
        for(Player p : queuePlayers()){
            //tp, add list, start counting
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();
            Location loc = getStartLocs().get(i);
            p.teleport(loc);
            canSpeed.put(p, true);
            checkPoints.put(p, loc);
            speed.put(p, 0d);
            i++;
            count(p);
            addPlayerToArena(p);
            FastBoard board = new FastBoard(p);
            board.updateTitle(ChatColor.translateAlternateColorCodes('&', "&aHAPPY &ePLACE&d&l - &2&lHAPPY KART RACING"));
            boards.add(board);
        }

        runnable = new BukkitRunnable(){
            @Override
            public void run() {
                runnable();
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
        data.set("queue", Collections.singletonList(""));

    }

    public void buff(Player player){
        ItemEffects.randomItem(player);
    }

    public void removeWinner(Player player){
        data.set("winners", Utils.removeStringFromList(data.getStringList("winners"), player.getUniqueId().toString()));
        vehicles.get(player).remove();
        vehicles.remove(player);
        player.setGameMode(GameMode.SURVIVAL);
        if(winners().size() >= 3 || winners().size() == arenaPlayers().size()){
            finish();
        }
    }

    public void addWinner(Player player){
        data.set("winners", Utils.addStringToList(data.getStringList("winners"), player.getUniqueId().toString()));
        vehicles.get(player).remove();
        vehicles.remove(player);
        player.setGameMode(GameMode.SPECTATOR);
        if(winners().size() >= 3 || winners().size() == arenaPlayers().size()){
            finish();
        }
    }

    public List<Player> winners(){
        List<Player> players = new ArrayList<>();
        for(String uuid : data.getStringList("winners")){
            if(uuid.equalsIgnoreCase("")) continue;
            Player player = Bukkit.getPlayer(UUID.fromString(uuid));
            if(player != null) players.add(player);
        }
        return players;
    }

    public void runnable(){
        if(System.currentTimeMillis() > startTime + 300000){
            finish();
            return;
        }
        for(Player p : arenaPlayers()){
            for(FastBoard board : boards){
                if(!board.isDeleted()) board.updateLines(getLine());
            }
            if(p.getGameMode().equals(GameMode.SPECTATOR)) continue;

            Location loc = p.getLocation();

            Entity vehicle = p.getVehicle();
            if(vehicle == null) {
                continue;
            }

            /*for(Entity e : p.getNearbyEntities(0.1, 0.1, 0.1)){
                if(MythicBukkit.inst().getAPIHelper().isMythicMob(e)) {
                    ActiveMob ab = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(e);
                    vehicles.put(p, ab);
                }
            }*/
            if(!vehicles.containsKey(p) || vehicles.get(p).isDead() || vehicles.get(p) == null) {
                if(System.currentTimeMillis() > startTime + 1500 && times.containsKey(p) && System.currentTimeMillis() > times.get(p) + 1500) {
                    MythicBukkit.inst().getAPIHelper().castSkill(p, getCar(p));
                    times.put(p, System.currentTimeMillis());
                }
                continue;
            }
            ActiveMob ab = vehicles.get(p);
            if(ab == null || ab.isDead()){
                if(System.currentTimeMillis() > startTime + 1500 && times.containsKey(p) && System.currentTimeMillis() > times.get(p) + 1000) {
                    MythicBukkit.inst().getAPIHelper().castSkill(p, getCar(p));
                    times.put(p, System.currentTimeMillis());
                }
                continue;
            }

            Vector vec = BukkitAdapter.adapt(ab.getEntity().getVelocity());
            vec.setX(0.0);
            vec.setZ(0.0);
            ab.getEntity().setVelocity(BukkitAdapter.adapt(vecs.get(p).clone().add(vec)));

            //check buffer
            for(Entity e : p.getNearbyEntities(1, 1, 1)){
                if(e instanceof EnderCrystal){
                    //add buff
                    buff(p);
                    e.remove();
                }
                if(e instanceof Item item){
                    if(item.getItemStack().getType().equals(Material.HORN_CORAL) && !Objects.requireNonNull(item.getItemStack().getItemMeta()).getDisplayName().contains(p.getName())){
                        slide(p);
                        item.remove();
                    }
                }
            }
            Block b = Utils.getTopBlock(loc, 4, 10);
            //check if falls
            if(b == null || (!Arrays.asList(ArenaMethods.whitelist).contains(b.getType()) && !b.getType().toString().toLowerCase().contains("carpet"))){
                Location loca = checkPoints.get(p);
                ((CraftEntity) BukkitAdapter.adapt(ab.getEntity())).getHandle().setPositionRotation(loca.getX(), loca.getY(), loca.getZ(), loca.getYaw(), loca.getPitch());
                p.sendMessage(ChatColor.RED + "You have teleported to the last checkpoint.");
                setSpeed(p, 0);
                continue;
            }

            //check point
            if(b.getType().equals(Material.WHITE_WOOL) && checkPoints.get(p).distance(b.getLocation()) > 10){
                checkPoints.put(p, b.getLocation().add(0.5, 4, 0.5));
                p.sendTitle(ChatColor.GREEN + "Checkpoint saved!", "", 5, 10, 5);
            }

            //finish location
            if(b.getType().equals(Material.BLACK_WOOL)){
                addWinner(p);
            }

            //push
            if(b.getType().equals(Material.MAGENTA_GLAZED_TERRACOTTA)){
                Directional directional = (Directional) b.getBlockData();
                Vector newVel;
                switch (directional.getFacing())
                {
                    case NORTH:
                        newVel = new Vector(0, 0, 0.1);
                        break;

                    case SOUTH:
                        newVel = new Vector(0, 0, -0.1);
                        break;

                    case EAST:
                        newVel = new Vector(-0.1, 0, 0);
                        break;

                    case WEST:
                        newVel = new Vector(0.1, 0, 0);
                        break;

                    default:
                        continue;
                }
                newVel.multiply(6);
                ab.getEntity().setVelocity(BukkitAdapter.adapt(BukkitAdapter.adapt(ab.getEntity().getVelocity()).add(newVel).multiply(3)));
            }
        }
    }

    public void finish(){
        setAvailable(true);
        runnable.cancel();
        Player winner = null;
        if(!winners().isEmpty()) winner = winners().get(0);
        GameFinishEvent event = new GameFinishEvent(winner, this);
        Bukkit.getPluginManager().callEvent(event);
        for(Player p : arenaPlayers()) {
            p.sendMessage(ChatColor.DARK_AQUA + "-------------------" + ChatColor.AQUA + "Winners" + ChatColor.DARK_AQUA + "-------------------");
            int i = 1;
            for(Player p1 : winners()){
                p.sendMessage(ChatColor.DARK_AQUA + "" + i + "-) " + ChatColor.GRAY + p1.getName());
                i++;
            }
            p.getInventory().setContents(storage.get(p));
            p.getInventory().setArmorContents(armors.get(p));
            p.teleport(p.getWorld().getSpawnLocation());
            p.setGameMode(GameMode.SURVIVAL);
        }
        emptyPlayers();

        //remove buffers
        for(EnderCrystal crystal : crystals){
            crystal.remove();
        }
        crystals.clear();

        for(FastBoard board : boards){
            if(!board.isDeleted()) board.delete();
        }
        boards.clear();

        for(ActiveMob e : vehicles.values()){
            e.remove();
        }
        data.set("winners", Arrays.asList(""));
        vehicles.clear();
    }

}

