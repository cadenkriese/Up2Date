package com.gamerking195.dev.up2date;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.gamerking195.dev.autoupdaterapi.AutoUpdaterAPI;
import com.gamerking195.dev.autoupdaterapi.util.UtilReader;
import com.gamerking195.dev.up2date.command.SetupCommand;
import com.gamerking195.dev.up2date.command.Up2DateCommand;
import com.gamerking195.dev.up2date.config.MainConfig;
import com.gamerking195.dev.up2date.listener.PlayerJoinListener;
import com.gamerking195.dev.up2date.update.UpdateManager;
import com.gamerking195.dev.up2date.util.UtilStatisticsDatabase;
import com.gamerking195.dev.up2date.util.UtilPlugin;
import com.gamerking195.dev.up2date.util.UtilU2dUpdater;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * This code is fully copyright by GamerKing195 (Caden Kriese)
 * If you are viewing this message you have already violated that rule (unless you're a spigot resource staff <3)
 * by decompiling the plugin.
 *
 * THIS IS NOT A SCARE MESSAGE
 * By default, any code without a license specifying otherwise
 * is automatically copyrighted by its creator.
 */

public final class Up2Date extends JavaPlugin {

    public static String fs = FileSystems.getDefault().getSeparator();

    @Getter
    private static Up2Date instance;

    @Getter
    private ProtocolManager protocolManager;

    @Getter
    private Metrics metrics;

    @Getter
    private MainConfig mainConfig;

    @Getter
    private ExecutorService fixedThreadPool;

    private Logger log;

    @Override
    public void onEnable() {
        //Base setup
        instance = this;
        log = getLogger();
        protocolManager = ProtocolLibrary.getProtocolManager();

        //Config
        try {
            mainConfig = new MainConfig(instance);
            mainConfig.init();
        } catch (Exception ex) {
            printError(ex, "Error occurred while initializing config.yml");
        }

        if (mainConfig.getCacheRefreshDelay() < 5)
            mainConfig.setCacheRefreshDelay(5);

        if (mainConfig.getDatabaseRefreshDelay() < 5)
            mainConfig.setDatabaseRefreshDelay(5);

        if (mainConfig.getConnectionPoolSize() > 15)
            mainConfig.setDatabaseRefreshDelay(15);

        if (mainConfig.getThreadPoolSize() > 12)
            mainConfig.setDatabaseRefreshDelay(12);

        //Listeners
        Stream.of(
                new PlayerJoinListener()
        ).forEach(listener -> Bukkit.getServer().getPluginManager().registerEvents(listener, instance));

        //Commands
        getCommand("stp").setExecutor(new SetupCommand());
        getCommand("up2date").setExecutor(new Up2DateCommand());
        getCommand("update").setExecutor(new Up2DateCommand());
        getCommand("u2d").setExecutor(new Up2DateCommand());

        //Metrics
        metrics = new Metrics(instance);

        //Classes
        UpdateManager.getInstance().init();
        UtilStatisticsDatabase.getInstance().init();

        //Threadpool
        fixedThreadPool = Executors.newFixedThreadPool(mainConfig.getThreadPoolSize());

        //Automatically update AutoUpdaterAPI
        fixedThreadPool.submit(() -> {
            try {

                //Latest version number.
                String latestVersionInfo = UtilReader.readFrom("https://api.spiget.org/v2/resources/39719/versions/latest");

                Type type = new TypeToken<JsonObject>() {}.getType();
                JsonObject object = new Gson().fromJson(latestVersionInfo, type);

                //if update available
                Plugin plugin = Bukkit.getPluginManager().getPlugin("AutoUpdaterAPI");
                if (!object.get("name").getAsString().equals(plugin.getDescription().getVersion()) && (!plugin.getDescription().getVersion().contains("SNAPSHOT") && !plugin.getDescription().getVersion().contains("PRERELEASE"))) {
                    log.info("Updating AutoUpdaterAPI V"+plugin.getDescription().getVersion()+" » "+object.get("name").getAsString()+".");

                    Bukkit.getPluginManager().disablePlugin(plugin);
                    UtilPlugin.unload(Bukkit.getPluginManager().getPlugin("AutoUpdaterAPI"));

                    //Download AutoUpdaterAPI
                    URL url = new URL("https://api.spiget.org/v2/resources/39719/download");
                    HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                    httpConnection.setRequestProperty("User-Agent", "SpigetResourceUpdater");

                    BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
                    FileOutputStream fos = new FileOutputStream(new File(plugin.getDataFolder().getPath().substring(0, plugin.getDataFolder().getPath().lastIndexOf(fs)) + fs + "AutoUpdaterAPI.jar"));
                    BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);

                    byte[] data = new byte[1024];
                    int x;
                    while ((x = in.read(data, 0, 1024)) >= 0)
                        bout.write(data, 0, x);

                    bout.close();
                    in.close();

                    Plugin target = Bukkit.getPluginManager().loadPlugin(new File(plugin.getDataFolder().getPath().substring(0, plugin.getDataFolder().getPath().lastIndexOf(fs)) + fs + "AutoUpdaterAPI.jar"));
                    target.onLoad();
                    Bukkit.getPluginManager().enablePlugin(target);
                }
            }
            catch (Exception ex) {
                printError(ex, "Error occurred while checking for AutoUpdaterAPI updates.");
            }
        });

        UtilStatisticsDatabase.getInstance().setPluginstracked(UpdateManager.getInstance().getLinkedPlugins().size());
        UtilStatisticsDatabase.getInstance().saveDataNow();

        if (mainConfig.isEnableDebug())
            AutoUpdaterAPI.getInstance().setDebug(true);

        //TODO Remove
        new BukkitRunnable() {
            @Override
            public void run() {
                UtilU2dUpdater.getInstance().init();
            }
        }.runTaskLater(instance, 20L);

        //Big message so we look cool
        Stream.of(
                "&f┏--------------------------------------------┓",
                "&f|                                            |",
                "&f|    &d/##   /##      &5/######      &d/#######    &f|",
                "&f|   &d| ##  | ##     &5/##__  ##    &d| ##__  ##   &f|",
                "&f|   &d| ##  | ##    &5|__/  \\ ##    &d| ##  \\ ##   &f|",
                "&f|   &d| ##  | ##      &5/######/    &d| ##  | ##   &f|",
                "&f|   &d| ##  | ##     &5/##____/     &d| ##  | ##   &f|",
                "&f|   &d| ##  | ##    &5| ##          &d| ##  | ##   &f|",
                "&f|   &d|  ######/    &5| ########    &d| #######/   &f|",
                "&f|    &d\\______/     &5|________/    &d|_______/    &f|",
                "&f|                                            |",
                "&f┗--------------------------------------------┛",
                "&a",
                "&bWelcome to Up2Date V"+getDescription().getVersion()+" by "+getDescription().getAuthors().toString().replace("[", "").replace("]", "")+"!"
        ).forEach(string -> getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', string)));
    }

    @Override
    public void onDisable() {
        UtilStatisticsDatabase.getInstance().saveDataNow();
        UpdateManager.getInstance().getCacheUpdater().cancel();
        fixedThreadPool.shutdownNow();

        try {
            mainConfig.save();
        } catch (InvalidConfigurationException ex) {
            printError(ex, "Error occurred while saving config.yml");
        }
    }

    public void printError(Exception ex, String extraInfo) {
        String latest = UtilU2dUpdater.getInstance().isUpdateAvailable() ? "(OUTDATED)" : "(LATEST)";

        //If the task failed, cancel it.
        UpdateManager.getInstance().setCurrentTask(false);

        System.out.println("A severe error has occurred with the Up2Date plugin.");
        System.out.println("If you cannot figure out this error on your own (e.g. a config error) please copy and paste everything from here to END ERROR and post it at https://github.com/GamerKing195/Up2Date/issues.");
        System.out.println("Or on spigot at https://spigotmc.org/threads/284883/");
        log.severe("");
        log.severe("============== BEGIN ERROR ==============");
        log.severe("PLUGIN VERSION: V" + getDescription().getVersion() + " " + latest);
        log.severe("");
        log.severe("PLUGIN MESSAGE: " + extraInfo);
        log.severe("");
        log.severe("MESSAGE: " + ex.getMessage());
        log.severe("");
        log.severe("STACKTRACE: ");
        ex.printStackTrace();
        log.severe("");
        log.severe("============== END ERROR ==============");
    }

    public void systemOutPrintError(Exception ex, String extraInfo) {
        String latest = UtilU2dUpdater.getInstance().isUpdateAvailable() ? "(OUTDATED)" : "(LATEST)";

        //If the task failed, cancel it.
        UpdateManager.getInstance().setCurrentTask(false);

        System.out.println("A severe error has occurred with the Up2Date plugin.");
        System.out.println("If you cannot figure out this error on your own (e.g. a config error) please copy and paste everything from here to END ERROR and post it at https://github.com/GamerKing195/Up2Date/issues.");
        System.out.println("Or on spigot at https://spigotmc.org/threads/284883/");
        System.out.println("");
        System.out.println("============== BEGIN ERROR ==============");
        System.out.println("PLUGIN VERSION: V" + getDescription().getVersion() + " " + latest);
        System.out.println("");
        System.out.println("PLUGIN MESSAGE: " + extraInfo);
        System.out.println("");
        System.out.println("MESSAGE: " + ex.getMessage());
        System.out.println("");
        System.out.println("STACKTRACE: ");
        ex.printStackTrace();
        System.out.println("");
        System.out.println("============== END ERROR ==============");
    }

    public void printPluginError(String header, String message) {
        String latest = UtilU2dUpdater.getInstance().isUpdateAvailable() ? "(OUTDATED)" : "(LATEST)";

        //If the task failed, cancel it.
        UpdateManager.getInstance().setCurrentTask(false);

        log.severe("============== BEGIN ERROR ==============");
        log.severe(header);
        log.severe("");
        log.severe("PLUGIN VERSION: V" + getDescription().getVersion() + " " + latest);
        log.severe("");
        log.severe("PLUGIN MESSAGE: " + message);
        log.severe("");
        log.severe("============== END ERROR ==============");
    }
}
