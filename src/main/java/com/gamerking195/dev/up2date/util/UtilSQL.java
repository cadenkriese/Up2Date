package com.gamerking195.dev.up2date.util;

import com.gamerking195.dev.up2date.Up2Date;
import com.gamerking195.dev.up2date.config.MainConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 * @author Caden Kriese (flogic)
 *
 * Created on 9/8/17
 */

public class UtilSQL {
    private UtilSQL() {}
    private @Getter static UtilSQL instance = new UtilSQL();

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

        runStatement("CREATE TABLE IF NOT EXISTS TABLENAME " +
                             "(id varchar(6) NOT NULL, " +
                             "name TEXT, " +
                             "author TEXT, " +
                             "description TEXT, " +
                             "version TEXT, " +
                             "premium TEXT, " +
                             "testedversions TEXT, " +
                             "lastupdated TIMESTAMP, " +
                             "PRIMARY KEY(id))", true);
    }

    public void shutdown() {
        if (dataSource != null)
            dataSource.close();
    }

    /*
     * QUERIES
     */


    /**
     * Executes a query on the user database on a separate thread.
     *
     * @param query The query to be run.
     * @param callback The callback to be called with the result set.
     */
    public void executeQueryAsync(String query, Callback callback) {
        final String updatedQuery = query.replace("TABLENAME", MainConfig.getConf().getTablename());

        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery)) {
            Up2Date.getInstance().getFixedThreadPool().submit(() -> {
                try {
                    callback.call(preparedStatement.executeQuery());
                } catch (Exception ex) {
                    Up2Date.getInstance().systemOutPrintError(ex, "Error occurred while executing query.");
                }
            });
        } catch (Exception ex) {
            Up2Date.getInstance().printError(ex, "Error occurred while running query '" + updatedQuery + "'.");
        }
    }

    /**
     * Executes a query on the user database on the current thread.
     *
     * @param query The query to be executed.
     * @return The result of the query.
     */
    public ResultSet executeQuery(String query) {
        final String updatedQuery = query.replace("TABLENAME", MainConfig.getConf().getTablename());

        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery)) {
            return preparedStatement.executeQuery();
        } catch (Exception ex) {
            Up2Date.getInstance().printError(ex, "Error occurred while running query '" + updatedQuery + "'.");
        }

        return null;
    }

    /*
     * STATEMENTS
     */

    /**
     * Executes an SQL statement on the users database.
     *
     * @param statement The statement to be executed.
     * @param synchronous Should the statement be executed on the main thread of a new thread from the pool.
     * @param suppressErrors Should errors be printed to console.
     */
    public void runStatement(String statement, boolean synchronous, boolean suppressErrors) {
        if (synchronous)
            executeStatement(statement, suppressErrors);
        else
            Up2Date.getInstance().getFixedThreadPool().submit(() -> executeStatement(statement, suppressErrors));
    }

    /**
     * Executes an SQL statement on the users database.
     *
     * @param statement The statement to be executed.
     * @param synchronous Should the statement be executed on the main thread of a new thread from the pool.
     */
    public void runStatement(String statement, boolean synchronous) {
        if (synchronous)
            executeStatement(statement, false);
        else
            Up2Date.getInstance().getFixedThreadPool().submit(() -> executeStatement(statement, false));
    }

    private void executeStatement(String statement, boolean suppressErrors) {
        final String updatedStatement = statement.replace("TABLENAME", MainConfig.getConf().getTablename());
        if (dataSource == null)
            init();

        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(updatedStatement)) {
            preparedStatement.execute();
        } catch (Exception ex) {
            if (suppressErrors)
                return;
            Up2Date.getInstance().systemOutPrintError(ex, "Error occurred while closing connection.");
        }
    }
}
