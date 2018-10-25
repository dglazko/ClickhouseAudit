package ru.anarok.audit;

import java.sql.SQLException;

public interface ClickhouseConnection {
    void connect() throws SQLException;

    void insert(AuditEvent e) throws SQLException;

    void shutdownImmediately() throws SQLException;
}
