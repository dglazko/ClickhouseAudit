package ru.anarok.audit.test;

import ru.anarok.audit.ClickhouseConnection;
import ru.anarok.audit.ClickhouseTable;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MainClass {

    public static void main(String[] args) throws SQLException {
        ClickhouseConnection connection = new ClickhouseConnection();
        connection.connect("localhost");

        ClickhouseTable<AuthAudit> table = connection.table(AuthAudit.class);

        table.insert(new AuthAudit("Test User 1", "Test ip 1", false));
        table.insert(new AuthAudit("Test User 2", "Test ip 2", true));


    }
}
