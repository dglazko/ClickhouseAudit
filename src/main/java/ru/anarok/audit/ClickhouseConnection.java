package ru.anarok.audit;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
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
        executeQuery("CREATE TABLE IF NOT EXISTS Audits (" +
            "id UInt64 PRIMARY KEY AUTO_INCREMENT" +
            ") ENGINE = MergeTree()");
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return connection.prepareStatement(sql).executeQuery();
    }


    public void shutdownImmediately() throws SQLException {
        connection.close();
    }

}
