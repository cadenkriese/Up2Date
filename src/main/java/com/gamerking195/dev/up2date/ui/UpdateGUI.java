package com.gamerking195.dev.up2date.ui;

import be.maximvdw.spigotsite.api.exceptions.ConnectionFailedException;
import be.maximvdw.spigotsite.api.resource.Resource;
import com.gamerking195.dev.autoupdaterapi.*;
import com.gamerking195.dev.autoupdaterapi.util.UtilReader;
import com.gamerking195.dev.up2date.Up2Date;
import com.gamerking195.dev.up2date.update.PluginInfo;
import com.gamerking195.dev.up2date.update.UpdateManager;
import com.gamerking195.dev.up2date.util.*;
import com.gamerking195.dev.up2date.util.gui.ConfirmGUI;
import com.gamerking195.dev.up2date.util.gui.PageGUI;
import com.gamerking195.dev.up2date.util.item.ItemStackBuilder;
import com.gamerking195.dev.up2date.util.text.MessageBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.wesjd.anvilgui.AnvilGUI;
import org.apache.commons.io.FileUtils;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by Caden Kriese (GamerKing195) on 10/9/17.
 * <p>
 * License is specified by the distributor which this
 * file was written for. Otherwise it can be found in the LICENSE file.
 * If there is no license file the code is then completely copyrighted
 * and you must contact me before using it IN ANY WAY.
 */
public class UpdateGUI extends PageGUI {
    private HashMap<Integer, PluginInfo> inventoryMap = new HashMap<>();

    private ArrayList<PluginInfo> updatesAvailable = new ArrayList<>();
    private ArrayList<PluginInfo> selection = new ArrayList<>();

    private Player player;

    public UpdateGUI(Player player) {
        super("&d&lUp&5&l2&d&lDate", 54);
        this.player = player;

        if (UpdateManager.getInstance().isCurrentTask()) {
            new MessageBuilder().addPlainText("&dThere's currently an update task running!").sendToPlayersPrefixed(player);

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.closeInventory();
                }
            }.runTaskLater(Up2Date.getInstance(), 2L);
        }
    }

    @Override
    protected List<ItemStack> getIcons() {
        if (UpdateManager.getInstance().isCurrentTask())
            return null;

        ArrayList<PluginInfo> linkedPlugins = UpdateManager.getInstance().getLinkedPlugins();
        ArrayList<PluginInfo> badPlugins = new ArrayList<>();

        List<ItemStack> stackList = new ArrayList<>();

        for (PluginInfo pluginInfo : linkedPlugins) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginInfo.getName());

            if (plugin == null) {
                badPlugins.add(pluginInfo);
                new MessageBuilder().addPlainText("&dThe plugin '&5"+pluginInfo.getName()+"&d' is missing, it &dhas &dbeen &dunlinked.").sendToPlayersPrefixed(player);
                continue;
            }

            try {
                short durability = 5;
                String updateStatus = "&dPlugin is &l&nUp&5&l&n2&d&l&nDate!";

                if (UtilPlugin.compareVersions(plugin.getDescription().getVersion(), pluginInfo.getLatestVersion())) {
                    updateStatus = "&e&lUpdate Available";
                    durability = 1;
                    if (!updatesAvailable.contains(pluginInfo))
                        updatesAvailable.add(pluginInfo);
                }

                stackList.add(new ItemStackBuilder(Material.STAINED_CLAY)
                                      .setDurability(durability)
                                      .setName("&f&l" + plugin.getName().toUpperCase())
                                      .setLore(getLore(WordUtils.wrap(pluginInfo.getDescription() != null ? pluginInfo.getDescription() : "None", 40, "%new%", false).split("%new%"),
                                              "",
                                              "&7&lAuthor: &d&l"+pluginInfo.getAuthor(),
                                              "&7&lID: &d&l" + pluginInfo.getId(),
                                              "&7&lServer Version: &d&l" + plugin.getDescription().getVersion(),
                                              "&7&lSpigot Version: &d&l" + pluginInfo.getLatestVersion(),
                                              "&7&lSupported MC Version: &d&l" + pluginInfo.getSupportedMcVersions(),
                                              "&7&lDescription: ",
                                              "%description%",
                                              "",
                                              updateStatus,
                                              "",
                                              "&8LEFT-CLICK &f| &a&oToggle Selection",
                                              "&8RIGHT-CLICK &f| &a&oUpdate ID",
                                              "&8SHIFT-LEFT-CLICK &f| &a&oUpdate Now",
                                              "&8SHIFT-RIGHT-CLICK &f| &c&oDelete Link",
                                              "&8DOUBLE-CLICK &f| &aCheck for Update"))
                                      .build());

                inventoryMap.put(stackList.size()-1, pluginInfo);
            } catch (Exception ex) {
                Up2Date.getInstance().printError(ex, "Error occurred while retrieving extra information for '"+pluginInfo.getName()+"' #1");
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (ChatColor.stripColor(player.getOpenInventory().getTitle()).equals("Up2Date")) {
                    ItemStack is = player.getOpenInventory().getItem(49);
                    ItemMeta im = is.getItemMeta();
                    im.setLore(Arrays.asList("", ChatColor.translateAlternateColorCodes('&', "&8LEFT-CLICK &f| &a&oOpen Settings"), ChatColor.translateAlternateColorCodes('&', "&8RIGHT-CLICK &f| &a&oRefresh GUI")));

                    is.setItemMeta(im);
                    player.getOpenInventory().setItem(49, is);
                }
            }
        }.runTaskLater(Up2Date.getInstance(), 1L);

        badPlugins.forEach(plugin -> UpdateManager.getInstance().removeLinkedPlugin(plugin));

        return stackList;
    }

    @Override
    protected void onPlayerClickIcon(InventoryClickEvent event) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

        if (selection == null)
            selection = new ArrayList<>();

        selection.remove(null);
        switch (event.getSlot()) {
            //VIEW UNLINKED PLUGINS
            case 45:
                updatePlugins(updatesAvailable);
                break;
            //APPLY ACTION TO SELECTED PLUGINS
            case 46:
                //LEFT-CLICK, UPDATE SELECTION
                if (event.getClick() == ClickType.LEFT) {
                    ArrayList<PluginInfo> validSelection = new ArrayList<>(selection);
                    ArrayList<PluginInfo> updatedPlugins = new ArrayList<>(UpdateManager.getInstance().getLinkedPlugins());
                    //remove all plugins that are updated.
                    updatedPlugins.removeAll(updatesAvailable);
                    validSelection.removeAll(updatedPlugins);
                    updatePlugins(validSelection);
                }
                //RIGHT-CLICK, REMOVE LINKS
                else if (event.getClick() == ClickType.RIGHT) {
                    new ConfirmGUI("&dContinue?",
                            () -> {
                                selection.forEach(info -> UpdateManager.getInstance().removeLinkedPlugin(info));
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
                            },

                            () -> {
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                new UpdateGUI(player).open(player);
                            },

                            "&7Click '&a&lCONFIRM&7' if you want U2D",
                            "to stop tracking the &d"+selection.size()+"&7 selected plugins.",
                            "&7You can always add them back later!"
                    ).open(player);
                    selection.forEach(info -> UpdateManager.getInstance().removeLinkedPlugin(info));
                    //SHIFT-RIGHT-CLICK, TOGGLE ENTIRE SELECTION
                } else if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    for (int i = 0; i < inventory.getSize()-9; i++) {
                        PluginInfo info = inventoryMap.get(i);
                        if (info == null || inventory.getItem(i) == null)
                            continue;

                        Plugin currentPlugin = Bukkit.getPluginManager().getPlugin(info.getName());

                        if (player.getOpenInventory().getItem(i).getDurability() == 4) {
                            player.getOpenInventory().getItem(i).setDurability((currentPlugin.getDescription().getVersion().equals(info.getLatestVersion()) ? (short) 5 : (short) 1));
                            selection.remove(inventoryMap.get(i));
                        } else {
                            player.getOpenInventory().getItem(i).setDurability((short) 4);
                            selection.add(inventoryMap.get(i));
                        }
                    }
                    //SHIFT-LEFT-CLICK, SELECT EVERYTHING
                } else if (event.getClick() == ClickType.SHIFT_LEFT) {
                    for (int i = 0; i < inventory.getSize()-9; i++) {
                        if (player.getOpenInventory().getItem(i) == null)
                            continue;

                        if (player.getOpenInventory().getItem(i).getDurability() != 4) {
                            player.getOpenInventory().getItem(i).setDurability((short) 4);
                            selection.add(inventoryMap.get(i));
                        }
                    }
                }
                break;
            //VIEW UNLINKED PLUGINS
            case 47:
                new UnlinkedGUI().open(player);
                break;
            //REFRESH GUI
            case 49:
                if (event.getClick() == ClickType.LEFT)
                    new SettingsGUI(true).open(player);
                else if (event.getClick() == ClickType.RIGHT)
                    new UpdateGUI(player).open(player);
                break;
            //REFRESH FROM DB
            case 51:
                UpdateManager.getInstance().getCacheUpdater().runTask(Up2Date.getInstance());
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                break;
            //DOWNLOAD & INSTALL A PLUGIN
            case 52:
                new AnvilGUI(Up2Date.getInstance(), player, "Enter plugin ID.", (p, pluginId) -> {
                    if (NumberUtils.isNumber(pluginId)) {
                        try {
                            player.closeInventory();
                            UtilText.getUtil().sendActionBar("&d&lU&5&l2&d&lD &7&oRetrieving info...", player);
                            String pluginJson = UtilReader.readFrom("https://api.spiget.org/v2/resources"+Up2Date.fs + pluginId);

                            boolean premium = pluginJson.contains("\"premium\": true");

                            if (pluginJson.contains("\"external\": true")) {
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                new MessageBuilder().addPlainText("&dPlugins with external downloads are not supported!").sendToPlayersPrefixed(player);
                                new UpdateGUI(player).open(player);
                                return "External downloads not supported!";
                            } else if (premium && AutoUpdaterAPI.getInstance().getCurrentUser() == null) {
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                new MessageBuilder().addPlainText("&dYou must be logged into spigot to download premium resources!").sendToPlayersPrefixed(player);
                                new UpdateGUI(player).open(player);
                                return "You must login to spigot for premium resources!";
                            } else if (!pluginJson.contains("\"type\": \".jar\"")) {
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                new MessageBuilder().addPlainText("&dResource download must be a jar!").sendToPlayersPrefixed(player);
                                new UpdateGUI(player).open(player);
                                return "Resource download must be a jar!";
                            }

                            Type type = new TypeToken<JsonObject>(){}.getType();
                            JsonObject object = new Gson().fromJson(pluginJson, type);

                            ArrayList<String> testedVersions = new ArrayList<>();
                            object.getAsJsonArray("testedVersions").forEach(testedVersion -> testedVersions.add(testedVersion.getAsString()));

                            UtilSiteSearch.SearchResult result = new UtilSiteSearch.SearchResult(object.get("id").getAsInt(), object.get("name").getAsString(), object.get("tag").getAsString(), pluginJson.contains("\"premium\": true"), testedVersions.toArray(new String[0]));

                            UpdateLocale locale = UpdateManager.getInstance().getDownloadLocale();

                            locale.setUpdatingDownload(locale.getUpdatingDownload().replace("%plugin%", result.getName()));
                            locale.setUpdating(locale.getUpdating().replace("%plugin%", result.getName()));
                            locale.setUpdateComplete(locale.getUpdateComplete().replace("%plugin%", result.getName()));
                            locale.setUpdateFailed(locale.getUpdateFailed().replace("%plugin%", result.getName()));

                            locale.setPluginName(null);
                            locale.setFileName(result.getName()+"-%new_version%");

                            Resource resource;
                            if (AutoUpdaterAPI.getInstance().getCurrentUser() != null)
                                resource = AutoUpdaterAPI.getInstance().getApi().getResourceManager().getResourceById(Integer.valueOf(pluginId), AutoUpdaterAPI.getInstance().getCurrentUser());
                            else
                                resource = AutoUpdaterAPI.getInstance().getApi().getResourceManager().getResourceById(Integer.valueOf(pluginId));

                            if (premium) {
                                new PremiumUpdater(player, Up2Date.getInstance(), Integer.valueOf(pluginId), locale, false, false, (success, ex, plugin, name) -> {
                                    if (success) {
                                        if (result.getName() != null && result.getTag() != null && result.getId() != 0)
                                            UpdateManager.getInstance().addLinkedPlugin(new PluginInfo(plugin, resource, result));
                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                                        UtilStatisticsDatabase.getInstance().addDownloadedFiles(1);
                                        UtilStatisticsDatabase.getInstance().addDownloadsize(getFileSize(plugin.getName()));
                                    } else
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                }).update();
                                player.closeInventory();

                                return "";
                            } else {
                                new Updater(player, Up2Date.getInstance(), Integer.valueOf(pluginId), locale, false, false, (success, ex, plugin, name) -> {
                                    if (success) {
                                        if (result.getName() != null && result.getTag() != null && result.getId() != 0)
                                            UpdateManager.getInstance().addLinkedPlugin(new PluginInfo(plugin, resource, result));
                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                                        UtilStatisticsDatabase.getInstance().addDownloadedFiles(1);
                                        UtilStatisticsDatabase.getInstance().addDownloadsize(getFileSize(plugin.getName()));
                                    } else {
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                    }
                                }).update();
                                player.closeInventory();

                                return "";
                            }
                        } catch (Exception ex) {
                            Up2Date.getInstance().printError(ex, "Error occurred while authenticating plugin with potential id '" + pluginId + "'");
                        }
                    }

                    return "Invalid String!";
                });
                break;
            //FORCE INFO REFRESH
            case 53:
                ArrayList<PluginInfo> plugins = new ArrayList<>(UpdateManager.getInstance().getLinkedPlugins());
                plugins.removeAll(updatesAvailable);

                player.closeInventory();

                ArrayList<PluginInfo> updatedInfo = new ArrayList<>();
                for (final PluginInfo info : plugins) {
                    if (info == null)
                        continue;

                    ExecutorService threadPool = Up2Date.getInstance().getFixedThreadPool();

                    threadPool.submit(() -> {
                        try {
                            //Update resource version
                            Resource resource;
                            if (AutoUpdaterAPI.getInstance().getCurrentUser() != null)
                                resource = AutoUpdaterAPI.getInstance().getApi().getResourceManager().getResourceById(info.getId(), AutoUpdaterAPI.getInstance().getCurrentUser());
                            else
                                resource = AutoUpdaterAPI.getInstance().getApi().getResourceManager().getResourceById(info.getId());

                            UpdateManager.getInstance().removeLinkedPlugin(info);
                            info.setLatestVersion(resource.getLastVersion());
                            UpdateManager.getInstance().addLinkedPlugin(info);

                            updatedInfo.add(info);

                            UtilSQL.getInstance().runStatement("INSERT INTO TABLENAME (name, id, author, version, description, premium, testedversions, lastupdated) VALUES ('"+info.getName()+"', '"+info.getId()+"', '"+info.getAuthor()+"', '"+info.getLatestVersion()+"', '"+info.getDescription()+"', '"+info.isPremium()+"', '"+info.getSupportedMcVersions()+"', '"+new Timestamp(System.currentTimeMillis())+"')" +
                                                                       " ON DUPLICATE KEY UPDATE name=VALUES(name),id=VALUES(id),author=VALUES(author),version=VALUES(version),description=VALUES(description),premium=VALUES(premium),testedversions=VALUES(testedversions),lastupdated=VALUES(lastupdated)");


                        } catch (ConnectionFailedException ex) {
                            Up2Date.getInstance().systemOutPrintError(ex, "Error occurred while updating info for '"+info.getName()+"'");
                        }
                    });
                }

                final long startingTime = System.currentTimeMillis();
                new BukkitRunnable() {
                    @Override
                    public void run() {

                        double percent = ((double) 100/plugins.size()) * updatedInfo.size();
                        UtilText.getUtil().sendActionBar("&d&lU&5&l2&d&lD &7&oUpdated information for "+updatedInfo.size()+"/"+plugins.size()+" plugins ("+String.format("%.2f", percent)+"%)", player);

                        if (updatedInfo.size() == plugins.size()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                            UtilText.getUtil().sendActionBar("&d&lU&5&l2&d&lD &7&oUpdated information for "+plugins.size()+" plugins in "+String.format("%.2f", ((double) (System.currentTimeMillis() - startingTime) / 1000))+" seconds.", player);

                            updatesAvailable = UpdateManager.getInstance().getAvailableUpdates();

                            cancel();
                        }
                    }
                }.runTaskTimer(Up2Date.getInstance(), 0L, 30L);

                break;
            default:
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() != Material.STAINED_CLAY)
                    return;

                PluginInfo pluginInfo = inventoryMap.get(event.getRawSlot());
                Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginInfo.getName());
                if (event.getCurrentItem().getType() == Material.STAINED_CLAY) {
                    //LEFT-CLICK, SELECT PLUGIN
                    if (event.getClick() == ClickType.LEFT) {
                        if (event.getCurrentItem().getDurability() == 4) {
                            event.getCurrentItem().setDurability((plugin.getDescription().getVersion().equals(pluginInfo.getLatestVersion()) ? (short) 5 : (short) 1));
                            selection.remove(inventoryMap.get(event.getRawSlot()));
                        } else {
                            event.getCurrentItem().setDurability((short) 4);
                            selection.add(inventoryMap.get(event.getRawSlot()));
                        }
                        //RIGHT-CLICK, CHANGE PLUGIN ID
                    } else if (event.getClick() == ClickType.RIGHT) {
                        new AnvilGUI(Up2Date.getInstance(), player, "Enter plugin ID", (player, reply) -> {
                            if (NumberUtils.isNumber(reply)) {
                                try {
                                    String pluginJson = UtilReader.readFrom("https://api.spiget.org/v2/resources"+Up2Date.fs+reply);

                                    boolean premium = pluginJson.contains("\"premium\": true");

                                    if (pluginJson.contains("\"external\": true")) {
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                        return "External downloads not supported!";
                                    } else if (premium && AutoUpdaterAPI.getInstance().getCurrentUser() == null) {
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                        return "You must login to spigot for premium resources!";
                                    } else if (!pluginJson.contains("\"type\": \".jar\"")) {
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                        return "Resource type must be a jar!";
                                    }

                                    Resource resource;
                                    if (AutoUpdaterAPI.getInstance().getCurrentUser() != null) {
                                        resource = AutoUpdaterAPI.getInstance().getApi().getResourceManager().getResourceById(Integer.valueOf(reply), AutoUpdaterAPI.getInstance().getCurrentUser());
                                    } else {
                                        resource = AutoUpdaterAPI.getInstance().getApi().getResourceManager().getResourceById(Integer.valueOf(reply));
                                    }
                                    if (resource != null) {
                                        UtilSiteSearch.SearchResult result = new UtilSiteSearch.SearchResult(resource.getResourceId(), plugin.getName(), plugin.getDescription().getDescription(), premium, new String[]{"NULL"});

                                        UpdateManager.getInstance().removeLinkedPlugin(plugin);
                                        UpdateManager.getInstance().removeUnlinkedPlugin(plugin);
                                        UpdateManager.getInstance().removeUnknownPlugin(plugin);

                                        UpdateManager.getInstance().addLinkedPlugin(new PluginInfo(plugin, resource, result));

                                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                        new UpdateGUI(player).open(player);
                                        return "Success.";
                                    } else {
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                        return "Resource info not found.";
                                    }
                                } catch (Exception ex) {
                                    Up2Date.getInstance().printError(ex, "Error occurred while authenticating plugin with potential id '"+reply+"'");
                                }
                            }

                            return "Invalid String!";
                        });
                        //SHIFT-LEFT-CLICK, UPDATE INDIVIDUAL PLUGIN
                    } else if (event.getClick() == ClickType.SHIFT_LEFT)
                        updatePlugin(pluginInfo, plugin, false);

                        //SHIFT-RIGHT-CLICK, REMOVE LINK
                    else if (event.getClick() == ClickType.SHIFT_RIGHT) {
                        PluginInfo info = inventoryMap.get(event.getRawSlot());
                        new ConfirmGUI("&dContinue?",
                                () -> {
                                    UpdateManager.getInstance().removeLinkedPlugin(info);

                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
                                    new UpdateGUI(player).open(player);
                                },

                                () -> {
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                    new UpdateGUI(player).open(player);
                                },

                                "&7Click '&a&lCONFIRM&7' if you want U2D",
                                "to stop tracking '&d"+info.getName()+"'",
                                "&7You can always add it back later!"
                        ).open(player);
                        //DOUBLE-CLICK Check for single update.
                    } else if (event.getClick() == ClickType.DOUBLE_CLICK) {
                        player.closeInventory();

                        UtilText.getUtil().sendActionBar("&5&lU&d&l2&5&lD &7&oChecking for updates, don't re-open the GUI.", player);

                        PluginInfo info = inventoryMap.get(event.getRawSlot());

                        ExecutorService threadPool = Up2Date.getInstance().getFixedThreadPool();

                        threadPool.submit(() -> {
                            try {
                                Resource resource;
                                if (AutoUpdaterAPI.getInstance().getCurrentUser() != null) {
                                    resource = AutoUpdaterAPI.getInstance().getApi().getResourceManager().getResourceById(info.getId(), AutoUpdaterAPI.getInstance().getCurrentUser());
                                } else {
                                    resource = AutoUpdaterAPI.getInstance().getApi().getResourceManager().getResourceById(info.getId());
                                }

                                UpdateManager.getInstance().removeLinkedPlugin(info);
                                boolean updateFound = UtilPlugin.compareVersions(info.getLatestVersion(), resource.getLastVersion());
                                info.setLatestVersion(resource.getLastVersion());
                                UpdateManager.getInstance().addLinkedPlugin(info);

                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (updateFound)
                                            new MessageBuilder().addPlainText("&dPlugin info updated, &aupdate found&d!").sendToPlayersPrefixed(player);
                                        else
                                            new MessageBuilder().addPlainText("&dPlugin info updated, &cno updates found&d.").sendToPlayersPrefixed(player);

                                        new UpdateGUI(player).open(player);
                                    }
                                }.runTask(Up2Date.getInstance());
                            } catch (ConnectionFailedException ex) {
                                Up2Date.getInstance().systemOutPrintError(ex, "Error occurred while updating info for '"+info.getName()+"'");
                            }
                        });
                    }
                }
                break;
        }
    }

    //Items for bottom row
    @Override
    protected void populateSpecial() {
        inventory.setItem(45, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                                      .setName("&2&lUPDATE ALL")
                                      .setLore(
                                              "",
                                              "&8CLICK &f| &a&oUpdate Everything")
                                      .setDurability((short) 5)
                                      .build());

        inventory.setItem(46, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                                      .setName("&e&lAPPLY ACTION TO SELECTED")
                                      .setLore(
                                              "",
                                              "&8LEFT-CLICK &f| &a&oUpdate Now",
                                              "&8RIGHT-CLICK &f| &c&oDelete Link",
                                              "&8SHIFT-RIGHT-CLICK &f| &a&oToggle Entire Selection",
                                              "&8SHIFT-LEFT-CLICK &f| &a&oSelect Everything")
                                      .setDurability((short) 4)
                                      .build());

        inventory.setItem(47, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                                      .setName("&4&lVIEW UNLINKED PLUGINS")
                                      .setLore(
                                              "",
                                              "&8CLICK &f| &a&oView all unlinked plugins.")
                                      .setDurability((short) 14)
                                      .build());

        String dataStorage = Up2Date.getInstance().getMainConfig().isEnableSQL() ? "database" : "file";

        inventory.setItem(51, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                                      .setName("&2&lSAVE ALL DATA")
                                      .setLore(
                                              "",
                                              "&8CLICK &f| &a&oSave all data to the "+dataStorage+".")
                                      .setDurability((short) 11)
                                      .build());

        inventory.setItem(52, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                                      .setName("&5&lINSTALL A PLUGIN")
                                      .setLore(
                                              "",
                                              "&8CLICK &f| &a&oEnter an ID for a plugin to be downloaded & installed.")
                                      .setDurability((short) 2)
                                      .build());

        inventory.setItem(53, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                                      .setName("&d&lFORCE REFRESH")
                                      .setLore(
                                              "",
                                              "&8CLICK &f| &a&oRetrieve latest plugin info from Spigot. (ETA "+UpdateManager.getInstance().getLinkedPlugins().size()*4+" Sec)")
                                      .setDurability((short) 6)
                                      .build());
    }

    /*
     * TODO Utilities
     */

    //Boolean for success/fail
    private boolean updatePlugin(PluginInfo pluginInfo, Plugin plugin, UpdaterRunnable customRunnable, boolean silent) {
        if (!updatesAvailable.contains(pluginInfo)) {
            if (!silent) {
                player.closeInventory();
                UtilText.getUtil().sendActionBar("&d&lU&5&l2&d&lD &7&oThat plugin is already &dUp&52&dDate!", player);
            }
            return false;
        }

        //Temporarily disable all plugins depending on the one we're updating to clear the instances and prevent a memory leak.
        ArrayList<Plugin> dependers = new ArrayList<>();

        for (Plugin depender : Bukkit.getPluginManager().getPlugins()) {
            if (depender.getDescription().getDepend().contains(plugin.getName()) || depender.getDescription().getSoftDepend().contains(plugin.getName())) {
                Bukkit.getPluginManager().disablePlugin(depender);
                dependers.add(depender);
            }
        }

        UtilPlugin.unload(plugin);

        //Cache version in case update goes wrong.
        String oldFile;
        try {
            File oldJar = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            oldFile = oldJar.getName();
            File cacheFile = new File(Up2Date.getInstance().getDataFolder().getAbsolutePath()+Up2Date.fs+"caches"+Up2Date.fs+plugin.getName()+Up2Date.fs+oldJar.getName());

            if (cacheFile.getParentFile().exists())
                FileUtils.deleteDirectory(cacheFile.getParentFile());

            if (!cacheFile.getParentFile().mkdirs()) {
                Up2Date.getInstance().printPluginError("Error occurred while caching old plugin version", "Directory creation failed.");
                return false;
            }

            if (cacheFile.exists())
                cacheFile.delete();
            FileUtils.copyFile(oldJar, cacheFile);
        } catch (URISyntaxException | IOException e) {
            Up2Date.getInstance().printError(e, "Error occurred while caching old plugin version.");
            return false;
        }

        final String oldFileResult = oldFile;

        //Runnable to be run after update completes/fails.
        final UpdaterRunnable runnable = (success, ex, pl, name) -> {

            customRunnable.run(success, ex, pl, name);
            if (!silent)
                UpdateManager.getInstance().setCurrentTask(false);

            if (success) {
                updateSuccess(pluginInfo, plugin, silent);
                //Re-enable previously disabled dependers.
                dependers.forEach(depender -> Bukkit.getPluginManager().enablePlugin(depender));

                if (!silent)
                    new UpdateGUI(player).open(player);
            } else {
                restoreFile(oldFileResult, name);

                if (!silent)
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);

                if (ex instanceof IllegalPluginAccessException || ex instanceof InvalidPluginException || ex instanceof InvalidDescriptionException) {
                    UtilStatisticsDatabase.getInstance().addIncompatiblePlugin(pluginInfo);
                }
            }
        };

        //Set the state of the plugin as performing an update.
        if (!silent)
            UpdateManager.getInstance().setCurrentTask(true);

        //Actually apply update.
        //Booleans, 1 delete the updater 2 delete the old version of the plugin, set to true since we make a copy to cache instead of a rename.

        //Supress actionbars if silent is true.
        Player p = silent ? null : player;

        if (pluginInfo.isPremium())
            new PremiumUpdater(p, plugin, pluginInfo.getId(), UpdateManager.getInstance().getUpdateLocale(), false, true, runnable).update();
        else
            new Updater(p, plugin, pluginInfo.getId(), UpdateManager.getInstance().getUpdateLocale(), false, true, runnable).update();

        return true;
    }

    private void updatePlugin(PluginInfo pluginInfo, Plugin plugin, boolean silent) {
        updatePlugin(pluginInfo, plugin, (b, e, plugin1, name) -> {}, silent);
    }

    private int previousUpdateCount = 0;

    private void updatePlugins(ArrayList<PluginInfo> updates) {
        //Duplicated arraylist to make sure it doesnt get modified.
        ArrayList<PluginInfo> updatesNeeded = new ArrayList<>(updates);

        new ConfirmGUI(
                "&dContinue?",

                //Confirm
                () -> {
                    player.closeInventory();
                    UpdateManager.getInstance().setCurrentTask(true);
                    final long startTime = System.currentTimeMillis();

                    ArrayList<PluginInfo> successfulUpdates = new ArrayList<>();
                    ArrayList<PluginInfo> failedUpdates = new ArrayList<>();

                    //Create runnable to update our info after an update.
                    UpdaterRunnable runnable = (success, exception, plugin, name) -> {
                        PluginInfo info = UpdateManager.getInstance().getInfoFromPluginName(name);
                        if (success)
                            successfulUpdates.add(info);
                        else
                            failedUpdates.add(info);
                    };

                    int totalUpdateCount = updatesNeeded.size();

                    PluginInfo info = updatesNeeded.get(0);
                    Plugin plugin = Bukkit.getPluginManager().getPlugin(info.getName());
                    updatePlugin(info, plugin, runnable, true);

                    new BukkitRunnable() {
                        @Override
                        public void run() {

                            //Check if a plugin has been updated if so start another one to never have more than one running at once. && Make sure we're not trying to start another update after we're done.
                            if (previousUpdateCount < (successfulUpdates.size() + failedUpdates.size()) && failedUpdates.size()+successfulUpdates.size() < totalUpdateCount) {
                                //Add one to the count
                                previousUpdateCount = (successfulUpdates.size() + failedUpdates.size());

                                PluginInfo info = updatesNeeded.get(previousUpdateCount);
                                Plugin plugin = Bukkit.getPluginManager().getPlugin(info.getName());
                                updatePlugin(info, plugin, runnable, true);
                            }

                            double percent = ((double) 100/totalUpdateCount) * (successfulUpdates.size() + failedUpdates.size());
                            UtilText.getUtil().sendActionBar("&d&lU&5&l2&d&lD &7&oUpdated "+(successfulUpdates.size()+failedUpdates.size())+"/"+totalUpdateCount+" plugins ("+String.format("%.2f", percent)+"%)", player);

                            if (failedUpdates.size()+successfulUpdates.size() >= totalUpdateCount) {
                                UpdateManager.getInstance().setCurrentTask(false);
                                if (successfulUpdates.size() == totalUpdateCount) {
                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                    UtilText.getUtil().sendActionBar("&d&lU&5&l2&d&lD &7&oSuccessfully updated all " + totalUpdateCount + " plugins in " + String.format("%.2f", ((double) (System.currentTimeMillis() - startTime) / 1000)) + " seconds.", player);
                                } else {
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                    UtilText.getUtil().sendActionBar("&d&lU&5&l2&d&lD &7&oSuccessfully updated " + successfulUpdates.size() + " plugins and " + failedUpdates.size() + " "+UtilText.getUtil().getEnding("update", failedUpdates.size(), false)+" failed in " + String.format("%.2f", ((double) (System.currentTimeMillis() - startTime) / 1000)) + " seconds. &c&o(Check Console.)", player);
                                }

                                try {
                                    FileUtils.deleteDirectory(new File(Up2Date.getInstance().getDataFolder().getPath()+Up2Date.fs+"caches"));
                                } catch (IOException ex) {
                                    Up2Date.getInstance().printError(ex, "Error occurred while deleting caches directory recursively.");
                                }

                                //TODO randomly removed because this happens on updates success, add back if errors.
                                //if (updatesNeeded == updatesAvailable)
                                //updatesAvailable.removeAll(successfulUpdates);

                                new UpdateGUI(player).open(player);
                                cancel();
                            }
                        }
                    }.runTaskTimer(Up2Date.getInstance(), 5L, 40L);
                },

                //Cancel
                () -> new UpdateGUI(player).open(player),

                "&7By clicking '&a&lCONFIRM&7' the server",
                "&7will download and install &d"+updatesNeeded.size()+"&7 updates &osimultaneously&7.",
                "&7we've made this as efficient as possible",
                "&7but still use common sense!"
        ).open(player);
    }

    private void restoreFile(String oldFileNameResult, String pluginName) {

        //Unload invalid plugin.
        if (Bukkit.getPluginManager().getPlugin(pluginName) != null)
            UtilPlugin.unload(Bukkit.getPluginManager().getPlugin(pluginName));

        //Delete any instances of the plugin from the plugins folder.
        for (File file : Objects.requireNonNull(Up2Date.getInstance().getDataFolder().getParentFile().listFiles())) {
            if (file.getName().contains(pluginName) && file.getName().contains(".jar")) {
                //Try to delete and if it fails try to force delete.
                if (!file.delete()) {
                    try {
                        FileUtils.forceDelete(file);
                    } catch (IOException e) {
                        Up2Date.getInstance().printError(e, "Corrupt download deletion failed, file name '" + file.getName() + "'.");
                    }
                }
            }
        }

        if (oldFileNameResult != null) {
            File cachedPlugin = null;

            //Find cached jar
            File cacheDirectory = new File (Up2Date.getInstance().getDataFolder().getAbsolutePath()+Up2Date.fs+"caches"+Up2Date.fs+pluginName);
            for (File file : Objects.requireNonNull(cacheDirectory.listFiles())) {
                if (file.getName().contains(pluginName)) {
                    cachedPlugin = file;
                }
            }

            if (cachedPlugin == null)
                return;

            File movedPlugin = new File(Up2Date.getInstance().getDataFolder().getParentFile().getAbsolutePath()+Up2Date.fs+cachedPlugin.getName());

            //Rename cached jar, putting it into the plugins folder.
            try {
                FileUtils.moveFile(cachedPlugin, movedPlugin);
            } catch (IOException e) {
                Up2Date.getInstance().printError(e, "Failed to move '" + cachedPlugin.getAbsolutePath() + "' to '"+movedPlugin.getAbsolutePath()+"'.");
            }

            //Initialize moved plugin.
            try {
                Plugin reinitializedPlugin = Bukkit.getPluginManager().loadPlugin(movedPlugin);
                if (reinitializedPlugin != null)
                    Bukkit.getPluginManager().enablePlugin(reinitializedPlugin);
            } catch (InvalidPluginException | InvalidDescriptionException e) {
                if (UpdateManager.getInstance().getInfoFromPluginName(pluginName) != null)
                    UtilStatisticsDatabase.getInstance().addIncompatiblePlugin(UpdateManager.getInstance().getInfoFromPluginName(pluginName));

                Up2Date.getInstance().printError(e, "Error occurred while fixing failed plugin download.");
            }

            //Clean up the cache
            try {
                FileUtils.deleteDirectory(new File(Up2Date.getInstance().getDataFolder().getPath()+Up2Date.fs+"caches"+Up2Date.fs+pluginName));

                if (Objects.requireNonNull(new File(Up2Date.getInstance().getDataFolder().getPath() + Up2Date.fs + "caches").listFiles()).length == 0) {
                        FileUtils.deleteDirectory(new File(Up2Date.getInstance().getDataFolder().getPath() + Up2Date.fs + "caches"));
                }
            } catch (IOException e) {
                Up2Date.getInstance().printError(e, "Error occurred while clearing caches.");
            }
        }
    }

    private void updateSuccess(PluginInfo pluginInfo, Plugin plugin, boolean silent) {
        //Clean up the cache
        try {
            FileUtils.deleteDirectory(new File(Up2Date.getInstance().getDataFolder().getPath()+Up2Date.fs+"caches"+Up2Date.fs+plugin.getName()));

            if (Objects.requireNonNull(new File(Up2Date.getInstance().getDataFolder().getPath() + Up2Date.fs + "caches").listFiles()).length == 0) {
                FileUtils.deleteDirectory(new File(Up2Date.getInstance().getDataFolder().getPath() + Up2Date.fs + "caches"));
            }
        } catch (IOException e) {
            Up2Date.getInstance().printError(e, "Error occurred while clearing caches.");
        }

        if (!silent)
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

        updatesAvailable.remove(pluginInfo);

        if (player.getOpenInventory().getTopInventory() == getInventory() && !silent)
            new UpdateGUI(player).open(player);

        UtilStatisticsDatabase.getInstance().addDownloadedFiles(1);
        UtilStatisticsDatabase.getInstance().addDownloadsize(getFileSize(plugin.getName(), pluginInfo.getLatestVersion()));
    }

    private float getFileSize(String pluginName, String newVersion) {
        return (float) new File(Up2Date.getInstance().getDataFolder().getParent() + FileSystems.getDefault().getSeparator() + UpdateManager.getInstance().getUpdateLocale().getFileName().replace("%plugin%", pluginName).replace("%new_version%", newVersion)+".jar").length();
    }

    private float getFileSize(String pluginName) {
        for (File file : Objects.requireNonNull(Up2Date.getInstance().getDataFolder().getParentFile().listFiles())) {
            if (file.getName().contains(pluginName) && file.getName().contains(".jar") && !file.isDirectory()) {
                return file.length();
            }
        }
        return 0;
    }

    private String[] getLore(String[] varArgLines, String... description) {
        ArrayList<String> lines = new ArrayList<>();

        for (String line : description) {
            if (line.contains("%description%")) {
                for (String varArgLine : varArgLines)
                    lines.add("&d&l"+varArgLine);
            } else
                lines.add(line);
        }
        return lines.toArray(new String[0]);
    }
}
