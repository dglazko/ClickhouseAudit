package ru.anarok.audit;

import cn.danielw.fop.ObjectFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestConnectionFactory implements ObjectFactory<ClickhouseConnection> {
    private final TestConnectionInsertHandler callback;

    @Override
    public ClickhouseConnection create() {
        return new ClickhouseConnection() {
            @Override
            public void close() {

            }

            @Override
            public void connect() {

            }

            @Override
            public void insert(AuditEvent e) throws Exception {
                callback.insert(e);
            }
        };
    }

    @Override
    public void destroy(ClickhouseConnection clickhouseConnection) {

    }

    @Override
    public boolean validate(ClickhouseConnection clickhouseConnection) {
        return true;
    }

    public interface TestConnectionInsertHandler {
        void insert(AuditEvent e) throws Exception;
    }
}
