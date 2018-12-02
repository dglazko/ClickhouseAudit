package ru.anarok.audit;

import ru.anarok.audit.impl.AuditEvent;

import java.sql.SQLException;

public interface ClickhouseConnection {
    void connect() throws SQLException;

    void insert(AuditEvent e) throws Exception;

    void shutdownImmediately() throws SQLException;
}
