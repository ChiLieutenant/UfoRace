package com.chilieutenant.uforace.utils;

import com.chilieutenant.uforace.Main;
import de.leonhard.storage.Yaml;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Utils {

    public static List<Entity> getEntitiesAroundPoint(final Location location, final double radius) {
        return new ArrayList<>(location.getWorld().getNearbyEntities(location, radius, radius, radius, entity -> !(entity.isDead() || (entity instanceof Player && ((Player) entity).getGameMode().equals(GameMode.SPECTATOR)))));
    }

    public static Method[] methods = ((Supplier<Method[]>) () -> {
        try {
            Method getHandle = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftEntity").getDeclaredMethod("getHandle");
            return new Method[] {
                    getHandle, getHandle.getReturnType().getDeclaredMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class)
            };
        } catch (Exception ex) {
            return null;
        }
    }).get();

    static public String getStringLocation(final Location l) {
        if (l == null) {
            return "";
        }
        return l.getWorld().getName() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ() + ":" + l.getYaw() + ":" + l.getPitch();
    }

    public static ItemStack itemBuilder(Material mat, String name){
        ItemStack item = new ItemStack(mat);
        ItemMeta itemmeta = item.getItemMeta();
        itemmeta.setDisplayName(replaceColorCodes(name));
        item.setItemMeta(itemmeta);

        return item;
    }

    static public Location getLocationString(final String s) {
        if (s == null || s.trim() == "") {
            return null;
        }
        final String[] parts = s.split(":");
        if (parts.length == 6) {
            final World w = Bukkit.getServer().getWorld(parts[0]);
            final int x = Integer.parseInt(parts[1]);
            final int y = Integer.parseInt(parts[2]);
            final int z = Integer.parseInt(parts[3]);
            final float yaw = Float.parseFloat(parts[4]);
            final float pitch = Float.parseFloat(parts[5]);
            return new Location(w, x, y, z, yaw, pitch);
        }
        return null;
    }

    public static List<String> addStringToList(List<String> lista, String s){
        List<String> list = new ArrayList<>(lista);
        list.add(s);
        list.remove("");
        return list;
    }

    public static List<String> removeStringFromList(List<String> lista, String s){
        List<String> list = new ArrayList<>(lista);
        list.remove(s);
        return list;
    }

    public static Location getRightSide(final Location location, final double distance) {
        final float angle = location.getYaw();
        return location.clone().subtract(new Vector(Math.cos(angle), 0, Math.sin(angle)).normalize().multiply(distance));
    }

    public static Location getLeftSide(final Location location, final double distance) {
        final float angle = location.getYaw();
        return location.clone().add(new Vector(Math.cos(angle), 0, Math.sin(angle)).normalize().multiply(distance));
    }

    public static boolean isAir(final Material material) {
        return material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR;
    }

    public static Block getTopBlock(final Location loc, final int positiveY, final int negativeY) {
        Block blockHolder = loc.getBlock();
        int y = 0;
        // Only one of these while statements will go
        while (!isAir(blockHolder.getType()) && Math.abs(y) < Math.abs(positiveY)) {
            y++;
            final Block tempBlock = loc.clone().add(0, y, 0).getBlock();
            if (isAir(tempBlock.getType())) {
                return blockHolder;
            }
            blockHolder = tempBlock;
        }

        while (isAir(blockHolder.getType()) && Math.abs(y) < Math.abs(negativeY)) {
            y--;
            blockHolder = loc.clone().add(0, y, 0).getBlock();
            if (!isAir(blockHolder.getType())) {
                return blockHolder;
            }
        }
        return blockHolder;
    }

    public static ItemStack getItem(Material material, String name){
        ItemStack item = new ItemStack(material);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        item.setItemMeta(im);
        return item;
    }

    public static void addPermission(Player player, String permission) {
        // Load, modify, then save
        Main.getLuckperms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            // Add the permission
            user.data().add(Node.builder(permission).build());
        });
    }

    public static void removePermission(Player player, String permission){
        Main.getLuckperms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            // Add the permission
            user.data().remove(Node.builder(permission).build());
        });
    }

    public static boolean hasPermission(Player player, String permission) {
        User user = Main.getLuckperms().getUserManager().getUser(player.getUniqueId());
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    public static String replaceColorCodes(String text){
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> replaceColors(List<String> l){
        List<String> list = new ArrayList<>();

        for(String string : l){
            list.add(replaceColorCodes(string));
        }

        return list;
    }

    public static ItemStack itemBuilder(String nameInTheConfig, Yaml config){
        ItemStack item = new ItemStack(Material.MINECART);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(replaceColorCodes(config.getString("vehs." + nameInTheConfig+".name")));
        itemMeta.setLore(replaceColors(config.getStringList("vehs." + nameInTheConfig+".lore")));

        item.setItemMeta(itemMeta);
        return item;
    }
}
