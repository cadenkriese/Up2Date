package com.gamerking195.dev.up2date.util.item;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author myles
 * @since 16/06/2017
 */
public class ItemStackBuilder {

    private ItemStack item;
    private ItemMeta itemMeta;

    public ItemStackBuilder(Material mat) {
        this.item = new ItemStack(mat);
        this.itemMeta = item.getItemMeta();
    }

    public ItemStackBuilder(Material mat, short damage) {
        this.item = new ItemStack(mat, 1, damage);
        this.itemMeta = item.getItemMeta();
    }

    public ItemStackBuilder(ItemStack item) {
        this.item = item;
        this.itemMeta = item.getItemMeta();
    }

    public ItemStackBuilder(Material mat, byte data) {
        this.item = new ItemStack(mat, 1, (short) 0, data);
        this.itemMeta = item.getItemMeta();
    }

    public ItemStackBuilder(Material mat, short damage, byte data) {
        this.item = new ItemStack(mat, 1, damage, data);
        this.itemMeta = item.getItemMeta();
    }

    public ItemStackBuilder setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemStackBuilder setDurability(short durability) {
        //TODO write compatibility for 1.13

        item.setDurability(durability);
        return this;
    }

    public ItemStackBuilder addEnchant(Enchantment e, int level) {
        itemMeta.addEnchant(e, level, true);
        return this;
    }

    public ItemStackBuilder removeEnchant(Enchantment e) {
        itemMeta.removeEnchant(e);
        return this;
    }

    public ItemStackBuilder addItemFlags(ItemFlag... list) {
        itemMeta.addItemFlags(list);
        return this;
    }

    public ItemStackBuilder removeItemFlags(ItemFlag... list) {
        itemMeta.removeItemFlags(list);
        return this;
    }

    public ItemStackBuilder setName(String name) {
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        return this;
    }

    public ItemStackBuilder setLore(String... lore) {
        List<String> list = new ArrayList<>();

        for (String string : lore)
            list.add(ChatColor.translateAlternateColorCodes('&', string));

        itemMeta.setLore(list);
        return this;
    }

    public ItemStackBuilder setLore(List<String> lore) {
        List<String> list = new ArrayList<>();

        for (String string : lore)
            list.add(ChatColor.translateAlternateColorCodes('&', string));

        itemMeta.setLore(list);
        return this;
    }

    public ItemStackBuilder setColor(Color color) {
        LeatherArmorMeta meta = (LeatherArmorMeta) itemMeta;
        meta.setColor(color);
        return this;
    }

    public ItemStackBuilder setSkull(String name) {
        SkullMeta meta = (SkullMeta) itemMeta;
        meta.setOwner(name);
        return this;
    }

    public ItemStackBuilder setUnbreakable(boolean unbreakable) {
        this.itemMeta.spigot().setUnbreakable(unbreakable);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(itemMeta);
        return item;
    }

    @Override
    public String toString() {
        return "Type=" + item.getType().name() + ", Amount=" + item.getAmount() + ", Durability=" + item.getDurability() + ", Enchantments=" + itemMeta.getEnchants() + ", ItemFlags=" + itemMeta.getItemFlags() + ", Name=" + itemMeta.getDisplayName() + ", Lore=" + itemMeta.getLore();
    }
}
