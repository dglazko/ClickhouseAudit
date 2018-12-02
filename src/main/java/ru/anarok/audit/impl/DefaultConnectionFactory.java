package ru.anarok.audit.impl;


import cn.danielw.fop.ObjectFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.anarok.audit.ClickhouseConnection;
import ru.anarok.audit.ClickhouseIdProvider;

import java.sql.SQLException;

@RequiredArgsConstructor
@Slf4j
public class DefaultConnectionFactory implements ObjectFactory<ClickhouseConnection> {
    private final ClickhouseIdProvider idProvider;
    private final String uri;
    private final String username;
    private final String password;

    @Override
    public ClickhouseConnection create() {
        DefaultConnection connection = new DefaultConnection(idProvider, uri, username, password);
        try {
            connection.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    @Override
    public void destroy(ClickhouseConnection clickhouseConnection) {
        try {
            clickhouseConnection.close();
        } catch (Exception e) {
            log.error("Unable to close Clickhouse Connection", e);
        }
    }

    @Override
    public boolean validate(ClickhouseConnection clickhouseConnection) {
        return true;
    }
}
