package com.gamerking195.dev.up2date.ui;

import be.maximvdw.spigotsite.api.resource.Resource;
import be.maximvdw.spigotsite.api.resource.ResourceManager;
import com.gamerking195.dev.autoupdaterapi.AutoUpdaterAPI;
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
import java.util.HashMap;
import java.util.List;

/**
 * @author Caden Kriese (flogic)
 * <p>
 * Created on 9/2/17
 */
public class PluginLinkGUI extends PageGUI {

    private Player player;
    private ResourceManager manager;


    public PluginLinkGUI(Player player) {
        super("&d&lU&5&l2&d&lD &8- &dLink plugins", 54);

        manager = AutoUpdaterAPI.getInstance().getApi().getResourceManager();
        this.player = player;
    }

    @Override
    protected List<ItemStack> getIcons() {
        ArrayList<PluginInfo> linkedPlugins = UpdateManager.getInstance().getLinkedPlugins();
        HashMap<Plugin, ArrayList<UtilSiteSearch.SearchResult>> unlinkedPlugins = UpdateManager.getInstance().getUnlinkedPlugins();
        ArrayList<Plugin> unknownPlugins = UpdateManager.getInstance().getUnknownPlugins();

        List<ItemStack> stackList = new ArrayList<>();

        for (PluginInfo pluginInfo : linkedPlugins) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginInfo.getName());

            try {

                stackList.add(new ItemStackBuilder(Material.STAINED_CLAY)
                                      .setDurability((short) 5)
                                      .setName("&f&l" + plugin.getName().toUpperCase())
                                      .setLore(getLore(WordUtils.wrap(pluginInfo.getDescription() != null ? pluginInfo.getDescription() : "None", 40, "%new%", false).split("%new%"),
                                              "",
                                              "&7&lAuthor: &d&l" + pluginInfo.getAuthor(),
                                              "&7&lVersion: &d&l" + plugin.getDescription().getVersion(),
                                              "&7&lSupported MC Versions: &d&l" + pluginInfo.getSupportedMcVersions(),
                                              "&7&lID: &d&l" + pluginInfo.getId(),
                                              "&7&lDescription: ",
                                              "%description%",
                                              "",
                                              "&2&lLINKED",
                                              "",
                                              "&8RIGHT-CLICK &f| &a&oChange ID",
                                              "&8SHIFT-RIGHT-CLICK &f| &c&oDelete Link"))
                                      .build());
            } catch (Exception ex) {
                Up2Date.getInstance().printError(ex, "Error occurred while retrieving extra information for '" + plugin.getName() + "' #1");
            }
        }

        for (Plugin plugin : unlinkedPlugins.keySet()) {

            ArrayList<UtilSiteSearch.SearchResult> results = unlinkedPlugins.get(plugin);

            stackList.add(new ItemStackBuilder(Material.STAINED_CLAY)
                                  .setDurability((short) 4)
                                  .setName("&f&l" + plugin.getName().toUpperCase())
                                  .setLore(getLore(WordUtils.wrap(plugin.getDescription().getDescription() != null ? plugin.getDescription().getDescription() : "None", 40, "%new%", false).split("%new%"),
                                          "",
                                          "&7&lSearch Results: &d&l" + results.size(),
                                          "&7&lDescription: ",
                                          "%description%",
                                          "",
                                          "&6&lUNLINKED",
                                          "",
                                          "&8LEFT-CLICK &f| &a&oView Results",
                                          "&8RIGHT-CLICK &f| &a&oEnter ID",
                                          "&8SHIFT-RIGHT-CLICK &f| &c&oDelete Link"))
                                  .build());
        }

        for (Plugin plugin : unknownPlugins) {
            stackList.add(new ItemStackBuilder(Material.STAINED_CLAY)
                                  .setDurability((short) 14)
                                  .setName("&f&l" + plugin.getName().toUpperCase())
                                  .setLore(getLore(WordUtils.wrap(plugin.getDescription().getDescription() != null ? plugin.getDescription().getDescription() : "None", 40, "%new%", false).split("%new%"),
                                          "",
                                          "&7&lDescription: ",
                                          "%description%",
                                          "",
                                          "&4&lUNKNOWN",
                                          "",
                                          "&8LEFT-CLICK &f| &a&oEnter ID",
                                          "&8SHIFT-RIGHT-CLICK &f| &c&oDelete Link")).build());
        }

        return stackList;
    }

    private String[] getLore(String[] varArgLines, String... description) {
        ArrayList<String> lines = new ArrayList<>();

        for (String line : description) {
            if (line.contains("%description%")) {
                for (String varArgLine : varArgLines)
                    lines.add("&d&l" + varArgLine);
            } else
                lines.add(line);
        }

        return lines.toArray(new String[0]);
    }

    @Override
    protected void onPlayerClickIcon(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() != null && event.getCurrentItem().getType() == Material.STAINED_CLAY) {

            event.setCancelled(true);

            String pluginName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName().toLowerCase());
            if (getCorrectPluginName(pluginName) != null)
                pluginName = getCorrectPluginName(pluginName);
            else return;


            //5 = Green, 4 = Yellow, 14 = Red

            //Green = Right | Change ID, Yellow = Right | Change ID, Left | Select result, Red = Left | Change ID

            if (event.getCurrentItem().getDurability() == 5) {

                if (event.getClick() == ClickType.RIGHT)
                    changeId(Bukkit.getServer().getPluginManager().getPlugin(pluginName));

                else if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginName);
                    new ConfirmGUI("&dDelete '&5" + plugin.getName() + "&d'?", () -> {
                        UpdateManager.getInstance().removeLinkedPlugin(plugin);
                        new PluginLinkGUI(player).open(player);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                    }, () -> new PluginLinkGUI(player).open(player),
                            "&7Click '&a&lCONFIRM&7' to remove",
                            "&7this plugin from the list of",
                            "&7linked plugins.",
                            "&7Don't worry you can add it back later.",
                            "&7You can also click '&c&lCANCEL&7' to return",
                            "&7to the overview GUI."
                    ).open(player);
                }
            } else if (event.getCurrentItem().getDurability() == 4) {
                if (event.getClick() == ClickType.RIGHT)
                    changeId(Bukkit.getServer().getPluginManager().getPlugin(pluginName));

                else if (event.getClick() == ClickType.LEFT)
                    new SelectResultGUI(player, Bukkit.getServer().getPluginManager().getPlugin(pluginName)).open(player);

                else if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginName);
                    new ConfirmGUI("&dDelete '&5" + plugin.getName() + "&d'?", () -> {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                        UpdateManager.getInstance().removeUnlinkedPlugin(plugin);
                        new PluginLinkGUI(player).open(player);
                    }, () -> new PluginLinkGUI(player).open(player),
                            "&7Click '&a&lCONFIRM&7' to remove",
                            "&7this plugin from the list of",
                            "&7linked plugins.",
                            "&7Don't worry you can add it back later.",
                            "&7You can also click '&c&lCANCEL&7' to return",
                            "&7to the overview GUI."
                    ).open(player);
                }
            } else if (event.getCurrentItem().getDurability() == 14) {

                if (event.getClick() == ClickType.LEFT)
                    changeId(Bukkit.getServer().getPluginManager().getPlugin(pluginName));

                else if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginName);
                    new ConfirmGUI("&dDelete '&5" + plugin.getName() + "&d'?", () -> {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                        UpdateManager.getInstance().removeUnknownPlugin(plugin);
                        new PluginLinkGUI(player).open(player);
                    }, () -> new PluginLinkGUI(player).open(player),
                            "&7Click '&a&lCONFIRM&7' to remove",
                            "&7this plugin from the list of",
                            "&7linked plugins.",
                            "&7Don't worry you can add it back later.",
                            "&7You can also click '&c&lCANCEL&7' to return",
                            "&7to the overview GUI."
                    ).open(player);
                }
            }
        } else if (event.getCurrentItem() != null && event.getCurrentItem().getType() != null && event.getCurrentItem().getType() == Material.EMERALD_BLOCK) {
            //TODO add again once incompatibility tracking works.
//            ArrayList<PluginInfo> incompatibles = UtilStatisticsDatabase.getInstance().getIncompatiblePlugins(UpdateManager.getInstance().getLinkedPlugins());
//            incompatibles.forEach(plugin -> UpdateManager.getInstance().removeLinkedPlugin(plugin));
//
//            StringBuilder list = new StringBuilder();
//            incompatibles.forEach(plugin -> list.append("\n&d- ").append(plugin.getName()));
//
//            if (incompatibles.size() > 0)
//                new MessageBuilder().addHoverText("&dWe noticed there were &5"+incompatibles.size()+" &dknown incompatible "+ UtilText.getUtil().getEnding("plugin", incompatibles.size(), false)+" we've &dautomatically &dremoved &dthem. &7&o(Hover for List)", "&5Known Incompatible Plugins:"+list.toString()).sendToPlayersPrefixed(player);

            UpdateManager.getInstance().saveData();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            player.performCommand("stp finish");
        }
    }

    private void changeId(Plugin plugin) {
        new AnvilGUI(Up2Date.getInstance(), player, "Enter plugin ID", (player, reply) -> {
            if (NumberUtils.isNumber(reply)) {
                try {
                    String pluginJson = UtilReader.readFrom("https://api.spiget.org/v2/resources/" + reply);

                    boolean premium = pluginJson.contains("\"premium\": true");

                    if (pluginJson.contains("\"external\": true")) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                        return "External downloads not supported!";
                    } else if (premium && AutoUpdaterAPI.getInstance().getCurrentUser() == null) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                        return "You must login to spigot for premium resources!";
                    } else if (!pluginJson.contains("\"type\": \".jar\"")) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                        return "Resource download type must be a jar!";
                    }

                    Resource resource = manager.getResourceById(Integer.valueOf(reply));
                    if (resource != null) {
                        UtilSiteSearch.SearchResult result = new UtilSiteSearch.SearchResult(resource.getResourceId(), plugin.getName(), plugin.getDescription().getDescription(), premium, new String[]{"NULL"});

                        UpdateManager.getInstance().removeLinkedPlugin(plugin);
                        UpdateManager.getInstance().removeUnlinkedPlugin(plugin);
                        UpdateManager.getInstance().removeUnknownPlugin(plugin);

                        UpdateManager.getInstance().addLinkedPlugin(new PluginInfo(plugin, resource, result));

                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        new PluginLinkGUI(player).open(player);
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
    }

    @Override
    protected void populateSpecial() {
        inventory.setItem(51, new ItemStackBuilder(Material.EMERALD_BLOCK).setName("&2&lSAVE").setLore("&aClick to save this information", "&ato a flatfile or database. (Defined in the config.yml)").build());
    }

    private String getCorrectPluginName(String pluginName) {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getName().equalsIgnoreCase(pluginName))
                return plugin.getName();
        }

        return null;
    }
}
