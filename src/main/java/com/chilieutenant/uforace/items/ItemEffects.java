package com.chilieutenant.uforace.items;

import com.chilieutenant.uforace.Main;
import com.chilieutenant.uforace.arena.Arena;
import com.chilieutenant.uforace.arena.ArenaMethods;
import com.chilieutenant.uforace.utils.ParticleEffect;
import com.chilieutenant.uforace.utils.Utils;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ItemEffects {

    public static void slide(Player player){
        Arena arena = ArenaMethods.getArena(player);
        if(arena == null) return;

        ActiveMob vehicle = arena.getVehicle(player);

        arena.setCanSpeed(player, false);
        Vector vec = player.getLocation().getDirection();
        vec.rotateAroundY(80);
        arena.setSpeed(player, 0);
        long time = System.currentTimeMillis();
        new BukkitRunnable(){
            @Override
            public void run() {
                if(System.currentTimeMillis() < time + 1500){
                    vehicle.getEntity().setVelocity(BukkitAdapter.adapt(vec.normalize().multiply(0.3)));
                }
                if(System.currentTimeMillis() > time + 2500){
                    arena.setCanSpeed(player, true);
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);

    }

    public static void throwHead(Player player){
        Location loc = player.getLocation();
        ArmorStand as = loc.getWorld().spawn(loc, ArmorStand.class);
        as.setVisible(false);
        as.getEquipment().setHelmet(Items.SADBOT_HEAD.getItem());
        as.setVelocity(player.getLocation().getDirection().normalize().multiply(2));

        long time = System.currentTimeMillis();
        new BukkitRunnable(){
            @Override
            public void run() {
                if((as.isOnGround() && System.currentTimeMillis() > time + 1500) || System.currentTimeMillis() > time + 5000){
                    as.remove();
                    this.cancel();
                    return;
                }

                for(Entity e : Utils.getEntitiesAroundPoint(as.getLocation(), 2)){
                    if(e instanceof Player target && e.getUniqueId() != player.getUniqueId()){
                        Arena arena = ArenaMethods.getArena(target);
                        if(arena == null) continue;

                        arena.setSpeed(target, 0);
                        target.damage(0.0005, player);
                        as.remove();
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }

    public static void speedBoost(Player player){
        player.sendTitle(ChatColor.YELLOW + "SPEED BOOST!", "", 5, 10, 5);
        Arena arena = ArenaMethods.getArena(player);
        if(arena == null) return;

        if(arena.getSpeedRunnable(player) != null && !arena.getSpeedRunnable(player).isCancelled()) return;

        BossBar bar = Bukkit.createBossBar(ChatColor.YELLOW + "Speed Boost", BarColor.YELLOW, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);
        bar.addPlayer(player);
        arena.setSpeedRunnable(player, new BukkitRunnable(){
            int i = 200;
            @Override
            public void run() {
                i--;
                bar.setProgress(((double) i)/200);

                if(i <= 0){
                    bar.removeAll();
                    arena.setSpeed(player, 5);
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1));
    }

    public static void superHorn(Player player){
        Location loc = player.getLocation();
        ParticleEffect.EXPLOSION_LARGE.display(loc, 3, 1, 1, 1);
        for(Entity e : Utils.getEntitiesAroundPoint(loc, 2)){
            if(e instanceof Player target && e.getUniqueId() != player.getUniqueId()){
                Arena arena = ArenaMethods.getArena(target);
                if(arena == null) continue;

                ActiveMob ab = arena.getVehicle(target);
                Vector vec = target.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(2);
                vec.setY(0.2);
                ab.getEntity().setVelocity(BukkitAdapter.adapt(vec));
            }
        }
    }

    public static void randomItem(Player player){
        Arena arena = ArenaMethods.getArena(player);
        if(arena == null) return;

        if(arena.getRandomRunnable(player) != null && !arena.getRandomRunnable(player).isCancelled()) return;

        arena.setRandomRunnable(player, new BukkitRunnable(){
            int i = 0;
            @Override
            public void run() {
                i++;
                player.getInventory().setItem(0, Arrays.asList(Items.values()).get((Items.values().length - 1) % i).getItem());
                if(i > 6){
                    //give random item
                    List<Items> givenList = Arrays.asList(Items.values());
                    Random rand = new Random();
                    Items randomElement = givenList.get(rand.nextInt(givenList.size()));
                    if(randomElement.equals(Items.SPEED_BOOST)){
                        speedBoost(player);
                    }else{
                        player.getInventory().setItem(0, randomElement.getItem());
                        player.sendMessage(ChatColor.GRAY + "Press Q to use the item!");
                    }
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 10));
    }
}
