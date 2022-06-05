package com.chilieutenant.uforace.NFTSelection;

import com.chilieutenant.uforace.Main;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class NFTItem {

    @Getter private int slot = 0;
    @Getter private ItemStack item;
    @Getter private String name;

    public NFTItem(String name, int slot, ItemStack item){
        this.name = name;
        this.slot = slot;
        this.item = item;

        Main.getItems().add(this);
    }
}
