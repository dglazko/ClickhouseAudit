package ru.anarok.audit.impl;

import lombok.extern.slf4j.Slf4j;
import ru.anarok.audit.AuditEvent;
import ru.anarok.audit.ClickhouseConnection;
import ru.anarok.audit.ClickhouseIdProvider;

import java.sql.*;
import java.util.Map;

@Slf4j
public class DefaultConnection implements ClickhouseConnection {
    private final ClickhouseIdProvider idProvider;
    private final String uri;
    private final String username;
    private final String password;
    private Connection connection;

    public DefaultConnection(ClickhouseIdProvider idProvider, String uri, String username, String password) {
        this.idProvider = idProvider;
        this.uri = uri;
        this.username = username;
        this.password = password;
    }

    public void connect() throws SQLException {
        if (username == null)
            connection = DriverManager.getConnection(uri);
        else
            connection = DriverManager.getConnection(uri, username, password);

        createTables();
    }

    private void createTables() throws SQLException {
        executeQuery(
                "CREATE TABLE IF NOT EXISTS Audits (" +
                        "auditId UInt64," +
                        "timestamp DateTime," +
                        "emitter String, " +
                        "type String," +
                        "message String" +
                        ") ENGINE = MergeTree() " +
                        "PARTITION BY toYYYYMM(timestamp) " +
                        "ORDER BY timestamp " +
                        "SETTINGS index_granularity=8192"
        );

        executeQuery(
                "CREATE TABLE IF NOT EXISTS AuditAttributes (" +
                        "auditId UInt64," +
                        "name String," +
                        "value String" +
                        ") ENGINE = MergeTree() " +
                        "PARTITION BY (auditId, name) " +
                        "ORDER BY (auditId, name) " +
                        "SETTINGS index_granularity=8192"
        );
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return connection.prepareStatement(sql).executeQuery();
    }

    @Override
    public void insert(AuditEvent e) throws SQLException {
        long id = idProvider.newRecordId();
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO Audits (auditId, timestamp, emitter, type, message)" +
                        "VALUES (?, ?, ?, ?, ?)"
        );
        stmt.setLong(1, id);
        stmt.setLong(2, e.getTimestamp() / 1000);
        stmt.setString(3, e.getEmitter());
        stmt.setString(4, e.getType());
        stmt.setString(5, e.getMessage());
        stmt.executeUpdate();
        stmt.close();

        for (Map.Entry<String, String> stringStringEntry : e.getData().entrySet()) {
            stmt = connection.prepareStatement(
                    "INSERT INTO AuditAttributes (auditId, name, value) VALUES (?,?,?)"
            );
            stmt.setLong(1, id);
            stmt.setString(2, stringStringEntry.getKey());
            stmt.setString(3, stringStringEntry.getValue());
            stmt.executeUpdate();
            stmt.close();
        }
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
