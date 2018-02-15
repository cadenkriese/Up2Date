package com.gamerking195.dev.up2date.ui.settings;

import com.gamerking195.dev.up2date.Up2Date;
import com.gamerking195.dev.up2date.config.MainConfig;
import com.gamerking195.dev.up2date.ui.SettingsGUI;
import com.gamerking195.dev.up2date.update.UpdateManager;
import com.gamerking195.dev.up2date.util.UtilText;
import com.gamerking195.dev.up2date.util.gui.ConfirmGUI;
import com.gamerking195.dev.up2date.util.gui.GUI;
import com.gamerking195.dev.up2date.util.item.ItemStackBuilder;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Caden Kriese (GamerKing195) on 11/19/17.
 * <p>
 * License is specified by the distributor which this
 * file was written for. Otherwise it can be found in the LICENSE file.
 * If there is no license file the code is then completely copyrighted
 * and you must contact me before using it IN ANY WAY.
 */
public class SQLGui extends GUI {
    public SQLGui() {
        super ("&d&lU&5&l2&d&lD &8- &DSettings &8- &dSQL", 45);
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        MainConfig config = Up2Date.getInstance().getMainConfig();

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

        switch (event.getRawSlot()) {
            case 4:
                String enable = config.isEnableSQL() ? "&cdisable&7" : "&aenable&7";

                new ConfirmGUI("&dContinue?",
                                      () -> {
                                          boolean enableSql = !config.isEnableSQL();

                                          config.setEnableSQL(enableSql);

                                          UpdateManager.getInstance().swapData(enableSql);

                                          player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
                                          new SQLGui().open(player);
                                      },

                                      () -> {
                                          player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                          new SQLGui().open(player);
                                      },

                                      "&7Click '&a&lCONFIRM&7' if you want U2D",
                                      "&7to "+enable+" SQL support and automatically",
                                      "&7swap all data."
                ).open(player);

                break;
            case 8:
                player.closeInventory();
                UtilText.getUtil().sendTitle("", "&7&oTesting connection...", 10, 1000, 0, player);

                BukkitRunnable success = new BukkitRunnable() {
                    @Override
                    public void run() {
                        UtilText.getUtil().sendTitle("&a&lSUCCESS", 10, 40, 0, player);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                new SQLGui().open(player);
                            }
                        }.runTaskLater(Up2Date.getInstance(), 45L);
                    }
                };

                BukkitRunnable failure = new BukkitRunnable() {
                    @Override
                    public void run() {
                        UtilText.getUtil().sendTitle("&4&lFAILED", "&7&oCheck console.", 10, 40, 0, player);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                new SQLGui().open(player);
                            }
                        }.runTaskLater(Up2Date.getInstance(), 45L);
                    }
                };

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            Class.forName("com.mysql.jdbc.Driver");
                            Connection connection = DriverManager.getConnection("jdbc:mysql://" + config.getHostName(), config.getUsername(), config.getPassword());
                            if (connection != null && connection.isValid(3)) {
                                success.runTask(Up2Date.getInstance());
                                connection.close();
                                return;
                            }

                            failure.runTask(Up2Date.getInstance());
                            if (connection != null)
                                connection.close();
                        } catch (SQLException | ClassNotFoundException ex) {
                            failure.runTask(Up2Date.getInstance());
                            Up2Date.getInstance().systemOutPrintError(ex, "Error occurred while testing SQL connection.");
                        }
                    }
                }.runTaskAsynchronously(Up2Date.getInstance());

                break;
            case 18:
                new AnvilGUI(Up2Date.getInstance(), player, "Enter hostname", (player1, response) -> {
                    config.setHostName(response);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    new SQLGui().open(player);
                    return "Success";
                });
                break;
            case 20:
                new AnvilGUI(Up2Date.getInstance(), player, "Enter username", (player1, response) -> {
                    config.setUsername(response);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    new SQLGui().open(player);
                    return "Success";
                });
                break;
            case 22:
                new AnvilGUI(Up2Date.getInstance(), player, "Enter password", (player1, response) -> {
                    config.setPassword(response);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    new SQLGui().open(player);
                    return "Success";
                });
                break;
            case 24:
                new AnvilGUI(Up2Date.getInstance(), player, "Enter database name", (player1, response) -> {
                    config.setDatabase(response);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    new SQLGui().open(player);
                    return "Success";
                });
                break;
            case 26:
                new AnvilGUI(Up2Date.getInstance(), player, "Enter tablename", (player1, response) -> {
                    config.setTablename(response);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    new SQLGui().open(player);
                    return "Success";
                });
                break;
            case 40:
                new SettingsGUI(true).open(player);
                break;
        }
    }

    @Override
    protected void populate() {
        inventory.setItem(4, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                                     .setDurability(Up2Date.getInstance().getMainConfig().isEnableSQL() ? (short) 5 : (short) 14)
                                     .setName("&f&lENABLE SQL")
                                     .setLore(
                                             "&7&lValue: &d&l"+ Up2Date.getInstance().getMainConfig().isEnableSQL(),
                                             "&7&lDescription: ",
                                             "     &d&lShould Up2Date data be stored in a MySQL ",
                                             "     &d&ldatabase so you can sync linked plugins",
                                             "     &d&lbetween servers.",
                                             "&7&lType: &d&lBoolean",
                                             "&7&lDefault: &d&lfalse")
                                     .build());


        inventory.setItem(18, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                                      .setDurability((short) 5)
                                      .setName("&f&lHOSTNAME")
                                      .setLore(
                                              "&7&lValue: &d&l"+ Up2Date.getInstance().getMainConfig().getHostName(),
                                              "&7&lDescription: ",
                                              "     &d&lHostname / IP to the MySQL db,",
                                              "     &d&lport included.",
                                              "&7&lType: &d&lString")
                                      .build());


        inventory.setItem(20, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                                      .setDurability((short) 5)
                                      .setName("&f&lUSERNAME")
                                      .setLore(
                                              "&7&lValue: &d&l"+ Up2Date.getInstance().getMainConfig().getUsername(),
                                              "&7&lDescription: ",
                                              "     &d&lUsername Up2Date will use to",
                                              "     &d&lconnect to the database.",
                                              "&7&lType: &d&lString")
                                      .build());


        inventory.setItem(22, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                                      .setDurability((short) 5)
                                      .setName("&f&lPASSWORD")
                                      .setLore(
                                              "&7&lValue: &d&l"+ Up2Date.getInstance().getMainConfig().getPassword(),
                                              "&7&lDescription: ",
                                              "     &d&lPassword Up2Date will use to",
                                              "     &d&lconnect to the database.",
                                              "&7&lType: &d&lString")
                                      .build());


        inventory.setItem(24, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                                      .setDurability((short) 5)
                                      .setName("&f&lDATABASE")
                                      .setLore(
                                              "&7&lValue: &d&l"+ Up2Date.getInstance().getMainConfig().getDatabase(),
                                              "&7&lDescription: ",
                                              "     &d&lThe database that Up2Date",
                                              "     &d&lwill store its table in.",
                                              "&7&lType: &d&lString")
                                      .build());


        inventory.setItem(26, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                                      .setDurability((short) 5)
                                      .setName("&f&lTABLENAME")
                                      .setLore(
                                              "&7&lValue: &d&l"+ Up2Date.getInstance().getMainConfig().getTablename(),
                                              "&7&lDescription: ",
                                              "     &d&lThe name of the table",
                                              "     &d&lUp2Date will store data in.",
                                              "&7&lType: &d&lString")
                                      .build());


        inventory.setItem(8, new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                                     .setDurability((short) 13)
                                     .setName("&f&lTEST CONNECTION")
                                     .setLore(
                                             "&7Click to test the connection",
                                             "&7to your database with the current credentials.")
                                     .build());

        inventory.setItem(40, new ItemStackBuilder(Material.BARRIER).setName("&4&l&m«---&r &cBACK").build());
    }
}
