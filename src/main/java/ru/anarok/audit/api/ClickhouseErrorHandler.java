package ru.anarok.audit.api;

import ru.anarok.audit.impl.AuditEvent;

public interface ClickhouseErrorHandler {
    void onInsertFailed(AuditEvent audit, Throwable throwable);
}
