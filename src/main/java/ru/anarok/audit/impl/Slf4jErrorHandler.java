package ru.anarok.audit.impl;

import lombok.extern.slf4j.Slf4j;
import ru.anarok.audit.ClickhouseErrorHandler;

@Slf4j
public class Slf4jErrorHandler implements ClickhouseErrorHandler {

    @Override
    public void onInsertFailed(AuditEvent audit, Throwable throwable) {
        log.error("Unable to write audit to the Clickhouse database [" + audit.toString() + "]",
                throwable);
    }
}
