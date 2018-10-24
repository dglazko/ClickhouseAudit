package ru.anarok.audit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClickhouseConnection {
    private Connection connection;

    public void connect(String host) throws SQLException {
        connect(host, 8123, null, null, null);
    }

    public void connect(String host, int port, String database, String username, String password) throws SQLException {
        assert host != null;

        String uri = "jdbc:clickhouse://" + host + ":" + port;

        if (database != null)
            uri += "/" + database;

        if (username == null)
            connection = DriverManager.getConnection(uri);
        else
            connection = DriverManager.getConnection(uri, username, password);
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return connection.prepareStatement(sql).executeQuery();
    }


    public void shutdownImmediately() throws SQLException {
        connection.close();
    }

    public <T> ClickhouseTable<T> table(Class<T> tableClass){
        return null;
    }
}
