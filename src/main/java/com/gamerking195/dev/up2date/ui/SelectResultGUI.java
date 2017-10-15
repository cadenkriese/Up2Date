package com.gamerking195.dev.up2date.ui;

import be.maximvdw.spigotsite.api.exceptions.ConnectionFailedException;
import be.maximvdw.spigotsite.api.resource.Resource;
import com.gamerking195.dev.autoupdaterapi.AutoUpdaterAPI;
import com.gamerking195.dev.autoupdaterapi.util.UtilReader;
import com.gamerking195.dev.up2date.Up2Date;
import com.gamerking195.dev.up2date.update.PluginInfo;
import com.gamerking195.dev.up2date.update.UpdateManager;
import com.gamerking195.dev.up2date.util.UtilSiteSearch;
import com.gamerking195.dev.up2date.util.gui.GUI;
import com.gamerking195.dev.up2date.util.item.ItemStackBuilder;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Created by Caden Kriese (GamerKing195) on 9/3/17.
 * <p>
 * License is specified by the distributor which this
 * file was written for. Otherwise it can be found in the LICENSE file.
 * If there is no license file the code is then completely copyrighted
 * and you must contact me before using it IN ANY WAY.
 */
public class SelectResultGUI extends GUI {

    private Player player;
    private Plugin plugin;
    private ArrayList<UtilSiteSearch.SearchResult> results;

    SelectResultGUI(Player player, Plugin plugin) {
        super("&d&lU&5&l2&d&lD &7- &dSetup &5"+plugin.getName(), 36);

        this.player = player;
        this.plugin = plugin;
        this.results = UpdateManager.getInstance().getUnlinkedPlugins().get(plugin);
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event) {
        if (event.getSlot() == 31)
            new PluginLinkGUI(player).open(player);

        if (event.getCurrentItem().getType() == Material.STAINED_CLAY && event.getCurrentItem().getDurability() == (short) 5) {
            int id = Integer.valueOf(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getLore().get(2)).replace("ID: ", ""));

            try {
                Resource resource = AutoUpdaterAPI.getInstance().getApi().getResourceManager().getResourceById(id, AutoUpdaterAPI.getInstance().getCurrentUser());

                UtilSiteSearch.SearchResult result = new UtilSiteSearch.SearchResult(resource.getResourceId(), resource.getResourceName(), plugin.getDescription().getDescription(), event.getCurrentItem().getItemMeta().getLore().get(3).contains("TRUE"));

                UpdateManager.getInstance().addLinkedPlugin(new PluginInfo(plugin, resource, result));
                UpdateManager.getInstance().removeUnlinkedPlugin(plugin);
            } catch (ConnectionFailedException e) {
                Up2Date.getInstance().printError(e, "Error occurred while getting extra information on '"+plugin.getName()+"' #4");
            }

            new PluginLinkGUI(player).open(player);

            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }
    }

    @Override
    protected void populate() {
        inventory.setItem(0, new ItemStackBuilder(Material.DOUBLE_PLANT)
                                     .setName("&f&l"+plugin.getName().toUpperCase())
                                     .setLore(getLore(WordUtils.wrap(plugin.getDescription().getDescription() != null ? plugin.getDescription().getDescription() : "None", 40, "%new%", false).split("%new%"),
                                             "",
                                             "&7&lSearch Results: &d&l"+results.size(),
                                             "&7&lDescription: ",
                                             "%description%",
                                             "",
                                             "&6&lUNLINKED"))
                                     .build());

        inventory.setItem(31, new ItemStackBuilder(Material.BARRIER).setName("&c&l«&m---&r &4&lBACK").build());

        for (int i = 0; i < 5; i++)
            inventory.setItem(11+i, new ItemStackBuilder(Material.STAINED_CLAY).setDurability((short) 9).setName("&8&oLoading results...").build());

        if (inventory.getItem(11) != null && inventory.getItem(11).getDurability() != (short) 5)
            loadItems();
    }

    private void loadItems() {
        final int initialSize = results.size();
        for (int i = 0; i < results.size(); i++) {
            UtilSiteSearch.SearchResult result = results.get(i);

            ExecutorService pool = Up2Date.getInstance().getFixedThreadPool();

            pool.submit(() -> {
                try {
                    boolean premium = false;

                    try {
                        String pluginJson = UtilReader.readFrom("https://api.spiget.org/v2/resources/"+result.getId());

                        premium = pluginJson.contains("\"premium\": true");

                        //Check if the result we're displaying is invalid if so remove it.
                        if (pluginJson.contains("\"external\": true")) {
                            results.remove(result);
                        } else if (premium && AutoUpdaterAPI.getInstance().getCurrentUser() == null) {
                            results.remove(result);
                        } else if (!pluginJson.contains("\"type\": \".jar\"")) {
                            results.remove(result);
                        }
                    } catch (IOException ex) {
                        Up2Date.getInstance().systemOutPrintError(ex, "Error occurred while retrieving extra information for search result for "+plugin.getName());
                    }

                    final Resource resource = AutoUpdaterAPI.getInstance().getApi().getResourceManager().getResourceById(result.getId(), AutoUpdaterAPI.getInstance().getCurrentUser());

                    final boolean premiumResult = premium;

                    if (resource == null || resource.getAuthor() == null || resource.getResourceId() == 0)
                        results.remove(result);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Inventory playerInventory = player.getOpenInventory().getTopInventory();

                            if (results.contains(result) && playerInventory.getTitle().equals(inventory.getTitle())) {
                                for (int j = 0; j < 5; j++) {
                                    if (playerInventory.getItem(11+j).getDurability() == (short) 9) {
                                        playerInventory.setItem(j + 11, new ItemStackBuilder(Material.STAINED_CLAY)
                                                                                .setDurability((short) 5)
                                                                                .setName("&f&l" + plugin.getName().toUpperCase())
                                                                                .setLore(getLore(WordUtils.wrap(result.getTag(), 40, "%new%", false).split("%new%"),
                                                                                        "",
                                                                                        "&7&lAuthor: &d&l" + resource.getAuthor().getUsername(),
                                                                                        "&7&lID: &d&l" + resource.getResourceId(),
                                                                                        "&7&lPremium: &d&l"+String.valueOf(premiumResult).toUpperCase(),
                                                                                        "&7&lDescription: ",
                                                                                        "%description%",
                                                                                        "",
                                                                                        "&8LEFT-CLICK &f| &aLink to plugin '" + plugin.getName() + "'"))
                                                                                .build());
                                        break;
                                    }
                                }

                                //update info item
                                playerInventory.setItem(0, new ItemStackBuilder(Material.DOUBLE_PLANT)
                                                                   .setName("&f&l"+plugin.getName().toUpperCase())
                                                                   .setLore(getLore(WordUtils.wrap(plugin.getDescription().getDescription() != null ? plugin.getDescription().getDescription() : "None", 40, "%new%", false).split("%new%"),
                                                                           "",
                                                                           "&7&lSearch Results: &d&l"+results.size(),
                                                                           "&7&lDescription: ",
                                                                           "%description%",
                                                                           "",
                                                                           "&6&lUNLINKED"))
                                                                   .build());
                            }

                            //Loop through in reverse (start at 15 and go backwards) and set the clay to "no results"
                            int difference = 5 - results.size();
                            for (int i = 0; i < difference; i++) {
                                if (playerInventory.getTitle().equals(inventory.getTitle()) && playerInventory.getItem(15-i).getDurability() != (short) 5)
                                    playerInventory.setItem(15 - i, new ItemStackBuilder(Material.STAINED_CLAY).setDurability((short) 14).setName("&4&lNO RESULT").build());
                            }
                        }
                    }.runTask(Up2Date.getInstance());
                } catch (ConnectionFailedException ex) {
                    Up2Date.getInstance().printError(ex, "Error occurred while retrieving extra information for '" + result.getName() + "' #2");
                }

                if (results.size() < initialSize) {
                    if (results.size() == 0) {
                        UpdateManager.getInstance().removeUnlinkedPlugin(plugin);
                        UpdateManager.getInstance().addUnknownPlugin(plugin);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                new PluginLinkGUI(player).open(player);
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                            }
                        }.runTask(Up2Date.getInstance());
                    } else if (results.size() == 1 && results.contains(result)) {
                        try {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    new PluginLinkGUI(player).open(player);
                                }
                            }.runTask(Up2Date.getInstance());

                            UtilSiteSearch.SearchResult newResult = results.get(0);
                            Resource resource = AutoUpdaterAPI.getInstance().getApi().getResourceManager().getResourceById(results.get(0).getId(), AutoUpdaterAPI.getInstance().getCurrentUser());

                            UpdateManager.getInstance().addLinkedPlugin(new PluginInfo(plugin, resource, newResult));
                            UpdateManager.getInstance().removeUnlinkedPlugin(plugin);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    new PluginLinkGUI(player).open(player);
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                }
                            }.runTask(Up2Date.getInstance());
                        } catch (ConnectionFailedException e) {
                            Up2Date.getInstance().printError(e, "Error occurred while getting extra information on '"+plugin.getName()+"' #6");
                        }
                    } else {
                        UpdateManager.getInstance().addUnlinkedPlugin(plugin, results);
                    }
                }
            });
        }
    }

    private String[] getLore(String[] varArgLines, String... description) {
        ArrayList<String> lines = new ArrayList<>();

        for (String line : description) {
            if (line.contains("%description%")) {
                for (String varArgLine : varArgLines)
                    lines.add("&d&d&l"+varArgLine);
            } else
                lines.add(line);
        }

        return lines.toArray(new String[0]);
    }
}
