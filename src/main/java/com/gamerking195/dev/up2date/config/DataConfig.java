package com.gamerking195.dev.up2date.config;

import com.gamerking195.dev.up2date.Up2Date;
import com.gamerking195.dev.up2date.update.PluginInfo;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 9/8/17
 */
public class DataConfig {
    private DataConfig() {}

    private static DataConfig instance = new DataConfig();

    public static DataConfig getConfig() {
        return instance;
    }

    private File dataFile = new File(Up2Date.getInstance().getDataFolder(), "plugindata.yml");
    private FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);

    public void init() {
        dataConfig.options().copyDefaults(true);

        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
                dataConfig.save(dataFile);
            } catch (IOException ex) {
                Up2Date.getInstance().printError(ex, "Error occurred while creating plugindata.yml");
            }
        }
    }

    public void saveFile() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException ex) {
            Up2Date.getInstance().printError(ex, "Error occurred while creating plugindata.yml");
        }
    }

    public PluginInfo getPluginInfo(String name) {
        if (fileContains(name)) {
            return new PluginInfo(name, dataConfig.getInt(name + ".ID"), dataConfig.getString(name + ".Description"), dataConfig.getString(name + ".Author"), dataConfig.getString(name + ".Version"), dataConfig.getBoolean(name + ".Premium"), dataConfig.getString(name + ".TestedVersions"));
        }
        return null;
    }

    public void writeInfoToFile(PluginInfo info) {
        dataConfig.set(info.getName() + ".ID", info.getId());
        dataConfig.set(info.getName() + ".Author", info.getAuthor());
        dataConfig.set(info.getName() + ".Version", info.getLatestVersion());
        dataConfig.set(info.getName() + ".Description", info.getDescription());
        dataConfig.set(info.getName() + ".Premium", info.isPremium());
        dataConfig.set(info.getName() + ".TestedVersions", info.getSupportedMcVersions());

        saveFile();
    }

    public boolean fileContains(String name) {
        return dataConfig.contains(name);
    }

    public void setFile(ArrayList<PluginInfo> map) {
        map.forEach(this::writeInfoToFile);

        saveFile();
    }

    public ArrayList<PluginInfo> getFile() {
        ArrayList<PluginInfo> infoList = new ArrayList<>();

        dataConfig.getKeys(false).forEach((name) -> infoList.add(getPluginInfo(name)));

        return infoList;
    }

    public void deletePath(String path) {
        dataConfig.set(path, null);
    }
}
