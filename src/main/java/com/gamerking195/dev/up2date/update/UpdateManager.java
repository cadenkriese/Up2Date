package com.gamerking195.dev.up2date.update;

import be.maximvdw.spigotsite.api.exceptions.ConnectionFailedException;
import be.maximvdw.spigotsite.api.resource.Resource;
import com.gamerking195.dev.autoupdaterapi.AutoUpdaterAPI;
import com.gamerking195.dev.autoupdaterapi.UpdateLocale;
import com.gamerking195.dev.autoupdaterapi.util.UtilReader;
import com.gamerking195.dev.up2date.Up2Date;
import com.gamerking195.dev.up2date.config.DataConfig;
import com.gamerking195.dev.up2date.util.UtilSQL;
import com.gamerking195.dev.up2date.util.UtilSiteSearch;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Caden Kriese (GamerKing195) on 9/2/17.
 * <p>
 * License is specified by the distributor which this
 * file was written for. Otherwise it can be found in the LICENSE file.
 * If there is no license file the code is then completely copyrighted
 * and you must contact me before using it IN ANY WAY.
 */
public class UpdateManager {

    private UpdateManager() {}

    @Getter
    private static UpdateManager instance = new UpdateManager();

    @Getter
    @Setter
    private ArrayList<PluginInfo> linkedPlugins = new ArrayList<>();
    @Getter
    @Setter
    private HashMap<Plugin, ArrayList<UtilSiteSearch.SearchResult>> unlinkedPlugins = new HashMap<>();
    @Getter
    @Setter
    private ArrayList<Plugin> unknownPlugins = new ArrayList<>();

    //Total refresh time is basedelay+(count*60)
    @Getter
    private ArrayList<BukkitRunnable> cacheUpdaters = new ArrayList<>();

    @Getter
    @Setter
    private boolean currentTask = false;

    public void init() {
        if (Up2Date.getInstance().getMainConfig().isEnableSQL()) {
            UtilSQL.getInstance().init();

            new BukkitRunnable() {
                @Override
                public void run() {
                    ResultSet resultSet = UtilSQL.getInstance().runQuery("SELECT * FROM TABLENAME");
                    ArrayList<PluginInfo> info = new ArrayList<>();

                    if (resultSet != null) {
                        try {
                            while (resultSet.next())
                                info.add(new PluginInfo(resultSet.getString("name"), resultSet.getInt("id"), resultSet.getString("author"), resultSet.getString("version"), resultSet.getString("description"), resultSet.getBoolean("premium")));

                            resultSet.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    linkedPlugins = info;
                }
            }.runTaskAsynchronously(Up2Date.getInstance());
        } else {
            DataConfig.getConfig().init();
            linkedPlugins = DataConfig.getConfig().getFile();
        }

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            boolean linked = false;
            for (PluginInfo info : linkedPlugins) {
                if (info.getName().equals(plugin.getName()))
                    linked = true;
            }

            if (Up2Date.getInstance().getMainConfig().isSetupComplete() && !linked && !plugin.getName().equals("Up2Date") && !plugin.getName().equals("AutoUpdaterAPI"))
                unknownPlugins.add(plugin);
        }

        int startDelay = 100;
        if (!Up2Date.getInstance().getMainConfig().isSetupComplete())
            startDelay+= 2400;

        //Not using threadpool here to have static seperations and spread out any server stress more since its in the background.
        for (int i = 0; i < linkedPlugins.size()*40; i+= 40) {
            final int index = i;
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    if (Bukkit.getPluginManager().getPlugin("AutoUpdaterAPI") == null)
                        return;

                    try {
                        PluginInfo info = linkedPlugins.get(index/20);

                        if (UtilReader.readFrom("https://www.spigotmc.org/resources/"+info.getId()+"/").contains("You do not have permission to view this page or perform this action."))
                            return;

                        Resource resource;
                        if (AutoUpdaterAPI.getInstance().getCurrentUser() != null) {
                            resource = AutoUpdaterAPI.getInstance().getApi().getResourceManager().getResourceById(info.getId(), AutoUpdaterAPI.getInstance().getCurrentUser());
                        } else {
                            resource = AutoUpdaterAPI.getInstance().getApi().getResourceManager().getResourceById(info.getId());
                        }

                        linkedPlugins.remove(index/20);
                        info.setLatestVersion(resource.getLastVersion());
                        linkedPlugins.add(info);
                    } catch (ConnectionFailedException | IOException ex) {
                        Up2Date.getInstance().systemOutPrintError(ex, "Error occurred while updating info for '"+linkedPlugins.get(index/20).getName()+"'");
                    }

                    if (index == linkedPlugins.size()*40)
                        saveData();
                }
            };

            cacheUpdaters.add(runnable);
            runnable.runTaskTimerAsynchronously(Up2Date.getInstance(), Up2Date.getInstance().getMainConfig().getCacheRefreshDelay()*20*60+startDelay+i, (Up2Date.getInstance().getMainConfig().getCacheRefreshDelay()*20*60)+i);
        }
    }

    /*
     * Data Utilities
     */

    public void saveData() {
        //TODO SQL
        if (Up2Date.getInstance().getMainConfig().isEnableSQL()) {
            for (PluginInfo info : linkedPlugins) {
                UtilSQL.getInstance().runStatement("INSERT INTO TABLENAME (id, name, author, version, description, premium) VALUES ('" + info.getId() + "','" + info.getName() + "', '" + info.getAuthor() + "', '" + info.getLatestVersion() + "', '" + info.getDescription() + "', '" + info.isPremium() + "') ON DUPLICATE KEY UPDATE name = '" + info.getName()+"', author = '"+info.getAuthor()+"', version = '"+info.getLatestVersion()+"', description = '"+info.getDescription()+"', premium = '"+info.isPremium()+"'");
            }
        }

        //TODO Flatfile
        else {
            linkedPlugins.forEach((pluginInfo) -> DataConfig.getConfig().writeInfoToFile(pluginInfo));
            DataConfig.getConfig().saveFile();
        }
    }

    public void saveDataNow() {
        //TODO SQL
        if (Up2Date.getInstance().getMainConfig().isEnableSQL()) {
            for (PluginInfo info : linkedPlugins) {
                UtilSQL.getInstance().runStatementSync("INSERT INTO TABLENAME (id, name, author, version, description, premium) VALUES ('" + info.getId() + "','" + info.getName() + "', '" + info.getAuthor() + "', '" + info.getLatestVersion() + "', '" + info.getDescription() + "', '" + info.isPremium() + "') ON DUPLICATE KEY UPDATE name = '" + info.getName()+"', author = '"+info.getAuthor()+"', version = '"+info.getLatestVersion()+"', description = '"+info.getDescription()+"', premium = '"+info.isPremium()+"'");
            }
        }

        //TODO Flatfile
        else {
            linkedPlugins.forEach((pluginInfo) -> DataConfig.getConfig().writeInfoToFile(pluginInfo));
            DataConfig.getConfig().saveFile();
        }
    }

    public void swapData(boolean toDatabase) {
        if (toDatabase) {
            DataConfig.getConfig().saveFile();

            UtilSQL.getInstance().init();

            ArrayList<PluginInfo> currentFile = DataConfig.getConfig().getFile();

            Iterator<PluginInfo> iterator = currentFile.iterator();

            StringBuilder statement = new StringBuilder("INSERT INTO TABLENAME (name, id, author, version, description, premium) VALUES ");

            int i = 0;

            while (iterator.hasNext()) {
                PluginInfo info = iterator.next();

                statement.append("('").append(info.getName()).append("', ").append(info.getId()).append(", ").append(info.getAuthor()).append(", ").append(info.getDescription()).append(", ").append(info.isPremium()).append(")");

                i++;

                if (i % 100 == 0) {
                    UtilSQL.getInstance().runStatement(statement.toString());

                    statement = new StringBuilder("INSERT INTO TABLENAME (name, id, author, version, description, premium) VALUES ");
                } else if (iterator.hasNext() || i + 1 % 100 == 0) {
                    statement.append(", ");
                }
            }

            if (i % 100 != 0)
                UtilSQL.getInstance().runStatement(statement.toString());
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    ResultSet rs = UtilSQL.getInstance().runQuery("SELECT * FROM TABLENAME");

                    ArrayList<PluginInfo> info = new ArrayList<>();

                    if (rs != null) {
                        try {
                            while (rs.next()) {
                                info.add(new PluginInfo(rs.getString("name"), rs.getInt("id"), rs.getString("author"), rs.getString("version"), rs.getString("description"), rs.getBoolean("premium")));
                            }

                            rs.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    UtilSQL.getInstance().runStatementSync("DROP TABLE IF EXISTS TABLENAME CASCADE");

                    //Switch back to main thread.
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            DataConfig.getConfig().setFile(info);
                        }
                    }.runTask(Up2Date.getInstance());
                }
            }.runTaskAsynchronously(Up2Date.getInstance());
        }
    }

    /*
    * ADDERS
    */
    public void addLinkedPlugin(PluginInfo info) {
        linkedPlugins.add(info);
    }

    public void addUnlinkedPlugin(Plugin plugin, ArrayList<UtilSiteSearch.SearchResult> results) {
        unlinkedPlugins.put(plugin, results);
    }

    public void addUnknownPlugin(Plugin plugin) {
        unknownPlugins.add(plugin);
    }

    /*
     * REMOVERS
     */
    public void removeLinkedPlugin(Plugin plugin) {
        PluginInfo info = null;

        for (PluginInfo pluginInfo : linkedPlugins) {
            if (pluginInfo.getName().equals(plugin.getName()))
                info = pluginInfo;
        }

        if (info != null)
            linkedPlugins.remove(info);
    }

    public void removeLinkedPlugin(PluginInfo info) {
        linkedPlugins.remove(info);
    }

    public void removeUnlinkedPlugin(Plugin plugin) {
        unlinkedPlugins.remove(plugin);
    }

    public void removeUnknownPlugin(Plugin plugin) {
        unknownPlugins.remove(plugin);
    }

    /*
     * GETTERS
     */

    public UpdateLocale getUpdateLocale() {
        UpdateLocale locale = new UpdateLocale();

        locale.setFileName("%plugin%-%new_version%");
        locale.setPluginName("%plugin%");

//        locale.setUpdating("&f&lUPDATING &5&l%plugin% &d&lV%old_version% &a&l» &d&l%new_version%");
//        locale.setUpdatingDownload("&f&lUPDATING &5&l%plugin% &d&lV%old_version% &a&l» &b&lV%new_version% &8| %download_bar% &8| &d%download_percent%");
//        locale.setUpdateComplete("&f&lUPDATED &5&l%plugin% &f&lTO &d&lV%new_version% &7&o(%elapsed_time%s)");
//        locale.setUpdateFailed("&f&lUPDATING &5&l%plugin% &d&lV%old_version% &a&l» &d&l%new_version% &8[&c&lUPDATE FAILED &7&o(Check Console)]");

        locale.setUpdating("&d&lU&5&l2&d&lD &7&oUpdating &d&o%plugin% &7V%old_version% » V%new_version%");
        locale.setUpdatingDownload("&d&lU&5&l2&d&lD &7&oUpdating &d&o%plugin% &7V%old_version% » V%new_version% &8| %download_bar% &8| &d%download_percent%");
        locale.setUpdateComplete("&d&lU&5&l2&d&lD &7&oUpdated &d&o%plugin% &7V%old_version% » V%new_version% &7&o(%elapsed_time%s)");
        locale.setUpdateFailed("&d&lU&5&l2&d&lD &7&oUpdating &d&o%plugin% &7V%old_version% » V%new_version% &8[&c&lUPDATE FAILED &7&o(Check Console)&8]");

        return locale;
    }

    public UpdateLocale getDownloadLocale() {
        UpdateLocale locale = new UpdateLocale();

        locale.setFileName("%plugin%-%new_version%");
        locale.setPluginName("%plugin%");

        locale.setUpdating("&d&lU&5&l2&d&lD &7&oDownloading &d&o%plugin% &7&oV%new_version%");
        locale.setUpdatingDownload("&d&lU&5&l2&d&lD &7&oDownloading &d&o%plugin% &7&oV%new_version% &8| %download_bar% &8| &d%download_percent%");
        locale.setUpdateComplete("&d&lU&5&l2&d&lD &7&oDownloaded &d&o%plugin% &7&oV%new_version% (%elapsed_time%s)");
        locale.setUpdateFailed("&d&lU&5&l2&d&lD &7&oDownloading &d&o%plugin% &7&oV%new_version% &8[&c&lDOWNLOAD FAILED &7&o(Check Console)]");

        return locale;
    }

    public ArrayList<PluginInfo> getAvailableUpdates() {
        ArrayList<PluginInfo> updates = new ArrayList<>();
        ArrayList<PluginInfo> badApples = new ArrayList<>();

        for (PluginInfo info : linkedPlugins) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(info.getName());

            if (plugin == null) {
                badApples.add(info);
                continue;
            }

            if (!plugin.getDescription().getVersion().equalsIgnoreCase(info.getLatestVersion())) {
                updates.add(info);
            }
        }

        badApples.forEach(this::removeLinkedPlugin);

        return updates;
    }
}
