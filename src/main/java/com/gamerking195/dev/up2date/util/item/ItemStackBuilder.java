package com.gamerking195.dev.up2date.util.item;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemStackBuilder {

    private String type = "AIR";
    private int amount = 1;
    private ItemMeta itemMeta;

    private static HashMap<String, Integer> colorConversions = new HashMap<String, Integer>() {{
        put("ORANGE", 1);
        put("MAGENTA", 2);
        put("LIGHT_BLUE", 3);
        put("YELLOW", 4);
        put("LIME", 5);
        put("PINK", 6);
        put("GRAY", 7);
        put("LIGHT_GRAY", 8);
        put("CYAN", 9);
        put("PURPLE", 10);
        put("BLUE", 11);
        put("BROWN", 12);
        put("GREEN", 13);
        put("RED", 14);
        put("BLACK", 15);

//        put(1, "ORANGE");
//        put(2, "MAGENTA");
//        put(3, "LIGHT_BLUE");
//        put(4, "YELLOW");
//        put(5, "LIME");
//        put(6, "PINK");
//        put(7, "GRAY");
//        put(8, "LIGHT_GRAY");
//        put(9, "CYAN");
//        put(10, "PURPLE");
//        put(11, "BLUE");
//        put(12, "BROWN");
//        put(13, "GREEN");
//        put(14, "RED");
//        put(15, "BLACK");
    }};

    public ItemStackBuilder(Material type) {
        this.itemMeta = new ItemStack(type).getItemMeta();
        this.type = type.name();
    }

    public ItemStackBuilder(ItemStack item) {
        this.itemMeta = item.getItemMeta();
        this.type = item.getType().name();
        this.amount = item.getAmount();
    }

    public ItemStackBuilder(Material type, int amount) {
        this.itemMeta = new ItemStack(type, amount).getItemMeta();
        this.type = type.name();
        this.amount = amount;
    }

    public ItemStackBuilder(Material type, short damage) {
        this.itemMeta = new ItemStack(type).getItemMeta();
        this.type = type.name();
        setDurability(damage);
    }

    public ItemStackBuilder(Material type, int amount, int damage) {
        this.itemMeta = new ItemStack(type, amount).getItemMeta();
        this.type = type.name();
        this.amount = amount;
        setDurability(damage);
    }

    public ItemStackBuilder setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemStackBuilder setDurability(int durability) {
        if (itemMeta instanceof Damageable)
            ((Damageable) itemMeta).setDamage(durability);

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
        this.itemMeta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemStack build() {
        //if the item type contains any colors.
        short durability = 0;
        if (!Bukkit.getBukkitVersion().contains("1.13") && !Bukkit.getBukkitVersion().contains("1.14")) {
            type = type.replace("CONCRETE", "STAINED_CLAY");

            if (colorConversions.keySet().parallelStream().anyMatch(type::contains)) {
                String matchingColor = colorConversions.keySet().parallelStream().filter(type::contains).findFirst().orElse(null);
                if (matchingColor != null) {
                    type = type.replace(matchingColor + "_", "");
                    durability = colorConversions.get(matchingColor).shortValue();
                }
            }
        }

        ItemStack item = new ItemStack(Material.valueOf(type), amount);
        item.setItemMeta(itemMeta);
        if (durability != 0)
            item.setDurability(durability);
        return item;
    }

    @Override
    public String toString() {
        return "Type=" + type + ", Amount=" + amount + ", Durability=" + (itemMeta instanceof Damageable ? ((Damageable) itemMeta).getDamage() : 0) + ", Enchantments=" + itemMeta.getEnchants() + ", ItemFlags=" + itemMeta.getItemFlags() + ", Name=" + itemMeta.getDisplayName() + ", Lore=" + itemMeta.getLore();
    }
}
