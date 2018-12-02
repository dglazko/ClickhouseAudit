package ru.anarok.audit;

import ru.anarok.audit.impl.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClickhouseAuditFactory {
    private final String hostname;
    private ClickhouseIdProvider idProvider = new DefaultIdProvider();
    private ClickhouseErrorHandler errorHandler = new Slf4jErrorHandler();
    private ClickhouseConnection connection;
    private ExecutorService executorService;
    private int port = 8123;
    private String database;
    private String username;
    private String password;

    private ClickhouseAuditFactory(String hostname) {
        this.hostname = hostname;
    }

    public static ClickhouseAuditFactory create(String hostname) {
        return new ClickhouseAuditFactory(hostname);
    }

    public ClickhouseAuditFactory port(int port) {
        this.port = port;
        return this;
    }

    public ClickhouseAuditFactory database(String database) {
        this.database = database;
        return this;
    }

    public ClickhouseAuditFactory username(String username) {
        this.username = username;
        return this;
    }

    public ClickhouseAuditFactory password(String password) {
        if (this.username == null)
            this.username = "default";
        this.password = password;
        return this;
    }

    public ClickhouseAuditFactory idProvider(ClickhouseIdProvider provider) {
        this.idProvider = provider;
        return this;
    }

    public ClickhouseAuditFactory errorHandler(ClickhouseErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public ClickhouseAuditFactory executorService(ExecutorService service) {
        this.executorService = service;
        return this;
    }

    public ClickhouseAuditFactory connection(ClickhouseConnection connection) {
        this.connection = connection;
        return this;
    }

    public ClickhouseAuditService build() {
        ClickhouseConnection connection = internalGetConnection();
        ExecutorService executorService = internalGetExecutor();


        return new DefaultAuditService(executorService, connection, errorHandler);
    }

    private ClickhouseConnection internalGetConnection() {
        if (this.connection != null)
            return this.connection;

        DefaultConnection connection;
        String uri = "jdbc:clickhouse://" + hostname + ":" + port;

        if (database != null)
            uri += "/" + database;
        connection = new DefaultConnection(idProvider, uri, username, password);
        return connection;
    }

    private ExecutorService internalGetExecutor() {
        if (executorService == null) {
            return Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread(r);
                thread.setDaemon(false);
                thread.setName("Clickhouse Executor Thread");

                return thread;
            });
        } else return executorService;
    }
}
