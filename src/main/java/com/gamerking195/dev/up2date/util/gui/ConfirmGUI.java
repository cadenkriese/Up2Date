package com.gamerking195.dev.up2date.util.gui;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author myles
 * @since 16/06/2017
 */
public class ConfirmGUI extends GUI {

    private boolean confirmed = false;

    private String title;
    private List<String> description;

    private Runnable confirmAction;
    private Runnable cancelAction;

    public ConfirmGUI(String title, Runnable confirmAction) {
        this(title, ImmutableList.of(), confirmAction, () -> {
        });
    }

    public ConfirmGUI(String title, List<String> description, Runnable confirmAction) {
        this(title, description, confirmAction, () -> {
        });
    }

    public ConfirmGUI(String title, Runnable confirmAction, Runnable cancelAction) {
        this(title, ImmutableList.of(), confirmAction, cancelAction);
    }

    public ConfirmGUI(String title, Runnable confirmAction, Runnable cancelAction, String... lore) {
        this(title, ImmutableList.of(), confirmAction, cancelAction);

        List<String> strings = new ArrayList<>();

        for (String string : lore)
            strings.add(ChatColor.translateAlternateColorCodes('&', string));

        this.description = strings;
    }
    public ConfirmGUI(String title, List<String> description, Runnable confirmAction, Runnable cancelAction) {
        super(ChatColor.translateAlternateColorCodes('&', "&d&lU&5&l2&d&lD &8- &dConfirm"), 27);
        this.title = ChatColor.translateAlternateColorCodes('&', title);
        this.description = description;
        this.confirmAction = confirmAction;
        this.cancelAction = cancelAction;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event) {
        if (Arrays.asList(0, 1, 2, 9, 10, 11, 18, 19, 20).contains(event.getRawSlot())) {
            confirmed = true;
            confirmAction.run();
        }

        if (Arrays.asList(6, 7, 8, 15, 16, 17, 24, 25, 26).contains(event.getRawSlot())) {
            confirmed = false;
            cancelAction.run();
        }
    }

//    @Override
//    protected void onPlayerCloseInv() {
//        if (!confirmed) {
//            cancelAction.run();
//        }
//    }

    @Override
    protected void populate() {
        for (int slot : new int[]{0, 1, 2, 9, 10, 11, 18, 19, 20}) {
            ItemStack icon = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 13);
            ItemMeta iconMeta = icon.getItemMeta();
            iconMeta.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD + "CONFIRM");
            icon.setItemMeta(iconMeta);
            this.inventory.setItem(slot, icon);
        }

        for (int slot : new int[]{6, 7, 8, 15, 16, 17, 24, 25, 26}) {
            ItemStack icon = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
            ItemMeta iconMeta = icon.getItemMeta();
            iconMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "CANCEL");
            icon.setItemMeta(iconMeta);
            this.inventory.setItem(slot, icon);
        }

        ItemStack questionIcon = new ItemStack(Material.SKULL_ITEM, 1, (short) 0, (byte) 3);
        Bukkit.getUnsafe().modifyItemStack(questionIcon, "{SkullOwner:{Id:\"808ac216-799a-4d42-bd68-7c9f0c1e89d1\",Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2FhYjI3Mjg0MGQ3OTBjMmVkMmJlNWM4NjAyODlmOTVkODhlMzE2YjY1YzQ1NmZmNmEzNTE4MGQyZTViZmY2In19fQ==\"}]}}}");
        ItemMeta questionIconMeta = questionIcon.getItemMeta();
        questionIconMeta.setDisplayName(ChatColor.WHITE + title);
        questionIconMeta.setLore(Stream.concat(Stream.of(""), description.stream().map(line -> ChatColor.WHITE + line)).collect(Collectors.toList()));
        questionIcon.setItemMeta(questionIconMeta);
        this.inventory.setItem(13, questionIcon);
    }
}
