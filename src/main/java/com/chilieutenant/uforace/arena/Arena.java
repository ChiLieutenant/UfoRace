package com.chilieutenant.uforace.arena;

import com.chilieutenant.uforace.Main;
import com.chilieutenant.uforace.events.GameFinishEvent;
import com.chilieutenant.uforace.items.ItemEffects;
import com.chilieutenant.uforace.items.Items;
import com.chilieutenant.uforace.utils.CenteredText;
import com.chilieutenant.uforace.utils.NFTMethods;
import com.chilieutenant.uforace.utils.Utils;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.mount.controller.MountController;
import com.ticxo.modelengine.api.model.mount.handler.IMountHandler;
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
import org.bukkit.attribute.Attribute;
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
    private boolean gameStarted;
    private List<EnderCrystal> crystals = new ArrayList<>();
    private HashMap<Player, Long> times = new HashMap<>();
    private HashMap<Player, Location> checkPoints = new HashMap<>();
    //private HashMap<Player, ActiveMob> vehicles = new HashMap<>();
    private HashMap<Player, Entity> vehcs = new HashMap<>();
    private HashMap<Player, Vector> vecs = new HashMap<>();
    private HashMap<Player, ModeledEntity> me = new HashMap<>();
    private HashMap<Player, Boolean> canSpeed = new HashMap<>();
    private HashMap<Player, Boolean> isSlow = new HashMap<>();
    private HashMap<Player, Double> speed = new HashMap<>();
    private HashMap<Player, Double> maxspeed = new HashMap<>();
    private List<FastBoard> boards = new ArrayList<>();
    private HashMap<Player, String> cars = new HashMap<>();
    private HashMap<Player, BukkitTask> speedRunnable = new HashMap<>();
    private HashMap<Player, BukkitTask> randomRunnable = new HashMap<>();
    private HashMap<Player, List<Location>> locations = new HashMap<>();
    @Getter private long startTime;
    private BukkitTask runnable;
    public static HashMap<Player, ItemStack[]> storage = new HashMap<>();
    public static HashMap<Player, ItemStack[]> armors = new HashMap<>();

    public Arena(String name){
        data = ArenaMethods.getArenaData(name);
        data.set("available", true);
        data.set("players", Collections.singletonList(""));
        data.set("queue", Collections.singletonList(""));
        gameStarted = false;
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

    public ModeledEntity getME(Player player){
        return me.get(player);
    }

    public void setCar(Player player, String car){
        cars.put(player, car);
    }

    public String getCar(Player player){
        return cars.get(player);
    }

    public Entity getVehicle(Player player){
        return vehcs.get(player);
    }

    public void setVehicle(Player player, Entity vehicle){
        vehcs.put(player, vehicle);
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

    public Location getMainLobbyLocation(){
        return Utils.getLocationString(data.getString("mainlobby"));
    }

    public void setMainLobbyLocation(Location location){
        data.set("mainlobby", Utils.getStringLocation(location));
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
        player.getInventory().clear();
        player.getInventory().setItem(0, new ItemStack(Material.COMPASS));
        player.getInventory().setItem(2, new ItemStack(Material.DIAMOND));
        player.getInventory().setItem(6, Utils.itemBuilder(Material.COMPASS, "&aVehicle selection"));
        player.getInventory().setItem(8, Utils.getItem(Material.RED_BED, ChatColor.RED + "Leave"));
        player.teleport(getLobbyLocation());
        setCar(player, NFTMethods.getCar(player));
    }

    public void removePlayerFromQueue(Player player){
        data.set("queue", Utils.removeStringFromList(data.getStringList("queue"), player.getUniqueId().toString()));
        player.getInventory().setContents(storage.get(player));
        player.getInventory().setArmorContents(armors.get(player));
        player.teleport(getMainLobbyLocation());
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
        if(vehcs.containsKey(player)){
            vehcs.get(player).remove();
            vehcs.remove(player);
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

    public void createCar(Player player){
        Horse horse = player.getWorld().spawn(player.getLocation().add(0, 1, 0), Horse.class);
        horse.setInvulnerable(true);
        horse.setInvisible(true);
        horse.setTamed(true);
        horse.setOwner(player);
        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);

        ActiveModel model = ModelEngineAPI.api.getModelManager().createActiveModel(getCar(player));
        model.setDamageTint(false);
        model.setClamp(0);

        ModeledEntity modeledEntity = ModelEngineAPI.api.getModelManager().createModeledEntity(horse);

        modeledEntity.addActiveModel(model);
        modeledEntity.detectPlayers();
        modeledEntity.setInvisible(true);
        modeledEntity.getMountHandler().setSteerable(true);

        IMountHandler mountHandler = modeledEntity.getMountHandler();
        MountController mountController = ModelEngineAPI.api.getControllerManager().createController("force_walking");
        mountHandler.removePassenger(player);
        mountHandler.setCanDamageMount(mountHandler.getDriver(), true);
        mountHandler.setDriver(null);
        mountHandler.setDriver(player, mountController);
        mountHandler.cannotDamageMount(player);

        //horse.addPassenger(player);
        me.put(player, modeledEntity);
        setVehicle(player, horse);
    }

    public void startForQueue(){
        if(isStarted()) return;
        setStarted(true);
        new BukkitRunnable(){
            int i = 1200;
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
        createCar(player);
        times.put(player, System.currentTimeMillis());
        new BukkitRunnable(){
            int i = 100;
            @Override
            public void run() {
                i--;
                if(i <= 0){
                    if(!gameStarted) {
                        gameStarted = true;
                    }
                    this.cancel();
                    return;
                }
                if(i % 20 == 0)
                    player.sendTitle(ChatColor.AQUA + "Game is starting in", ChatColor.DARK_AQUA + "" + (i/20) + " seconds...", 10, 20, 10);
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }

    public List<String> getLine(){
        List<String> line = new ArrayList<>();
        line.add(ChatColor.translateAlternateColorCodes('&', "&e&lTime:"));
        line.add(ChatColor.translateAlternateColorCodes('&', "&r &r &e" + Utils.getMMSS(((300000 - (System.currentTimeMillis() - startTime)) / 1000)) + " seconds"));
        line.add(ChatColor.translateAlternateColorCodes('&', "&r &r &r"));
        line.add(ChatColor.translateAlternateColorCodes('&', "&e&lDistances:"));
        for(Player p : arenaPlayers()){
            if(winners().contains(p)) {
                line.add(ChatColor.translateAlternateColorCodes('&', "&r &r &e" + p.getName() + ": &70"));
            }else{
                line.add(ChatColor.translateAlternateColorCodes('&', "&r &r &e" + p.getName() + ": &7" + (int) p.getLocation().distance(getEndLocation()) * ((Math.abs(p.getLocation().getY() - getEndLocation().getY()) / 5) + 1)));
            }
        }
        return line;
    }

    public double getSpeed(Player player){
        if(isSlow.get(player)){
            double spd = speed.get(player);
            spd -= 0.2;
            if(spd < 0) spd = 0.1;
            return spd;
        }
        return speed.get(player);
    }

    public double getMaxSpeed(Player player){
        return maxspeed.get(player);
    }

    public void setMaxSpeed(Player player, double spd){
        maxspeed.put(player, spd);
    }

    public void forceSetSpeed(Player player, double spd){
        float exp = (float) (spd * (7/2));
        if(exp >= 1) exp = 0.99f;
        player.setExp(exp);
        speed.put(player, spd);
    }

    public void setSpeed(Player player, double spd){
        if(spd < 0 || !gameStarted) return;
        if(getSpeed(player) > spd && spd < 0.2) spd = 0.2;
        float exp = (float) (spd * (7/2));
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
            isSlow.put(p, false);
            canSpeed.put(p, true);
            checkPoints.put(p, loc);
            maxspeed.put(p, 1.6d);
            speed.put(p, 0.3d);
            vecs.put(p, p.getLocation().getDirection());
            locations.put(p, new ArrayList<>());
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
        vehcs.get(player).remove();
        vehcs.remove(player);
        player.setGameMode(GameMode.SURVIVAL);
        if(winners().size() >= 3 || winners().size() == arenaPlayers().size()){
            finish();
        }
    }

    public void addWinner(Player player){
        data.set("winners", Utils.addStringToList(data.getStringList("winners"), player.getUniqueId().toString()));
        vehcs.get(player).remove();
        player.sendTitle(ChatColor.GREEN + "Congratulation!", ChatColor.GRAY + "Spectator Mode", 10, 20, 10);
        player.setGameMode(GameMode.SPECTATOR);
        if(winners().size() == arenaPlayers().size()){
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
        if(!gameStarted) return;
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

            Entity vehicle = getVehicle(p);

            Vector vec = vehicle.getVelocity();
            vec.setX(0.0);
            vec.setZ(0.0);
            vehicle.setVelocity(vecs.get(p).clone().add(vec));

            //check buffer
            for(Entity e : p.getNearbyEntities(1, 1, 1)){
                if(e instanceof EnderCrystal){
                    //add buff
                    buff(p);
                    e.remove();
                }
                if(e instanceof Item item){
                    if(item.getItemStack().getType().equals(Material.HORN_CORAL) && !item.getCustomName().contains(p.getName())){
                        slide(p);
                        item.remove();
                    }
                }
            }
            Block b = Utils.getTopBlock(loc, 4, 10);
            //check if falls
            if(b == null || (!Arrays.asList(ArenaMethods.whitelist).contains(b.getType()) && !b.getType().toString().toLowerCase().contains("carpet"))){
                Location loca;
                if(locations.get(p).size() > 6){
                    loca = locations.get(p).get(locations.get(p).size() - 5);
                }else{
                    loca = locations.get(p).get(locations.get(p).size() - 1);
                }
                ((CraftEntity) vehicle).getHandle().setPositionRotation(loca.getX(), loca.getY(), loca.getZ(), loca.getYaw(), loca.getPitch());
                forceSetSpeed(p, 0);
                setCanSpeed(p, false);
                p.sendTitle(ChatColor.RED + "!!!!!!!!", "", 10, 30, 10);
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    setCanSpeed(p, true);
                }, 60);
                continue;
            }else{
                List<Location> locs = locations.get(p);
                locs.add(p.getLocation());
                locations.put(p, locs);
            }

            if(b.getType().equals(Material.BLACK_CARPET) || b .getType().equals(Material.COAL_BLOCK)){
                if(!isSlow.get(p)) isSlow.put(p, true);
            }else{
                if(isSlow.get(p)) isSlow.put(p, false);
            }

            //check point
            if(b.getType().equals(Material.WHITE_WOOL) && checkPoints.get(p).distance(b.getLocation()) > 10){
                checkPoints.put(p, b.getLocation().add(0.5, 4, 0.5));
                p.sendTitle(ChatColor.GREEN + "Checkpoint saved!", "", 5, 10, 5);
            }

            //finish location
            if(b.getType().equals(Material.BLACK_WOOL) && p.getLocation().distance(getEndLocation()) < 16){
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
                vehicle.setVelocity(vehicle.getVelocity().add(newVel).multiply(3));
            }
        }
    }

    public void finish(){
        setAvailable(true);
        gameStarted = false;
        runnable.cancel();
        Player winner = null;

        for(Entity e : vehcs.values()){
            e.remove();
        }

        if(!winners().isEmpty()) winner = winners().get(0);
        GameFinishEvent event = new GameFinishEvent(winner, this);
        Bukkit.getPluginManager().callEvent(event);
        for(Player p : arenaPlayers()) {
            vehcs.get(p).remove();
            int i = 1;
            CenteredText.sendCenteredMessage(p, "&a&m                                                                    ");
            CenteredText.sendCenteredMessage(p, "&l&eWINNERS");
            for(Player p1 : winners()){
                CenteredText.sendCenteredMessage(p, "&7" + i + "-) &b" + p1.getName());
                i++;
            }
            CenteredText.sendCenteredMessage(p, "&a&m                                                                    ");
            p.getInventory().setContents(storage.get(p));
            p.getInventory().setArmorContents(armors.get(p));
            p.teleport(getMainLobbyLocation());
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

        data.set("winners", Arrays.asList(""));
        vehcs.clear();
    }

}

