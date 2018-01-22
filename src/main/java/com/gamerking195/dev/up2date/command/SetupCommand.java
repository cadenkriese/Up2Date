package com.gamerking195.dev.up2date.command;

import be.maximvdw.spigotsite.api.resource.Resource;
import com.gamerking195.dev.autoupdaterapi.AutoUpdaterAPI;
import com.gamerking195.dev.autoupdaterapi.PremiumUpdater;
import com.gamerking195.dev.autoupdaterapi.UpdateLocale;
import com.gamerking195.dev.autoupdaterapi.util.UtilReader;
import com.gamerking195.dev.up2date.Up2Date;
import com.gamerking195.dev.up2date.ui.PluginLinkGUI;
import com.gamerking195.dev.up2date.update.PluginInfo;
import com.gamerking195.dev.up2date.update.UpdateManager;
import com.gamerking195.dev.up2date.util.UtilDatabase;
import com.gamerking195.dev.up2date.util.UtilSiteSearch;
import com.gamerking195.dev.up2date.util.UtilText;
import com.gamerking195.dev.up2date.util.text.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by Caden Kriese (GamerKing195) on 8/14/17.
 * <p>
 * License is specified by the distributor which this
 * file was written for. Otherwise it can be found in the LICENSE file.
 */
public class SetupCommand implements CommandExecutor {

    public static boolean inSetup = false;

    private ArrayList<PluginInfo> linkedPlugins = new ArrayList<>();
    private HashMap<Plugin, ArrayList<UtilSiteSearch.SearchResult>> unlinkedPlugins = new HashMap<>();
    private ArrayList<Plugin> unknownPlugins = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length > 0) {

                if (Up2Date.getInstance().getMainConfig().isSetupComplete()) {
                    new MessageBuilder().addPlainText("&c&oSetup already complete!").sendToPlayersPrefixed(player);
                    return true;
                }

                if (args[0].equals("deny")) {
                    new MessageBuilder().addPlainText("&dSetup cancelled.").sendToPlayersPrefixed(player);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                } else if (args[0].equals("accept")) {
                    inSetup = true;
                    UtilText.getUtil().sendMultipleTitles(player, 20, 80, 20, "&dWelcome to &d&lU&5&l2&d&lD!\n&7&oThis setup wizard will help you get started.", "&a&lStep &2&o1/3\n&7Downloading dependencies.");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (Bukkit.getPluginManager().getPlugin("AutoUpdaterAPI") != null) {
                                if (AutoUpdaterAPI.getInstance().getCurrentUser() == null) {
                                    UtilText.getUtil().sendTitle("&a&lStep &2&o2/3", "&7Login to Spigot.", 20, 60, 20, player);
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                    new MessageBuilder().addPlainText("&dGood job! You're ahead of the game, we've detected you've &dalready installed the required dependencies so we'll skip &dstraight to the setup!").sendToPlayersPrefixed(player);
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                            new MessageBuilder().addPlainText("").sendToPlayers(player);
                                            new MessageBuilder().addPlainText("&dIf you want Up2Date to work with premium resources &d(including itself) you'll &dneed to login to Spigot.").sendToPlayersPrefixed(player);
                                            new MessageBuilder().addPlainText("&dDon't worry, we don't do anything with your credentials &d&dexcept login to Spigot.").sendToPlayersPrefixed(player);
                                            new MessageBuilder().addPlainText("&dThey will be locally stored but fully encrypted and hidden.").sendToPlayersPrefixed(player);
                                            new MessageBuilder().addPlainText("&dIf you don't need to use Up2Date for premium plugins you &dcan skip this step and finish the tutorial.").sendToPlayersPrefixed(player);
                                            new MessageBuilder().addPlainText("&dMay we proceed?").sendToPlayersPrefixed(player);

                                            new MessageBuilder().addHoverClickText("&2&l✔ &aYES", "&2&lPROCEED", "/stp authenticate", false).addPlainText("    &8&l|    ").addHoverClickText("&4&l✘ &cNO", "&4&lSKIP", "/stp skip", false).sendToPlayers(player);
                                        }
                                    }.runTaskLater(Up2Date.getInstance(), 100L);
                                } else {
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                    new MessageBuilder().addPlainText("&dGood job! You're ahead of the game, we've detected you've &dalready installed the required dependencies and set them up so &dwe'll skip straight to the plugin linking!").sendToPlayersPrefixed(player);
                                    beginStepThree(player);
                                }

                            } else {
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                new MessageBuilder().addPlainText("").sendToPlayers(player);
                                new MessageBuilder().addPlainText("&dUp2Date needs to download some dependencies.").sendToPlayersPrefixed(player);
                                new MessageBuilder().addPlainText("&dThis could take up to &520 &dseconds depending on your &dservers internet connection.").sendToPlayersPrefixed(player);
                                new MessageBuilder().addPlainText("&dThis will &oadd &51&d&o plugin to your plugin list&d called &dAutoUpdaterAPI.").sendToPlayersPrefixed(player);
                                new MessageBuilder().addPlainText("&dMay we proceed?").sendToPlayersPrefixed(player);
                                new MessageBuilder().addHoverClickText("&2&l✔ &aYES", "&2&lPROCEED", "/stp download", false).addPlainText("    &8&l|    ").addHoverClickText("&4&l✘ &cNO", "&4&lCANCEL", "/stp deny", false).sendToPlayers(player);
                            }
                        }
                    }.runTaskLater(Up2Date.getInstance(), 240);
                } else if (args[0].equals("download")) {
                    if (Bukkit.getServer().getPluginManager().getPlugin("AutoUpdaterAPI") != null) {
                        if (AutoUpdaterAPI.getInstance().getCurrentUser() != null) {
                            beginStepThree(player);

                            return true;
                        }
                    } else
                        downloadResources(player);
                } else if (args[0].equalsIgnoreCase("authenticate")) {
                    UpdateLocale locale = new UpdateLocale();
                    locale.setUpdating("&d&lU&5&l2&d&lD &7&oAuthenticating account...");
                    locale.setUpdatingNoVar("&d&lU&5&l2&d&lD &7&oAuthenticating account...");
                    locale.setUpdateFailed("&d&lU&5&l2&d&lD &c&oAuthentication failed! Check console.");
                    locale.setUpdateFailedNoVar("&d&lU&5&l2&d&lD &c&oAuthentication failed! Check console.");

                    new PremiumUpdater(player, Up2Date.getInstance(), 1, locale, false, false).authenticate(false);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (new File(Up2Date.getInstance().getDataFolder().getParentFile().getPath()+"/.creds").exists() && AutoUpdaterAPI.getInstance().getCurrentUser() != null) {
                                beginStepThree(player);

                                cancel();
                            }
                        }
                    }.runTaskTimer(Up2Date.getInstance(), 500L, 60L);
                } else if (args[0].equalsIgnoreCase("skip")) {
                    beginStepThree(player);
                } else if (args[0].equalsIgnoreCase("plugins")) {
                    parsePlugins(player);
                } else if (args[0].equalsIgnoreCase("manualparse")) {
                    new PluginLinkGUI(player).open(player);
                } else if (args[0].equals("finish")) {
                    player.closeInventory();
                    finishSetup(player);
                }
            }
        }

        return false;
    }

    private void finishSetup(Player player) {
        inSetup = false;
        UtilText.getUtil().sendTitle("&d&lU&5&l2&d&lD &8- &a&oSetup complete!", "&7Thanks for downloading &dUp&52&dDate&7!", 20, 60, 20, player);
        Up2Date.getInstance().getMainConfig().setSetupComplete(true);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

        for (int i = 0; i < 100; i++)
            player.sendMessage(ChatColor.RED+"");


        new MessageBuilder().addPlainText("&dThanks again for purchasing Up2Date! The plugin is now &dfully setup and ready to roll.").sendToPlayersPrefixed(player);
        new MessageBuilder().addPlainText("&dYou can start using the plugin by typing ").addHoverClickText("&5/u2d", "&7Click to run the command.", "/u2d", true).addPlainText("&d.").sendToPlayersPrefixed(player);
        //TODO even more messages here.
    }

    private void beginStepThree(Player player) {
        UtilText.getUtil().sendTitle("&a&lStep &2&o3/3", "&7Plugins setup.", 20, 60, 20, player);

        new BukkitRunnable() {
            @Override
            public void run() {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                new MessageBuilder().addPlainText("").sendToPlayers(player);
                new MessageBuilder().addPlainText("&dUp2Date needs to parse through your plugins to establish &dlinks from the server side plugin to a spigot web-page.").sendToPlayersPrefixed(player);
                new MessageBuilder().addPlainText("&dWe will run a &oVERY &dintensive algorithm to establish as many &dlinks as we can on our own.").sendToPlayersPrefixed(player);
                new MessageBuilder().addPlainText("&dWe highly recommend that you have anyone else leave the &dserver (you must stay) and don't move or do anything while this &druns.").sendToPlayersPrefixed(player);
                new MessageBuilder().addPlainText("&dThis could take up to &52&d minutes depending on how many &dplugins you have.").sendToPlayersPrefixed(player);
                new MessageBuilder().addHoverClickText("&2&l✔ &aYES", "&2&lPROCEED", "/stp plugins", false).addPlainText("    &8&l|    ").addHoverClickText("&4&l✘ &cNO", "&4&lCANCEL", "/stp deny", false).sendToPlayers(player);
            }
        }.runTaskLater(Up2Date.getInstance(), 95L);
    }

    private void parsePlugins(Player player) {
        linkedPlugins = new ArrayList<>();
        unlinkedPlugins = new HashMap<>();
        unknownPlugins = new ArrayList<>();

        List<Plugin> currentPlugins = new LinkedList<>(Arrays.asList(Bukkit.getPluginManager().getPlugins()));

        currentPlugins.remove(AutoUpdaterAPI.getInstance());

        ExecutorService pool = Up2Date.getInstance().getFixedThreadPool();

        long startTime = System.currentTimeMillis();

        for (final Plugin plugin : currentPlugins) {
            if (plugin.getName().equals("AutoUpdaterAPI") || plugin.getName().equals("Up2Date"))
                continue;

            pool.submit(() -> {

                ArrayList<UtilSiteSearch.SearchResult> searchResults = UtilSiteSearch.getInstance().searchResources(plugin.getName(), 5);

                if (searchResults == null || searchResults.size() == 0) {
                    unknownPlugins.add(plugin);
                    return;
                }

                HashMap<UtilSiteSearch.SearchResult, Integer> priorities = new HashMap<>();

                for (UtilSiteSearch.SearchResult result : searchResults) {
                    int priority = 0;

                    if (searchResults.size() == 1)
                        priority += 4;
                    else if (plugin.getDescription().getWebsite() != null && plugin.getDescription().getWebsite().contains(String.valueOf(result.getId())))
                        priority += 3;
                    else if (plugin.getDescription().getDescription() != null && plugin.getDescription().getDescription().equalsIgnoreCase(result.getTag()))
                        priority += 2;
                    else if (plugin.getName().equals(result.getName()))
                        priority += 1;

                    try {
                        String pluginJson = UtilReader.readFrom("https://api.spiget.org/v2/resources/"+result.getId());

                        if (pluginJson.contains("\"external\": true")) {
                            priority = -1;
                        } else if (pluginJson.contains("\"premium\": true")) {
                            result.setPremium(true);

                            if (AutoUpdaterAPI.getInstance().getCurrentUser() == null)
                                priority = -1;

                        } else if (!pluginJson.contains("\"type\": \".jar\"")) {
                            priority = -1;
                        }
                    } catch (IOException ex) {
                        Up2Date.getInstance().printError(ex, "Error occurred while validating search result for '"+plugin.getName()+"'");
                    }

                    priorities.put(result, priority);
                }

                parsePriorities(priorities, plugin, searchResults);
            });
        }

        //Repeating until all plugin data received.
        new BukkitRunnable() {
            @Override
            public void run() {
                double percent = ((double) 100/currentPlugins.size()) * (unlinkedPlugins.size() + linkedPlugins.size() + unknownPlugins.size());
                UtilText.getUtil().sendActionBar("&d&lU&5&l2&d&lD &7&oRetrieved plugin data for "+(unlinkedPlugins.size()+linkedPlugins.size()+unknownPlugins.size())+"/"+(currentPlugins.size())+" plugins ("+String.format("%.2f", percent)+"%)", player);
                if (unlinkedPlugins.size()+linkedPlugins.size()+unknownPlugins.size() == currentPlugins.size()) {

                    UtilText.getUtil().sendActionBar("&d&lU&5&l2&d&lD &7&oRetrieved plugin data for "+currentPlugins.size()+" plugins in "+String.format("%.2f", ((double)(System.currentTimeMillis()-startTime)/1000)) +"s", player);

                    if (linkedPlugins.size() > 0)
                        linkedPlugins.removeAll(UtilDatabase.getInstance().getIncompatiblePlugins(linkedPlugins));

                    UpdateManager.getInstance().setLinkedPlugins(linkedPlugins);
                    UpdateManager.getInstance().setUnlinkedPlugins(unlinkedPlugins);
                    UpdateManager.getInstance().setUnknownPlugins(unknownPlugins);

                    if (linkedPlugins.size() == Bukkit.getPluginManager().getPlugins().length) {
                        UpdateManager.getInstance().saveData();

                        finishSetup(player);
                        return;
                    }

                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    new MessageBuilder().addPlainText("").sendToPlayers(player);
                    new MessageBuilder().addPlainText("&dUp2Date has parsed through your plugins in order to link &dthem to actual spigot plugins.").sendToPlayersPrefixed(player);
                    new MessageBuilder().addPlainText("&dHowever since this is only a program we can't obtain the &ddata for all of your plugins perfectly.").sendToPlayersPrefixed(player);

                    String notMatched = unknownPlugins.size() == 0 ? "" : "find &5"+ unknownPlugins.size()+"&d "+UtilText.getUtil().getEnding("plugin", unknownPlugins.size(), false)+" that will need to be &dmanually setup.";
                    String partiallyMatched = unlinkedPlugins.size() == 0 ? "" : "partially match &5"+unlinkedPlugins.size()+" &d"+UtilText.getUtil().getEnding("plugin", unlinkedPlugins.size(), false);
                    String perfectlyMatched = linkedPlugins.size() == 0 ? "" : "perfectly match &5"+linkedPlugins.size()+"&d "+UtilText.getUtil().getEnding("plugin", linkedPlugins.size(), false);

                    if (notMatched.length() > 0 || partiallyMatched.length() > 0)
                        perfectlyMatched += ", ";

                    if (notMatched.length() > 0)
                        partiallyMatched += ", ";

                    if (perfectlyMatched.length() > 0  || partiallyMatched.length() > 0)
                        notMatched = "however there's still &5"+ unknownPlugins.size()+"&d "+UtilText.getUtil().getEnding("plugin", unknownPlugins.size(), false)+" that will need to be &dmanually setup.";

                    new MessageBuilder().addPlainText("&dWe managed to "+perfectlyMatched+partiallyMatched+notMatched).sendToPlayersPrefixed(player);
                    new MessageBuilder().addPlainText("&dWe need you to go in and manually tell us which plugins &dmatch which search result, and for those that have no search &dresults, you can either provide their ID or have Up2Date ignore &dthem.").sendToPlayersPrefixed(player);
                    new MessageBuilder().addPlainText("&dWe apologize that this is a rather complex task; just know &dthat you'll only have to do it once. You can even copy the data &dfile between similar servers or use a database.").sendToPlayersPrefixed(player);
                    new MessageBuilder().addPlainText("").sendToPlayers(player);
                    new MessageBuilder().addPlainText("&dIf you ever get stuck you can visit the Spigot page ").addURLText("&5&n&ohere", "https://www.spigotmc.org/resources/up2date.49313/").addPlainText("&d or &dthe wiki ").addURLText("&5&n&ohere", "https://github.com/GamerKing195/Up2Date/wiki").addPlainText("&d or watch our youtube tutorial ").addURLText("&5&n&ohere&d.", "https://youtu.be/gSnFSRUTqGU").sendToPlayersPrefixed(player);
                    new MessageBuilder().addPlainText("&dAre you ready to begin?").sendToPlayersPrefixed(player);
                    new MessageBuilder().addHoverClickText("&2&l✔ &aYES", "&2&lPROCEED", "/stp manualparse", false).addPlainText("    &8&l|    ").addHoverClickText("&4&l✘ &cNO", "&4&lCANCEL", "/stp deny", false).sendToPlayers(player);
                    cancel();
                }
            }
        }.runTaskTimer(Up2Date.getInstance(), 10L, 20L);
        //Don't put a significant first delay so that the player gets constant actionbars of updates.
    }

    private void downloadResources(Player player) {
        try {
            Up2Date plugin = Up2Date.getInstance();
            URL url = new URL("https://api.spiget.org/v2/resonew urces/39719/download");
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestProperty("User-Agent", "SpigetResourceUpdater");
            long completeFileSize = httpConnection.getContentLength();

            BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());

            File location = new File(plugin.getDataFolder().getParentFile().getPath() + "/AutoUpdaterAPI.jar");
            location.getParentFile().mkdirs();

            FileOutputStream fos = new FileOutputStream(location);
            BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);

            byte[] data = new byte[1024];
            long downloadedFileSize = 0;
            int x;

            long interval = 10000;
            while ((x = in.read(data, 0, 1024)) >= 0) {
                downloadedFileSize += x;
                interval -= x;

                if (interval <= 0) {
                    final int currentProgress = (int) ((((double) downloadedFileSize) / ((double) completeFileSize)) * 15);

                    final String currentPercent = String.format("%.2f", (((double) downloadedFileSize) / ((double) completeFileSize)) * 100);

                    String bar = "&a:::::::::::::::";

                    bar = bar.substring(0, currentProgress + 2) + "&c" + bar.substring(currentProgress + 2);

                    UtilText.getUtil().sendTitle("&dDownloading &5&oAutoUpdaterAPI", "&8| " + bar + " &8| &2&l" + currentPercent + "%", 0, 20, 0, player);
                    interval = 10000;
                }

                bout.write(data, 0, x);
            }

            bout.close();
            in.close();

            UtilText.getUtil().sendTitle("&dInitializing &5&oAutoUpdaterAPI", 20, 10000, 0, player);

            Plugin target = Bukkit.getPluginManager().loadPlugin(location);
            Bukkit.getPluginManager().enablePlugin(target);

            //Soooooo I know this looks useless but I guess the plugin takes longer to initialize than I thought bc it has to make connections to spigot
            //soooo I made an extra delay for the title bc it makes it fit in with all the other titles so yeah :)
            new BukkitRunnable() {
                @Override
                public void run() {
                    UtilText.getUtil().sendTitle("&a&lStep &2&o2/3", "&7Login to Spigot.", 20, 60, 20, player);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                            new MessageBuilder().addPlainText("").sendToPlayers(player);
                            new MessageBuilder().addPlainText("&dIf you want Up2Date to work with premium resources you'll &dneed to login to Spigot.").sendToPlayersPrefixed(player);
                            new MessageBuilder().addPlainText("&dDon't worry, we don't do anything with your credentials &dexcept login to Spigot.").sendToPlayersPrefixed(player);
                            new MessageBuilder().addPlainText("&dThey will be locally stored but fully encrypted and hidden.").sendToPlayersPrefixed(player);
                            new MessageBuilder().addPlainText("&dIf you don't need to use Up2Date for Premium plugins you &dcan skip this step and finish the tutorial.").sendToPlayersPrefixed(player);
                            new MessageBuilder().addPlainText("&dMay we proceed?").sendToPlayersPrefixed(player);

                            new MessageBuilder().addHoverClickText("&2&l✔ &aYES", "&2&lPROCEED", "/stp authenticate", false).addPlainText("    &8&l|    ").addHoverClickText("&4&l✘ &cNO", "&4&lSKIP", "/stp skip", false).sendToPlayers(player);
                        }
                    }.runTaskLater(Up2Date.getInstance(), 100L);
                }
            }.runTaskLater(Up2Date.getInstance(), 100L);
        } catch (Exception ex) {
            Up2Date.getInstance().printError(ex, "Error occurred while downloading dependencies.");
        }
    }

    private void parsePriorities(HashMap<UtilSiteSearch.SearchResult, Integer> priorities, Plugin plugin, ArrayList<UtilSiteSearch.SearchResult> searchResults) {
        UtilSiteSearch.SearchResult highestResult = null;

        for (UtilSiteSearch.SearchResult result : priorities.keySet()) {
            if ((highestResult == null || priorities.get(result) > priorities.get(highestResult)) && priorities.get(result) >= 0)
                highestResult = result;
        }

        if (highestResult != null) {
            if (priorities.size() > 0 && priorities.get(highestResult) > 0) {
                try {
                    if (AutoUpdaterAPI.getInstance().getApi() == null) {
                        unknownPlugins.add(plugin);
                        return;
                    }

                    Resource resource = AutoUpdaterAPI
                                                .getInstance()
                                                .getApi()
                                                .getResourceManager()
                                                .getResourceById(
                                                        highestResult
                                                                .getId()
                                                        , AutoUpdaterAPI
                                                                  .getInstance()
                                                                  .getCurrentUser());

                    if (resource != null)
                        linkedPlugins.add(new PluginInfo(plugin, resource, highestResult));
                    else {
                        priorities.remove(highestResult);
                        searchResults.remove(highestResult);
                        parsePriorities(priorities, plugin, searchResults);
                    }
                } catch (Exception e) {
                    unknownPlugins.add(plugin);
                    Up2Date.getInstance().printError(e, "Error occurred while pinging Spigot!");
                }
            }
            else if (priorities.size()-1 == 0)
                unknownPlugins.add(plugin);
            else
                unlinkedPlugins.put(plugin, searchResults);
        } else
            unknownPlugins.add(plugin);
    }
}
