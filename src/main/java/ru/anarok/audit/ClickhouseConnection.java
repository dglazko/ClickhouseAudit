package ru.anarok.audit;

import java.sql.SQLException;

public interface ClickhouseConnection extends AutoCloseable {
    void connect() throws SQLException;

    void insert(AuditEvent e) throws Exception;
}
