package com.gamerking195.dev.up2date.ui;

import be.maximvdw.spigotsite.api.exceptions.ConnectionFailedException;
import be.maximvdw.spigotsite.api.resource.Resource;
import com.gamerking195.dev.autoupdaterapi.*;
import com.gamerking195.dev.autoupdaterapi.util.UtilPlugin;
import com.gamerking195.dev.autoupdaterapi.util.UtilReader;
import com.gamerking195.dev.up2date.Up2Date;
import com.gamerking195.dev.up2date.update.PluginInfo;
import com.gamerking195.dev.up2date.update.UpdateManager;
import com.gamerking195.dev.up2date.util.UtilDatabase;
import com.gamerking195.dev.up2date.util.UtilSiteSearch;
import com.gamerking195.dev.up2date.util.UtilText;
import com.gamerking195.dev.up2date.util.gui.ConfirmGUI;
import com.gamerking195.dev.up2date.util.gui.PageGUI;
import com.gamerking195.dev.up2date.util.item.ItemStackBuilder;
import com.gamerking195.dev.up2date.util.text.MessageBuilder;
import com.google.common.io.Files;
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
import java.nio.file.StandardCopyOption;
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
                new MessageBuilder().addPlainText("&dThe plugin '&5"+pluginInfo.getName()+"&d' is missing, it has been unlinked.").sendToPlayersPrefixed(player);
                continue;
            }

            try {
                short durability = 5;
                String updateStatus = "&d&l&nUp&5&l&n2&d&l&nDate!";

                if (!plugin.getDescription().getVersion().equals(pluginInfo.getLatestVersion())) {
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
            //SAVE DATA
            case 51:
                UpdateManager.getInstance().saveData();
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                break;
            //DOWNLOAD & INSTALL A PLUGIN
            case 52:
                new AnvilGUI(Up2Date.getInstance(), player, "Enter plugin ID.", (p, pluginId) -> {
                    if (NumberUtils.isNumber(pluginId)) {
                        try {
                            player.closeInventory();
                            UtilText.getUtil().sendActionBar("&d&lU&5&l2&d&lD &7&oRetrieving info...", player);
                            String pluginJson = UtilReader.readFrom("https://api.spiget.org/v2/resources/" + pluginId);

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
                                new PremiumUpdater(player, Up2Date.getInstance(), Integer.valueOf(pluginId), locale, false, false, (success, ex, plugin) -> {
                                    if (success) {
                                        if (result.getName() != null && result.getTag() != null && result.getId() != 0)
                                            UpdateManager.getInstance().addLinkedPlugin(new PluginInfo(plugin, resource, result));
                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                                        UtilDatabase.getInstance().addDownloadedFiles(1);
                                        UtilDatabase.getInstance().addDownloadsize(getFileSize(plugin.getName()));
                                    } else
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                }).update();
                                player.closeInventory();

                                return "";
                            } else {
                                new Updater(player, Up2Date.getInstance(), Integer.valueOf(pluginId), locale, false, false, (success, ex, plugin) -> {
                                    if (success) {
                                        if (result.getName() != null && result.getTag() != null && result.getId() != 0)
                                            UpdateManager.getInstance().addLinkedPlugin(new PluginInfo(plugin, resource, result));
                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                                        UtilDatabase.getInstance().addDownloadedFiles(1);
                                        UtilDatabase.getInstance().addDownloadsize(getFileSize(plugin.getName()));
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
                            Resource resource;
                            if (AutoUpdaterAPI.getInstance().getCurrentUser() != null) {
                                resource = AutoUpdaterAPI.getInstance().getApi().getResourceManager().getResourceById(info.getId(), AutoUpdaterAPI.getInstance().getCurrentUser());
                            } else {
                                resource = AutoUpdaterAPI.getInstance().getApi().getResourceManager().getResourceById(info.getId());
                            }

                            UpdateManager.getInstance().removeLinkedPlugin(info);
                            info.setLatestVersion(resource.getLastVersion());
                            updatedInfo.add(info);
                            UpdateManager.getInstance().addLinkedPlugin(info);
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

                            UpdateManager.getInstance().saveData();

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
                                    String pluginJson = UtilReader.readFrom("https://api.spiget.org/v2/resources/"+reply);

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
                    } else if (event.getClick() == ClickType.SHIFT_LEFT) {
                        if (!updatesAvailable.contains(pluginInfo)) {
                            player.closeInventory();
                            UtilText.getUtil().sendActionBar("&d&lU&5&l2&d&lD &7&oThat plugin is already &dUp&52&dDate!", player);
                            return;
                        }

                        if (plugin.getName().equals("Up2Date")) {
                            new PremiumUpdater(player, plugin, pluginInfo.getId(), UpdateManager.getInstance().getUpdateLocale(), false, true, (success, ex, pl) -> {
                                UpdateManager.getInstance().setCurrentTask(false);
                                if (success) {
                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                    updatesAvailable.remove(pluginInfo);

                                    UtilDatabase.getInstance().addDownloadedFiles(1);
                                    UtilDatabase.getInstance().addDownloadsize(getFileSize(plugin.getName(), pluginInfo.getLatestVersion()));
                                } else {
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                }
                            }).update();
                            return;
                        }

                        //Cache version in case update goes wrong.
                        String oldFile = null;
                        try {
                            File oldJar = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                            oldFile = oldJar.getName();
                            File cacheFile = new File(Up2Date.getInstance().getDataFolder().getAbsolutePath()+"/caches/"+plugin.getName()+"/"+oldJar.getName());
                            if (!cacheFile.getParentFile().mkdirs()) {
                                Up2Date.getInstance().printPluginError("Error occurred while caching old plugin version", "Directory creation failed.");
                                return;
                            }

                            if (!oldJar.renameTo(cacheFile)) {
                                Up2Date.getInstance().printPluginError("Error occurred while caching old plugin version.", "Rename failed.");
                                return;
                            }
                        } catch (URISyntaxException  e) {
                            Up2Date.getInstance().printError(e, "Error occurred while caching old plugin version.");
                        }

                        UtilPlugin.unload(plugin);

                        final String oldFileResult = oldFile;
                        //Actually apply update.
                        UpdateManager.getInstance().setCurrentTask(true);
                        if (pluginInfo.isPremium()) {
                            new PremiumUpdater(player, plugin, pluginInfo.getId(), UpdateManager.getInstance().getUpdateLocale(), false, false, (success, ex, pl) -> {
                                UpdateManager.getInstance().setCurrentTask(false);
                                if (success)
                                    updateSuccess(pluginInfo, plugin);
                                else {
                                    restoreFile(oldFileResult, plugin.getName(), plugin.getDescription().getVersion());
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);

                                    if (ex instanceof IllegalPluginAccessException || ex instanceof InvalidPluginException || ex instanceof InvalidDescriptionException) {
                                        UtilDatabase.getInstance().addIncompatiblePlugin(pluginInfo);
                                    }
                                }
                            }).update();
                        } else {

                            new Updater(player, plugin, pluginInfo.getId(), UpdateManager.getInstance().getUpdateLocale(), false, false, (success, ex, pl) -> {
                                UpdateManager.getInstance().setCurrentTask(false);
                                if (success)
                                    updateSuccess(pluginInfo, plugin);
                                else {
                                    restoreFile(oldFileResult, plugin.getName(), plugin.getDescription().getVersion());
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                    if (ex instanceof IllegalPluginAccessException || ex instanceof InvalidPluginException || ex instanceof InvalidDescriptionException)
                                        UtilDatabase.getInstance().addIncompatiblePlugin(pluginInfo);
                                }
                            }).update();
                        }
                        //SHIFT-RIGHT-CLICK, REMOVE LINK
                    } else if (event.getClick() == ClickType.SHIFT_RIGHT) {
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
                                boolean updateFound = !resource.getLastVersion().equals(info.getLatestVersion());
                                info.setLatestVersion(resource.getLastVersion());
                                UpdateManager.getInstance().addLinkedPlugin(info);
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
                                              "&8CLICK &f| &a&oEnter an ID for a plugin to be downloaded & installed.",
                                              "",
                                              "&7Please note that this task is pretty intensive",
                                              "&7and may cause server lag while it is installing.")
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
     * Utilities
     */

    private void restoreFile(String oldFileNameResult, String pluginName, String version) {
        //Move cached old version back to plugins folder & enable
        File downloadedFile = new File(Up2Date.getInstance().getDataFolder().getParentFile().getPath()+"/"+pluginName+"-"+version);
        if (downloadedFile.exists()) {
            if (!downloadedFile.delete()) {
                Up2Date.getInstance().printPluginError("Error occurred while fixing failed plugin download.", "Corrupt download deletion failed.");
                return;
            }
        }

        //Unload invalid plugin.
        if (Bukkit.getPluginManager().getPlugin(pluginName) != null)
            UtilPlugin.unload(Bukkit.getPluginManager().getPlugin(pluginName));

        //Restore cached version
        if (oldFileNameResult != null) {
            File restoredPluginFile = new File(Up2Date.getInstance().getDataFolder().getParentFile().getPath()+"/"+pluginName+"-"+version+".jar");
            if (!new File(Up2Date.getInstance().getDataFolder().getAbsolutePath()+"/caches/"+pluginName+"/"+oldFileNameResult).renameTo(restoredPluginFile)) {
                Up2Date.getInstance().printPluginError("Error occurred while fixing failed plugin download.", "File move failed.");
                return;
            }

            try {

                Plugin reinitializedPlugin = Bukkit.getPluginManager().loadPlugin(restoredPluginFile);
                if (reinitializedPlugin != null)
                    Bukkit.getPluginManager().enablePlugin(reinitializedPlugin);
            } catch (InvalidPluginException | InvalidDescriptionException e) {
                //TODO add this to invalid db list & email plugin author.
                Up2Date.getInstance().printError(e, "Error occurred while fixing failed plugin download.");
            }


            try {
                FileUtils.deleteDirectory(new File(Up2Date.getInstance().getDataFolder().getPath()+"/caches/"+pluginName));
            } catch (IOException e) {
                Up2Date.getInstance().printError(e, "Error occurred while clearing caches.");
            }
        }
    }

    private String[] getLore(String[] varArgLines, String... description) {
        ArrayList<String> lines = new ArrayList<>();

        for (String line : description) {
            if (line.contains("%description%")) {
                for (String varArgLine : varArgLines)
                    lines.add("&d&l"+varArgLine);
            } else {
                lines.add(line);
            }
        }

        return lines.toArray(new String[0]);
    }

    private void updatePlugins(ArrayList<PluginInfo> updatesNeeded) {
        if (updatesNeeded.size() == 0) {
            player.closeInventory();
            UtilText.getUtil().sendActionBar("&d&lU&5&l2&d&lD &7&oNo plugins to update!", player);
            return;
        }

        new ConfirmGUI(
                "&dContinue?",

                //Confirm
                () -> {
                    player.closeInventory();
                    UpdateManager.getInstance().setCurrentTask(true);
                    ArrayList<PluginInfo> failedUpdates = new ArrayList<>();
                    ArrayList<PluginInfo> successfulUpdates = new ArrayList<>();

                    for (int i = 0; i < updatesNeeded.size(); i++) {
                        PluginInfo info = updatesNeeded.get(i);
                        if (updatesNeeded.get(i) == null)
                            updatesNeeded.remove(i);

                        Plugin plugin = Bukkit.getPluginManager().getPlugin(info.getName());

                        if (plugin == null) {
                            UpdateManager.getInstance().removeLinkedPlugin(info);
                            new MessageBuilder().addPlainText(Up2Date.getInstance().getMainConfig().getPrefix()+"&dThe plugin '&5"+info.getName()+"&d' is missing, it has been unlinked.");
                            continue;
                        }

                        if (plugin.getName().equalsIgnoreCase("Up2Date")) {
                            if (i+1 != updatesNeeded.size())
                                updatesNeeded.add(updatesNeeded.remove(0));

                            new PremiumUpdater(player, plugin, info.getId(), UpdateManager.getInstance().getUpdateLocale(), false, true, (success, ex, pl) -> {
                                UpdateManager.getInstance().setCurrentTask(false);
                                if (success) {
                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                    updatesAvailable.remove(info);

                                    UtilDatabase.getInstance().addDownloadedFiles(1);
                                    UtilDatabase.getInstance().addDownloadsize(getFileSize(plugin.getName()));
                                } else
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                            }).update();
                            continue;
                        }

                        String oldFileName = null;
                        try {
                            UtilPlugin.unload(plugin);

                            File oldVersion = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                            oldFileName = oldVersion.getName();
                            //Cache version in case update goes wrong.
                            File cacheFile = new File(Up2Date.getInstance().getDataFolder().getAbsolutePath()+"/caches/"+plugin.getName()+"/"+oldVersion.getName());
                            if (!cacheFile.getParentFile().mkdirs()) {
                                Up2Date.getInstance().printPluginError("Error occurred while caching old plugin version.", "Directory creation failed.");
                                return;
                            }
                            if (!oldVersion.renameTo(cacheFile)) {
                                Up2Date.getInstance().printPluginError("Error occurred while caching old plugin version.", "Rename failed.");
                                return;
                            }
                        } catch (URISyntaxException e) {
                            Up2Date.getInstance().printError(e, "Error occurred while caching old plugin version.");
                        }

                        final String oldFileNameResult = oldFileName;

                        final UpdaterRunnable runnable = (success, ex, pl) -> {
                            if (success) {
                                successfulUpdates.add(info);

                                UtilDatabase.getInstance().addDownloadedFiles(1);
                                UtilDatabase.getInstance().addDownloadsize(getFileSize(plugin.getName()));
                                //Just made updates to autoupdaterapi, need to test this code for statistics^^^
                            } else {
                                failedUpdates.add(info);
                                restoreFile(oldFileNameResult, plugin.getName(), plugin.getDescription().getVersion());
                                if (ex instanceof IllegalPluginAccessException || ex instanceof InvalidPluginException || ex instanceof InvalidDescriptionException) {
                                    UtilDatabase.getInstance().addIncompatiblePlugin(info);
                                }
                            }
                        };

                        final int index = i;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (successfulUpdates.size() + failedUpdates.size() == index) {
                                    if (info.isPremium()) {
                                        new PremiumUpdater(null, plugin, info.getId(), UpdateManager.getInstance().getUpdateLocale(), false, false, runnable).update();
                                    } else {
                                        new Updater(null, plugin, info.getId(), UpdateManager.getInstance().getUpdateLocale(), false, false, runnable).update();
                                    }
                                    cancel();
                                }
                            }
                        }.runTaskTimer(Up2Date.getInstance(), 0L, 30L);
                    }



                    final long startTime = System.currentTimeMillis();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            double percent = ((double) 100/updatesNeeded.size()) * (successfulUpdates.size() + failedUpdates.size());
                            UtilText.getUtil().sendActionBar("&d&lU&5&l2&d&lD &7&oUpdated "+(successfulUpdates.size()+failedUpdates.size())+"/"+updatesNeeded.size()+" plugins ("+String.format("%.2f", percent)+"%)", player);

                            UpdateManager.getInstance().setCurrentTask(false);

                            if (failedUpdates.size()+successfulUpdates.size() >= updatesNeeded.size()) {
                                if (successfulUpdates.size() == updatesNeeded.size()) {
                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                    UtilText.getUtil().sendActionBar("&d&lU&5&l2&d&lD &7&oSuccessfully updated all " + updatesNeeded.size() + " plugins in " + String.format("%.2f", ((double) (System.currentTimeMillis() - startTime) / 1000)) + " seconds.", player);
                                } else {
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                    UtilText.getUtil().sendActionBar("&d&lU&5&l2&d&lD &7&oSuccessfully updated " + successfulUpdates.size() + " plugins and failed " + failedUpdates.size() + " updates in" + String.format("%.2f", ((double) (System.currentTimeMillis() - startTime) / 1000)) + " seconds. &c&o(Check Console.)", player);
                                }

                                try {
                                    FileUtils.deleteDirectory(new File(Up2Date.getInstance().getDataFolder().getPath()+"/caches"));
                                } catch (IOException ex) {
                                    Up2Date.getInstance().printError(ex, "Error occurred while deleting caches directory recursively.");
                                }

                                if (updatesNeeded == updatesAvailable)
                                    updatesAvailable.removeAll(successfulUpdates);

                                new UpdateGUI(player).open(player);
                                cancel();
                            }
                        }
                    }.runTaskTimer(Up2Date.getInstance(), 5L, 40L);
                },

                //Cancel
                () -> new UpdateGUI(player).open(player),

                "&7By clicking '&a&lCONFIRM&7' the server",
                "&7will download and install &d"+updatesNeeded.size()+"&7 updates &oinstantly&7.",
                "&7we've made this as efficient as possible",
                " but still use common sense!").open(player);
    }

    private void updateSuccess(PluginInfo pluginInfo, Plugin plugin) {
        try {
            FileUtils.deleteDirectory(new File(Up2Date.getInstance().getDataFolder().getPath()+"/caches/"+pluginInfo.getName()));
        } catch (IOException e) {
            Up2Date.getInstance().printError(e, "Error occurred while clearing caches.");
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        updatesAvailable.remove(pluginInfo);

        UtilDatabase.getInstance().addDownloadedFiles(1);
        UtilDatabase.getInstance().addDownloadsize(getFileSize(plugin.getName(), pluginInfo.getLatestVersion()));
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
}
