package ru.anarok.audit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class AccessTest {
    @Test
    public void test() throws SQLException {
        ClickhouseConnection connection = new ClickhouseConnection();
        connection.connect("localhost");

        ResultSet resultSet = connection.executeQuery("SELECT * FROM (" +
                "SELECT * FROM Audits" +
                ") ALL INNER JOIN (" +
                " SELECT * FROM AuditAttributes" +
                ") USING auditId " +
                "ORDER BY auditId, name");
        while (resultSet.next()) {
            log.info(
                    "'{}' '{}' '{}' '{}' '{}' {} => {}",
                    resultSet.getLong("auditId"),
                    resultSet.getString("timestamp"),
                    resultSet.getString("emitter"),
                    resultSet.getString("type"),
                    resultSet.getString("message"),
                    resultSet.getString("name"),
                    resultSet.getString("value")
            );
        }
    }

    @Test
    @Disabled("For internal testing purposes")
    public void dropTables() throws SQLException {
        ClickhouseConnection connection = new ClickhouseConnection();
        connection.connect("localhost");

        connection.executeQuery("DROP TABLE Audits");
        connection.executeQuery("DROP TABLE AuditAttributes");

    }
}
