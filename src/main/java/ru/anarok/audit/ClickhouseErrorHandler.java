package ru.anarok.audit;

import ru.anarok.audit.impl.AuditEvent;

public interface ClickhouseErrorHandler {
    void onInsertFailed(AuditEvent audit, Throwable throwable);
}
