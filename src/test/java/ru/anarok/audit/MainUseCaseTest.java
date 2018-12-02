package ru.anarok.audit;

import org.junit.jupiter.api.Test;
import ru.anarok.audit.api.ClickhouseConnection;
import ru.anarok.audit.api.ClickhouseConnectionFactory;
import ru.anarok.audit.impl.AuditEvent;

import java.sql.SQLException;

class MainUseCaseTest {

    @Test
    void insertionTest() throws SQLException {
        ClickhouseConnection connection = ClickhouseConnectionFactory.create("localhost").build();
        connection.connect();

        AuditEvent authEvent = new AuditEvent("Auth Server", "Auth Event", "User logged in");
        authEvent.getData().put("user_name", "iisereb");
        authEvent.getData().put("nonce", String.valueOf(System.currentTimeMillis() % 1000));

        connection.insert(authEvent);
    }
}
