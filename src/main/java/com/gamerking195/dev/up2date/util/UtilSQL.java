package com.gamerking195.dev.up2date.util;

import com.gamerking195.dev.up2date.Up2Date;
import com.gamerking195.dev.up2date.config.MainConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;


/**
 * @author Caden Kriese (flogic)
 * <p>
 * Created on 9/8/17
 */

public class UtilSQL {
    private UtilSQL() {
    }

    private static UtilSQL instance = new UtilSQL();

    public static UtilSQL getInstance() {
        return instance;
    }

    private HikariDataSource dataSource;

    public void init() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + MainConfig.getConf().getHostName() + "/" + MainConfig.getConf().getDatabase());
            config.setUsername(MainConfig.getConf().getUsername());
            config.setPassword(MainConfig.getConf().getPassword());

            config.setMaximumPoolSize(MainConfig.getConf().getConnectionPoolSize());

            config.setPoolName("U2D - User DB (" + MainConfig.getConf().getUsername() + "@" + MainConfig.getConf().getHostName() + ")");

            dataSource = new HikariDataSource(config);
        }

        runStatementSync("CREATE TABLE IF NOT EXISTS TABLENAME (id varchar(6) NOT NULL, name TEXT, author TEXT, description TEXT, version TEXT, premium TEXT, testedversions TEXT, lastupdated TIMESTAMP, PRIMARY KEY(id))");
    }

    public void shutdown() {
        if (dataSource != null)
            dataSource.close();
    }

    public void runStatement(String statement) {
        final String updatedStatement = statement.replace("TABLENAME", MainConfig.getConf().getTablename());

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
                    preparedStatement.close();

                    connection.close();

                } catch (Exception ex) {
                    Up2Date.getInstance().printError(ex, "Error occurred while running MySQL statement.");
                } finally {
                    try {
                        preparedStatement.close();
                        connection.close();
                    } catch (SQLException ex) {
                        Up2Date.getInstance().printError(ex, "Error occurred while running MySQL statement.");
                    }
                }
            });
        } catch (Exception ex) {
            Up2Date.getInstance().printError(ex, "Error occurred while running MySQL statement.");
        }
    }

    public void runStatement(String statement, boolean supressErrors) {
        final String updatedStatement = statement.replace("TABLENAME", MainConfig.getConf().getTablename());

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
                    preparedStatement.close();

                    connection.close();
                } catch (Exception ex) {
                    Up2Date.getInstance().printError(ex, "Error occurred while running MySQL statement.");
                } finally {
                    try {
                        preparedStatement.close();
                        connection.close();
                    } catch (Exception ex) {
                        Up2Date.getInstance().printError(ex, "Error occurred while running MySQL statement.");
                    }
                }
            });
        } catch (Exception ex) {
            if (supressErrors)
                return;

            Up2Date.getInstance().printError(ex, "Error occurred while running MySQL statement.");
        }
    }

    public void runStatementSync(String statement) {
        final String updatedStatement = statement.replace("TABLENAME", MainConfig.getConf().getTablename());

        if (dataSource == null)
            init();

        Connection connection;

        try {
            connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(updatedStatement);

            try {
                preparedStatement.execute();
                preparedStatement.close();

                connection.close();
            } catch (Exception ex) {
                Up2Date.getInstance().systemOutPrintError(ex, "Error occurred while closing connection.");
            } finally {
                preparedStatement.close();
                connection.close();
            }
        } catch (Exception ex) {
            Up2Date.getInstance().printError(ex, "Error occurred while running MySQL statement.");
        }
    }

    public void runStatementSync(String statement, boolean supressErrors) {
        final String updatedStatement = statement.replace("TABLENAME", MainConfig.getConf().getTablename());

        if (dataSource == null)
            init();

        Connection connection;

        try {
            connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(updatedStatement);

            try {
                preparedStatement.execute();
                preparedStatement.close();

                connection.close();
            } catch (Exception ex) {
                if (supressErrors)
                    return;

                Up2Date.getInstance().systemOutPrintError(ex, "Error occurred while closing connection.");
            } finally {
                preparedStatement.close();
                connection.close();
            }
        } catch (Exception ex) {
            if (supressErrors)
                return;

            Up2Date.getInstance().printError(ex, "Error occurred while running MySQL statement.");
        }
    }

    public ResultSet runQuery(String query) {
        final String updatedQuery = query.replace("TABLENAME", MainConfig.getConf().getTablename());

        Connection connection;

        try {
            connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery);

            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        connection.close();
                    } catch (Exception ex) {
                        Up2Date.getInstance().printError(ex, "Error occurred while closing connection.");
                    }
                }
            }.runTaskLater(Up2Date.getInstance(), 40L);

            return preparedStatement.executeQuery();
        } catch (Exception ex) {
            Up2Date.getInstance().printError(ex, "Error occurred while running query '" + updatedQuery + "'.");
        }

        return null;
    }
}
