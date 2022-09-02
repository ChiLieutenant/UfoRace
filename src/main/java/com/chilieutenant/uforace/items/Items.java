package com.chilieutenant.uforace.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum Items {

    BANANA_PEEL(new ItemStack(Material.HORN_CORAL), ChatColor.YELLOW + "Banana Peel"),
    SADBOT_HEAD(new ItemStack(Material.PLAYER_HEAD), ChatColor.GRAY + "Sad Bot Head"),
    SPEED_BOOST(new ItemStack(Material.GREEN_STAINED_GLASS_PANE), ChatColor.GREEN + "Speed Boost"),
    SUPER_HORN(new ItemStack(Material.CONDUIT), ChatColor.AQUA + "Super Horn"),
    BOMB(new ItemStack(Material.BLACK_CONCRETE), ChatColor.BLACK + "Bomb");


    ItemStack item;

    Items(ItemStack item, String display_name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(display_name);
        this.item = item;
        this.item.setItemMeta(meta);
    }

    public ItemStack getItem()
    {
        return item;
    }
}
