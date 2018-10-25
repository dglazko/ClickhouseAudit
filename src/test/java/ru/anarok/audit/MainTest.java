package ru.anarok.audit;

import org.junit.jupiter.api.Test;
import ru.anarok.audit.domain.AuditEvent;

import java.sql.SQLException;

class MainTest {

    @Test
    void comprehensiveTest() throws SQLException {
        ClickhouseConnection connection = new ClickhouseConnection();
        connection.connect("10.48.40.178");

        while (true) {
            AuditEvent authEvent = new AuditEvent("Auth Server", "Auth Event", "User loggined in");
            authEvent.getData().put("user_name", "iisereb");
            authEvent.getData().put("nonce", String.valueOf(System.currentTimeMillis() % 1000));

            connection.insert(authEvent);
        }
    }
}
