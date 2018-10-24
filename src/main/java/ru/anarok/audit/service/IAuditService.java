package ru.anarok.audit.service;

import ru.anarok.audit.domain.AuditEvent;

public interface IAuditService {
    void audit(AuditEvent auditEvent);
}
