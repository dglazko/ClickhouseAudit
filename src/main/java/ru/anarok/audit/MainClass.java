package ru.anarok.audit;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MainClass {

    public static void main(String[] args) throws SQLException {
        ClickhouseConnection connection = new ClickhouseConnection();
        connection.connect("localhost");

        ResultSet set = connection.executeQuey("SELECT 1");

        while (set.next()) {
            System.out.println(set.getInt(1));
        }

    }
}
