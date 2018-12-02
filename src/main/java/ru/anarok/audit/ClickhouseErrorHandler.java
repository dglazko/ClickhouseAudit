package ru.anarok.audit;

public interface ClickhouseErrorHandler {
    void onInsertFailed(AuditEvent audit, Throwable throwable);
}
