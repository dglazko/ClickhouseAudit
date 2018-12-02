package ru.anarok.audit;

import cn.danielw.fop.ObjectFactory;
import cn.danielw.fop.ObjectPool;
import cn.danielw.fop.PoolConfig;
import ru.anarok.audit.impl.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClickhouseAuditFactory {
    private ExecutorService executorService;
    private ObjectFactory<ClickhouseConnection> connectionFactory;
    private PoolConfig poolConfig;

    private ClickhouseIdProvider idProvider = new DefaultIdProvider();
    private ClickhouseErrorHandler errorHandler = new Slf4jErrorHandler();

    private int port = 8123;
    private String hostname;
    private String database;
    private String username;
    private String password;

    private ClickhouseAuditFactory(String hostname) {
        this.hostname = hostname;
    }

    private ClickhouseAuditFactory(ObjectFactory<ClickhouseConnection> factory) {
        this.connectionFactory = factory;
    }

    public static ClickhouseAuditFactory create(String hostname) {
        return new ClickhouseAuditFactory(hostname);
    }

    public static ClickhouseAuditFactory create(ObjectFactory<ClickhouseConnection> factory) {
        return new ClickhouseAuditFactory(factory);
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

    public ClickhouseAuditFactory connectionFactory(ObjectFactory<ClickhouseConnection> factory) {
        this.connectionFactory = factory;
        return this;
    }

    public ClickhouseAuditFactory poolConfig(PoolConfig config) {
        this.poolConfig = config;
        return this;
    }

    public ClickhouseAuditService build() {
        ObjectFactory<ClickhouseConnection> connection = internalGetConnection();
        ExecutorService executorService = internalGetExecutor();
        PoolConfig poolConfig = internalGetPoolConfig();

        ObjectPool<ClickhouseConnection> pool = new ObjectPool<>(poolConfig, connection);

        return new DefaultAuditService(executorService, pool, errorHandler);
    }

    private PoolConfig internalGetPoolConfig() {
        if (poolConfig != null)
            return poolConfig;

        PoolConfig config = new PoolConfig();
        config.setPartitionSize(5);
        config.setMaxSize(10);
        config.setMinSize(1);
        config.setMaxIdleMilliseconds(60 * 1000 * 5);

        return config;
    }

    private ObjectFactory<ClickhouseConnection> internalGetConnection() {
        if (connectionFactory != null)
            return connectionFactory;

        String uri = "jdbc:clickhouse://" + hostname + ":" + port + (database != null ? ("/" + database) : "");
        return new DefaultConnectionFactory(idProvider, uri, username, password);
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
