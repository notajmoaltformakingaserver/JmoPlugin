package me.jmo.jmoplugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class UnholyItems {

    public static ItemStack unholyBeef() {
        ItemStack beef = new ItemStack(Material.BEEF);
        ItemMeta meta = beef.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_RED + "Unholy Beef");
        meta.setLore(List.of(ChatColor.GRAY + "It pulses with dark energy"));
        beef.setItemMeta(meta);
        return beef;
    }
}
