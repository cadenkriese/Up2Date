package com.gamerking195.dev.up2date.util;

import com.gamerking195.dev.up2date.Up2Date;
import com.gamerking195.dev.up2date.update.PluginInfo;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Created by Caden Kriese (GamerKing195) on 10/21/17.
 * <p>
 * License is specified by the distributor which this
 * file was written for. Otherwise it can be found in the LICENSE file.
 * If there is no license file the code is then completely copyrighted
 * and you must contact me before using it IN ANY WAY.
 */
public class UtilStatisticsDatabase {
    private UtilStatisticsDatabase() {}
    private static UtilStatisticsDatabase instance = new UtilStatisticsDatabase();
    public static UtilStatisticsDatabase getInstance() {
        return instance;
    }

    private HikariDataSource dataSource;

    private String tablename = "incompatibilities";
    private String statstable = "statistics";

    @Getter
    @Setter
    double downloadsize = 0;
    @Getter
    @Setter
    int downloadedfiles = 0;
    @Getter
    @Setter
    int pluginstracked = 0;

    public void init() {
        //Do all this async bc we cant let my DB being down affect the performance of the plugin.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (dataSource == null) {
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl("jdbc:mysql://dba.gamerking195.com/u2d");
                    config.setUsername("u2d");
                    config.setPassword("EnPJArbx8c87xAaf");

                    config.setMaximumPoolSize(4);

                    config.setPoolName("U2D - Statistics DB");

                    dataSource = new HikariDataSource(config);
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        runStatementSync("CREATE TABLE IF NOT EXISTS "+tablename+" (id varchar(6) NOT NULL, name TEXT, author TEXT, description TEXT, version TEXT, premium TEXT, notified TEXT, disabled TEXT, PRIMARY KEY(id))");
                        runStatementSync("CREATE TABLE IF NOT EXISTS " + statstable + " (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, downloadsize INT, downloadedfiles INT, pluginstracked INT)");

                        if (Up2Date.getInstance().getMainConfig().getServerId() == 0) {
                            int serverId;
                            ResultSet rs = runQuery("SELECT MAX(id) FROM "+statstable);
                            if (rs != null) {
                                try {
                                    rs.first();

                                    serverId = rs.getInt(1) + 1;
                                    Up2Date.getInstance().getMainConfig().setServerId(serverId);
                                } catch (SQLException e) {
                                    Up2Date.getInstance().systemOutPrintError(e, "Error occurred while retrieving server ID from database.");
                                }
                            }
                            runStatementSync("INSERT INTO " + statstable + " (downloadsize, downloadedfiles, pluginstracked) VALUES ('0', '0', '0')");
                        }
                    }
                }.runTaskLater(Up2Date.getInstance(), 20L);
            }
        }.runTaskAsynchronously(Up2Date.getInstance());

        //Statistics refreshing
        new BukkitRunnable() {
            @Override
            public void run() {
                /*INSERT INTO statistics
                *
                * (id, downloadsize, downloadedfiles, pluginstracked)
                * VALUES(1, 1, 1, 1)
                *
                * ON DUPLICATE KEY UPDATE
                *
                * downloadsize = downloadsize+1,
                * downloadedfiles = downloadedfiles+1,
                * pluginstracked = pluginstracked+1
                */
                runStatementSync("INSERT INTO " + statstable + " (id, downloadsize, downloadedfiles, pluginstracked) VALUES ('"+Up2Date.getInstance().getMainConfig().getServerId()+"', '"+downloadsize+"', '"+downloadedfiles+"', '"+pluginstracked+"') ON DUPLICATE KEY UPDATE downloadsize = downloadsize+"+downloadsize+", downloadedfiles = downloadedfiles+"+downloadedfiles+", pluginstracked = "+pluginstracked);
            }
        }.runTaskTimerAsynchronously(Up2Date.getInstance(), 60L, 10*20*60);
    }

    public void shutdown() {
        if (dataSource != null)
            dataSource.close();
    }

    public void saveDataNow() {
        runStatementSync("INSERT INTO " + statstable + " (id, downloadsize, downloadedfiles, pluginstracked) VALUES ('"+Up2Date.getInstance().getMainConfig().getServerId()+"', '"+downloadsize+"', '"+downloadedfiles+"', '"+pluginstracked+"') ON DUPLICATE KEY UPDATE downloadsize = downloadsize+"+downloadsize+", downloadedfiles = downloadedfiles+"+downloadedfiles+", pluginstracked = pluginstracked+"+pluginstracked);
    }

    public void addIncompatiblePlugin(PluginInfo info) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    boolean notified = false;

                    ResultSet rs = runQuery("SELECT version FROM TABLENAME WHERE id = '"+info.getId()+"'");
                    if (rs != null && rs.first() && !rs.isClosed()) {
                        String version = rs.getString("version");
                        if (!version.equalsIgnoreCase(info.getLatestVersion())) {
                            notified = true;
                        }
                    }

                    runStatement("INSERT INTO TABLENAME (id, name, author, version, description, premium, notified) VALUES ('" + info.getId() + "','" + info.getName() + "', '" + info.getAuthor() + "', '" + info.getLatestVersion() + "', '" + info.getDescription() + "', '" + info.isPremium() + "', 'true') ON DUPLICATE KEY UPDATE notified = '"+notified+"'");
                } catch (Exception ex) {
                    Up2Date.getInstance().printError(ex, "Error occurred while checking version difference.");
                }
            }
        }.runTaskAsynchronously(Up2Date.getInstance());
    }

    public ArrayList<PluginInfo> getIncompatiblePlugins(ArrayList<PluginInfo> infos) {

        ArrayList<PluginInfo> incompatibles = new ArrayList<>();

        StringBuilder sb = new StringBuilder();

        sb.append("SELECT * FROM TABLENAME WHERE id IN (");
        sb.append(infos.get(0).getId());
        if (infos.size() > 1) {
            sb.append(",");

            for (int i = 1; i < infos.size(); i++) {
                if (i + 1 == infos.size()) {
                    sb.append(infos.get(i).getId());
                    sb.append(")");
                } else {
                    sb.append(infos.get(i).getId());
                    sb.append(",");
                }
            }
        } else
            sb.append(")");


        ResultSet rs = runQuery(sb.toString());

        try {
            if (rs != null && rs.first() && !rs.isClosed()) {
                while (rs.next())
                    incompatibles.add(new PluginInfo(rs.getString("name"), rs.getInt("id"), rs.getString("author"), rs.getString("version"), rs.getString("description"), rs.getBoolean("premium"), ""));
                rs.close();
            }
        } catch (Exception ex) {
            Up2Date.getInstance().printError(ex, "Error occurred while reading result set.");
        }

        return incompatibles;
    }

    /*
     * SQL UTILITIES
     */

    private void runStatement(String statement) {
        final String updatedStatement = statement.replace("TABLENAME", tablename);

        Connection connection;

        if (dataSource == null)
            init();

        try {
            connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(updatedStatement);

            ExecutorService pool = Up2Date.getInstance().getFixedThreadPool();

            pool.submit(() -> {
                try {
                    preparedStatement.execute();

                    connection.close();
                } catch (Exception ex) {
                    Up2Date.getInstance().systemOutPrintError(ex, "Error occurred while executing statement to private DB.");
                }
            });
        }
        catch(Exception ex) {
            Up2Date.getInstance().printError(ex, "Error occurred while running MySQL statement");
        }
    }

    private void runStatementSync(String statement) {
        final String updatedStatement = statement.replace("TABLENAME", tablename);

        if (dataSource == null)
            return;

        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(updatedStatement);

            try {
                preparedStatement.execute();

                connection.close();
            } catch (Exception ex) {
                Up2Date.getInstance().systemOutPrintError(ex, "Error occurred while closing connection");
            } finally {
                connection.close();
            }
        }
        catch(Exception ex) {
            Up2Date.getInstance().printError(ex, "Error occurred while running MySQL statement.");
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException ex) {
               Up2Date.getInstance().printError(ex, "Error occurred while closing SQL connection!");
            }
        }
    }

    private ResultSet runQuery(String query) {
        final String updatedQuery = query.replace("TABLENAME", tablename);

        Connection connection;

        if (dataSource == null) {
            init();
            return null;
        }

        try {
            connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery);

            //Give whatever task that is using this 2 seconds to complete it, TODO find a better way to close the connection.
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        connection.close();
                    }
                    catch(Exception ex) {
                        Up2Date.getInstance().printError(ex, "Error occurred while closing connection");
                    }
                }
            }.runTaskLater(Up2Date.getInstance(), 40L);

            return preparedStatement.executeQuery();
        } catch(Exception ex) {
            Up2Date.getInstance().printError(ex, "Error occurred while running query '"+updatedQuery+"'.");
        }

        return null;
    }

    /*
     * ADDERS
     */

    public void addDownloadsize(float downloadsize) {
        this.downloadsize += Double.valueOf(String.format("%.3f", downloadsize/1024).replace(",", ""));
    }

    public void addDownloadedFiles(int downloadedfiles) {
        this.downloadedfiles += downloadedfiles;
    }


    /* TODO add stats command, use this code.



                            ResultSet rs = runQuery("SELECT * FROM "+statstable+" WHERE id ='" + Up2Date.getInstance().getMainConfig().getServerId() + "'");
                            try {
                                if (rs != null && !rs.isClosed()) {
                                    rs.first();

                                    downloadedfiles = rs.getInt("downloadedfiles");
                                    downloadsize = rs.getInt("downloadsize");
                                    pluginstracked = rs.getInt("pluginstracked");
                                } else {
                                    Up2Date.getInstance().printPluginError("Error occurred while managing statistics.", "Invalid resultset given!");
                                }
                            } catch (SQLException e) {
                                Up2Date.getInstance().systemOutPrintError(e, "Error occurred while retrieving server statistics.");
                            }
     */
}
