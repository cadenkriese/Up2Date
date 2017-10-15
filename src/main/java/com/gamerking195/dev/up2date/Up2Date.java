package com.gamerking195.dev.up2date;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.gamerking195.dev.autoupdaterapi.UpdateLocale;
import com.gamerking195.dev.up2date.command.SetupCommand;
import com.gamerking195.dev.up2date.command.Up2DateCommand;
import com.gamerking195.dev.up2date.config.MainConfig;
import com.gamerking195.dev.up2date.listener.PlayerJoinListener;
import com.gamerking195.dev.up2date.update.UpdateManager;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import org.bukkit.scheduler.BukkitRunnable;

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

        //Threadpool
        fixedThreadPool = Executors.newFixedThreadPool(mainConfig.getThreadPoolSize());

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
        UpdateManager.getInstance().saveDataNow();
        UpdateManager.getInstance().getCacheUpdaters().forEach(BukkitRunnable::cancel);
        fixedThreadPool.shutdownNow();

        try {
            mainConfig.save();
        } catch (InvalidConfigurationException ex) {
            printError(ex, "Error occurred while saving config.yml");
        }
    }

    public void printError(Exception ex, String extraInfo) {
        log.severe("A severe error has occurred with the Up2Date plugin.");
        log.severe("If you cannot figure out this error on your own (e.g. a config error) please copy and paste everything from here to END ERROR and post it at https://github.com/GamerKing195/Up2Date/issues.");
        log.severe("");
        log.severe("============== BEGIN ERROR ==============");
        log.severe("PLUGIN VERSION: Up2Date V" + getDescription().getVersion());
        log.severe("");
        log.severe("PLUGIN MESSAGE: "+extraInfo);
        log.severe("");
        log.severe("MESSAGE: " + ex.getMessage());
        log.severe("");
        log.severe("STACKTRACE: ");
        ex.printStackTrace();
        log.severe("");
        log.severe("============== END ERROR ==============");
    }

    //use system.out.println to avoid async bukkit calls so it can be run async
    public void systemOutPrintError(Exception ex, String extraInfo) {
        System.out.println("A severe error has occurred with the Up2Date plugin.");
        System.out.println("If you cannot figure out this error on your own (e.g. a config error) please copy and paste everything from here to END ERROR and post it at https://github.com/GamerKing195/Up2Date/issues.");
        System.out.println("");
        System.out.println("============== BEGIN ERROR ==============");
        System.out.println("PLUGIN VERSION: Up2Date V" + getDescription().getVersion());
        System.out.println("");
        System.out.println("PLUGIN MESSAGE: "+extraInfo);
        System.out.println("");
        System.out.println("MESSAGE: " + ex.getMessage());
        System.out.println("");
        System.out.println("STACKTRACE: ");
        ex.printStackTrace();
        System.out.println("");
        System.out.println("============== END ERROR ==============");
    }

    public void printPluginError(String header, String message) {
        log.severe("============== BEGIN ERROR ==============");
        log.severe(header);
        log.severe("");
        log.severe("PLUGIN VERSION: Up2Date V" + getDescription().getVersion());
        log.severe("");
        log.severe("PLUGIN MESSAGE: "+message);
        log.severe("");
        log.severe("============== END ERROR ==============");
    }
}
