package com.gamerking195.dev.up2date.update;

import be.maximvdw.spigotsite.api.resource.Resource;
import com.gamerking195.dev.up2date.util.UtilSiteSearch;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.plugin.Plugin;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 9/6/17
 */
public @Getter @Setter class PluginInfo {
    private String name;
    private int id;
    private String author;
    private String description;
    private String latestVersion;
    private String supportedMcVersions;
    private boolean premium;

    public PluginInfo(String name, int id, String description, String author, String latestVersion, boolean premium, String[] supportedMcVersions) {
        this.name = name;
        this.id = id;
        this.description = description;
        this.author = author;
        this.latestVersion = latestVersion;
        this.premium = premium;
        this.supportedMcVersions = StringUtils.join(supportedMcVersions, ", ");
    }

    public PluginInfo(String name, int id, String description, String author, String latestVersion, boolean premium, String supportedMcVersions) {
        this.name = name;
        this.id = id;
        this.description = description;
        this.author = author;
        this.latestVersion = latestVersion;
        this.premium = premium;
        this.supportedMcVersions = supportedMcVersions;
    }

    public PluginInfo(Plugin plugin, Resource resource, UtilSiteSearch.SearchResult result) {
        name = plugin.getName();
        id = resource.getResourceId();
        description = result.getTag();
        author = resource.getAuthor().getUsername();
        latestVersion = resource.getLastVersion();
        premium = result.isPremium();
        this.supportedMcVersions = StringUtils.join(result.getTestedVersions(), ", ");
    }

    public PluginInfo(Plugin plugin, UtilSiteSearch.SearchResult result) {
        name = plugin.getName();
        id = result.getId();
        description = result.getTag();
        this.supportedMcVersions = StringUtils.join(result.getTestedVersions(), ", ");
        author = "";
        latestVersion = "";
    }
}
