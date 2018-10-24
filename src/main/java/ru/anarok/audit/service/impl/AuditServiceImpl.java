package ru.anarok.audit.service.impl;

import ru.anarok.audit.dao.Dao;
import ru.anarok.audit.domain.AuditEvent;
import ru.anarok.audit.service.IAuditService;

public class AuditServiceImpl implements IAuditService {
    private Dao<AuditEvent, Integer> auditEventDao; //Create clickhouse dao

    @Override
    public void audit(AuditEvent auditEvent) {
        auditEventDao.save(auditEvent);
    }
}
