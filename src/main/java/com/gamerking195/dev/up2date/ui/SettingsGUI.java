package com.gamerking195.dev.up2date.ui;

import com.gamerking195.dev.up2date.ui.settings.AdvancedGUI;
import com.gamerking195.dev.up2date.ui.settings.GeneralGUI;
import com.gamerking195.dev.up2date.ui.settings.SQLGui;
import com.gamerking195.dev.up2date.util.gui.GUI;
import com.gamerking195.dev.up2date.util.item.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Created by Caden Kriese (GamerKing195) on 11/19/17.
 * <p>
 * License is specified by the distributor which this
 * file was written for. Otherwise it can be found in the LICENSE file.
 * If there is no license file the code is then completely copyrighted
 * and you must contact me before using it IN ANY WAY.
 */
public class SettingsGUI extends GUI {
    boolean backButton;

    public SettingsGUI(boolean backButton) {
        super("&d&lU&5&l2&d&lD &8- &dSettings", 36);
        this.backButton = backButton;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event) {
        ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

        switch (event.getRawSlot()) {
            case 11:
                new GeneralGUI().open((Player) event.getWhoClicked());
                break;
            case 13:
                new AdvancedGUI().open((Player) event.getWhoClicked());
                break;
            case 15:
                new SQLGui().open((Player) event.getWhoClicked());
                break;
            case 31:
                if (backButton)
                    new UpdateGUI((Player) event.getWhoClicked()).open((Player) event.getWhoClicked());
                break;
        }

    }

    @Override
    protected void populate() {
        inventory.setItem(11, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                                      .setDurability((short) 2)
                                      .setName("&d&lGENERAL")
                                      .setLore(
                                              "",
                                              "&5&lSettings:",
                                              "     &7• &fCache Refresh Delay",
                                              "     &f• &fDatabase Refresh Delay")
                                      .build());


        inventory.setItem(13, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                                      .setDurability((short) 2)
                                      .setName("&d&lADVANCED")
                                      .setLore(
                                              "",
                                              "&5&lSettings:",
                                              "     &7• &fThread Pool Size",
                                              "     &7• &fConnection Pool Size")
                                      .build());


        inventory.setItem(15, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                                      .setDurability((short) 2)
                                      .setName("&d&lSQL")
                                      .setLore(
                                              "",

                                              "",
                                              "&5&lSettings:",
                                              "     &7• &fEnable SQL",
                                              "     &7• &fHostname",
                                              "     &7• &fUsername",
                                              "     &7• &fPassword",
                                              "     &7• &fDatabase",
                                              "     &7• &fTablename")
                                      .build());

        if (backButton)
            inventory.setItem(31, new ItemStackBuilder(Material.BARRIER).setName("&4&l&m«---&r &cBACK").build());
    }
}
