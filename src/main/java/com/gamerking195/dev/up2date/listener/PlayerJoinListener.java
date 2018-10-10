package com.gamerking195.dev.up2date.listener;

import com.gamerking195.dev.autoupdaterapi.AutoUpdaterAPI;
import com.gamerking195.dev.up2date.Up2Date;
import com.gamerking195.dev.up2date.config.MainConfig;
import com.gamerking195.dev.up2date.update.PluginInfo;
import com.gamerking195.dev.up2date.update.UpdateManager;
import com.gamerking195.dev.up2date.util.UtilText;
import com.gamerking195.dev.up2date.util.UtilU2dUpdater;
import com.gamerking195.dev.up2date.util.text.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

/**
 * @author Caden Kriese (flogic)
 * <p>
 * Created on 8/13/17
 */

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().isOp() || event.getPlayer().hasPermission("u2d.setup") || event.getPlayer().hasPermission("u2d.*")) {
            if (!MainConfig.getConf().isSetupComplete()) {
                if (UpdateManager.getInstance().getLinkedPlugins().size() > 0) {
                    if (Bukkit.getPluginManager().getPlugin("AutoUpdaterAPI") != null && AutoUpdaterAPI.getInstance().getCurrentUser() != null) {
                        Up2Date.getInstance().getLogger().info("Dependencies already setup, skipping tutorial.");
                        MainConfig.getConf().setSetupComplete(true);
                        return;
                    }
                } else
                    MainConfig.getConf().setSetupComplete(false);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        new MessageBuilder().addPlainText("&dHey there! Thanks for purchasing Up2Date!").sendToPlayersPrefixed(event.getPlayer());
                        new MessageBuilder().addPlainText("&dWe've noticed you haven't done the in-game setup yet, &dwould you like to begin?").sendToPlayersPrefixed(event.getPlayer());
                        new MessageBuilder().addHoverClickText("&2&l✔ &aYES", "&2&lPROCEED", "/stp accept", false).addPlainText("    &8&l|    ").addHoverClickText("&4&l✘ &cNO", "&4&lCANCEL", "/stp deny", false).sendToPlayers(event.getPlayer());
                    }
                }.runTaskLater(Up2Date.getInstance(), 20L);
            }
        }

        if (event.getPlayer().isOp() || event.getPlayer().hasPermission("u2d.update") || event.getPlayer().hasPermission("u2d.*")) {

            for (String string : UtilU2dUpdater.getInstance().getUpdateMessage()) {
                if (string.equalsIgnoreCase("ACCEPT"))
                    event.getPlayer().spigot().sendMessage(UtilU2dUpdater.getInstance().getAccept());
                else
                    event.getPlayer().sendMessage(string);
            }

            ArrayList<PluginInfo> availableUpdates = UpdateManager.getInstance().getAvailableUpdates();
            if (availableUpdates.size() > 0) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new MessageBuilder().addHoverClickText("&dThere " + UtilText.getUtil().getEnding("are", availableUpdates.size(), false) + " currently &5" + availableUpdates.size() + "&d " + UtilText.getUtil().getEnding("update", availableUpdates.size(), false) + " available, &dclick &dto &dopen &dthe &dGUI.", "&5View plugins.", "/u2d", false).sendToPlayersPrefixed(event.getPlayer());
                    }
                }.runTaskLater(Up2Date.getInstance(), 20L);
            }
        }
    }
}
