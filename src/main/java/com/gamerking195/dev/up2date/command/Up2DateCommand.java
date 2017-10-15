package com.gamerking195.dev.up2date.command;

import com.gamerking195.dev.autoupdaterapi.AutoUpdaterAPI;
import com.gamerking195.dev.up2date.Up2Date;
import com.gamerking195.dev.up2date.ui.UpdateGUI;
import com.gamerking195.dev.up2date.update.UpdateManager;
import com.gamerking195.dev.up2date.util.text.MessageBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Spigot: &dUNKNOWN"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Issue Tracker: &dhttps://github.com/GamerKing195/Up2Date/issues"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Help: &d/up2date help"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&m-----------------------"));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&m-----------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d&lUp&5&l2&d&lDate &5V"+ Up2Date.getInstance().getDescription().getVersion()+" &dby &5"+ Up2Date.getInstance().getDescription().getAuthors().toString().replace("[", "").replace("]", "")));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/up2date [INFO:HELP:SETUP] | &dBase command for Up2Date. (No arguments will open the Update GUI)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/update [INFO:HELP:SETUP] | &dAlias for /up2date"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/u2d [I:H:S] | &dAlias for /u2d"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&m-----------------------"));
    }
}
