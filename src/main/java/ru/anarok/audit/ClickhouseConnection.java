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

    public <T> ClickhouseTable<T> table(Class<T> tableClass) throws SQLException {
        AuditTable tableNameAnnotation = tableClass.getAnnotation(AuditTable.class);
        String tableName = tableClass.getSimpleName().toLowerCase();
        if (tableNameAnnotation != null && !tableNameAnnotation.value().isEmpty())
            tableName = tableNameAnnotation.value();
        Field[] declaredFields = tableClass.getDeclaredFields();

        if (!tableSchemaMap.containsKey(tableName)) {
            createTable(tableName, declaredFields);
        }

        return new ClickhouseTable<T>();
    }

    private void createTable(String tableName, Field[] declaredFields) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append("(");
        for (Field declaredField : declaredFields) {
            sb.append(declaredField.getName()).append(" ");
            sb.append(getDatabaseDataType(declaredField.getType()));
            if (declaredField != declaredFields[declaredFields.length - 1])
                sb.append(", ");
        }
        sb.append(") ENGINE = Memory()");
        System.out.println(sb.toString());
        executeQuery(sb.toString());
    }

    private String getDatabaseDataType(Class prim) {
        if (prim == byte.class) return "Int8";
        if (prim == short.class) return "Int16";
        if (prim == int.class) return "Int32";
        if (prim == long.class) return "Int64";
        if (prim == float.class) return "Float32";
        if (prim == double.class) return "Float64";
        if (prim == boolean.class) return "UInt8";
        if (prim == char.class) return "UInt16";
        if (prim == String.class) return "String";
        throw new IllegalArgumentException("Unknown datatype " + prim.getName());
    }

    private void getRemoteSchema() throws SQLException {
        ResultSet rs = executeQuery("select * from system.columns");
        tableSchemaMap.clear();
        while (rs.next()) {
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
