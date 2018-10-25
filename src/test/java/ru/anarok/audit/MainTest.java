package ru.anarok.audit;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

class MainTest {

    @Test
    void comprehensiveTest() throws SQLException {
        ClickhouseConnection connection = new ClickhouseConnection();
        connection.connect("localhost");

        ClickhouseTable<AuthAudit> table = connection.table(AuthAudit.class);

        table.insert(new AuthAudit("Test User 1", "Test ip 1", false));
        table.insert(new AuthAudit("Test User 2", "Test ip 2", true));
    }
}
