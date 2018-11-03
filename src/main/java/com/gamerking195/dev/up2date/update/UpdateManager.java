package com.gamerking195.dev.up2date.update;

import be.maximvdw.spigotsite.api.resource.Resource;
import com.gamerking195.dev.autoupdaterapi.AutoUpdaterAPI;
import com.gamerking195.dev.autoupdaterapi.UpdateLocale;
import com.gamerking195.dev.up2date.Up2Date;
import com.gamerking195.dev.up2date.command.SetupCommand;
import com.gamerking195.dev.up2date.config.DataConfig;
import com.gamerking195.dev.up2date.config.MainConfig;
import com.gamerking195.dev.up2date.util.UtilPlugin;
import com.gamerking195.dev.up2date.util.UtilSQL;
import com.gamerking195.dev.up2date.util.UtilSiteSearch;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

/**
 * @author Caden Kriese (flogic)
 * <p>
 * Created on 9/2/17
 */
public class UpdateManager {

    private UpdateManager() {
    }

    public @Getter boolean initialized = false;
    public @Getter Timestamp latestDbUpdate;
    private @Getter static UpdateManager instance = new UpdateManager();

    private @Getter @Setter ArrayList<PluginInfo> linkedPlugins = new ArrayList<>();
    private @Getter @Setter HashMap<Plugin, ArrayList<UtilSiteSearch.SearchResult>> unlinkedPlugins = new HashMap<>();
    private @Getter @Setter ArrayList<Plugin> unknownPlugins = new ArrayList<>();

    //Total refresh time is basedelay+(count*60)
    private @Getter BukkitRunnable cacheUpdater;
    private @Getter @Setter boolean currentTask = false;

    //How many plugins get grouped in one massive statement.
    private int entriesPerStatement = 50;

    public void init() {
        //Setup Linked plugins
        if (MainConfig.getConf().isEnableSQL()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (SetupCommand.inSetup)
                        return;

                    //Select everything that has changed since last query
                    String query = "SELECT * FROM TABLENAME";

                    if (latestDbUpdate != null) {
                        query = "SELECT * FROM TABLENAME WHERE lastupdated > '" + latestDbUpdate.toString() + "'";
                    }

                    UtilSQL.getInstance().runQuerySync(query).stream()
                                                       //Ensure we aren't adding duplicates & that we're only adding valid plugins.
                                                       .filter(info -> getInfoFromPluginName(info.getName()) == null && Bukkit.getPluginManager().getPlugin(info.getName()) != null)
                                                       .forEach(UpdateManager.this::addLinkedPlugin);

                    latestDbUpdate = new Timestamp(System.currentTimeMillis());
                }
            }.runTaskTimerAsynchronously(Up2Date.getInstance(), 0, MainConfig.getConf().getDatabaseRefreshDelay() * 20 * 60);
        } else {
            DataConfig.getConfig().init();
            linkedPlugins = DataConfig.getConfig().getFile();
        }


        //Setup unknown plugins.
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            boolean linked = false;
            for (PluginInfo info : linkedPlugins) {
                if (info.getName().equals(plugin.getName()))
                    linked = true;
            }

            if (MainConfig.getConf().isSetupComplete() && !linked && !plugin.getName().equals("Up2Date") && !plugin.getName().equals("AutoUpdaterAPI"))
                unknownPlugins.add(plugin);
        }

        //Refreshing
        int startDelay = 100;
        if (!MainConfig.getConf().isSetupComplete()) {
            startDelay += 2400;
        }

        cacheUpdater = new BukkitRunnable() {
            @Override
            public void run() {
                refresh();
            }
        };

        cacheUpdater.runTaskTimerAsynchronously(Up2Date.getInstance(), MainConfig.getConf().getCacheRefreshDelay() * 20 * 60 + startDelay, (MainConfig.getConf().getCacheRefreshDelay() * 20 * 60));

        PluginInfo bad = null;

        for (PluginInfo info : linkedPlugins) {
            if (info.getName().equalsIgnoreCase("Up2Date")) {
                bad = info;
            }
        }

        if (bad != null)
            linkedPlugins.remove(bad);

        initialized = true;
    }

    /*
     * Data Utilities
     */

    public void saveData() {
        //TODO SQL
        if (MainConfig.getConf().isEnableSQL()) {
            Iterator<PluginInfo> iterator = linkedPlugins.iterator();

            StringBuilder statement = new StringBuilder("INSERT INTO TABLENAME (name, id, author, version, description, premium, testedversions, lastupdated) VALUES ");

            int i = 0;

            while (iterator.hasNext()) {
                PluginInfo info = iterator.next();

                String desc = info.getDescription() != null ? info.getDescription().replace("'", "") : "Description not found.";

                //use ` instead of ' to easily remove all extra '
                statement
                        .append("(`")
                        .append(info.getName())
                        .append("`, ")
                        .append(info.getId())
                        .append(", `")
                        .append(info.getAuthor())
                        .append("`, `")
                        .append(info.getLatestVersion())
                        .append("`, `")
                        .append(desc)
                        .append("`, `")
                        .append(info.isPremium())
                        .append("`, `")
                        .append(info.getSupportedMcVersions())
                        .append("`, `")
                        .append(new Timestamp(System.currentTimeMillis()))
                        .append("`)");

                i++;

                if (i % entriesPerStatement == 0) {
                    statement.append(" ON DUPLICATE KEY UPDATE name=VALUES(name),id=VALUES(id),author=VALUES(author),version=VALUES(version),description=VALUES(description),premium=VALUES(premium),testedversions=VALUES(testedversions),lastupdated=VALUES(lastupdated)");
                    UtilSQL.getInstance().runStatementAsync(statement.toString().replace("'", "\\'").replace("`", "'"));

                    statement = new StringBuilder("INSERT INTO TABLENAME (name, id, author, version, description, premium, testedversions, lastupdated) VALUES ");
                } else if (iterator.hasNext() || i + 1 % entriesPerStatement == 0) {
                    statement.append(", ");
                }
            }

            if (i % entriesPerStatement != 0) {
                statement.append(" ON DUPLICATE KEY UPDATE name=VALUES(name), id=VALUES(id), author=VALUES(author), version=VALUES(version), description=VALUES(description), premium=VALUES(premium), testedversions=VALUES(testedversions), lastupdated=VALUES(lastupdated)");
                UtilSQL.getInstance().runStatementAsync(statement.toString().replace("'", "\'").replace("`", "'"));
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

            StringBuilder statement = new StringBuilder("INSERT INTO TABLENAME (name, id, author, version, description, premium, testedversions, lastupdated) VALUES ");

            int i = 0;

            while (iterator.hasNext()) {
                PluginInfo info = iterator.next();

                String desc = info.getDescription() != null ? info.getDescription().replace("'", "") : "Description not found.";

                statement
                        .append("(`")
                        .append(info.getName())
                        .append("`, ")
                        .append(info.getId())
                        .append(", `")
                        .append(info.getAuthor())
                        .append("`, `")
                        .append(info.getLatestVersion())
                        .append("`, `")
                        .append(desc)
                        .append("`, `")
                        .append(info.isPremium())
                        .append("`, `")
                        .append(info.getSupportedMcVersions())
                        .append("`, `")
                        .append(new Timestamp(System.currentTimeMillis()))
                        .append("`)");

                i++;

                if (i % entriesPerStatement == 0) {
                    UtilSQL.getInstance().runStatementAsync(statement.toString().replace("'", "\'").replace("`", "'"));

                    statement = new StringBuilder("INSERT INTO TABLENAME (name, id, author, version, description, premium, testedversions, lastupdated) VALUES ");
                } else if (iterator.hasNext() || i + 1 % 100 == 0) {
                    statement.append(", ");
                }
            }

            if (i % entriesPerStatement != 0)
                UtilSQL.getInstance().runStatementAsync(statement.toString().replace("'", "\'").replace("`", "'"));

        } else {
            DataConfig.getConfig().init();

            UtilSQL.getInstance().runQueryAsync("SELECT * FROM TABLENAME", result -> {

                UtilSQL.getInstance().runStatementSync("DROP TABLE IF EXISTS TABLENAME CASCADE");
                DataConfig.getConfig().setFile(result);
            });
        }
    }

    private void refresh() {
        if (SetupCommand.inSetup)
            return;

        if (Bukkit.getPluginManager().getPlugin("AutoUpdaterAPI") == null)
            return;

        ArrayList<PluginInfo> plugins = new ArrayList<>(UpdateManager.getInstance().getLinkedPlugins());
        //only check for updates on plugins that aren't already checked
        plugins.removeAll(getAvailableUpdates());

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

                    info.setLatestVersion(resource.getLastVersion());

                    replacePlugin(info);

                } catch (Exception ex) {
                    Up2Date.getInstance().systemOutPrintError(ex, "Error occurred while updating info for '" + info.getName() + "'");
                }
            });
        }
    }

    /*
     * UTILITIES
     */

    public void replacePlugin(PluginInfo info) {
        removeLinkedPlugin(getInfoFromPluginName(info.getName()));
        addLinkedPlugin(info);

        if (MainConfig.getConf().isEnableSQL())
            UtilSQL.getInstance().runStatementAsync("INSERT INTO TABLENAME (name, id, author, version, description, premium, testedversions, lastupdated) VALUES ('" + info.getName() + "', '" + info.getId() + "', '" + info.getAuthor() + "', '" + info.getLatestVersion() + "', '" + info.getDescription() + "', '" + info.isPremium() + "', '" + info.getSupportedMcVersions() + "', '" + new Timestamp(System.currentTimeMillis()) + "')" +
                                                       " ON DUPLICATE KEY UPDATE name=VALUES(name),id=VALUES(id),author=VALUES(author),version=VALUES(version),description=VALUES(description),premium=VALUES(premium),testedversions=VALUES(testedversions),lastupdated=VALUES(lastupdated)");
        else
            DataConfig.getConfig().writeInfoToFile(info);
    }

    /*
     * ADDERS
     */
    public void addLinkedPlugin(PluginInfo info) {
        linkedPlugins.add(info);

        if (MainConfig.getConf().isEnableSQL())
            UtilSQL.getInstance().runStatementAsync("INSERT INTO TABLENAME (name, id, author, version, description, premium, testedversions, lastupdated) VALUES ('" + info.getName() + "', '" + info.getId() + "', '" + info.getAuthor() + "', '" + info.getLatestVersion() + "', '" + info.getDescription() + "', '" + info.isPremium() + "', '" + info.getSupportedMcVersions() + "', '" + new Timestamp(System.currentTimeMillis()) + "')" +
                                                       " ON DUPLICATE KEY UPDATE name=VALUES(name),id=VALUES(id),author=VALUES(author),version=VALUES(version),description=VALUES(description),premium=VALUES(premium),testedversions=VALUES(testedversions),lastupdated=VALUES(lastupdated)");
        else
            DataConfig.getConfig().writeInfoToFile(info);
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
        PluginInfo info = getInfoFromPlugin(plugin);

        if (info != null)
            removeLinkedPlugin(info);
    }

    public void removeLinkedPlugin(PluginInfo info) {
        if (MainConfig.getConf().isEnableSQL()) {
            UtilSQL.getInstance().runStatementAsync("DELETE FROM TABLENAME WHERE id='" + info.getId() + "'");
        } else {
            DataConfig.getConfig().deletePath(info.getName());
            DataConfig.getConfig().saveFile();
        }

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

            if (UtilPlugin.compareVersions(plugin.getDescription().getVersion(), info.getLatestVersion())) {
                updates.add(info);
            }
        }

        badApples.forEach(this::removeLinkedPlugin);

        return updates;
    }

    private PluginInfo getInfoFromPlugin(Plugin plugin) {
        for (PluginInfo info : linkedPlugins) {
            if (info.getName().equals(plugin.getName()))
                return info;
        }

        return null;
    }

    public PluginInfo getInfoFromPluginName(String pluginName) {
        for (PluginInfo info : linkedPlugins) {
            if (info.getName().equals(pluginName))
                return info;
        }

        return null;
    }
}
