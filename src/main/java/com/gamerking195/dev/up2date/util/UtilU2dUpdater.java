package com.gamerking195.dev.up2date.util;

import com.gamerking195.dev.autoupdaterapi.AutoUpdaterAPI;
import com.gamerking195.dev.autoupdaterapi.PremiumUpdater;
import com.gamerking195.dev.autoupdaterapi.util.UtilReader;
import com.gamerking195.dev.up2date.Up2Date;
import com.gamerking195.dev.up2date.update.UpdateManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Caden Kriese (GamerKing195) on 12/26/17.
 * <p>
 * License is specified by the distributor which this
 * file was written for. Otherwise it can be found in the LICENSE file.
 * If there is no license file the code is then completely copyrighted
 * and you must contact me before using it IN ANY WAY.
 */
public class UtilU2dUpdater {

    private UtilU2dUpdater() {}
    private static UtilU2dUpdater instance = new UtilU2dUpdater();
    public static UtilU2dUpdater getInstance() {
        return instance;
    }

    @Getter
    private String latestVersion;
    @Getter
    private boolean updateAvailable;
    @Getter
    private List<String> updateMessage = new ArrayList<>();
    @Getter
    private TextComponent accept;


    private String updateInfo;
    private List<String> testedVersions;
    private boolean updating;

    private JavaPlugin plugin = Up2Date.getInstance();

    private Gson gson = new Gson();

    /*
     * UTILITIES
     */

    public void init() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getPluginManager().getPlugin("AutoUpdaterAPI") == null || AutoUpdaterAPI.getInstance().getCurrentUser() == null)
                    return;

                checkForUpdate();

                if (updateAvailable) {
                    String currentVersion = Up2Date.getInstance().getDescription().getVersion();
                    String mcVersion = Bukkit.getServer().getClass().getPackage().getName();
                    mcVersion = mcVersion.substring(mcVersion.lastIndexOf(".") + 1);
                    mcVersion = mcVersion.substring(1, mcVersion.length()-3).replace("_", ".");

                    updateMessage.add(ChatColor.translateAlternateColorCodes('&', "&f&m------------------------------"));
                    updateMessage.add(ChatColor.translateAlternateColorCodes('&', "&d&lUp&5&l2&d&lDate &5V"+ Up2Date.getInstance().getDescription().getVersion()+" &dby &5"+ Up2Date.getInstance().getDescription().getAuthors().toString().replace("[", "").replace("]", "")));
                    updateMessage.add("");
                    updateMessage.add(ChatColor.translateAlternateColorCodes('&', "&7There is an &dUp&52&dDate &7update available!"));
                    updateMessage.add(ChatColor.translateAlternateColorCodes('&', "&7Version: &d" + latestVersion));
                    updateMessage.add(ChatColor.translateAlternateColorCodes('&', "&7Updates: \n" + updateInfo));
                    updateMessage.add(ChatColor.translateAlternateColorCodes('&', "&7Supported MC Versions: &d" + StringUtils.join(testedVersions, ", ")));
                    if (!testedVersions.contains(mcVersion))
                        updateMessage.add(ChatColor.DARK_RED+"Warning your current version, "+mcVersion+", is not supported by this update, there may be unexpected bugs!");
                    updateMessage.add("");

                    accept = new TextComponent("[CLICK TO UPDATE]");
                    accept.setColor(ChatColor.DARK_PURPLE);
                    accept.setBold(true);
                    accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/u2d u2dupdate"));
                    accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&d&lUP&5&l2&d&lDATE &dV" + currentVersion + " &a&l» &dV" + latestVersion+"\n&b\n&d    CLICK TO UPDATE")).create()));

                    updateMessage.add("ACCEPT");

                    updateMessage.add("");
                    updateMessage.add(ChatColor.translateAlternateColorCodes('&', "&f&m------------------------------"));


                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.isOp() || player.hasPermission("u2d.update") || player.hasPermission("u2d.*")) {
                            for (String string : updateMessage) {
                                if (string.equalsIgnoreCase("ACCEPT"))
                                    player.spigot().sendMessage(accept);
                                else
                                    player.sendMessage(string);
                            }
                        }
                    }
                }
                //Do update check once every 90 minutes.
            }
        }.runTaskTimer(plugin, 240L, 90*(20*60));
    }

    private void checkForUpdate() {
        try {
            //Latest version number.
            latestVersion = AutoUpdaterAPI.getInstance().getApi().getResourceManager().getResourceById(49313, AutoUpdaterAPI.getInstance().getCurrentUser()).getLastVersion();

            updateAvailable = !latestVersion.equals(Up2Date.getInstance().getDescription().getVersion());

            if (updateAvailable) {
                //Supported mc versions

                Type objectType = new TypeToken<JsonObject>(){}.getType();

                JsonObject pluginInfoObject = gson.fromJson(UtilReader.readFrom("https://api.spiget.org/v2/resources/49313/"), objectType);

                testedVersions = gson.fromJson(pluginInfoObject.get("testedVersions"), new TypeToken<List<String>>(){}.getType());

                //Update description

                JsonObject latestUpdateObject = gson.fromJson(UtilReader.readFrom("https://api.spiget.org/v2/resources/49313/updates/latest"), objectType);

                String descriptionBase64 = gson.fromJson(latestUpdateObject.get("description"), new TypeToken<String>(){}.getType());
                String decodedDescription = new String(Base64.getDecoder().decode(descriptionBase64));

                Pattern pat = Pattern.compile("<li>(.*)</li>");

                Matcher match = pat.matcher(decodedDescription);

                StringBuilder sb = new StringBuilder();

                while (match.find())
                    sb.append(ChatColor.LIGHT_PURPLE).append(" - ").append(match.group(1)).append("\n");

                updateInfo = sb.toString();
            }
        } catch (Exception exception) {
            //TODO Error suppressed for now bc it causes lots of console spam.

            //Up2Date.getInstance().printError(exception, "Error occurred whilst pinging spiget.");
            //try {
            //    Up2Date.getInstance().printPluginError("Json received from spigot.", UtilReader.readFrom("https://api.spiget.org/v2/resources/49313/"));
            //} catch (Exception ignored) {}
        }
    }

    public void update(Player player) {
        if (!updating) {
            updating = true;

            //Do the optimal shutdown.
            UpdateManager.getInstance().saveDataNow();
            UtilDatabase.getInstance().saveDataNow();
            UpdateManager.getInstance().getCacheUpdater().cancel();
            Up2Date.getInstance().getFixedThreadPool().shutdown();

            new BukkitRunnable() {
                @Override
                public void run() {
                    new PremiumUpdater(player, plugin, 49313, UpdateManager.getInstance().getUpdateLocale(), false, true).update();
                }
            }.runTaskLater(plugin, 30L);
        }
    }
}
