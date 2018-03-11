package com.gamerking195.dev.up2date.update;

import be.maximvdw.spigotsite.api.resource.Resource;
import com.gamerking195.dev.up2date.util.UtilSiteSearch;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.plugin.Plugin;

/**
 * Created by Caden Kriese (GamerKing195) on 9/6/17.
 * <p>
 * License is specified by the distributor which this
 * file was written for. Otherwise it can be found in the LICENSE file.
 * If there is no license file the code is then completely copyrighted
 * and you must contact me before using it IN ANY WAY.
 */

@Getter
@Setter
public class PluginInfo {
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
