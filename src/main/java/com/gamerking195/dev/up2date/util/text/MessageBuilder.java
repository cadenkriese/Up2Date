package com.gamerking195.dev.up2date.util.text;

import com.gamerking195.dev.up2date.Up2Date;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

/**
 * Created by GamerKing195 on 8/13/17.
 * <p>
 * License is specified by the distributor which this
 * file was written for. Otherwise it can be found in the LICENSE file.
 */
public class MessageBuilder {
    @Getter
    private TextComponent component = new TextComponent();

    public MessageBuilder() {}
    public MessageBuilder(TextComponent component) {
        this.component = component;
    }

    public MessageBuilder addPlainText(String text) {
        component.addExtra(ChatColor.translateAlternateColorCodes('&', text));
        return this;
    }

    public MessageBuilder addComponent(TextComponent component) {
        this.component.addExtra(component);
        return this;
    }

    public MessageBuilder addHoverText(String text, String hover) {
        TextComponent newComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', text));

        newComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hover)).create()));
        component.addExtra(newComponent);
        return this;
    }

    public MessageBuilder addClickText(String text, String command, boolean suggest) {
        TextComponent newComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', text));
        ClickEvent.Action event = suggest ? ClickEvent.Action.SUGGEST_COMMAND : ClickEvent.Action.RUN_COMMAND;

        newComponent.setClickEvent(new ClickEvent(event, command));

        component.addExtra(newComponent);
        return this;
    }

    public MessageBuilder addURLText(String text, String url) {
        TextComponent newComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', text));

        newComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

        component.addExtra(newComponent);
        return this;
    }

    public MessageBuilder addHoverClickText(String text, String hover, String command, boolean suggest) {
        TextComponent newComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', text));
        ClickEvent.Action event = suggest ? ClickEvent.Action.SUGGEST_COMMAND : ClickEvent.Action.RUN_COMMAND;

        newComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hover)).create()));
        newComponent.setClickEvent(new ClickEvent(event, command));

        component.addExtra(newComponent);
        return this;
    }

    public MessageBuilder addHoverUrlText(String text, String hover, String url) {
        TextComponent newComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', text));

        newComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hover)).create()));
        newComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

        component.addExtra(newComponent);
        return this;
    }

    public MessageBuilder sendToPlayers(Player... players) {
        component.setText(component.getText().replace(" ", " "+ChatColor.LIGHT_PURPLE));
        for (Player player : players) {
            player.spigot().sendMessage(ChatMessageType.CHAT, component);
        }

        return this;
    }

    public MessageBuilder sendToPlayersPrefixed(Player... players) {
        component.setText(component.getText().replace(" ", " "+ChatColor.LIGHT_PURPLE));
        for (Player player : players) {

            TextComponent prefix = new TextComponent(ChatColor.translateAlternateColorCodes('&', Up2Date.getInstance().getMainConfig().getPrefix()));

            prefix.addExtra(component);

            player.spigot().sendMessage(ChatMessageType.CHAT, prefix);
        }

        return this;
    }

    public MessageBuilder sendToPlayersPrefixVariable(Player... players) {
        component.setText(component.getText().replace(" ", " " + ChatColor.LIGHT_PURPLE).replace("%prefix%", Up2Date.getInstance().getMainConfig().getPrefix()));

        for (Player player : players)
            player.spigot().sendMessage(ChatMessageType.CHAT, component);

        return this;
    }
}
