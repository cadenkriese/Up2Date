package com.gamerking195.dev.up2date.ui;

import be.maximvdw.spigotsite.api.resource.Resource;
import be.maximvdw.spigotsite.api.resource.ResourceManager;
import com.gamerking195.dev.autoupdaterapi.AutoUpdaterAPI;
import com.gamerking195.dev.autoupdaterapi.util.UtilPlugin;
import com.gamerking195.dev.autoupdaterapi.util.UtilReader;
import com.gamerking195.dev.up2date.Up2Date;
import com.gamerking195.dev.up2date.update.PluginInfo;
import com.gamerking195.dev.up2date.update.UpdateManager;
import com.gamerking195.dev.up2date.util.UtilSiteSearch;
import com.gamerking195.dev.up2date.util.gui.ConfirmGUI;
import com.gamerking195.dev.up2date.util.gui.PageGUI;
import com.gamerking195.dev.up2date.util.item.ItemStackBuilder;
import net.wesjd.anvilgui.AnvilGUI;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Caden Kriese (GamerKing195) on 10/15/17.
 * <p>
 * License is specified by the distributor which this
 * file was written for. Otherwise it can be found in the LICENSE file.
 * If there is no license file the code is then completely copyrighted
 * and you must contact me before using it IN ANY WAY.
 */
public class UnlinkedGUI extends PageGUI {
    UnlinkedGUI() {
        super("&d&lU&5&l2&d&lD &8- &dUnlinked plugins.", 54);

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            boolean linked = false;
            for (PluginInfo info : UpdateManager.getInstance().getLinkedPlugins()) {
                if (info.getName().equals(plugin.getName()))
                    linked = true;
            }

            if (Up2Date.getInstance().getMainConfig().isSetupComplete() && !linked && !UpdateManager.getInstance().getUnknownPlugins().contains(plugin) && !plugin.getName().equals("Up2Date") && !plugin.getName().equals("AutoUpdaterAPI"))
                UpdateManager.getInstance().addUnknownPlugin(plugin);
            else if (UpdateManager.getInstance().getUnknownPlugins().contains(plugin) && !plugin.getName().equals("Up2Date") && !plugin.getName().equals("AutoUpdaterAPI"))
                UpdateManager.getInstance().getUnknownPlugins().remove(plugin);
        }
    }

    @Override
    protected List<ItemStack> getIcons() {
        List<ItemStack> stackList = new ArrayList<>();
        List<Plugin> pluginList = new ArrayList<>();

        for (Plugin plugin : UpdateManager.getInstance().getUnknownPlugins()) {
            if (plugin.getName().equalsIgnoreCase("ProtocolLib") || plugin.getName().equals("Up2Date") || plugin.getName().equals("AutoUpdaterAPI"))
                continue;

            //remove all plugins that are invalid.
            if (!Arrays.asList(Bukkit.getPluginManager().getPlugins()).contains(plugin)) {
                pluginList.add(plugin);
                continue;
            }

            stackList.add(new ItemStackBuilder(Material.STAINED_CLAY)
                                  .setDurability((short) 14)
                                  .setName("&f&l"+plugin.getName().toUpperCase())
                                  .setLore(getLore(WordUtils.wrap(plugin.getDescription().getDescription() != null ? plugin.getDescription().getDescription() : "None", 40, "%new%", false).split("%new%"),
                                          "",
                                          "&7&lDescription: ",
                                          "%description%",
                                          "",
                                          "&4&lUNKNOWN",
                                          "",
                                          "&8LEFT-CLICK &f| &a&oEnter ID",
                                          "&8RIGHT-CLICK &f| &a&oFully Delete")).build());
        }

        pluginList.forEach(plugin -> UpdateManager.getInstance().removeUnknownPlugin(plugin));

        return stackList;
    }

    @Override
    protected void onPlayerClickIcon(InventoryClickEvent event) {
        ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK, 1 ,1);

        if (event.getRawSlot() == 45)
            new UpdateGUI((Player) event.getWhoClicked()).open((Player) event.getWhoClicked());
        else {
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.STAINED_CLAY) {
                if (event.getClick() == ClickType.LEFT) {
                    new AnvilGUI(Up2Date.getInstance(), (Player) event.getWhoClicked(), "Enter plugin ID", (player, reply) -> {
                        if (NumberUtils.isNumber(reply)) {
                            try {
                                Plugin plugin = Bukkit.getPluginManager().getPlugin(getCorrectPluginName(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName())));
                                String pluginJson = UtilReader.readFrom("https://api.spiget.org/v2/resources/" + reply);

                                boolean premium = pluginJson.contains("\"premium\": true");
                                ResourceManager manager = AutoUpdaterAPI.getInstance().getApi().getResourceManager();

                                if (pluginJson.contains("\"external\": true")) {
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                    return "External downloads not supported.";
                                } else if (premium && AutoUpdaterAPI.getInstance().getCurrentUser() == null) {
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                    return "You must login to spigot for premium resources.";
                                } else if (!pluginJson.contains("\"type\": \".jar\"")) {
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                    return "Resource type must be a jar.";
                                }

                                Resource resource;
                                if (AutoUpdaterAPI.getInstance().getCurrentUser() != null) {
                                    resource = manager.getResourceById(Integer.valueOf(reply), AutoUpdaterAPI.getInstance().getCurrentUser());
                                } else {
                                    resource = manager.getResourceById(Integer.valueOf(reply));
                                }
                                if (resource != null) {
                                    UtilSiteSearch.SearchResult result = new UtilSiteSearch.SearchResult(resource.getResourceId(), plugin.getName(), plugin.getDescription().getDescription(), premium);

                                    UpdateManager.getInstance().removeLinkedPlugin(plugin);
                                    UpdateManager.getInstance().removeUnlinkedPlugin(plugin);
                                    UpdateManager.getInstance().removeUnknownPlugin(plugin);

                                    UpdateManager.getInstance().addLinkedPlugin(new PluginInfo(plugin, resource, result));

                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                    new UnlinkedGUI().open(player);
                                    return "Success!";
                                } else {
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                    return "Resource info not found!";
                                }
                            } catch (Exception ex) {
                                Up2Date.getInstance().printError(ex, "Error occurred while authenticating plugin with potential id '" + reply + "'");
                            }
                        }

                        return "Invalid String!";
                    });
                } else if (event.getClick() == ClickType.RIGHT) {
                    Plugin plugin = Bukkit.getPluginManager().getPlugin(getCorrectPluginName(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName())));

                    new ConfirmGUI("&dContinue?",
                                          () -> {
                                              UtilPlugin.unload(plugin);
                                              ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
                                              new UnlinkedGUI().open((Player) event.getWhoClicked());
                                          },

                                          () -> {
                                              ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                              new UnlinkedGUI().open((Player) event.getWhoClicked());
                                          },

                                          "&7Click '&a&lCONFIRM&7' if you want U2D",
                                          "to &nfully delete&7 '&d"+plugin.getName()+"&7'"
                    ).open((Player) event.getWhoClicked());
                }
            }
        }

    }

    @Override
    protected void populateSpecial() {
        inventory.setItem(45, new ItemStackBuilder(Material.BARRIER).setName("&4&l&m«---&r &cBACK").build());
    }

    /*
     * Utilities
     */

    private String[] getLore(String[] varArgLines, String... description) {
        ArrayList<String> lines = new ArrayList<>();

        for (String line : description) {
            if (line.contains("%description%")) {
                for (String varArgLine : varArgLines)
                    lines.add("&a&d&l"+varArgLine);
            } else
                lines.add(line);
        }

        return lines.toArray(new String[0]);
    }

    private String getCorrectPluginName(String pluginName) {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getName().equalsIgnoreCase(pluginName))
                return plugin.getName();
        }

        return null;
    }
}
