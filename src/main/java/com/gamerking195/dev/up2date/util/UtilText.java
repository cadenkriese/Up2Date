package com.gamerking195.dev.up2date.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.gamerking195.dev.up2date.Up2Date;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Caden Kriese (flogic)
 * <p>
 * Created on 8/13/17
 */
public class UtilText {

    private UtilText() {
    }

    private static UtilText instance = new UtilText();

    public static UtilText getUtil() {
        return instance;
    }

    /**
     * Sends a title to certain players.
     *
     * @param players The player the title will be sent too.
     * @param title   The text within the title (& chatcolors supported).
     * @param fadeIn  Fade in time for the title (in ticks).
     * @param stay    Stay time for the title (in ticks).
     * @param fadeOut Fade out time for the title (in ticks).
     */
    public void sendTitle(String title, int fadeIn, int stay, int fadeOut, Player... players) {
        sendTitle(title, "", fadeIn, stay, fadeOut, players);
    }

    /**
     * Sends a title & subtitle to certain players.
     *
     * @param players  The players the title will be sent too.
     * @param title    The text within the title (& chatcolors supported).
     * @param subTitle The text within the subtitle (& chatcolors supported).
     * @param fadeIn   Fade in time for the title (in ticks).
     * @param stay     Stay time for the title (in ticks).
     * @param fadeOut  Fade out time for the title (in ticks).
     */
    public void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut, Player... players) {

        ProtocolManager protocol = Up2Date.getInstance().getProtocolManager();

        PacketContainer titlePacket = protocol.createPacket(PacketType.Play.Server.TITLE);

        titlePacket.getIntegers().write(0, fadeIn);
        titlePacket.getIntegers().write(1, stay);
        titlePacket.getIntegers().write(2, fadeOut);

        titlePacket.getChatComponents().write(0,
                WrappedChatComponent.fromText(ChatColor.translateAlternateColorCodes('&', title)));

        titlePacket.getTitleActions().write(0, EnumWrappers.TitleAction.TITLE);

        PacketContainer subTitlePacket = protocol.createPacket(PacketType.Play.Server.TITLE);

        subTitlePacket.getChatComponents().write(0,
                WrappedChatComponent.fromText(ChatColor.translateAlternateColorCodes('&', subTitle)));

        subTitlePacket.getTitleActions().write(0, EnumWrappers.TitleAction.SUBTITLE);

        PacketContainer timingPacket = protocol.createPacket(PacketType.Play.Server.TITLE);

        if (fadeIn < 0)
            fadeIn = 1;

        if (stay < 0)
            stay = 1;

        if (fadeOut < 0)
            fadeOut = 1;

        timingPacket.getIntegers().write(0, fadeIn);
        timingPacket.getIntegers().write(1, stay);
        timingPacket.getIntegers().write(2, fadeOut);

        timingPacket.getTitleActions().write(0, EnumWrappers.TitleAction.TIMES);

        try {
            for (Player player : players) {
                if (player != null && player.isOnline()) {
                    protocol.sendServerPacket(player, timingPacket);
                    protocol.sendServerPacket(player, titlePacket);
                    protocol.sendServerPacket(player, subTitlePacket);
                }
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a subtitle to certain players.
     *
     * @param players  The player the title will be sent too.
     * @param subTitle The text within the subtitle (& chatcolors supported).
     * @param fadeIn   Fade in time for the title (in ticks).
     * @param stay     Stay time for the title (in ticks).
     * @param fadeOut  Fade out time for the title (in ticks).
     */
    public void sendSubTitle(String subTitle, int fadeIn, int stay, int fadeOut, Player... players) {
        sendTitle("", subTitle, fadeIn, stay, fadeOut, players);
    }

    /**
     * Clears the current title for certain players.
     *
     * @param players the players effected by this.
     */
    public void clearTitle(Player... players) {
        sendTitle("", "", 0, 0, 0, players);
    }

    /**
     * Send multiple titles to a player with a delay.
     * Titles sent will have an interval of (fadein+stay+fadeout) * title index.
     * (All timing uses ticks)
     *
     * @param player  The player the titles will be sent to.
     * @param fadeIn  The time a title fade in animation will take.
     * @param stay    The time the title will stay on screen.
     * @param fadeOut The time the title fade out animation will take.
     * @param titles  The list of titles, use \n to send a subtitle.
     */
    public void sendMultipleTitles(Player player, int fadeIn, int stay, int fadeOut, String... titles) {
        for (int index = 0; index < titles.length; index++) {
            String title = titles[index];

            new BukkitRunnable() {
                @Override
                public void run() {
                    String[] lines = title.split("\\n");
                    if (lines.length == 2) {
                        sendTitle(lines[0], lines[1], fadeIn, stay, fadeOut, player);
                    } else {
                        sendTitle(title, fadeIn, stay, fadeOut, player);
                    }
                }
            }.runTaskLater(Up2Date.getInstance(), (fadeIn + stay + fadeOut) * index);
        }
    }

    /**
     * Send an action bar to a player.
     *
     * @param text    The text to be sent.
     * @param players The players who will recieve the action bar.
     */
    public void sendActionBar(String text, Player... players) {
        for (Player player : players) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', text)));
        }
    }

    /**
     * Send an action bar to a player.
     *
     * @param text    The text to be sent.
     * @param players The players who will recieve the action bar.
     */
    public void sendActionBarSync(String text, Player... players) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : players) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', text)));
                }
            }
        }.runTask(Up2Date.getInstance());
    }

    /**
     * Gets the proper ending for a word.
     *
     * @param number
     * @param possessive
     * @return
     */
    public String getEnding(String word, int number, boolean possessive) {
        String ending = "";

        if (word.equalsIgnoreCase("is") || word.equalsIgnoreCase("are"))
            return number == 1 ? "is" : "are";

        if (word.endsWith("s") || word.endsWith("z"))
            ending += "e";

        if (number != 1) {
            ending += possessive ? "s'" : "s";
        } else
            ending += possessive ? "'s" : "";

        return word + ending;
    }
}
