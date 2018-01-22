package com.gamerking195.dev.up2date.command;

import com.gamerking195.dev.autoupdaterapi.AutoUpdaterAPI;
import com.gamerking195.dev.autoupdaterapi.PremiumUpdater;
import com.gamerking195.dev.autoupdaterapi.UpdateLocale;
import com.gamerking195.dev.up2date.Up2Date;
import com.gamerking195.dev.up2date.ui.SettingsGUI;
import com.gamerking195.dev.up2date.ui.UpdateGUI;
import com.gamerking195.dev.up2date.update.UpdateManager;
import com.gamerking195.dev.up2date.util.UtilU2dUpdater;
import com.gamerking195.dev.up2date.util.text.MessageBuilder;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

/**
 * Created by Caden Kriese (GamerKing195) on 10/11/17.
 * <p>
 * License is specified by the distributor which this
 * file was written for. Otherwise it can be found in the LICENSE file.
 * If there is no license file the code is then completely copyrighted
 * and you must contact me before using it IN ANY WAY.
 */
public class Up2DateCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                if (sender.hasPermission("u2d.manage") || sender.hasPermission("u2d.update") || sender.hasPermission("u2d.*"))
                    new UpdateGUI((Player) sender).open((Player) sender);
                else
                    new MessageBuilder().addPlainText(Up2Date.getInstance().getMainConfig().getNoPermissionMessage()).sendToPlayersPrefixed((Player) sender);

                return true;
            }
            else
                sendInfo(sender);
        } else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("i")) {
            if (sender.hasPermission("u2d.command") || sender.hasPermission("u2d.*"))
                sendInfo(sender);
            else
                new MessageBuilder().addPlainText(Up2Date.getInstance().getMainConfig().getNoPermissionMessage()).sendToPlayersPrefixed((Player) sender);
        } else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h")) {
            if (sender.hasPermission("u2d.command") || sender.hasPermission("u2d.*"))
                sendHelp(sender);
            else
                new MessageBuilder().addPlainText(Up2Date.getInstance().getMainConfig().getNoPermissionMessage()).sendToPlayersPrefixed((Player) sender);
        } else if (args[0].equalsIgnoreCase("setup") || args[0].equalsIgnoreCase("s")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("u2d.manage") || sender.hasPermission("u2d.*")) {
                    if (!Up2Date.getInstance().getMainConfig().isSetupComplete()) {
                        if (UpdateManager.getInstance().getLinkedPlugins().size() > 0) {
                            if (Bukkit.getPluginManager().getPlugin("AutoUpdaterAPI") != null && AutoUpdaterAPI.getInstance().getCurrentUser() != null) {
                                Up2Date.getInstance().getLogger().info("Dependencies already setup, skipping tutorial.");
                                Up2Date.getInstance().getMainConfig().setSetupComplete(true);
                                return true;
                            }
                        } else
                            Up2Date.getInstance().getMainConfig().setSetupComplete(false);

                        ((Player) sender).performCommand("stp accept");
                    }
                } else {
                    new MessageBuilder().addPlainText(Up2Date.getInstance().getMainConfig().getNoPermissionMessage()).sendToPlayersPrefixed((Player) sender);
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Up2Date.getInstance().getMainConfig().getPrefix()+"&dYou must be a player to setup the plugin!"));
                return true;
            }
        } else if (args[0].equalsIgnoreCase("login") || args[0].equalsIgnoreCase("l")) {
            if (sender instanceof Player) {
                if (sender.hasPermission("u2d.setup") || sender.hasPermission("u2d.*")) {
                    new PremiumUpdater((Player) sender, Up2Date.getInstance(), 1, new UpdateLocale(), false, false).authenticate(false);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (new File(Up2Date.getInstance().getDataFolder().getParentFile().getPath()+"/.creds").exists() && AutoUpdaterAPI.getInstance().getCurrentUser() != null) {
                                ((Player) sender).spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', "&d&lU&5&l2&d&lD &a&oSuccessfully logged in!")));
                                ((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

                                cancel();
                            }
                        }
                    }.runTaskTimer(Up2Date.getInstance(), 400L, 40L);
                } else
                    new MessageBuilder().addPlainText(Up2Date.getInstance().getMainConfig().getNoPermissionMessage()).sendToPlayersPrefixed((Player) sender);
            } else
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Up2Date.getInstance().getMainConfig().getPrefix()+"&dYou must be a player to setup the plugin!"));

            return true;
        } else if (args[0].equalsIgnoreCase("configure") || args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase("c")) {
            if (sender instanceof Player) {
                new SettingsGUI(false).open((Player) sender);
            } else
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Up2Date.getInstance().getMainConfig().getPrefix()+"&dYou must be a player to setup the plugin!"));
        } else if (args[0].equalsIgnoreCase("u2dupdate")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.isOp() || player.hasPermission("u2d.update") || player.hasPermission("u2d.*")) {
                    UtilU2dUpdater.getInstance().update(player);
                }
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("u2d.manage") || sender.hasPermission("u2d.*")) {
                try {
                    Up2Date.getInstance().getMainConfig().reload();

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Up2Date.getInstance().getMainConfig().getPrefix()+"&dConfig reloaded!"));
                } catch (InvalidConfigurationException e) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Up2Date.getInstance().getMainConfig().getPrefix()+"&dConfig reload failed, check console."));
                    Up2Date.getInstance().printError(e, "Error occurred while reloading the config!");
                }
            } else {
                new MessageBuilder().addPlainText(Up2Date.getInstance().getMainConfig().getNoPermissionMessage()).sendToPlayersPrefixed((Player) sender);
            }
        } else {
            if (sender instanceof Player) {
                if (sender.hasPermission("u2d.manage") || sender.hasPermission("u2d.update") || sender.hasPermission("u2d.*"))
                    new UpdateGUI((Player) sender).open((Player) sender);
                else
                    new MessageBuilder().addPlainText(Up2Date.getInstance().getMainConfig().getNoPermissionMessage()).sendToPlayersPrefixed((Player) sender);

                return true;
            }
            else
                sendHelp(sender);
        }
        return true;
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&m-----------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d&lUp&5&l2&d&lDate &5V"+ Up2Date.getInstance().getDescription().getVersion()+" &dby &5"+ Up2Date.getInstance().getDescription().getAuthors().toString().replace("[", "").replace("]", "")));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Spigot: &dhttps://www.spigotmc.org/resources/up2date.49313/"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Issue Tracker: &dhttps://github.com/GamerKing195/Up2Date/issues"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Help: &d/up2date help"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&m-----------------------"));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&m-----------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d&lUp&5&l2&d&lDate &5V"+ Up2Date.getInstance().getDescription().getVersion()+" &dby &5"+ Up2Date.getInstance().getDescription().getAuthors().toString().replace("[", "").replace("]", "")));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/up2date [INFO:HELP:SETUP:LOGIN:CONFIGURE] | &dBase command for Up2Date. (No arguments will open the Update GUI)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/update [INFO:HELP:SETUP:LOGIN:CONFIG] | &dAlias for /up2date"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/u2d [I:H:S:L:C] | &dAlias for /u2d"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&m-----------------------"));
    }
}
