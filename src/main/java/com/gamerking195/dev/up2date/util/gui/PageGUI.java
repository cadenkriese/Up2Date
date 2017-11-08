package com.gamerking195.dev.up2date.util.gui;

import com.gamerking195.dev.up2date.util.item.SkullUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author myles
 * @since 16/06/2017
 */
public abstract class PageGUI extends GUI {

    private int page;
    private int headerRows;
    private final int internalSize;

    public PageGUI(String title, int size) {
        super(title, size);
        this.internalSize = getInvSizeForCount(size - 9);
    }

    protected abstract List<ItemStack> getIcons();

    protected abstract void onPlayerClickIcon(InventoryClickEvent event);

    protected abstract void populateSpecial();

    public void setHeaderRows(int headerRows) {
        this.headerRows = headerRows;
    }

    @Override
    protected final void onPlayerClick(InventoryClickEvent event) {
        List<ItemStack> items = getIcons();
        if (items == null) {
            items = new ArrayList<>();
        }
        int pageSize = internalSize - (headerRows * 9);
        int pages = (items.size() / pageSize);
        if (items.size() % pageSize > 0) pages++;

        if (event.getRawSlot() == internalSize + 3 && page > 0) {
            ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            page--;
            this.repopulate();
            return;
        }

        if (event.getRawSlot() == internalSize + 5 && (page + 1 < pages)) {
            ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            page++;
            this.repopulate();
            return;
        }

        this.onPlayerClickIcon(event);
    }

    @Override
    protected final void populate() {
        List<ItemStack> items = getIcons();
        if (items == null) {
            items = new ArrayList<>();
        }
        int pageSize = internalSize - (headerRows * 9);
        int pages = (items.size() / pageSize);
        if (items.size() % pageSize > 0) pages++;

        if (page > pages) {
            this.page--;
            this.repopulate();
            return;
        }

        int slot = 0;
        for (int i = (page * pageSize); i < items.size(); i++) {
            if (slot > pageSize + (headerRows * 9) - 1) break;

            /*if (slot == 17 || slot == 26) {
                slot = slot + 2;
            }*/

            this.inventory.setItem(slot++, items.get(i));
        }

        if (page > 0) {
            this.inventory.setItem(internalSize + 3, SkullUtil.makeSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzM3NjQ4YWU3YTU2NGE1Mjg3NzkyYjA1ZmFjNzljNmI2YmQ0N2Y2MTZhNTU5Y2U4YjU0M2U2OTQ3MjM1YmNlIn19fQ==", ChatColor.translateAlternateColorCodes('&', "&4&l&m«---&r &cPREVIOUS"), null));
        }

        if (page + 1 < pages) {
            this.inventory.setItem(internalSize + 5, SkullUtil.makeSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWE0ZjY4YzhmYjI3OWU1MGFiNzg2ZjlmYTU0Yzg4Y2E0ZWNmZTFlYjVmZDVmMGMzOGM1NGM5YjFjNzIwM2Q3YSJ9fX0=", ChatColor.translateAlternateColorCodes('&', "&aNEXT &2&m---&l»"), null));
        }

        this.inventory.setItem(internalSize + 4, SkullUtil.makeSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWNhNmFiNzJlMDdiN2E1NTcwNGJkN2NjZjNkODJkYTBhNzM0NDNiZWViZGM1M2FjN2M5MDE0NDI3OWYwIn19fQ==", ChatColor.LIGHT_PURPLE + "Page " + ChatColor.DARK_PURPLE.toString()+ChatColor.ITALIC+"#" + (page + 1), null));

        this.populateSpecial();
    }
}
