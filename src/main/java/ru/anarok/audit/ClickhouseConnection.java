package ru.anarok.audit;

import javafx.scene.control.Tab;
import ru.anarok.audit.internal.ColumnSchema;
import ru.anarok.audit.internal.TableSchema;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ClickhouseConnection {
    private Connection connection;
    private Map<String, TableSchema> tableSchemaMap = new HashMap<>();

    public void connect(String host) throws SQLException {
        connect(host, 8123, null, null, null);
    }

    public void connect(String host, int port, String database, String username, String password) throws SQLException {
        assert host != null;

        String uri = "jdbc:clickhouse://" + host + ":" + port;

        if (database != null)
            uri += "/" + database;

        if (username == null)
            connection = DriverManager.getConnection(uri);
        else
            connection = DriverManager.getConnection(uri, username, password);
        getRemoteSchema();
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return connection.prepareStatement(sql).executeQuery();
    }


    public void shutdownImmediately() throws SQLException {
        connection.close();
    }

    public <T> ClickhouseTable<T> table(Class<T> tableClass){
        Field[] declaredFields = tableClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            System.out.println(declaredField);
        }
        return new ClickhouseTable<T>();
    }

    private void getRemoteSchema() throws SQLException {
        ResultSet rs = executeQuery("select * from system.columns");
        tableSchemaMap.clear();
        while(rs.next()){
            TableSchema tableSchema =
                tableSchemaMap.computeIfAbsent(rs.getString("table"), TableSchema::new);
            ColumnSchema columnSchema = new ColumnSchema(
                rs.getString("name"),
                rs.getString("type"),
                rs.getString("default_kind"),
                rs.getString("default_expression")
            );
            tableSchema.getColumnSchemaList().add(columnSchema);
        }
        System.out.println("Table schema is ready");
    }
}
