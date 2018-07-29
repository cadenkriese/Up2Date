package com.gamerking195.dev.up2date.util;

import com.gamerking195.dev.up2date.Up2Date;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;

/**
 * Created by Caden Kriese (GamerKing195) on 9/8/17.
 * <p>
 * License is specified by the distributor which this
 * file was written for. Otherwise it can be found in the LICENSE file.
 * If there is no license file the code is then completely copyrighted
 * and you must contact me before using it IN ANY WAY.
 */

public class UtilSQL {
    private UtilSQL() {}
    private static UtilSQL instance = new UtilSQL();
    public static UtilSQL getInstance() {
        return instance;
    }

    private HikariDataSource dataSource;

    public void init() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://"+ Up2Date.getInstance().getMainConfig().getHostName()+"/"+ Up2Date.getInstance().getMainConfig().getDatabase());
            config.setUsername(Up2Date.getInstance().getMainConfig().getUsername());
            config.setPassword(Up2Date.getInstance().getMainConfig().getPassword());

            config.setMaximumPoolSize(Up2Date.getInstance().getMainConfig().getConnectionPoolSize());

            config.setPoolName("U2D - User DB ("+Up2Date.getInstance().getMainConfig().getUsername()+"@"+Up2Date.getInstance().getMainConfig().getHostName()+")");

            dataSource = new HikariDataSource(config);
        }

        runStatementSync("CREATE TABLE IF NOT EXISTS TABLENAME (id varchar(6) NOT NULL, name TEXT, author TEXT, description TEXT, version TEXT, premium TEXT, testedversions TEXT, lastupdated TIMESTAMP, PRIMARY KEY(id))");
    }

    public void runStatement(String statement) {
        final String updatedStatement = statement.replace("TABLENAME", Up2Date.getInstance().getMainConfig().getTablename());

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
                    //cant do fancy error logging bc it does bukkit calls ;-;
                    ex.printStackTrace();
                }
            });
        }
        catch(Exception ex) {
            Up2Date.getInstance().printError(ex, "Error occurred while running MySQL statement.");
        }
    }

    public void runStatement(String statement, boolean supressErrors) {
        final String updatedStatement = statement.replace("TABLENAME", Up2Date.getInstance().getMainConfig().getTablename());

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
                    //cant do fancy error logging bc it does bukkit calls ;-;
                    ex.printStackTrace();
                }
            });
        }
        catch(Exception ex) {
            if (supressErrors)
                return;

            Up2Date.getInstance().printError(ex, "Error occurred while running MySQL statement.");
        }
    }

    public void runStatementSync(String statement) {
        final String updatedStatement = statement.replace("TABLENAME", Up2Date.getInstance().getMainConfig().getTablename());

        if (dataSource == null)
            init();

        Connection connection;

        try {
            connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(updatedStatement);

            try {
                preparedStatement.execute();

                connection.close();
            } catch (Exception ex) {
                Up2Date.getInstance().systemOutPrintError(ex, "Error occurred while closing connection.");
            }
        }
        catch(Exception ex) {
            Up2Date.getInstance().printError(ex, "Error occurred while running MySQL statement.");
        }
    }

    public void runStatementSync(String statement, boolean supressErrors) {
        final String updatedStatement = statement.replace("TABLENAME", Up2Date.getInstance().getMainConfig().getTablename());

        if (dataSource == null)
            init();

        Connection connection;

        try {
            connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(updatedStatement);

            try {
                preparedStatement.execute();

                connection.close();
            } catch (Exception ex) {
                if (supressErrors)
                    return;

                Up2Date.getInstance().systemOutPrintError(ex, "Error occurred while closing connection.");
            }
        }
        catch(Exception ex) {
            if (supressErrors)
                return;

            Up2Date.getInstance().printError(ex, "Error occurred while running MySQL statement.");
        }
    }

    public ResultSet runQuery(String query) {
        final String updatedQuery = query.replace("TABLENAME", Up2Date.getInstance().getMainConfig().getTablename());

        Connection connection;

        try {
            connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery);

            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        connection.close();
                    }
                    catch(Exception ex) {
                        Up2Date.getInstance().printError(ex, "Error occurred while closing connection.");
                    }
                }
            }.runTaskLater(Up2Date.getInstance(), 40L);

            return preparedStatement.executeQuery();
        }
        catch(Exception ex) {
            Up2Date.getInstance().printError(ex, "Error occurred while running query '"+updatedQuery+"'.");
        }

        return null;
    }
}
