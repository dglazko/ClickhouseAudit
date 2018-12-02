package ru.anarok.audit.api;

import ru.anarok.audit.impl.DefaultConnection;
import ru.anarok.audit.impl.DefaultIdProvider;

public class ClickhouseConnectionFactory {
    private final String hostname;
    private ClickhouseIdProvider idProvider = new DefaultIdProvider();
    private int port = 8123;
    private String database;
    private String username;
    private String password;

    private ClickhouseConnectionFactory(String hostname) {
        this.hostname = hostname;
    }

    public static ClickhouseConnectionFactory create(String hostname) {
        return new ClickhouseConnectionFactory(hostname);
    }

    public ClickhouseConnectionFactory port(int port) {
        this.port = port;
        return this;
    }

    public ClickhouseConnectionFactory database(String database) {
        this.database = database;
        return this;
    }

    public ClickhouseConnectionFactory username(String username) {
        this.username = username;
        return this;
    }

    public ClickhouseConnectionFactory password(String password) {
        if (this.username == null)
            this.username = "default";
        this.password = password;
        return this;
    }

    public ClickhouseConnectionFactory idProvider(ClickhouseIdProvider provider) {
        this.idProvider = provider;
        return this;
    }

    public ClickhouseConnection build() {
        String uri = "jdbc:clickhouse://" + hostname + ":" + port;

        if (database != null)
            uri += "/" + database;


        return new DefaultConnection(idProvider, uri, username, password);
    }
}